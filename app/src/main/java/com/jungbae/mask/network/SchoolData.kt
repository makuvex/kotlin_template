package com.jungbae.mask.network

//import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName

enum class SchoolDataIndex(val index: Int) {
    HEAD(0),
    ROW(1),
    RESULT_CODE(1)
}

/* SchoolData */
data class SchoolData(val schoolInfo: ArrayList<SchoolInfo>): BaseRespData()
data class SchoolInfo(val head: ArrayList<Head>,
                      var row: ArrayList<School>)

data class Head(    @SerializedName("list_total_count") val listCount: Int,
                    @SerializedName("RESULT") val result: Result)

data class Result(  @SerializedName("CODE") val code: String,
                    @SerializedName("MESSAGE") val msg: String)

data class School(  @SerializedName("ATPT_OFCDC_SC_CODE") val eduOfficecode: String,
                    @SerializedName("ATPT_OFCDC_SC_NM") val eduOfficName: String,
                    @SerializedName("SD_SCHUL_CODE") val schoolCode: String,
                    @SerializedName("SCHUL_NM") val schoolName: String,
                    @SerializedName("LCTN_SC_NM") val administrativeDistrict: String,
                    @SerializedName("ORG_RDNZC") val zipCode: String,
                    @SerializedName("ENG_SCHUL_NM") val schoolEngName: String,
                    @SerializedName("ORG_RDNMA") val roadNameAddress: String,
                    @SerializedName("ORG_RDNDA") val roadNameDetailAddress: String,
                    @SerializedName("ORG_TELNO") val telNumber: String,
                    @SerializedName("HMPG_ADRES") val homePage: String,
                    @SerializedName("FOND_YMD") val establishmentDate: String)

/* SchoolMealData */
data class SchoolMealData(val mealServiceDietInfo: ArrayList<MealServiceDietInfo>): BaseRespData()
data class MealServiceDietInfo(val head: ArrayList<Head>,
                               var row: ArrayList<Meal>)

data class Meal(  @SerializedName("ATPT_OFCDC_SC_CODE") val eduOfficecode: String,
                  @SerializedName("ATPT_OFCDC_SC_NM") val eduOfficName: String,
                  @SerializedName("SD_SCHUL_CODE") val schoolCode: String,
                  @SerializedName("SCHUL_NM") val schoolName: String,
                  @SerializedName("MMEAL_SC_CODE") val mealCode: String,
                  @SerializedName("MMEAL_SC_NM") val mealName: String,
                  @SerializedName("MLSV_YMD") val mealTime: String,
                  @SerializedName("MLSV_FGR") val mealServiceMaxCapa: String,
                  @SerializedName("DDISH_NM") val dishName: String,
                  @SerializedName("ORPLC_INFO") val originInfo: String,
                  @SerializedName("CAL_INFO") val calInfo: String,
                  @SerializedName("NTR_INFO") val nutiritionInfo: String,
                  @SerializedName("MLSV_FROM_YMD") val queryStartDate: String,
                  @SerializedName("MLSV_TO_YMD") val queryEndDate: String)

/* UserData */

data class SimpleSchoolData(@SerializedName("name") val name: String = "",
                            @SerializedName("address") val address: String = "",
                            @SerializedName("schoolCode") val schoolCode: String = "",
                            @SerializedName("officeCode") val officeCode: String = "")



data class FBSchoolData(val id: String, val data: SimpleSchoolData)

data class SimpleSchoolMealData(val name: String,
                                val date: String,
                                val meal: String,
                                val mealKind: String,
                                val cal: String,
                                val schoolCode: String,
                                val officeCode: String,
                                val today: Boolean = false)

data class UserModel(val result: User): BaseRespData()
data class User(val seq: Int, val comment: Int, val recommend: Int)

data class Keywords(val result: ArrayList<AlertKeyword>): BaseRespData()
data class AlertKeyword(val keyword: String = "", var alert: Int = 0)

data class BaseResult(val result: Unit?): BaseRespData()

/*
class SchoolData {

    @SerializedName("schoolInfo")
    var schoolInfo: ArrayList<SchoolInfo>? = null

    inner class SchoolInfo {

        @SerializedName("head")
        var head: ArrayList<Head>? = null

        inner class Head {
            @SerializedName("list_total_count")
            var listCount: Int? = null

            @SerializedName("RESULT")
            var result: Result? = null

            inner class Result {
                @SerializedName("CODE")
                var code: String? = null

                @SerializedName("MESSAGE")
                var message: String? = null
            }
        }

        @SerializedName("row")
        var row: ArrayList<School>? = null

        inner class School {
            @SerializedName("ATPT_OFCDC_SC_CODE")
            var eduOfficecode: String? = null

            @SerializedName("ATPT_OFCDC_SC_NM")
            var eduOfficName: String? = null

            @SerializedName("SD_SCHUL_CODE")
            var schoolCode: String? = null

            @SerializedName("SCHUL_NM")
            var schoolName: String? = null

            @SerializedName("LCTN_SC_NM")   // 행정구역
            var administrativeDistrict: String? = null

            @SerializedName("ORG_RDNZC")
            var zipCode: String? = null

            @SerializedName("ENG_SCHUL_NM")
            var schoolEngName: String? = null

            @SerializedName("ORG_RDNMA")
            var roadNameAddress: String? = null

            @SerializedName("ORG_RDNDA")
            var roadNameDetailAddress: String? = null

            @SerializedName("ORG_TELNO")
            var telNumber: String? = null

            @SerializedName("HMPG_ADRES")
            var homePage: String? = null

            @SerializedName("FOND_YMD")
            var establishmentDate: String? = null
        }
    }
}
*/