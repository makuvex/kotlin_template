package com.jungbae.mask.network

import io.reactivex.Single
import retrofit2.http.*

interface MaskApiInterface {

    @GET(ApiSetting.Service.GET_MASK_SALER)
    fun getMaskSaler(@Query("page") page: Int,
                      @Query("perPage") perPage: Int): Single<MaskSaler>

    @GET(ApiSetting.Service.GET_STORES_BY_GEO)
    fun getStoresByGeo( @Query("lat") lat: Double,
                        @Query("lng") lng: Double,
                        @Query("m") radius: Int): Single<StoresByData>

    @GET(ApiSetting.Service.GET_STORES_BY_ADDR)
    fun getStoresByAddr(@Query("address") address: String): Single<StoresByData>

    @POST(ApiSetting.Service.User)
    fun registId(@Query("fcmToken") fcmToken: String,
                 @Query("deviceId") deviceId: String): Single<UserModel>

    @GET(ApiSetting.Service.Keyword)
    fun keyword(@Query("userSeq") userSeq: String): Single<StoresByKeyword>

    @POST(ApiSetting.Service.Keyword)
    fun registKeyword(@Query("lat") lat: Double,
                      @Query("lng") lng: Double,
                      @Query("userSeq") userSeq: Int,
                      @Query("code") code: String): Single<BaseResult>

//    @PUT(ApiSetting.Service.Keyword)
//    fun updateKeyword(@Query("keyword") keyword: String,
//                      @Query("userSeq") userSeq: String,
//                      @Query("alert") alert: Int): Single<BaseResult>

    @DELETE(ApiSetting.Service.Keyword)
    fun deleteKeyword(@Query("code") code: String,
                      @Query("userSeq") userSeq: Int): Single<BaseResult>

/*
    @GET(ApiSetting.Service.GET_SCHOOL_INFO)
    fun getSchoolData(@Query("Type") type: String,
                      @Query("pIndex") index: Int,
                      @Query("pSize") size: Int,
                      @Query("SCHUL_NM") name: String,
                      @Query("KEY") authKey: String): Single<SchoolData>

    @GET(ApiSetting.Service.GET_MEAL_SERVIE_INFO)
    fun getSchoolMealData(@Query("Type") type: String,
                          @Query("pIndex") index: Int,
                          @Query("pSize") size: Int,
                          @Query("ATPT_OFCDC_SC_CODE") officeEduCode: String,
                          @Query("SD_SCHUL_CODE") schoolCode: String,
                          @Query("KEY") authKey: String,
                          @Query("MLSV_FROM_YMD") fromDate: String,
                          @Query("MLSV_TO_YMD") toDate: String): Single<SchoolMealData>
*/
}