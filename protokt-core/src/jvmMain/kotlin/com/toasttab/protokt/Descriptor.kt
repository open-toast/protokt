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

package com.toasttab.protokt

@Deprecated("use v1")
object DescriptorProtos {
    val descriptor: FileDescriptor by lazy {
        val descriptorData = arrayOf(
            "\n google/protobuf/descriptor.protogoog" +
                "le.protobuf\"G\nFileDescriptorSet2\nfile" +
                " (2\$.google.protobuf.FileDescriptorP" +
                "roto\"ￛ\nFileDescriptorProto\nname " +
                "(\t\npackage (\t\n\ndependency (\t" +
                "\npublic_dependency\n (\nweak_depen" +
                "dency (6\nmessage_type (2 .goog" +
                "le.protobuf.DescriptorProto7\n\tenum_type" +
                " (2\$.google.protobuf.EnumDescriptorP" +
                "roto8\nservice (2\'.google.protobuf." +
                "ServiceDescriptorProto8\n\textension (" +
                "2%.google.protobuf.FieldDescriptorProto" +
                "-\noptions\b (2.google.protobuf.File" +
                "Options9\nsource_code_info\t (2.goog" +
                "le.protobuf.SourceCodeInfo\nsyntax " +
                "(\t\"ﾩ\nDescriptorProto\nname (\t4\n" +
                "field (2%.google.protobuf.FieldDescr" +
                "iptorProto8\n\textension (2%.google.p" +
                "rotobuf.FieldDescriptorProto5\nnested_t" +
                "ype (2 .google.protobuf.DescriptorPr" +
                "oto7\n\tenum_type (2\$.google.protobuf" +
                ".EnumDescriptorProtoH\nextension_range" +
                " (2/.google.protobuf.DescriptorProto." +
                "ExtensionRange9\n\noneof_decl\b (2%.goo" +
                "gle.protobuf.OneofDescriptorProto0\nopt" +
                "ions (2.google.protobuf.MessageOpti" +
                "onsF\nreserved_range\t (2..google.pro" +
                "tobuf.DescriptorProto.ReservedRange\n\rr" +
                "eserved_name\n (\te\nExtensionRange\r\n" +
                "start (\nend (7\noptions (" +
                "2&.google.protobuf.ExtensionRangeOption" +
                "s+\n\rReservedRange\r\nstart (\nend" +
                " (\"g\nExtensionRangeOptionsC\nunint" +
                "erpreted_option￧ (2\$.google.protobuf" +
                ".UninterpretedOption*\t\b￨ﾀﾀﾀﾀ\"ￕ\nFiel" +
                "dDescriptorProto\nname (\t\nnumber" +
                " (:\nlabel (2+.google.protobuf." +
                "FieldDescriptorProto.Label8\ntype (" +
                "2*.google.protobuf.FieldDescriptorProto." +
                "Type\n\ttype_name (\t\n\bextendee (" +
                "\t\n\rdefault_value (\t\noneof_index" +
                "\t (\n\tjson_name\n (\t.\noptions\b (" +
                "2.google.protobuf.FieldOptions\nprot" +
                "o3_optional (\b\"ﾶ\nType\nTYPE_DOUBL" +
                "E\n\nTYPE_FLOAT\n\nTYPE_INT64\nT" +
                "YPE_UINT64\n\nTYPE_INT32\nTYPE_FIX" +
                "ED64\nTYPE_FIXED32\r\n\tTYPE_BOOL\b" +
                "\nTYPE_STRING\t\n\nTYPE_GROUP\n\nTYPE" +
                "_MESSAGE\n\nTYPE_BYTES\nTYPE_UINT3" +
                "2\r\r\n\tTYPE_ENUM\n\rTYPE_SFIXED32\n" +
                "\rTYPE_SFIXED64\nTYPE_SINT32\nTYP" +
                "E_SINT64\"C\nLabel\nLABEL_OPTIONAL" +
                "\nLABEL_REQUIRED\nLABEL_REPEATED\"" +
                "T\nOneofDescriptorProto\nname (\t.\n" +
                "options (2.google.protobuf.OneofOp" +
                "tions\"ﾤ\nEnumDescriptorProto\nname " +
                "(\t8\nvalue (2).google.protobuf.Enu" +
                "mValueDescriptorProto-\noptions (2" +
                ".google.protobuf.EnumOptionsN\nreserved" +
                "_range (26.google.protobuf.EnumDescr" +
                "iptorProto.EnumReservedRange\n\rreserved" +
                "_name (\t/\nEnumReservedRange\r\nstar" +
                "t (\nend (\"l\nEnumValueDescrip" +
                "torProto\nname (\t\nnumber (2" +
                "\noptions (2!.google.protobuf.EnumVa" +
                "lueOptions\"ﾐ\nServiceDescriptorProto\n" +
                "name (\t6\nmethod (2&.google.pro" +
                "tobuf.MethodDescriptorProto0\noptions" +
                " (2.google.protobuf.ServiceOptions\"￁" +
                "\nMethodDescriptorProto\nname (\t\n" +
                "\ninput_type (\t\noutput_type (\t/" +
                "\noptions (2.google.protobuf.Method" +
                "Options\nclient_streaming (\b:false" +
                "\nserver_streaming (\b:false\"ﾥ\nFi" +
                "leOptions\njava_package (\t\njava_" +
                "outer_classname\b (\t\"\njava_multiple_f" +
                "iles\n (\b:false)\njava_generate_equal" +
                "s_and_hash (\bB%\njava_string_chec" +
                "k_utf8 (\b:falseF\noptimize_for\t (" +
                "2).google.protobuf.FileOptions.Optimize" +
                "Mode:SPEED\n\ngo_package (\t\"\ncc_ge" +
                "neric_services (\b:false\$\njava_gene" +
                "ric_services (\b:false\"\npy_generic_" +
                "services (\b:false#\nphp_generic_ser" +
                "vices* (\b:false\n\ndeprecated (\b:" +
                "false\ncc_enable_arenas (\b:true\n" +
                "objc_class_prefix\$ (\t\ncsharp_names" +
                "pace% (\t\nswift_prefix\' (\t\nphp_" +
                "class_prefix( (\t\n\rphp_namespace) (" +
                "\t\nphp_metadata_namespace, (\t\nrub" +
                "y_package- (\tC\nuninterpreted_option" +
                "￧ (2\$.google.protobuf.UninterpretedOp" +
                "tion\":\nOptimizeMode\t\nSPEED\r\n\tCODE_" +
                "SIZE\nLITE_RUNTIME*\t\b￨ﾀﾀﾀﾀJ\b&" +
                "\'\"ﾄ\nMessageOptions&\nmessage_set_wire" +
                "_format (\b:false.\nno_standard_desc" +
                "riptor_accessor (\b:false\n\ndeprecat" +
                "ed (\b:false\n\tmap_entry (\bC\nun" +
                "interpreted_option￧ (2\$.google.proto" +
                "buf.UninterpretedOption*\t\b￨ﾀﾀﾀﾀJ\b" +
                "J\bJ\bJ\b\b\tJ\b\t\n\"ﾾ\nFieldOption" +
                "s:\nctype (2#.google.protobuf.Field" +
                "Options.CType:STRING\npacked (\b?\n" +
                "jstype (2\$.google.protobuf.FieldOpt" +
                "ions.JSType:\tJS_NORMAL\nlazy (\b:fa" +
                "lse\nunverified_lazy (\b:false\n\nd" +
                "eprecated (\b:false\nweak\n (\b:fa" +
                "lseC\nuninterpreted_option￧ (2\$.goo" +
                "gle.protobuf.UninterpretedOption\"/\nCTyp" +
                "e\n\nSTRING \b\nCORD\nSTRING_PIECE" +
                "\"5\nJSType\r\n\tJS_NORMAL \r\n\tJS_STRING" +
                "\r\n\tJS_NUMBER*\t\b￨ﾀﾀﾀﾀJ\b\"^\nOne" +
                "ofOptionsC\nuninterpreted_option￧ (" +
                "2\$.google.protobuf.UninterpretedOption*\t" +
                "\b￨ﾀﾀﾀﾀ\"ﾓ\nEnumOptions\nallow_alias" +
                " (\b\n\ndeprecated (\b:falseC\nuni" +
                "nterpreted_option￧ (2\$.google.protob" +
                "uf.UninterpretedOption*\t\b￨ﾀﾀﾀﾀJ\b\"" +
                "}\nEnumValueOptions\n\ndeprecated (\b:" +
                "falseC\nuninterpreted_option￧ (2\$." +
                "google.protobuf.UninterpretedOption*\t\b￨" +
                "ﾀﾀﾀﾀ\"{\nServiceOptions\n\ndeprecated!" +
                " (\b:falseC\nuninterpreted_option￧ " +
                "(2\$.google.protobuf.UninterpretedOption" +
                "*\t\b￨ﾀﾀﾀﾀ\"ﾭ\n\rMethodOptions\n\ndepreca" +
                "ted! (\b:false_\nidempotency_level\" " +
                "(2/.google.protobuf.MethodOptions.Idem" +
                "potencyLevel:IDEMPOTENCY_UNKNOWNC\nuni" +
                "nterpreted_option￧ (2\$.google.protob" +
                "uf.UninterpretedOption\"P\nIdempotencyLev" +
                "el\nIDEMPOTENCY_UNKNOWN \nNO_SIDE_E" +
                "FFECTS\n\nIDEMPOTENT*\t\b￨ﾀﾀﾀﾀ\"ﾞ\n" +
                "UninterpretedOption;\nname (2-.goog" +
                "le.protobuf.UninterpretedOption.NamePart" +
                "\nidentifier_value (\t\npositive_i" +
                "nt_value (\nnegative_int_value " +
                "(\ndouble_value (\nstring_value" +
                " (\naggregate_value\b (\t3\n\bNameP" +
                "art\n\tname_part (\t\nis_extension" +
                " (\b\"ￕ\nSourceCodeInfo:\n\blocation (" +
                "2(.google.protobuf.SourceCodeInfo.Locat" +
                "ionﾆ\n\bLocation\npath (B\nsp" +
                "an (B\nleading_comments (\t" +
                "\ntrailing_comments (\t!\nleading_det" +
                "ached_comments (\t\"ﾧ\nGeneratedCodeIn" +
                "foA\n\nannotation (2-.google.protobuf" +
                ".GeneratedCodeInfo.AnnotationO\n\nAnnotat" +
                "ion\npath (B\nsource_file " +
                "(\t\r\nbegin (\nend (B~\ncom.go" +
                "ogle.protobufBDescriptorProtosHZ-googl" +
                "e.golang.org/protobuf/types/descriptorpb" +
                "￸ﾢGPBﾪGoogle.Protobuf.Reflection"
        )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf()
        )
    }
}

@Deprecated("use v1")
val FieldDescriptorProto.Type.Deserializer.descriptor: EnumDescriptor
    get() = FieldDescriptorProto.descriptor.enumTypes[0]

@Deprecated("use v1")
val FieldDescriptorProto.Label.Deserializer.descriptor: EnumDescriptor
    get() = FieldDescriptorProto.descriptor.enumTypes[1]

@Deprecated("use v1")
val FileOptions.OptimizeMode.Deserializer.descriptor: EnumDescriptor
    get() = FileOptions.descriptor.enumTypes[0]

@Deprecated("use v1")
val FieldOptions.CType.Deserializer.descriptor: EnumDescriptor
    get() = FieldOptions.descriptor.enumTypes[0]

@Deprecated("use v1")
val FieldOptions.JSType.Deserializer.descriptor: EnumDescriptor
    get() = FieldOptions.descriptor.enumTypes[1]

@Deprecated("use v1")
val MethodOptions.IdempotencyLevel.Deserializer.descriptor: EnumDescriptor
    get() = MethodOptions.descriptor.enumTypes[0]

@Deprecated("use v1")
val FileDescriptorSet.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[0]

@Deprecated("use v1")
val FileDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[1]

@Deprecated("use v1")
val DescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[2]

@Deprecated("use v1")
val DescriptorProto.ExtensionRange.Deserializer.descriptor: Descriptor
    get() = DescriptorProto.descriptor.nestedTypes[0]

@Deprecated("use v1")
val DescriptorProto.ReservedRange.Deserializer.descriptor: Descriptor
    get() = DescriptorProto.descriptor.nestedTypes[1]

@Deprecated("use v1")
val ExtensionRangeOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[3]

@Deprecated("use v1")
val FieldDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[4]

@Deprecated("use v1")
val OneofDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[5]

@Deprecated("use v1")
val EnumDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[6]

@Deprecated("use v1")
val EnumDescriptorProto.EnumReservedRange.Deserializer.descriptor: Descriptor
    get() = EnumDescriptorProto.descriptor.nestedTypes[0]

@Deprecated("use v1")
val EnumValueDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[7]

@Deprecated("use v1")
val ServiceDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[8]

@Deprecated("use v1")
val MethodDescriptorProto.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[9]

@Deprecated("use v1")
val FileOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[10]

@Deprecated("use v1")
val MessageOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[11]

@Deprecated("use v1")
val FieldOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[12]

@Deprecated("use v1")
val OneofOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[13]

@Deprecated("use v1")
val EnumOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[14]

@Deprecated("use v1")
val EnumValueOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[15]

@Deprecated("use v1")
val ServiceOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[16]

@Deprecated("use v1")
val MethodOptions.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[17]

@Deprecated("use v1")
val UninterpretedOption.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[18]

@Deprecated("use v1")
val UninterpretedOption.NamePart.Deserializer.descriptor: Descriptor
    get() = UninterpretedOption.descriptor.nestedTypes[0]

@Deprecated("use v1")
val SourceCodeInfo.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[19]

@Deprecated("use v1")
val SourceCodeInfo.Location.Deserializer.descriptor: Descriptor
    get() = SourceCodeInfo.descriptor.nestedTypes[0]

@Deprecated("use v1")
val GeneratedCodeInfo.Deserializer.descriptor: Descriptor
    get() = DescriptorProtos.descriptor.messageTypes[20]

@Deprecated("use v1")
val GeneratedCodeInfo.Annotation.Deserializer.descriptor: Descriptor
    get() = GeneratedCodeInfo.descriptor.nestedTypes[0]
