package com.study.FlowTrack.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskIdentifier {
    private String projectKey;
    private Long taskNumber;

    public String getFullKey() {
        return projectKey + "-" + taskNumber;
    }

    @Override
    public String toString() {
        return getFullKey();
    }
}
