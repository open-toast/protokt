package com.toasttab.protokt.codegen.annotators

import com.toasttab.protokt.codegen.impl.namedCodeBlock

internal class CodeBlockComponents(
    private val formatWithNamedArgs: String,
    private val args: Map<String, Any> = emptyMap()
) {
    fun prepend(
        formatWithNamedArgs: String,
        args: Map<String, Any> = emptyMap()
    ): CodeBlockComponents {
        val intersect = args.keys.intersect(this.args.keys)
        check(intersect.isEmpty()) {
            "duplicate keys in args: $intersect"
        }
        return CodeBlockComponents(
            formatWithNamedArgs + " " + this.formatWithNamedArgs,
            args + this.args
        )
    }

    fun append(
        formatWithNamedArgs: String,
        args: Map<String, Any> = emptyMap()
    ) =
        CodeBlockComponents(formatWithNamedArgs, args) + this

    operator fun plus(other: CodeBlockComponents) =
        other.prepend(formatWithNamedArgs, args)

    fun toCodeBlock() =
        namedCodeBlock(formatWithNamedArgs, args)
}
