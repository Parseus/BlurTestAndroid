package at.favre.app.blurbenchmark.models

/**
 * Simple wrapper for easier json serialization.
 *
 * @author pfavre
 */
data class BenchmarkResultList(var benchmarkWrappers: MutableList<@JvmSuppressWildcards BenchmarkWrapper> = ArrayList())
