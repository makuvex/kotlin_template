package com.jungbae.mask.network

import android.util.Log
import io.reactivex.observers.DisposableObserver


open class ObservableResponse<T>(val onSuccess: ((T) -> Unit)? = {}, val onError: ((T?) -> Unit)? = {}): DisposableObserver<T>() {

    override fun onNext(t: T) {
        when(checkValidResponseCode(t)) {
            true -> {
                // 정상 응답이나 response 값이 없는 경우
                onSuccess?.let {
                    it(t)
                }
            }
            false -> {
                onError?.let {
                    it(t)
                }
            }
        }
    }

    override fun onError(e: Throwable) {
        Log.d("@@@", "onError 1")
        onError?.let {
            it(null)
        }
    }

    override fun onComplete() {
        Log.d("@@@", "onComplete 1")
    }

    private fun checkValidResponseCode(t: T): Boolean {
        if (t as? SchoolData != null) {
            return validSchoolData(t)
        } else if(t as? SchoolMealData != null) {
            return validSchoolMealData(t)
        }

//        when(t) {
//            SchoolData::class -> return validSchoolData(t)
//            SchoolMealData::class -> return validSchoolMealData(t)
//        }

        //val school: SchoolData = t as SchoolData
//        val code = school.schoolInfo.get(SchoolDataIndex.HEAD.index).head.get(SchoolDataIndex.RESULT_CODE.index).result.code
//
//        if (code == "INFO-000") {
//            return true
//        }
        return false
    }

    private fun validSchoolData(t: T): Boolean {
        val school: SchoolData = t as SchoolData
        val code = school.schoolInfo.get(SchoolDataIndex.HEAD.index).head.get(SchoolDataIndex.RESULT_CODE.index).result.code

        if (code == "INFO-000") {
            return true
        }
        return false
    }

    private fun validSchoolMealData(t: T): Boolean {
        val meal: SchoolMealData = t as SchoolMealData
        val code = meal.mealServiceDietInfo?.let {
            it.get(SchoolDataIndex.HEAD.index).head?.get(SchoolDataIndex.RESULT_CODE.index).result.code
        }
        //val code = meal?.mealServiceDietInfo?.get(SchoolDataIndex.HEAD.index).head?.get(SchoolDataIndex.RESULT_CODE.index).result.code

        if (code == "INFO-000") {
            return true
        }
        return false
    }
}