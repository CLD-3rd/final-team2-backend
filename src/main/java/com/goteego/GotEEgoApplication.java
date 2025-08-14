package com.goteego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class )
@EnableJpaAuditing
public class GotEEgoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GotEEgoApplication.class, args);
    }

}
