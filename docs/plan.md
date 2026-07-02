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
- **Именование модулей:** `:layer:name:api` / `:layer:name:impl` (например `:feature:media:api`).
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
    ├── media/                              # каталог: фильмы / сериалы / актёры (фаза 8+, ранее discover)
    │   ├── api/
    │   └── impl/
    ├── person/                             # карточка актёра (фаза 11)
    │   ├── api/
    │   └── impl/
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
 ├─ login:impl, main:impl, media:impl, details:impl, search:impl, person:impl
 └─ core:*

media:impl     → media:api, auth:api, data:tmdb:api, core:ui
details:impl   → details:api, auth:api, data:tmdb:api, core:ui
search:impl    → search:api, auth:api, data:tmdb:api, core:ui
login:impl     → login:api, auth:api, core:ui
person:impl    → person:api, data:tmdb:api, core:ui
main:impl      → main:api, media:api, details:api, search:api, login:api, person:api, auth:api, core:ui

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

Каталог (popular, details, search) работает с **API Read Access Token** (v4 Bearer) в режиме `Guest`.
User session нужна только для account-фич (favorites, watchlist, рейтинги).

**Root всегда показывает `MainComponent`** — без переключения Unauthorized ↔ Authorized.

| Состояние | Доступ |
|-----------|--------|
| `Guest` | browse: popular, details, search |
| `Authenticated` | browse + favorites, watchlist, рейтинги |

### Платформенная стратегия login

Два способа входа; после получения `session_id` оба вызывают `AuthResultHandler.onLoginSuccess()` — дальше только `auth:impl`.

#### Способ A — через сайт (рекомендуемый TMDB)

`request_token` → подтверждение на themoviedb.org → `session_id`.

| Платформа | Механизм |
|-----------|----------|
| Android / iOS / Desktop | `LocalUriHandler.openUri()` → экран «Продолжить» в `login:impl` |

#### Способ B — логин и пароль (альтернатива без браузера)

`request_token` → `POST /authentication/token/validate_with_login` → `session_id`.

| Шаг | Endpoint / действие |
|-----|---------------------|
| 1 | `GET /authentication/token/new` |
| 2 | `POST /authentication/token/validate_with_login` (username, password, request_token) |
| 3 | `POST /authentication/session/new` |
| 4 | `GET /account` → `onLoginSuccess()` |

**Ограничения:** TMDB не рекомендует этот способ; пароль **не сохраняется** — только в RAM на время запроса. Обязателен HTTPS.

| Платформа | UI |
|-----------|-----|
| Все (CMP) | форма логин/пароль на экране профиля в `login:impl` (push с фазы 8) |

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

interface TmdbSessionClient {
    suspend fun createRequestToken(): AppResult<String>
    suspend fun validateWithLogin(requestToken: String, username: String, password: String): AppResult<String>
    suspend fun createSession(requestToken: String): AppResult<String>
    suspend fun getAccount(sessionId: String): AppResult<TmdbAccountInfo>
    suspend fun loginWithCredentials(username: String, password: String): AppResult<TmdbLoginResult>
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
| `SessionManager` | `SessionRepository` + `AuthResultHandler`; restore сессии из storage при старте |
| `TmdbSessionClient` | request_token, validate_with_login, create session, account через TMDB API |
| `LogoutCoordinator` | (в `SessionManager`) clear storage → `Guest` |

**Нет** `TokenRefresher` и зоны `Refreshing` — TMDB session долгоживущая.

### Persist сессии (`data:storage:impl`)

| Платформа | Реализация |
|-----------|------------|
| Android | `EncryptedSharedPreferences` + `MasterKey` (AES256, Android Keystore) |
| iOS / Desktop | `InMemorySessionStorage` (persistent — фаза 7) |

Хранится: `session_id`, `accountId`, `username`. Пароль TMDB **не** хранится.

### Обработка 401 на account endpoints

Ktor interceptor при 401 на account-запросах:
1. `logout()` → `Guest`;
2. Store эмитит Label `NavigateToLogin` (через auth state, не из UI напрямую).

### Владение credentials

| Слой | Роль |
|------|------|
| `config:impl` | API Read Access Token (не в git) |
| `storage:impl` | persist: session_id, accountId, username |
| `auth:impl` | session lifecycle, logout, `SessionState` |
| `tmdb:impl` | Bearer token из `TmdbConfig`; session_id через `SessionRepository` |

### Скрытие account-фич

1. **UI** — кнопки favorites/watchlist видны только при `Authenticated`; для Guest — «Войти».
2. **Store** — intent `AddToFavorites` при Guest → Label `NavigateToLogin`.
3. **Repository** — account-методы требуют `session_id`; без сессии — `NotAuthenticated`.

---

## Навигация (Decompose)

### Сейчас (фазы 1–7)

```
RootComponent                              [:app]
└─ MainComponent                           [:feature:main]
     ├─ DiscoverComponent (tab)            [:feature:discover]  → media в фазе 8
     ├─ SearchComponent (tab)              [:feature:search]
     ├─ ProfileComponent (tab)             [:feature:login]
     └─ DetailsComponent (child stack)     [:feature:details]
```

### Целевая (фаза 8+)

```
RootComponent                              [:app]
└─ MainComponent                           [:feature:main]
     ├─ MediaComponent (основной экран)    [:feature:media]
     │    ├─ top: 🔍 Search (push)         [:feature:search]
     │    ├─ top: 👤 Profile (push)        [:feature:login]   ← фаза 9: избранное
     │    ├─ FAB: фильтры (bottom sheet)   ← фаза 8b
     │    └─ bottom: Фильмы | Сериалы | Актёры  (ContentType)
     ├─ DetailsComponent (movie/tv)        [:feature:details]
     └─ PersonComponent                    [:feature:person]    ← фаза 11
```

- Root **не** переключает зоны — всегда `MainComponent`.
- Поиск и профиль — **push** в child stack, не нижние табы.
- Login — экран профиля или Label `NavigateToLogin` из других фич.
- Logout: `sessionRepository.logout()` → `Guest`, Main остаётся, account UI скрывается.

### Паттерн на экран

- `*Component` (Decompose) — `Value<Model>`, `onIntent()`, навигация по `Label`;
- `*Store` (MVIKotlin) — `Intent` → `State` → `Label`;
- UI (`@Composable`) — подписка на state, вызов intents.

---

## Первая фича: каталог (`feature:discover` → `feature:media`)

> **Фазы 3–7:** реализовано как `feature:discover` (вертикальный список popular).
> **Фаза 8:** переименование в `feature:media`, новый UI и shell (см. план по этапам).

### Каталог MVP (фазы 3–7, legacy)

- таб «Главная» в `main-impl`;
- список popular с постерами;
- pull-to-refresh, пагинация при скролле;
- клик → `DetailsComponent`;
- состояния: Loading / Error / Empty / Content.

### Главный экран (`feature:media`, фаза 8+)

Один feature-модуль для **фильмов / сериалов / актёров** — общий grid UI, различие по `ContentType` в Store.

#### Layout

| Зона | Поведение |
|------|-----------|
| Top-left | 🔍 → push `SearchComponent` |
| Top-right | 👤 → push `LoginComponent` / профиль (фаза 9) |
| Контент | `LazyVerticalGrid`, **2 колонки**, фиксированная высота ячейки, постер 2:3 + название |
| FAB | Фильтры → bottom sheet (фаза 8b; только для фильмов) |
| Bottom bar | **Фильмы** \| **Сериалы** \| **Актёры** (`ContentType`) |

#### Ячейка сетки (`core:ui`)

- Постер (`ContentScale.Crop`) + название под ним (max 2 строки, ellipsis).
- Опционально badge рейтинга на постере.
- Фиксированная высота ячейки — без `wrapContent` по вертикали.

#### MVIKotlin `MediaStore`

| | |
|---|---|
| **Intent** | `Load`, `LoadMore`, `OnItemClick`, `OnContentTypeSelected`, `OnSearchClick`, `OnProfileClick`, `OnFilterClick`, `ApplyFilters`, `ResetFilters` |
| **State** | `contentType`, `items`, `isLoading`, `isLoadingMore`, `error`, `page`, `endReached`, `filters` (фаза 8b) |
| **Label** | `NavigateToDetails(id)`, `NavigateToPerson(id)`, `NavigateToSearch`, `NavigateToProfile`, `ShowFilterSheet`, `ShowError(message)` |

#### Детали по типу контента

| ContentType | Каталог | Детали |
|-------------|---------|--------|
| `Movies` | grid + пагинация | `feature:details` (фильм) |
| `Tv` | grid + пагинация (фаза 10) | `feature:details` (сериал, расширение) |
| `People` | grid, круглое фото (фаза 11) | `feature:person` |

Фильмы и сериалы — **общий каркас** экрана деталей; актёр — **отдельный** модуль `feature:person`.

#### Поток данных

```
MediaContent (Compose)
  → MediaComponent.onIntent
  → MediaStore
  → MediaRepository / MovieRepository (tmdb-impl)
  → TmdbApiClient (Bearer из TmdbConfig)
```

---

## Фича: детали (`feature:details`)

| Endpoint | Назначение |
|----------|------------|
| `GET /movie/{movie_id}` | название, описание, рейтинг, жанры, runtime |
| `GET /movie/{movie_id}/credits` | актёры (будущее расширение) |
| `GET /tv/{series_id}` | детали сериала (фаза 10) |

Label из media/search: `NavigateToDetails(id)` → push в child stack `main-impl`.
Сериал — тот же модуль `details`, общий каркас UI с фильмом; актёр — `feature:person` (фаза 11).

---

## Main-модуль

`main-impl` — **тонкий shell**: навигация + child stack. Без бизнес-логики.

| Эра | Shell |
|-----|-------|
| Фазы 3–7 | Bottom: Discover / Search / Profile + stack Details |
| Фаза 8+ | `MediaComponent` + bottom ContentType (Фильмы/Сериалы/Актёры); Search и Profile — push; stack Details / Person |

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
3. `config:impl` читает значение в generated constant.

### Сейчас (фаза 7+)

- Используется **Ключ доступа к API** (v4): заголовок `Authorization: Bearer <token>` в `HttpClient`.
- В `local.properties`:

```properties
TMDB_ACCESS_TOKEN=your_v4_read_access_token_here
```

- Fallback при генерации: если `TMDB_ACCESS_TOKEN` не задан, читается `TMDB_API_KEY` (для обратной совместимости; v3-ключ **не** работает как Bearer — нужен v4 token).

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
    mediaFeatureModule,        // ранее discoverFeatureModule (фаза 8)
    detailsFeatureModule,
    searchFeatureModule,
    loginFeatureModule,
    personFeatureModule,       // фаза 11
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

- [x] `auth:impl`: TMDB session flow (`TmdbSessionClient`, `SessionManager`)
- [x] `login:impl`: UI — вход через сайт + альтернатива логин/пароль
- [x] Profile tab: Guest → login, Authenticated → профиль + logout
- [x] Account-фичи: favorites на details (скрытие для Guest, `NavigateToLogin`)
- [x] Android: encrypted session storage (Keystore / `EncryptedSharedPreferences`)
- [x] `AccountRepository`: favorites + logout при 401

#### Login UI (`feature:login:impl`)

| Intent | Действие |
|--------|----------|
| `Login` | browser flow: token → `OpenBrowser` → `ConfirmApproval` |
| `LoginWithCredentials` | `loginWithCredentials()` без браузера |
| `Logout` | `sessionRepository.logout()` |

#### TMDB API (auth)

| Endpoint | Назначение |
|----------|------------|
| `GET /authentication/token/new` | request_token |
| `POST /authentication/token/validate_with_login` | альтернативный вход (логин/пароль) |
| `POST /authentication/session/new` | session_id |
| `GET /account` | accountId, username |
| `GET /movie/{id}/account_states` | isFavorite |
| `POST /account/{id}/favorite` | add/remove favorite |

### Фаза 7 — Полировка ✅

- [x] Android: Custom Tabs; iOS: SFSafariViewController; Desktop: `Desktop.browse()`
- [x] iOS: persistent session storage (Keychain)
- [x] Desktop: persistent encrypted session storage (AES-GCM file)
- [x] Shimmer, улучшение error states (`core:ui`)
- [x] **Миграция на Ключ доступа к API** (v4 Bearer вместо `api_key` query param)
- [x] Тесты: `DiscoverStore`, `SessionManager` с fakes

### Фаза 8 — Главный экран + Media (фильмы)

- [ ] Переименовать `feature:discover` → `feature:media` (api/impl, пакеты `pas.tmdb.feature.media.*`)
- [ ] `main:impl`: новый shell — top bar (🔍 / 👤), bottom bar (Фильмы / Сериалы / Актёры)
- [ ] Убрать нижние табы Search и Profile; Search и Profile — **push** в child stack
- [ ] `media:impl`: `LazyVerticalGrid` 2 колонки, фиксированная высота ячейки, бесконечная пагинация
- [ ] `core:ui`: `MediaPosterGrid` / `MediaPosterCell` (постер 2:3 + название)
- [ ] Данные: `GET /movie/popular`, `language=ru-RU`, `page`
- [ ] Вкладки **Сериалы** и **Актёры** — заглушка «Скоро»
- [ ] Shimmer / error / empty для grid
- [ ] Клик по фильму → `DetailsComponent`
- [ ] Тесты: `MediaStore` с fakes

#### Навигация (фаза 8)

| Действие | Переход |
|----------|---------|
| 🔍 | `NavigateToSearch` → push Search |
| 👤 | `NavigateToProfile` → push Login/Profile |
| Клик по постеру | push Details |
| Bottom: Фильмы | `ContentType.Movies` |
| Bottom: Сериалы / Актёры | заглушка |

### Фаза 8b — Фильтры (только фильмы)

- [ ] FAB на экране Media → bottom sheet
- [ ] Фильтры MVP: **жанр** (dropdown), **сортировка** (популярность / дата / рейтинг)
- [ ] `GET /genre/movie/list` — список жанров
- [ ] При активных фильтрах: `GET /discover/movie` (`with_genres`, `sort_by`)
- [ ] Без фильтров: `GET /movie/popular` (как в фазе 8)
- [ ] Кнопки «Применить» / «Сбросить»; сброс перезагружает popular
- [ ] FAB скрыт или disabled для вкладок Сериалы / Актёры

#### TMDB API (фаза 8b)

| Endpoint | Назначение |
|----------|------------|
| `GET /discover/movie` | каталог с фильтрами |
| `GET /genre/movie/list` | жанры для dropdown |

| `sort_by` (примеры) | UI |
|---------------------|-----|
| `popularity.desc` | По популярности |
| `primary_release_date.desc` | По дате выхода |
| `vote_average.desc` | По оценке |

### Фаза 9 — Профиль 2.0

- [ ] Профиль — полноэкранный push (не таб)
- [ ] **Guest:** экран входа (как сейчас в `login:impl`)
- [ ] **Authenticated:** имя пользователя, logout
- [ ] **Избранное** — сетка 2 колонки (тот же `MediaPosterGrid` из `core:ui`)
- [ ] `GET /account/{account_id}/favorite/movies` — список избранного (как на сайте TMDB)
- [ ] Клик по избранному → Details; toggle ★ на Details без изменений
- [ ] Guest: секция избранного скрыта или CTA «Войти»

#### TMDB API (фаза 9)

| Endpoint | Назначение |
|----------|------------|
| `GET /account/{account_id}/favorite/movies` | список избранных фильмов |

### Фаза 10 — Сериалы

- [ ] Вкладка **Сериалы** в `MediaComponent` — рабочий каталог
- [ ] `GET /discover/tv` или `GET /tv/popular` — grid + пагинация
- [ ] `data:tmdb`: модели `TvShow`, `TvRepository` / методы в `MediaRepository`
- [ ] `feature:details`: расширение для сериала (`GET /tv/{id}`) — общий каркас с фильмом + сезоны/эпизоды/статус
- [ ] `feature:search`: `GET /search/tv` при открытии поиска с активной вкладкой Сериалы
- [ ] Фильтры для TV — отдельная итерация (после 8b, по аналогии с `discover/tv`)

#### TMDB API (фаза 10)

| Endpoint | Назначение |
|----------|------------|
| `GET /tv/popular` | каталог сериалов (MVP) |
| `GET /tv/{series_id}` | детали сериала |
| `GET /search/tv` | поиск сериалов |

### Фаза 11 — Актёры

- [ ] Вкладка **Актёры** в `MediaComponent` — рабочий каталог
- [ ] `GET /person/popular` — grid (круглое фото + имя), пагинация
- [ ] `feature:person:api/impl` — экран актёра (`GET /person/{id}`, filmography)
- [ ] `feature:search`: `GET /search/person`
- [ ] Push `PersonComponent` из media/search (отдельно от movie Details)
- [ ] FAB фильтров на вкладке Актёры — не в MVP (или только сортировка позже)

#### TMDB API (фаза 11)

| Endpoint | Назначение |
|----------|------------|
| `GET /person/popular` | каталог актёров |
| `GET /person/{person_id}` | карточка актёра |
| `GET /search/person` | поиск актёров |

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
| Browse без login | Да, через API Read Access Token (v4 Bearer) |
| Main | Тонкий shell; фаза 8+: media + push search/profile |
| Каталог (главная) | Один модуль `feature:media` — Movies / Tv / People по `ContentType` |
| Первая фича | `feature:discover` (фазы 3–7) → `feature:media` (фаза 8+) |
| Grid главной | 2 колонки, фикс. высота ячейки, постер + название, пагинация |
| Фильтры | FAB + bottom sheet; MVP: жанр + сортировка (только фильмы, фаза 8b) |
| Детали | Фильм и сериал — `feature:details`; актёр — `feature:person` |
| Профиль | Полноэкранный push; избранное — сетка как на главной (фаза 9) |
| Язык API | `ru-RU` |
| Gradle | Convention plugins в `build-logic` |
| Пакет | `pas.tmdb.*` |
