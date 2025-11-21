package com.study.FlowTrack.service.kafka;

import com.study.FlowTrack.event.TaskStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(
            topics = "${kafka.topic.task-status}",
            groupId = "flowtrack-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTaskStatusChange(TaskStatusChangedEvent event) {
        log.info("<- [KAFKA CONSUMER] Получено событие. Задача ID: {}, Статус изменен с '{}' на '{}'",
                event.getTaskId(),
                event.getOldStatus(),
                event.getNewStatus());

        // --- Здесь начинается логика WebSockets  ---
        // 1. Найти всех пользователей проекта (по event.getProjectId())
        // 2. Отправить им уведомление через Spring WebSockets.

    }
}
