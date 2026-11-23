package com.example.cryptomarket.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CoinApiClient {
    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val coinLore: CoinLoreApi by lazy {
        retrofit("https://api.coinlore.net/api/").create(CoinLoreApi::class.java)
    }

    val binance: BinanceMarketApi by lazy {
        retrofit("https://data-api.binance.vision/").create(BinanceMarketApi::class.java)
    }

    val coinPaprika: CoinPaprikaApi by lazy {
        retrofit("https://api.coinpaprika.com/").create(CoinPaprikaApi::class.java)
    }

    val kraken: KrakenMarketApi by lazy {
        retrofit("https://api.kraken.com/").create(KrakenMarketApi::class.java)
    }
}
