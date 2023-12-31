package org.snappy.cache

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import io.github.oshai.kotlinlogging.KLogger
import org.snappy.SnappyConfig
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Base class for cache types. Handles the storage, cache initialization, and basic fetching from
 * the cache. Implementations must provide the strategy for auto classes to auto-cache as well as
 * some other properties.
 *
 * Caches are readonly after initialization and the initialization is threadsafe using a [lazy]
 * delegate. Initialization starts by scanning the packages specified in the [config] provided.
 * The [ScanResult] is then passed to the implementors [processAllAutoCacheClasses] to return a
 * [Sequence] of [KType] paired to the cache type [C]. Each item from the [Sequence] is then packed
 * into the [cache].
 */
abstract class AbstractTypeCache<C : Any>(private val config: SnappyConfig) {
    abstract val log: KLogger
    internal var cacheLoaded: Boolean = false
        private set

    private val cache: HashMap<KType, C> by lazy { initCache() }

    protected abstract val cacheType: KClass<C>

    abstract fun processAllAutoCacheClasses(result: ScanResult): Sequence<Pair<KType, C>>

    private fun initCache(): HashMap<KType, C> {
        val start = System.currentTimeMillis()
        val cache = HashMap<KType, C>()
        val packages = config.packages.toTypedArray()
        ClassGraph().enableAllInfo().acceptPackages(*packages).scan().use { result ->
            for((type, decoder) in processAllAutoCacheClasses(result)) {
                if (cache.contains(type)) {
                    log.warn {
                        "Multiple decoders exist for type '$type'. The latest one seen is kept"
                    }
                }
                cache[type] = decoder
            }
        }
        cacheLoaded = true
        val end = System.currentTimeMillis()
        log.info {
            "${cacheType.simpleName} cache has been initialized. Took %.2f seconds".format((end - start)/1000.0)
        }
        return cache
    }

    internal fun loadCache() {
        cache
    }

    /**
     * Get a cached item [C] for the provided [type]. Checks the [cache] for an existing items and
     * returns immediately if it exists. Otherwise, it throws an [IllegalStateException]
     */
    @PublishedApi
    internal fun getOrThrow(type: KType): C {
        return cache[type] ?: error("No RowParser cached for '$type'")
    }

    /**
     * Get a cached item [C] for the provided [type]. Checks the [cache] for an existing items and
     * returns immediately if it exists. Otherwise, it returns null
     */
    @PublishedApi
    internal fun getOrNull(type: KType): C? {
        return cache[type]
    }
}
