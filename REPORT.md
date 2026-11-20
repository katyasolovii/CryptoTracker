# Звіт про проєкт «Біржа криптовалют»

Застосунок на Kotlin і Jetpack Compose показує топ-30 криптовалют у USD,
зберігає їх у Room та оновлює вручну й у фоні через WorkManager. Список
доступний офлайн після успішного завантаження.

## Реалізація

- `domain`: модель, контракти репозиторію та планувальника, Use Case і стан
  синхронізації. Шар не залежить від Android UI, Room, Retrofit чи WorkManager.
- `data`: CoinGecko Demo API/DTO, Room, мапери, репозиторій, Worker та scheduler.
  Пакет `data` не імпортує `presentation`.
- `presentation`: ViewModel, Compose UI та стани. Він використовує контракти
  `domain` і не імпортує типи `data`.
- Hilt-модулі в `data` надають залежності мережі, Room, WorkManager і
  реалізації контрактів `domain`. `CryptoApplication` запускає Hilt та
  налаштовує WorkManager з `HiltWorkerFactory`.

Безкоштовний тариф CoinDesk скасовано 21.05.2026, а заявка на ключ через
Personal не працює, тому для проєкту обрано CoinGecko Demo API. Запит
`GET https://api.coingecko.com/api/v3/coins/markets` використовує параметри
`vs_currency=usd`, `order=market_cap_desc`, `per_page=30`, `page=1` і
`sparkline=false`; ключ з `local.properties` передається в заголовку
`x-cg-demo-api-key`. Room оновлює перевірений рейтинг в одній транзакції; при
невдалій відповіді попередній список зберігається. Worker планується на годину
з умовою `NetworkType.CONNECTED`, а кнопка запускає унікальне одноразове
оновлення.

## Команда

[Учасник 1 — Катерина Соловій] відповідає за API, Room, мапери, репозиторій та
Hilt-модулі для шару `data`. [Учасник 2 — Юлія Ужвенко] відповідає за
`domain`, фонові задачі, ViewModel і Compose. Розподіл роботи орієнтовно
порівну; обидві учасниці мають пояснити весь шлях даних.

## Запуск

В Android Studio відкрийте проєкт з Android SDK 36 та JDK 17. У корені
налаштуйте `local.properties` зі шляхом до SDK і власним ключем
`COINGECKO_API_KEY=`. У шаблоні ключ залишений порожнім. Не додавайте цей файл
до Git. Синхронізуйте Gradle і запустіть застосунок на Android 8.0 (API 26) чи
новішому. Додаткові кроки й демонстраційне відео описані в [README](./README.md).
