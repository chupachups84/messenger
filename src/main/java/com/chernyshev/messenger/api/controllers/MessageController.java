package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.MessageRequest;
import com.chernyshev.messenger.api.dtos.MessageResponse;
import com.chernyshev.messenger.api.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Messages",description = "MessagesAPI")
public class MessageController {

    private final MessageService messageService;
    @Operation(summary = "Отправляет сообщение")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageRequest.class),
                    examples ={
                            @ExampleObject(
                                    name = "messageRequest",
                                    value  = "{" + "\"text\":\"Hello Swagger!\"" + "}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Сообщение отправлено",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Проблема с входными данными",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден/Нельзя отправить сообщение пользователю",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @PostMapping("/{id}")
    public ResponseEntity<String> sendMessage(@RequestBody @Valid MessageRequest messageRequest, @PathVariable Long id, Principal principal)  {
        messageService.sendMessage(principal.getName(), id,messageRequest.getText());
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Показывает историю сообщений с пользователем")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = {
                            @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = MessageResponse.class
                                    )
                            )
                        )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Проблема с входными данными",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<List<MessageResponse>> getMessageHistory(@PathVariable Long id, Principal principal) {
        List<MessageResponse> responses=messageService.getMessageHistory(principal.getName(),id);
        return ResponseEntity.ok().body(responses);
    }
    @Operation(summary = "Удаляет историю сообщений с пользователем")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Проблема с входными данными",
                    content = {@Content(mediaType = "text/plain")}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @DeleteMapping("/{id}")
    public  void deleteMessageHistory(@PathVariable Long id , Principal principal) {
        messageService.deleteMessageHistory(principal.getName(),id);
    }
}
