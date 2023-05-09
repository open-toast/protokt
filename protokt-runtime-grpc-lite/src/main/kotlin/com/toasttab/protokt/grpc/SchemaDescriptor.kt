package com.toasttab.protokt.grpc

class SchemaDescriptor(
    private val className: String,
    private val fileDescriptorClassName: String
) {

    /**
     * This returns a com.toasttab.protokt.FileDescriptor, which isn't available in the
     * lite runtime.
     */
    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        "You can only use this with the non-lite runtime. If you're using the non-lite runtime, replace this with" +
            "`fileDescriptor` found in `com.toasttab.protokt.grpc.SchemaDescriptorExtensions`",
        ReplaceWith("com.toasttab.protokt.grpc.fileDescriptor")
    )
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
        getDescriptor.invoke(obj)
    }
}

@Suppress("UNCHECKED_CAST")
private val <T> Class<T>.objectInstance: T
    get() = getDeclaredField("INSTANCE").get(null) as T
