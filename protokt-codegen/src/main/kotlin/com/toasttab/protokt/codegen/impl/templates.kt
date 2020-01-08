/*
 * Copyright (c) 2019. Toast Inc.
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

package com.toasttab.protokt.codegen.impl

// ADT (Algebraic Data Type) for template renderers
sealed class GroupSt(val value: String)
object Templates : GroupSt("templates.stg")
object Renderers : GroupSt("renderers.stg")
object Services : GroupSt("services.stg")
object Options : GroupSt("options.stg")

// type safe variable
interface Var { val value: String }
sealed class HeaderVar(override val value: String) : Var
sealed class MessageVar(override val value: String) : Var
sealed class EnumVar(override val value: String) : Var
sealed class OneOfVar(override val value: String) : Var
sealed class SizeOfVar(override val value: String) : Var
sealed class RenderVar(override val value: String) : Var
sealed class ServiceVar(override val value: String) : Var
sealed class MethodVar(override val value: String) : Var
sealed class OptionVar(override val value: String) : Var

sealed class TemplateSt<out T>(
    val group: GroupSt,
    val name: String,
    val vars: Set<T>
)

object ServiceSt : TemplateSt<ServiceVar>(
    Services,
    "service",
    setOf())

object MethodSt : TemplateSt<MethodVar>(
    Services,
    "method",
    setOf()
)

object PackageHeaderVar : HeaderVar("package")
object RuntimePackageHeaderVar : HeaderVar("runtimePackage")
object ExtPackageHeaderVar : HeaderVar("extPackage")
object ExtHeaderVar : HeaderVar("ext")

object HeaderSt : TemplateSt<HeaderVar>(
    Templates,
    "header",
    setOf(
        PackageHeaderVar,
        RuntimePackageHeaderVar,
        ExtPackageHeaderVar,
        ExtHeaderVar
    )
)

object MessageMessageVar : MessageVar("message")
object EntryMessageVar : MessageVar("entry")
object SerializeMessageVar : MessageVar("serialize")
object DeserializeMessageVar : MessageVar("deserialize")
object SizeofMessageVar : MessageVar("sizeof")
object InnerMessageVar : MessageVar("inner")
object ParamsMessageVar : MessageVar("params")
object OneOfsMessageVar : MessageVar("oneofs")
object OptionsMessageVar : MessageVar("options")

object MessageSt : TemplateSt<MessageVar>(
    Templates,
    "message",
    setOf(
        MessageMessageVar,
        EntryMessageVar,
        SerializeMessageVar,
        DeserializeMessageVar,
        SizeofMessageVar,
        InnerMessageVar,
        ParamsMessageVar,
        OneOfsMessageVar,
        OptionsMessageVar
    )
)

object NameEnumVar : EnumVar("name")
object MapEnumVar : EnumVar("map")
object OptionsEnumVar : EnumVar("options")
object EnumSt : TemplateSt<EnumVar>(
    Templates, "enum", setOf(NameEnumVar, MapEnumVar, OptionsEnumVar)
)

object NameOneOfVar : OneOfVar("name")
object TypesOneOfVar : OneOfVar("types")
object OptionsOneOfVar : OneOfVar("options")
object OneOfSt : TemplateSt<OneOfVar>(
    Templates, "oneof", setOf(NameOneOfVar, TypesOneOfVar, OptionsOneOfVar)
)

object NameSizeOfVar : SizeOfVar("name")
object FieldSizeOfVar : SizeOfVar("field")
object TypeSizeOfVar : SizeOfVar("type")
object FieldValueSizeOfVar : SizeOfVar("fieldSizeof")
object SizeOfRF : TemplateSt<SizeOfVar>(
    Renderers,
    "sizeof",
    setOf(NameSizeOfVar, FieldSizeOfVar, TypeSizeOfVar, FieldValueSizeOfVar)
)

object FieldSizeOfRF : TemplateSt<SizeOfVar>(
    Renderers,
    "fieldSizeof",
    setOf(NameSizeOfVar, FieldSizeOfVar)
)

object AnyRenderVar : RenderVar("any")
object TypeRenderVar : RenderVar("type")
object BuilderRenderVar : RenderVar("builder")
object NameRenderVar : RenderVar("name")
object DefRenderVar : RenderVar("def")
object FieldRenderVar : RenderVar("field")
object ReadRenderVar : RenderVar("read")
object TagRenderVar : RenderVar("tag")
object BoxRenderVar : RenderVar("box")
object OneOfRenderVar : RenderVar("oneof")
object ScopedValueRenderVar : RenderVar("scopedValue")
object LhsRenderVar : RenderVar("lhs")
object NullableRenderVar : RenderVar("nullable")

object WrapNameVar : OptionVar("wrapName")
object ArgVar : OptionVar("arg")
object TypeOptionVar : OptionVar("type")
object OneofOptionVar : OptionVar("oneof")

object ConvertTypeRF : TemplateSt<RenderVar>(
    Renderers,
    "type2nativeF",
    setOf(TypeRenderVar)
)

object ReadFunctionRF : TemplateSt<RenderVar>(
    Renderers,
    "readF",
    setOf(TypeRenderVar, BuilderRenderVar)
)

object BoxRF : TemplateSt<RenderVar>(
    Renderers,
    "boxF",
    setOf(TypeRenderVar, DefRenderVar)
)

object BoxMapRF : TemplateSt<RenderVar>(
    Renderers,
    "boxMapF",
    setOf(TypeRenderVar, BoxRenderVar)
)

object ConcatWithScopeRF : TemplateSt<RenderVar>(
    Renderers,
    "concatWithScopeF",
    setOf(ScopedValueRenderVar)
)

object TypeRF : TemplateSt<RenderVar>(
    Renderers,
    "typeF",
    setOf(FieldRenderVar, AnyRenderVar, NullableRenderVar, OneOfRenderVar)
)

object SerializeRF : TemplateSt<RenderVar>(
    Renderers,
    "serializeF",
    setOf(FieldRenderVar, NameRenderVar, TagRenderVar, BoxRenderVar)
)

object DeserializeRF : TemplateSt<RenderVar>(
    Renderers,
    "deserializeF",
    setOf(FieldRenderVar, TypeRenderVar, ReadRenderVar, LhsRenderVar)
)

object OneOfDeserializeRF : TemplateSt<RenderVar>(
    Renderers,
    "oneOfDeserializeF",
    setOf(OneOfRenderVar, NameRenderVar, ReadRenderVar)
)

object StandardRF : TemplateSt<RenderVar>(
    Renderers,
    "standardF",
    setOf(FieldRenderVar, AnyRenderVar, TypeRenderVar, NullableRenderVar)
)

object DefaultValueRF : TemplateSt<RenderVar>(
    Renderers,
    "defaultValueF",
    setOf(FieldRenderVar, TypeRenderVar, NameRenderVar)
)

object NonDefaultValueRF : TemplateSt<RenderVar>(
    Renderers,
    "nonDefaultValueF",
    setOf(FieldRenderVar, NameRenderVar)
)

object OneOfDefaultValueRF : TemplateSt<RenderVar>(
    Renderers,
    "oneofDefaultValueF",
    emptySet()
)

object WrapFieldRF : TemplateSt<OptionVar>(
    Options,
    "wrapField",
    setOf(WrapNameVar, ArgVar, TypeOptionVar, OneofOptionVar)
)

object AccessFieldRF : TemplateSt<OptionVar>(
    Options,
    "accessField",
    setOf(WrapNameVar, ArgVar)
)

object SizeofOptionRF : TemplateSt<OptionVar>(
    Options,
    "sizeof",
    setOf(ArgVar)
)

object BytesSliceRF : TemplateSt<OptionVar>(
    Options,
    "bytesSlice",
    setOf()
)

object ReadBytesSliceRF : TemplateSt<OptionVar>(
    Options,
    "readBytesSlice",
    setOf()
)

object DefaultBytesSliceRF : TemplateSt<OptionVar>(
    Options,
    "defaultBytesSlice",
    setOf()
)

data class MessageDataSt(
    val name: String,
    val doesImplement: Boolean,
    val implements: String,
    val documentation: List<String>,
    val deprecation: Deprecation.RenderOptions?,
    val suppressDeprecation: Boolean,
    val fullTypeName: String
)

data class MapEntrySt(
    val entry: Boolean,
    val kType: String,
    val vType: String
)

data class ParameterSt(
    val name: String,
    val type: String,
    val defaultValue: String,
    val messageType: String = "",
    val repeated: Boolean = false,
    val map: Boolean = false,
    val oneOf: Boolean = false,
    val nullable: Boolean = true,
    val wrapped: Boolean = false,
    val nonNullOption: Boolean,
    val overrides: Boolean = false,
    val documentation: List<String>,
    val deprecation: Deprecation.RenderOptions? = null
)

data class ScopedValueSt(
    val scope: String,
    val value: String
)

data class ConditionalSt(
    val condition: String,
    val consequent: String
)

data class AssignmentSt(
    val fieldName: String,
    val value: String,
    val long: Boolean
)

data class SizeofSt(
    val std: Boolean,
    val fieldName: String,
    val skipDefaultValue: Boolean,
    /** A singleton list for standard fields; one per type for enum fields */
    val conditionals: List<ConditionalSt>
)

data class SerializerSt(
    val std: Boolean,
    val fieldName: String,
    val skipDefaultValue: Boolean,
    /** A singleton list for standard fields; one per type for enum fields */
    val conditionals: List<ConditionalSt>
)

data class DeserializerSt(
    val std: Boolean,
    val repeated: Boolean,
    val tag: String,
    val assignment: AssignmentSt
)
