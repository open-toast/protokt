@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/system_parameter.proto
package com.google.api

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.copyList
import com.toasttab.protokt.rt.finishList
import com.toasttab.protokt.rt.sizeof
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList

/**
 * ### System parameter configuration
 *
 *  A system parameter is a special kind of parameter defined by the API system, not by an
 * individual API. It is typically mapped to an HTTP header and/or a URL query parameter. This
 * configuration specifies which methods change the names of the system parameters.
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.SystemParameters")
class SystemParameters private constructor(
    /**
     * Define system parameters.
     *
     *  The parameters defined here will override the default parameters implemented by the system.
     * If this field is missing from the service config, default system parameters will be used.
     * Default system parameters and names is implementation-dependent.
     *
     *  Example: define api key for all methods
     *
     *      system_parameters       rules:         - selector: "*"
     * parameters:             - name: api_key               url_query_parameter: api_key
     *
     *
     *
     *  Example: define 2 api key names for a specific method.
     *
     *      system_parameters       rules:         - selector: "/ListShelves"
     * parameters:             - name: api_key               http_header: Api-Key1             - name:
     * api_key               http_header: Api-Key2
     *
     *  **NOTE:** All service configuration rules follow "last one wins" order.
     */
    val rules: List<SystemParameterRule>,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (rules.isNotEmpty()) {
            result += (sizeof(Tag(1)) * rules.size) + rules.sumOf { sizeof(it) }
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (rules.isNotEmpty()) {
            rules.forEach { serializer.write(Tag(10)).write(it) }
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is SystemParameters &&
        other.rules == rules &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + rules.hashCode()
        return result
    }

    override fun toString(): String = "SystemParameters(" +
        "rules=$rules" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: SystemParametersDsl.() -> Unit): SystemParameters =
            SystemParameters.Deserializer {
        rules = this@SystemParameters.rules
        unknownFields = this@SystemParameters.unknownFields
        dsl()
    }

    class SystemParametersDsl {
        var rules: List<SystemParameterRule> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): SystemParameters = SystemParameters(finishList(rules),
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<SystemParameters>,
            (SystemParametersDsl.() -> Unit) -> SystemParameters {
        override fun deserialize(deserializer: KtMessageDeserializer): SystemParameters {
            var rules : MutableList<SystemParameterRule>? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return SystemParameters(finishList(rules),
                            UnknownFieldSet.from(unknownFields))
                    10 -> rules = (rules ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {

                                    add(deserializer.readMessage(com.google.api.SystemParameterRule))
                                   }
                               }
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: SystemParametersDsl.() -> Unit): SystemParameters =
                SystemParametersDsl().apply(dsl).build()
    }
}

/**
 * Define a system parameter rule mapping system parameter definitions to methods.
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.SystemParameterRule")
class SystemParameterRule private constructor(
    /**
     * Selects the methods to which this rule applies. Use '*' to indicate all methods in all APIs.
     *
     *  Refer to [selector][google.api.DocumentationRule.selector] for syntax details.
     */
    val selector: String,
    /**
     * Define parameters. Multiple names may be defined for a parameter. For a given method call,
     * only one of them should be used. If multiple names are used the behavior is
     * implementation-dependent. If none of the specified names are present the behavior is
     * parameter-dependent.
     */
    val parameters: List<SystemParameter>,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (selector.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(selector)
        }
        if (parameters.isNotEmpty()) {
            result += (sizeof(Tag(2)) * parameters.size) + parameters.sumOf { sizeof(it) }
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (selector.isNotEmpty()) {
            serializer.write(Tag(10)).write(selector)
        }
        if (parameters.isNotEmpty()) {
            parameters.forEach { serializer.write(Tag(18)).write(it) }
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is SystemParameterRule &&
        other.selector == selector &&
        other.parameters == parameters &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + selector.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

    override fun toString(): String = "SystemParameterRule(" +
        "selector=$selector, " +
        "parameters=$parameters" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: SystemParameterRuleDsl.() -> Unit): SystemParameterRule =
            SystemParameterRule.Deserializer {
        selector = this@SystemParameterRule.selector
        parameters = this@SystemParameterRule.parameters
        unknownFields = this@SystemParameterRule.unknownFields
        dsl()
    }

    class SystemParameterRuleDsl {
        var selector: String = ""

        var parameters: List<SystemParameter> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): SystemParameterRule = SystemParameterRule(selector,
        finishList(parameters),
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<SystemParameterRule>,
            (SystemParameterRuleDsl.() -> Unit) -> SystemParameterRule {
        override fun deserialize(deserializer: KtMessageDeserializer): SystemParameterRule {
            var selector = ""
            var parameters : MutableList<SystemParameter>? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return SystemParameterRule(selector,
                            finishList(parameters),
                            UnknownFieldSet.from(unknownFields))
                    10 -> selector = deserializer.readString()
                    18 -> parameters = (parameters ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readMessage(com.google.api.SystemParameter))
                                   }
                               }
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: SystemParameterRuleDsl.() -> Unit): SystemParameterRule =
                SystemParameterRuleDsl().apply(dsl).build()
    }
}

/**
 * Define a parameter's name and location. The parameter may be passed as either an HTTP header or a
 * URL query parameter, and if both are passed the behavior is implementation-dependent.
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.SystemParameter")
class SystemParameter private constructor(
    /**
     * Define the name of the parameter, such as "api_key" . It is case sensitive.
     */
    val name: String,
    /**
     * Define the HTTP header name to use for the parameter. It is case insensitive.
     */
    val httpHeader: String,
    /**
     * Define the URL query parameter name to use for the parameter. It is case sensitive.
     */
    val urlQueryParameter: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (httpHeader.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(httpHeader)
        }
        if (urlQueryParameter.isNotEmpty()) {
            result += sizeof(Tag(3)) + sizeof(urlQueryParameter)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (httpHeader.isNotEmpty()) {
            serializer.write(Tag(18)).write(httpHeader)
        }
        if (urlQueryParameter.isNotEmpty()) {
            serializer.write(Tag(26)).write(urlQueryParameter)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is SystemParameter &&
        other.name == name &&
        other.httpHeader == httpHeader &&
        other.urlQueryParameter == urlQueryParameter &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + httpHeader.hashCode()
        result = 31 * result + urlQueryParameter.hashCode()
        return result
    }

    override fun toString(): String = "SystemParameter(" +
        "name=$name, " +
        "httpHeader=$httpHeader, " +
        "urlQueryParameter=$urlQueryParameter" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: SystemParameterDsl.() -> Unit): SystemParameter =
            SystemParameter.Deserializer {
        name = this@SystemParameter.name
        httpHeader = this@SystemParameter.httpHeader
        urlQueryParameter = this@SystemParameter.urlQueryParameter
        unknownFields = this@SystemParameter.unknownFields
        dsl()
    }

    class SystemParameterDsl {
        var name: String = ""

        var httpHeader: String = ""

        var urlQueryParameter: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): SystemParameter = SystemParameter(name,
        httpHeader,
        urlQueryParameter,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<SystemParameter>,
            (SystemParameterDsl.() -> Unit) -> SystemParameter {
        override fun deserialize(deserializer: KtMessageDeserializer): SystemParameter {
            var name = ""
            var httpHeader = ""
            var urlQueryParameter = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return SystemParameter(name,
                            httpHeader,
                            urlQueryParameter,
                            UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> httpHeader = deserializer.readString()
                    26 -> urlQueryParameter = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: SystemParameterDsl.() -> Unit): SystemParameter =
                SystemParameterDsl().apply(dsl).build()
    }
}
