package com.chernyshev.messenger;

import com.chernyshev.messenger.messages.dtos.MessageRequest;
import com.chernyshev.messenger.users.dtos.*;
import com.chernyshev.messenger.users.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql"},executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/clear.sql"},executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ValidationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Test
    public void invalidRegisterLastnameTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("firstname")
                .lastname("")
                .email("email@mail.ru")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidRegisterFirstnameTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("")
                .lastname("lastname")
                .email("email@mail.ru")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidRegisterEmailNotAnEmailTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("firstname")
                .lastname("lastname")
                .email("email")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidRegisterEmailIsEmptyTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("firstname")
                .lastname("lastname")
                .email("")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidRegisterPasswordLessThen8SymbolsTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("firstname")
                .lastname("lastname")
                .email("email@mail.ru")
                .password("test12")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidRegisterPasswordIsBlankTest() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .firstname("firstname")
                .lastname("lastname")
                .email("email@mail.ru")
                .password("")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidLoginUsernameTest() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .username("")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void invalidLoginPasswordTest() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .username("test1234")
                .password("")
                .build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserEmailNotAnEmailTest() throws Exception{
        EmailRequest emailRequest = EmailRequest.builder()
                .email("email")
                .build();
        mockMvc.perform(post("/api/v1/user/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserEmailIsEmptyTest() throws Exception{
        EmailRequest emailRequest = EmailRequest.builder()
                .email("")
                .build();
        mockMvc.perform(post("/api/v1/user/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserUsernameTest() throws Exception {
        UsernameRequest usernameRequest = UsernameRequest.builder()
                .username("")
                .build();
        mockMvc.perform(post("/api/v1/user/change-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(usernameRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserOldPasswordLessThen8SymbolsTest() throws Exception{
        PasswordRequest passwordRequest = PasswordRequest.builder()
                .oldPassword("1234567")
                .newPassword("12345678")
                .build();
        mockMvc.perform(post("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserOldPasswordIsBlankTest() throws Exception{
        PasswordRequest passwordRequest = PasswordRequest.builder()
                .newPassword("12345678")
                .build();
        mockMvc.perform(post("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserNewPasswordLessThen8SymbolsTest() throws Exception{
        PasswordRequest passwordRequest = PasswordRequest.builder()
                .oldPassword("12345678")
                .newPassword("1234567")
                .build();
        mockMvc.perform(post("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidUserNewPasswordIsBlankTest() throws Exception{
        PasswordRequest passwordRequest = PasswordRequest.builder()
                .oldPassword("12345678")
                .build();
        mockMvc.perform(post("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails("test1234")
    public void invalidMessageTest() throws Exception{
        var user = userRepository.findByUsername("test3456").orElse(null);
        assert user!=null;
        MessageRequest messageRequest = MessageRequest.builder()
                .text("")
                .build();
        mockMvc.perform(post("/api/v1/messages/"+user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest());
    }

}
