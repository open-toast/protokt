package com.toasttab.protokt.grpc

import com.toasttab.protokt.FileDescriptor

class SchemaDescriptor(
    private val className: String,
    private val fileDescriptorClassName: String
) {

    /**
     * This returns a com.toasttab.protokt.FileDescriptor, which isn't available in the
     * lite runtime.
     */
    @Suppress("UNCHECKED_CAST")
    val fileDescriptorUntyped: Any by lazy {
        val clazz =
            try {
                Class.forName(fileDescriptorClassName) as Class<Any>
            } catch (ex: ClassNotFoundException) {
                throw IllegalStateException(
                    "descriptor class `$fileDescriptorClassName` not found for `$className`; " +
                        "are the descriptor objects available?",
                    ex
                )
            }
        val obj = clazz.objectInstance
        val getDescriptor = clazz.methods.find { it.name == "getDescriptor" }
            ?: error("No getDescriptor method found on $clazz")
        getDescriptor.invoke(obj) as FileDescriptor
    }
}

@Suppress("UNCHECKED_CAST")
private val <T> Class<T>.objectInstance: T
    get() = getDeclaredField("INSTANCE").get(null) as T
