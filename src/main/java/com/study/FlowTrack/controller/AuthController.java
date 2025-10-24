package com.study.FlowTrack.controller;

import com.study.FlowTrack.config.security.jwt.JwtTokenProvider;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.auth.AuthResponseDto;
import com.study.FlowTrack.payload.auth.LoginDto;
import com.study.FlowTrack.payload.auth.RegistrationDto;
import com.study.FlowTrack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @GetMapping({"", "/"})
    public ResponseEntity<String> authRoot() {
        return new ResponseEntity<>("Auth API is ready. Use /register or /login with POST method.", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationDto registrationDto) {
        User userToRegister = new User();
        userToRegister.setUserName(registrationDto.getUsername());
        userToRegister.setPassword(registrationDto.getPassword());

        authService.registerUser(userToRegister);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }

}
