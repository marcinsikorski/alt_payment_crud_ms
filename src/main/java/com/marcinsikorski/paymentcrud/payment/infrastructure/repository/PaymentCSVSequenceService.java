package com.marcinsikorski.paymentcrud.payment.infrastructure.repository;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class PaymentCSVSequenceService {

    private static final String SAMPLE_CSV_FILE = "./payments.csv";
    private final AtomicLong atomicLong;

    public PaymentCSVSequenceService(){
        this.atomicLong = new AtomicLong(getGreatestNumberInTable());
    }
    //load sequence on first load
    public long getNextSequenceValue(){
        return atomicLong.incrementAndGet();
    }

    private synchronized long getGreatestNumberInTable(){
        ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
        ms.setType(PaymentCSVRecord.class);

        try{
            Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE));
            CsvToBean csvToBean = new CsvToBeanBuilder(reader)
                    .withType(PaymentCSVRecord.class)
                    .withMappingStrategy(ms)
                    .build();
            Long maxLong = 0L;
            for (Object object : csvToBean) {
                PaymentCSVRecord paymentCSVRecord = (PaymentCSVRecord) object;
                if(paymentCSVRecord.getPaymentId()>maxLong){
                    maxLong = paymentCSVRecord.getPaymentId();
                }
            }
            return maxLong;
        } catch (NoSuchFileException e){
            log.info("Csv db is empty. Uses starting value");
            return 0L;
        } catch (IOException e){
            log.info("Cannot read csv file", e);
            throw new RuntimeException("Cannot read given csv file. App should not start");
        }
    }


}
