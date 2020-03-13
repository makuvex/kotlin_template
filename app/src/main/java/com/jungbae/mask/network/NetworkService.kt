package com.jungbae.mask.network

import io.reactivex.Observable

/*
open class NetworkService {

    @Volatile private var instance: NetworkService? = null

    fun getInstance(): NetworkService {
        val imInstance = instance
        if(imInstance != null) {
            return imInstance
        }

        instance = NetworkService()
        return synchronized(this) {
            val i = instance
            return i!!
        }
    }
}
*/

class NetworkService {

    companion object {
        private var instance: NetworkService? = null

        fun getInstance(): NetworkService {
            if(instance == null) {
                instance = NetworkService()

            }
            return instance!!
        }
    }

    private val service: MaskService
    private val pushService: PushService

    init {
        service = MaskService.create()
        pushService = PushService.create()
    }

    fun getMaskSaler(page: Int, perPage: Int): Observable<MaskSaler> {
        return service.getMaskSaler(page, perPage)
    }

    fun getStoresByGeo(lat: Double, lng: Double, radius: Int): Observable<StoresByData> {
        return service.getStoresByGeo(lat, lng, radius)
    }

    fun getStoresByAddr(addr: String): Observable<StoresByData> {
        return service.getStoresByAddr(addr)
    }
    fun registUser(fcmToken: String, deviceId: String): Observable<UserModel> {
        return pushService.registUser(fcmToken, deviceId)
    }

    fun keyword(userSeq: String): Observable<StoresByKeyword> {
        return pushService.keyword(userSeq)
    }

    fun registKeyword(lat: Double, lng: Double, userSeq: Int, code: String): Observable<BaseResult> {
        return pushService.registKeyword(lat, lng, userSeq, code)
    }


    fun deleteKeyword(keyword: String, userSeq: Int): Observable<BaseResult> {
        return pushService.deleteKeyword(keyword, userSeq)
    }

    /*
    fun getSchoolData(@Query("Type") type: String,
                   @Query("pIndex") index: Int,
                   @Query("pSize") size: Int,
                   @Query("SCHUL_NM") name: String): Single<SchoolData>

     */

    /*

    fun getSchoolData(type: String, index: Int, size: Int, schoolName: String, key: String): Observable<SchoolData> {
        return service.getSchoolData(type, index, size, schoolName, key)
    }

    fun getSchoolMealData(type: String,
                          index: Int,
                          size: Int,
                          officeCode: String,
                          schoolCode: String,
                          authKey: String,
                          fromDate: String,
                          toDate: String): Observable<SchoolMealData> {

        Log.e("@@@", "officeCode ${officeCode}, schoolCode ${schoolCode}")
        return service.getSchoolMealData(type, index, size, officeCode, schoolCode, authKey, fromDate, toDate)
    }
    */
}

