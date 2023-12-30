package com.chernyshev.messenger.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfoResponse {
    private String lastname;
    private String firstname;
    private String username;
    private String email;
    private String bio;
    private String status;
    private String avatarUrl;
}

