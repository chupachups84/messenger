package com.chernyshev.messenger;

import com.chernyshev.messenger.controllers.AuthenticationController;
import com.chernyshev.messenger.dtos.LoginDto;
import com.chernyshev.messenger.dtos.RegisterDto;
import com.chernyshev.messenger.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = {"/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void registerTest() throws Exception {

        mockMvc.perform(
                        post(AuthenticationController.REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                RegisterDto.builder()
                                                        .firstname("test4")
                                                        .lastname("test4")
                                                        .email("test4@gmail.com")
                                                        .username("test4567")
                                                        .password("test4567")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()));
    }

    @Test
    public void registerUsernameAlreadyExistTest() throws Exception {

        mockMvc.perform(
                        post(AuthenticationController.REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                RegisterDto.builder()
                                                        .firstname("test4")
                                                        .lastname("test4")
                                                        .email("test4@gmail.com")
                                                        .username("test3456")
                                                        .password("test4567")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(
                        jsonPath("$.error_description", is(String.format(UserService.USER_EXIST, "test3456")))
                );
    }

    @Test
    public void registerEmailAlreadyExistTest() throws Exception {

        mockMvc.perform(
                        post(AuthenticationController.REGISTER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                RegisterDto.builder()
                                                        .firstname("test4")
                                                        .lastname("test4")
                                                        .email("test3@gmail.com")
                                                        .username("test4567")
                                                        .password("test4567")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(
                        jsonPath("$.error_description", is(String.format(UserService.EMAIL_EXIST, "test3@gmail.com")))
                );
    }

    @Test
    public void loginTest() throws Exception {
        mockMvc.perform(
                        post(AuthenticationController.LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                LoginDto.builder()
                                                        .username("test1234")
                                                        .password("test1234")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()));
    }

    @Test
    public void loginBadCredentialsTest() throws Exception {
        mockMvc.perform(
                        post(AuthenticationController.LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                LoginDto.builder()
                                                        .username("test1234")
                                                        .password("test12345")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.error_description", notNullValue()));
    }

    @Test
    public void loginUserNodFoundTest() throws Exception {
        mockMvc.perform(
                        post(AuthenticationController.LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                LoginDto.builder()
                                                        .username("test12345")
                                                        .password("test1234")
                                                        .build()
                                        )
                                )
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.error_description", notNullValue()));
    }

    @Test
    public void logoutTest() throws Exception {
        MvcResult result = mockMvc.perform(
                post(AuthenticationController.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                        LoginDto.builder()
                                                .username("test1234")
                                                .password("test1234")
                                                .build()
                                )
                        )
        ).andReturn();
        mockMvc.perform(post(AuthenticationController.LOGOUT)
                        .header("Authorization",
                                "Bearer " + JsonPath
                                        .read(result.getResponse().getContentAsString(), "$.access_token")
                        )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(UserService.LOGOUT_SUCCESS)));
    }

    @Test
    public void confirmEmailTokenTest() throws Exception {
        mockMvc
                .perform(
                        get(
                                AuthenticationController.EMAIL_CONFIRMATION +
                                        "?confirmationToken=some_valid_email_token"
                        )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(UserService.EMAIL_CONFIRM_SUCCESS)));

    }

    @Test
    public void confirmInvalidEmailTokenTest() throws Exception {
        mockMvc
                .perform(
                        get(
                                AuthenticationController.EMAIL_CONFIRMATION +
                                        "?confirmationToken=some_invalid_email_token"
                        )
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error_description", is(UserService.INVALID_CONFIRM_TOKEN)));
    }

    @Test
    public void refreshTokenTest() throws Exception {
        MvcResult result = mockMvc.perform(
                        post(AuthenticationController.LOGIN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                LoginDto.builder()
                                                        .username("test1234")
                                                        .password("test1234")
                                                        .build()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        mockMvc.perform(put(AuthenticationController.REFRESH_TOKEN)
                        .header("Authorization",
                                "Bearer " + JsonPath
                                        .read(result.getResponse().getContentAsString(), "$.refresh_token")
                        )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()));
    }

    @Test
    public void refreshInvalidTokenTest() throws Exception {
        mockMvc.perform(put(AuthenticationController.REFRESH_TOKEN)
                        .header("Authorization",
                                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MTI" +
                                        "zNCIsImlhdCI6MTcwNTYxNDcyMywiZXhwIjoxNzA1NzAxMTIzfQ." +
                                        "FjInW_Dpc_WgD6ugf904XWHD9_7RW2G2oQYS1gyMHXQ")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.error_description", is(UserService.INVALID_JWT)));
    }


}
