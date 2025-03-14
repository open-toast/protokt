@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/month.proto
package com.google.type

import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import kotlin.Int
import kotlin.String

@Deprecated("use v1")
sealed class Month(
    override val `value`: Int,
    override val name: String,
) : KtEnum() {
    object MONTH_UNSPECIFIED : Month(0, "MONTH_UNSPECIFIED")

    object JANUARY : Month(1, "JANUARY")

    object FEBRUARY : Month(2, "FEBRUARY")

    object MARCH : Month(3, "MARCH")

    object APRIL : Month(4, "APRIL")

    object MAY : Month(5, "MAY")

    object JUNE : Month(6, "JUNE")

    object JULY : Month(7, "JULY")

    object AUGUST : Month(8, "AUGUST")

    object SEPTEMBER : Month(9, "SEPTEMBER")

    object OCTOBER : Month(10, "OCTOBER")

    object NOVEMBER : Month(11, "NOVEMBER")

    object DECEMBER : Month(12, "DECEMBER")

    class UNRECOGNIZED(
        `value`: Int,
    ) : Month(value, "UNRECOGNIZED")

    companion object Deserializer : KtEnumDeserializer<Month> {
        override fun from(`value`: Int): Month = when (value) {
          0 -> MONTH_UNSPECIFIED
          1 -> JANUARY
          2 -> FEBRUARY
          3 -> MARCH
          4 -> APRIL
          5 -> MAY
          6 -> JUNE
          7 -> JULY
          8 -> AUGUST
          9 -> SEPTEMBER
          10 -> OCTOBER
          11 -> NOVEMBER
          12 -> DECEMBER
          else -> UNRECOGNIZED(value)
        }
    }
}
