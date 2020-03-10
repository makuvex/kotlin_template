package com.jungbae.mask.network

import java.lang.reflect.Modifier
import java.util.*

abstract class BaseRespData (
    var code: String? = null
)

fun Any.reflectionToString(): String {
    val s = LinkedList<String>()
    var clazz: Class<in Any>? = this.javaClass
    while (clazz != null) {
        for (prop in clazz.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }) {
            prop.isAccessible = true
            s += "${prop.name}=" + prop.get(this)?.toString()?.trim()
        }
        clazz = clazz.superclass
    }
    return "${this.javaClass.simpleName}=[${s.joinToString(", ")}]"
}