package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One current session per user (single device). userId is unique.
 */
@Document(collection = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private String id;
    @Indexed(unique = true)
    private String userId;
    private String currentSessionId;
}
