/*
 * Copyright (c) 2019 Toast, Inc.
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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

object Deprecation {
    fun renderOptions(message: String) =
        RenderOptions(message.ifBlank { null }?.bindSpaces())

    class RenderOptions(
        val message: String?
    )

    fun PropertySpec.Builder.handleDeprecation(renderOptions: RenderOptions?) =
        apply {
            if (renderOptions != null) {
                addAnnotation(
                    AnnotationSpec.builder(Deprecated::class)
                        .handleDeprecationMessage(renderOptions.message.orEmpty())
                        .build()
                )
            }
        }

    fun TypeSpec.Builder.handleDeprecation(deprecated: Boolean, message: String) {
        if (deprecated) {
            addAnnotation(
                AnnotationSpec.builder(Deprecated::class)
                    .handleDeprecationMessage(message)
                    .build()
            )
        }
    }

    private fun AnnotationSpec.Builder.handleDeprecationMessage(message: String) =
        apply {
            if (message.isNotEmpty()) {
                addMember(message.embed())
            } else {
                addMember("deprecated in proto".embed())
            }
        }

    fun FileSpec.Builder.addDeprecationSuppression() {
        addAnnotation(deprecationSuppression())
    }

    private fun deprecationSuppression() =
        AnnotationSpec.builder(Suppress::class)
            .addMember("DEPRECATION".embed())
            .build()
}
