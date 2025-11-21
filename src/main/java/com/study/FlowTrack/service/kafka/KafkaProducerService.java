package com.study.FlowTrack.service.kafka;

import com.study.FlowTrack.event.TaskStatusChangedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaProducerService {
    @Value("${kafka.topic.task-status}")
    private String taskStatusTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTaskStatusChangedEvent(TaskStatusChangedEvent event) {
        kafkaTemplate.send(taskStatusTopic, event.getTaskId().toString(), event);
        log.info("-> [KAFKA PRODUCER] Отправлено событие в топик {} для задачи ID: {}, Новый статус: {}",
                taskStatusTopic,
                event.getTaskId(),
                event.getNewStatus());
    }
}
