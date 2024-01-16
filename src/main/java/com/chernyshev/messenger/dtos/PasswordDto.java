package com.chernyshev.messenger.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDto {
    @JsonProperty(value = "old_password")
    private String oldPassword;
    @JsonProperty(value = "new_password")
    private String newPassword;
    @JsonProperty(value = "confirm_password")
    private String confirmPassword;
}
