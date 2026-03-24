package ru.practicum.stats.validation.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.user.NewUserRequest;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidatior {
    private final UserRepository userRepository;


    public void validateNewUser(NewUserRequest request) {
        validateEmailUniqueness(request.getEmail());
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Попытка регистрации с уже существующим email: {}", email);
            throw new ConflictException(
                    String.format("Пользователь с email \"%s\" уже существует", email)
            );
        }
    }
}