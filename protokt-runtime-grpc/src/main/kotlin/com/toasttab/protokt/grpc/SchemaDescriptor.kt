package com.toasttab.protokt.grpc

import com.toasttab.protokt.FileDescriptor

class SchemaDescriptor(
    val ktClassName: String,
    val ktFileDescriptorClassName: String
) {

    @Suppress("UNCHECKED_CAST")
    val fileDescriptor: FileDescriptor by lazy {
        val clazz = Class.forName(ktFileDescriptorClassName) as Class<Any>
        val obj = clazz.objectInstance
        val getDescriptor = clazz.methods.find { it.name == "getDescriptor" }
            ?: throw IllegalStateException("No getDescriptor method found on $clazz")
        getDescriptor.invoke(obj) as FileDescriptor
    }
}

@Suppress("UNCHECKED_CAST")
val <T> Class<T>.objectInstance: T
    get() = getDeclaredField("INSTANCE").get(null) as T
