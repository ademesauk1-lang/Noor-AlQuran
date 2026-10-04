package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface QuranApiService {
    @GET("surah/{id}")
    suspend fun getSurahArabic(@Path("id") surahId: Int): SurahResponse

    @GET("surah/{id}/{endpoint}")
    suspend fun getSurahTranslation(@Path("id") surahId: Int, @Path("endpoint") endpoint: String): SurahResponse
}

data class SurahResponse(
    val code: Int,
    val status: String,
    val data: SurahData
)

data class SurahData(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val ayahs: List<AyahData>
)

data class AyahData(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int
)

object RetrofitClient {
    private const val BASE_URL = "https://api.alquran.cloud/v1/"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val apiService: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranApiService::class.java)
    }
}
