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

fun Int.getBoolean(): Boolean {
    return this == 1
}

fun Boolean.getInt(): Int {
    return if(this) 1 else 0
}

class PushService {

    companion object {
        private var t: MaskApiInterface? = null
        private var instance: NetworkService? = null

        fun create(): PushService {
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
                .baseUrl("http://makuvex7.cafe24.com:8090")
                //.baseUrl("http://192.168.0.106:5000")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            when(t) {
                null -> { t = retrofit.create(MaskApiInterface::class.java)}
            }

            return PushService()
        }

    }


    fun registUser(fcmToken: String, deviceId: String): Observable<UserModel> {
        return t?.let {
            it.registId(fcmToken, deviceId).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun keyword(userSeq: String): Observable<StoresByKeyword> {
        return t?.let {
            it.keyword(userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun registKeyword(lat: Double, lng: Double, userSeq: Int, code: String): Observable<BaseResult> {
        return t?.let {
            it.registKeyword(lat, lng, userSeq, code).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }


    fun deleteKeyword(keyword: String, userSeq: Int): Observable<BaseResult> {
        return t?.let {
            it.deleteKeyword(keyword, userSeq).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }
}