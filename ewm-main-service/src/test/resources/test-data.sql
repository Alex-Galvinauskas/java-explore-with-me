-- Очистка таблиц перед загрузкой тестовых данных
DELETE FROM comments;
DELETE FROM requests;
DELETE FROM events;
DELETE FROM users;
DELETE FROM categories;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE events ALTER COLUMN id RESTART WITH 1;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 1;

-- Создание категории
INSERT INTO categories (id, name) VALUES (1, 'Концерты');

-- Создание пользователей
INSERT INTO users (id, email, name) VALUES
    (1, 'user1@test.com', 'Test User 1'),
    (2, 'user2@test.com', 'Test User 2');

-- Создание событий
INSERT INTO events (
    id, annotation, category_id, confirmed_requests, created_on,
    description, event_date, initiator_id, lat, lon, paid,
    participant_limit, published_on, request_moderation, state, title, views
) VALUES
    (
        1, 'Тестовое событие 1', 1, 0, '2024-01-01 10:00:00',
        'Описание тестового события 1', '2024-12-31 20:00:00', 1,
        55.7558, 37.6173, false, 0, '2024-01-01 12:00:00',
        true, 'PUBLISHED', 'Тестовое событие 1', 100
    ),
    (
        2, 'Тестовое событие 2', 1, 0, '2024-01-01 10:00:00',
        'Описание тестового события 2', '2024-12-31 20:00:00', 1,
        55.7558, 37.6173, false, 0, '2024-01-01 12:00:00',
        true, 'PUBLISHED', 'Тестовое событие 2', 50
    ),
    (
        3, 'Неопубликованное событие', 1, 0, '2024-01-01 10:00:00',
        'Описание неопубликованного события', '2024-12-31 20:00:00', 1,
        55.7558, 37.6173, false, 0, NULL,
        true, 'PENDING', 'Неопубликованное событие', 0
    );