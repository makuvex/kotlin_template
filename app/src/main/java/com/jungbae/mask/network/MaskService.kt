package com.jungbae.mask.network

import android.os.Build
import com.jungbae.mask.BuildConfig
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class MaskService {

    companion object {
        private var t: MaskApiInterface? = null
        private var instance: NetworkService? = null

        fun create(): MaskService {
            val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client: OkHttpClient = OkHttpClient.Builder().apply {
                addInterceptor { chain ->
                    val req = chain.request().newBuilder().apply {
                        header("version", BuildConfig.VERSION_NAME)
                        header("os_info", "android_" + Build.VERSION.SDK_INT.toString())
                        header("device", Build.MANUFACTURER + "_" + Build.MODEL)
                    }.build()

                    chain.proceed(req)
                }

                addInterceptor(logging)
            }.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://8oi9s0nnth.apigw.ntruss.com")
                //.baseUrl("http://192.168.0.106:5000")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            when(t) {
                null -> { t = retrofit.create(MaskApiInterface::class.java)}
            }

            return MaskService()
        }

//        fun create(): MaskService {
//            val retrofit = Retrofit.Builder()
//                //.baseUrl("https://open.neis.go.kr/")
//                .baseUrl("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/")
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            if(t == null) {
//                t = retrofit.create(MaskApiInterface::class.java)
//            }
//
//            return MaskService()
//        }
    }

    fun getMaskSaler(page: Int, perPage: Int): Observable<MaskSaler> {
        return t?.let {
            it.getMaskSaler(page, perPage).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun getStoresByGeo(lat: Double, lng: Double, radius: Int): Observable<StoresByData> {
        return t?.let {
            it.getStoresByGeo(lat, lng, radius).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun getStoresByAddr(address: String): Observable<StoresByData> {
        return t?.let {
            it.getStoresByAddr(address).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }

}