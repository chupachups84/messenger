package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.ErrorDto;
import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.dtos.TextMessageDto;
import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Message")
public class MessageController {
    private final MessageService messageService;
    public static final String MESSAGE = "/api/v1/messages/{username}";

    @Operation(summary = "Return message message history", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    @GetMapping(MESSAGE)
    public ResponseEntity<List<MessageDto>> getMessageHistory(@PathVariable String username, Principal principal) {
        return ResponseEntity.ok().body(messageService.getMessageHistory(principal.getName(), username));
    }

    @Operation(summary = "Send text to user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                    )
            )

    })
    @PostMapping(MESSAGE)
    public ResponseEntity<TextMessageDto> sendMessage(
            Principal principal, @PathVariable String username, @RequestBody TextMessageDto text
    ) {
        return ResponseEntity.ok().body(
                TextMessageDto.builder().text(
                        messageService.sendMessage(principal.getName(), username, text)
                ).build()
        );
    }
}
