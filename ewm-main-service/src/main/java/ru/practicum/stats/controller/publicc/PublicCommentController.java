package ru.practicum.stats.controller.publicc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.service.comment.CommentService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
@Tag(name = "Public: Comments", description = "Публичный API для работы с комментариями")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    @Operation(summary = "Получить комментарий по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    public ResponseEntity<CommentDto> getCommentById(
            @Parameter(description = "ID комментария", required = true, example = "1")
            @PathVariable @Positive Long commentId) {
        CommentDto comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/events/{eventId}")
    @Operation(summary = "Получить список комментариев события")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Событие не найдено")
    })
    public ResponseEntity<List<CommentDto>> getCommentsByEvent(
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        List<CommentDto> comments = commentService.getCommentsByEventId(eventId, from, size);
        return ResponseEntity.ok(comments);
    }
}