package com.chernyshev.messenger;

import com.chernyshev.messenger.services.FriendService;
import com.chernyshev.messenger.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql"},executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/clear.sql"},executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FriendControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendService friendService;

    @Test
    @WithUserDetails("test1234")
    public void getFriendsTest() throws Exception {
        mockMvc.perform(get("/api/v1/friends"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails("test1234")
    public void getUserFriendsTest() throws Exception{
        var user = userRepository.findByUsername("test3456").orElse(null);
        assert user!=null;
        mockMvc.perform(get("/api/v1/friends/"+user.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails("test1234")
    public void getUserFriendsFromPrivateProfileTest() throws Exception{
        var user = userRepository.findByUsername("test2345").orElse(null);
        assert user!=null;
        mockMvc.perform(get("/api/v1/friends/"+user.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
    @Test
    @WithUserDetails("test3456")
    public void getUserFriendsFromPrivateProfileWhenAreFriendsTest() throws Exception{
        var user1 = userRepository.findByUsername("test3456").orElse(null);
        var user2 = userRepository.findByUsername("test2345").orElse(null);
        assert user2!=null&&user1!=null;
        friendService.sendFriendRequest(user1.getUsername(),user2.getId());
        friendService.sendFriendRequest(user2.getUsername(),user1.getId());
        mockMvc.perform(get("/api/v1/friends/"+user2.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails("test1234")
    public void postSendFriendRequestTest() throws Exception {
        var user = userRepository.findByUsername("test2345").orElse(null);
        assert user!=null;
        mockMvc.perform(post("/api/v1/friends/"+user.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails("test1234")
    public void deleteMessageHistory() throws Exception{
        var user = userRepository.findByUsername("test3456").orElse(null);
        assert user!=null;
        mockMvc.perform(post("/api/v1/friends/"+user.getId()))
                .andDo(print());
        mockMvc.perform(delete("/api/v1/friends/"+user.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
