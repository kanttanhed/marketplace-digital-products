package com.github.kanttanhed.user_service;

import com.github.kanttanhed.user_service.dto.UserRequestDTO;
import com.github.kanttanhed.user_service.dto.UserResponseDTO;
import com.github.kanttanhed.user_service.entity.User;
import com.github.kanttanhed.user_service.repository.UserRepository;
import com.github.kanttanhed.user_service.service.UserProducer;
import com.github.kanttanhed.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private UserProducer userProducer;

    // This method is executed before each test case
    // to set up the necessary objects and dependencies for the test class.
    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userProducer = mock(UserProducer.class);
        userService = new UserService(userRepository, userProducer);
    }

    @Test
    void testCreateUser() {
        // Test the createUser method
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("joao@email.com");

        // Create a user entity to be returned by the mock repository
        User user = new User("João", "joao@email.com");
        user.setId(1L);

        // Mock the behavior of the userRepository to return the created user
        when(userRepository.save(any(User.class))).thenReturn(user);


        // Call the createUser method
        UserResponseDTO responseDTO = userService.createUser(requestDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("João", responseDTO.getName());
        assertEquals("joao@email.com", responseDTO.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUserWithInvalidData() {
        UserRequestDTO requestDTO = new UserRequestDTO(); // nome e email nulos

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(requestDTO);
        });

        assertEquals("Name and email are required", exception.getMessage());

        // Verifica que nada foi salvo ou enviado ao Kafka
        verify(userRepository, never()).save(any());
        verify(userProducer, never()).send(any());
    }

    @Test
    void testCreateUserWithNullName() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("email@email.com");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        assertEquals("Name and email are required", ex.getMessage());
    }

    @Test
    void testCreateUserWithNullEmail() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Teste");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        assertEquals("Name and email are required", ex.getMessage());
    }


    @Test
    void testGetUserById(){
        // Test the getUserById method
        User user = new User("Maria", "maria@email.com");
        user.setId(2L);

        // Mock the behavior of the userRepository to return the created user
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(user));

        // Call the getUserById method
        UserResponseDTO responseDTO = userService.getUserById(2L);

        assertNotNull(responseDTO);
        assertEquals(2L, responseDTO.getId());
        assertEquals("Maria", responseDTO.getName());
        assertEquals("maria@email.com", responseDTO.getEmail());

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(99L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(
                new User("Alice", "alice@email.com"),
                new User("Bob", "bob@email.com")
        );
        users.get(0).setId(1L);
        users.get(1).setId(2L);

        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsersReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateUserSuccess() {
        User user = new User("Antigo Nome", "antigo@email.com");
        user.setId(5L);

        UserRequestDTO updateDTO = new UserRequestDTO();
        updateDTO.setName("Novo Nome");
        updateDTO.setEmail("novo@email.com");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO updated = userService.updateUser(5L, updateDTO);

        assertEquals(5L, updated.getId());
        assertEquals("Novo Nome", updated.getName());
        assertEquals("novo@email.com", updated.getEmail());

        verify(userRepository).findById(5L);
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserNotFound() {
        UserRequestDTO updateDTO = new UserRequestDTO();
        updateDTO.setName("Teste");

        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(77L, updateDTO);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(77L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserOnlyName() {
        User user = new User("Nome Antigo", "email@original.com");
        user.setId(10L);
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Novo Nome");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO updated = userService.updateUser(10L, dto);

        assertEquals("Novo Nome", updated.getName());
        assertEquals("email@original.com", updated.getEmail());
    }

    @Test
    void testUpdateUserOnlyEmail() {
        User user = new User("Nome", "email@velho.com");
        user.setId(11L);
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("email@novo.com");

        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO updated = userService.updateUser(11L, dto);

        assertEquals("Nome", updated.getName());
        assertEquals("email@novo.com", updated.getEmail());
    }
}