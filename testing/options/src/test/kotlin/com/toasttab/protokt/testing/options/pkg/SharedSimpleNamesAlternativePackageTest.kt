package com.toasttab.protokt.testing.options.pkg

import com.google.common.truth.Truth.assertThat
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure
import org.junit.jupiter.api.Test

class SharedSimpleNamesAlternativePackageTest {
    @Test
    fun `check types of each duration`() {
        checkDurationTypes(ImportsWrapperModel::class)
    }
}

fun checkDurationTypes(klass: KClass<*>) {
    assertThat(klass.propertyType("nativeDuration"))
        .isEqualTo(com.toasttab.protokt.Duration::class)

    assertThat(klass.propertyType("javaDuration"))
        .isEqualTo(java.time.Duration::class)

    assertThat(klass.propertyType("superfluousDuration"))
        .isEqualTo(com.toasttab.protokt.testing.options.Duration::class)
}

private fun KClass<*>.propertyType(name: String) =
    declaredMemberProperties.single { it.name == name }
        .returnType.jvmErasure
