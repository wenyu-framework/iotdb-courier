package io.wenyu.courier.emitter.listener;

import io.wenyu.courier.emitter.model.Message;
import io.wenyu.courier.emitter.service.IoTDBService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(name = "using-queue", havingValue = "rocketmq")
@RocketMQMessageListener(topic = "${using-topic}", consumerGroup = "${rocketmq.consumer.group}", consumeMode = ConsumeMode.ORDERLY)
public class RocketMQConsumerListener implements RocketMQListener<Object>, RocketMQPushConsumerLifecycleListener {

    @Resource
    IoTDBService ioTDBService;

    @Override
    public void onMessage(Object messages) {
        // 保留此空实现方法，原因是rocketmq-springboot-starter不支持批量调用
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setPullInterval(1000);
        defaultMQPushConsumer.setConsumeThreadMin(2);
        defaultMQPushConsumer.setConsumeThreadMax(16);
        defaultMQPushConsumer.setPullBatchSize(100);
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(100);
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                List<Message> ms = messages.stream().map(msg -> new Message(0, msg.getQueueOffset(), msg.getKeys(), msg.getTags(),
                new String(msg.getBody()), null)).collect(Collectors.toList());
                if(ioTDBService.insert(ms)) {
                    // 消费成功的返回结果
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                // 消费异常时的返回结果
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
    }
}
