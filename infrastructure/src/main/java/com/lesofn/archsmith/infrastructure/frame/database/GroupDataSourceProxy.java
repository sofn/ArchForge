package com.lesofn.archsmith.infrastructure.frame.database;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * DataSource proxy that routes to a specific dynamic-datasource group. Pushes the group name onto
 * DynamicDataSourceContextHolder before obtaining a connection, ensuring the correct datasource is
 * used.
 *
 * @author sofn
 */
public class GroupDataSourceProxy implements DataSource {

    private final DataSource delegate;
    private final String group;

    public GroupDataSourceProxy(DataSource delegate, String group) {
        this.delegate = delegate;
        this.group = group;
    }

    @Override
    public Connection getConnection() throws SQLException {
        DynamicDataSourceContextHolder.push(group);
        try {
            return delegate.getConnection();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        DynamicDataSourceContextHolder.push(group);
        try {
            return delegate.getConnection(username, password);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
