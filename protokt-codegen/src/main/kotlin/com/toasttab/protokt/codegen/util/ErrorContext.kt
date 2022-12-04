package com.toasttab.protokt.codegen.util

object ErrorContext {
    private const val FILE_NAME = "fileName"
    private const val MESSAGE_NAME = "messageName"
    private const val ENUM_NAME = "enumName"
    private const val SERVICE_NAME = "serviceName"

    private val context = mutableMapOf<String, Any?>()

    fun fileName() =
        context[FILE_NAME]

    fun messageName() =
        context[MESSAGE_NAME]

    fun <T> withFileName(name: String, action: () -> T) =
        withProperty(FILE_NAME, name, action)

    fun <T> withMessageName(name: Any, action: () -> T) =
        withProperty(MESSAGE_NAME, name, action)

    fun <T> withEnumName(name: Any, action: () -> T) =
        withProperty(ENUM_NAME, name, action)

    fun <T> withServiceName(name: Any, action: () -> T) =
        withProperty(SERVICE_NAME, name, action)

    private fun <T> withProperty(propertyName: String, propertyValue: Any, action: () -> T): T {
        context[propertyName] = propertyValue
        val result = action()
        context[propertyName] = null
        return result
    }
}
