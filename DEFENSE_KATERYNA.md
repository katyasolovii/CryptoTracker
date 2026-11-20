# Матеріали до захисту — Катерина Соловій

## Моя частина

Моя частина — завантажити дані CoinGecko, перевірити їх, зберегти в Room і
передати залежності застосунку. ViewModel не отримує відповіді мережі напряму:
вона бачить дані з бази через репозиторій.

### Мережа

- [`NetworkModule`](./app/src/main/java/ua/edu/cointracker/data/NetworkModule.kt)
  — Hilt-модуль, який надає OkHttp, Retrofit та API-інтерфейс. Interceptor
  додає заголовок `x-cg-demo-api-key`, якщо ключ заданий. Адреса API —
  CoinGecko Demo API; ключ читається Gradle з локального `local.properties`.
- [`CoinGeckoApi`](./app/src/main/java/ua/edu/cointracker/data/remote/CoinGeckoApi.kt)
  описує запит `GET /coins/markets`. Метод `getTopAssets()` передає параметри
  `vs_currency=usd`, `order=market_cap_desc`, `per_page=30`, `page=1` та
  `sparkline=false`. `suspend` означає, що запит можна чекати з корутини, не
  блокуючи головний потік.
- [`CoinGeckoMarketDto`](./app/src/main/java/ua/edu/cointracker/data/remote/CoinGeckoMarketDto.kt)
  відображає елементи JSON-масиву відповіді, зокрема `id`, `name`, `symbol`,
  `image` і `current_price`. Це мережева модель, UI її не використовує.

**Retrofit** перетворює описаний API-метод на HTTP-запит і відповідь Kotlin
об'єкта. Заголовок `x-cg-demo-api-key` передає Demo-ключ CoinGecko.

CoinDesk замінено, оскільки безкоштовний тариф припинено 21.05.2026, а
заявка на ключ через план Personal не працює.

### Локальна база та перетворення

- [`CryptoEntity`](./app/src/main/java/ua/edu/cointracker/data/local/CryptoEntity.kt)
  — один рядок Room. У ньому є стабільний ID, назва, символ, посилання на
  логотип, ціна, позиція в рейтингу та час успішного збереження.
- [`CryptoDao`](./app/src/main/java/ua/edu/cointracker/data/local/CryptoDao.kt)
  описує операції з таблицею:
  - `observeAll()` повертає відсортований список як `Flow`; Room сповіщає
    підписників після зміни рядків.
  - `insertAll()` додає/оновлює отримані записи.
  - `deleteAll()` прибирає попередній рейтинг.
  - `replaceAll()` виконує видалення й додавання в одній `@Transaction`.
    Якщо операція не завершиться, Room відкочує транзакцію; користувач не
    побачить порожню чи наполовину оновлену таблицю.
- [`CryptoDatabase`](./app/src/main/java/ua/edu/cointracker/data/local/CryptoDatabase.kt)
  описує базу й дає DAO через `cryptoDao()`. [`DatabaseModule`](./app/src/main/java/ua/edu/cointracker/data/DatabaseModule.kt)
  надає один екземпляр бази для застосунку та її DAO.
- [`CryptoMappers`](./app/src/main/java/ua/edu/cointracker/data/mapper/CryptoMappers.kt)
  містить дві прості функції:
  - `CoinGeckoMarketDto.toEntity()` перевіряє наявність ID, назви та символу,
    ставить позицію у відсортованому списку та створює Room-рядок.
  - `CryptoEntity.toDomain()` перетворює запис бази в модель, потрібну решті
    застосунку.

**Room** — локальна база SQLite з Kotlin-анотаціями. Вона потрібна для
офлайн-перегляду й для того, щоб екран завжди читав єдиний, вже збережений
список.

### Репозиторій і зв'язування

- [`CryptoRepositoryImpl`](./app/src/main/java/ua/edu/cointracker/data/repository/CryptoRepositoryImpl.kt)
  реалізує контракт із `domain`; Hilt отримує його конструкторні залежності:
  - `observeCryptos()` бере записи DAO та перетворює кожен у модель `domain`.
  - `syncCryptos()` чекає CoinGecko, перевіряє відповідь, кількість рядків,
    обов'язкові поля та повторні ID. Лише після повної перевірки викликає
    `replaceAll()`. Якщо мережа чи відповідь помилкова, старі дані залишаються.
- [`RepositoryModule`](./app/src/main/java/ua/edu/cointracker/data/RepositoryModule.kt)
  через `@Binds` пов'язує контракти `CryptoRepository` і планувальника з
  реалізаціями з `data`.
- [`CryptoApplication`](./app/src/main/java/ua/edu/cointracker/CryptoApplication.kt)
  позначений `@HiltAndroidApp`. Він налаштовує WorkManager через
  `HiltWorkerFactory` і один раз реєструє унікальну періодичну роботу.

## Як працює частина напарниці і як з'єднуються частини

Юлія відповідає за моделі/контракти `domain`, CoroutineWorker і планувальник,
ViewModel та Compose. Worker запускає `SyncCryptosUseCase`; той викликає
контракт репозиторію, а реалізація з моєї частини робить запит і транзакційно
оновлює Room. Потім DAO випускає новий список через `Flow`, ViewModel формує
`StateFlow`, а Compose перемальовує екран. Такий самий контракт репозиторію
дозволяє UI не знати про DTO, SQL або HTTP.

**WorkManager** зберігає фонові задачі навіть після закриття екрана чи
перезапуску застосунку. **Compose** описує інтерфейс Kotlin-функціями; він
перемальовує потрібні частини при зміні стану. **Плейсхолдер** — тимчасова
заглушка для логотипа, поки зображення вантажиться або якщо воно недоступне.

## Запитання викладача

1. **Навіщо тут DTO окремо від Room Entity?**  
   DTO відображає формат відповіді CoinGecko, Entity — колонки бази. Якщо
   провайдер змінить JSON, не доведеться міняти схему UI чи бази напряму.
2. **Чому UI бере ціни з Room, а не з відповіді Retrofit?**  
   Room — єдине джерело списку для екрана. Тому дані залишаються доступними
   офлайн і переживають перезапуск застосунку.
3. **Навіщо оновлення в одній транзакції?**  
   Видалення старого списку та вставлення нового відбуваються як одна
   операція. Не буде проміжного порожнього списку або часткового результату.
4. **Що буде, якщо API поверне помилку?**  
   Репозиторій не викликає `replaceAll()`, тому старий список і час його
   оновлення лишаються в базі.
5. **Для чого потрібен Flow від DAO?**  
   Flow повідомляє підписника про зміни Room. ViewModel отримує нові дані без
   ручного повторного запиту після запису.
6. **Де лежить API-ключ?**  
   Локально в `local.properties`, який не додається до Git. Gradle передає
   його через `BuildConfig`, а interceptor додає `x-cg-demo-api-key` до запиту.
