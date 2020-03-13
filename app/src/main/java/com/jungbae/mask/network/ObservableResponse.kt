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


    private fun validDealData(t: T): Boolean {
        val resp: BaseRespData = t as BaseRespData
        if(resp.code == "2000") {
            return true
        }
        return false
    }

    private fun validKeywordData(t: T): Boolean {
        val resp: StoresByKeyword = t as StoresByKeyword
        if(resp.code == "2000") {
            return true
        }
        return false
    }

    private fun checkValidResponseCode(t: T): Boolean {
        if (t as? StoresByData != null) {
            return validData(t)
        } else if (t as? BaseRespData != null) {
            return validDealData(t)
        } else if (t as? StoresByKeyword != null) {
            return validKeywordData(t)
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

    private fun validData(t: T): Boolean {
        val data: StoresByData = t as StoresByData
        //val code = school.schoolInfo.get(SchoolDataIndex.HEAD.index).head.get(SchoolDataIndex.RESULT_CODE.index).result.code

        if (data.count >= 0) {
            return true
        }
        return false
    }


}