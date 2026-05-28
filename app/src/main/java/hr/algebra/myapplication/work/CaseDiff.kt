package hr.algebra.myapplication.work

/**
 * Returns true if any case in [current] has a different status than in [previous], or if any
 * case present in [previous] has been removed from [current]. Pure function — no Android or
 * coroutine context required, so it can be unit-tested directly on the JVM.
 */
internal fun detectCaseChanges(
    previous: Map<Int, String>,
    current: Map<Int, String>,
): Boolean =
    current.any { (id, status) -> previous[id] != status } ||
        previous.keys.any { it !in current }
