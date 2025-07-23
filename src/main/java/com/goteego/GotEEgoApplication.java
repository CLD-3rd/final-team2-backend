package com.goteego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GotEEgoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GotEEgoApplication.class, args);
    }

}
