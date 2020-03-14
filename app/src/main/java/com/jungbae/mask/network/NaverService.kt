package com.jungbae.mask.network

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class NaverService {

    companion object {
        private var t: MaskApiInterface? = null
        private var instance: NetworkService? = null

        fun create(): NaverService {
            val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client: OkHttpClient = OkHttpClient.Builder().apply {
                addInterceptor { chain ->
                    val req = chain.request().newBuilder().apply {
                        header("X-NCP-APIGW-API-KEY-ID", "syr5kxfrr1")
                        header("X-NCP-APIGW-API-KEY", "uZPonbuDI2h1now5Ntp0E40KzunlnRs2drLHKOOk")
                    }.build()

                    chain.proceed(req)
                }

                addInterceptor(logging)
            }.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            when(t) {
                null -> { t = retrofit.create(MaskApiInterface::class.java)}
            }

            return NaverService()
        }
    }

    fun getGeocode(address: String): Observable<Geocode> {
        return t?.let {
            it.geocode(address, 50).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }

}