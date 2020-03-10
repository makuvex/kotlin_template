package com.jungbae.mask.network

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class SchoolFoodService {

    companion object {
        private var t: SchoolFoodApiInterface? = null

        fun create(): SchoolFoodService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://open.neis.go.kr/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            if(t == null) {
                t = retrofit.create(SchoolFoodApiInterface::class.java)
            }

            return SchoolFoodService()
        }
    }

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

    fun <T> ioMain(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
        }
    }
}