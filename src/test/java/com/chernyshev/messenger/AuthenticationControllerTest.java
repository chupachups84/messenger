package com.chernyshev.messenger;

import com.chernyshev.messenger.dtos.AuthenticationDto;
import com.chernyshev.messenger.dtos.RegisterDto;
import com.chernyshev.messenger.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql"},executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/clear.sql"},executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Test
    public void postRegisterTest() throws Exception {
        RegisterDto registerDTO = RegisterDto.builder()
                .firstname("test")
                .lastname("test")
                .email("shpota.den@mail.ru")
                .username("test")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    public void postRegisterUsernameAlreadyExistTest() throws Exception {
        RegisterDto registerDTO = RegisterDto.builder()
                .firstname("test")
                .lastname("test")
                .email("test@gmail.com")
                .username("test1234")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    @Test
    public void postRegisterEmailAlreadyExistTest() throws Exception {
        RegisterDto registerDTO = RegisterDto.builder()
                .firstname("test")
                .lastname("test")
                .email("test1@gmail.com")
                .username("test")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registerDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    @Test
    public void postLoginTest() throws Exception{
        AuthenticationDto authenticationDTO = AuthenticationDto.builder()
                .username("test1234")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authenticationDTO)))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    public void postLoginUsernameNotFoundTest() throws Exception{
        AuthenticationDto authenticationDTO = AuthenticationDto.builder()
                .username("test1234")
                .password("test12345")
                .build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authenticationDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    @WithUserDetails("test1234")
    public void postLoginUserDeactivatedTest() throws Exception{
        mockMvc.perform(delete("/api/v1/user/delete"));
        AuthenticationDto authenticationDTO = AuthenticationDto.builder()
                .username("test1234")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authenticationDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    public void getEmailConfirmationTokenTest() throws Exception {
        RegisterDto registerDTO = RegisterDto.builder()
                .firstname("test")
                .lastname("test")
                .email("test@mail.ru")
                .username("test")
                .password("test1234")
                .build();
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(registerDTO)));
        var user = userRepository.findByUsername("test").orElse(null);
        assert user!=null;
        String token = user.getEmailConfirmationToken();
        System.out.println("\n\n\nToken: = "+token+"\n\n\n");
        mockMvc.perform(post("/api/v1/auth/confirm/"+token))
                .andDo(print())
                .andExpect(status().isOk());
        user = userRepository.findByUsername("test").orElse(null);
        assert user!=null;
        token = user.getEmailConfirmationToken();
        System.out.println("\n\n\nToken: = "+token+"\n\n\n");
    }
    @Test
    @WithUserDetails("test1234")
    public void getWrongEmailConfirmationTokenTest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/confirm/wrong_token"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    public void postLogoutTest() throws Exception {
        AuthenticationDto authenticationDTO = AuthenticationDto.builder()
                .username("test1234")
                .password("test1234")
                .build();
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(authenticationDTO))).andReturn();
        String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
        System.out.println("\n\n\nAuth = "+SecurityContextHolder.getContext()+"\n\n\n");
    }
    @Test
    public void postLogoutWithNoAuthTest() throws Exception {
        System.out.println("\n\n\nAuth = "+SecurityContextHolder.getContext().getAuthentication());
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isForbidden());
        System.out.println("\n\n\nAuth = "+SecurityContextHolder.getContext()+"\n\n\n");
    }
}
