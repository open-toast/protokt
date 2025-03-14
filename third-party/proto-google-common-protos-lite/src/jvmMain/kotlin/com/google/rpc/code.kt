@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/rpc/code.proto
package com.google.rpc

import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import kotlin.Int
import kotlin.String

@Deprecated("use v1")
sealed class Code(
    override val `value`: Int,
    override val name: String,
) : KtEnum() {
    object OK : Code(0, "OK")

    object CANCELLED : Code(1, "CANCELLED")

    object UNKNOWN : Code(2, "UNKNOWN")

    object INVALID_ARGUMENT : Code(3, "INVALID_ARGUMENT")

    object DEADLINE_EXCEEDED : Code(4, "DEADLINE_EXCEEDED")

    object NOT_FOUND : Code(5, "NOT_FOUND")

    object ALREADY_EXISTS : Code(6, "ALREADY_EXISTS")

    object PERMISSION_DENIED : Code(7, "PERMISSION_DENIED")

    object UNAUTHENTICATED : Code(16, "UNAUTHENTICATED")

    object RESOURCE_EXHAUSTED : Code(8, "RESOURCE_EXHAUSTED")

    object FAILED_PRECONDITION : Code(9, "FAILED_PRECONDITION")

    object ABORTED : Code(10, "ABORTED")

    object OUT_OF_RANGE : Code(11, "OUT_OF_RANGE")

    object UNIMPLEMENTED : Code(12, "UNIMPLEMENTED")

    object INTERNAL : Code(13, "INTERNAL")

    object UNAVAILABLE : Code(14, "UNAVAILABLE")

    object DATA_LOSS : Code(15, "DATA_LOSS")

    class UNRECOGNIZED(
        `value`: Int,
    ) : Code(value, "UNRECOGNIZED")

    companion object Deserializer : KtEnumDeserializer<Code> {
        override fun from(`value`: Int): Code = when (value) {
          0 -> OK
          1 -> CANCELLED
          2 -> UNKNOWN
          3 -> INVALID_ARGUMENT
          4 -> DEADLINE_EXCEEDED
          5 -> NOT_FOUND
          6 -> ALREADY_EXISTS
          7 -> PERMISSION_DENIED
          16 -> UNAUTHENTICATED
          8 -> RESOURCE_EXHAUSTED
          9 -> FAILED_PRECONDITION
          10 -> ABORTED
          11 -> OUT_OF_RANGE
          12 -> UNIMPLEMENTED
          13 -> INTERNAL
          14 -> UNAVAILABLE
          15 -> DATA_LOSS
          else -> UNRECOGNIZED(value)
        }
    }
}
