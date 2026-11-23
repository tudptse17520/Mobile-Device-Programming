package com.example.cryptomarket.model

import com.google.gson.annotations.SerializedName

data class CoinMarket(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("price_change_percentage_24h") val change24h: Double?,
    @SerializedName("market_cap") val marketCap: Double?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("sparkline_in_7d") val sparklineIn7d: SparklineData? = null
)

data class SparklineData(val price: List<Double> = emptyList())

data class CoinLoreTicker(
    val symbol: String,
    val name: String,
    @SerializedName("price_usd") val priceUsd: String?,
    @SerializedName("percent_change_24h") val change24h: String?
)

data class BinanceTicker(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String
)

data class CoinPaprikaTicker(
    val symbol: String,
    val name: String,
    val quotes: Map<String, CoinPaprikaQuote> = emptyMap()
)

data class CoinPaprikaQuote(
    val price: Double?,
    @SerializedName("percent_change_24h") val change24h: Double?
)

data class KrakenTickerResponse(
    val error: List<String> = emptyList(),
    val result: Map<String, KrakenTicker> = emptyMap()
)

data class KrakenTicker(
    val c: List<String> = emptyList(),
    val o: String = ""
)

object SampleCoins {
    val items = listOf(
        CoinMarket("ethereum", "eth", "Ethereum", "", 1245.34, -0.9, null, 2),
        CoinMarket("bitcoin", "btc", "Bitcoin", "", 3451.12, 1.4, null, 1),
        CoinMarket("litecoin", "ltc", "Litecoin", "", 2125.68, 1.2, null, 20),
        CoinMarket("monero", "xmr", "Monero", "", 120.45, -0.3, null, 35),
        CoinMarket("solana", "sol", "Solana", "", 148.22, 2.1, null, 5),
        CoinMarket("ripple", "xrp", "XRP", "", 0.54, -0.6, null, 6)
    )
}
