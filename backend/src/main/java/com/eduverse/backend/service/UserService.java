package com.eduverse.backend.service;

import com.eduverse.backend.Entity.User;
import com.eduverse.backend.dto.LoginRequestDTO;
import com.eduverse.backend.dto.UserRequestDTO;
import com.eduverse.backend.dto.UserResponseDTO;
import com.eduverse.backend.exception.DuplicateResourceException;
import com.eduverse.backend.exception.InvalidCredentialsException;
import com.eduverse.backend.repository.UserRepository;
import com.eduverse.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    // 1. Register a user with clear field mapping
    public UserResponseDTO registerUser(UserRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Account email is already registered inside our system");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        User savedUser = userRepository.save(user);
        log.info("action=register_user userId={} role={}", savedUser.getId(), savedUser.getRole());
        return convertToResponseDTO(savedUser);
    }

    // 2. Get all users using a simple for-each loop instead of streams
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> dtos = new ArrayList<>();

        for (User user : users) {
            UserResponseDTO dto = convertToResponseDTO(user);
            dtos.add(dto);
        }

        return dtos;
    }

    // 3. Authenticate user and issue token
    public String loginUser(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("action=login_failed email={}", dto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password credentials");
        }

        log.info("action=login_success userId={}", user.getId());
        return tokenProvider.generateToken(user.getEmail(), user.getRole());
    }

    // Helper: Map Entity properties directly to Response DTO
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}
