package ru.practicum.stats.controller.privatee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.dto.comment.NewCommentDto;
import ru.practicum.stats.dto.comment.UpdateCommentDto;
import ru.practicum.stats.service.comment.CommentService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
@Tag(name = "Private: Comments", description = "Управление комментариями текущего пользователя")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @Operation(summary = "Добавить новый комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Комментарий создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные, ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден, событие не найдено"),
            @ApiResponse(responseCode = "409", description = "Конфликт: комментарий уже существует, событие не опубликовано")
    })
    public ResponseEntity<CommentDto> createComment(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody NewCommentDto dto) {
        CommentDto comment = commentService.createComment(userId, eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "Обновить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные, ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден, комментарий не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт: комментарий уже опубликован")
    })
    public ResponseEntity<CommentDto> updateComment(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDto dto) {
        CommentDto comment = commentService.updateCommentByAuthor(userId, commentId, dto);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий удален"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID комментария"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден, комментарий не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт: комментарий уже опубликован")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId) {
        commentService.deleteCommentByAuthor(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Получить все комментарии текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<CommentDto>> getUserComments(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        List<CommentDto> comments = commentService.getCommentsByUserId(userId, from, size);
        return ResponseEntity.ok(comments);
    }
}