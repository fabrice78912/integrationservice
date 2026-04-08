package com.demo.integrationservice.payload;


import com.demo.integrationservice.enumeration.EventType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Event {
    private String entityId;
    private EventType eventType;
    private Map<String, ?> data;
}
