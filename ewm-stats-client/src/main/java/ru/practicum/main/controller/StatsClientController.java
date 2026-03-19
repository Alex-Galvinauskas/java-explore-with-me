package ru.practicum.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.client.StatsClient;
import ru.practicum.main.dto.EndpointHit;
import ru.practicum.main.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class StatsClientController {

    private final StatsClient statsClient;

    @PostMapping("/hit")
    public ResponseEntity<Void> sendHit(@RequestBody EndpointHit hit) {
        statsClient.hit(hit);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/hit/async")
    public ResponseEntity<String> sendHitAsync(@RequestBody EndpointHit hit) {
        CompletableFuture<Void> future = statsClient.hitAsync(hit);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Асинхронная отправка не удалась: " + ex.getMessage());
            }
        });

        return ResponseEntity.accepted().body("Запрос принят в обработку");
    }

    @PostMapping("/hit/batch")
    public ResponseEntity<String> sendHitBatch(@RequestBody List<EndpointHit> hits) {
        CompletableFuture<List<Void>> future = statsClient.hitBatchAsync(hits);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("Пакетная отправка не удалась: " + ex.getMessage());
            }
        });

        return ResponseEntity.accepted().body("Пакетный запрос принят в обработку");
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        List<ViewStats> stats = statsClient.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/async")
    public CompletableFuture<ResponseEntity<List<ViewStats>>> getStatsAsync(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        return statsClient.getStatsAsync(start, end, uris, unique)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/stats/client")
    public ResponseEntity<?> getClientStats() {
        return ResponseEntity.ok(statsClient.getClientStats());
    }
}