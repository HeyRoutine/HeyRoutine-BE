package com.saeparam.HeyRoutine.domain.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumerCluster {

    @KafkaListener(topics = "${spring.kafka.producer.topic3}", groupId = "${spring.kafka.group-id}")
    public void consume(@Payload Ranker3RequestDto message,
                        @Headers MessageHeaders messageHeaders) {

        // 받은 객체를 가지고 로직 수행

        log.info("consumer: success >>> message: {}, headers: {}", message.toString(), messageHeaders);
    }
}