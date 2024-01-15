package com.chernyshev.messenger.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String lastname;
    private String firstname;
    private String username;
    private String email;
    private String bio;
    private String status;
    @JsonProperty(value = "avatar_url")
    private String avatarUrl;
}

