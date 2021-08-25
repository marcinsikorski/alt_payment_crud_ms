package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentCSVDataProviderAdapter implements PaymentDataProvider {

    private final PaymentCSVSequenceService paymentCSVSequenceService;
    private static final String SAMPLE_CSV_FILE = "./payments.csv";
    private static final String TEMP_CSV_FILE = SAMPLE_CSV_FILE + "_TMP";

    private final Object lock = new Object();

    @Override
    public Optional<PaymentDTO> findById(Long paymentId){
        Long maxLong = 0L;
        synchronized (lock){
            try(Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE));){
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
            }
        }
    }

    @Override
    public List<PaymentDTO> findAllByUserId(Long userId){
        return null;
    }

    @Override
    public PaymentDTO save(PaymentDTO paymentDTO){
        synchronized (lock){
            try(Writer writer = new FileWriter(SAMPLE_CSV_FILE, true);){
                StatefulBeanToCsv beanToCsv = getBeanToCsv(writer);
                PaymentCSVRecord paymentCSVRecord = DTOtoCSVRecord(paymentDTO);
                paymentCSVRecord.setPaymentId(this.paymentCSVSequenceService.getNextSequenceValue());
                beanToCsv.write(paymentCSVRecord);
                PaymentDTO savedPayments = recordToDTO(paymentCSVRecord);
                return savedPayments;
            } catch (Exception e){
                log.error("Failed csv save", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed csv save");
            }
        }
    }

    @Override
    public PaymentDTO update(PaymentDTO paymentDTO){
        boolean foundFlag = false;
        PaymentCSVRecord paymentToUpdate = DTOtoCSVRecord(paymentDTO);
        synchronized (lock) {
            try(Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE));
                Writer writer = new FileWriter(TEMP_CSV_FILE, true);
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
                replaceFiles(SAMPLE_CSV_FILE, TEMP_CSV_FILE);
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
            }
        }

    }

    @Override
    public void delete(PaymentDTO paymentDTO){
        boolean foundFlag = false;
        PaymentCSVRecord paymentToUpdate = DTOtoCSVRecord(paymentDTO);
        synchronized (lock) {
            try(Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE));
                Writer writer = new FileWriter(TEMP_CSV_FILE, true);
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
                replaceFiles(SAMPLE_CSV_FILE, TEMP_CSV_FILE);
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
            }
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
