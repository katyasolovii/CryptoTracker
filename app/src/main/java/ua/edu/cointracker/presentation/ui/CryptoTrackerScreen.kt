package ua.edu.cointracker.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import ua.edu.cointracker.R
import ua.edu.cointracker.presentation.CryptoViewModel
import ua.edu.cointracker.domain.model.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTrackerScreen(viewModel: CryptoViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    // Відступи під системні панелі та виріз екрана (камеру) — вміст не перекривається ними,
    // а фон усе одно займає весь екран
    val insets = WindowInsets.systemBars.union(WindowInsets.displayCutout)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = insets,
        topBar = {
            TopAppBar(
                windowInsets = insets,
                title = {
                    Text(
                        text = "Біржа криптовалют",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusLabel(state.syncStatus, state.assets.isEmpty()),
                        style = MaterialTheme.typography.titleSmall
                    )
                    state.syncMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    state.lastUpdated?.let {
                        Text(
                            text = "Оновлено: ${formatTimestamp(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = viewModel::requestUpdate,
                    modifier = Modifier.widthIn(min = 120.dp),
                    enabled = state.syncStatus !in setOf(
                        SyncStatus.RUNNING,
                        SyncStatus.WAITING_FOR_NETWORK,
                        SyncStatus.RETRYING,
                        SyncStatus.QUEUED
                    )
                ) {
                    // Кнопка завжди в один рядок і не стискається по ширині
                    Text(
                        text = if (state.syncStatus == SyncStatus.RUNNING) "Оновлення…" else "Оновити",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (state.syncStatus == SyncStatus.RUNNING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }

            if (state.assets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (state.error == null) {
                            "Натисніть «Оновити», щоб завантажити дані."
                        } else {
                            "Поки що немає збережених криптовалют."
                        }
                    )
                }
            } else {
                // Завжди одна колонка — і в портретному, і в горизонтальному режимі
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.assets, key = { it.id }) { asset ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (asset.imageUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(asset.symbol.take(1))
                                    }
                                } else {
                                    AsyncImage(
                                        model = asset.imageUrl,
                                        contentDescription = "${asset.name} logo",
                                        modifier = Modifier.size(44.dp),
                                        contentScale = ContentScale.Fit,
                                        placeholder = painterResource(R.drawable.crypto_placeholder),
                                        error = painterResource(R.drawable.crypto_placeholder)
                                    )
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = asset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = asset.symbol,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = PriceFormatter.format(asset.priceUsd),
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun statusLabel(status: SyncStatus, isEmpty: Boolean): String = when (status) {
    SyncStatus.IDLE -> if (isEmpty) "Дані ще не завантажені" else "Топ криптовалют за капіталізацією"
    SyncStatus.RUNNING -> "Оновлення даних"
    SyncStatus.WAITING_FOR_NETWORK -> "Очікування мережі"
    SyncStatus.RETRYING -> "Повторна спроба"
    SyncStatus.QUEUED -> "Оновлення в черзі"
    SyncStatus.FAILED -> "Помилка оновлення"
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
