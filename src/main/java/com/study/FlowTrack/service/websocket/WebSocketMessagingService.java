package com.study.FlowTrack.service.websocket;

import com.study.FlowTrack.payload.task.TaskResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketMessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTaskStatusChange(Long taskId, TaskResponseDto updatedTaskDto) {
        String destination = "/topic/tasks/" + taskId;

        messagingTemplate.convertAndSend(destination, updatedTaskDto);

       log.info("Sent WebSocket message to destination: {}", destination);
    }
}