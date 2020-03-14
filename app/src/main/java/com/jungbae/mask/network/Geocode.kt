package com.jungbae.mask.network

data class Geocode (
    val status : String,
    val meta : Meta,
    val addresses : List<Addresses>,
    val errorMessage : String
)

data class Meta (
    val totalCount : Int,
    val page : Int,
    val count : Int
)

data class Addresses (
    val roadAddress : String,
    val jibunAddress : String,
    val englishAddress : String,
    val addressElements : List<AddressElements>,
    val x : Double,
    val y : Double,
    val distance : Int
)

data class AddressElements (
    val types : List<String>,
    val longName : String,
    val shortName : String,
    val code : String
)