package com.bolivartech.utils.log;

import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.files.FileManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.sort.Sorter;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Copyright 2014 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement the Singlenton
 * BolivarTech's File Logger using text files.</p>
 *
 * <p>
 * Implementa una clase Singlenton que define el manejador de bitacoras usando
 * archivos de texto.</p>
 *
 * <p>
 * Clase del tipo Singleton</p>
 *
 * <ul>
 * <li>Class ID: "35DFLMN"</li>
 * <li>Loc: 000-026</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2014 - April 20, 2016.
 * @version 1.2.0
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (02/05/2014): Version Inicial.</li>
 * <li>v1.0.1 (06/16/2015): Se agrego logs para informar cuando el logger se
 * inicia y se detiene.</li>
 * <li>v1.1.0 (07/12/2015): Se agrego la LLave de Control del Logger para poder
 * hacer shutdown o limpiar la cola de mensajes de manera segura.</li>
 * <li>v1.1.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.2.0 (2016-04-20) Se implemento el uso de hebra de monitoreo dinamica
 * para optimizar los recursos de memoria y procesador cuando el logger excede
 * un TIMEOUT sin recibir mensajes a procesar.</li>
 * </ul>
 */
public class FileLogger implements LoggerManager, Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DFLMN";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Tiempo maximo en milisegundos que una conecion puede estar sin usarse antes de ser eliminada
    private static final long MAX_IDLE_TIME = 60000; // 1 minutos

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

    // Tipo de Entorno
    private final String OS;
    private final String FileSeparador;

    // Instancia de la clase
    @GuardedBy("rwl")
    private static FileLogger instance = null;
    // Define la ruta donde se va a guardar el archivo de la bitacora
    @GuardedBy("rwl")
    private String FilePath;
    // Define el nombre base que va a tener el archivo de la bitacora
    @GuardedBy("rwl")
    private String FileBaseName;
    // Define la bandera donde se indica que se cambuio la ruta o el nombre base del archivo
    @GuardedBy("rwl")
    private boolean ChangedFileBaseName;
    // Define el tipo de Evento que se va a guardar
    @GuardedBy("rwl")
    private int SaveEventType;
    // Define la mascara del nivel de registro que se va a guardar
    @GuardedBy("rwl")
    private int SaveRegLevel;
    // Timestamp de la creadion del ultimo archivo
    @GuardedBy("rwl")
    private long LastCreateFileTime;
    // Threshold de cambio de archivo por tiempo en milisegundos
    @GuardedBy("rwl")
    private long RotateFileTimeThreshold;
    // Threshold de cambio de archivo por tamaño en bytes
    @GuardedBy("rwl")
    private long RotateFileSizeThreshold;
    // El numero de archivos a mantener en la bitacora, menor o igual 0 no realiza la limpieza
    @GuardedBy("rwl")
    private int NumFileToKeep;

    /**
     * Constructor privado con los
     */
    private FileLogger() {

        this.Random = new MersenneTwisterPlus();
        this.OS = EnvironmentUtils.getOS();
        if ((this.OS != null) && (this.OS.toLowerCase(new Locale("en")).contains("windows"))) {
            this.FileSeparador = "\\";
        } else {
            this.FileSeparador = "/";
        }
        this.FilePath = "." + this.FileSeparador;
        this.FileBaseName = "BolivarTech_FileLogger";
        this.ChangedFileBaseName = false;
        this.SaveEventType = TYPE_ALL;
        this.SaveRegLevel = LEVEL_ALL;
        this.queue = new ArrayBlockingQueue<>(QUEUESIZE);
        this.dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        this.LastCreateFileTime = 0;
        this.RotateFileTimeThreshold = 3600000; // 60 minutos 
        this.RotateFileSizeThreshold = 10485760; // 10 Megabytes
        this.NumFileToKeep = 10;
    }

    /**
     * Retorna le Instancia de la clase Singlenton.
     *
     * @return Instancia de la clase
     */
    public static FileLogger getInstance() {

        rwl.writeLock().lock();
        try {
            if (instance == null) {
                instance = new FileLogger();
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
     * Retorna el identificador de la Clase
     *
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }

    /**
     * Establece el descriptor del archivo donde se va a almaceran la bitacora.
     *
     * @param FilePath Ruta donde se almacena la bitacora
     * @param FileBaseName Nombre base del archivo
     */
    public void setFullFileDescriptor(String FilePath, String FileBaseName) {

        // Verifica si el Path esta correcto
        if (!FilePath.substring(FilePath.length() - 1, FilePath.length()).equalsIgnoreCase(this.FileSeparador)) {
            FilePath += this.FileSeparador;
        }
        rwl.writeLock().lock();
        try {
            this.FilePath = FilePath;
            this.FileBaseName = FileBaseName;
            this.ChangedFileBaseName = true;
        } finally {
            rwl.writeLock().unlock();
        }

    }

    /**
     * Retorna la ruta donde estan almacenados los archivos de bitacoras
     *
     * @return
     */
    public String getFilePath() {
        String Local;

        rwl.readLock().lock();
        try {
            Local = FilePath;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece la ruta donde estan almacenados los archivos de bitacoras
     *
     * @param FilePath
     */
    public void setFilePath(String FilePath) {

        // Verifica si el Path esta correcto
        if (!FilePath.substring(FilePath.length() - 1, FilePath.length()).equalsIgnoreCase(this.FileSeparador)) {
            FilePath += this.FileSeparador;
        }
        rwl.writeLock().lock();
        try {
            this.FilePath = FilePath;
            this.ChangedFileBaseName = true;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna el nombre base del archivo de bitacoras
     *
     * @return Nombre base del archivo de bitacoras
     */
    public String getFileBaseName() {
        String Local;

        rwl.readLock().lock();
        try {
            Local = FileBaseName;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece el nombre base del archivo de bitacoras
     *
     * @param FileBaseName Nombre base del archivo
     */
    public void setFileBaseName(String FileBaseName) {

        rwl.writeLock().lock();
        try {
            this.FileBaseName = FileBaseName;
            this.ChangedFileBaseName = true;
        } finally {
            rwl.writeLock().unlock();
        }
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
     * Retorna el umbral de tiempo para la rotacion de los archivos en minutos
     *
     * @return Umbral de tiempo de rotacion en minutos
     */
    public long getRotateFileTimeThreshold() {
        long Local;

        rwl.readLock().lock();
        try {
            Local = RotateFileTimeThreshold;
        } finally {
            rwl.readLock().unlock();
        }
        Local /= 60000; // Comvierte los milisegundos en minutos
        return Local;
    }

    /**
     * Establece el umbrar de tiempo para lo rotacion de los archivos en minutos
     *
     * @param RotateFileTimeThreshold Umbral de rotacion en minutos
     */
    public void setRotateFileTimeThreshold(long RotateFileTimeThreshold) {

        RotateFileTimeThreshold *= 60000; // Conveirte los minutos en milisegundos
        rwl.writeLock().lock();
        try {
            this.RotateFileTimeThreshold = RotateFileTimeThreshold;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna el umbrar del tamaño del archivo para la rotacion en MB
     *
     * @return Umbrar del tamaño de archivo en MB
     */
    public long getRotateFileSizeThreshold() {
        long Local;

        rwl.readLock().lock();
        try {
            Local = RotateFileSizeThreshold;
        } finally {
            rwl.readLock().unlock();
        }
        Local /= 1048576;  // Comvierte los bytes en Megabytes
        return Local;
    }

    /**
     * Establece el umbrar del tamaño del archivo para la rotacion en MB
     *
     * @param RotateFileSizeThreshold Umbrar del tamaño del archivo en MB
     */
    public void setRotateFileSizeThreshold(long RotateFileSizeThreshold) {

        RotateFileSizeThreshold *= 1048576; // Convierte los MB en bytes 
        rwl.writeLock().lock();
        try {
            this.RotateFileSizeThreshold = RotateFileSizeThreshold;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna la cantidad de archivos a mantener en la bitacora, si el valor es
     * menor a 1, se almacenan todos los archivos
     *
     * @return Numero de archivos a mantener en la bitacora
     */
    public int getNumFileToKeep() {
        int Local;

        rwl.readLock().lock();
        try {
            Local = NumFileToKeep;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece la cantidad de archivos a mantener en la bitacora, si el valor
     * es menor a 1 se almacenan todos los archivos.
     *
     * @param NumFileToKeep Numero de archivos a mantener en la bitacora
     */
    public void setNumFileToKeep(int NumFileToKeep) {

        rwl.writeLock().lock();
        try {
            this.NumFileToKeep = NumFileToKeep;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Envia el mensaje de registro al manejador de bitacoras.
     *
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
        // Copia local de la bandera de ejecucion
        boolean LEjecutar;
        // Instante cuando se genero el mensaje
        String dateNow;
        DateFormat formatter;
        Calendar rightNow;

        rwl.readLock().lock();
        try {
            LType = this.SaveEventType & Type;
            Llevel = this.SaveRegLevel & level;
            LEjecutar = this.Ejecutar;
        } finally {
            rwl.readLock().unlock();
        }
        if ((LType != 0) && (Llevel != 0)) {
            formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SS");
            rightNow = Calendar.getInstance();
            dateNow = formatter.format(rightNow.getTime());
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
        boolean lFin = true;

        rwl.writeLock().lock();
        try {
            this.Ejecutar = false;
        } finally {
            rwl.writeLock().unlock();
        }
        do {
            rwl.readLock().lock();
            try {
                lFin = this.Finalizado;
            } finally {
                rwl.readLock().unlock();
            }
            if (!lFin) {
                try {
                    EnvironmentUtils.randomSleep(THREADSLEEP);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "010").log(Level.SEVERE, ex.getMessage());
                }
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
     * Verifica para mantener el numero de archivo a convervar
     */
    private void VerifyFilesToKeep() {
        // Manejador de archivo
        FileManager Directorio = null;
        FileManager[] Archivos = null;
        // Nombre base y ruta del archivo
        String ArchPath = null;
        // Copia local del numero de archivos a mantener
        int LNumFileToKeep;
        // Contador generico
        int i;

        // Filtro del Archivo
        FilenameFilter LogFilter = (Dir, Name) -> {
            if (Name.contains(FileBaseName.subSequence(0, FileBaseName.length()))) {
                return true;
            } else {
                return false;
            }
        };
        // Recupera los archivos locales
        rwl.readLock().lock();
        try {
            ArchPath = this.FilePath;
            LNumFileToKeep = this.NumFileToKeep;
        } finally {
            rwl.readLock().unlock();
        }
        if (LNumFileToKeep > 0) {
            // Verifica si el Path apunta al directorio
            if (ArchPath.substring(ArchPath.length() - 1, ArchPath.length()).equalsIgnoreCase(this.FileSeparador)) {
                rwl.writeLock().lock();
                try {
                    ArchPath = ArchPath.substring(0, ArchPath.length() - 1);
                } finally {
                    rwl.writeLock().unlock();
                }
            }
            Directorio = new FileManager(ArchPath);
            if (Directorio.isDirectory()) {
                // Recupera la lista de archivos contenidos en el directorio
                try {
                    Archivos = Directorio.ListDirectory(LogFilter);
                    Sorter.RelativeSort(Archivos, Sorter.DESCENDING);
                    if (Archivos.length > LNumFileToKeep) {
                        for (i = LNumFileToKeep; i < Archivos.length; i++) {
                            if (Archivos[i].Exists()) {
                                Archivos[i].Delete();
                            }
                        }
                    }
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "011").log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(this.CLASSID + "012").log(Level.WARNING, ArchPath + " NOT is a directory");
            }
        }
    }

    /**
     * Retorna el nombre base y la ruta del archivo
     *
     * @return nombre base y la ruta del archivo
     */
    private String getCanonicalFileBase() {
        // Nombre base y ruta del archivo
        String ArchName = null;

        // Crea el archivo base
        rwl.readLock().lock();
        try {
            ArchName = this.FilePath;
        } finally {
            rwl.readLock().unlock();
        }
        // Verifica si el Path esta correcto
        if (!ArchName.substring(ArchName.length() - 1, ArchName.length()).equalsIgnoreCase(this.FileSeparador)) {
            rwl.writeLock().lock();
            try {
                this.FilePath += this.FileSeparador;
                ArchName = this.FilePath;
            } finally {
                rwl.writeLock().unlock();
            }
        }
        rwl.readLock().lock();
        try {
            ArchName += this.FileBaseName;
        } finally {
            rwl.readLock().unlock();
        }
        return ArchName;
    }

    /**
     * Realiza el formateo del mensaje a guardar en el archivo
     */
    private String FormatMessage(MessageLog msg) {
        StringBuffer Salida;
        int Type, Level;
        String dateNow;

        Type = msg.getType();
        Level = msg.getLevel();
        dateNow = msg.getTimeStamp();
        Salida = new StringBuffer("++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        Salida = Salida.append("When: " + dateNow + "\n");
        Salida = Salida.append("Where: " + msg.getUnit() + "\n");
        Salida = Salida.append("Type: ");
        switch (Type) {
            case LoggerManager.TYPE_EVENT:
                Salida = Salida.append("Event\n");
                break;
            case LoggerManager.TYPE_ERROR:
                Salida = Salida.append("Error\n");
                break;
            default:
                Salida = Salida.append("Unknow\n");
        }
        Salida = Salida.append("Level: ");
        switch (Level) {
            case LoggerManager.LEVEL_TRACE:
                Salida = Salida.append("Trace\n");
                break;
            case LoggerManager.LEVEL_DEBUG:
                Salida = Salida.append("Debug\n");
                break;
            case LoggerManager.LEVEL_INFO:
                Salida = Salida.append("Info\n");
                break;
            case LoggerManager.LEVEL_WARNING:
                Salida = Salida.append("Warning\n");
                break;
            case LoggerManager.LEVEL_ERROR:
                Salida = Salida.append("Error\n");
                break;
            case LoggerManager.LEVEL_FATAL:
                Salida = Salida.append("Fatal\n");
                break;
            default:
                Salida = Salida.append("Unknow\n");
        }
        Salida = Salida.append("Message: " + msg.getMessaje() + "\n");
        Salida = Salida.append("------------------------------------------------------\n");
        return Salida.toString();
    }

    @Override
    public void run() {
        // Manejador de archivo
        FileManager Archivo = null;
        // Nombre base y ruta del archivo
        String ArchName = null;
        // Copia del mensaje de la cola
        MessageLog Mensaje = null;
        // Copia local de la bandera de ejecucion
        boolean LEjecutar = false;
        // Copia local de la bandera de cambio de archivo
        boolean LChangedFileBaseName = false;
        // Tiempo Actual
        Calendar rightNow;
        // Copia local de los Thresholds
        long RotFTThr, RotFSThr;
        // Copia local del tiempo de creacion del archivo actual
        long LocLastCreateFileTime;
        // Mensaje Formateado para ser guardado en el archivo
        String MsgFrmtd;
        // Numero de mensaje que quedan en la cola
        int RemainMsg;
        // Contadores de tiempo
        long StartTime, DiffTime;

        // Crea el archivo base
        ArchName = this.getCanonicalFileBase();
        RotFTThr = 0;
        RotFSThr = 0;
        LocLastCreateFileTime = 0;
        RemainMsg = 0;
        StartTime = System.currentTimeMillis();
        // Inicializa el Loop de Ejecucion del Handler
        do {
            rwl.readLock().lock();
            try {
                Mensaje = this.queue.poll(QUEUEREADTIMEOUT, TimeUnit.MILLISECONDS);
                RemainMsg = this.queue.size();
                LEjecutar = this.Ejecutar;
                RotFTThr = this.RotateFileTimeThreshold;
                RotFSThr = this.RotateFileSizeThreshold;
                LocLastCreateFileTime = this.LastCreateFileTime;
                LChangedFileBaseName = this.ChangedFileBaseName;
            } catch (InterruptedException ex) {
                Logger.getLogger(this.CLASSID + "013").log(Level.SEVERE, ex.getLocalizedMessage());
            } finally {
                rwl.readLock().unlock();
            }
            if (LChangedFileBaseName) {
                ArchName = this.getCanonicalFileBase();
                if (Archivo != null) {
                    try {
                        Archivo.Close();
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "014").log(Level.SEVERE, null, ex);
                    } finally {
                        Archivo.Delete();
                        Archivo = null;
                    }
                }
                rwl.writeLock().lock();
                try {
                    this.ChangedFileBaseName = false;
                    LChangedFileBaseName = this.ChangedFileBaseName;
                } finally {
                    rwl.writeLock().unlock();
                }
            }
            if (Mensaje != null) {
                rightNow = Calendar.getInstance();
                // Verifica si el archivo se debe rotar por tiempo
                if ((Archivo != null) && (Math.abs(rightNow.getTimeInMillis() - LocLastCreateFileTime) > RotFTThr)) {
                    // Se rota el archivo por tiempo
                    try {
                        Archivo.Close();
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "015").log(Level.SEVERE, null, ex);
                    }
                    do {
                        Archivo = new FileManager(ArchName + "-" + this.dateFormat.format(rightNow.getTime()) + ".log");
                        if (Archivo.Exists()) {
                            // Espera por un segundo para crear un archivo que NO exista
                            try {
                                Thread.sleep(1100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(this.CLASSID + "016").log(Level.SEVERE, ex.getMessage());
                            }
                            rightNow = Calendar.getInstance();
                        }
                    } while (Archivo.Exists());
                    rwl.writeLock().lock();
                    try {
                        this.LastCreateFileTime = rightNow.getTimeInMillis();
                        LocLastCreateFileTime = this.LastCreateFileTime;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    try {
                        Archivo.Open(FileManager.WRITE, false);
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "017").log(Level.SEVERE, null, ex);
                    }
                    // Verifica el numero de archivos a mantener
                    this.VerifyFilesToKeep();
                } else if ((Archivo != null) && (Archivo.getFileLength() > RotFSThr)) {
                    // Se rota el archivo por tamaño
                    try {
                        Archivo.Close();
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "018").log(Level.SEVERE, null, ex);
                    }
                    do {
                        Archivo = new FileManager(ArchName + "-" + this.dateFormat.format(rightNow.getTime()) + ".log");
                        if (Archivo.Exists()) {
                            // Espera por un segundo para crear un archivo que NO exista
                            try {
                                Thread.sleep(1100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(this.CLASSID + "019").log(Level.SEVERE, ex.getMessage());
                            }
                        }
                    } while (Archivo.Exists());
                    rwl.writeLock().lock();
                    try {
                        this.LastCreateFileTime = rightNow.getTimeInMillis();
                        LocLastCreateFileTime = this.LastCreateFileTime;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    try {
                        Archivo.Open(FileManager.WRITE, false);
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "020").log(Level.SEVERE, null, ex);
                    }
                    // Verifica el numero de archivos a mantener
                    this.VerifyFilesToKeep();
                } else if (Archivo == null) {
                    // Por algun motivo el archivo es NULL, se corrige la situacion
                    do {
                        Archivo = new FileManager(ArchName + "-" + this.dateFormat.format(rightNow.getTime()) + ".log");
                        if (Archivo.Exists()) {
                            // Espera por un segundo para crear un archivo que NO exista
                            try {
                                Thread.sleep(1100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(this.CLASSID + "021").log(Level.SEVERE, ex.getMessage());
                            }
                            rightNow = Calendar.getInstance();
                        }
                    } while (Archivo.Exists());
                    rwl.writeLock().lock();
                    try {
                        this.LastCreateFileTime = rightNow.getTimeInMillis();
                        LocLastCreateFileTime = this.LastCreateFileTime;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    try {
                        Archivo.Open(FileManager.WRITE, false);
                    } catch (UtilsException ex) {
                        Logger.getLogger(this.CLASSID + "022").log(Level.SEVERE, null, ex);
                    }
                    // Verifica el numero de archivos a mantener
                    this.VerifyFilesToKeep();
                }
                // Escribe en el archivo el evento
                MsgFrmtd = this.FormatMessage(Mensaje);
                try {
                    Archivo.Write(MsgFrmtd.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(this.CLASSID + "023").log(Level.SEVERE, null, ex);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "024").log(Level.SEVERE, null, ex);
                }
                Mensaje = null;
            }
            if (RemainMsg <= 0) {
                try {
                    EnvironmentUtils.randomSleep(THREADSLEEP);
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "025").log(Level.SEVERE, null, ex);
                }
                DiffTime = System.currentTimeMillis() - StartTime;
                if (DiffTime > FileLogger.MAX_IDLE_TIME) {
                    LEjecutar = false;
                }
            } else {
                StartTime = System.currentTimeMillis();
            }
        } while (LEjecutar);
        rwl.writeLock().lock();
        try {
            if (Archivo != null) {
                try {
                    Archivo.Close();
                } catch (UtilsException ex) {
                    Logger.getLogger(this.CLASSID + "026").log(Level.SEVERE, null, ex);
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
