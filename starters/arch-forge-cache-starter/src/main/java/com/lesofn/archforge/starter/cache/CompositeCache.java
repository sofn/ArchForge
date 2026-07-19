package com.lesofn.archforge.starter.cache;

import java.util.concurrent.Callable;
import org.springframework.cache.Cache;

/**
 * Composite Cache: L1 (Caffeine) + L2 (Redis).
 *
 * <p>
 * Reads go L1 → L2; a miss in L1 but hit in L2 promotes the value back to L1.
 * Writes go L2 → L1, then broadcast an L1 invalidation so other instances reload
 * from L2 lazily.
 */
public class CompositeCache implements Cache {

    private final String name;
    private final Cache l1Cache;
    private final Cache l2Cache;
    private final CacheSyncBroadcaster broadcaster;

    public CompositeCache(String name, Cache l1Cache, Cache l2Cache, CacheSyncBroadcaster broadcaster) {
        this.name = name;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.broadcaster = broadcaster;
    }

    private void broadcastEvict(Object key) {
        if (broadcaster != null && l1Cache != null) {
            broadcaster.broadcastEvict(name, key);
        }
    }

    private void broadcastClear() {
        if (broadcaster != null && l1Cache != null) {
            broadcaster.broadcastClear(name);
        }
    }

    @Override
    public String getName() { return name; }

    @Override
    public Object getNativeCache() { return this; }

    @Override
    public ValueWrapper get(Object key) {
        if (l1Cache != null) {
            ValueWrapper wrapper = l1Cache.get(key);
            if (wrapper != null) {
                return wrapper;
            }
        }
        if (l2Cache != null) {
            ValueWrapper wrapper = l2Cache.get(key);
            if (wrapper != null) {
                if (l1Cache != null) {
                    l1Cache.put(key, wrapper.get());
                }
                return wrapper;
            }
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (l1Cache != null) {
            T value = l1Cache.get(key, type);
            if (value != null) {
                return value;
            }
        }
        if (l2Cache != null) {
            T value = l2Cache.get(key, type);
            if (value != null) {
                if (l1Cache != null) {
                    l1Cache.put(key, value);
                }
                return value;
            }
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (l1Cache != null) {
            ValueWrapper wrapper = l1Cache.get(key);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return value;
            }
        }
        if (l2Cache != null) {
            T value = l2Cache.get(key, valueLoader);
            if (value != null && l1Cache != null) {
                l1Cache.put(key, value);
            }
            return value;
        }
        try {
            return valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        // Write the shared L2 first, then the local L1, so a concurrent reader on this
        // instance never promotes a value newer than what L2 holds.
        if (l2Cache != null) {
            l2Cache.put(key, value);
        }
        if (l1Cache != null) {
            l1Cache.put(key, value);
        }
        broadcastEvict(key);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existing = get(key);
        if (existing != null) {
            return existing;
        }
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        if (l2Cache != null) {
            l2Cache.evict(key);
        }
        if (l1Cache != null) {
            l1Cache.evict(key);
        }
        broadcastEvict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean evicted = false;
        if (l2Cache != null) {
            evicted = l2Cache.evictIfPresent(key);
        }
        if (l1Cache != null) {
            evicted = l1Cache.evictIfPresent(key) || evicted;
        }
        broadcastEvict(key);
        return evicted;
    }

    @Override
    public void clear() {
        if (l2Cache != null) {
            l2Cache.clear();
        }
        if (l1Cache != null) {
            l1Cache.clear();
        }
        broadcastClear();
    }

    @Override
    public boolean invalidate() {
        boolean invalidated = false;
        if (l2Cache != null) {
            invalidated = l2Cache.invalidate();
        }
        if (l1Cache != null) {
            invalidated = l1Cache.invalidate() || invalidated;
        }
        broadcastClear();
        return invalidated;
    }
}
