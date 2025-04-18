/*
 * Copyright (c) 2023 Toast, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/protobuf/api.proto
package com.toasttab.protokt

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
 * Api is a light-weight descriptor for an API Interface.
 *
 *  Interfaces are also described as "protocol buffer services" in some contexts, such as by the
 * "service" keyword in a .proto file, but they are different from API Services, which represent a
 * concrete implementation of an interface as opposed to simply a description of methods and bindings.
 * They are also sometimes simply referred to as "APIs" in other contexts, such as the name of this
 * message itself. See https://cloud.google.com/apis/design/glossary for detailed terminology.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Api")
class Api private constructor(
    /**
     * The fully qualified name of this interface, including package name followed by the
     * interface's simple name.
     */
    val name: String,
    /**
     * The methods of this interface, in unspecified order.
     */
    val methods: List<Method>,
    /**
     * Any metadata attached to the interface.
     */
    val options: List<Option>,
    /**
     * A version string for this interface. If specified, must have the form
     * `major-version.minor-version`, as in `1.10`. If the minor version is omitted, it defaults to
     * zero. If the entire version field is empty, the major version is derived from the package name,
     * as outlined below. If the field is not empty, the version in the package name will be verified
     * to be consistent with what is provided here.
     *
     *  The versioning schema uses [semantic versioning](http://semver.org) where the major version
     * number indicates a breaking change and the minor version an additive, non-breaking change. Both
     * version numbers are signals to users what to expect from different versions, and should be
     * carefully chosen based on the product plan.
     *
     *  The major version is also reflected in the package name of the interface, which must end in
     * `v<major-version>`, as in `google.feature.v1`. For major versions 0 and 1, the suffix can be
     * omitted. Zero major versions must only be used for experimental, non-GA interfaces.
     *
     *
     */
    val version: String,
    /**
     * Source context for the protocol buffer service represented by this message.
     */
    val sourceContext: SourceContext?,
    /**
     * Included interfaces. See [Mixin][].
     */
    val mixins: List<Mixin>,
    /**
     * The source syntax of the service.
     */
    val syntax: Syntax,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (methods.isNotEmpty()) {
            result += (sizeof(Tag(2)) * methods.size) + methods.sumOf { sizeof(it) }
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(3)) * options.size) + options.sumOf { sizeof(it) }
        }
        if (version.isNotEmpty()) {
            result += sizeof(Tag(4)) + sizeof(version)
        }
        if (sourceContext  != null) {
            result += sizeof(Tag(5)) + sizeof(sourceContext)
        }
        if (mixins.isNotEmpty()) {
            result += (sizeof(Tag(6)) * mixins.size) + mixins.sumOf { sizeof(it) }
        }
        if (syntax.value != 0) {
            result += sizeof(Tag(7)) + sizeof(syntax)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (methods.isNotEmpty()) {
            methods.forEach { serializer.write(Tag(18)).write(it) }
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(26)).write(it) }
        }
        if (version.isNotEmpty()) {
            serializer.write(Tag(34)).write(version)
        }
        if (sourceContext  != null) {
            serializer.write(Tag(42)).write(sourceContext)
        }
        if (mixins.isNotEmpty()) {
            mixins.forEach { serializer.write(Tag(50)).write(it) }
        }
        if (syntax.value != 0) {
            serializer.write(Tag(56)).write(syntax)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Api &&
            other.name == name &&
            other.methods == methods &&
            other.options == options &&
            other.version == version &&
            other.sourceContext == sourceContext &&
            other.mixins == mixins &&
            other.syntax == syntax &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + methods.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + sourceContext.hashCode()
        result = 31 * result + mixins.hashCode()
        result = 31 * result + syntax.hashCode()
        return result
    }

    override fun toString(): String = "Api(" +
            "name=$name, " +
            "methods=$methods, " +
            "options=$options, " +
            "version=$version, " +
            "sourceContext=$sourceContext, " +
            "mixins=$mixins, " +
            "syntax=$syntax" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ApiDsl.() -> Unit): Api = Api.Deserializer {
        name = this@Api.name
        methods = this@Api.methods
        options = this@Api.options
        version = this@Api.version
        sourceContext = this@Api.sourceContext
        mixins = this@Api.mixins
        syntax = this@Api.syntax
        unknownFields = this@Api.unknownFields
        dsl()
    }

    class ApiDsl {
        var name: String = ""

        var methods: List<Method> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var version: String = ""

        var sourceContext: SourceContext? = null

        var mixins: List<Mixin> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var syntax: Syntax = Syntax.from(0)

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Api = Api(name,
            finishList(methods),
            finishList(options),
            version,
            sourceContext,
            finishList(mixins),
            syntax,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Api>, (ApiDsl.() -> Unit) -> Api {
        override fun deserialize(deserializer: KtMessageDeserializer): Api {
            var name = ""
            var methods : MutableList<Method>? = null
            var options : MutableList<Option>? = null
            var version = ""
            var sourceContext : SourceContext? = null
            var mixins : MutableList<Mixin>? = null
            var syntax = Syntax.from(0)
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Api(name,
                        finishList(methods),
                        finishList(options),
                        version,
                        sourceContext,
                        finishList(mixins),
                        syntax,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> methods = (methods ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Method))
                        }
                    }
                    26 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    34 -> version = deserializer.readString()
                    42 -> sourceContext =
                        deserializer.readMessage(com.toasttab.protokt.SourceContext)
                    50 -> mixins = (mixins ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Mixin))
                        }
                    }
                    56 -> syntax = deserializer.readEnum(com.toasttab.protokt.Syntax)
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ApiDsl.() -> Unit): Api = ApiDsl().apply(dsl).build()
    }
}

/**
 * Method represents a method of an API interface.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Method")
class Method private constructor(
    /**
     * The simple name of this method.
     */
    val name: String,
    /**
     * A URL of the input message type.
     */
    val requestTypeUrl: String,
    /**
     * If true, the request is streamed.
     */
    val requestStreaming: Boolean,
    /**
     * The URL of the output message type.
     */
    val responseTypeUrl: String,
    /**
     * If true, the response is streamed.
     */
    val responseStreaming: Boolean,
    /**
     * Any metadata attached to the method.
     */
    val options: List<Option>,
    /**
     * The source syntax of this method.
     */
    val syntax: Syntax,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (requestTypeUrl.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(requestTypeUrl)
        }
        if (requestStreaming) {
            result += sizeof(Tag(3)) + sizeof(requestStreaming)
        }
        if (responseTypeUrl.isNotEmpty()) {
            result += sizeof(Tag(4)) + sizeof(responseTypeUrl)
        }
        if (responseStreaming) {
            result += sizeof(Tag(5)) + sizeof(responseStreaming)
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(6)) * options.size) + options.sumOf { sizeof(it) }
        }
        if (syntax.value != 0) {
            result += sizeof(Tag(7)) + sizeof(syntax)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (requestTypeUrl.isNotEmpty()) {
            serializer.write(Tag(18)).write(requestTypeUrl)
        }
        if (requestStreaming) {
            serializer.write(Tag(24)).write(requestStreaming)
        }
        if (responseTypeUrl.isNotEmpty()) {
            serializer.write(Tag(34)).write(responseTypeUrl)
        }
        if (responseStreaming) {
            serializer.write(Tag(40)).write(responseStreaming)
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(50)).write(it) }
        }
        if (syntax.value != 0) {
            serializer.write(Tag(56)).write(syntax)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Method &&
            other.name == name &&
            other.requestTypeUrl == requestTypeUrl &&
            other.requestStreaming == requestStreaming &&
            other.responseTypeUrl == responseTypeUrl &&
            other.responseStreaming == responseStreaming &&
            other.options == options &&
            other.syntax == syntax &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + requestTypeUrl.hashCode()
        result = 31 * result + requestStreaming.hashCode()
        result = 31 * result + responseTypeUrl.hashCode()
        result = 31 * result + responseStreaming.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + syntax.hashCode()
        return result
    }

    override fun toString(): String = "Method(" +
            "name=$name, " +
            "requestTypeUrl=$requestTypeUrl, " +
            "requestStreaming=$requestStreaming, " +
            "responseTypeUrl=$responseTypeUrl, " +
            "responseStreaming=$responseStreaming, " +
            "options=$options, " +
            "syntax=$syntax" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: MethodDsl.() -> Unit): Method = Method.Deserializer {
        name = this@Method.name
        requestTypeUrl = this@Method.requestTypeUrl
        requestStreaming = this@Method.requestStreaming
        responseTypeUrl = this@Method.responseTypeUrl
        responseStreaming = this@Method.responseStreaming
        options = this@Method.options
        syntax = this@Method.syntax
        unknownFields = this@Method.unknownFields
        dsl()
    }

    class MethodDsl {
        var name: String = ""

        var requestTypeUrl: String = ""

        var requestStreaming: Boolean = false

        var responseTypeUrl: String = ""

        var responseStreaming: Boolean = false

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var syntax: Syntax = Syntax.from(0)

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Method = Method(name,
            requestTypeUrl,
            requestStreaming,
            responseTypeUrl,
            responseStreaming,
            finishList(options),
            syntax,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Method>, (MethodDsl.() -> Unit) -> Method
    {
        override fun deserialize(deserializer: KtMessageDeserializer): Method {
            var name = ""
            var requestTypeUrl = ""
            var requestStreaming = false
            var responseTypeUrl = ""
            var responseStreaming = false
            var options : MutableList<Option>? = null
            var syntax = Syntax.from(0)
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Method(name,
                        requestTypeUrl,
                        requestStreaming,
                        responseTypeUrl,
                        responseStreaming,
                        finishList(options),
                        syntax,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> requestTypeUrl = deserializer.readString()
                    24 -> requestStreaming = deserializer.readBool()
                    34 -> responseTypeUrl = deserializer.readString()
                    40 -> responseStreaming = deserializer.readBool()
                    50 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    56 -> syntax = deserializer.readEnum(com.toasttab.protokt.Syntax)
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: MethodDsl.() -> Unit): Method =
            MethodDsl().apply(dsl).build()
    }
}

/**
 * Declares an API Interface to be included in this interface. The including interface must
 * redeclare all the methods from the included interface, but documentation and options are inherited
 * as follows:
 *
 *  - If after comment and whitespace stripping, the documentation   string of the redeclared method
 * is empty, it will be inherited   from the original method.
 *
 *  - Each annotation belonging to the service config (http,   visibility) which is not set in the
 * redeclared method will be   inherited.
 *
 *  - If an http annotation is inherited, the path pattern will be   modified as follows. Any
 * version prefix will be replaced by the   version of the including interface plus the [root][] path
 * if   specified.
 *
 *  Example of a simple mixin:
 *
 *      package google.acl.v1;     service AccessControl {       // Get the underlying ACL object.
 *     rpc GetAcl(GetAclRequest) returns (Acl) {         option (google.api.http).get =
 * "/v1/{resource=**}:getAcl";       }     }
 *
 *      package google.storage.v2;     service Storage {       rpc GetAcl(GetAclRequest) returns
 * (Acl);
 *
 *        // Get a data record.       rpc GetData(GetDataRequest) returns (Data) {         option
 * (google.api.http).get = "/v2/{resource=**}";       }     }
 *
 *  Example of a mixin configuration:
 *
 *      apis:     - name: google.storage.v2.Storage       mixins:       - name:
 * google.acl.v1.AccessControl
 *
 *  The mixin construct implies that all methods in `AccessControl` are also declared with same name
 * and request/response types in `Storage`. A documentation generator or annotation processor will see
 * the effective `Storage.GetAcl` method after inheriting documentation and annotations as follows:
 *
 *      service Storage {       // Get the underlying ACL object.       rpc GetAcl(GetAclRequest)
 * returns (Acl) {         option (google.api.http).get = "/v2/{resource=**}:getAcl";       }       ...
 *     }
 *
 *  Note how the version in the path pattern changed from `v1` to `v2`.
 *
 *  If the `root` field in the mixin is specified, it should be a relative path under which
 * inherited HTTP paths are placed. Example:
 *
 *      apis:     - name: google.storage.v2.Storage       mixins:       - name:
 * google.acl.v1.AccessControl         root: acls
 *
 *  This implies the following inherited HTTP annotation:
 *
 *      service Storage {       // Get the underlying ACL object.       rpc GetAcl(GetAclRequest)
 * returns (Acl) {         option (google.api.http).get = "/v2/acls/{resource=**}:getAcl";       }
 *  ...     }
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Mixin")
class Mixin private constructor(
    /**
     * The fully qualified name of the interface which is included.
     */
    val name: String,
    /**
     * If non-empty specifies a path under which inherited HTTP paths are rooted.
     */
    val root: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (root.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(root)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (root.isNotEmpty()) {
            serializer.write(Tag(18)).write(root)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Mixin &&
            other.name == name &&
            other.root == root &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + root.hashCode()
        return result
    }

    override fun toString(): String = "Mixin(" +
            "name=$name, " +
            "root=$root" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: MixinDsl.() -> Unit): Mixin = Mixin.Deserializer {
        name = this@Mixin.name
        root = this@Mixin.root
        unknownFields = this@Mixin.unknownFields
        dsl()
    }

    class MixinDsl {
        var name: String = ""

        var root: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Mixin = Mixin(name,
            root,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Mixin>, (MixinDsl.() -> Unit) -> Mixin {
        override fun deserialize(deserializer: KtMessageDeserializer): Mixin {
            var name = ""
            var root = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Mixin(name,
                        root,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> root = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: MixinDsl.() -> Unit): Mixin = MixinDsl().apply(dsl).build()
    }
}
