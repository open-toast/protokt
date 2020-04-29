package com.toasttab.protokt.testing.options

import com.toasttab.protokt.testing.options.pkg.checkDurationTypes
import org.junit.jupiter.api.Test

class SharedSimpleNamesTest {
    @Test
    fun `check types of each duration`() {
        checkDurationTypes(ImportsWrapperModel::class)
    }
}
