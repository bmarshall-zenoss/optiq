/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.optiq.jdbc;

import net.hydromatic.optiq.server.OptiqServerStatement;

import java.sql.*;

/**
 * Implementation of {@link java.sql.Statement}
 * for the Optiq engine.
 */
abstract class OptiqStatement
    implements Statement, OptiqServerStatement
{
    final OptiqConnectionImpl connection;
    private boolean closed;

    /**
     * Support for {@link #closeOnCompletion()} method.
     */
    protected boolean closeOnCompletion;

    /**
     * Current result set, or null if the statement is not executing anything.
     * Any method which modifies this member must synchronize
     * on the OptiqStatement.
     */
    OptiqResultSet openResultSet;

    private int queryTimeoutMillis;
    final int resultSetType;
    final int resultSetConcurrency;
    final int resultSetHoldability;
    private int fetchSize;
    private int fetchDirection;

    OptiqStatement(
        OptiqConnectionImpl connection,
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability)
    {
        assert connection != null;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.connection = connection;
        this.closed = false;
    }

    // implement Statement

    public ResultSet executeQuery(String sql) throws SQLException {
        OptiqPrepare.PrepareResult x = parseQuery(sql);
        return executeQueryInternal(x);
    }

    public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public synchronized void close() {
        if (!closed) {
            closed = true;
            connection.server.removeStatement(this);
            if (openResultSet != null) {
                OptiqResultSet c = openResultSet;
                openResultSet = null;
                c.close();
            }
        }
    }

    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getMaxRows() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setMaxRows(int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getQueryTimeout() throws SQLException {
        long timeoutSeconds = getQueryTimeoutMillis() / 1000;
        if (timeoutSeconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (timeoutSeconds == 0 && getQueryTimeoutMillis() > 0) {
            // Don't return timeout=0 if e.g. timeoutMillis=500. 0 is special.
            return 1;
        }
        return (int) timeoutSeconds;
    }

    int getQueryTimeoutMillis() {
        return queryTimeoutMillis;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        if (seconds < 0) {
            throw connection.helper.createException(
                "illegal timeout value " + seconds);
        }
        setQueryTimeoutMillis(seconds * 1000);
    }

    void setQueryTimeoutMillis(int millis) {
        this.queryTimeoutMillis = millis;
    }

    public synchronized void cancel() throws SQLException {
        if (openResultSet != null) {
            openResultSet.cancel();
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet() throws SQLException {
        // NOTE: result set becomes visible in this member while
        // executeQueryInternal is still in progress, and before it has
        // finished executing. Its internal state may not be ready for API
        // calls. JDBC never claims to be thread-safe! (Except for calls to the
        // cancel method.) It is not possible to synchronize, because it would
        // block 'cancel'.
        return openResultSet;
    }

    public int getUpdateCount() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setFetchDirection(int direction) throws SQLException {
        this.fetchDirection = direction;
    }

    public int getFetchDirection() {
        return fetchDirection;
    }

    public void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public OptiqConnectionImpl getConnection() {
        return connection;
    }

    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int executeUpdate(
        String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public int executeUpdate(
        String sql, int columnIndexes[]) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public int executeUpdate(
        String sql, String columnNames[]) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public boolean execute(
        String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public boolean execute(
        String sql, int columnIndexes[]) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public boolean execute(
        String sql, String columnNames[]) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isClosed() throws SQLException {
        return closed;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isPoolable() throws SQLException {
        throw new UnsupportedOperationException();
    }

    // implement Wrapper

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw connection.helper.createException(
            "does not implement '" + iface + "'");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    /**
     * Executes a parsed query, closing any previously open result set.
     *
     * @param query Parsed query
     * @return Result set
     * @throws SQLException if a database error occurs
     */
    protected ResultSet executeQueryInternal(
        OptiqPrepare.PrepareResult query) throws SQLException
    {
        // Close the previous open CellSet, if there is one.
        synchronized (this) {
            if (openResultSet != null) {
                final OptiqResultSet cs = openResultSet;
                openResultSet = null;
                try {
                    cs.close();
                } catch (Exception e) {
                    throw connection.helper.createException(
                        "Error while closing previous result set", e);
                }
            }

            openResultSet = connection.factory.newResultSet(this, query);
        }
        // Release the monitor before executing, to give another thread the
        // opportunity to call cancel.
        try {
            openResultSet.execute();
        } catch (Exception e) {
            throw connection.helper.createException(
                "exception while executing query", e);
        }
        return openResultSet;
    }

    /**
     * Called by each child result set when it is closed.
     *
     * @param resultSet Result set or cell set
     */
    void onResultSetClose(ResultSet resultSet) {
        if (closeOnCompletion) {
            close();
        }
    }

    protected OptiqPrepare.PrepareResult parseQuery(String sql) {
        return OptiqPrepare.prepare(this, sql);
    }

}

// End OptiqStatement.java