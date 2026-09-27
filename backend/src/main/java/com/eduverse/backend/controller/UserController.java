package com.eduverse.backend.controller;

import com.eduverse.backend.dto.LoginRequestDTO;
import com.eduverse.backend.dto.UserRequestDTO;
import com.eduverse.backend.dto.UserResponseDTO;
import com.eduverse.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    // in this we have not used @autowired we used constructor injection

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //  Create a brand new user account
    @PostMapping
    public UserResponseDTO createAccount(@Valid @RequestBody UserRequestDTO dto) {
        return userService.registerUser(dto);
    }

    // 2. Fetch a list of all user profile data
    @GetMapping
    public List<UserResponseDTO> fetchAllProfiles() {
        return userService.getAllUsers();
    }

    // 3. Process user credentials and return a token payload
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequestDTO dto) {
        String token = userService.loginUser(dto);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}
