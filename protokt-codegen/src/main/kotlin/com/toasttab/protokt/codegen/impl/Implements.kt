/*
 * Copyright (c) 2020 Toast Inc.
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

import arrow.syntax.function.memoize
import com.toasttab.protokt.codegen.MessageType
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.ClassLookup.getClass
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.possiblyQualify

internal object Implements {
    fun StandardField.overrides(
        ctx: Context,
        msg: MessageType
    ) =
        msg.options.protokt.implements.emptyToNone().fold(
            { false },
            {
                !it.delegates() &&
                    PropertyOverrideChecker.classIncludesProperty(
                        PClass.fromName(it).possiblyQualify(ctx.pkg),
                        fieldName,
                        ctx
                    )
            }
        )

    private fun String.delegates() =
        contains(" by ")

    val MessageType.doesImplement
        get() = options.protokt.implements.isNotEmpty()

    val MessageType.implements
        get() = options.protokt.implements
}

private object PropertyOverrideChecker {
    val classIncludesProperty =
        { pClass: PClass, prop: String, ctx: Context ->
            getClass(pClass, ctx.desc.params)
                .members.map { m -> m.name }
                .contains(prop)
        }.memoize()
}
