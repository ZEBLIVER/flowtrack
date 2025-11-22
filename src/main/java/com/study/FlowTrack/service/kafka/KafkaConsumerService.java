package com.study.FlowTrack.service.kafka;

import com.study.FlowTrack.event.TaskStatusChangedEvent;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.service.TaskService;
import com.study.FlowTrack.service.websocket.WebSocketMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    private final TaskService taskService;
    private final WebSocketMessagingService webSocketMessagingService;

    public KafkaConsumerService(TaskService taskService, WebSocketMessagingService webSocketMessagingService) {
        this.taskService = taskService;
        this.webSocketMessagingService = webSocketMessagingService;
    }

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

        try {
            TaskResponseDto updatedTaskDto = taskService.getTaskById(event.getTaskId());

            webSocketMessagingService.notifyTaskStatusChange(event.getTaskId(), updatedTaskDto);

            log.info("-> [WEBSOCKET] Уведомление о смене статуса задачи ID {} отправлено.", event.getTaskId());

        } catch (Exception e) {
            log.error("Ошибка при обработке Kafka-события или отправке WebSocket уведомления для задачи ID {}: {}",
                    event.getTaskId(), e.getMessage());
        }

    }
}
