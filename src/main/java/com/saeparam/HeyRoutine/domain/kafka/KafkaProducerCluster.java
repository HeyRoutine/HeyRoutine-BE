package com.saeparam.HeyRoutine.domain.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerCluster {

    private final KafkaTemplate<String, Ranker3RequestDto> kafkaTemplate;

    @Value("${spring.kafka.producer.topic3}")
    private String topicName;

    public void sendMessage(Ranker3RequestDto ranker3RequestDto) {

        Message<Ranker3RequestDto> message = MessageBuilder
                .withPayload(ranker3RequestDto)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .build();

        CompletableFuture<SendResult<String, Ranker3RequestDto>> future =
                kafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("producer: success >>> message: {}, offset: {}",
                        result.getProducerRecord().value().toString(), result.getRecordMetadata().offset());
            } else {
                log.info("producer: failure >>> message: {}", ex.getMessage());
            }
        });

    }

}