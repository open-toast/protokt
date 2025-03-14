@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/launch_stage.proto
package com.google.api

import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import kotlin.Int
import kotlin.String

@Deprecated("use v1")
sealed class LaunchStage(
    override val `value`: Int,
    override val name: String,
) : KtEnum() {
    object LAUNCH_STAGE_UNSPECIFIED : LaunchStage(0, "LAUNCH_STAGE_UNSPECIFIED")

    object UNIMPLEMENTED : LaunchStage(6, "UNIMPLEMENTED")

    object PRELAUNCH : LaunchStage(7, "PRELAUNCH")

    object EARLY_ACCESS : LaunchStage(1, "EARLY_ACCESS")

    object ALPHA : LaunchStage(2, "ALPHA")

    object BETA : LaunchStage(3, "BETA")

    object GA : LaunchStage(4, "GA")

    object DEPRECATED : LaunchStage(5, "DEPRECATED")

    class UNRECOGNIZED(
        `value`: Int,
    ) : LaunchStage(value, "UNRECOGNIZED")

    companion object Deserializer : KtEnumDeserializer<LaunchStage> {
        override fun from(`value`: Int): LaunchStage = when (value) {
          0 -> LAUNCH_STAGE_UNSPECIFIED
          6 -> UNIMPLEMENTED
          7 -> PRELAUNCH
          1 -> EARLY_ACCESS
          2 -> ALPHA
          3 -> BETA
          4 -> GA
          5 -> DEPRECATED
          else -> UNRECOGNIZED(value)
        }
    }
}
