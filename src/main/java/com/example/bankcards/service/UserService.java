package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.ResourceAlreadyOccupiedException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.util.mapper.CardMapper;
import com.example.bankcards.util.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDTO) {
        User user = findByCredentials(userCredentialsDTO);
        return jwtService.generateAuthToken(user.getEmail(), user.getRoles());
    }

    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken, user.getRoles());
        }
        throw new AuthenticationException("Invalid refresh token");
    }

    public List<UserDto> readUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<User> users = userRepository.findAll(pageRequest).getContent();
        return users.stream()
                .map(UserMapper.INSTANCE::userToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.INSTANCE.userDtoToUser(userDto);
        validateUserDtoEmail(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(user);
        return UserMapper.INSTANCE.userToUserDto(savedUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(validateUserId(id).getId());
    }

    @Transactional
    public void updateUser(UpdatedUserDto updatedUserDto) {
        userRepository.findById(validateUserId(updatedUserDto.getId()).getId()).map(user ->
                validateUser(user,
                        updatedUserDto.getUsername(),
                        updatedUserDto.getEmail(),
                        updatedUserDto.getPassword(),
                        updatedUserDto.getRoles()));
    }

    private void validateUserDtoEmail(UserDto userDto) {
        userRepository.findByEmail(userDto.getEmail()).ifPresent(user -> {
            throw new ResourceAlreadyOccupiedException("User with this email already exists");
        });
    }

    private User validateUser(User user, String username, String email, String password, String roles) {
        if (username != null) user.setUsername(username);
        if (email != null) user.setEmail(validateUserEmail(email, user));
        if (password != null) user.setPassword(passwordEncoder.encode(password));
        if (roles != null) user.setRoles(roles);
        return user;
    }

    private String validateUserEmail(String email, User user) {
        if (!user.getEmail().equals(email)) {
            userRepository.findByEmail(email).ifPresent(userOpt -> {
                throw new ResourceAlreadyOccupiedException("User with this email already exists");
            });
        }
        return email;
    }

    public User validateUserId(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by id=%s".formatted(id)));
    }

    public List<CardDto> readUserCards(String email, PageRequest pageRequest) {
        return userRepository.findCardsByEmail(email, pageRequest).stream()
                .map(CardMapper.INSTANCE::cardToCardDto)
                .collect(Collectors.toList());
    }

    public User findByEmailForUpdate(String email) {
        return userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email=%s".formatted(email)));
    }

    public CardDto readUserCard(String email, Long cardId) {
        return userRepository.findCardByEmail(email, cardId)
                .map(CardMapper.INSTANCE::cardToCardDto)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found by id=%s for user".formatted(cardId)));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found by email=%s".formatted(email)));
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDTO) {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDTO.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDTO.getPassword(), user.getPassword())) {
                return user;
            }
        }
        throw new AuthenticationException("Username or password is not correct");
    }
}
