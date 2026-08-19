package com.lesofn.archforge.infrastructure.frame.database;

/**
 * Compatibility facade. Domain modules should depend on the common-jpa type.
 */
public class GroupDataSourceProxy extends com.lesofn.archforge.common.persistence.GroupDataSourceProxy {

    public GroupDataSourceProxy(javax.sql.DataSource delegate, String group) {
        super(delegate, group);
    }
}
