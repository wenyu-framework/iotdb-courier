package io.wenyu.courier.emitter;

import io.wenyu.courier.emitter.config.IoTDBConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableConfigurationProperties(IoTDBConfig.class)
@SpringBootApplication
public class MQCourierEmitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MQCourierEmitterApplication.class, args);
    }
}
