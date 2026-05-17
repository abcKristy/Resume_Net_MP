# Полный анализ проекта Resume Analyzer

## Содержание

1. [Общее описание проекта](#1-общее-описание-проекта)
2. [Технологический стек](#2-технологический-стек)
3. [Архитектура проекта](#3-архитектура-проекта)
4. [Модели данных (Domain)](#4-модели-данных-domain)
5. [UseCase'ы (Domain слой)](#5-usecaseы-domain-слой)
6. [База данных (Room)](#7-база-данных-room)
7. [ML и анализ резюме](#8-ml-и-анализ-резюме)
8. [UI (Presentation слой)](#9-ui-presentation-слой)
9. [Навигация](#11-навигация)
10. [DI (Внедрение зависимостей)](#12-di-внедрение-зависимостей)
11. [Управление памятью и производительность](#13-управление-памятью-и-производительность)
12. [Обработка ошибок](#14-обработка-ошибок)
13. [Сборка и запуск](#15-сборка-и-запуск)

---

## 1. Общее описание проекта

### Назначение приложения
**Resume Analyzer** (бренд "HireMind") — Android-приложение для автоматического анализа резюме с использованием нейросетевой модели PyTorch. Приложение оценивает качество резюме по 20 критериям, выявляет проблемы и дает рекомендации по улучшению.

### Ключевые функции
- **Анализ резюме**: ML-модель анализирует текст резюме, выставляет оценку от 1 до 5 и определяет вероятности 20 проблемных тегов
- **Кэширование результатов**: SHA-256 хеширование текста, хранение результатов 30 дней
- **История диалогов**: Все анализы сохраняются в Room БД, поддерживается переименование и удаление
- **Чат с AI-ассистентом**: После анализа можно продолжать диалог, задавать уточняющие вопросы
- **Реактивный UI**: Автоматическое обновление списка диалогов через Flow
- **Адаптивная тема**: Поддержка светлой/тёмной темы и динамических цветов (Android 12+)

### Целевая аудитория
- Соискатели, желающие улучшить свои резюме
- HR-специалисты для быстрой предварительной оценки
- Карьерные консультанты

---

## 2. Технологический стек

| Компонент | Версия | Назначение |
|-----------|--------|-------------|
| **Kotlin** | 2.0.21 | Основной язык |
| **Jetpack Compose** | BOM 2026.05.00 | Декларативный UI |
| **Clean Architecture** | - | Разделение на слои (data, domain, presentation) |
| **MVVM** | - | Паттерн для presentation слоя |
| **Room** | 2.8.4 | Локальная БД (три таблицы) |
| **PyTorch Mobile** | 2.1.0 | Инференс ML-модели |
| **Koin** | 4.0.1 | Внедрение зависимостей |
| **Coroutines** | 1.9.0 | Асинхронность |
| **Flow** | - | Реактивные потоки данных |
| **Compose Navigation** | 2.9.8 | Навигация между экранами |
| **kotlinx.serialization** | 1.8.1 | JSON сериализация |
| **kotlinx-datetime** | 0.6.1 | Работа с временными метками |
| **MockK** | 1.14.2 | Unit-тестирование |

### Дополнительно
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 36 (Android 15)
- **Java version**: 11

---

## 3. Архитектура проекта

### Схема слоёв

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION (UI)                            │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐           │
│  │ Conversations│ │ NewAnalysis  │ │    Chat      │           │
│  │   Screen     │ │    Screen    │ │    Screen    │           │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘           │
│  ┌──────▼───────┐ ┌──────▼───────┐ ┌──────▼───────┐           │
│  │ViewModel(State│ │ViewModel(State│ │ViewModel(State│           │
│  │Event+Effect) │ │Event+Effect) │ │Event+Effect) │           │
│  └──────────────┘ └──────────────┘ └──────────────┘           │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DOMAIN (UseCases)                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐  │
│  │CreateConv   │ │GetConvers   │ │AnalyzeAndAddToConv      │  │
│  └──────┬──────┘ └──────┬──────┘ └───────────┬─────────────┘  │
│         │               │                    │                 │
│         ▼               ▼                    ▼                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              Repository Interfaces (Domain)              │  │
│  │     ConversationRepository │ ResumeRepository           │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA (Implementation)                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐  │
│  │ Room (DAO)  │ │PyTorchModel │ │ BertTokenizer           │  │
│  │ + Entities  │ │ + IValueMap │ │ + AnalysisCache         │  │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SOURCES                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐  │
│  │  SQLite DB  │ │ .pt модель  │ │ Assets (vocab, json)    │  │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Структура пакетов

```
com.example.resume_net/
├── data/
│   ├── cache/           # AnalysisCache (кэширование результатов)
│   ├── db/              # Room: AppDatabase, Entities, DAOs
│   ├── dto/             # Data Transfer Objects
│   ├── mapper/          # IValueMapper (PyTorch → Kotlin)
│   ├── ml/              # PyTorchModelFacade
│   ├── repository/      # Реализации репозиториев
│   └── tokenizer/       # BertTokenizer
├── di/
│   ├── AppModule.kt
│   ├── DataModule.kt
│   └── DomainModule.kt
├── domain/
│   ├── model/           # Domain-модели + sealed class AnalysisError
│   ├── repository/      # Интерфейсы репозиториев
│   └── usecase/         # UseCase'ы
├── presentation/
│   ├── chat/            # Экран чата + компоненты
│   ├── conversations/   # Список диалогов + компоненты
│   ├── navigation/      # Screen, NavGraph
│   └── newanalysis/     # Экран нового анализа + компоненты
├── ui/theme/            # Цвета, типографика, тема
├── MainActivity.kt
└── ResumeApp.kt
```

### Взаимодействие между слоями

1. **UI** → отправляет `Event` в **ViewModel**
2. **ViewModel** → вызывает **UseCase** (или напрямую **Repository**)
3. **UseCase** → координирует вызовы **Repository**
4. **Repository** → работает с **Room**, **PyTorchModel**, **Cache**
5. **State** (через Flow) и **Effect** (одноразовые действия) возвращаются во **ViewModel** → **UI**

---

## 4. Модели данных (Domain)

### AnalysisResult
```kotlin
data class AnalysisResult(
    val score: Float,                    // Оценка 1.0–5.0
    val issues: List<AnalysisIssue>,     // Критические проблемы (severity = CRITICAL)
    val warnings: List<AnalysisIssue>,   // Предупреждения (severity = WARNING)
    val allTags: List<AnalysisIssue>     // Все 20 тегов с вероятностями
)
```

### ResumeTag (20 тегов)
```kotlin
enum class ResumeTag(val displayName: String) {
    NO_NUMBERS("Нет цифр и метрик"),
    TOO_MUCH_WATER("Много воды"),
    NO_SKILLS("Нет навыков"),
    BAD_STRUCTURE("Плохая структура"),
    NO_ACHIEVEMENTS("Нет достижений"),
    TOO_SHORT("Слишком короткое"),
    BAD_GRAMMAR("Ошибки и опечатки"),
    NO_ABOUT("Нет раздела «Обо мне»"),
    WEAK_LANGUAGE("Слабый язык"),
    TOO_VAGUE("Слишком расплывчато"),
    WEAK_ACHIEVEMENTS("Слабые достижения"),
    LOW_EXPERIENCE("Мало опыта"),
    NO_EDUCATION("Не указано образование"),
    GENERIC_TEMPLATE("Шаблонное резюме"),
    NO_CONTACTS("Нет контактов"),
    TOO_MANY_JOBS("Частая смена работы"),
    BAD_FORMATTING("Плохое форматирование"),
    WRONG_TONE("Неподходящий тон"),
    SALARY_FOCUS("Фокус на зарплате"),
    IRRELEVANT_INFO("Лишняя информация")
}
```

### ChatMessage (sealed class)
```kotlin
sealed class ChatMessage {
    abstract val id: Long
    abstract val conversationId: Long
    abstract val timestamp: Long
    abstract val isFavorite: Boolean

    data class UserMessage(
        override val id: Long,
        override val conversationId: Long,
        val text: String,
        override val timestamp: Long,
        override val isFavorite: Boolean = false
    ) : ChatMessage()

    data class AssistantMessage(
        override val id: Long,
        override val conversationId: Long,
        val analysisResult: AnalysisResult,
        override val timestamp: Long,
        override val isFavorite: Boolean = false
    ) : ChatMessage()
}
```

---

## 5. UseCase'ы (Domain слой)

### CreateConversationUseCase
**Назначение**: Создание нового диалога с анализом резюме

```kotlin
suspend operator fun invoke(params: Params): Result

data class Params(
    val resumeText: String,
    val useCache: Boolean = true
)

data class Result(
    val conversationId: Long,
    val analysisResult: AnalysisResult,
    val fromCache: Boolean
)
```

**Алгоритм**:
1. Анализ резюме через `resumeRepository.analyze()`
2. Создание диалога через `conversationRepository.createConversation()`

---

### GetConversationsUseCase
**Назначение**: Получение списка диалогов (одноразово или через Flow)

```kotlin
// Одноразово
suspend operator fun invoke(params: Params = Params()): List<Conversation>

// Реактивно (Flow)
fun observe(): Flow<List<Conversation>>

// Вспомогательные методы
fun getNextPageParams(currentPage: Int, limit: Int = 20): Params
fun hasNextPage(totalCount: Int, currentOffset: Int, limit: Int): Boolean
```

---

### DeleteConversationUseCase
**Назначение**: Удаление диалога по ID

```kotlin
suspend operator fun invoke(conversationId: Long): Boolean
```

- Возвращает `true` если диалог существовал и удалён
- `false` если диалог не найден

---

### AnalyzeResumeUseCase
**Назначение**: Анализ текста резюме без создания диалога

```kotlin
suspend operator fun invoke(resumeText: String): Result<AnalysisResult>
```

Валидация:
- `EmptyResume` — текст пустой
- `TooShort` — длина < 50 символов

---

### AddMessageUseCase
**Назначение**: Добавление нового сообщения в существующий диалог

```kotlin
suspend operator fun invoke(params: Params): Result

data class Params(
    val conversationId: Long,
    val text: String,
    val isResumeAnalysis: Boolean = true
)

data class Result(
    val userMessage: ChatMessage.UserMessage,
    val assistantMessage: ChatMessage.AssistantMessage?
)
```

- Если `isResumeAnalysis = true` и длина текста ≥ 50 символов, выполняется анализ
- Ответ ассистента сохраняется только после успешного анализа

---

### AnalyzeAndAddToConversationUseCase
**Назначение**: Композитный UseCase — анализ + добавление в диалог

```kotlin
suspend operator fun invoke(params: Params): Result

data class Params(
    val conversationId: Long,
    val resumeText: String,
    val addUserMessage: Boolean = true
)

data class Result(
    val analysisResult: AnalysisResult,
    val userMessage: ChatMessage.UserMessage?,
    val assistantMessage: ChatMessage.AssistantMessage
)
```

**Дополнительная логика**:
- Автоматическое обновление названия диалога, если это первое сообщение (≤2 сообщений в диалоге)

---

## 6. Репозитории (Domain интерфейсы)

### ConversationRepository

| Метод | Описание |
|-------|----------|
| `createConversation(resumeText, analysisResult): Long` | Создание диалога с анализом |
| `getConversations(limit, offset): List<Conversation>` | Список с пагинацией |
| `observeConversations(): Flow<List<Conversation>>` | Реактивный список диалогов |
| `getConversationById(id): Conversation?` | Получение по ID |
| `deleteConversation(id)` | Удаление |
| `renameConversation(id, newTitle)` | Переименование |
| `addUserMessage(conversationId, text): Result<UserMessage>` | Добавление сообщения пользователя |
| `addAssistantMessage(conversationId, analysisResult): Result<AssistantMessage>` | Добавление ответа ассистента |
| `getMessages(conversationId, limit, offset): List<ChatMessage>` | Сообщения с пагинацией |
| `observeMessages(conversationId): Flow<List<ChatMessage>>` | Реактивные сообщения |
| `getLastMessage(conversationId): ChatMessage?` | Последнее сообщение |

### ResumeRepository

| Метод | Описание |
|-------|----------|
| `analyze(resume): Result<AnalysisResult>` | Анализ без кэширования |
| `analyzeWithCache(resumeText): Result<AnalysisResult>` | Анализ с кэшированием (SHA-256, TTL 30 дней) |

---

## 7. База данных (Room)

### Схема БД

```
┌─────────────────────────────────────────────────────────────────┐
│                    analysis_history                              │
├─────────────────────────────────────────────────────────────────┤
│ id (PK auto)        Long                                        │
│ resume_text         String                                      │
│ resume_text_hash    String (SHA-256)                            │
│ score               Float                                       │
│ issues_json         String (JSON: CachedResultDto)              │
│ created_at          Long                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ (не связана с другими таблицами)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    conversations                                 │
├─────────────────────────────────────────────────────────────────┤
│ id (PK auto)        Long                                        │
│ title               String                                      │
│ created_at          Long                                        │
│ updated_at          Long                                        │
│ resume_text_hash    String? (SHA-256, optional)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1 : N (CASCADE)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    messages                                      │
├─────────────────────────────────────────────────────────────────┤
│ id (PK auto)        Long                                        │
│ conversation_id     Long (FK → conversations.id, CASCADE)       │
│ role                String ("USER" / "ASSISTANT")               │
│ content             String                                      │
│ score               Float? (только для ASSISTANT)               │
│ tags_json           String? (JSON: List<TagProbability>)        │
│ created_at          Long                                        │
│ is_favorite         Boolean (default false)                     │
└─────────────────────────────────────────────────────────────────┘
```

### Индексы

**analysis_history**:
- `idx_analysis_hash` на `resume_text_hash`

**conversations**:
- `idx_conversations_created_at` на `created_at`
- `idx_conversations_updated_at` на `updated_at`
- `idx_conversations_hash` на `resume_text_hash`

**messages**:
- `idx_messages_conversation_id` на `conversation_id`
- `idx_messages_created_at` на `created_at`
- `idx_messages_conversation_created` на `(conversation_id, created_at)`

---

## 8. ML и анализ резюме

### PyTorchModelFacade
**Путь**: `data/ml/PyTorchModelFacade.kt`

```kotlin
class PyTorchModelFacade(private val modelPath: String) {
    fun load()                          // Загрузка модели из файла
    fun predict(inputIds: LongArray, attentionMask: LongArray): Pair<Float, FloatArray>
    fun close()                         // Освобождение ресурсов (destroy)
}
```

**Особенности**:
- Модель ожидает вход: `(input_ids, attention_mask)` — оба тензора размера `[1, 300]`
- Выход: Tuple из двух тензоров — оценка `[1, 1]` и вероятности 20 тегов `[1, 20]`
- Максимальная длина последовательности: **300 токенов**

### BertTokenizer
**Путь**: `data/tokenizer/BertTokenizer.kt`

```kotlin
class BertTokenizer(private val context: Context) {
    fun load()                          // Загрузка vocab.txt или vocab.json
    fun tokenize(text: String): Pair<LongArray, LongArray>  // (input_ids, attention_mask)
}
```

**Алгоритм токенизации**:
1. `cleanText()` — очистка, lowercase, замена символов
2. Разбиение на слова по пробелам
3. WordPiece токенизация каждого слова (субтокены с `##`)
4. Добавление `[CLS]` в начало и `[SEP]` в конец
5. Truncation до maxLength (300)
6. Padding до maxLength (0 для `[PAD]`)
7. Attention mask: 1 для реальных токенов, 0 для padding

### IValueMapper
**Путь**: `data/mapper/IValueMapper.kt`

```kotlin
object IValueMapper {
    fun toScore(output: IValue): Float
    fun toProbs(output: IValue): FloatArray
    fun toResult(output: IValue): Pair<Float, FloatArray>
}
```

Извлекает из Tuple PyTorch оценку и вероятности.

### ModelDownloader
**Путь**: `data/repository/ModelDownloader.kt`

```kotlin
class ModelDownloader(private val context: Context) {
    suspend fun getModelPath(): String  // Копирует .pt из assets в cacheDir
}
```

### AnalysisCache
**Путь**: `data/cache/AnalysisCache.kt`

```kotlin
class AnalysisCache(private val dao: AnalysisDao) {
    suspend fun saveResult(textHash: String, resumeText: String, result: AnalysisResult)
    suspend fun getByHash(textHash: String): AnalysisResult?
    suspend fun getHistory(): List<Pair<String, AnalysisResult>>
    suspend fun clearHistory()
}
```

**TTL**: 30 дней (`CACHE_TTL_DAYS = 30L`)

**Хеширование**:
```kotlin
private fun hashText(text: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
```

### Логика анализа (ResumeRepositoryImpl)

**Пороги вероятностей** (из `model_metadata.json`):
- `threshold_high` (critical): **0.5** (50%)
- `threshold_low` (warning): **0.3** (30%)

**Алгоритм**:
1. Валидация (не пусто, длина ≥ 50 символов)
2. Проверка кэша (если `useCache = true`)
3. Загрузка модели (если не загружена)
4. Токенизация через `BertTokenizer`
5. Инференс через `PyTorchModelFacade`
6. Построение `AnalysisIssue` для каждого тега
7. Сохранение в кэш (если `useCache = true`)

**Асинхронная загрузка модели**:
- Флаги: `isModelLoaded`, `isLoadingModel`
- Таймаут ожидания: **5 секунд** (`MODEL_LOAD_TIMEOUT_MS`)
- Метод `waitForModelLoad()` с polling каждые 100 мс

### Список 20 тегов (ResumeTag)

См. раздел 4. Полный список из 20 тегов с русскими отображаемыми именами.

---

## 9. UI (Presentation слой)

### 9.1 Экран списка диалогов (ConversationsListScreen)

**Файлы**:
- `ConversationsListScreen.kt`
- `ConversationListViewModel.kt`
- `ConversationListState.kt`
- `ConversationListEvent.kt`
- `ConversationListEffect.kt`
- `components/ConversationListItem.kt`

**События (ConversationListEvent)**:
- `LoadConversations` — загрузка списка
- `UpdateSearchQuery(query)` — поиск по названию/превью
- `ClearSearch` — очистка поиска
- `DeleteConversation(conversationId)` — удаление
- `RenameConversation(conversationId, newTitle)` — переименование

**Эффекты (ConversationListEffect)**:
- `ShowError(message)` — Snackbar с ошибкой
- `ShowSuccess(message)` — Snackbar с успехом
- `NavigateToChat(conversationId)` — переход к чату

**Ключевые компоненты**:
- **ConversationListItem**: карточка диалога с меню (три точки), оценкой (звезда + число), датой ("Сегодня"/"Вчера"/"dd MMM")
- **EmptyStateContent**: заглушка для пустого списка или поиска

**Реактивность**:
```kotlin
private fun observeConversations() {
    viewModelScope.launch {
        getConversationsUseCase.observe().collect { conversations ->
            allConversations = conversations
            updateFilteredList()
            _state.update { it.copy(conversations = conversations, isLoading = false) }
        }
    }
}
```

### 9.2 Экран нового анализа (NewAnalysisScreen)

**Файлы**:
- `NewAnalysisScreen.kt`
- `NewAnalysisViewModel.kt`
- `NewAnalysisState.kt`
- `NewAnalysisEvent.kt`
- `NewAnalysisEffect.kt`
- `components/ExpandableSection.kt`
- `components/ExampleCard.kt`

**Состояние (NewAnalysisState)**:
```kotlin
data class NewAnalysisState(
    val resumeText: String = "",
    val conversationTitle: String = "",
    val isLoading: Boolean = false,
    val charCount: Int = 0,
    val isAnalyzeEnabled: Boolean = false,
    val error: String? = null,
    val showModelLoadingDialog: Boolean = false,
    val showDuplicateDialog: Boolean = false,
    val duplicateConversationId: Long? = null
)
```

**События (NewAnalysisEvent)**:
- `UpdateResumeText(text)` — обновление текста
- `UpdateConversationTitle(title)` — обновление названия чата
- `Analyze` — запуск анализа
- `Cancel` — закрытие экрана
- `DismissDuplicateDialog` / `OpenExistingConversation` — обработка дубликатов

**Эффекты (NewAnalysisEffect)**:
- `NavigateToChat(conversationId)`
- `NavigateBack`
- `ShowError(message)`
- `ShowTooltip(message)` — анимированная подсказка при <50 символах

**Валидация**:
- Минимум **50 символов**
- Анимированная карточка ошибки (expandVertically + fadeIn)

**Примеры резюме** (ResumeExamples.kt):
3 примера с заголовком, описанием и полным текстом:
1. Хорошее резюме (Frontend Developer) — оценка ~4.5
2. Среднее резюме (Product Manager) — оценка ~3.0
3. Плохое резюме (iOS Developer) — оценка ~2.0

**Компоненты**:
- **ExpandableSection**: аккордеон с иконкой и анимацией
- **ExampleCard**: карточка примера (фон AccentDark, текст AccentLight)

### 9.3 Экран чата (ChatScreen)

**Файлы**:
- `ChatScreen.kt`
- `ChatViewModel.kt`
- `ChatState.kt`
- `ChatEvent.kt`
- `ChatEffect.kt`
- `components/` — 9 компонентов

**Состояние (ChatState)**:
```kotlin
data class ChatState(
    val conversationId: Long = 0L,
    val conversationTitle: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isTyping: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val inputText: String = "",
    val error: String? = null,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExportDialog: Boolean = false
)
```

**События (ChatEvent)**:
- `LoadConversation(id)`
- `LoadMoreMessages` — пагинация при скролле вверх
- `UpdateInputText(text)`
- `SendMessage` — обычное сообщение (без анализа)
- `SendResumeForAnalysis` — анализ резюме
- `RenameConversation(newTitle)`
- `DeleteConversation`
- `ShowRenameDialog` / `DismissRenameDialog`
- `ShowDeleteConfirmation` / `DismissDeleteDialog`

**Эффекты (ChatEffect)**:
- `ShowError(message)`
- `ShowSuccess(message)`
- `NavigateBack`
- `ScrollToBottom`

**Пагинация сообщений**:
- Размер страницы: **20 сообщений**
- Метод `loadMoreMessages()` вызывается при скролле вверх
- Сообщения добавляются в **начало** списка

**Автопрокрутка**:
```kotlin
LaunchedEffect(state.messages.size) {
    if (state.messages.isNotEmpty()) {
        listState.animateScrollToItem(state.messages.size - 1)
    }
}
```

**Компоненты**:

| Компонент | Описание |
|-----------|----------|
| `MessageUser` | Сообщение пользователя. Градиентный фон (primary → primary copy). Кнопка "Читать далее" при длине >300 символов. |
| `MessageAssistant` | Сообщение ассистента. Аватар (иконка Psychology), градиентный фон (AssistantGradientStart → End). Содержит ScoreChip + список TagItem. |
| `ScoreChip` | Оценка с градиентом: зелёный (≥4.0), оранжевый (3.0–4.0), красный (<3.0). |
| `TagItem` | Тег проблемы: название + прогресс-бар (4dp). Разворачивается по клику, показывает рекомендацию. Цвета: >0.6 → красный, >0.3 → жёлтый, иначе зелёный. |
| `TagDetailDialog` / `TagDetailBottomSheet` | Диалог с рекомендацией, кнопки "Копировать" и "Как исправить" (поиск в Google). |
| `MessageInputField` | Поле ввода с кнопкой отправки. Отправка: если текст ≥50 символов → анализ, иначе обычное сообщение. |
| `ChatTopAppBar` | Верхняя панель: название, стрелка назад, меню (три точки) с действиями. |
| `RenameDialog` / `DeleteConfirmationDialog` | Диалоги для переименования и удаления. |
| `TypingIndicator` | Анимация трёх точек (infiniteRepeatable + scale) при `isTyping = true`. |

### 9.4 Компоненты UI (общие)

| Компонент | Расположение | Описание |
|-----------|--------------|----------|
| `ConversationListItem` | `conversations/components/` | Карточка диалога с меню, оценкой, датой |
| `TagItem` | `chat/components/` | Тег с прогресс-баром и раскрывающейся рекомендацией |
| `ScoreChip` | `chat/components/` | Градиентная оценка от 1 до 5 |
| `ExpandableSection` | `newanalysis/components/` | Аккордеон для примеров |
| `ExampleCard` | `newanalysis/components/` | Карточка примера резюме |
| `MessageUser` / `MessageAssistant` | `chat/components/` | Сообщения в чате |
| `MessageInputField` | `chat/components/` | Поле ввода с кнопкой отправки |
| `ChatTopAppBar` | `chat/components/` | Верхняя панель чата |

---

### 10. Типографика (Type.kt)

**Кастомный шрифт**: `font_app_lifehack.ttf` (поддерживает русский язык)

## 11. Навигация

### Маршруты (Screen.kt)

```kotlin
sealed class Screen(val route: String) {
    data object Conversations : Screen("conversations")
    data object NewAnalysis : Screen("new_analysis")
    data object Chat : Screen("chat")
}
```

### NavGraph (NavGraph.kt)

```kotlin
@Composable
fun NavGraph(startDestination: String = Screen.Conversations.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        // Список диалогов
        composable(Screen.Conversations.route) {
            ConversationsListScreen(
                onNavigateToChat = { conversationId ->
                    navController.navigate("${Screen.Chat.route}/$conversationId")
                },
                onNavigateToNewAnalysis = {
                    navController.navigate(Screen.NewAnalysis.route)
                }
            )
        }

        // Новый анализ
        composable(Screen.NewAnalysis.route) {
            NewAnalysisScreen(
                onNavigateBack = { navController.popBackStack() },
                onAnalysisComplete = { conversationId ->
                    navController.popBackStack()
                    navController.navigate("${Screen.Chat.route}/$conversationId")
                }
            )
        }

        // Чат
        composable(
            route = "${Screen.Chat.route}/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
            ChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

**Передача параметров**:
- `conversationId` передаётся через маршрут: `"chat/{conversationId}"`
- В `ChatViewModel` параметр извлекается через `SavedStateHandle`

---

## 12. DI (Внедрение зависимостей)

### DataModule.kt (синглтоны)

```kotlin
val dataModule = module {
    single { BertTokenizer(androidContext()) }
    single { ModelDownloader(androidContext()) }
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().analysisDao() }
    single { AnalysisCache(get()) }
    single { get<AppDatabase>().conversationDao() }
    single { get<AppDatabase>().messageDao() }

    single<ResumeRepository> {
        ResumeRepositoryImpl(
            context = androidContext(),
            tokenizer = get(),
            modelDownloader = get(),
            analysisCache = get()
        )
    }

    single<ConversationRepository> {
        ConversationRepositoryImpl(
            conversationDao = get(),
            messageDao = get()
        )
    }
}
```

### DomainModule.kt (фабрики UseCase)

```kotlin
val domainModule = module {
    factory { AnalyzeResumeUseCase(get()) }
    factory { CreateConversationUseCase(get(), get()) }
    factory { GetConversationsUseCase(get()) }
    factory { DeleteConversationUseCase(get()) }
    factory { AddMessageUseCase(get(), get()) }
    factory { AnalyzeAndAddToConversationUseCase(get(), get()) }
}
```

### AppModule.kt (ViewModel)

```kotlin
val appModule = module {
    viewModel { ConversationListViewModel(get(), get(), get()) }
    viewModel { NewAnalysisViewModel(get()) }
    viewModel { params ->
        ChatViewModel(
            savedStateHandle = params.get(),
            conversationRepository = get(),
            resumeRepository = get(),
            addMessageUseCase = get(),
            analyzeAndAddToConversationUseCase = get()
        )
    }
}
```

### Запуск Koin (ResumeApp.kt)

```kotlin
class ResumeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ResumeApp)
            modules(domainModule, dataModule, appModule)
        }
    }
}
```

---

## 13. Управление памятью и производительность

### Загрузка модели

**Асинхронная загрузка**:
- Флаги: `@Volatile var isModelLoaded`, `@Volatile var isLoadingModel`
- Таймаут ожидания: **5000 мс**
- Метод `waitForModelLoad()` с polling каждые 100 мс

```kotlin
suspend fun waitForModelLoad(): Boolean = withContext(Dispatchers.IO) {
    if (isModelLoaded) return@withContext true
    val result = withTimeoutOrNull(MODEL_LOAD_TIMEOUT_MS) {
        while (!isModelLoaded && isLoadingModel) delay(100)
        isModelLoaded
    }
    return@withContext result == true
}
```

### Освобождение модели (MainActivity)

```kotlin
override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    when (level) {
        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
            // Приложение ушло в фон
            lifecycleScope.launch(Dispatchers.IO) { repository.releaseModel() }
        }
        ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
            // Критическая нехватка памяти
            repository.releaseModel()
        }
    }
}

override fun onDestroy() {
    super.onDestroy()
    if (isFinishing) repository.releaseModel()
}
```

### Кэширование анализов

- SHA-256 хеширование текста
- TTL: **30 дней** (`CACHE_TTL_DAYS = 30L`)
- При получении из кэша проверяется возраст записи
- Устаревшие записи удаляются при запросе

### Пагинация

- Сообщения: **20 на страницу**
- Список диалогов: реактивный через Flow (Room автоматически)

### Потоки

- Все операции с БД — `Dispatchers.IO`
- Инференс модели — `Dispatchers.Default`
- UI — `Dispatchers.Main` (Compose)

---

## 14. Обработка ошибок

### Типы ошибок (AnalysisError)

```kotlin
sealed class AnalysisError : Exception() {
    data object EmptyResume : AnalysisError()
    data object TooShort : AnalysisError()
    data object ModelNotAvailable : AnalysisError()
    data class TokenizerError(override val message: String) : AnalysisError()
    data class InferenceError(override val message: String) : AnalysisError()
}
```

### Отображение ошибок

| Экран | Способ |
|-------|--------|
| **NewAnalysisScreen** | Snackbar через `ShowError`, анимированная карточка при <50 символов |
| **ConversationsListScreen** | Snackbar через `ShowError` / `ShowSuccess` |
| **ChatScreen** | Snackbar через `ShowError` / `ShowSuccess` |

### Валидация текста

- Минимальная длина: **50 символов** (`MIN_TEXT_LENGTH`)
- Проверка на пустоту
- Тримминг пробелов в начале и конце

### Обработка ошибок модели

```kotlin
val errorMessage = when (e) {
    is AnalysisError.EmptyResume -> "Текст резюме пуст"
    is AnalysisError.TooShort -> "Текст слишком короткий (минимум 50 символов)"
    is AnalysisError.ModelNotAvailable -> "Модель не загружена. Проверьте подключение"
    else -> e.message ?: "Ошибка анализа"
}
_effect.emit(ChatEffect.ShowError(errorMessage))
```

---

## 15. Сборка и запуск

### Требования

| Параметр | Значение |
|----------|----------|
| **minSdk** | 24 (Android 7.0 Nougat) |
| **targetSdk** | 36 (Android 15) |
| **Java** | 11 |
| **Kotlin** | 2.0.21 |
| **Android Studio** | Koala (2024.1.1) или новее |


### Структура assets

```
app/src/main/assets/
└── ml/
    ├── resume_model_android_lite.pt   # PyTorch модель (≈50-100 MB)
    ├── vocab.txt                      # Словарь BERT (построчно: токен)
    ├── model_metadata.json            # Метаданные
    └── recommendations.json           # Рекомендации по тегам
```


#  Заключение


Проект **Resume Analyzer** представляет собой профессиональную реализацию Android-приложения с использованием современных технологий и лучших практик.

### Сильные стороны

1. **Архитектура**
   - Чёткое разделение слоёв (Clean Architecture)
   - MVVM с State + Event + Effect
   - Koin для DI (простота и производительность)

2. **ML-интеграция**
   - Асинхронная загрузка модели с таймаутами
   - Кэширование результатов с SHA-256 и TTL
   - Полная токенизация BERT на устройстве

3. **UI/UX**
   - Адаптивная светлая/тёмная тема
   - Кастомная типографика и градиенты
   - Анимированные переходы и индикаторы
   - Плавная пагинация и автопрокрутка

4. **База данных**
   - Room с миграциями
   - Внешние ключи с CASCADE
   - Реактивные Flow для обновления UI

5. **Обработка ошибок**
   - Sealed class для типов ошибок
   - Snackbar для уведомлений
   - Graceful degradation при отсутствии модели

6. **Тестирование**
   - Unit-тесты для всех UseCase'ов
   - MockK для мокирования зависимостей
   - Test Coroutines Dispatchers

### Что можно улучшить

1. **Производительность**
   - LRU-кэш для результатов анализа (в дополнение к БД)
   - Предзагрузка модели после запуска приложения (а не при первом анализе)

2. **Функциональность**
   - Реализовать экспорт диалогов (кнопка уже есть)
   - Добавить загрузку файлов (PDF/DOCX)
   - Добавить поиск по истории сообщений (метод в MessageDao уже есть)

3. **UI**
   - Pull-to-refresh для списка диалогов (Accompanist уже подключён, не используется)
   - Добавить анимацию появления сообщений

4. **Документация**
   - KDoc для всех публичных методов
   - Пользовательская документация (встроенный туториал)
