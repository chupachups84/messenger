package com.chernyshev.messenger.friends.controllers;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.friends.dtos.FriendResponse;
import com.chernyshev.messenger.friends.services.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Friends",description = "FriendsApi")
public class FriendController {
    private final FriendService friendService;
    @Operation(summary = "Выводит список друзей")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = FriendResponse.class)
                                    )
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь не найден",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @GetMapping()
    public ResponseEntity<List<FriendResponse>> getMyFriends(Principal principal) {
        List<FriendResponse> responseList = friendService.getFriends(principal.getName());
        return ResponseEntity.ok(responseList);
    }
    @Operation(summary = "Выводит список друзей конкретного пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = FriendResponse.class)
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
                    description = "Пользователь не найден/Нельзя вывести друзей этого пользователя",
                    content = {@Content(mediaType = "text/plain")}
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<List<FriendResponse>> getUserFriends(@PathVariable Long id,Principal principal) {
        List<FriendResponse> responseList = friendService.getUserFriends(id, principal.getName());
        return ResponseEntity.ok(responseList);
    }
    @Operation(summary = "Отправляет заявку в друзья")
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
    @PostMapping("/{id}")
    public ResponseEntity<String> addFriend(@PathVariable Long id, Principal principal)  {
        friendService.sendFriendRequest(principal.getName(),id);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Удаляет заявку в друзья")
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
    public void deleteFriend(@PathVariable Long id, Principal principal){
        friendService.deleteFriend(principal.getName(),id);
    }
}
