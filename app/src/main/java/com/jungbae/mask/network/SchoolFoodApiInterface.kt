package com.jungbae.mask.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SchoolFoodApiInterface {

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

}