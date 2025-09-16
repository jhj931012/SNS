package com.example.SNS.user.controller;

import com.example.SNS.user.dto.request.UserLoginRequest;
import com.example.SNS.user.dto.request.UserSignupRequest;
import com.example.SNS.user.dto.response.UserLoginResponse;
import com.example.SNS.user.dto.response.UserResponse;
import com.example.SNS.user.entity.UserEntity;
import com.example.SNS.user.service.AuthService;
import com.example.SNS.user.service.CustomUserDetailsService;
import com.example.SNS.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthService authService;

    private final CustomUserDetailsService customUserDetailsService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    // 아이디로 회원 조회 (예시)
    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new UserLoginResponse(token));
    }

    // 로그인한 유저 정보 가져오기
    @GetMapping("/userInfo")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        // username으로 DB 조회
        UserEntity userEntity = customUserDetailsService.loadUserEntityByUsername(user.getUsername());

        UserResponse response = UserResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .bio(userEntity.getBio())
                .build();

        return ResponseEntity.ok(response);
    }

}
