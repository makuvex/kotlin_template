package com.jungbae.mask.network

data class StoresByData (

    val count : Int,
    val stores : List<Store>
)

data class StoresByKeyword (
    val code: String,
    val count : Int,
    val result : List<Store>
)

data class Store (

    val code : String = "",
    val name : String = "",
    val addr : String = "",
    val type : String = "",          // 판매처 유형[약국: '01', 우체국: '02', 농협: '03']
    val lat : Float = 0.0f,
    val lng : Float = 0.0f,
    val stock_at : String?,      // 입고시간, string($YYYY/MM/DD HH:mm:ss)
    val remain_stat : String?,   // 재고 상태[100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty']
    val created_at : String?,     // 데이터 생성 일자, ($YYYY/MM/DD HH:mm:ss)

    var convertedLat : Double = .0,
    var convertedLng : Double = .0,
    var distance: Float = .0f,

    var favorite: Boolean = false
)

data class UserModel(val result: User): BaseRespData()
data class User(val seq: Int, val comment: Int, val recommend: Int)

data class Keywords(val result: ArrayList<AlertKeyword>): BaseRespData()
data class AlertKeyword(val keyword: String = "", var alert: Int = 0)

data class BaseResult(val result: Unit?): BaseRespData()