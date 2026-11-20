# Матеріали до захисту — Юлія Ужвенко

## Моя частина

Моя частина визначає, що застосунок робить, коли запускається фонове
оновлення, і як показати користувачу локальні дані та стан синхронізації.
ViewModel працює з контрактами `domain`, а не напряму з Retrofit, Room чи
WorkManager.

### Domain — прості правила застосунку

- [`CryptoCurrency`](./app/src/main/java/ua/edu/cointracker/domain/model/CryptoCurrency.kt)
  — модель криптовалюти для застосунку: ID, назва, символ, URL логотипа, ціна,
  позиція в рейтингу та час оновлення.
- [`CryptoRepository`](./app/src/main/java/ua/edu/cointracker/domain/repository/CryptoRepository.kt)
  — контракт: `observeCryptos()` дає збережений список як `Flow`, а
  `syncCryptos()` запускає синхронізацію. Інтерфейс не знає, що реалізація
  використовує Retrofit або Room.
- [`SyncState` та `SyncStatus`](./app/src/main/java/ua/edu/cointracker/domain/model/SyncState.kt)
  — прості значення стану: очікування мережі, виконання, повтор, помилка чи
  спокійний стан. `SyncState` також тримає повідомлення й час помилки/успіху.
- [`CryptoSyncScheduler`](./app/src/main/java/ua/edu/cointracker/domain/repository/CryptoSyncScheduler.kt)
  — контракт для спостереження за станом фонової роботи та постановки ручного
  оновлення. Деталі WorkManager лишаються у `data`.
- [`GetCryptosUseCase`](./app/src/main/java/ua/edu/cointracker/domain/usecase/GetCryptosUseCase.kt)
  просто повертає потік збережених криптовалют.
- [`SyncCryptosUseCase`](./app/src/main/java/ua/edu/cointracker/domain/usecase/SyncCryptosUseCase.kt)
  запускає `syncCryptos()` у репозиторії. Worker викликає цей сценарій, а не
  API напряму.

### Фонова синхронізація

- [`CryptoSyncWorker`](./app/src/main/java/ua/edu/cointracker/data/worker/CryptoSyncWorker.kt)
  — `CoroutineWorker` з `@HiltWorker`. Hilt передає Use Case через
  конструктор, а `doWork()` чекає його завершення, після чого
  повертає успіх; `CancellationException` не приховується. `handleHttpError()`
  розділяє помилки доступу, обмеження API та тимчасові помилки. `retry()`
  передає WorkManager результат повторної спроби; `failure()` записує
  зрозуміле повідомлення і час помилки. Для 401/403 повтору немає, а мережеві,
  408, 429 і серверні помилки можна повторити.
- [`CryptoSyncScheduler`](./app/src/main/java/ua/edu/cointracker/data/worker/CryptoSyncScheduler.kt)
  — реалізація доменного контракту:
  - `schedulePeriodicSync()` створює годинне періодичне завдання з мережею,
    backoff, сталим ім'ям і політикою `KEEP`.
  - `enqueueManualSync()` створює одноразове оновлення з тими самими мережевими
    умовами та унікальним ім'ям.
  - `observeSyncState()` об'єднує стани WorkManager і мережі, щоб UI бачив
    виконання, чергу, очікування мережі, повтор або актуальну помилку.
  - `observeNetworkConnection()` повідомляє про зміну мережі та прибирає
    callback після завершення спостереження. `hasNetworkConnection()` читає
    поточний стан підключення.
  - `networkConstraints` вимагає `NetworkType.CONNECTED`.

**WorkManager** зберігає відкладену роботу незалежно від відкритого екрана.
Година — інтервал, а не обіцянка виконання в точну хвилину: Android враховує
мережу, заряд і системні обмеження. Унікальні імена разом із `KEEP` не дають
створювати дублікати.

### Екран і ViewModel

- [`CryptoUiState`](./app/src/main/java/ua/edu/cointracker/presentation/CryptoUiState.kt)
  описує все, що показує екран: криптовалюти, статус, повідомлення, помилку й
  час останнього оновлення.
- [`CryptoViewModel`](./app/src/main/java/ua/edu/cointracker/presentation/CryptoViewModel.kt)
  позначений `@HiltViewModel` і об'єднує `Flow` з Room та стан синхронізації у
  `uiState` типу `StateFlow`.
  `stateIn()` тримає останнє значення для Compose. `init` ставить перше
  оновлення, тільки якщо база порожня. `requestUpdate()` передає дію
  планувальнику. При повороті екрану Android зберігає ViewModel, тому новий
  запит не запускається лише через зміну орієнтації.
- [`MainActivity`](./app/src/main/java/ua/edu/cointracker/presentation/MainActivity.kt)
  позначена `@AndroidEntryPoint`. Hilt створює ViewModel, а Activity у
  `onCreate()` встановлює Compose UI та вибирає палітру за темою системи.
- [`CryptoTrackerScreen`](./app/src/main/java/ua/edu/cointracker/presentation/ui/CryptoTrackerScreen.kt)
  збирає `StateFlow` через `collectAsStateWithLifecycle()`, показує стан,
  помилку, час та кнопку оновлення. `LazyColumn` має стабільний ключ `id`;
  `rememberLazyListState()` зберігає позицію списку. `AsyncImage` з Coil 3
  показує логотип або заглушку. `statusLabel()` добирає текст для стану,
  `formatTimestamp()` показує час у локальному форматі.
- [`PriceFormatter`](./app/src/main/java/ua/edu/cointracker/presentation/ui/PriceFormatter.kt)
  показує ціну: для відсутнього значення повертає «Немає даних», для малих
  значень не підставляє однакове округлення до `0.00`.

**StateFlow** зберігає останній стан і віддає його новому підписнику. Саме
ViewModel переживає поворот, а Compose підписується на стан і перемальовує
екран без ручного оновлення Views. **Coil 3** завантажує зображення; placeholder
допомагає уникнути порожнього місця, якщо логотип ще вантажиться чи недоступний.

## Що зробила напарниця і як наші частини з'єднуються

Катерина реалізувала CoinGecko Demo API, передачу ключа, DTO, Room, мапери та
`CryptoRepositoryImpl`. Мої Use Case викликають її реалізацію через
`CryptoRepository`. У Worker той самий сценарій синхронізації доходить до
репозиторію; нові рядки Room з'являються у `Flow`, а мій ViewModel передає
їх Compose як стан. Hilt-модулі розміщені в `data`; `CryptoApplication`
налаштовує `HiltWorkerFactory`, а Worker отримує Use Case через конструктор.

**Retrofit** виконує HTTP-запит до CoinGecko; **Room** зберігає локальну копію;
**Compose** показує UI зі стану. Ці інструменти відповідають за різні частини
і з'єднані через прості моделі та контракти. Реалізації контрактів налаштовані
в [`RepositoryModule`](./app/src/main/java/ua/edu/cointracker/data/RepositoryModule.kt),
а Android-точка входу — у
[`CryptoApplication`](./app/src/main/java/ua/edu/cointracker/CryptoApplication.kt).

## Запитання викладача

1. **Що відбувається після натискання «Оновити»?**  
   ViewModel звертається до контракту планувальника. WorkManager ставить
   одноразовий Worker, Worker викликає Use Case, а той — репозиторій.
2. **Чому Worker — CoroutineWorker?**  
   Мережевий запит і запис у базу можна чекати послідовно. Worker повертає
   успіх лише після завершення обох операцій.
3. **Чи зупиниться фонова задача, якщо закрити екран?**  
   Ні. WorkManager зберігає задачу, вона не залежить від Activity чи ViewModel.
4. **Як екран дізнається, що дані змінилися?**  
   Room видає нові значення через `Flow`; ViewModel перетворює їх на
   `StateFlow`, на який підписаний Compose.
5. **Що відбувається під час повороту?**  
   ViewModel залишається в Android ViewModelStore, а Compose знову збирає її
   стан. Список відображається з останнього стану, позиція прокрутки збережена.
6. **Як обробляються неправильний ключ і тимчасова мережева помилка?**  
   Для 401/403 показується помилка ключа чи доступу без безкінечних повторів.
   Мережеві/серверні помилки повертають `Result.retry()` з backoff.
