package com.chernyshev.messenger.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String sender;
    private String receiver;
    private String message;
    @JsonProperty(value = "sent_at")
    private Instant sentAt;

    @Override
    public String toString() {
        return "sender:" + sender + ' ' +
                "receiver:" + receiver + ' ' +
                "message:" + message + ' ' +
                "sentAt=" + sentAt +
                '}';
    }
}
