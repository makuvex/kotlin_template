package com.jungbae.mask.network

data class MaskSaler (

    val totalPages : Int,
    val totalCount : Int,
    val page : Int,
    val count : Int,
    val storeInfos : List<StoreInfos>
)

data class StoreInfos (

    val code : String,
    val name : String,
    val addr : String,
    val type : String,
    val lat : Float,
    val lng : Float
)