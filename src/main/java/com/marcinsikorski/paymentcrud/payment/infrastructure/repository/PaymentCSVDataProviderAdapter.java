package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.marcinsikorski.paymentcrud.payment.domain.PaymentDTO;
import com.marcinsikorski.paymentcrud.payment.domain.PaymentDataProvider;
import com.opencsv.bean.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                        .withMappingStrategy(getMappingStrategy())
                        .build();
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
        return null;
    }

    @Override
    public void delete(PaymentDTO paymentDTO){
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
