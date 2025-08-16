package com.lesofn.matrixboot.frame.database;

import com.lesofn.matrixboot.frame.context.RequestContext;
import com.lesofn.matrixboot.frame.context.ThreadLocalContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-10-13 11:57
 */
public class ReadWriteDataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        RequestContext rc = ThreadLocalContext.getRequestContext();
        return rc.isReadMasterDB() || rc.isShouldReadMasterDB() ? DataBaseType.Master : DataBaseType.Slave;
    }

}
