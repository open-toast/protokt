@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/http.proto
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
 * Defines the HTTP configuration for an API service. It contains a list of
 * [HttpRule][google.api.HttpRule], each specifying the mapping of an RPC method to one or more HTTP
 * REST API methods.
 */
@KtGeneratedMessage("google.api.Http")
class Http private constructor(
    /**
     * A list of HTTP configuration rules that apply to individual API methods.
     *
     *  **NOTE:** All service configuration rules follow "last one wins" order.
     */
    val rules: List<HttpRule>,
    /**
     * When set to true, URL path parameters will be fully URI-decoded except in cases of single
     * segment matches in reserved expansion, where "%2F" will be left encoded.
     *
     *  The default behavior is to not decode RFC 6570 reserved characters in multi segment matches.
     */
    val fullyDecodeReservedExpansion: Boolean,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (rules.isNotEmpty()) {
            result += (sizeof(Tag(1)) * rules.size) + rules.sumOf { sizeof(it) }
        }
        if (fullyDecodeReservedExpansion) {
            result += sizeof(Tag(2)) + sizeof(fullyDecodeReservedExpansion)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (rules.isNotEmpty()) {
            rules.forEach { serializer.write(Tag(10)).write(it) }
        }
        if (fullyDecodeReservedExpansion) {
            serializer.write(Tag(16)).write(fullyDecodeReservedExpansion)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Http &&
        other.rules == rules &&
        other.fullyDecodeReservedExpansion == fullyDecodeReservedExpansion &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + rules.hashCode()
        result = 31 * result + fullyDecodeReservedExpansion.hashCode()
        return result
    }

    override fun toString(): String = "Http(" +
        "rules=$rules, " +
        "fullyDecodeReservedExpansion=$fullyDecodeReservedExpansion" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: HttpDsl.() -> Unit): Http = Http.Deserializer {
        rules = this@Http.rules
        fullyDecodeReservedExpansion = this@Http.fullyDecodeReservedExpansion
        unknownFields = this@Http.unknownFields
        dsl()
    }

    class HttpDsl {
        var rules: List<HttpRule> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var fullyDecodeReservedExpansion: Boolean = false

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Http = Http(finishList(rules),
        fullyDecodeReservedExpansion,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<Http>, (HttpDsl.() -> Unit) -> Http {
        override fun deserialize(deserializer: KtMessageDeserializer): Http {
            var rules : MutableList<HttpRule>? = null
            var fullyDecodeReservedExpansion = false
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Http(finishList(rules),
                            fullyDecodeReservedExpansion,
                            UnknownFieldSet.from(unknownFields))
                    10 -> rules = (rules ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readMessage(com.google.api.HttpRule))
                                   }
                               }
                    16 -> fullyDecodeReservedExpansion = deserializer.readBool()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: HttpDsl.() -> Unit): Http = HttpDsl().apply(dsl).build()
    }
}

/**
 * # gRPC Transcoding
 *
 *  gRPC Transcoding is a feature for mapping between a gRPC method and one or more HTTP REST
 * endpoints. It allows developers to build a single API service that supports both gRPC APIs and REST
 * APIs. Many systems, including [Google APIs](https://github.com/googleapis/googleapis), [Cloud
 * Endpoints](https://cloud.google.com/endpoints), [gRPC
 * Gateway](https://github.com/grpc-ecosystem/grpc-gateway), and
 * [Envoy](https://github.com/envoyproxy/envoy) proxy support this feature and use it for large scale
 * production services.
 *
 *  `HttpRule` defines the schema of the gRPC/REST mapping. The mapping specifies how different
 * portions of the gRPC request message are mapped to the URL path, URL query parameters, and HTTP
 * request body. It also controls how the gRPC response message is mapped to the HTTP response body.
 * `HttpRule` is typically specified as an `google.api.http` annotation on the gRPC method.
 *
 *  Each mapping specifies a URL path template and an HTTP method. The path template may refer to
 * one or more fields in the gRPC request message, as long as each field is a non-repeated field with a
 * primitive (non-message) type. The path template controls how fields of the request message are
 * mapped to the URL path.
 *
 *  Example:
 *
 *      service Messaging {       rpc GetMessage(GetMessageRequest) returns (Message) {
 * option (google.api.http) = {             get: "/v1/{name=messages&#47;*}"         };       }     }
 *   message GetMessageRequest {       string name = 1; // Mapped to URL path.     }     message
 * Message {       string text = 1; // The resource content.     }
 *
 *  This enables an HTTP REST to gRPC mapping as below:
 *
 *  HTTP | gRPC -----|----- `GET /v1/messages/123456`  | `GetMessage(name: "messages/123456")`
 *
 *  Any fields in the request message which are not bound by the path template automatically become
 * HTTP query parameters if there is no HTTP request body. For example:
 *
 *      service Messaging {       rpc GetMessage(GetMessageRequest) returns (Message) {
 * option (google.api.http) = {             get:"/v1/messages/{message_id}"         };       }     }
 *  message GetMessageRequest {       message SubMessage {         string subfield = 1;       }
 * string message_id = 1; // Mapped to URL path.       int64 revision = 2;    // Mapped to URL query
 * parameter `revision`.       SubMessage sub = 3;    // Mapped to URL query parameter `sub.subfield`.
 *    }
 *
 *  This enables a HTTP JSON to RPC mapping as below:
 *
 *  HTTP | gRPC -----|----- `GET /v1/messages/123456?revision=2&sub.subfield=foo` |
 * `GetMessage(message_id: "123456" revision: 2 sub: SubMessage(subfield: "foo"))`
 *
 *  Note that fields which are mapped to URL query parameters must have a primitive type or a
 * repeated primitive type or a non-repeated message type. In the case of a repeated type, the
 * parameter can be repeated in the URL as `...?param=A&param=B`. In the case of a message type, each
 * field of the message is mapped to a separate parameter, such as `...?foo.a=A&foo.b=B&foo.c=C`.
 *
 *  For HTTP methods that allow a request body, the `body` field specifies the mapping. Consider a
 * REST update method on the message resource collection:
 *
 *      service Messaging {       rpc UpdateMessage(UpdateMessageRequest) returns (Message) {
 *  option (google.api.http) = {           patch: "/v1/messages/{message_id}"           body: "message"
 *         };       }     }     message UpdateMessageRequest {       string message_id = 1; // mapped
 * to the URL       Message message = 2;   // mapped to the body     }
 *
 *  The following HTTP JSON to RPC mapping is enabled, where the representation of the JSON in the
 * request body is determined by protos JSON encoding:
 *
 *  HTTP | gRPC -----|----- `PATCH /v1/messages/123456 { "text": "Hi!" }` |
 * `UpdateMessage(message_id: "123456" message { text: "Hi!" })`
 *
 *  The special name `*` can be used in the body mapping to define that every field not bound by the
 * path template should be mapped to the request body.  This enables the following alternative
 * definition of the update method:
 *
 *      service Messaging {       rpc UpdateMessage(Message) returns (Message) {         option
 * (google.api.http) = {           patch: "/v1/messages/{message_id}"           body: "*"         };
 *    }     }     message Message {       string message_id = 1;       string text = 2;     }
 *
 *
 *
 *  The following HTTP JSON to RPC mapping is enabled:
 *
 *  HTTP | gRPC -----|----- `PATCH /v1/messages/123456 { "text": "Hi!" }` |
 * `UpdateMessage(message_id: "123456" text: "Hi!")`
 *
 *  Note that when using `*` in the body mapping, it is not possible to have HTTP parameters, as all
 * fields not bound by the path end in the body. This makes this option more rarely used in practice
 * when defining REST APIs. The common usage of `*` is in custom methods which don't use the URL at all
 * for transferring data.
 *
 *  It is possible to define multiple HTTP methods for one RPC by using the `additional_bindings`
 * option. Example:
 *
 *      service Messaging {       rpc GetMessage(GetMessageRequest) returns (Message) {
 * option (google.api.http) = {           get: "/v1/messages/{message_id}"
 * additional_bindings {             get: "/v1/users/{user_id}/messages/{message_id}"           }
 *   };       }     }     message GetMessageRequest {       string message_id = 1;       string user_id
 * = 2;     }
 *
 *  This enables the following two alternative HTTP JSON to RPC mappings:
 *
 *  HTTP | gRPC -----|----- `GET /v1/messages/123456` | `GetMessage(message_id: "123456")` `GET
 * /v1/users/me/messages/123456` | `GetMessage(user_id: "me" message_id: "123456")`
 *
 *  ## Rules for HTTP mapping
 *
 *  1. Leaf request fields (recursive expansion nested messages in the request    message) are
 * classified into three categories:    - Fields referred by the path template. They are passed via the
 * URL path.    - Fields referred by the [HttpRule.body][google.api.HttpRule.body]. They are passed via
 * the HTTP      request body.    - All other fields are passed via the URL query parameters, and the
 *    parameter name is the field path in the request message. A repeated      field can be represented
 * as multiple query parameters under the same      name.  2. If
 * [HttpRule.body][google.api.HttpRule.body] is "*", there is no URL query parameter, all fields
 * are passed via URL path and HTTP request body.  3. If [HttpRule.body][google.api.HttpRule.body] is
 * omitted, there is no HTTP request body, all     fields are passed via URL path and URL query
 * parameters.
 *
 *  ### Path template syntax
 *
 *      Template = "/" Segments [ Verb ] ;     Segments = Segment { "/" Segment } ;     Segment  =
 * "*" | "**" | LITERAL | Variable ;     Variable = "{" FieldPath [ "=" Segments ] "}" ;     FieldPath
 * = IDENT { "." IDENT } ;     Verb     = ":" LITERAL ;
 *
 *  The syntax `*` matches a single URL path segment. The syntax `**` matches zero or more URL path
 * segments, which must be the last part of the URL path except the `Verb`.
 *
 *  The syntax `Variable` matches part of the URL path as specified by its template. A variable
 * template must not contain other variables. If a variable matches a single path segment, its template
 * may be omitted, e.g. `{var}` is equivalent to `{var=*}`.
 *
 *  The syntax `LITERAL` matches literal text in the URL path. If the `LITERAL` contains any
 * reserved character, such characters should be percent-encoded before the matching.
 *
 *  If a variable contains exactly one path segment, such as `"{var}"` or `"{var=*}"`, when such a
 * variable is expanded into a URL path on the client side, all characters except `[-_.~0-9a-zA-Z]` are
 * percent-encoded. The server side does the reverse decoding. Such variables show up in the [Discovery
 * Document](https://developers.google.com/discovery/v1/reference/apis) as `{var}`.
 *
 *  If a variable contains multiple path segments, such as `"{var=foo&#47;*}"` or `"{var=**}"`, when
 * such a variable is expanded into a URL path on the client side, all characters except
 * `[-_.~/0-9a-zA-Z]` are percent-encoded. The server side does the reverse decoding, except "%2F" and
 * "%2f" are left unchanged. Such variables show up in the [Discovery
 * Document](https://developers.google.com/discovery/v1/reference/apis) as `{+var}`.
 *
 *  ## Using gRPC API Service Configuration
 *
 *  gRPC API Service Configuration (service config) is a configuration language for configuring a
 * gRPC service to become a user-facing product. The service config is simply the YAML representation
 * of the `google.api.Service` proto message.
 *
 *  As an alternative to annotating your proto file, you can configure gRPC transcoding in your
 * service config YAML files. You do this by specifying a `HttpRule` that maps the gRPC method to a
 * REST endpoint, achieving the same effect as the proto annotation. This can be particularly useful if
 * you have a proto that is reused in multiple services. Note that any transcoding specified in the
 * service config will override any matching transcoding configuration in the proto.
 *
 *  Example:
 *
 *      http:       rules:         # Selects a gRPC method and applies HttpRule to it.         -
 * selector: example.v1.Messaging.GetMessage           get: /v1/messages/{message_id}/{sub.subfield}
 *
 *  ## Special notes
 *
 *  When gRPC Transcoding is used to map a gRPC to JSON REST endpoints, the proto to JSON conversion
 * must follow the [proto3
 * specification](https://developers.google.com/protocol-buffers/docs/proto3#json).
 *
 *  While the single segment variable follows the semantics of [RFC
 * 6570](https://tools.ietf.org/html/rfc6570) Section 3.2.2 Simple String Expansion, the multi segment
 * variable **does not** follow RFC 6570 Section 3.2.3 Reserved Expansion. The reason is that the
 * Reserved Expansion does not expand special characters like `?` and `#`, which would lead to invalid
 * URLs. As the result, gRPC Transcoding uses a custom encoding for multi segment variables.
 *
 *  The path variables **must not** refer to any repeated or mapped field, because client libraries
 * are not capable of handling such variable expansion.
 *
 *  The path variables **must not** capture the leading "/" character. The reason is that the most
 * common use case "{var}" does not capture the leading "/" character. For consistency, all path
 * variables must share the same behavior.
 *
 *  Repeated message fields must not be mapped to URL query parameters, because no client library
 * can support such complicated mapping.
 *
 *  If an API needs to use a JSON array for request or response body, it can map the request or
 * response body to a repeated field. However, some gRPC Transcoding implementations may not support
 * this feature.
 */
@KtGeneratedMessage("google.api.HttpRule")
class HttpRule private constructor(
    /**
     * Selects a method to which this rule applies.
     *
     *  Refer to [selector][google.api.DocumentationRule.selector] for syntax details.
     */
    val selector: String,
    /**
     * Determines the URL pattern is matched by this rules. This pattern can be used with any of the
     * {get|put|post|delete|patch} methods. A custom method can be defined using the 'custom' field.
     */
    val pattern: Pattern?,
    /**
     * The name of the request field whose value is mapped to the HTTP request body, or `*` for
     * mapping all request fields not captured by the path pattern to the HTTP body, or omitted for not
     * having any HTTP request body.
     *
     *  NOTE: the referred field must be present at the top-level of the request message type.
     */
    val body: String,
    /**
     * Additional HTTP bindings for the selector. Nested bindings must not contain an
     * `additional_bindings` field themselves (that is, the nesting may only be one level deep).
     */
    val additionalBindings: List<HttpRule>,
    /**
     * Optional. The name of the response field whose value is mapped to the HTTP response body.
     * When omitted, the entire response message will be used as the HTTP response body.
     *
     *  NOTE: The referred field must be present at the top-level of the response message type.
     */
    val responseBody: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (selector.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(selector)
        }
        when (pattern) {
            is HttpRule.Pattern.Get -> {
                result += sizeof(Tag(2)) + sizeof(pattern.get)}
            is HttpRule.Pattern.Put -> {
                result += sizeof(Tag(3)) + sizeof(pattern.put)}
            is HttpRule.Pattern.Post -> {
                result += sizeof(Tag(4)) + sizeof(pattern.post)}
            is HttpRule.Pattern.Delete -> {
                result += sizeof(Tag(5)) + sizeof(pattern.delete)}
            is HttpRule.Pattern.Patch -> {
                result += sizeof(Tag(6)) + sizeof(pattern.patch)}
            is HttpRule.Pattern.Custom -> {
                result += sizeof(Tag(8)) + sizeof(pattern.custom)}
            null -> Unit
        }
        if (body.isNotEmpty()) {
            result += sizeof(Tag(7)) + sizeof(body)
        }
        if (additionalBindings.isNotEmpty()) {
            result += (sizeof(Tag(11)) * additionalBindings.size) + additionalBindings.sumOf {
                    sizeof(it) }
        }
        if (responseBody.isNotEmpty()) {
            result += sizeof(Tag(12)) + sizeof(responseBody)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (selector.isNotEmpty()) {
            serializer.write(Tag(10)).write(selector)
        }
        when (pattern) {
            is HttpRule.Pattern.Get -> {
                serializer.write(Tag(18)).write(pattern.get)
            }
            is HttpRule.Pattern.Put -> {
                serializer.write(Tag(26)).write(pattern.put)
            }
            is HttpRule.Pattern.Post -> {
                serializer.write(Tag(34)).write(pattern.post)
            }
            is HttpRule.Pattern.Delete -> {
                serializer.write(Tag(42)).write(pattern.delete)
            }
            is HttpRule.Pattern.Patch -> {
                serializer.write(Tag(50)).write(pattern.patch)
            }
            is HttpRule.Pattern.Custom -> {
                serializer.write(Tag(66)).write(pattern.custom)
            }
            null -> Unit
        }
        if (body.isNotEmpty()) {
            serializer.write(Tag(58)).write(body)
        }
        if (additionalBindings.isNotEmpty()) {
            additionalBindings.forEach { serializer.write(Tag(90)).write(it) }
        }
        if (responseBody.isNotEmpty()) {
            serializer.write(Tag(98)).write(responseBody)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is HttpRule &&
        other.selector == selector &&
        other.pattern == pattern &&
        other.body == body &&
        other.additionalBindings == additionalBindings &&
        other.responseBody == responseBody &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + selector.hashCode()
        result = 31 * result + pattern.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + additionalBindings.hashCode()
        result = 31 * result + responseBody.hashCode()
        return result
    }

    override fun toString(): String = "HttpRule(" +
        "selector=$selector, " +
        "pattern=$pattern, " +
        "body=$body, " +
        "additionalBindings=$additionalBindings, " +
        "responseBody=$responseBody" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: HttpRuleDsl.() -> Unit): HttpRule = HttpRule.Deserializer {
        selector = this@HttpRule.selector
        pattern = this@HttpRule.pattern
        body = this@HttpRule.body
        additionalBindings = this@HttpRule.additionalBindings
        responseBody = this@HttpRule.responseBody
        unknownFields = this@HttpRule.unknownFields
        dsl()
    }

    sealed class Pattern {
        /**
         * Maps to HTTP GET. Used for listing and getting information about resources.
         */
        data class Get(
            val `get`: String,
        ) : Pattern()

        /**
         * Maps to HTTP PUT. Used for replacing a resource.
         */
        data class Put(
            val put: String,
        ) : Pattern()

        /**
         * Maps to HTTP POST. Used for creating a resource or performing an action.
         */
        data class Post(
            val post: String,
        ) : Pattern()

        /**
         * Maps to HTTP DELETE. Used for deleting a resource.
         */
        data class Delete(
            val delete: String,
        ) : Pattern()

        /**
         * Maps to HTTP PATCH. Used for updating a resource.
         */
        data class Patch(
            val patch: String,
        ) : Pattern()

        /**
         * The custom pattern is used for specifying an HTTP method that is not included in the
         * `pattern` field, such as HEAD, or "*" to leave the HTTP method unspecified for this rule.
         * The wild-card rule is useful for services that provide content to Web (HTML) clients.
         */
        data class Custom(
            val custom: CustomHttpPattern,
        ) : Pattern()
    }

    class HttpRuleDsl {
        var selector: String = ""

        var pattern: Pattern? = null

        var body: String = ""

        var additionalBindings: List<HttpRule> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var responseBody: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): HttpRule = HttpRule(selector,
        pattern,
        body,
        finishList(additionalBindings),
        responseBody,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<HttpRule>,
            (HttpRuleDsl.() -> Unit) -> HttpRule {
        override fun deserialize(deserializer: KtMessageDeserializer): HttpRule {
            var selector = ""
            var pattern : Pattern? = null
            var body = ""
            var additionalBindings : MutableList<HttpRule>? = null
            var responseBody = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return HttpRule(selector,
                            pattern,
                            body,
                            finishList(additionalBindings),
                            responseBody,
                            UnknownFieldSet.from(unknownFields))
                    10 -> selector = deserializer.readString()
                    18 -> pattern = Pattern.Get(deserializer.readString())
                    26 -> pattern = Pattern.Put(deserializer.readString())
                    34 -> pattern = Pattern.Post(deserializer.readString())
                    42 -> pattern = Pattern.Delete(deserializer.readString())
                    50 -> pattern = Pattern.Patch(deserializer.readString())
                    58 -> body = deserializer.readString()
                    66 -> pattern =
                            Pattern.Custom(deserializer.readMessage(com.google.api.CustomHttpPattern))
                    90 -> additionalBindings = (additionalBindings ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readMessage(com.google.api.HttpRule))
                                   }
                               }
                    98 -> responseBody = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: HttpRuleDsl.() -> Unit): HttpRule =
                HttpRuleDsl().apply(dsl).build()
    }
}

/**
 * A custom pattern is used for defining custom HTTP verb.
 */
@KtGeneratedMessage("google.api.CustomHttpPattern")
class CustomHttpPattern private constructor(
    /**
     * The name of this custom HTTP verb.
     */
    val kind: String,
    /**
     * The path matched by this custom verb.
     */
    val path: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (kind.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(kind)
        }
        if (path.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(path)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (kind.isNotEmpty()) {
            serializer.write(Tag(10)).write(kind)
        }
        if (path.isNotEmpty()) {
            serializer.write(Tag(18)).write(path)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is CustomHttpPattern &&
        other.kind == kind &&
        other.path == path &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    override fun toString(): String = "CustomHttpPattern(" +
        "kind=$kind, " +
        "path=$path" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: CustomHttpPatternDsl.() -> Unit): CustomHttpPattern =
            CustomHttpPattern.Deserializer {
        kind = this@CustomHttpPattern.kind
        path = this@CustomHttpPattern.path
        unknownFields = this@CustomHttpPattern.unknownFields
        dsl()
    }

    class CustomHttpPatternDsl {
        var kind: String = ""

        var path: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): CustomHttpPattern = CustomHttpPattern(kind,
        path,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<CustomHttpPattern>,
            (CustomHttpPatternDsl.() -> Unit) -> CustomHttpPattern {
        override fun deserialize(deserializer: KtMessageDeserializer): CustomHttpPattern {
            var kind = ""
            var path = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return CustomHttpPattern(kind,
                            path,
                            UnknownFieldSet.from(unknownFields))
                    10 -> kind = deserializer.readString()
                    18 -> path = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: CustomHttpPatternDsl.() -> Unit): CustomHttpPattern =
                CustomHttpPatternDsl().apply(dsl).build()
    }
}
