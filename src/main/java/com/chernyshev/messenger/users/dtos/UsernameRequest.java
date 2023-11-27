package com.chernyshev.messenger.users.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsernameRequest {
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    String username;
}
