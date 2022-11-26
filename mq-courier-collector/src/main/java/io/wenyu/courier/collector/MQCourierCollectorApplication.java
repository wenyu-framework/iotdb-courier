package io.wenyu.courier.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class MQCourierCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MQCourierCollectorApplication.class, args);
    }

}
