package ru.practicum.stats.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.stats.dto.comment.NewCommentDto;
import ru.practicum.stats.dto.comment.UpdateCommentDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class CommentIntegrationTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_EVENT_ID = 1L;
    private static final Long ANOTHER_USER_ID = 2L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestDataHelper testDataHelper;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // ==================== PRIVATE ENDPOINTS TESTS ====================

    @Test
    @Order(1)
    @DisplayName("POST /users/{userId}/comments/events/{eventId} - Создание комментария - Успех")
    void createComment_Success() {
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Отличное событие! Очень понравилось.")
                .build();

        logRequest("POST", String.format("/users/%d/comments/events/%d", TEST_USER_ID, TEST_EVENT_ID));

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("text", equalTo("Отличное событие! Очень понравилось."))
                .body("eventId", equalTo(TEST_EVENT_ID.intValue()))
                .body("authorId", equalTo(TEST_USER_ID.intValue()))
                .body("status", equalTo("PENDING"))
                .body("edited", equalTo(false))
                .body("createdOn", notNullValue());

        Long commentId = response.jsonPath().getLong("id");
        testDataHelper.setCreatedCommentId(commentId);
    }

    @Test
    @Order(2)
    @DisplayName("POST /users/{userId}/comments/events/{eventId} - Создание комментария - Событие не опубликовано")
    void createComment_EventNotPublished_ShouldReturn409() {
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Комментарий к неопубликованному событию")
                .build();

        Long unpublishedEventId = 3L;

        logRequest("POST", String.format("/users/%d/comments/events/%d", TEST_USER_ID, unpublishedEventId));

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, unpublishedEventId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(409)
                .body("message", containsString("Cannot comment on unpublished event"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /users/{userId}/comments/events/{eventId} - Создание комментария - Пустой текст")
    void createComment_EmptyText_ShouldReturn400() {
        NewCommentDto newComment = NewCommentDto.builder()
                .text("")
                .build();

        logRequest("POST", String.format("/users/%d/comments/events/%d", TEST_USER_ID, TEST_EVENT_ID));

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @DisplayName("PATCH /users/{userId}/comments/{commentId} - Обновление комментария - Успех")
    void updateComment_Success() {
        Long commentId = testDataHelper.getCreatedCommentId();
        UpdateCommentDto updateDto = UpdateCommentDto.builder()
                .text("Обновленный комментарий! Действительно отличное событие.")
                .build();

        logRequest("PATCH", String.format("/users/%d/comments/%d", TEST_USER_ID, commentId));

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .patch("/users/{userId}/comments/{commentId}", TEST_USER_ID, commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(commentId.intValue()))
                .body("text", equalTo("Обновленный комментарий! Действительно отличное событие."))
                .body("edited", equalTo(true))
                .body("updatedOn", notNullValue())
                .body("status", equalTo("PENDING"));
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /users/{userId}/comments/{commentId} - Обновление комментария - Не автор")
    void updateComment_NotAuthor_ShouldReturn403() {
        Long commentId = testDataHelper.getCreatedCommentId();
        UpdateCommentDto updateDto = UpdateCommentDto.builder()
                .text("Попытка обновить чужой комментарий")
                .build();

        logRequest("PATCH", String.format("/users/%d/comments/%d", ANOTHER_USER_ID, commentId));

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .patch("/users/{userId}/comments/{commentId}", ANOTHER_USER_ID, commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(403)
                .body("message",
                        containsString("Only author can perform this action on comment"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /users/{userId}/comments - Получение всех комментариев пользователя")
    void getUserComments_Success() {
        logRequest("GET", String.format("/users/%d/comments", TEST_USER_ID));

        Response response = given()
                .log().all()
                .queryParam("from", 0)
                .queryParam("size", 10)
                .when()
                .get("/users/{userId}/comments", TEST_USER_ID);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].authorId", equalTo(TEST_USER_ID.intValue()));
    }

    // ==================== ADMIN ENDPOINTS TESTS ====================

    @Test
    @Order(7)
    @DisplayName("PATCH /admin/comments/{commentId}/publish - Публикация комментария - Успех")
    void publishComment_Success() {
        // Создаем комментарий для публикации
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Комментарий для публикации")
                .build();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        Long commentId = createResponse.jsonPath().getLong("id");

        logRequest("PATCH", String.format("/admin/comments/%d/publish", commentId));

        Response response = given()
                .log().all()
                .when()
                .patch("/admin/comments/{commentId}/publish", commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(commentId.intValue()))
                .body("status", equalTo("PUBLISHED"));
    }

    @Test
    @Order(8)
    @DisplayName("PATCH /admin/comments/{commentId}/reject - Отклонение комментария - Успех")
    void rejectComment_Success() {
        // Создаем комментарий для отклонения
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Комментарий для отклонения")
                .build();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        Long commentId = createResponse.jsonPath().getLong("id");

        logRequest("PATCH", String.format("/admin/comments/%d/reject", commentId));

        Response response = given()
                .log().all()
                .when()
                .patch("/admin/comments/{commentId}/reject", commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(commentId.intValue()))
                .body("status", equalTo("REJECTED"));
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /admin/comments/{commentId} - Удаление комментария админом - Успех")
    void deleteCommentByAdmin_Success() {
        // Создаем комментарий для удаления
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Комментарий для удаления админом")
                .build();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        Long commentId = createResponse.jsonPath().getLong("id");

        logRequest("DELETE", String.format("/admin/comments/%d", commentId));

        Response response = given()
                .log().all()
                .when()
                .delete("/admin/comments/{commentId}", commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(204);
    }

    @Test
    @Order(10)
    @DisplayName("GET /admin/comments/search - Поиск комментариев по тексту")
    void searchComments_ByText_Success() {
        logRequest("GET", "/admin/comments/search?text=отличное");

        Response response = given()
                .log().all()
                .queryParam("text", "отличное")
                .queryParam("from", 0)
                .queryParam("size", 10)
                .when()
                .get("/admin/comments/search");

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200);
    }

    // ==================== PUBLIC ENDPOINTS TESTS ====================

    @Test
    @Order(11)
    @DisplayName("GET /comments/{commentId} - Получение комментария по ID - Успех")
    void getCommentById_Success() {
        // Создаем и публикуем комментарий
        NewCommentDto newComment = NewCommentDto.builder()
                .text("Публичный комментарий")
                .build();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(newComment)
                .when()
                .post("/users/{userId}/comments/events/{eventId}", TEST_USER_ID, TEST_EVENT_ID);

        Long commentId = createResponse.jsonPath().getLong("id");

        // Публикуем
        given()
                .patch("/admin/comments/{commentId}/publish", commentId)
                .then()
                .statusCode(200);

        logRequest("GET", String.format("/comments/%d", commentId));

        Response response = given()
                .log().all()
                .when()
                .get("/comments/{commentId}", commentId);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(commentId.intValue()))
                .body("text", equalTo("Публичный комментарий"))
                .body("status", equalTo("PUBLISHED"));
    }

    @Test
    @Order(12)
    @DisplayName("GET /comments/events/{eventId} - Получение комментариев события - Успех")
    void getCommentsByEvent_Success() {
        logRequest("GET", String.format("/comments/events/%d", TEST_EVENT_ID));

        Response response = given()
                .log().all()
                .queryParam("from", 0)
                .queryParam("size", 10)
                .when()
                .get("/comments/events/{eventId}", TEST_EVENT_ID);

        logResponse(response);

        response.then()
                .log().all()
                .statusCode(200);
    }

    // ==================== HELPER METHODS ====================

    private void logRequest(String method, String path) {
        System.out.println("\n========================================");
        System.out.println("📤 REQUEST: " + method + " " + path);
        System.out.println("⏰ Time: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("========================================\n");
    }

    private void logResponse(Response response) {
        System.out.println("\n========================================");
        System.out.println("📥 RESPONSE: " + response.statusCode());
        System.out.println("⏰ Time: " + LocalDateTime.now().format(FORMATTER));
        System.out.println("📦 Body: " + response.getBody().asString());
        System.out.println("========================================\n");
    }
}