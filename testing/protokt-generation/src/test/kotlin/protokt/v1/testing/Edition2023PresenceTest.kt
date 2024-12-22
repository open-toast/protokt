package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class Edition2023PresenceTest {
    @Test
    fun `file with implicit presence and no field features has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("foo")).isFalse()
        assertThat(TestFileImplicit {}.foo).isEqualTo(0)

        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("corge")).isFalse()
        assertThat(TestFileImplicit {}.corge).isEqualTo(0)
    }

    @Test
    fun `file with implicit presence and no field features has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("bar")).isTrue()
        assertThat(TestFileImplicit {}.bar).isNull()
    }

    @Test
    fun `file with implicit presence and field with explicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("baz")).isTrue()
        assertThat(TestFileImplicit {}.baz).isNull()
    }

    @Test
    fun `file with implicit presence and field with explicit presence has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("qux")).isTrue()
        assertThat(TestFileImplicit {}.qux).isNull()
    }

    @Test
    fun `file with implicit presence and field with legacy required presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("garply")).isFalse()
        assertThat(TestFileImplicit {}.garply).isEqualTo(0)
    }

    @Test
    fun `file with implicit presence and field with legacy required has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("thud")).isFalse()
        assertThat(TestFileImplicit {}.thud).isNull()
    }

    @Test
    fun `file with explicit presence and no field features has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("foo")).isFalse()
        assertThat(TestFileImplicit {}.foo).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and no field features has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("bar")).isTrue()
        assertThat(TestFileImplicit {}.bar).isNull()
    }

    @Test
    fun `file with explicit presence and field with explicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("baz")).isTrue()
        assertThat(TestFileImplicit {}.baz).isNull()
    }

    @Test
    fun `file with explicit presence and field with explicit presence has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("qux")).isTrue()
        assertThat(TestFileImplicit {}.qux).isNull()
    }

    @Test
    fun `file with explicit presence and field with implicit presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("corge")).isFalse()
        assertThat(TestFileImplicit {}.corge).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and field with legacy required presence has correct behavior on primitive`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("garply")).isFalse()
        assertThat(TestFileImplicit {}.garply).isEqualTo(0)
    }

    @Test
    fun `file with explicit presence and field with legacy required has correct behavior on message`() {
        assertThat(TestFileImplicit::class.propertyIsMarkedNullable("thud")).isFalse()
        assertThat(TestFileImplicit {}.thud).isNull()
    }
}
