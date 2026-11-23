package com.example.cryptomarket.network

import com.example.cryptomarket.model.BinanceTicker
import com.example.cryptomarket.model.CoinLoreTicker
import com.example.cryptomarket.model.CoinPaprikaTicker
import com.example.cryptomarket.model.KrakenTickerResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinLoreApi {
    @GET("tickers/")
    suspend fun getTickers(@Query("start") start: Int = 0, @Query("limit") limit: Int = 100): List<CoinLoreTicker>
}

interface BinanceMarketApi {
    @GET("api/v3/ticker/24hr")
    suspend fun getTickers(@Query("symbols") symbols: String): List<BinanceTicker>
}

interface CoinPaprikaApi {
    @GET("v1/tickers")
    suspend fun getTickers(@Query("quotes") currency: String = "USD"): List<CoinPaprikaTicker>
}

interface KrakenMarketApi {
    @GET("0/public/Ticker")
    suspend fun getTickers(@Query("pair") pairs: String = "XBTUSD,ETHUSD,LTCUSD,SOLUSD,XRPUSD"): KrakenTickerResponse
}
