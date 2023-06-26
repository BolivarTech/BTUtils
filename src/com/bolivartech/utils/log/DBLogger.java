package com.bolivartech.utils.log;

import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.db.poolconnections.ConnectionsPoolManager;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.exception.UtilsException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Copyright 2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement the Singlenton
 * BolivarTech's Logger into the BDLogger table.</p>
 *
 * <p>
 * NOTE: This class is Thread Safe.</p>
 *
 * <p>
 * Implementa una clase Singlenton que define el almacenamiento de bitacoras en
 * la tabla BDLogger.</p>
 *
 * <p>
 * Clase del tipo Singleton.</p>
 *
 * <p>
 * NOTA: Esta clase es segura para las concurrencias.</p>
 *
 * <p>
 * NOTE: MySQL Table Structure:</p>
 *
 * <p>
 * CREATE TABLE `DBLogger` (</p>
 * <p>
 * `when` datetime NOT NULL,</p>
 * <p>
 * `where` varchar(1024) NOT NULL DEFAULT 'Unknown',</p>
 * <p>
 * `type` varchar(8) NOT NULL DEFAULT 'Unknown' COMMENT 'Tipos: Event, Error o
 * Unknown',</p>
 * <p>
 * `level` varchar(9) NOT NULL DEFAULT 'Unknown' COMMENT 'Niveles: Trace, Debug,
 * Info, Warning, Error, Fatal, Unknown',</p>
 * <p>
 * `message` varchar(4096) NOT NULL DEFAULT 'Unknown'</p>
 * <p>
 * ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Repositorio de la clase
 * DBLogger';</p>
 *
 * </br></br>
 * <ul>
 * <li>Class ID: "35DBLMN"</li>
 * <li>Loc: 000-018</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2016 - April 22, 2016.
 * @version 1.0.1
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-04-20): Version Inicial.</li>
 * <li>v1.0.1 (2016-04-22): Se implemento el uso de PreparedStatement para
 * evitar ataques de SQL Injection.</li>
 * </ul>
 */
public class DBLogger implements LoggerManager, Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DBLMN";

    // Descriptor de la base de datos a la cual se realiza la conexion
    private static final String DBDESCRIPTOR = "Database";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Tiempo maximo en milisegundos que una conecion puede estar sin usarse antes de ser eliminada
    private static final long MAX_IDLE_TIME = 60000; // 1 minutos

    // Nombre de la tabla donde almacenar la bitacora
    private static final String TABLE_NAME = "DBLogger";

    private static final int QUEUESIZE = 1024;     // Numero de Mensajes
    private static final long QUEUEWRITETIMEOUT = 100;  // Milisegundos
    private static final long QUEUEREADTIMEOUT = 10;  // Milisegundos
    private static final int THREADSLEEP = 500;     // Milisegundos que duerme la hebra

    // Define la mascara del tipo de Evento que se va a Guardar
    public final static int TYPE_ALL = 0xFFFFFFFF;
    // Define la mascara del nivel de registro que se va a Guardar
    public final static int LEVEL_ALL = 0xFFFFFFFF;

    // Los lock para el manejo de concurrencia
    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Generador de numeros aleatorios
    private MersenneTwisterPlus Random;
    // Formateador de datos de tiempo
    private DateFormat dateFormat;

    // Cola FIFO de mensajes
    @GuardedBy("rwl")
    private ArrayBlockingQueue<MessageLog> queue = null;

    // Hebra de Ejecucion
    private Thread Ejecutor = null;
    // Bandera de Ejecucion del Handler
    @GuardedBy("rwl")
    private boolean Ejecutar;
    // Indica si se finalizo la ejecucion de la hebra
    @GuardedBy("rwl")
    private boolean Finalizado = true;
    // Descriptor de la base de datos 
    @GuardedBy("rwl")
    private String dbDescriptor = DBDESCRIPTOR;

    // Instancia de la clase
    @GuardedBy("rwl")
    private static DBLogger instance = null;
    // Define el tipo de Evento que se va a guardar
    @GuardedBy("rwl")
    private int SaveEventType;
    // Define la mascara del nivel de registro que se va a guardar
    @GuardedBy("rwl")
    private int SaveRegLevel;

    /**
     * Constructor privado
     */
    private DBLogger() {

        this.Random = new MersenneTwisterPlus();
        this.SaveEventType = TYPE_ALL;
        this.SaveRegLevel = LEVEL_ALL;
        this.queue = new ArrayBlockingQueue<>(QUEUESIZE);
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SS");
    }

    /**
     * Retorna le Instancia de la clase Singlenton.
     *
     * @return Instancia de la clase
     */
    public static DBLogger getInstance() {

        rwl.writeLock().lock();
        try {
            if (instance == null) {
                instance = new DBLogger();
            }
        } finally {
            rwl.writeLock().unlock();
        }
        return instance;
    }

    /**
     * Metodo privado que inicializa la hebra de escucha del logger
     */
    private void StartThread() {

        // Inicializa la Hebra Principal del Handler
        if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
            Ejecutor = new Thread(this);
            Ejecutor.setName(this.CLASSID + "[" + Long.toHexString(Random.nextLong63()) + "]");
            this.Ejecutor.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    StringWriter StackTrace;
                    long StartTime, DiffTime;
                    StringBuffer Stack;

                    Logger.getLogger(CLASSID + "000").log(Level.SEVERE, "Uncaught Exception\n" + e.toString());
                    Stack = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Stack.append(STE.toString() + "\n");
                        }
                    } else {
                        Stack.append("ERROR: Thread is NULL at StackTrace");
                    }
                    Logger.getLogger(CLASSID + "001").log(Level.SEVERE, "Stack:\n" + Stack.toString());
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    Logger.getLogger(CLASSID + "002").log(Level.SEVERE, "StackTrace:\n" + StackTrace.toString());
                    DiffTime = 0;
                    StartTime = System.currentTimeMillis();
                    while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            Logger.getLogger(CLASSID + "003").log(Level.SEVERE, ex.getMessage());
                        } finally {
                            DiffTime = System.currentTimeMillis() - StartTime;
                        }
                    }
                    if (DiffTime >= TIMEOUT) {
                        Logger.getLogger(CLASSID + "004").log(Level.WARNING, "Thread Finished by TimeOut");
                    }
                    if (Ejecutor != null) {
                        if (Ejecutor.getState() != Thread.State.TERMINATED) {
                            Ejecutor.interrupt();
                            Logger.getLogger(CLASSID + "005").log(Level.WARNING, "Forced Thread Finish by Interruption");
                        }
                        Ejecutor = null;
                    }
                    rwl.writeLock().lock();
                    try {
                        Finalizado = true;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                }
            });
            Ejecutar = true;
            Finalizado = false;
            //Ejecutor.setDaemon(true);
            Ejecutor.start();
        } else {
            Logger.getLogger(CLASSID + "006").log(Level.WARNING, "Can't start Thread because the previous still is Running");
        }
    }

    /**
     * Retorna el descriptor del pool de la base de datos a la cual se conecta
     * la bitacora
     *
     * @return Descriptor de la base de datos
     */
    public String getDBDescriptor() {
        String Local;

        rwl.readLock().lock();
        try {
            Local = this.dbDescriptor;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece le descriptor del pool a la base de datos a la cual se conexta
     * la bitacora
     *
     * @param dbDescriptor Descriptor a la base de datos
     */
    public void setDBDescriptor(String dbDescriptor) {

        rwl.writeLock().lock();
        try {
            this.dbDescriptor = dbDescriptor;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna el identificador de la Clase
     *
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }

    /**
     * Retorna la mascara de banderas del tipo de eventos a guardar
     *
     * @return Banderas del tipo de eventos
     */
    public int getSaveEventTypes() {
        int Local;

        rwl.readLock().lock();
        try {
            Local = SaveEventType;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece las banderas del tipo de evento a salvar
     *
     * Ejemplo: LoggerManager.TYPE_EVENT | LoggerManager.TYPE_ERROR.
     *
     * @param SaveEventType Banderas de tipos de eventos
     */
    public void setSaveEventTypes(int SaveEventType) {

        rwl.writeLock().lock();
        try {
            this.SaveEventType = SaveEventType;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna la mascara de niveles de registros a almacenar.
     *
     * @return Mascara de niveles de registros
     */
    public int getSaveRegLevel() {
        int Local;

        rwl.readLock().lock();
        try {
            Local = SaveRegLevel;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece las banderas de los niveles de registros a almacenar
     *
     * Emeplo: LoggerManager.LEVEL_DEBUG | LoggerManager.LEVEL_WARNING
     *
     * @param SaveRegLevel
     */
    public void setSaveRegLevel(int SaveRegLevel) {

        rwl.writeLock().lock();
        try {
            this.SaveRegLevel = SaveRegLevel;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Envia el mensaje de registro al manejador de bitacoras.
     *
     * NOTE: Cuando se implemente esta funcion debe ser "synchronized" para
     * asegurar que sea thread safe.
     *
     * @param Type Tipo de Evento
     * @param level Nivel del Evento
     * @param Unit Unidad o clase donde se produce el evento
     * @param Messaje Mensaje referente al evento
     */
    @Override
    public void LogMessage(int Type, int level, String Unit, String Messaje) {
        boolean Agregado = false;
        MessageLog msg = null;
        int LType, Llevel;
        // Instante cuando se genero el mensaje
        String dateNow;
        Calendar rightNow;

        rwl.readLock().lock();
        try {
            LType = this.SaveEventType & Type;
            Llevel = this.SaveRegLevel & level;
        } finally {
            rwl.readLock().unlock();
        }
        if ((LType != 0) && (Llevel != 0)) {
            rightNow = Calendar.getInstance();
            dateNow = this.dateFormat.format(rightNow.getTime());
            msg = new MessageLog(Type, level, Unit, dateNow, Messaje);
            do {
                rwl.writeLock().lock();
                try {
                    Agregado = this.queue.offer(msg, QUEUEWRITETIMEOUT, TimeUnit.MILLISECONDS);
                    if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
                        // Inicia la hebra de monitoreo
                        this.StartThread();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.CLASSID + "007").log(Level.SEVERE, ex.getMessage());
                } finally {
                    rwl.writeLock().unlock();
                }
                if (!Agregado) {
                    try {
                        EnvironmentUtils.randomSleep(3 * THREADSLEEP);
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "008").log(Level.SEVERE, ex.getMessage());
                    }
                }
            } while (!Agregado);
        }
    }

    /**
     * Limpia la cola de mensajes del manejador de bitacoras
     */
    public final void Clear() {

        Logger.getLogger(this.CLASSID + "009").log(Level.INFO, "Message Queue Cleaned");
        rwl.writeLock().lock();
        try {
            this.queue.clear();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Finaliza la Ejecucion del manejador de bitacora de forma controlada
     */
    public void Shutdown() {
        boolean lFin;

        rwl.writeLock().lock();
        try {
            this.Ejecutar = false;
        } finally {
            rwl.writeLock().unlock();
        }
        do {
            try {
                EnvironmentUtils.randomSleep(THREADSLEEP);
            } catch (UtilsException ex) {
                Logger.getLogger(this.CLASSID + "010").log(Level.SEVERE, ex.getMessage());
            }
            rwl.readLock().lock();
            try {
                lFin = this.Finalizado;
            } finally {
                rwl.readLock().unlock();
            }
        } while (!lFin);
    }

    /**
     * Finaliza el ejecucion del manejador de bitacora de forma controlada
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {

        this.Shutdown();
        super.finalize();
    }

    /**
     * Realiza el formateo del mensaje y lo guarda en la base de datos.
     */
    private void LogMessage(Connection connection, MessageLog msg) {
        int Type, Nivel;
        String dateNow;
        String Unit, Messaje, insertStatement;
        PreparedStatement statement = null;

        try {
            insertStatement = "INSERT INTO " + TABLE_NAME + " (`when`,`where`,`type`,`level`,`message`) VALUES (?,?,?,?,?);";
            statement = connection.prepareStatement(insertStatement);
            dateNow = msg.getTimeStamp();
            if (dateNow.contains("/")) {
                dateNow = dateNow.replace('/', '-');
            }
            Unit = msg.getUnit();
            Type = msg.getType();
            Nivel = msg.getLevel();
            Messaje = msg.getMessaje();
            statement.setTimestamp(1, Timestamp.valueOf(dateNow));
            statement.setString(2, Unit);
            statement.setString(5, Messaje);
            switch (Type) {
                case LoggerManager.TYPE_EVENT:
                    statement.setString(3, "Event");
                    break;
                case LoggerManager.TYPE_ERROR:
                    statement.setString(3, "Error");
                    break;
                default:
                    statement.setString(3, "Unknown");
            }
            switch (Nivel) {
                case LoggerManager.LEVEL_ERROR:
                    statement.setString(4, "Error");
                    break;
                case LoggerManager.LEVEL_DEBUG:
                    statement.setString(4, "Debug");
                    break;
                case LoggerManager.LEVEL_INFO:
                    statement.setString(4, "Info");
                    break;
                case LoggerManager.LEVEL_TRACE:
                    statement.setString(4, "Trace");
                    break;
                case LoggerManager.LEVEL_WARNING:
                    statement.setString(4, "Warning");
                    break;
                case LoggerManager.LEVEL_FATAL:
                    statement.setString(4, "Fatal");
                    break;
                default:
                    statement.setString(4, "Unknown");
            }
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException | IllegalArgumentException ex) {
            Logger.getLogger(this.CLASSID + "011").log(Level.SEVERE, null, ex);
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException exx) {
                Logger.getLogger(this.CLASSID + "012").log(Level.SEVERE, null, exx);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(this.CLASSID + "013").log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void run() {
        // Copia del mensaje de la cola
        MessageLog Mensaje = null;
        // Copia local de la bandera de ejecucion
        boolean LEjecutar = false;
        // Numero de mensaje que quedan en la cola
        int RemainMsg = 0;
        // Contadores de tiempo
        long StartTime, DiffTime;
        // Pool de conexiones a base de datos
        ConnectionsPoolManager pool;
        // Conexion a la base de datos
        Connection connection = null;

        pool = ConnectionsPoolManager.getInstance();
        StartTime = System.currentTimeMillis();
        do {
            // Recupera los mensajes de la cola
            rwl.readLock().lock();
            try {
                Mensaje = this.queue.poll(QUEUEREADTIMEOUT, TimeUnit.MILLISECONDS);
                RemainMsg = this.queue.size();
                LEjecutar = this.Ejecutar;
            } catch (InterruptedException ex) {
                Logger.getLogger(this.CLASSID + "014").log(Level.SEVERE, ex.getMessage());
            } finally {
                rwl.readLock().unlock();
            }
            // Envia el mensaje para su implesion
            if (Mensaje != null) {
                try {
                    if (connection == null) {
                        // Recupera una conexion a la base de datos del pool
                        connection = pool.checkOut(this.getDBDescriptor());
                    }
                    // Escribe el evento
                    this.LogMessage(connection, Mensaje);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "015").log(Level.SEVERE, null, ex);
                } finally {
                    if ((RemainMsg == 0) && (connection != null)) {
                        // Si no hay mas mensajes se retorna la conexion al pool
                        try {
                            pool.checkIn(connection);
                        } catch (UtilsException ex) {
                            Logger.getLogger(this.CLASSID + "016").log(Level.SEVERE, null, ex);
                        } finally {
                            connection = null;
                        }
                    }
                    Mensaje = null;
                }
            }
            if (RemainMsg <= 0) {
                try {
                    EnvironmentUtils.randomSleep(THREADSLEEP);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "017").log(Level.SEVERE, null, ex);
                }
                DiffTime = System.currentTimeMillis() - StartTime;
                if (DiffTime > DBLogger.MAX_IDLE_TIME) {
                    LEjecutar = false;
                }
            } else {
                StartTime = System.currentTimeMillis();
            }
        } while (LEjecutar);
        rwl.writeLock().lock();
        try {
            if (connection != null) {
                // Se retorna la conexion al pool
                try {
                    pool.checkIn(connection);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "018").log(Level.SEVERE, null, ex);
                } finally {
                    connection = null;
                }
            }
        } finally {
            this.Ejecutor = null;
            this.Finalizado = true;
            rwl.writeLock().unlock();
        }
    }

    /**
     * Clase privada que encapsula el mansaje que se va a guardar en la bitacora
     */
    private class MessageLog {

        private int Type;
        private int Level;
        private String Unit;
        private String Messaje;
        private String TimeStamp;

        /**
         * Constructor por defecto.
         *
         * @param Type Tipo de evento
         * @param Level Nivel del evento
         * @param Unit Unidad donde se genero el error
         * @param TimeStamp Fecha y hora cuando se produjo el evento
         * @param Messaje Mensaje de error
         */
        public MessageLog(int Type, int Level, String Unit, String TimeStamp, String Messaje) {
            this.Type = Type;
            this.Level = Level;
            this.Unit = Unit;
            this.TimeStamp = TimeStamp;
            this.Messaje = Messaje;
        }

        /**
         * Retorna el tipo de evento.
         *
         * @return Tipo de evento
         */
        public int getType() {
            return Type;
        }

        /**
         * Retorna el nivel del evento
         *
         * @return Nivel del evento
         */
        public int getLevel() {
            return Level;
        }

        /**
         * Retorna la unidad donde se produjo el evento
         *
         * @return Unidad donde se produjo el evento
         */
        public String getUnit() {
            return Unit;
        }

        /**
         * Retorna el mensaje de error
         *
         * @return Mensaje de error
         */
        public String getMessaje() {
            return Messaje;
        }

        /**
         * Retorna la Fecha y hora en la que se produjo el evento
         *
         * @return
         */
        public String getTimeStamp() {
            return TimeStamp;
        }

    }
}
