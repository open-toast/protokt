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

package com.toasttab.protokt.v1.codegen.util

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.NoUnitReturnRule
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider

fun tidy(rawCode: String, context: GeneratorContext): String {
    var code = stripApiMode(rawCode)
    if (context.formatOutput) {
        code = format(code)
    }
    return code
}

// strips Explicit API mode declarations
// https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
private fun stripApiMode(code: String) =
    code
        // https://stackoverflow.com/a/64970734
        .replace("public class ", "class ")
        .replace("public abstract ", "abstract ")
        .replace("public open ", "open ")
        .replace("public final ", "final ")
        .replace("public const ", "final ")
        .replace("public val ", "val ")
        .replace("public var ", "var ")
        .replace("public fun ", "fun ")
        .replace("public object ", "object ")
        .replace("public companion ", "companion ")
        .replace("public override ", "override ")
        .replace("public sealed ", "sealed ")
        .replace("public data ", "data ")

private fun format(code: String) =
    KtLint.format(
        KtLint.ExperimentalParams(
            text = code,
            ruleProviders = ruleProviders(),
            cb = { _, _ -> }
        )
    )

private fun ruleProviders() =
    StandardRuleSetProvider()
        .getRuleProviders()
        // If the generated class' name is Unit then the deserializer must
        // explicitly return Unit, and kotlinpoet will not qualify the name
        // since it is contained within the Unit class definition.
        //
        // This could be avoided if the deserializer is moved out of the
        // companion object into a private top-level function, but is required
        // in strict API mode.
        .filterNot { it.createNewRuleInstance().id == NoUnitReturnRule().id }
        .toSet()
