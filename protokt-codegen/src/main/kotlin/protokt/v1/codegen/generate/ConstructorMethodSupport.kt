/*
 * Copyright (c) 2022 Toast, Inc.
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

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.codegen.generate.Deprecation.handleDeprecation
import protokt.v1.codegen.util.Message

fun addConstructorFunction(msg: Message, addFunction: (FunSpec) -> Unit) {
    addFunction(
        FunSpec.builder(msg.className.simpleName)
            .returns(msg.className)
            .addParameter(
                "dsl",
                LambdaTypeName.get(
                    msg.dslClassName,
                    emptyList(),
                    Unit::class.asTypeName()
                )
            )
            .addStatement("return %T().apply(dsl).build()", msg.dslClassName)
            .handleDeprecation(msg)
            .build()
    )
}
