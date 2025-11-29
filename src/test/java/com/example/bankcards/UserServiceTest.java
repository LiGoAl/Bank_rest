package com.example.bankcards;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.ResourceAlreadyOccupiedException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User existingUser, existingUser2;
    private UserDto userDto;
    private UpdatedUserDto updatedUserDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("user");
        existingUser.setEmail("user@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setRoles("ROLE_USER");

        existingUser2 = new User();
        existingUser2.setId(1L);
        existingUser2.setUsername("admin");
        existingUser2.setEmail("admin@example.com");
        existingUser2.setPassword("encodedPasswordAdmin");
        existingUser2.setRoles("ROLE_USER,ROLE_ADMIN");

        userDto = UserMapper.INSTANCE.userToUserDto(existingUser);

        updatedUserDto = new UpdatedUserDto();
        updatedUserDto.setId(1L);
        updatedUserDto.setUsername("newusername");
        updatedUserDto.setEmail("newemail@example.com");
        updatedUserDto.setPassword("newpassword");
        updatedUserDto.setRoles("ROLE_ADMIN");
    }

    @Test
    public void testSignInSuccess() {
        UserCredentialsDto credentials = new UserCredentialsDto("user@example.com", "password");
        JwtAuthenticationDto authenticationDto = new JwtAuthenticationDto();
        authenticationDto.setToken("token");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(credentials.getPassword(), existingUser.getPassword())).thenReturn(true);
        when(jwtService.generateAuthToken(existingUser.getEmail(), existingUser.getRoles())).thenReturn(authenticationDto);

        JwtAuthenticationDto authDto = userService.signIn(credentials);

        assertNotNull(authDto);
        assertEquals("token", authDto.getToken());
    }

    @Test
    public void testSignInFailure_InvalidEmail() {
        UserCredentialsDto credentials = new UserCredentialsDto("wrong@example.com", "password");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.signIn(credentials);
        });

        assertEquals("Username or password is not correct", exception.getMessage());
    }

    @Test
    public void testSignInFailure_InvalidPassword() {
        UserCredentialsDto credentials = new UserCredentialsDto("user@example.com", "wrongPassword");

        when(userRepository.findByEmail(credentials.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(credentials.getPassword(), existingUser.getPassword())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.signIn(credentials);
        });

        assertEquals("Username or password is not correct", exception.getMessage());
    }

    @Test
    public void testRefreshTokenSuccess() {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("valid_refresh_token");
        JwtAuthenticationDto authenticationDto = new JwtAuthenticationDto();
        authenticationDto.setToken("newToken");

        when(jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())).thenReturn(true);
        when(jwtService.getEmailFromToken(refreshTokenDto.getRefreshToken())).thenReturn(existingUser.getEmail());
        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        when(jwtService.refreshBaseToken(existingUser.getEmail(), refreshTokenDto.getRefreshToken(), existingUser.getRoles()))
                .thenReturn(authenticationDto);

        JwtAuthenticationDto result = userService.refreshToken(refreshTokenDto);

        assertNotNull(result);
        assertEquals("newToken", result.getToken());
    }

    @Test
    public void testRefreshTokenInvalidToken() {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto("invalid_refresh_token");

        when(jwtService.validateJwtToken(refreshTokenDto.getRefreshToken())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.refreshToken(refreshTokenDto);
        });

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    public void testReadUsers() {
        int page = 0;
        int size = 2;

        Page<User> userPage = mock(Page.class);
        when(userPage.getContent()).thenReturn(Arrays.asList(existingUser, existingUser2));

        when(userRepository.findAll(PageRequest.of(page, size))).thenReturn(userPage);

        List<UserDto> result = userService.readUsers(page, size);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(existingUser.getId(), result.get(0).getId());
        assertEquals(existingUser.getUsername(), result.get(0).getUsername());
        assertEquals(existingUser.getPassword(), result.get(0).getPassword());
        assertEquals(existingUser.getEmail(), result.get(0).getEmail());
        assertEquals(existingUser.getRoles(), result.get(0).getRoles());

        assertEquals(existingUser2.getId(), result.get(1).getId());
        assertEquals(existingUser2.getUsername(), result.get(1).getUsername());
        assertEquals(existingUser2.getPassword(), result.get(1).getPassword());
        assertEquals(existingUser2.getEmail(), result.get(1).getEmail());
        assertEquals(existingUser2.getRoles(), result.get(1).getRoles());
    }

    @Test
    public void testCreateUser() {
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto createdUserDto = userService.createUser(userDto);

        assertNotNull(createdUserDto);
        assertEquals(userDto.getUsername(), createdUserDto.getUsername());
        assertEquals(userDto.getEmail(), createdUserDto.getEmail());
        assertEquals(userDto.getRoles(), createdUserDto.getRoles());

        verify(passwordEncoder).encode(userDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testCreateUser_WithInvalidEmail() {
        userDto.setEmail("invalidEmail");
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(new User()));

        ResourceAlreadyOccupiedException e = assertThrows(ResourceAlreadyOccupiedException.class, () -> {
            userService.createUser(userDto);
        });

        assertEquals("User with this email already exists", e.getMessage());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).deleteById(existingUser.getId());

        userService.deleteUser(existingUser.getId());

        verify(userRepository).deleteById(existingUser.getId());
    }

    @Test
    public void testDeleteUser_UserNotFound() {

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(existingUser.getId());
        });

        assertEquals("User not found by id=1", e.getMessage());

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void testUpdateUser_Success() {
        when(userRepository.findById(updatedUserDto.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(updatedUserDto.getPassword())).thenReturn("hashedPassword");

        userService.updateUser(updatedUserDto);

        assertEquals(updatedUserDto.getUsername(), existingUser.getUsername());
        assertEquals("hashedPassword", existingUser.getPassword());
        assertEquals(updatedUserDto.getEmail(), existingUser.getEmail());
        assertEquals(updatedUserDto.getRoles(), existingUser.getRoles());

        verify(userRepository, times(2)).findById(updatedUserDto.getId());
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        when(userRepository.findById(updatedUserDto.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(updatedUserDto);
        });

        assertEquals("User not found by id=1", e.getMessage());
        verify(userRepository).findById(updatedUserDto.getId());
    }
}
