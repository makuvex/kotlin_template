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

class SchoolFoodService {

    companion object {
        private var t: SchoolFoodApiInterface? = null
        private var instance: NetworkService? = null

        fun create(): SchoolFoodService {
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
                null -> { t = retrofit.create(SchoolFoodApiInterface::class.java)}
            }

            return SchoolFoodService()
        }

//        fun create(): SchoolFoodService {
//            val retrofit = Retrofit.Builder()
//                //.baseUrl("https://open.neis.go.kr/")
//                .baseUrl("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/")
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            if(t == null) {
//                t = retrofit.create(SchoolFoodApiInterface::class.java)
//            }
//
//            return SchoolFoodService()
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
/*
    fun getSchoolData(type: String, index: Int, size: Int, name: String, key: String): Observable<SchoolData> {
        return t?.let {
            it.getSchoolData(type, index, size, name, key).toObservable().compose(ioMain())
        } ?: Observable.empty()
    }

    fun getSchoolMealData(type: String,
                          index: Int,
                          size: Int,
                          officeCode: String,
                          schoolCode: String,
                          authKey: String,
                          fromDate: String,
                          toDate: String): Observable<SchoolMealData> {

        return t?.let {
            it.getSchoolMealData(
                type,
                index,
                size,
                officeCode,
                schoolCode,
                authKey,
                fromDate,
                toDate)
                .toObservable().compose(ioMain())

        } ?: Observable.empty()
    }
*/
    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }
}