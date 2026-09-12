package com.lesofn.archforge.meta.table.api.datascope;

/**
 * Port supplying the current request's data scope for meta-table row queries.
 *
 * <p>
 * Implementations live in the hosting application (e.g. server-admin reads the
 * {@code @DataPermission}-populated context holder and expands the dept tree).
 * When no implementation bean is present, callers must treat the scope as
 * {@link MetaDataScope#all()}.
 */
public interface MetaDataScopeProvider {

    /**
     * Returns the effective data scope of the current request.
     *
     * @return never null; {@link MetaDataScope#all()} when unauthenticated or unrestricted
     */
    MetaDataScope current();
}
