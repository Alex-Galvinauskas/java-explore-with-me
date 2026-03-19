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
- **Swagger UI**: После запуска приложения доступен по `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

Основные эндпоинты:
- **Публичный API**: `/categories`, `/compilations`, `/events`
- **Закрытый API**: `/users/{userId}/events`, `/users/{userId}/requests`
- **Админ API**: `/admin/categories`, `/admin/events`, `/admin/users`, `/admin/compilations`
- **Статистика**: `/hit` (POST), `/stats` (GET)

## Структура проекта

explore-with-me/                                  # КОРНЕВОЙ ПРОЕКТ (родительский модуль)  
├── pom.xml                                       # Управляет версиями и объединяет все модули  
├── docker-compose.yml                            # Оркестрация контейнеров: main-service, stats-service, 2 БД  
│
├── ewm-main-service/                              # МОДУЛЬ 1: ОСНОВНОЙ СЕРВИС (порт 8080)  
│   ├── pom.xml                                    # Зависимости: stats-client, Spring Web, JPA, PostgreSQL  
│   ├── Dockerfile                                 # Сборка образа основного сервиса  
│   └── src/  
│       └── main/  
│           ├── java/ru/practicum/main/  
│           │   ├── MainServiceApplication.java    # Точка входа (@SpringBootApplication)  
│           │   │  
│           │   ├── controller/                     # REST-контроллеры (обработка HTTP-запросов)  
│           │   │   ├── admin/                      # 🔐 Административный API (только для админов)  
│           │   │   │   ├── AdminCategoryController.java     # CRUD категорий  
│           │   │   │   ├── AdminCompilationController.java  # CRUD подборок событий  
│           │   │   │   ├── AdminEventController.java         # Модерация событий (публикация/отклонение)  
│           │   │   │   └── AdminUserController.java          # Управление пользователями  
│           │   │   │  
│           │   │   ├── private/                    # 🔐 Закрытый API (только авторизованные)  
│           │   │   │   ├── PrivateEventController.java      # Создание/редактирование своих событий  
│           │   │   │   └── PrivateRequestController.java     # Управление заявками на участие  
│           │   │   │  
│           │   │   └── public/                      # 🌍 Публичный API (доступен всем)  
│           │   │       ├── PublicCategoryController.java    # Просмотр категорий  
│           │   │       ├── PublicCompilationController.java # Просмотр подборок  
│           │   │       └── PublicEventController.java       # Поиск и фильтрация событий + СТАТИСТИКА  
│           │   │  
│           │   ├── service/                         # Бизнес-логика (сервисный слой)  
│           │   │   ├── category/                     # Работа с категориями  
│           │   │   │   ├── CategoryService.java       # Интерфейс  
│           │   │   │   └── CategoryServiceImpl.java   # Реализация  
│           │   │   │  
│           │   │   ├── compilation/                   # Работа с подборками  
│           │   │   │   ├── CompilationService.java  
│           │   │   │   └── CompilationServiceImpl.java  
│           │   │   │  
│           │   │   ├── event/                         # Работа с событиями (основная логика)  
│           │   │   │   ├── EventService.java  
│           │   │   │   └── EventServiceImpl.java      # Здесь интеграция со StatsClient для статистики  
│           │   │   │  
│           │   │   ├── request/                       # Работа с заявками на участие  
│           │   │   │   ├── RequestService.java  
│           │   │   │   └── RequestServiceImpl.java    # Логика подтверждения/отклонения заявок  
│           │   │   │  
│           │   │   └── user/                          # Работа с пользователями  
│           │   │       ├── UserService.java  
│           │   │       └── UserServiceImpl.java  
│           │   │  
│           │   ├── repository/                       # DAO слой (доступ к БД через Spring Data JPA)  
│           │   │   ├── CategoryRepository.java        # Category JpaRepository  
│           │   │   ├── CompilationRepository.java     # Compilation JpaRepository + @Query для сложных запросов  
│           │   │   ├── EventRepository.java           # Event JpaRepository + кастомные методы фильтрации  
│           │   │   ├── RequestRepository.java         # Request JpaRepository  
│           │   │   └── UserRepository.java            # User JpaRepository  
│           │   │  
│           │   ├── model/                            # JPA-сущности (таблицы БД)  
│           │   │   ├── Category.java                  # Категории событий  
│           │   │   ├── Compilation.java               # Подборки событий (ManyToMany с Event)  
│           │   │   ├── Event.java                     # События (главная сущность)  
│           │   │   ├── User.java                       # Пользователи  
│           │   │   ├── Request.java                    # Заявки на участие (связь User <-> Event)  
│           │   │   ├── Location.java                   # Встраиваемый объект (координаты)  
│           │   │   └── enums/                         # Перечисления  
│           │   │       ├── EventState.java             # PENDING, PUBLISHED, CANCELED  
│           │   │       └── RequestStatus.java          # PENDING, CONFIRMED, REJECTED, CANCELED  
│           │   │  
│           │   ├── dto/                               # 📦 DTO (Data Transfer Objects) для API  
│           │   │   ├── category/                       # ДТО для категорий  
│           │   │   │   ├── CategoryDto.java            # Ответ: id + name  
│           │   │   │   └── NewCategoryDto.java         # Запрос: только name  
│           │   │   │  
│           │   │   ├── compilation/                    # ДТО для подборок  
│           │   │   │   ├── CompilationDto.java         # Полная информация о подборке  
│           │   │   │   ├── NewCompilationDto.java      # Создание подборки  
│           │   │   │   └── UpdateCompilationRequest.java # Обновление подборки  
│           │   │   │  
│           │   │   ├── event/                          # ДТО для событий  
│           │   │   │   ├── EventFullDto.java           # Полная информация (для /events/{id})  
│           │   │   │   ├── EventShortDto.java          # Краткая информация (для списков)  
│           │   │   │   ├── NewEventDto.java            # Создание события  
│           │   │   │   ├── UpdateEventAdminRequest.java # Модерация (админ)  
│           │   │   │   └── UpdateEventUserRequest.java # Редактирование (пользователь)  
│           │   │   │  
│           │   │   ├── request/                        # ДТО для заявок  
│           │   │   │   ├── ParticipationRequestDto.java # Информация о заявке  
│           │   │   │   ├── EventRequestStatusUpdateRequest.java # Подтверждение/отклонение  
│           │   │   │   └── EventRequestStatusUpdateResult.java # Результат обработки  
│           │   │   │  
│           │   │   └── user/                           # ДТО для пользователей  
│           │   │       ├── UserDto.java                # Полная информация  
│           │   │       ├── UserShortDto.java           # Краткая информация (для Event)  
│           │   │       └── NewUserRequest.java         # Регистрация нового пользователя  
│           │   │  
│           │   ├── mapper/                             # 🗺️ Мапперы (Entity <-> DTO)  
│           │   │   ├── CategoryMapper.java             # Category <-> CategoryDto  
│           │   │   ├── CompilationMapper.java          # Compilation <-> CompilationDto  
│           │   │   ├── EventMapper.java                # Event <-> EventFullDto/EventShortDto  
│           │   │   ├── RequestMapper.java              # Request <-> ParticipationRequestDto  
│           │   │   └── UserMapper.java                 # User <-> UserDto/UserShortDto  
│           │   │  
│           │   └── exception/                          # 🚨 Глобальная обработка ошибок  
│           │       ├── ErrorHandler.java               # @ControllerAdvice, обработка исключений  
│           │       └── ApiError.java                   # Модель ошибки (статус, сообщение, причина)  
│           │  
│           └── resources/  
│               ├── application.properties              # Порт 8080, настройки БД, URL stats-сервиса  
│               └── schema.sql                           # DDL для создания таблиц основного сервиса  
│  
├── ewm-stats-service/                                  # МОДУЛЬ 2: СЕРВИС СТАТИСТИКИ (порт 9090)  
│   ├── pom.xml                                          # Зависимости: stats-dto, Spring Web, JPA, PostgreSQL  
│   ├── Dockerfile                                       # Сборка образа сервиса статистики  
│   └── src/  
│       └── main/  
│           ├── java/ru/practicum/stats/  
│           │   ├── StatsServiceApplication.java         # Точка входа (@SpringBootApplication)  
│           │   │  
│           │   ├── controller/  
│           │   │   └── StatsController.java             # REST-контроллер статистики  
│           │   │       ├── POST /hit                    # Сохранить информацию о запросе  
│           │   │       └── GET /stats                    # Получить статистику с фильтрацией  
│           │   │  
│           │   ├── service/  
│           │   │   ├── StatsService.java                 # Интерфейс  
│           │   │   └── StatsServiceImpl.java             # Реализация логики статистики  
│           │   │  
│           │   ├── repository/  
│           │   │   └── StatsRepository.java              # JpaRepository для EndpointHitEntity  
│           │   │       # Кастомные @Query для агрегации данных с GROUP BY  
│           │   │  
│           │   ├── mapper/  
│           │   │   └── StatsMapper.java                  # EndpointHit <-> EndpointHitEntity  
│           │   │  
│           │   └── model/  
│           │       └── EndpointHitEntity.java            # JPA-сущность (таблица hits)  
│           │           # Поля: id, app, uri, ip, timestamp  
│           │  
│           └── resources/  
│               ├── application.properties                # Порт 9090, настройки БД статистики  
│               └── schema.sql                             # CREATE TABLE IF NOT EXISTS hits (...)  
│  
├── ewm-stats-client/                                     # МОДУЛЬ 3: КЛИЕНТ ДЛЯ СТАТИСТИКИ  
│   ├── pom.xml                                            # Зависимости: stats-dto, Spring Web, Apache HttpClient  
│   └── src/  
│       └── main/  
│           └── java/ru/practicum/stats/client/  
│               ├── StatsClient.java                       # 🔌 HTTP-клиент для stats-service  
│               │   # Инкапсулирует вызовы:  
│               │   # - hit() -> POST /hit  
│               │   # - getStats() -> GET /stats  
│               │  
│               ├── StatsClientConfig.java                 # Настройка RestClient  
│               │   # @Bean RestClient с таймаутами, логированием  
│               │  
│               └── exception/  
│                   └── StatsClientException.java          # Собственное исключение для ошибок клиента  
│  
└── ewm-stats-dto/                                         # МОДУЛЬ 4: ОБЩИЕ DTO ДЛЯ СТАТИСТИКИ  
├── pom.xml                                             # Lombok, Jackson, validation  
└── src/  
└── main/  
└── java/ru/practicum/stats/dto/  
├── EndpointHit.java                        # 📤 Запрос к POST /hit  
│   # Поля: app, uri, ip, timestamp  
│
└── ViewStats.java                           # 📥 Ответ от GET /stats  
Поля: app, uri, hits  
  

# Зависимости
![Визуализация зависимостей между модулями.png](%D0%92%D0%B8%D0%B7%D1%83%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B7%D0%B0%D0%B2%D0%B8%D1%81%D0%B8%D0%BC%D0%BE%D1%81%D1%82%D0%B5%D0%B9%20%D0%BC%D0%B5%D0%B6%D0%B4%D1%83%20%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8F%D0%BC%D0%B8.png)