package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/user/auth")
@RestController
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user.",
            description = "This API is used to register a new user.")
    public ResponseEntity<ResponsePayload> register(@RequestBody CreateUserDto dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.register(dto)))
                .build()
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate an existing user.",
            description = "This API is used to authenticate an existing user.")
    public ResponseEntity<ResponsePayload> login(@RequestBody LoginDTO dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.login(dto)))
                .build()
        );
    }


}
