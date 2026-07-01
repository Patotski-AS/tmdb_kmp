# План проекта tmdb_kmp

Пет-проект на Kotlin Multiplatform с данными из [TMDB API](https://developer.themoviedb.org/docs).

**Рабочая папка:** `D:\Develop\Android\simple\tmdb_kmp`

---

## Цель

Собрать небольшое, но аккуратно спроектированное приложение для просмотра фильмов из TMDB: список популярных, поиск и экран деталей. Код должен демонстрировать KMP, Clean Architecture и работу с REST API.

## MVP — функциональность

| # | Фича | Эндпоинт TMDB | Приоритет |
|---|------|---------------|-----------|
| 1 | Список популярных фильмов с пагинацией | `GET /movie/popular` | P0 |
| 2 | Поиск фильмов | `GET /search/movie` | P0 |
| 3 | Детали фильма | `GET /movie/{id}` | P0 |
| 4 | Избранное (локально) | — | P1 |
| 5 | Pull-to-refresh / retry при ошибке сети | — | P1 |

## Экраны

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  PopularMovies  │────▶│   MovieDetails  │     │                 │
│  (стартовый)    │     │                 │     │                 │
└────────┬────────┘     └─────────────────┘     │                 │
         │                                       │                 │
         ▼                                       │                 │
┌─────────────────┐                              │                 │
│  SearchMovies   │──────────────────────────────┘                 │
└─────────────────┘
```

**PopularMovies** — сетка/список карточек (постер, название, год, рейтинг), бесконечная прокрутка.
**SearchMovies** — поле ввода с debounce, результаты поиска.
**MovieDetails** — постер, описание, жанры, длительность, рейтинг, кнопка «В избранное».

---

## Структура модулей

```
tmdb_kmp/
├── androidApp/              # Application, MainActivity, Theme, Navigation
├── shared/
│   └── src/
│       ├── commonMain/kotlin/
│       │   └── com/.../tmdb/
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   ├── repository/
│       │       │   └── usecase/
│       │       ├── data/
│       │       │   ├── remote/       # TmdbApi, DTO, HttpClient
│       │       │   ├── local/        # SQLDelight (этап P1)
│       │       │   ├── mapper/
│       │       │   └── repository/
│       │       ├── presentation/
│       │       │   ├── popular/
│       │       │   ├── search/
│       │       │   └── details/
│       │       └── di/
│       ├── androidMain/
│       └── commonTest/
├── gradle/
│   └── libs.versions.toml
├── PLAN.md
├── README.md
└── local.properties         # TMDB_API_KEY (не в git)
```

---

## Этапы разработки

### Этап 0 — Подготовка репозитория
- [x] Создать git-репозиторий
- [x] Правила Cursor (`.cursor/rules/project.mdc`)
- [x] План (`PLAN.md`)
- [ ] Удалить/заменить артефакты от предыдущего проекта (если остались локально)
- [ ] Получить API Read Access Token на [TMDB](https://www.themoviedb.org/settings/api)

### Этап 1 — Скелет KMP
- [ ] Инициализировать проект: `shared` + `androidApp` (Kotlin Multiplatform + Compose)
- [ ] Настроить `libs.versions.toml`
- [ ] Подключить Koin, Ktor, kotlinx.serialization
- [ ] Добавить `local.properties` → `BuildConfig` / `gradle` для API-ключа
- [ ] Базовая тема Material 3 и навигация (Compose Navigation)

### Этап 2 — Data layer
- [ ] `TmdbApi` — интерфейс Ktor с эндпоинтами popular, search, details
- [ ] DTO + `@Serializable` модели ответов TMDB
- [ ] Мапперы DTO → domain (`Movie`, `MovieDetails`, `PagedMovies`)
- [ ] `MovieRepository` + реализация
- [ ] Unit-тесты мапперов

### Этап 3 — Domain layer
- [ ] Domain-модели
- [ ] Use cases: `GetPopularMovies`, `SearchMovies`, `GetMovieDetails`
- [ ] Обработка ошибок (сеть, пустой ответ, 404)

### Этап 4 — Presentation: Popular
- [ ] `PopularMoviesViewModel` + `PopularMoviesUiState`
- [ ] UI: LazyVerticalGrid / LazyColumn с Coil для постеров
- [ ] Пагинация (подгрузка следующей страницы)
- [ ] Состояния: Loading / Content / Error / Empty

### Этап 5 — Presentation: Search
- [ ] `SearchViewModel` с debounce 300–500 ms
- [ ] UI поиска и список результатов
- [ ] Переход на детали

### Этап 6 — Presentation: Details
- [ ] `MovieDetailsViewModel`
- [ ] UI: hero-постер, overview, метаданные
- [ ] Обработка отсутствующего постера (`placeholder`)

### Этап 7 — Избранное (P1)
- [ ] SQLDelight: таблица `favorite_movies`
- [ ] `FavoriteRepository`
- [ ] Кнопка избранного на деталях + экран/вкладка избранного

### Этап 8 — Полировка
- [ ] Pull-to-refresh
- [ ] Snackbar при ошибках + кнопка «Повторить»
- [ ] Shimmer / skeleton при загрузке
- [ ] README: скриншоты, инструкция по API-ключу, архитектура

---

## Модели данных (domain)

```kotlin
data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseYear: String?,
    val voteAverage: Double,
)

data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val genres: List<String>,
    val voteAverage: Double,
)

data class PagedMovies(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
)
```

---

## Зависимости (ориентир)

```toml
# gradle/libs.versions.toml — ключевые библиотеки
kotlin = "2.1.x"
compose = "1.7.x"
ktor = "3.0.x"
koin = "4.0.x"
coil = "3.0.x"
sqldelight = "2.0.x"
```

---

## Риски и решения

| Риск | Решение |
|------|---------|
| Лимиты TMDB API | Кэшировать ответы; не спамить search без debounce |
| Утечка API-ключа | Только `local.properties`, проверить `.gitignore` |
| Сложность KMP | Начать с Android-only target в `shared`, iOS отложить |
| Большие постеры | Размер `w500` для списка, `w780` для деталей |

---

## Критерии готовности MVP

- [ ] Приложение запускается на Android-эмуляторе/устройстве
- [ ] Отображается список популярных фильмов с TMDB
- [ ] Работает поиск и переход на детали
- [ ] Ошибки сети показываются пользователю
- [ ] API-ключ не попадает в git
- [ ] Есть минимум unit-тестов на мапперы

---

## После MVP (идеи)

- Тёмная тема
- Фильтр по жанрам (`/discover/movie`)
- iOS target
- CI (GitHub Actions): сборка + тесты
