package ru.practicum.stats.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.service.comment.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
@Tag(name = "Admin: Comments", description = "Управление комментариями (административный доступ)")
public class AdminCommentController {

    private final CommentService commentService;

    @PatchMapping("/{commentId}/publish")
    @Operation(summary = "Опубликовать комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий опубликован"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "409", description = "Комментарий уже опубликован или отклонен")
    })
    public ResponseEntity<CommentDto> publishComment(
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId) {
        CommentDto comment = commentService.publishComment(commentId);
        return ResponseEntity.ok(comment);
    }

    @PatchMapping("/{commentId}/reject")
    @Operation(summary = "Отклонить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий отклонен"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "409", description = "Комментарий уже опубликован или отклонен")
    })
    public ResponseEntity<CommentDto> rejectComment(
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId) {
        CommentDto comment = commentService.rejectComment(commentId);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий удален"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск комментариев по фильтрам")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    public ResponseEntity<List<CommentDto>> searchComments(
            @Parameter(description = "Текст для поиска в комментарии")
            @RequestParam(required = false) String text,
            @Parameter(description = "Статус комментария (PENDING, PUBLISHED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Начало диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @Parameter(description = "Конец диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        List<CommentDto> comments = commentService.searchComments(text, status, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(comments);
    }
}