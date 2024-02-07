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

package protokt.v1.codegen.util

import com.squareup.kotlinpoet.ClassName

const val DESERIALIZER = "Deserializer"
const val BUILDER = "Builder"

internal fun GeneratorContext.className(simpleNames: List<String>) =
    ClassName(kotlinPackage, simpleNames)

fun inferClassName(className: String, pkg: String): Pair<String, List<String>> {
    val inferred = bestGuessPackageName(className)
    return if (inferred == null) {
        pkg to className.split(".")
    } else {
        inferred to className.substringAfter("$inferred.").split(".")
    }
}

private fun bestGuessPackageName(classNameString: String): String? {
    var p = 0
    while (p < classNameString.length && Character.isLowerCase(classNameString.codePointAt(p))) {
        p = classNameString.indexOf('.', p) + 1
        require(p != 0) { "couldn't make a guess for $classNameString" }
    }
    return if (p != 0) {
        classNameString.substring(0, p - 1)
    } else {
        null
    }
}
