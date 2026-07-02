# TMDB KMP — план проекта

Пет-проект на Kotlin Multiplatform с [TMDB API](https://developer.themoviedb.org/docs).

## Стек

| Слой | Технология |
|------|------------|
| UI | Compose Multiplatform |
| Навигация | Decompose |
| Состояние | MVIKotlin |
| Сеть | Ktor Client + kotlinx.serialization |
| Хранение | DataStore / platform secure storage |
| Изображения | Coil |
| DI | Koin |
| Платформы | Android, iOS, Desktop (JVM) |

## Архитектурные принципы

- **Многомодульность:** `core` / `data` / `auth` / `feature`, каждый слой разделён на **api** и **impl**.
- **Именование модулей:** `:layer:name:api` / `:layer:name:impl` (например `:feature:discover:api`).
- **Зависимости:** feature-модули зависят только от **api** других модулей, никогда от чужих **impl**.
- **Сборка DI:** только модуль `app` подключает все **impl** и собирает граф зависимостей.
- **UI:** весь UI на Compose; бизнес-логика и навигация — в shared.
- **Auth:** мягкая сессия `Guest` / `Authenticated` — каталог доступен всегда; login разблокирует account-фичи.

---

## Модули

```
tmdb-kmp/
├── build-logic/
│   └── convention/                         # convention plugins
│
├── app/                                    # entry point, DI root, RootComponent
│
├── core/
│   ├── common/                             # Result, extensions, DispatcherProvider
│   ├── ui/                                 # Theme, design system, common composables
│   ├── navigation/                         # Decompose utils, serializers
│   └── network/                            # HttpClient factory, logging (без TMDB-специфики)
│
├── auth/
│   ├── api/                                # :auth:api — SessionState, SessionRepository
│   └── impl/                               # :auth:impl — SessionManager, TMDB session flow
│
├── data/
│   ├── config/
│   │   ├── api/                            # TmdbConfig (api key, base url)
│   │   └── impl/                           # BuildConfig / expect-actual
│   ├── storage/
│   │   ├── api/
│   │   └── impl/
│   └── tmdb/
│       ├── api/                            # DTO, repository interfaces
│       └── impl/                           # Ktor, mappers, TmdbApiClient
│
└── feature/
    ├── login/
    │   ├── api/
    │   └── impl/                           # Login UI + PlatformAuthLauncher
    ├── main/
    │   ├── api/
    │   └── impl/                           # shell: tabs, child stacks
    ├── discover/
    │   ├── api/
    │   └── impl/                           # первая фича — каталог
    ├── details/
    │   ├── api/
    │   └── impl/                           # карточка фильма
    └── search/
        ├── api/
        └── impl/                           # поиск (фаза 5)
```

### Граф зависимостей

```
app
 ├─ auth:impl, storage:impl, config:impl, tmdb:impl
 ├─ login:impl, main:impl, discover:impl, details:impl, search:impl
 └─ core:*

discover:impl  → discover:api, auth:api, data:tmdb:api, core:ui
details:impl   → details:api, auth:api, data:tmdb:api, core:ui
search:impl    → search:api, auth:api, data:tmdb:api, core:ui
login:impl     → login:api, auth:api, core:ui
main:impl      → main:api, discover:api, details:api, search:api, login:api, auth:api, core:ui

auth:impl      → auth:api, storage:api, data:tmdb:api, core:network
tmdb:impl      → tmdb:api, config:api, auth:api, core:network
storage:impl   → storage:api
config:impl    → config:api

*:api          → core:common (минимум)
feature-*:api  → decompose (типы Component)
```

---

## Auth-модуль

Auth — **не экран логина**, а инфраструктура сессии. UI логина — в `feature:login:impl`.

### Модель сессии (мягкая, без зон Root)

Каталог (popular, details, search) работает с **API key** в режиме `Guest`.
User session нужна только для account-фич (favorites, watchlist, рейтинги).

**Root всегда показывает `MainComponent`** — без переключения Unauthorized ↔ Authorized.

| Состояние | Доступ |
|-----------|--------|
| `Guest` | browse: popular, details, search |
| `Authenticated` | browse + favorites, watchlist, рейтинги |

### Платформенная стратегия login

TMDB user flow: `request_token` → подтверждение на themoviedb.org → `session_id`.

| Платформа | Механизм |
|-----------|----------|
| Android | Custom Tabs / WebView → `PlatformAuthLauncher` (actual) |
| iOS | ASWebAuthenticationSession → `PlatformAuthLauncher` (actual) |
| Desktop | браузер + localhost callback → `OAuthClient` + `BrowserLauncher` (actual) |

После успеха все платформы вызывают `AuthResultHandler.onLoginSuccess()` — дальше только `auth:impl`.

### Контракты (`auth:api`)

```kotlin
sealed interface SessionState {
    data object Guest : SessionState
    data class Authenticated(
        val accountId: Int,
        val username: String?,
    ) : SessionState
}

interface SessionRepository {
    val sessionState: StateFlow<SessionState>
    suspend fun getSessionId(): String?          // null для Guest
    suspend fun logout()
}

interface AuthResultHandler {
    suspend fun onLoginSuccess(sessionId: String, accountId: Int, username: String?)
}

interface AuthLauncher {
    suspend fun launch(): AuthLaunchResult
}

sealed interface AuthLaunchResult {
    data class Success(val sessionId: String) : AuthLaunchResult
    data object Cancelled : AuthLaunchResult
    data class Error(val message: String) : AuthLaunchResult
}
```

### Реализация (`auth:impl`)

| Компонент | Роль |
|-----------|------|
| `AuthResultHandlerImpl` | save session → `SessionState.Authenticated` |
| `SessionManager` | `SessionRepository`, единый источник правды |
| `TmdbSessionClient` | request_token, create session через TMDB API |
| `LogoutCoordinator` | clear storage, `Guest` |

**Нет** `TokenRefresher` и зоны `Refreshing` — TMDB session долгоживущая.

### Обработка 401 на account endpoints

Ktor interceptor при 401 на account-запросах:
1. `logout()` → `Guest`;
2. Store эмитит Label `NavigateToLogin` (через auth state, не из UI напрямую).

### Владение credentials

| Слой | Роль |
|------|------|
| `config:impl` | API key / read token (не в git) |
| `storage:impl` | persist: session_id, accountId, username |
| `auth:impl` | session lifecycle, logout, `SessionState` |
| `tmdb:impl` | API key из `TmdbConfig`; session_id через `SessionRepository` |

### Скрытие account-фич

1. **UI** — кнопки favorites/watchlist видны только при `Authenticated`; для Guest — «Войти».
2. **Store** — intent `AddToFavorites` при Guest → Label `NavigateToLogin`.
3. **Repository** — account-методы требуют `session_id`; без сессии — `NotAuthenticated`.

---

## Навигация (Decompose)

```
RootComponent                              [:app]
└─ MainComponent                           [:feature:main]
     ├─ DiscoverComponent (tab)            [:feature:discover]
     ├─ SearchComponent (tab)              [:feature:search]   ← фаза 5
     ├─ ProfileComponent (tab)             [:feature:login]   ← Guest: login, Auth: профиль + logout
     └─ DetailsComponent (child stack)     [:feature:details]  ← push из discover/search
```

- Root **не** переключает зоны — всегда `MainComponent`.
- Login — таб «Профиль» или Label `NavigateToLogin` из других фич.
- Logout: `sessionRepository.logout()` → `Guest`, Main остаётся, account UI скрывается.

### Паттерн на экран

- `*Component` (Decompose) — `Value<Model>`, `onIntent()`, навигация по `Label`;
- `*Store` (MVIKotlin) — `Intent` → `State` → `Label`;
- UI (`@Composable`) — подписка на state, вызов intents.

---

## Первая фича: каталог (`feature:discover`)

### MVP

- таб «Главная» в `main-impl`;
- список popular / trending с постерами;
- pull-to-refresh;
- пагинация при скролле;
- клик → `DetailsComponent`;
- состояния: Loading / Error / Empty / Content;
- account-кнопки скрыты для Guest.

### MVIKotlin `DiscoverStore`

| | |
|---|---|
| **Intent** | `Load`, `Refresh`, `LoadMore`, `OnMovieClick` |
| **State** | `movies`, `isLoading`, `isRefreshing`, `error`, `page`, `endReached`, `sessionState` |
| **Label** | `NavigateToDetails(movieId)`, `ShowError(message)` |

### TMDB API (MVP)

| Endpoint | Назначение |
|----------|------------|
| `GET /movie/popular` | популярные фильмы |
| `GET /trending/movie/day` | тренды (опционально, вторая секция) |
| `GET /configuration` | base URL для постеров |

Параметры: `api_key`, `language=ru-RU`, `page`.

### Поток данных

```
DiscoverContent (Compose)
  → DiscoverComponent.onIntent
  → DiscoverStore
  → GetPopularMoviesUseCase
  → MovieRepository (tmdb-impl)
  → TmdbApiClient (+ api_key из TmdbConfig)
```

---

## Фича: детали (`feature:details`)

| Endpoint | Назначение |
|----------|------------|
| `GET /movie/{movie_id}` | название, описание, рейтинг, жанры, runtime |
| `GET /movie/{movie_id}/credits` | актёры (фаза 4+) |

Label из discover: `NavigateToDetails(movieId)` → push в child stack `main-impl`.

---

## Main-модуль

`main-impl` — **тонкий shell**: bottom navigation + tab navigator + child stack для details. Без бизнес-логики.

Каждый таб — отдельный feature-модуль (`discover`, `search`, `login`/profile).

---

## Convention plugins (`build-logic`)

| Plugin ID | Модули | Включает |
|-----------|--------|----------|
| `tmdb.kmp.library` | `core:common`, `core:navigation`, `core:network`, `data:*:api`, `auth:*` | kmp + android + ios + desktop |
| `tmdb.cmp.library` | `core:ui` | + compose |
| `tmdb.kmp.data.impl` | `data:*:impl` | + ktor, serialization |
| `tmdb.kmp.feature.api` | `feature:*:api` | + decompose |
| `tmdb.kmp.feature.impl` | `feature:*:impl` | + compose, mvikotlin |
| `tmdb.kmp.app` | `app` | application, все impl |

Фиксируют: `compileSdk`, `minSdk`, targets (`android`, `iosArm64`, `iosSimulatorArm64`, `jvm("desktop")`).

---

## API key

1. Зарегистрировать приложение на [developer.themoviedb.org](https://developer.themoviedb.org/).
2. Ключ хранить в `local.properties` — **не коммитить** в git.
3. `config:impl` читает значение в generated constant / expect-actual.

### Сейчас (фаза 2)

- Используется **Ключ API** (v3): query-параметр `api_key` в `TmdbApiClient`.
- В `local.properties`:

```properties
TMDB_API_KEY=your_v3_api_key_here
```

### План: перейти на Ключ доступа к API

- Целевой способ — **API Read Access Token** (v4): заголовок `Authorization: Bearer <token>`.
- Единый механизм auth для v3/v4 эндпоинтов; ключ не попадает в URL.
- Изменения: `TmdbConfig` → bearer token, `HttpClient` default request / `TmdbApiClient` — убрать `parameter("api_key", ...)`.
- Задача зафиксирована в **фазе 7** (можно выполнить раньше, до auth).

```properties
# целевое имя свойства (после миграции)
TMDB_ACCESS_TOKEN=your_v4_read_access_token_here
```

---

## Desktop — особенности login

1. Нет мобильного WebView SDK → `OAuthClient` в `auth:impl`.
2. Redirect: `http://127.0.0.1:<port>/callback` или custom URL scheme.
3. Браузер: `java.awt.Desktop.getDesktop().browse(uri)`.
4. Хранение session: encrypted file / Java Preferences в `storage:impl desktopMain`.

---

## DI (Koin) — порядок модулей

```kotlin
modules(
    coreModule,
    configImplModule,
    storageImplModule,
    authImplModule,
    tmdbImplModule,
    discoverFeatureModule,
    detailsFeatureModule,
    searchFeatureModule,
    loginFeatureModule,
    mainFeatureModule,
    appModule,             // RootComponent factory
)
```

---

## План по этапам

### Фаза 1 — Скелет ✅

- [x] Gradle: `build-logic` + пустые модули
- [x] `app`: Compose, Root → Main placeholder
- [x] Convention plugins
- [x] `local.properties` + `config:impl` для API key

### Фаза 2 — Сеть + TMDB ✅

- [x] `core:network`: HttpClient, JSON, debug-логирование
- [x] `tmdb:impl`: DTO, `TmdbApiClient`, `MovieRepository`
- [x] `GET /movie/popular`, `GET /configuration`
- [x] Mappers → domain models

### Фаза 3 — Discover ✅

- [x] `discover:impl`: Store + UI
- [x] `main:impl`: таб Discover (bottom navigation)
- [x] Pull-to-refresh, pagination, error/empty states
- [x] Coil для постеров

### Фаза 4 — Details ✅

- [x] `details:impl`: Store + UI
- [x] Child stack в `main-impl`
- [x] `GET /movie/{id}`

### Фаза 5 — Search ✅

- [x] `search:impl`: Store + UI (`GET /search/movie`)
- [x] Debounce ввода, пагинация, состояния (empty/loading/error/not found)
- [x] Второй таб в main + переход в Details

### Фаза 6 — Auth ✅

- [x] `auth:impl`: TMDB session flow
- [x] `login:impl`: UI + platform launchers
- [x] Profile tab: Guest → login, Authenticated → logout
- [x] Account-фичи: favorites (скрытие для Guest)

### Фаза 7 — Полировка
- iOS + Desktop launchers
- Shimmer, улучшение error states
- **Миграция на Ключ доступа к API** (v4 Bearer вместо `api_key` query param)
- Тесты: `DiscoverStore`, `SessionManager` с fakes

---

## Зафиксированные решения

| Вопрос | Решение |
|--------|---------|
| Платформы | Android + iOS + Desktop |
| UI | Compose Multiplatform |
| Архитектура UI/State | Decompose + MVIKotlin |
| Структура модулей | `:layer:name:api` / `:layer:name:impl` |
| Auth | Мягкая сессия Guest/Authenticated; Root всегда Main |
| Auth-модуль | Session, login flow, logout — не UI логина |
| Browse без login | Да, через API key (v3 сейчас; v4 Bearer — см. фазу 7) |
| Main | Тонкий shell, табы = отдельные feature-модули |
| Первая фича | `feature:discover` — popular movies |
| Язык API | `ru-RU` |
| Gradle | Convention plugins в `build-logic` |
| Пакет | `pas.tmdb.*` |
