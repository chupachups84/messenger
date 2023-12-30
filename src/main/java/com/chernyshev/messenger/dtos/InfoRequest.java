package com.chernyshev.messenger.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfoRequest {
    @Size(min = 1,message = "Фамилия не может быть пустой")
    private String lastname;
    @Size(min = 1,message = "Имя не может быть пустым")
    private String firstname;
    @Size(min = 1,message = "Био не может быть пустым")
    private String bio;
    @Size(min = 1,message = "Статус не может быть пустым")
    private String status;
    @Size(min = 1,message = "Ссылка на аватарку не может быть пустой")
    private String avatarUrl;
}
