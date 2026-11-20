package ua.edu.cointracker.domain.model

// у жодному з цих 6 файлів немає залежностей від Android, Room чи Retrofit
data class CryptoCurrency(
    val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String?,  // зроблені nullable (?) навмисно. Якщо ціна відсутня (null), форматер виводить "Немає даних", а не $0.00, щоб не дезінформувати користувача
    val priceUsd: Double?,  // те саме
    val marketRank: Int,
    val updatedAt: Long
)
