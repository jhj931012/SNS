package com.example.SNS.user;

import com.example.SNS.user.dto.request.UserLoginRequest;
import com.example.SNS.user.dto.request.UserSignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 정상 처리 테스트")
    public void signupTest() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .nickname("Tester")
                .build();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.nickname").value("Tester"))
                .andExpect(jsonPath("$.password").doesNotExist());  // 비밀번호는 응답에 포함 안됨
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    public void signupFail_DuplicateUsername() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .nickname("Tester")
                .build();

        // 먼저 정상 회원가입
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 중복 회원가입 시도
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 아이디입니다."));
    }


    @Test
    @DisplayName("로그인 정상 처리 테스트")
    public void loginTest() throws Exception {
        // 먼저 회원가입 진행
        UserSignupRequest signupRequest = UserSignupRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .nickname("Tester")
                .build();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 로그인 요청 DTO
        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())  // 토큰 존재 여부 체크
                .andExpect(jsonPath("$.accessToken").isString());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    public void loginFail_UserNotFound() throws Exception {
        // 회원가입은 일부러 안 함 (아이디 없는 상태)

        // 존재하지 않는 아이디로 로그인 시도
        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username("nonexistentuser")
                .password("anyPassword")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인 실패: 아이디 또는 비밀번호를 확인하세요"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    public void loginFail_WrongPassword() throws Exception {
        // 회원가입
        UserSignupRequest signupRequest = UserSignupRequest.builder()
                .username("loginuser")
                .email("loginuser@example.com")
                .password("correctpassword")
                .nickname("Tester")
                .build();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 틀린 비밀번호로 로그인 시도
        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username("loginuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인 실패: 아이디 또는 비밀번호를 확인하세요"));
    }

    @Test
    @DisplayName("토큰 기반 현재 사용자 정보 조회 테스트")
    @WithMockUser(username = "test")  // 인증된 유저로 흉내
    public void getCurrentUserInfoTest() throws Exception {
        mockMvc.perform(get("/api/users/userInfo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"));
    }


}
