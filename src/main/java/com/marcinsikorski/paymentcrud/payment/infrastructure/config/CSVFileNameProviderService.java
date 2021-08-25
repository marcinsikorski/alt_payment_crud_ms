package com.marcinsikorski.paymentcrud.payment.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CSVFileNameProviderService {

    @Value("${com.marcinsikorski.paymentcrud.csv.tableFileName}")
    private String SAMPLE_CSV_FILE;

    public String getCsvTableFileName(){
        return SAMPLE_CSV_FILE;
    }

    public String getTempCsvFileName(){
        return SAMPLE_CSV_FILE + "_TMP";
    }
}
