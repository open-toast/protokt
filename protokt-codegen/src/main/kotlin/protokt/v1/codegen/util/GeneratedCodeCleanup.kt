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

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.pinterest.ktlint.ruleset.standard.rules.NO_UNIT_RETURN_RULE_ID
import com.pinterest.ktlint.ruleset.standard.rules.TrailingCommaOnCallSiteRule.Companion.TRAILING_COMMA_ON_CALL_SITE_PROPERTY
import com.pinterest.ktlint.ruleset.standard.rules.TrailingCommaOnDeclarationSiteRule.Companion.TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

fun tidy(code: String, context: GeneratorContext) =
    if (context.formatOutput) {
        try {
            format(code)
        } catch (t: Throwable) {
            logger.error { "Failed to format generated code; try disabling code formatting." }
            throw t
        }
    } else {
        code
    }

private fun format(code: String) =
    KtLintRuleEngine(
        ruleProviders(),
        editorConfigOverride = EditorConfigOverride.from(
            INDENT_SIZE_PROPERTY to 2,
            TRAILING_COMMA_ON_CALL_SITE_PROPERTY to false,
            TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false
        )
    ).format(Code.fromSnippet(code))

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
        .filterNot { it.ruleId == NO_UNIT_RETURN_RULE_ID }
        .toSet()
