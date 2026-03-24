package ru.practicum.stats.controller.privatee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.request.ParticipationRequestDto;
import ru.practicum.stats.service.request.RequestService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Tag(name = "Private: Requests", description = "Управление запросами на участие в событиях")
public class PrivateRequestController {

    private final RequestService requestService;

    @PostMapping
    @Operation(summary = "Создать запрос на участие в событии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Запрос создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса, неверный ID события"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден, событие не найдено"),
            @ApiResponse(responseCode = "409",
                    description = "Конфликт: лимит участников превышен, повторный запрос," +
                            " инициатор события не может подать запрос")
    })
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @RequestParam @Positive Long eventId,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        ParticipationRequestDto request = requestService.addParticipationRequest(userId, eventId, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @GetMapping
    @Operation(summary = "Получить все запросы пользователя на участие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список запросов получен"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId) {
        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/{requestId}/cancel")
    @Operation(summary = "Отменить запрос на участие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос отменен"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID запроса"),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден, запрос не найден, запрос не принадлежит пользователю")
    })
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID запроса", required = true, example = "1")
            @PathVariable @Positive Long requestId) {
        ParticipationRequestDto request = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(request);
    }
}