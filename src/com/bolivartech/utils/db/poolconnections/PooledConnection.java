package com.bolivartech.utils.db.poolconnections;

import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import java.sql.Connection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Copyright 2016 BolivarTech INC.</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's Class to store the pooled database connection
 * and status.</p>
 *
 * <ul>
 * <li>Class ID: "DBPLDCN"</li>
 * <li>Loc: - </li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2016 - Date: April 16, 2016.
 * @version 1.0.0
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-04-16): Version Inicial.</li>
 * </ul>
 */
public class PooledConnection {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "DBPLDCN";

    // Default database descriptor
    private static final String DEFAULDATABASEDESCRIPTOR = "Database";

    // A boolean that keeps track of whether the connection is in use. A value of 
    // true indicates that the connection has been checked out. 
    // A value of false indicates that the connection is available.   
    @GuardedBy("rwl")
    private boolean inUse;
    // A JDBC Connection object that is cached in the pool.
    @GuardedBy("rwl")
    private Connection conn;
    // A long that holds the time the connection was last checked out. This is 
    // used by the management class to determine when to close and remove from 
    // the cache connections that have not been used in a predetermined period of time.
    @GuardedBy("rwl")
    private long lastUsed;
    // A String object that holds the database descriptor of the pool to which this connection 
    // belongs. This allows you to manage several different connection pools simultaneously.
    @GuardedBy("rwl")
    private String databaseDescriptor;

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Default constructor
     */
    public PooledConnection() {
        this(null, false, DEFAULDATABASEDESCRIPTOR);
    }

    /**
     * Constructor with database connection and inUse flag initialization.
     *
     * @param conn database Connection
     * @param inUse In use flag
     */
    public PooledConnection(Connection conn, boolean inUse) {
        this(conn, inUse, DEFAULDATABASEDESCRIPTOR);
    }

    /**
     * Constructor with database connection, inUse flag initialization and
     * databaseDescriptor initialization.
     *
     * @param conn database Connection
     * @param inUse in use flag
     * @param databaseDescriptor Database Descriptor
     */
    public PooledConnection(Connection conn, boolean inUse, String databaseDescriptor) {
        this.conn = conn;
        this.inUse = inUse;
        this.lastUsed = System.currentTimeMillis();
        this.databaseDescriptor = databaseDescriptor;
    }

    /**
     * Return the database connection
     *
     * @return Database connection or NULL
     */
    public Connection getConnection() {
        Connection Result = null;

        this.rwl.readLock().lock();
        try {
            Result = this.conn;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Set the database connection
     *
     * @param conn Database connection
     */
    public void setConnection(Connection conn) {

        this.rwl.writeLock().lock();
        try {
            this.conn = conn;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Return the inUse flag for the connection
     *
     * @return TRUE or FALSE for the inUse flag
     */
    public boolean getInUse() {
        boolean Result = false;

        this.rwl.readLock().lock();
        try {
            Result = this.inUse;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Check if the connection is in use.
     *
     * @return TRUE if the connection is in USE or FALSE if not.
     */
    public boolean isInUse() {
        return this.getInUse();
    }

    /**
     * Set the inUse flag to the 'inUse' value specified. This method update the
     * lastUse time stamp when the flag is set to FALSE
     *
     * @param inUse TRUE or FALSE for the inUse flag
     */
    public void setInUse(boolean inUse) {

        this.rwl.writeLock().lock();
        try {
            if (!inUse) {
                this.lastUsed = System.currentTimeMillis();
            }
            this.inUse = inUse;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Return the Database Descriptor for the connection.
     *
     * @return Connection's Database Descriptor
     */
    public String getDatabaseDescriptor() {
        String Result = null;

        this.rwl.readLock().lock();
        try {
            Result = this.databaseDescriptor;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Set the Database Descriptor for the connection.
     *
     * @param databaseDescriptor Database Descriptor
     */
    public void setDatabaseDescriptor(String databaseDescriptor) {

        this.rwl.writeLock().lock();
        try {
            this.databaseDescriptor = databaseDescriptor;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Return the last used time stamp for the connection in millisecond from
     * Jan,1, 1970
     *
     * @return Last used time stamp in milliseconds.
     */
    public long getLastUsed() {
        long Result = 0;

        this.rwl.readLock().lock();
        try {
            Result = this.lastUsed;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }
}
