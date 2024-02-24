package com.chernyshev.messenger;

import com.chernyshev.messenger.controllers.MessageController;
import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.dtos.TextMessageDto;
import com.chernyshev.messenger.services.MessageService;
import com.chernyshev.messenger.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = {"/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MessageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithUserDetails("test1234")
    public void getMessageHistoryTest() throws Exception {
        List<MessageDto> messages = new ArrayList<>();
        messages.add(
                MessageDto.builder()
                        .sender("test1234")
                        .receiver("test2345")
                        .message("Hello test2345")
                        .build()
        );
        mockMvc.perform(get(MessageController.MESSAGE.replaceAll("\\{username\\}", "test2345")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(messages)));
    }

    @Test
    public void getMessageHistoryNoUserDetailsTest() throws Exception {
        mockMvc.perform(get(MessageController.MESSAGE.replaceAll("\\{username\\}", "test2345")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("test1234")
    public void getMessageHistoryUserNotFoundTest() throws Exception {
        mockMvc.perform(get(MessageController.MESSAGE.replaceAll("\\{username\\}", "test5678")))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_description", is(UserService.NOT_FOUND_MESSAGE)));
    }

    @Test
    @WithUserDetails("test2345")
    public void sendMessageToUserTest() throws Exception {
        mockMvc
                .perform(
                        post(MessageController.MESSAGE.replaceAll("\\{username\\}", "test1234"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                TextMessageDto.builder()
                                                        .text("Hello test1234")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(MessageService.SUCCESS))
                );
    }

    @Test
    @WithUserDetails("test3456")
    public void sendMessageToUserWithFriendOnlyRecipientTest() throws Exception {
        mockMvc
                .perform(
                        post(MessageController.MESSAGE.replaceAll("\\{username\\}", "test2345"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                TextMessageDto.builder()
                                                        .text("Hello test2345")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_description", is(MessageService.FRIEND_EXC)));

    }

    @Test
    @WithUserDetails("test1234")
    public void sendMessageToUserBadRequestTest() throws Exception {
        mockMvc
                .perform(
                        post(MessageController.MESSAGE.replaceAll("\\{username\\}", "test2345"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                TextMessageDto.builder()
                                                        .text("")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_description", is(MessageService.EMPTY_MESSAGE)));
    }

    @Test
    @WithUserDetails("test1234")
    public void sendMessageToUserNotFoundExcTest() throws Exception {
        mockMvc
                .perform(
                        post(MessageController.MESSAGE.replaceAll("\\{username\\}", "test5678"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                TextMessageDto.builder()
                                                        .text("Hello test5678")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_description", is(UserService.NOT_FOUND_MESSAGE)));

    }
}
