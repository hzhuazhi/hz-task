package com.hz.task.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class HzTaskMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HzTaskMasterApplication.class, args);
    }

}

