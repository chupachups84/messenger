//package com.chernyshev.messenger.api.controllers;
//
//import com.chernyshev.messenger.api.dtos.MessageRequest;
//import com.chernyshev.messenger.api.dtos.MessageResponse;
//import com.chernyshev.messenger.api.services.MessageService;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/messages")
//@RequiredArgsConstructor
//@SecurityRequirement(name = "bearerAuth")
//@Tag(name = "Messages",description = "MessagesAPI")
//public class MessageController {
//
//    private final MessageService messageService;
//
//    @PostMapping("/{id}")
//    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest messageRequest, @PathVariable Long id, Principal principal)  {
//        messageService.sendMessage(principal.getName(), id,messageRequest.getText());
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<List<MessageResponse>> getMessageHistory(@PathVariable Long id, Principal principal) {
//        List<MessageResponse> responses=messageService.getMessageHistory(principal.getName(),id);
//        return ResponseEntity.ok().body(responses);
//    }
//
//    @DeleteMapping("/{id}")
//    public  void deleteMessageHistory(@PathVariable Long id , Principal principal) {
//        messageService.deleteMessageHistory(principal.getName(),id);
//    }
//}
