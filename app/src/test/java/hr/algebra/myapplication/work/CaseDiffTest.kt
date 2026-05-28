package hr.algebra.myapplication.work

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CaseDiffTest {

    @Test fun `no change returns false`() {
        val snap = mapOf(1 to "OPEN", 2 to "CLOSED")
        assertThat(detectCaseChanges(previous = snap, current = snap)).isFalse()
    }

    @Test fun `status flip returns true`() {
        val previous = mapOf(1 to "OPEN")
        val current = mapOf(1 to "CLOSED")
        assertThat(detectCaseChanges(previous, current)).isTrue()
    }

    @Test fun `added case returns true`() {
        val previous = mapOf(1 to "OPEN")
        val current = mapOf(1 to "OPEN", 2 to "OPEN")
        assertThat(detectCaseChanges(previous, current)).isTrue()
    }

    @Test fun `removed case returns true`() {
        val previous = mapOf(1 to "OPEN", 2 to "OPEN")
        val current = mapOf(1 to "OPEN")
        assertThat(detectCaseChanges(previous, current)).isTrue()
    }

    @Test fun `both empty returns false`() {
        assertThat(detectCaseChanges(emptyMap(), emptyMap())).isFalse()
    }

    @Test fun `empty previous with non-empty current returns true`() {
        assertThat(detectCaseChanges(emptyMap(), mapOf(1 to "OPEN"))).isTrue()
    }
}
