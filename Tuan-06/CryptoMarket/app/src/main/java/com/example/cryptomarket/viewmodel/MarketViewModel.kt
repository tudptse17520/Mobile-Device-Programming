package com.example.cryptomarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptomarket.model.BinanceTicker
import com.example.cryptomarket.model.CoinLoreTicker
import com.example.cryptomarket.model.CoinMarket
import com.example.cryptomarket.model.CoinPaprikaTicker
import com.example.cryptomarket.model.KrakenTickerResponse
import com.example.cryptomarket.model.SampleCoins
import com.example.cryptomarket.network.CoinApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MarketUiState(
    val coins: List<CoinMarket> = SampleCoins.items,
    val loading: Boolean = false,
    val usingSampleData: Boolean = true,
    val error: String? = null,
    val apiStatus: String = "Đang kết nối các nguồn giá…",
    val lastUpdated: Long? = null,
    val favorites: Set<String> = setOf("ethereum", "bitcoin")
)

class MarketViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState = _uiState.asStateFlow()
    private val refreshLock = Mutex()

    init {
        refresh()
        viewModelScope.launch {
            while (isActive) {
                delay(3_000)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (!refreshLock.tryLock()) return@launch
            try {
                _uiState.update { it.copy(loading = true, error = null) }
                val results = coroutineScope {
                    val coinLore = async { runCatching { CoinApiClient.coinLore.getTickers() } }
                    val binance = async {
                        runCatching {
                            CoinApiClient.binance.getTickers(
                                "[\"BTCUSDT\",\"ETHUSDT\",\"LTCUSDT\",\"XMRUSDT\",\"SOLUSDT\",\"XRPUSDT\"]"
                            )
                        }
                    }
                    val coinPaprika = async { runCatching { CoinApiClient.coinPaprika.getTickers() } }
                    val kraken = async { runCatching { CoinApiClient.kraken.getTickers() } }
                    awaitAll(coinLore, binance, coinPaprika, kraken)
                    Triple(coinLore.await(), binance.await(), Pair(coinPaprika.await(), kraken.await()))
                }
                val coins = mergePrices(
                    results.first.getOrNull(),
                    results.second.getOrNull(),
                    results.third.first.getOrNull(),
                    results.third.second.getOrNull()
                )
                val hasCoinLore = results.first.isSuccess
                val hasBinance = results.second.isSuccess
                val hasPaprika = results.third.first.isSuccess
                val hasKraken = results.third.second.isSuccess
                val activeSources = listOf("CoinLore" to hasCoinLore, "Binance" to hasBinance, "CoinPaprika" to hasPaprika, "Kraken" to hasKraken)
                    .filter { it.second }.map { it.first }
                val hasLiveData = activeSources.isNotEmpty()
                val status = if (hasLiveData) "${activeSources.joinToString(" + ")} · làm mới mỗi 3 giây" else "Không kết nối được API · đang dùng dữ liệu mẫu"
                _uiState.update {
                    it.copy(
                        coins = coins,
                        loading = false,
                        usingSampleData = !hasLiveData,
                        error = if (hasLiveData) null else "Kiểm tra kết nối mạng rồi thử làm mới.",
                        apiStatus = status,
                        lastUpdated = if (hasLiveData) System.currentTimeMillis() else it.lastUpdated
                    )
                }
            } finally {
                refreshLock.unlock()
            }
        }
    }

    fun toggleFavorite(id: String) {
        _uiState.update { state ->
            val next = if (id in state.favorites) state.favorites - id else state.favorites + id
            state.copy(favorites = next)
        }
    }

    private fun mergePrices(
        coinLore: List<CoinLoreTicker>?,
        binance: List<BinanceTicker>?,
        coinPaprika: List<CoinPaprikaTicker>?,
        kraken: KrakenTickerResponse?
    ): List<CoinMarket> {
        val baseCoins = SampleCoins.items
        val loreBySymbol = coinLore.orEmpty().associateBy { it.symbol.uppercase() }
        val binanceBySymbol = binance.orEmpty().associateBy { it.symbol.removeSuffix("USDT").uppercase() }
        val paprikaBySymbol = coinPaprika.orEmpty().associateBy { it.symbol.uppercase() }
        return baseCoins.map { sample ->
            val symbol = sample.symbol.uppercase()
            val lore = loreBySymbol[symbol]
            val ticker = binanceBySymbol[symbol]
            val paprika = paprikaBySymbol[symbol]?.quotes?.get("USD")
            val krakenTicker = kraken?.result?.entries?.firstOrNull { (pair, _) ->
                when (symbol) {
                    "BTC" -> pair.contains("XBT", ignoreCase = true) || pair.contains("BTC", ignoreCase = true)
                    else -> pair.contains(symbol, ignoreCase = true)
                }
            }?.value
            val krakenPrice = krakenTicker?.c?.firstOrNull()?.toDoubleOrNull()
            val krakenChange = krakenTicker?.let { ticker ->
                val open = ticker.o.toDoubleOrNull()
                val close = krakenPrice
                if (open != null && open != 0.0 && close != null) (close - open) / open * 100.0 else null
            }
            sample.copy(
                name = lore?.name ?: sample.name,
                currentPrice = ticker?.lastPrice?.toDoubleOrNull() ?: krakenPrice ?: paprika?.price ?: lore?.priceUsd?.toDoubleOrNull() ?: sample.currentPrice,
                change24h = ticker?.priceChangePercent?.toDoubleOrNull() ?: krakenChange ?: paprika?.change24h ?: lore?.change24h?.toDoubleOrNull() ?: sample.change24h
            )
        }
    }
}
