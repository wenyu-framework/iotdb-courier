package io.wenyu.courier.emitter.listener;

import io.wenyu.courier.emitter.model.Message;
import io.wenyu.courier.emitter.service.IoTDBService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "using-queue", havingValue = "kafka")
public class KafkaConsumerListener {

    @Resource
    IoTDBService ioTDBService;

    @KafkaListener(topics = {"${using-topic}"})
    public void emitter(ConsumerRecords<String, String> consumerRecords, Acknowledgment acknowledgment) {
        List<Message> messages = new ArrayList<>();
        for (ConsumerRecord<String, String> record : consumerRecords) {
            messages.add(new Message(record.partition(), record.offset(), record.key(), null, record.value(), null));
        }
        if (ioTDBService.insert(messages)) {
            acknowledgment.acknowledge();
        }
    }
}
