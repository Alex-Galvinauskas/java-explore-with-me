# *Explore With Me* 🗺️
*Приложение для поиска и организации событий, позволяющее пользователям делиться информацией об интересных мероприятиях и находить компанию для участия.*

## Технологии
- **Java 17**
- **Spring Boot 3** (Web, Data JPA, Validation)
- **PostgreSQL**
- **Docker & Docker Compose**
- **Maven**
- **Lombok**
- **OpenAPI 3.0**

## Запуск приложения

### Предварительные требования
- Docker и Docker Compose
- Java 17 (для локального запуска без Docker)

### Запуск с Docker Compose
1. Клонируйте репозиторий:
   ```bash
   git clone <repository-url>
   cd java-explore-with-me
   ```

2. Запустите сервисы:
   ```bash
   docker-compose up --build
   ```

   Это запустит:
   - Основной сервис на `http://localhost:8080`
   - Сервис статистики на `http://localhost:9090`
   - Базы данных PostgreSQL на портах 5432 и 5433

3. Остановите сервисы:
   ```bash
   docker-compose down
   ```

### Локальный запуск
1. Установите PostgreSQL и создайте базы данных:
   - `explore_with_me_main` для основного сервиса
   - `explore_with_me_stats` для сервиса статистики

2. Соберите проект:
   ```bash
   mvn clean install
   ```

3. Запустите сервисы:
   - Stats Service: `java -jar ewm-stats-service/target/ewm-stats-service-0.0.1-SNAPSHOT.jar`
   - Main Service: `java -jar ewm-main-service/target/ewm-main-service-0.0.1-SNAPSHOT.jar`

## API Документация
- Спецификация основного сервиса: [ewm-main-service-spec.json](ewm-main-service-spec.json)
- Спецификация сервиса статистики: [ewm-stats-service-spec.json](ewm-stats-service-spec.json)
- **Swagger UI**: После запуска приложения доступен по 
- http://localhost:8080/swagger-ui/index.html
- http://localhost:9090/swagger-ui/index.html
- **OpenAPI JSON**: 
- http://localhost:8080/v3/api-docs
- http://localhost:9090/v3/api-docs

Основные эндпоинты:
- **Публичный API**: `/categories`, `/compilations`, `/events`
- **Закрытый API**: `/users/{userId}/events`, `/users/{userId}/requests`
- **Админ API**: `/admin/categories`, `/admin/events`, `/admin/users`, `/admin/compilations`
- **Статистика**: `/hit` (POST), `/stats` (GET)

## Структура проекта

explore-with-me (родительский проект)  
│  
├── pom.xml (родительский)  
│  
├── ewm-main-service (основной модуль)  
│   ├── pom.xml  
│   ├── src/  
│   │   ├── main/  
│   │   │   ├── java/  
│   │   │   │   └── ru/practicum/  
│   │   │   │       └── stats/  
│   │   │   │           ├── MainServiceApplication.java  
│   │   │   │           │  
│   │   │   │           ├── config/  
│   │   │   │           │   └── OpenApiConfig.java  
│   │   │   │           │  
│   │   │   │           ├── controller/  
│   │   │   │           │   ├── admin/  
│   │   │   │           │   │   ├── AdminCategoryController.java  
│   │   │   │           │   │   ├── AdminCompilationController.java  
│   │   │   │           │   │   ├── AdminEventController.java  
│   │   │   │           │   │   └── AdminUserController.java  
│   │   │   │           │   ├── privatee/  
│   │   │   │           │   │   ├── PrivateEventController.java  
│   │   │   │           │   │   └── PrivateRequestController.java  
│   │   │   │           │   └── publicc/  
│   │   │   │           │       ├── PublicCategoryController.java  
│   │   │   │           │       ├── PublicCompilationController.java  
│   │   │   │           │       └── PublicEventController.java  
│   │   │   │           │  
│   │   │   │           ├── core/  
│   │   │   │           │   ├── compilation/  
│   │   │   │           │   │   ├── loader/  
│   │   │   │           │   │   │   └── CompilationEventLoader.java  
│   │   │   │           │   │   └── mapper/  
│   │   │   │           │   │       └── CompilationMapperHelper.java  
│   │   │   │           │   ├── event/  
│   │   │   │           │   │   ├── builder/  
│   │   │   │           │   │   │   ├── EventBuilder.java  
│   │   │   │           │   │   │   ├── EventFieldUpdater.java  
│   │   │   │           │   │   │   └── EventUpdater.java  
│   │   │   │           │   │   ├── enricher/  
│   │   │   │           │   │   │   ├── EventEnricher.java  
│   │   │   │           │   │   │   └── EventResponseEnricher.java  
│   │   │   │           │   │   └── search/  
│   │   │   │           │   │       ├── DateRangeNormalizer.java  
│   │   │   │           │   │       ├── EventResponsePostProcessor.java  
│   │   │   │           │   │       ├── EventSearchOrchestrator.java  
│   │   │   │           │   │       ├── EventSearchParamsProcessor.java  
│   │   │   │           │   │       └── EventSearchService.java  
│   │   │   │           │   ├── request/  
│   │   │   │           │   │   ├── RequestCanceller.java  
│   │   │   │           │   │   ├── RequestConfirmer.java  
│   │   │   │           │   │   ├── RequestCreator.java  
│   │   │   │           │   │   ├── RequestFetcher.java  
│   │   │   │           │   │   ├── RequestRejecter.java  
│   │   │   │           │   │   └── RequestStatusUpdater.java  
│   │   │   │           │   └── user/  
│   │   │   │           │       └── query/  
│   │   │   │           │           └── UserQueryService.java  
│   │   │   │           │  
│   │   │   │           ├── dto/  
│   │   │   │           │   ├── category/  
│   │   │   │           │   │   ├── CategoryDto.java  
│   │   │   │           │   │   └── NewCategoryDto.java  
│   │   │   │           │   ├── compilation/  
│   │   │   │           │   │   ├── CompilationDto.java  
│   │   │   │           │   │   ├── NewCompilationDto.java  
│   │   │   │           │   │   └── UpdateCompilationRequest.java  
│   │   │   │           │   ├── event/  
│   │   │   │           │   │   ├── EventFullDto.java  
│   │   │   │           │   │   ├── EventShortDto.java  
│   │   │   │           │   │   ├── NewEventDto.java  
│   │   │   │           │   │   ├── UpdateEventAdminRequest.java  
│   │   │   │           │   │   ├── UpdateEventUserRequest.java  
│   │   │   │           │   │   ├── LocationDto.java  
│   │   │   │           │   │   └── EventSearchParams.java  
│   │   │   │           │   ├── request/  
│   │   │   │           │   │   ├── ParticipationRequestDto.java  
│   │   │   │           │   │   ├── EventRequestStatusUpdateRequest.java  
│   │   │   │           │   │   └── EventRequestStatusUpdateResult.java  
│   │   │   │           │   └── user/  
│   │   │   │           │       ├── NewUserRequest.java  
│   │   │   │           │       ├── UserDto.java  
│   │   │   │           │       └── UserShortDto.java  
│   │   │   │           │  
│   │   │   │           ├── exception/  
│   │   │   │           │   ├── ApiError.java  
│   │   │   │           │   ├── BadRequestException.java  
│   │   │   │           │   ├── ConflictException.java  
│   │   │   │           │   ├── ErrorHandler.java  
│   │   │   │           │   ├── ForbiddenException.java  
│   │   │   │           │   ├── NotFoundException.java  
│   │   │   │           │   └── ValidationException.java  
│   │   │   │           │  
│   │   │   │           ├── mapper/  
│   │   │   │           │   ├── CategoryMapper.java  
│   │   │   │           │   ├── CompilationMapper.java  
│   │   │   │           │   ├── EventMapper.java  
│   │   │   │           │   ├── LocationMapper.java  
│   │   │   │           │   ├── RequestMapper.java  
│   │   │   │           │   └── UserMapper.java  
│   │   │   │           │  
│   │   │   │           ├── model/  
│   │   │   │           │   ├── BaseEntity.java  
│   │   │   │           │   ├── Category.java  
│   │   │   │           │   ├── Compilation.java  
│   │   │   │           │   ├── Event.java  
│   │   │   │           │   ├── Location.java  
│   │   │   │           │   ├── Request.java  
│   │   │   │           │   ├── User.java  
│   │   │   │           │   └── enums/  
│   │   │   │           │       ├── EventState.java  
│   │   │   │           │       └── RequestStatus.java  
│   │   │   │           │  
│   │   │   │           ├── repository/  
│   │   │   │           │   ├── BaseRepository.java  
│   │   │   │           │   ├── CategoryRepository.java  
│   │   │   │           │   ├── CompilationRepository.java  
│   │   │   │           │   ├── EventRepository.java  
│   │   │   │           │   ├── RequestRepository.java  
│   │   │   │           │   └── UserRepository.java  
│   │   │   │           │  
│   │   │   │           ├── service/  
│   │   │   │           │   ├── category/  
│   │   │   │           │   │   ├── CategoryService.java  
│   │   │   │           │   │   └── CategoryServiceImpl.java  
│   │   │   │           │   ├── compilation/  
│   │   │   │           │   │   ├── CompilationService.java  
│   │   │   │           │   │   └── CompilationServiceImpl.java  
│   │   │   │           │   ├── event/  
│   │   │   │           │   │   ├── EventService.java  
│   │   │   │           │   │   └── EventServiceImpl.java  
│   │   │   │           │   ├── request/  
│   │   │   │           │   │   ├── RequestService.java  
│   │   │   │           │   │   └── RequestServiceImpl.java  
│   │   │   │           │   └── user/  
│   │   │   │           │       ├── UserService.java  
│   │   │   │           │       └── UserServiceImpl.java  
│   │   │   │           │  
│   │   │   │           ├── statistics/  
│   │   │   │           │   ├── event/  
│   │   │   │           │   │   └── StatisticsService.java  
│   │   │   │           │   └── request/  
│   │   │   │           │       └── ViewStatsIncrementor.java  
│   │   │   │           │  
│   │   │   │           ├── strategy/  
│   │   │   │           │   └── request/  
│   │   │   │           │       └── RequestAutoConfirmer.java  
│   │   │   │           │  
│   │   │   │           └── validation/  
│   │   │   │               ├── compilation/  
│   │   │   │               │   └── CompilationValidator.java  
│   │   │   │               ├── event/  
│   │   │   │               │   ├── EventFieldValidator.java  
│   │   │   │               │   ├── EventValidator.java  
│   │   │   │               │   └── PaginationValidator.java  
│   │   │   │               ├── request/  
│   │   │   │               │   └── RequestValidator.java  
│   │   │   │               └── user/  
│   │   │   │                   └── UserValidatior.java  
│   │   │   │  
│   │   │   └── resources/  
│   │   │       ├── application.properties  
│   │   │       ├── application.yaml  
│   │   │       ├── application-docker.properties  
│   │   │       ├── application-test.properties  
│   │   │       └── schema.sql  
│   │   │  
│   │   └── test/  
│   │
├── ewm-stats-dto (модуль DTO для статистики)  
│   ├── pom.xml  
│   ├── src/  
│   │   ├── main/  
│   │   │   ├── java/  
│   │   │   │   └── ru/practicum/stats/dto/  
│   │   │   │       ├── CategoryDto.java  
│   │   │   │       ├── EndpointHit.java  
│   │   │   │       ├── NewCategoryDto.java  
│   │   │   │       └── ViewStats.java  
│   │   │   └── resources/  
│   │   └── test/  
│   │  
├── ewm-stats-client (модуль клиента статистики)  
│   ├── pom.xml  
│   ├── src/  
│   │   ├── main/  
│   │   │   ├── java/  
│   │   │   │   └── ru/practicum/stats/  
│   │   │   │       ├── client/  
│   │   │   │       │   ├── StatsClient.java  
│   │   │   │       │   └── ClientStatsMonitoring.java  
│   │   │   │       └── config/  
│   │   │   │           └── StatsClientConfig.java  
│   │   │   └── resources/  
│   │   │       └── application.yaml  
│   │   └── test/  
│   │  
└── ewm-stats-service (модуль сервиса статистики)  
├── pom.xml  
├── src/  
│   ├── main/  
│   │   ├── java/  
│   │   │   └── ru/practicum/stats/  
│   │   │       ├── StatsServiceApplication.java  
│   │   │       │  
│   │   │       ├── config/  
│   │   │       │   ├── JacksonConfig.java  
│   │   │       │   └── OpenApiConfig.java  
│   │   │       │  
│   │   │       ├── controller/  
│   │   │       │   └── StatsController.java  
│   │   │       │  
│   │   │       ├── exception/  
│   │   │       │   ├── ApiError.java  
│   │   │       │   ├── BadRequestException.java  
│   │   │       │   ├── ConflictException.java  
│   │   │       │   ├── ErrorHandler.java  
│   │   │       │   ├── NotFoundException.java  
│   │   │       │   ├── StatsValidationException.java  
│   │   │       │   └── ValidationException.java  
│   │   │       │  
│   │   │       ├── mapper/  
│   │   │       │   └── StatsMapper.java  
│   │   │       │  
│   │   │       ├── model/  
│   │   │       │   └── EndpointHitEntity.java  
│   │   │       │  
│   │   │       ├── repository/  
│   │   │       │   └── StatsRepository.java  
│   │   │       │  
│   │   │       └── service/  
│   │   │           ├── StatsService.java  
│   │   │           └── StatsServiceImpl.java  
│   │   │  
│   │   └── resources/  
│   │       ├── application.properties  
│   │       ├── application-dev.properties  
│   │       ├── application-docker.properties  
│   │       ├── application-test.properties  
│   │       └── schema.sql  
│   │  
│   └── test/  
  

# Зависимости
![Визуализация зависимостей между модулями.png](%D0%92%D0%B8%D0%B7%D1%83%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B7%D0%B0%D0%B2%D0%B8%D1%81%D0%B8%D0%BC%D0%BE%D1%81%D1%82%D0%B5%D0%B9%20%D0%BC%D0%B5%D0%B6%D0%B4%D1%83%20%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8F%D0%BC%D0%B8.png)