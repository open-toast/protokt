package com.toasttab.protokt.grpc

import com.toasttab.protokt.FileDescriptor
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

data class KtSchemaDescriptor(
    val ktClassName: String,
    val ktFileDescriptorClassName: String
) {

    @Suppress("UNCHECKED_CAST")
    val fileDescriptor: FileDescriptor by lazy {
        val clazz = Class.forName(ktFileDescriptorClassName).kotlin as KClass<Any>
        val obj = clazz.objectInstance!!
        val descriptorProperty = clazz.memberProperties.find { it.name == "descriptor" }!!
        descriptorProperty.get(obj) as FileDescriptor
    }
}
