package com.study.FlowTrack.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TaskIdentifierConverter implements Converter<String, TaskIdentifier> {
    @Override
    public TaskIdentifier convert(String source) {

        String[] parts = source.split("-");

        String projectKey = parts[0];
        Long taskNumber;

        try {
            taskNumber = Long.valueOf(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Task number part must be a valid number.");
        }

        return new TaskIdentifier(projectKey, taskNumber);
    }
}
