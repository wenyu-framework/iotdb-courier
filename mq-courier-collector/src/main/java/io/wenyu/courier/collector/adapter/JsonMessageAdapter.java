package io.wenyu.courier.collector.adapter;

import io.wenyu.courier.collector.model.Response;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/accept")
public class JsonMessageAdapter {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private KafkaTemplate kafkaTemplate;

    @Value("${using-topic}")
    private String topic;

    @Value("${using-queue}")
    private String usingQueue;

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Response receiveMessage(@RequestBody String payload, @RequestParam String series, @RequestParam(required = false) String key) {
        if ("rocketmq".equals(usingQueue)) {
            return sendMessageRMQ(key, series, payload);
        } else if ("kafka".equals(usingQueue)) {
            return sendMessageKafka(key, series, payload);
        }
        throw new RuntimeException(String.format("'%s' is not supported now.", usingQueue));
    }

    private Response sendMessageKafka(String key, String series, String payload) {
        ListenableFuture future = null;
        if(null != key && !"".equals(key)) {
            future = kafkaTemplate.send(topic, new StringJoiner(":").add(series).add(key).toString(), payload);
        } else {
            future = kafkaTemplate.send(topic, series, payload);
        }
        try {
            future.get(6, TimeUnit.SECONDS);
            return Response.ok();
        } catch (Exception e) {
            return Response.serverError(String.format("send message fail : %s", e.getLocalizedMessage()));
        }
    }

    private Response sendMessageRMQ(String key, String series, String payload) {
        Message message = new Message(topic, series, payload.getBytes());
        if(null != key && !"".equals(key)) {
            message.setKeys(key);
        }
        SendResult sendResult = null;
        try {
            sendResult = rocketMQTemplate.getProducer().send(message, 6000);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                return Response.ok();
            } else {
                return Response.clientError(String.format("send message fail of status: %s", sendResult.getSendStatus().toString()));
            }
        } catch (Exception e) {
            return Response.serverError(String.format("send message fail : %s", e.getLocalizedMessage()));
        }
    }
}
