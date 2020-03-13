package com.jungbae.mask.network.preference

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jungbae.mask.CommonApplication
import com.jungbae.mask.network.SimpleSchoolData

object SchoolFoodPreferencesConstant {
    val CHILD_NAME = "CHILD_NAME"
    val CHILD_HASH_ID = "CHILD_HASH_ID"

    val OFFICE_SC_CODE = "OFFICE_SC_CODE"
    val OFFICE_SC_NAME = "OFFICE_SC_NAME"

    val SCHOOL_CODE = "SCHOOL_CODE"
    val SCHOOL_NAME = "SCHOOL_NAME"
}

class SchoolFoodPreference {

    companion object {
        private var instance: SharedPreferences? = null

        init {
            instance = CommonApplication.context.getSharedPreferences("SchoolFood", 0)
        }


        @JvmStatic
        var schoolData: MutableSet<SimpleSchoolData>?
            get() {
                instance?.run {
                    val gson = GsonBuilder().create()
                    val data = getString(PreferencesConstant.SCHOOL_CODE, null)
                    Log.e("@@@","schoolData get data ${data}")

                    val type = object: TypeToken<MutableSet<SimpleSchoolData>>(){}.type
                    val set = gson.fromJson<MutableSet<SimpleSchoolData>>(data, type)

                    return gson.fromJson<MutableSet<SimpleSchoolData>>(data, type)
                }
                return null
            }
            set(data) {
                instance?.let {
                    data?.run {
                        Log.e("@@@","schoolData set data@@@")
                        val set = GsonBuilder().create().toJson(data)
                        it.edit().putString(PreferencesConstant.SCHOOL_CODE, set).apply()
                    }
                }
            }

        fun addSchoolData(data: SimpleSchoolData) {
            instance?.run {
                val preSet = getString(PreferencesConstant.SCHOOL_CODE, null)
                if(preSet == null) {
                    Log.e("@@@","preSet null set will be changed")

                    val set = mutableSetOf<SimpleSchoolData>()
                    set.add(data)

                    schoolData = set
                    //edit().putStringSet(PreferencesConstant.SCHOOL_CODE, set.map { GsonBuilder().create().toJson(it) }.toMutableSet()).apply()
                } else {
                    var pre = schoolData
                    pre?.add(data)
                    schoolData = pre
                }
            }
        }

//
//        @JvmStatic
//        var officeCodeList: MutableSet<String>?
//            get() = instance?.let { it.stringSet(PreferencesConstant.OFFICE_SC_CODE).get() }
//            private set(id) {
//                instance?.let {
//                    it.stringSet(PreferencesConstant.OFFICE_SC_CODE).set(id!!)
//                }
//            }
//
//        fun addOfficeCode(code: String) {
//            instance?.let {
//                var list = this.officeCodeList
//                list?.let {
//                    it.add(code)
//                    officeCodeList = list
//                }
//            }
//        }

//        @JvmStatic
//        var officeCode: Long?
//            get() = instance?.let { it.long(PreferencesConstant.OFFICE_SC_CODE).get() }
//            set(time) {
//                instance?.let {
//                    it.long(PreferencesConstant.OFFICE_SC_CODE).set(time ?: 0)
//                }
//            }
    }

}



