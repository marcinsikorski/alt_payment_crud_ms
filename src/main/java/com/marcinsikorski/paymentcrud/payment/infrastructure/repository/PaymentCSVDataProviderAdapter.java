package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.marcinsikorski.paymentcrud.payment.infrastructure.config.CSVFileNameProviderService;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentCSVDataProviderAdapter implements PaymentDataProvider {

    private final PaymentCSVSequenceService paymentCSVSequenceService;
    private final CSVFileNameProviderService csvName;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public Optional<PaymentDTO> findById(Long paymentId){
        Long maxLong = 0L;
        readWriteLock.readLock().lock();
        try(Reader reader = Files.newBufferedReader(Paths.get(this.csvName.getCsvTableFileName()));){
            CsvToBean csvToBean = getCsvToBean(reader);
            for (Object object : csvToBean) {
                PaymentCSVRecord paymentCSVRecord = (PaymentCSVRecord) object;
                if(paymentCSVRecord.getPaymentId().equals(paymentId)){
                    return Optional.of(recordToDTO(paymentCSVRecord));
                }
            }
            return Optional.empty();
        } catch (NoSuchFileException e){
            log.info("Csv db is empty. Returns empty Optional");
            return Optional.empty();
        } catch (IOException e){
            log.info("Cannot read csv file", e);
            throw new RuntimeException("Cannot read given csv file.");
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public List<PaymentDTO> findAll(){
        return findByPredicate(p -> true);
    }

    @Override
    public List<PaymentDTO> findAllByUserId(Long userId){
        return findByPredicate(p -> p.getUserId() !=null && p.getUserId().equals(userId));
    }

    @Override
    public List<PaymentDTO> findAllByCurrency(Currency currency){
        return findByPredicate(p -> p.getCurrency() !=null && p.getCurrency().equals(currency));
    }

    @Override
    public List<PaymentDTO> findAllByUserIdAndCurrency(Long userId, Currency currency){
        return findByPredicate(p -> (p.getUserId() !=null && p.getUserId().equals(userId)) &&
                        (p.getCurrency() !=null && p.getCurrency().equals(currency)));
    }

    @Override
    public PaymentDTO save(PaymentDTO paymentDTO){
        readWriteLock.writeLock().lock();
        try(Writer writer = new FileWriter(this.csvName.getCsvTableFileName(), true);){
                StatefulBeanToCsv beanToCsv = getBeanToCsv(writer);
                PaymentCSVRecord paymentCSVRecord = DTOtoCSVRecord(paymentDTO);
                paymentCSVRecord.setPaymentId(this.paymentCSVSequenceService.getNextSequenceValue());
                beanToCsv.write(paymentCSVRecord);
                PaymentDTO savedPayments = recordToDTO(paymentCSVRecord);
                return savedPayments;
        } catch (Exception e){
            log.error("Failed csv save", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed csv save");
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public PaymentDTO update(PaymentDTO paymentDTO){
        boolean foundFlag = false;
        PaymentCSVRecord paymentToUpdate = DTOtoCSVRecord(paymentDTO);
        readWriteLock.writeLock().lock();
        try(Reader reader = Files.newBufferedReader(Paths.get(this.csvName.getCsvTableFileName()));
                Writer writer = new FileWriter(this.csvName.getTempCsvFileName(), true);
            ){
            CsvToBean csvToBean = getCsvToBean(reader);
            StatefulBeanToCsv beanToCsv = getBeanToCsv(writer);
            for (Object object : csvToBean) {
                PaymentCSVRecord paymentCSVRecord = (PaymentCSVRecord) object;
                if(!paymentCSVRecord.getPaymentId().equals(paymentDTO.getPaymentId())){
                    beanToCsv.write(paymentCSVRecord);
                } else {
                    foundFlag=true;
                    beanToCsv.write(paymentToUpdate);
                }
            }
            replaceFiles(this.csvName.getCsvTableFileName(), this.csvName.getTempCsvFileName());
            if(foundFlag){
                return recordToDTO(paymentToUpdate);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment wasn't found.");
            }
        } catch (NoSuchFileException e){
            log.info("Csv db is empty. Returns empty Optional");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV Db is empty, cannot find payment");
        } catch (IOException e){
            log.info("Error on updating csv file", e);
            throw new RuntimeException("Cannot read given csv file.");
        } catch (CsvDataTypeMismatchException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV DB is populated wrong");
        } catch (CsvRequiredFieldEmptyException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV DB data constraints are not met");
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void delete(PaymentDTO paymentDTO){
        boolean foundFlag = false;
        PaymentCSVRecord paymentToUpdate = DTOtoCSVRecord(paymentDTO);
        readWriteLock.writeLock().lock();
        try(Reader reader = Files.newBufferedReader(Paths.get(this.csvName.getCsvTableFileName()));
            Writer writer = new FileWriter(this.csvName.getTempCsvFileName(), true);
            ){
            CsvToBean csvToBean = getCsvToBean(reader);
            StatefulBeanToCsv beanToCsv = getBeanToCsv(writer);
            for (Object object : csvToBean) {
                PaymentCSVRecord paymentCSVRecord = (PaymentCSVRecord) object;
                if(!paymentCSVRecord.getPaymentId().equals(paymentDTO.getPaymentId())){
                    beanToCsv.write(paymentCSVRecord);
                } else {
                    foundFlag=true;
                }
            }
            replaceFiles(this.csvName.getCsvTableFileName(), this.csvName.getTempCsvFileName());
            if(!foundFlag){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find " + paymentDTO.getPaymentId() + " payment id");
            }
        } catch (NoSuchFileException e){
                log.info("Csv db is empty. Returns empty Optional");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV Db is empty, cannot find payment");
        } catch (IOException e){
                log.info("Error on updating csv file", e);
                throw new RuntimeException("Cannot read given csv file.");
        } catch (CsvDataTypeMismatchException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV DB is populated wrong");
        } catch (CsvRequiredFieldEmptyException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV DB data constraints are not met");
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private boolean replaceFiles(String fileNameToBeReplaced, String newFileName) throws IOException{
        // File (or directory) with old name
        File olderFile = new File(fileNameToBeReplaced);
        // File (or directory) with new name
        File fileNew = new File(newFileName);
        // File (or directory) with new name
        File fileBackup = new File(olderFile + "_BKP");
        //prepare backup of csv table
        FileUtils.copyFile(olderFile, fileBackup);
        if(olderFile.delete()) {
            if (olderFile.exists()) {
                throw new java.io.IOException("Old file wasn't deleted.");
            }
            return fileNew.renameTo(olderFile);
        }
        return false;
    }

    private List<PaymentDTO> findByPredicate(Predicate<PaymentCSVRecord> predicate){
        Long maxLong = 0L;
        List<PaymentCSVRecord> matchedList = new LinkedList<>();
        readWriteLock.readLock().lock();
        try(Reader reader = Files.newBufferedReader(Paths.get(this.csvName.getCsvTableFileName()));){
            CsvToBean csvToBean = getCsvToBean(reader);
            for (Object object : csvToBean) {
                PaymentCSVRecord paymentCSVRecord = (PaymentCSVRecord) object;
                if(predicate.test(paymentCSVRecord)){
                    matchedList.add(paymentCSVRecord);
                }
            }
            return matchedList.stream()
                    .map(this::recordToDTO)
                    .collect(Collectors.toList());
        } catch (NoSuchFileException e){
            log.info("Csv db is empty. Returns empty list");
            return Collections.EMPTY_LIST;
        } catch (IOException e){
            log.info("Cannot read csv file", e);
            throw new RuntimeException("Cannot read given csv file.");
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private MappingStrategy getMappingStrategy(){
        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(PaymentCSVRecord.class);
        return mappingStrategy;
    }

    private CsvToBean getCsvToBean(Reader reader) {
        return new CsvToBeanBuilder(reader)
                .withType(PaymentCSVRecord.class)
                .withMappingStrategy(getMappingStrategy())
                .build();
    }

    private StatefulBeanToCsv getBeanToCsv(Writer writer){
        return new StatefulBeanToCsvBuilder(writer)
                .withMappingStrategy(getMappingStrategy())
                .build();
    }

    private PaymentCSVRecord DTOtoCSVRecord(PaymentDTO paymentDTO){
        return PaymentCSVRecord.builder()
                .amount(paymentDTO.getAmount())
                .paymentId(paymentDTO.getPaymentId())
                .currency(paymentDTO.getCurrency())
                .userId(paymentDTO.getUserId())
                .targetBankAccount(paymentDTO.getTargetBankAccount())
                .build();
    };

    private PaymentDTO recordToDTO(PaymentCSVRecord paymentCSVRecord){
        return PaymentDTO.builder()
                .amount(paymentCSVRecord.getAmount())
                .paymentId(paymentCSVRecord.getPaymentId())
                .currency(paymentCSVRecord.getCurrency())
                .userId(paymentCSVRecord.getUserId())
                .targetBankAccount(paymentCSVRecord.getTargetBankAccount())
                .build();
    }
}
