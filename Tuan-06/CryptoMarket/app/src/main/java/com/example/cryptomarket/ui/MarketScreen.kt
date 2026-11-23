package com.example.cryptomarket.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.cryptomarket.model.CoinMarket
import com.example.cryptomarket.viewmodel.MarketViewModel
import java.text.NumberFormat
import java.util.Locale

private val PageBg = Color(0xFF191B2A)
private val CardBg = Color(0xFF303340)
private val Muted = Color(0xFF9EA3B2)
private val Blue = Color(0xFF5F91FF)
private val Green = Color(0xFF55C97A)
private val Red = Color(0xFFE96D66)

@Composable
fun MarketScreen(viewModel: MarketViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val favoriteCoins = state.coins.filter { it.id in state.favorites }
    val shownCoins = if (selectedTab == 0) state.coins else favoriteCoins

    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF202231)) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Text("⌂", style = MaterialTheme.typography.titleLarge) }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Star, null) }, label = { Text("Favorites") })
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedTab == 0) "Home" else "Favorites", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = viewModel::refresh, enabled = !state.loading) { Icon(Icons.Default.Refresh, "Refresh market data", tint = Color.White) }
                }
                if (selectedTab == 0) NewsBanner()
                Text(state.error ?: state.apiStatus, color = if (state.usingSampleData) Color(0xFFFFC36A) else Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 12.dp))
                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Blue)
                if (selectedTab == 0) {
                    Text("Favorites", Modifier.padding(top = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                    if (favoriteCoins.isEmpty()) Text("Tap a star to add a favorite coin.", color = Muted, modifier = Modifier.padding(vertical = 12.dp))
                    favoriteCoins.chunked(2).forEach { rowCoins ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            rowCoins.forEach { coin -> FavoriteCard(coin, Modifier.weight(1f)) }
                            if (rowCoins.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    Text("All Fluctuations", Modifier.padding(top = 8.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                } else {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (shownCoins.isEmpty()) {
                item { Text("No favorite coins yet.", color = Muted, modifier = Modifier.padding(12.dp)) }
            } else {
                items(shownCoins, key = { it.id }) { coin -> CoinRow(coin, coin.id in state.favorites) { viewModel.toggleFavorite(coin.id) } }
            }
        }
    }
}

@Composable
private fun NewsBanner() {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF203A67)).padding(14.dp)) {
        Text("NEW · MARKET UPDATE", color = Color(0xFFB9D1FF), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("BREAKING", color = Color(0xFF171821), modifier = Modifier.background(Color(0xFFFFD743), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 5.dp), fontWeight = FontWeight.Black)
            Spacer(Modifier.width(6.dp))
            Text("NEWS", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light)
        }
        Text("Market dashboard · 24h data", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun FavoriteCard(coin: CoinMarket, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(126.dp), colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinIcon(coin, 28.dp)
                Spacer(Modifier.width(8.dp))
                Text(coin.symbol.uppercase(), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatPrice(coin.currentPrice), color = Color.White, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatChange(coin.change24h), color = changeColor(coin.change24h), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            Sparkline(coin, Modifier.fillMaxWidth().height(30.dp))
        }
    }
}

@Composable
private fun CoinRow(coin: CoinMarket, favorite: Boolean, onFavorite: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(coin, 38.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(coin.symbol.uppercase(), color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(coin.name, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatPrice(coin.currentPrice), color = Color.White, fontWeight = FontWeight.Medium)
                Text(formatChange(coin.change24h), color = changeColor(coin.change24h), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "Toggle favorite", tint = if (favorite) Color(0xFFFFC84B) else Muted) }
        }
    }
}

@Composable
private fun CoinIcon(coin: CoinMarket, size: androidx.compose.ui.unit.Dp) {
    if (coin.image.isNotBlank()) {
        AsyncImage(model = coin.image, contentDescription = coin.name, modifier = Modifier.size(size).clip(CircleShape))
    } else {
        Box(Modifier.size(size).clip(CircleShape).background(Color(0xFF263A5B)), contentAlignment = Alignment.Center) {
            Text(coin.symbol.take(1).uppercase(), color = Blue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Sparkline(coin: CoinMarket, modifier: Modifier = Modifier) {
    val color = changeColor(coin.change24h)
    Canvas(modifier) {
        val prices = coin.sparklineIn7d?.price.orEmpty()
        val values = if (prices.size >= 2) prices else (0..20).map { index ->
            val seed = coin.id.hashCode()
            val noise = (((seed shr (index % 16)) and 31) - 15) / 40.0
            val trend = (coin.change24h ?: 0.0) / 120.0 * index
            .6 - trend - noise
        }
        val minimum = values.minOrNull() ?: 0.0
        val range = (values.maxOrNull() ?: 1.0) - minimum
        val points = values.mapIndexed { index, value ->
            val y = if (range == 0.0) .5 else 1.0 - ((value - minimum) / range)
            Offset(size.width * index / (values.size - 1).coerceAtLeast(1), size.height * y.toFloat().coerceIn(.08f, .92f))
        }
        val path = Path().apply {
            points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun formatPrice(price: Double?): String {
    if (price == null) return "—"
    val format = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = if (price < 1) 4 else 2 }
    return format.format(price)
}

private fun formatChange(change: Double?): String = change?.let { String.format(Locale.US, "%+.2f%%", it) } ?: "—"
private fun changeColor(change: Double?): Color = if ((change ?: 0.0) >= 0) Green else Red
