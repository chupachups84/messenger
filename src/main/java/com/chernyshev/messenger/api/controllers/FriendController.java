package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.FriendResponse;
import com.chernyshev.messenger.api.services.FriendService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @GetMapping("/friends")
    public ResponseEntity<List<FriendResponse>> getMyFriends(Principal principal) {
        List<FriendResponse> responseList = friendService.getFriends(principal.getName());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<FriendResponse>> getUserFriends(@PathVariable Long id,Principal principal) {
        List<FriendResponse> responseList = friendService.getUserFriends(id, principal.getName());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> addFriend(@PathVariable Long id, Principal principal)  {
        friendService.sendFriendRequest(principal.getName(),id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public void deleteFriend(@PathVariable Long id, Principal principal){
        friendService.deleteFriend(principal.getName(),id);
    }
}
