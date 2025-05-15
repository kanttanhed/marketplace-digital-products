package com.github.kanttanhed.user_service.service;

import com.github.kanttanhed.user_service.dto.UserRequestDTO;
import com.github.kanttanhed.user_service.dto.UserResponseDTO;
import com.github.kanttanhed.user_service.entity.User;
import com.github.kanttanhed.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProducer userProducer;

    public UserService(UserRepository userRepository, UserProducer userProducer) {
        this.userRepository = userRepository;
        this.userProducer = userProducer;
    }

    // Method to create a new user
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        // Validate the request
        if (userRequestDTO.getName() == null || userRequestDTO.getEmail() == null) {
            throw new IllegalArgumentException("Name and email are required");
        }

        // Create a new user entity
        User user = new User(userRequestDTO.getName(), userRequestDTO.getEmail());

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Send the user to the Kafka topic
        userProducer.send(savedUser);

        // Return the saved user as a response DTO
        return new UserResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    // Method to get all users
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    // Method to get a user by ID
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }

    // Method to update a user
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update the user entity with new values
        if (userRequestDTO.getName() != null) {
            user.setName(userRequestDTO.getName());
        }
        if (userRequestDTO.getEmail() != null) {
            user.setEmail(userRequestDTO.getEmail());
        }

        // Save the updated user to the database
        User updatedUser = userRepository.save(user);

        // Return the updated user as a response DTO
        return new UserResponseDTO(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
    }
}