package com.quiz.user.services;

import com.quiz.user.dao.UserDao;
import com.quiz.user.entities.Login;
import com.quiz.user.entities.LoginResponse;
import com.quiz.user.entities.User;
import com.quiz.user.entities.UserDTO;
import com.quiz.user.exceptions.ResourceAlreadyExistsException;
import com.quiz.user.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUserId(1);
        user1.setEmail("user1@example.com");

        user2 = new User();
        user2.setUserId(2);
        user2.setEmail("user2@example.com");
    }

    @Test
    void testGetUsers() {
        when(userDao.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserDTO> users = userService.getUsers();

        assertEquals(2, users.size());
        assertEquals("user1@example.com", users.get(0).getEmail());
        assertEquals("user2@example.com", users.get(1).getEmail());

        verify(userDao, times(1)).findAll();
    }

    // Other test methods for registerUser, getUsers(int), loginUser, getUserDetails
    @Test
    void testRegisterUser() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password");

        when(userDao.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userDao.save(newUser)).thenReturn(newUser);

        UserDTO registeredUser = userService.registerUser(newUser);

        assertEquals("newuser@example.com", registeredUser.getEmail());
        assertEquals("encodedPassword", newUser.getPassword());

        verify(userDao, times(1)).findByEmail("newuser@example.com");
        verify(userDao, times(1)).save(newUser);
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        User existingUser = new User();
        existingUser.setEmail("existinguser@example.com");

        when(userDao.findByEmail("existinguser@example.com")).thenReturn(Optional.of(existingUser));

        User newUser = new User();
        newUser.setEmail("existinguser@example.com");

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerUser(newUser));

        verify(userDao, times(1)).findByEmail("existinguser@example.com");
        verify(userDao, times(0)).save(newUser);
    }

    @Test
    void testGetUsersById() {
        when(userDao.findById(1)).thenReturn(Optional.of(user1));

        UserDTO userDto = userService.getUsers(1);

        assertNotNull(userDto);
        assertEquals("user1@example.com", userDto.getEmail());

        verify(userDao, times(1)).findById(1);
    }

    @Test
    void testGetUsersById_UserNotFound() {
        when(userDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUsers(1));

        verify(userDao, times(1)).findById(1);
    }

    @Test
    void testLoginUser() {
        Login loginRequest = new Login();
        loginRequest.setEmail("user1@example.com");
        loginRequest.setPassword("password");

        when(userDao.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
        when(jwtService.generateToken(user1)).thenReturn("jwtToken");

        LoginResponse response = userService.loginUser(loginRequest);

        assertEquals("success", response.getStatus());
        assertEquals("jwtToken", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("user1@example.com", response.getUser().getEmail());

        verify(authManager, times(1)).authenticate(any());
        verify(userDao, times(1)).findByEmail("user1@example.com");
    }

    @Test
    void testGetUserDetails() {
        when(userDao.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));

        User user = userService.getUserDetails("user1@example.com");

        assertNotNull(user);
        assertEquals("user1@example.com", user.getEmail());

        verify(userDao, times(1)).findByEmail("user1@example.com");
    }

    @Test
    void testGetUserDetails_UserNotFound() {
        when(userDao.findByEmail("user1@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserDetails("user1@example.com"));

        verify(userDao, times(1)).findByEmail("user1@example.com");
    }

}
