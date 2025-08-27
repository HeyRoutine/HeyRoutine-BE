package com.saeparam.HeyRoutine.global.config;

import com.saeparam.HeyRoutine.domain.kafka.Ranker3RequestDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfigCluster {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    
    @Value("${spring.kafka.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Ranker3RequestDto> pushEntityConsumerFactory() {
        JsonDeserializer<Ranker3RequestDto> deserializer = gcmPushEntityJsonDeserializer();
        return new DefaultKafkaConsumerFactory<>(
                consumerFactoryConfig(deserializer),
                new StringDeserializer(),
                deserializer);
    }

    private Map<String, Object> consumerFactoryConfig(JsonDeserializer<Ranker3RequestDto> deserializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        return props;
    }

    private JsonDeserializer<Ranker3RequestDto> gcmPushEntityJsonDeserializer() {
        JsonDeserializer<Ranker3RequestDto> deserializer = new JsonDeserializer<>(Ranker3RequestDto.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);
        return deserializer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Ranker3RequestDto>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Ranker3RequestDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(pushEntityConsumerFactory());
        return factory;
    }
    
}