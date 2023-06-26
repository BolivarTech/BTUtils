package com.bolivartech.utils.db.poolconnections;

import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * This Class is the BolivarTech's Singlenton Class to manage the pool of
 * connections.</p>
 *
 * <ul>
 * <li>Class ID: "DBPLCNM"</li>
 * <li>Loc: 000-021 </li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2016 - Date: April 22, 2016.
 * @version 1.0.1
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-04-16): Version Inicial.</li>
 * <li>v1.0.1 (2016-04-22): Se agrego el commit cuando se cierra la conexion y
 * no esta definido el autocommit en la base de datos.</li>
 * </ul>
 */
public class ConnectionsPoolManager implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "DBPLCNM";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Tiempo maximo en milisegundos que una conecion puede estar sin usarse antes de ser eliminada
    private static final long MAX_IDLE_TIME = 60000; // 1 minutos

    // Numero maximo de coneciones que puede contener el pool
    private static final int MAX_NUM_CONNECTIONS = 200;

    // Numero de coneciones en el que se incrementa el pool
    private static final int NUM_CONNECTIONS_BLOCK = 5;

    // Error de parametro NULL
    public static final int ERROR_NULLINPUT = -1;
    // Error de conexion no encontrado en el pool
    public static final int ERROR_CONNECTIONNOTFOUND = -2;
    // Error monitor not running
    public static final int ERROR_MONITORNOTRUNNING = -3;
    // Error de apuntador NULL
    public static final int ERROR_NULLPOINTER = -4;
    // Error de conexion no establecida
    public static final int ERROR_CONNECTIONNOTESTABLISHED = -5;
    // Error de que el Pool esta a su liminte
    public static final int ERROR_POOLISFULL = -6;
    // Error el limite del Pool esta fuera de rango
    public static final int ERROR_POOLIMITRANGE = -7;
    // Error la dimension del bloque de incremento del Pool esta fuera de rango
    public static final int ERROR_POOBLOCKSIZERANGE = -8;

    // Vector con el pool de conecciones
    @GuardedBy("rwl")
    private ArrayList<PooledConnection> poolConnections;
    // Hebra de monitoreo
    @GuardedBy("rwl")
    private Thread Ejecutor = null;

    // Identificador de la hebra de ejecucion
    private long ThreadID;

    // Bandera para indicar si se compreto la operacion de cierre del manejador de conexiones
    @GuardedBy("rwl")
    private boolean Finished = true;
    // Bandera para indicar qu se finalice el manejador de caches de conexion
    @GuardedBy("rwl")
    private boolean Finish;

    // Los lock para el manejo de concurrencia
    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Formateador de Bitacoras
    private LoggerFormatter BTLogF;

    // Generador de Numeros Aleatorios
    private MersenneTwisterPlus Random;

    // Instancia de la clase
    @GuardedBy("rwl")
    private static ConnectionsPoolManager instance = null;

    // Limite del numero de conexiones en el pool
    @GuardedBy("rwl")
    private int poolLimit = ConnectionsPoolManager.MAX_NUM_CONNECTIONS;

    // Numero de conexiones cuando secrea un nuevo bloque en el pool.
    @GuardedBy("rwl")
    private int poolBlockSize = ConnectionsPoolManager.NUM_CONNECTIONS_BLOCK;

    /**
     * Constructor privado
     */
    private ConnectionsPoolManager(LoggerManager BTLogM) {

        this.BTLogF = LoggerFormatter.getInstance(BTLogM);
        this.Random = new MersenneTwisterPlus();
        this.poolConnections = new ArrayList<>(5);
        this.Ejecutor = null;
    }

    /**
     * Retorna le Instancia de la clase Singlenton.
     *
     * @return Instancia de la clase singlenton
     */
    public static ConnectionsPoolManager getInstance() {

        rwl.writeLock().lock();
        try {
            if (ConnectionsPoolManager.instance == null) {
                ConnectionsPoolManager.instance = new ConnectionsPoolManager(null);
            }
        } finally {
            rwl.writeLock().unlock();
        }
        return ConnectionsPoolManager.instance;
    }

    /**
     * Metodo privado que inicializa la hebra de escucha del handler
     */
    private final void StartThread() {

        // Inicializa la Hebra Principal del Handler
        if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
            this.Ejecutor = new Thread(this);
            this.ThreadID = Random.nextLong63();
            this.Ejecutor.setName(this.CLASSID + "[" + Long.toHexString(this.ThreadID) + "]");
            this.Ejecutor.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    StringWriter StackTrace;
                    long StartTime, DiffTime;
                    LoggerFormatter BTLogF;
                    StringBuffer Stack;

                    BTLogF = LoggerFormatter.getInstance(null);
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Uncaught Exception\n" + e.toString(), ConnectionsPoolManager.CLASSID, "000");
                    Stack = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Stack.append(STE.toString() + "\n");
                        }
                    } else {
                        Stack.append("ERROR: Thread is NULL at StackTrace");
                    }
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Stack:\n" + Stack.toString(), ConnectionsPoolManager.CLASSID, "001");
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "StackTrace:\n" + StackTrace.toString(), ConnectionsPoolManager.CLASSID, "002");
                    DiffTime = 0;
                    StartTime = System.currentTimeMillis();
                    while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ConnectionsPoolManager.CLASSID, "003");
                        } finally {
                            DiffTime = System.currentTimeMillis() - StartTime;
                        }
                    }
                    if (DiffTime >= TIMEOUT) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Finished by TimeOut", ConnectionsPoolManager.CLASSID, "004");
                    }
                    if (Ejecutor != null) {
                        if (Ejecutor.getState() != Thread.State.TERMINATED) {
                            Ejecutor.interrupt();
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Forced Thread Finish by Interruption", ConnectionsPoolManager.CLASSID, "005");
                        }
                        Ejecutor = null;
                    }
                    rwl.writeLock().lock();
                    try {
                        Finished = true;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                }
            });
            this.Finished = false;
            this.Finish = false;
            this.Ejecutor.setDaemon(true);
            this.Ejecutor.start();
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Can't start Thread because the previous still is Running", ConnectionsPoolManager.CLASSID, "006");
        }
    }

    /**
     * Verifica si termino de finalizar el manejador de cache de conexiones
     *
     * @return TRUE si se termino o FALSE si no
     */
    public boolean isFinished() {
        boolean Result = false;

        this.rwl.readLock().lock();
        try {
            Result = this.Finished;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Inicia la finalizacin del manejador de cache de conexiones a base datos.
     */
    public void Finish() {

        this.rwl.writeLock().lock();
        try {
            this.Finish = true;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna la dimension maxima que puede alcanzar el pool de conexiones
     *
     * @return Maximo tamaño que puede alcanzar el pool de conexiones
     */
    public int getPoolLimit() {
        int Result = ConnectionsPoolManager.MAX_NUM_CONNECTIONS;

        this.rwl.readLock().lock();
        try {
            Result = this.poolLimit;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece la dimension maxima que puede alcanzar el pool de conexiones
     *
     * @param poolLimit Maximo tamaño que puede alcanzar el pool de conexiones
     * @throws UtilsException
     */
    public void setPoolLimit(int poolLimit) throws UtilsException {

        this.rwl.writeLock().lock();
        try {
            if (poolLimit > 0) {
                this.poolLimit = poolLimit;
            } else {
                throw new UtilsException("Pool limit is less than 1(" + Integer.toString(poolLimit) + ")", ConnectionsPoolManager.ERROR_POOLIMITRANGE, ConnectionsPoolManager.CLASSID + "007");
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna la cantidad de conexiones que se generan en un bloque cuando se
     * incrementa la dimension del pool.
     *
     * @return Numero de conexiones en un bloque para incrementar el pool
     */
    public int getPoolBlockSize() {
        int Result = ConnectionsPoolManager.NUM_CONNECTIONS_BLOCK;

        this.rwl.readLock().lock();
        try {
            Result = this.poolBlockSize;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece la cantidad de conexiones que se generan en un bloque cuando se
     * incrementa la dimension del pool.
     *
     * @param poolBlockSize Numero de conexiones en un bloque para incrementar
     * el pool.
     * @throws UtilsException
     */
    public void setPoolBlockSize(int poolBlockSize) throws UtilsException {

        this.rwl.writeLock().lock();
        try {
            if (poolBlockSize > 0) {
                this.poolBlockSize = poolBlockSize;
            } else {
                throw new UtilsException("Pool Block Size is less than 1(" + Integer.toString(poolBlockSize) + ")", ConnectionsPoolManager.ERROR_POOBLOCKSIZERANGE, ConnectionsPoolManager.CLASSID + "008");
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Obtiene una conexion del pool en cache en base a las propiedades de
     * conexion con la base de datos especificados en baseName.
     *
     * Si no existen las conexiones o no hay disponibles, la crea y la retorna.
     *
     * @param databaseDescriptor archivo de properties para la conexion de la
     * base de datos
     * @return Conneccion a la base de datos o null si no pudo conseguirla
     * @throws UtilsException
     */
    public Connection checkOut(String databaseDescriptor) throws UtilsException {
        boolean found = false;
        PooledConnection cached = null;
        int i, numberConnections;
        Connection Conn = null;

        this.rwl.writeLock().lock();
        try {
            numberConnections = this.poolConnections.size();
            // Busca una conexion al descriptor de la dase de datos que no este en uso
            for (i = 0; !found && (i < numberConnections); i++) {
                cached = (PooledConnection) this.poolConnections.get(i);
                if ((cached != null) && (!cached.isInUse()) && cached.getDatabaseDescriptor().equals(databaseDescriptor) && (cached.getConnection() != null)) {
                    found = true;
                }
            }
            if (found) {
                // Se encontro una conexion disponible en el pool
                cached.setInUse(true);
                Conn = cached.getConnection();
            } else if (numberConnections < this.poolLimit) {
                // No se encontro ninguna conexion disponible en el pool
                // Se crea un nuevo bloque de conexiones en el pool
                numberConnections = Math.min((this.poolLimit - numberConnections), this.poolBlockSize);
                if (numberConnections > 0) {
                    // Genera el bloque de conexiones que estaran disponibles en el pool
                    numberConnections--;
                    for (i = 0; i < numberConnections; i++) {
                        Conn = Database.getConnection(databaseDescriptor);
                        if (Conn != null) {
                            cached = new PooledConnection(Conn, false, databaseDescriptor);
                            this.poolConnections.add(cached);
                        } else {
                            throw new UtilsException("Connection to DB NOT Established", ConnectionsPoolManager.ERROR_CONNECTIONNOTESTABLISHED, ConnectionsPoolManager.CLASSID + "009");
                        }
                    }
                    // Genera la ultima conexion del bloque y se la retorna al que realizo la peticion
                    Conn = Database.getConnection(databaseDescriptor);
                    if (Conn != null) {
                        cached = new PooledConnection(Conn, true, databaseDescriptor);
                        this.poolConnections.add(cached);
                    } else {
                        // Trata de encontrar una de las conexiones que se creo en el bloque para retornarla
                        numberConnections = this.poolConnections.size();
                        // Busca una conexion al descriptor de la dase de datos que no este en uso
                        for (i = numberConnections - 1; !found && (i >= 0); i--) {
                            cached = (PooledConnection) this.poolConnections.get(i);
                            if ((cached != null) && (!cached.isInUse()) && cached.getDatabaseDescriptor().equals(databaseDescriptor) && (cached.getConnection() != null)) {
                                found = true;
                            }
                        }
                        if (found) {
                            // Se encontro una conexion disponible en el pool
                            cached.setInUse(true);
                            Conn = cached.getConnection();
                        } else {
                            // No se pudo encontrar ninguna disponible
                            Conn = null;
                            throw new UtilsException("Connection to DB NOT Established", ConnectionsPoolManager.ERROR_CONNECTIONNOTESTABLISHED, ConnectionsPoolManager.CLASSID + "010");
                        }
                    }
                } else {
                    Conn = null;
                    throw new UtilsException("Number of connextion in Block to create in the Pool is " + Integer.toString(numberConnections) + ",Can't create new Connections Block to DB", ConnectionsPoolManager.ERROR_POOLISFULL, ConnectionsPoolManager.CLASSID + "011");
                }
            } else {
                Conn = null;
                throw new UtilsException("Pool is FULL(" + Integer.toString(numberConnections) + "/" + Integer.toString(this.poolLimit) + "),Can't create new Connections to DB", ConnectionsPoolManager.ERROR_POOLISFULL, ConnectionsPoolManager.CLASSID + "012");
            }
            if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
                // Inicia la hebra de monitoreo
                this.StartThread();
            }
        } catch (NullPointerException ex) {
            Conn = null;
            throw new UtilsException("Null Pointer Exception: " + ex.getMessage(), ConnectionsPoolManager.ERROR_NULLPOINTER, ConnectionsPoolManager.CLASSID + "013");
        } finally {
            this.rwl.writeLock().unlock();
        }
        return Conn;
    }

    /**
     * Retorna la coneccion 'Connect' al pool de conexiones en cache, dejandola
     * libre para que pueda ser reutilizada.
     *
     * @param Connect Conexion a retornar al pool de conexiones
     * @throws UtilsException
     */
    public void checkIn(Connection Connect) throws UtilsException {
        boolean found = false;
        boolean closed = false;
        PooledConnection cached = null;
        Connection conn = null;
        int i, numberConnections;

        if (Connect == null) {
            throw new UtilsException("Connection is NULL!!!", ConnectionsPoolManager.ERROR_NULLINPUT, ConnectionsPoolManager.CLASSID + "014");
        }
        this.rwl.writeLock().lock();
        try {
            numberConnections = this.poolConnections.size();
            // Busca la conexion en el pool de conexiones
            for (i = 0; !found && (i < numberConnections); i++) {
                cached = (PooledConnection) this.poolConnections.get(i);
                conn = cached.getConnection();
                if ((conn != null) && (Connect.equals(conn))) {
                    found = true;
                }
            }
            // Verifica si se encontro la conexion
            if (found) {
                // Se verifica si la conexion esta cerrada
                try {
                    closed = (conn != null ? conn.isClosed() : true);
                } catch (SQLException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "Can't verify connection status: " + ex.getMessage(), ConnectionsPoolManager.CLASSID, "015");
                    closed = true;
                }
                if ((cached != null) && (!closed)) {
                    // Si no esta cerrada se libera en el pool para ser reutilizada
                    cached.setInUse(false);
                } else {
                    // Si esta cerrada o es null se elimina del pool
                    this.poolConnections.remove(i);
                }
            } else {
                // La conexion pasada como parametro no pertenece al pool
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "In use Connection not found in the pool!!!", ConnectionsPoolManager.CLASSID, "016");
                throw new UtilsException("In use Connection not found in the pool!!!", ConnectionsPoolManager.ERROR_CONNECTIONNOTFOUND, ConnectionsPoolManager.CLASSID + "017");
            }
        } catch (NullPointerException ex) {
            throw new UtilsException("Null Pointer Exception: " + ex.getMessage(), ConnectionsPoolManager.ERROR_NULLPOINTER, ConnectionsPoolManager.CLASSID + "018");
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Verifica y limpia las conexiones que no han sido usadas por mas de
     * 'MAX_IDLE_TIME' miliseguindos, que estan cerradas o que son NULL.
     */
    private void cleanUnusedConnections() {
        PooledConnection cached = null;
        Connection conn = null;
        int i, numberConnections;
        long now, before, diffTime;

        before = 0;
        now = System.currentTimeMillis();
        this.rwl.writeLock().lock();
        try {
            numberConnections = this.poolConnections.size();
            for (i = numberConnections - 1; i >= 0; i--) {
                cached = (PooledConnection) this.poolConnections.get(i);
                if (cached != null) {
                    conn = cached.getConnection();
                    if (conn != null) {
                        if (!cached.isInUse()) {
                            before = cached.getLastUsed();
                            diffTime = (now - before);
                            if (diffTime > ConnectionsPoolManager.MAX_IDLE_TIME) {
                                try {
                                    if (!conn.getAutoCommit()) {
                                        conn.commit();
                                    }
                                    conn.close();
                                } catch (SQLException ex) {
                                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "Unable to close connection: " + ex.getMessage(), ConnectionsPoolManager.CLASSID, "019");
                                }
                                this.poolConnections.remove(i);
                            }
                        }
                    } else {
                        this.poolConnections.remove(i);
                    }
                } else {
                    this.poolConnections.remove(i);
                }
            }
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Se asegura de finalizar el pool antes de concluir las conexiones
     *
     * @throws Throwable
     */
    @Override
    public void finalize() throws Throwable {

        // Finaliza el Manager
        this.Finish();
    }

    /**
     * Ejecutor de la hebra de monitoreo de conexiones
     */
    @Override
    public void run() {
        boolean lFinish;
        int i, numberConnections;
        PooledConnection cached = null;
        Connection conn = null;
        long StartTime, DiffTime;

        StartTime = System.currentTimeMillis();
        do {
            // Limpia las conexiones que no estan siendo usadas
            this.cleanUnusedConnections();
            try {
                // Duerme por un tiempo aleatorio entre 0.001 hasta 1 segundo
                EnvironmentUtils.randomSleep(1000);
            } catch (UtilsException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ConnectionsPoolManager.CLASSID, "020");
            }
            this.rwl.readLock().lock();
            try {
                // Actualiza el numero de conexiones en el pool
                numberConnections = this.poolConnections.size();
                // Actualiza el estado de la bandera local de finializacion
                lFinish = this.Finish;
            } finally {
                this.rwl.readLock().unlock();
            }
            if (numberConnections <= 0) {
                DiffTime = System.currentTimeMillis() - StartTime;
                if (DiffTime > ConnectionsPoolManager.MAX_IDLE_TIME) {
                    lFinish = true;
                }
            } else {
                StartTime = System.currentTimeMillis();
            }
        } while (!lFinish);
        // Finaliza las conexiones existentes 
        this.rwl.writeLock().lock();
        try {
            // Actualiza el numero de conexiones en el pool
            numberConnections = this.poolConnections.size();
            for (i = 0; i < numberConnections; i++) {
                cached = (PooledConnection) this.poolConnections.get(i);
                if (cached != null) {
                    conn = cached.getConnection();
                    if (conn != null) {
                        try {
                            if (!conn.getAutoCommit()) {
                                conn.commit();
                            }
                            conn.close();
                        } catch (SQLException ex) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "Can't close connection!!!", ConnectionsPoolManager.CLASSID, "021");
                        }
                    }
                }
            }
            this.poolConnections.clear();
            this.poolConnections.trimToSize();
            this.Ejecutor = null;
            this.Finished = true;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }
}
