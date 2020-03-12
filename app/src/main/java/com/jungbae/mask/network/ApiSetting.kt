package com.jungbae.mask.network

class ApiSetting {

    object Service {
        //https://open.neis.go.kr/hub/schoolInfo?Type=json&pIndex=1&pSize=100&SCHUL_NM=신중
//        const val GET_SCHOOL_INFO               = "/hub/schoolInfo"
//        const val GET_MEAL_SERVIE_INFO          = "/hub/mealServiceDietInfo"


        const val GET_MASK_SALER                    = "/corona19-masks/v1/stores/json"
        const val GET_STORES_BY_GEO                 = "/corona19-masks/v1/storesByGeo/json"
        const val GET_STORES_BY_ADDR                = "/corona19-masks/v1/storesByAddr/json"
    }

}