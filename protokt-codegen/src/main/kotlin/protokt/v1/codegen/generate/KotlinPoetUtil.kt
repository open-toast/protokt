/*
 * Copyright (c) 2021 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.util.protoktV1
import kotlin.reflect.KClass

fun String.embed() =
    "\"" + this + "\""

fun String.bindSpaces() =
    replace(" ", "·")

fun constructorProperty(name: String, type: KClass<*>, override: Boolean = false) =
    PropertySpec.builder(name, type).apply {
        initializer(name)
        if (override) {
            addModifiers(KModifier.OVERRIDE)
        }
    }.build()

fun constructorProperty(name: String, type: TypeName, override: Boolean = false) =
    PropertySpec.builder(name, type).apply {
        initializer(name)
        if (override) {
            addModifiers(KModifier.OVERRIDE)
        }
    }.build()

fun buildFunSpec(name: String, funSpecBuilder: FunSpec.Builder.() -> Unit): FunSpec {
    return FunSpec.builder(name).apply(funSpecBuilder).build()
}

fun namedCodeBlock(format: String, arguments: Map<String, *>) =
    CodeBlock.builder().addNamed(format, arguments).build()

fun runtimeFunction(name: String) = MemberName(protoktV1, name)

fun CodeBlock.Builder.endControlFlowWithoutNewline() {
    unindent()
    add("}")
}

fun inferClassName(className: String, ctx: Context) =
    inferClassName(className, ctx.info.kotlinPackage)

fun inferClassName(className: String, pkg: String): ClassName {
    val inferred = ClassName.bestGuess(className)
    return if (inferred.packageName == "") {
        ClassName(pkg, className.split("."))
    } else {
        inferred
    }
}
