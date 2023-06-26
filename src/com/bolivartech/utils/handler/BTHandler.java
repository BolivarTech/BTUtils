package com.bolivartech.utils.handler;

import com.bolivartech.utils.converters.Base64;
import com.bolivartech.utils.kerneltasks.BTTask;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerFormatter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Copyright 2014,2015,2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement BolivarTech's Messages
 * Handler to communicate between thread.</p>
 *
 * <p>
 * Implementa una clase que define el manejador de mensaje usando para la
 * comunicacion entre hebras.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHN"</li>
 * <li>Loc: 000-040</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @version 1.7.0 - April 20, 2016
 * @since 2014
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2014-12-12): Version Inicial.</li>
 * <li>v1.0.1 (2014-12-27): Se agrego la capasidad de reenviar a la cola los
 * runnables rechazados por el pool de ejecucion.</li>
 * <li>v1.2.0 (2015-01-19): Se agrego la capasidad de enviar mensajes y post en
 * un tiempo especifico en el futuro.</li>
 * <li>v1.3.0 (2015-05-30): Se agrego el HandlerID para identificar cada
 * manejador.</li>
 * <li>v1.3.1 (2015-06-16): Se agregaron entradas al Log para cuando el handler
 * inicia o se detiene.</li>
 * <li>v1.5.0 (2015-07-02): Se agregaron el soporte de prioritizacion de los
 * mensajes recibidos por BTHandler.</li>
 * <li>v1.5.1 (2015-07-05): Se agregaron el soporte de prioritizacion de las
 * tareas recibidas por BTHandler.</li>
 * <li>v1.5.2 (2015-08-05): Se agrego la LLave de Control del Handler para poder
 * hacer shutdown o limpiar la cola de mensajes de manera segura.</li>
 * <li>v1.6.1 (2015-04-08): Se optimizo el uso de la cola de mensajes para los
 * envios diferidos.</li>
 * <li>v1.6.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.6.3 (2016-03-28) Se implemento el uso del LoggerFormatter para la
 * bitacora.</li>
 * <li>v1.7.0 (2016-04-20) Se implemento el uso del MessageHandler para procesar
 * los mensajes recibidos; se implemento el uso de hebra de monitoreo dinamica
 * para optimizar los recursos de memoria y procesador cuando el handler excede
 * un TIMEOUT sin recibir mensajes a procesar.</li>
 * </ul>
 */
public class BTHandler implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHN";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Tiempo maximo en milisegundos que puede estar el handler sin usarse antes de apagar la hebra de monitoreo
    private static final long MAX_IDLE_TIME = 60000; // 1 minutos

    private static final int THREADSLEEP = 500;     // Milisegundos que duerme la hebra

    private static final int QUEUESIZE = 1024;     // Numero de Mensajes
    private static final long QUEUEWRITETIMEOUT = 100;  // Milisegundos
    private static final long QUEUEREADTIMEOUT = 10;  // Milisegundos

    // Maximo tiempo que una habra adicional al core puede estar idle
    private final static int DEFMAXIMUMPOOLSIZEKEEPALIVETIME = 5; // en Segundos

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Manejador de Bitacoras
    private final LoggerFormatter BTLogF;
    // Bandera de Ejecucion del Handler
    private boolean Ejecutar;
    // Identificador del Handler que es definido durante su inicializacion
    private long HandlerID;

    // Cola FIFO de mensajes
    @GuardedBy("rwl")
    private PriorityBlockingQueue<BTMessage> queue = null;

    // Bandera para indicar si se finalizo el handler
    @GuardedBy("rwl")
    private boolean isFinished = true;

    // Hebra de Ejecucion
    private Thread Ejecutor = null;
    // LLave de contro del Handler
    private String ControlKey = null;

    // Manejador de mensajes
    private BTMessageHandler MsgHandler;

    //creating the ThreadPoolExecutor
    private ThreadPoolExecutor executorPool = null;
    // Tamano del nucleo de Threads
    private int ThreadCorePoolSize;
    // Tamano del nucleo de Threads
    private int MaxThreadCorePoolSize;
    // Numero de CPUs
    private int NumCPUs;

    /**
     * Constructor por defecto
     */
    public BTHandler() {
        this(QUEUESIZE, null);
    }

    /**
     * Constructor con inicializacion de la bitacora
     *
     * @param vLog Apuntador a la Bitacora
     */
    public BTHandler(LoggerManager vLog) {
        this(QUEUESIZE, vLog);
    }

    /**
     * Constructor con inicializacion de la bitacora y tamaño de la cola FIFO
     *
     * @param Size Tamaño de la cola FIFO
     * @param vLog Apuntador a la Bitacora
     */
    public BTHandler(int Size, LoggerManager vLog) {
        MersenneTwisterPlus Random;

        this.BTLogF = LoggerFormatter.getInstance(vLog);
        this.ControlKey = null;
        this.NumCPUs = Runtime.getRuntime().availableProcessors();
        this.MaxThreadCorePoolSize = this.NumCPUs;
        this.ThreadCorePoolSize = this.NumCPUs / 2;
        this.ThreadCorePoolSize = (this.ThreadCorePoolSize > 0 ? this.ThreadCorePoolSize : 1);
        if (Size < 1) {
            queue = new PriorityBlockingQueue<BTMessage>(QUEUESIZE);
        } else {
            queue = new PriorityBlockingQueue<BTMessage>(Size);
        }
        Random = new MersenneTwisterPlus();
        this.HandlerID = Random.nextLong63();
        this.MsgHandler = null;
    }

    /**
     * Metodo privado que inicializa la hebra de escucha del handler
     */
    private final void StartThread() {

        // Inicializa la Hebra Principal del Handler
        if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
            Ejecutor = new Thread(this);
            Ejecutor.setName(this.CLASSID + "[" + Long.toHexString(this.HandlerID) + "]");
            Ejecutor.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    StringWriter StackTrace;
                    long StartTime, DiffTime;
                    LoggerFormatter BTLogF;
                    StringBuffer Stack;

                    BTLogF = LoggerFormatter.getInstance(null);
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Uncaught Exception\n" + e.toString(), BTHandler.CLASSID, "000");
                    Stack = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Stack.append(STE.toString() + "\n");
                        }
                    } else {
                        Stack.append("ERROR: Thread is NULL at StackTrace");
                    }
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Stack:\n" + Stack.toString(), BTHandler.CLASSID, "001");
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "StackTrace:\n" + StackTrace.toString(), BTHandler.CLASSID, "002");
                    // Finaliza el pool de hebras de ejecucion
                    try {
                        if (executorPool != null) {
                            executorPool.shutdown();
                            if (executorPool.awaitTermination(DEFMAXIMUMPOOLSIZEKEEPALIVETIME, TimeUnit.SECONDS)) {
                                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Finished", BTHandler.CLASSID, "003");
                            } else {
                                executorPool.shutdownNow();
                                if (executorPool.awaitTermination(DEFMAXIMUMPOOLSIZEKEEPALIVETIME, TimeUnit.SECONDS)) {
                                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Forced to Finished", BTHandler.CLASSID, "004");
                                } else {
                                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool NO Finished before TimeOut", BTHandler.CLASSID, "005");
                                }
                            }
                        } else {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool is NULL", BTHandler.CLASSID, "006");
                        }
                    } catch (InterruptedException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "007");
                    } catch (SecurityException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "008");
                    } finally {
                        executorPool = null;
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Forced Thread Pool Finish by Interruption", BTHandler.CLASSID, "009");
                    }
                    // Finaliza el Handler
                    rwl.writeLock().lock();
                    try {
                        Ejecutar = false;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    DiffTime = 0;
                    StartTime = System.currentTimeMillis();
                    while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "010");
                        } finally {
                            DiffTime = System.currentTimeMillis() - StartTime;
                        }
                    }
                    if (DiffTime >= TIMEOUT) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Handler Finished by TimeOut", BTHandler.CLASSID, "011");
                    }
                    if (Ejecutor != null) {
                        if (Ejecutor.getState() != Thread.State.TERMINATED) {
                            Ejecutor.interrupt();
                            Ejecutor = null;
                        }

                    }
                    rwl.writeLock().lock();
                    try {
                        isFinished = true;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                }
            }
            );
            this.isFinished = false;
            this.Ejecutar = true;
            this.Ejecutor.start();
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Can't start Thread because the previous still is Running", BTHandler.CLASSID, "012");
        }
    }

    /**
     * Metodo privado que genera la llave aleatoria de control del Handler, la
     * cual se debe usar para hacerle Shutdown o Clear de la cola de mensajes.
     *
     * @return Llave aleatoria de control.
     */
    private String KeyGenerator() {
        String Llave = null;
        MersenneTwisterPlus Random;
        byte[] BinaryKey;

        Random = new MersenneTwisterPlus();
        BinaryKey = new byte[128];
        Random.nextBytes(BinaryKey);
        try {
            Llave = Base64.encodeBytes(BinaryKey, Base64.URL_SAFE);
        } catch (IOException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "013");
        }
        return Llave;
    }

    /**
     * Retorna la llave aleatoria de control del BTHandler, esta llave permite
     * hacer shutdown del o limpiar la cola de mensajes del BTHandler.
     *
     * La llave de control del BTHandler se establece LA PRIMERA VEZ que se
     * llama este metodo SE ENTREGA LA LLAVE DE CONTROL VALIDA, POP SEGURIDAD
     * LAS SUBSECUENTES LLAMADAS SE ENTREGAN LLAVES DUMMIES NO VALIDAS PARA
     * CONTROLAR EL BTHandler.
     *
     * WARNING: Por seguridad se recomienda llamar este metodo apenas se realiza
     * la instanciacion del BTHandler con el objeto de establecer la llave de
     * control de una forma temprana.
     *
     * NOTE: Si este metodo no es llamado la LLave De Control por defecto es un
     * apuntador NULL.
     *
     * @return Llave aleatoria de control.
     */
    public final String getControlKey() {
        String Llave;

        Llave = this.KeyGenerator();
        if (this.ControlKey == null) {
            this.ControlKey = new String(Llave);
        }
        return Llave;
    }

    /**
     * Retorna TRUE si finalizo el BTHandler y FALSE si no se ha finalizado.
     *
     * @return TRUE si se finalizo y FALSE si NO
     */
    public boolean isFinished() {
        boolean Local = true;

        rwl.readLock().lock();
        try {
            Local = this.isFinished;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
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
     * Retorna el ID del Handler que fue asignado de forma aleatoria durante su
     * inicializacion.
     *
     * @return HandlerID
     */
    public final long getHandlerID() {
        long Local = 0;

        rwl.readLock().lock();
        try {
            Local = this.HandlerID;
        } finally {
            rwl.readLock().unlock();
        }
        return Local;
    }

    /**
     * Establece el manejador de mensajes entrantes al handler
     *
     * @param MsgHandler Manejador de mensajes
     */
    public final void setMessageHandler(BTMessageHandler MsgHandler) {

        rwl.writeLock().lock();
        try {
            this.MsgHandler = MsgHandler;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Envia el mensaje a la cola del manejador retornando TRUE si lo logro
     * agregar o FALSE si no.
     *
     * @param msg Mensaje a agregar a la cola
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean sendMessage(BTMessage msg) {
        boolean Salida = false;

        if (msg != null) {
            rwl.writeLock().lock();
            try {
                Salida = this.queue.offer(msg, QUEUEWRITETIMEOUT, TimeUnit.MILLISECONDS);
                if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
                    // Inicia la hebra de monitoreo
                    this.StartThread();
                }
            } catch (ClassCastException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "014");
            } catch (NullPointerException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "015");
            } finally {
                rwl.writeLock().unlock();
            }
        }
        return Salida;
    }

    /**
     * Envia el mensaje a la cola del manejador para ser procesado en el momento
     * indicado por "uptimeMillis" en milisegundos a partir de la media noche
     * del 1 de Enero de 1970.; retornando TRUE si lo logro agregar o FALSE si
     * no.
     *
     * @param msg Mensaje a agregar a la cola
     * @param uptimeMillis milisegundos a partir de la media noche del 1 de
     * Enero de 1970.
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean sendMessageAtTime(BTMessage msg, long uptimeMillis) {
        boolean Salida = false;
        DelayedRunManager DelayManager;

        if (msg != null) {
            DelayManager = new DelayedRunManager(msg, uptimeMillis, this.BTLogF.getBTLoggerManager());
            DelayManager.setPriority(msg.getPriority());
            Salida = this.Post(DelayManager);
        }
        return Salida;
    }

    /**
     * Envia el mensaje a la cola del manejador para ser procesado en
     * "delayMillis" milisegundos a partir del momento actual; retornando TRUE
     * si lo logro agregar o FALSE si no.
     *
     * @param msg Mensaje a agregar a la cola
     * @param delayMillis milisegundos de retrazo a partir del momento actual.
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean sendMessageDelayed(BTMessage msg, long delayMillis) {
        long currentTime;

        currentTime = System.currentTimeMillis();
        return sendMessageAtTime(msg, delayMillis + currentTime);
    }

    /**
     * Envia el BTTask a la cola del manejador retornando TRUE si lo logro
     * agregar o FALSE si no.
     *
     * El BTTask se ejecuta en una hebra separada de
     *
     * @param r BTTask a agregar a la cola
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean Post(BTTask r) {
        BTMessage Mensaje = null;
        boolean Salida = false;

        if (r != null) {
            Mensaje = new BTMessage();
            Mensaje.Ejecutable = r;
            Mensaje.setPriority(r.getPriority());
            // Para el caso que sea un mesaje de entreda postergada le asigna el mensaje contenedor el
            // timestamp en el que se deberia ejecutar para optimizar el uso de las colas de mensajes
            if (DelayedRunManager.class.isInstance(r)) {
                Mensaje.setTimeStamp(((DelayedRunManager) r).getUptimeMillis());
            }
            Salida = this.sendMessage(Mensaje);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "BTTask is NULL", BTHandler.CLASSID, "016");
        }
        return Salida;
    }

    /**
     * Envia el BTTask a la cola del manejador para ser procesado en el momento
     * indicado por "uptimeMillis" en milisegundos a partir de la media noche
     * del 1 de Enero de 1970.; retornando TRUE si lo logro agregar o FALSE si
     * no.
     *
     * @param r BTTask a agregar a la cola
     * @param uptimeMillis milisegundos a partir de la media noche del 1 de
     * Enero de 1970.
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean postAtTime(BTTask r, long uptimeMillis) {
        BTMessage Mensaje = null;
        boolean Salida = false;

        if (r != null) {
            Mensaje = new BTMessage();
            Mensaje.Ejecutable = r;
            Mensaje.setPriority(r.getPriority());
            Salida = this.sendMessageAtTime(Mensaje, uptimeMillis);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "BTTask is NULL", BTHandler.CLASSID, "017");
        }
        return Salida;
    }

    /**
     * Envia el BTTask a la cola del manejador para ser procesado en
     * "delayMillis" milisegundos a partir del momento actual; retornando TRUE
     * si lo logro agregar o FALSE si no.
     *
     * @param r BTTask a agregar a la cola
     * @param delayMillis milisegundos de retrazo a partir del momento actual.
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean postDelayed(BTTask r, long delayMillis) {
        BTMessage Mensaje = null;
        boolean Salida = false;

        if (r != null) {
            Mensaje = new BTMessage();
            Mensaje.Ejecutable = r;
            Mensaje.setPriority(r.getPriority());
            Salida = this.sendMessageDelayed(Mensaje, delayMillis);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "BTTask is NULL", BTHandler.CLASSID, "018");
        }
        return Salida;
    }

    /**
     * Retorna un mensaje vacio para ser enviado al handler
     *
     * @return Mensaje vacio
     */
    public final static BTMessage getEmptyMessage() {
        BTMessage Mensaje = null;

        Mensaje = new BTMessage();
        return Mensaje;
    }

    /**
     * Retorna un mensaje vacio para ser enviado al handler, pero en el cual se
     * agrego el apuntador al BTHandler que envio el mensaje para que el
     * destinatario pueda responder el mensaje.
     *
     * @return Mensaje vacio con apuntador al BTHandler que lo envia
     */
    public final BTMessage getReturnableEmptyMessage() {
        BTMessage Mensaje = null;

        Mensaje = new BTMessage(this);
        return Mensaje;
    }

    /**
     * Limpia la cola de mensajes del Handler
     *
     * @param CtrlKey Llave de control del BTHandler
     */
    public final void Clear(String CtrlKey) {

        if (Ejecutor != null) {
            if ((this.ControlKey == null) || (this.ControlKey.equals(CtrlKey))) {
                rwl.writeLock().lock();
                try {
                    this.queue.clear();
                    this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Message Queue Cleaned", BTHandler.CLASSID, "019");
                } finally {
                    rwl.writeLock().unlock();
                }
            } else {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Message Queue Clean Failed due Control Key MISMATCH", BTHandler.CLASSID, "020");
            }
        }
    }

    /**
     * Finaliza la Ejecucion del handler de forma controlada
     *
     * @param CtrlKey Llave de control del BTHandler
     */
    public final void Shutdown(String CtrlKey) {

        if ((this.ControlKey == null) || (this.ControlKey.equals(CtrlKey))) {
            rwl.writeLock().lock();
            try {
                this.Ejecutar = false;
                this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Shutdown Started", BTHandler.CLASSID, "021");
            } finally {
                rwl.writeLock().unlock();
            }
        }
    }

    /**
     * Finaliza el ejecucion del handler de forma controlada
     *
     * @throws Throwable
     */
    @Override
    protected final void finalize() throws Throwable {

        try {
            if (Ejecutor != null) {
                rwl.writeLock().lock();
                try {
                    this.Ejecutar = false;
                    this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Finalize Started", BTHandler.CLASSID, "022");
                } finally {
                    rwl.writeLock().unlock();
                }
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Process received messages.
     *
     * @param msg
     */
    private void handleMessage(BTMessage msg) {

        if (this.MsgHandler != null) {
            this.MsgHandler.handleMessage(msg);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "BTMessageHandler NEED to be defined to handel incoming messages at BTHandler[" + Long.toHexString(this.HandlerID) + "]", BTHandler.CLASSID, "023");
            throw new UnsupportedOperationException("BTMessageHandler NEED to be defined to handel incoming messages at BTHandler[" + Long.toHexString(this.HandlerID) + "]");
        }
    }

    // Inicializa el Pool de Threads de ejecucion
    private void InitThreadPool() {
        //RejectedExecutionHandler implementation
        RejectedExecutionHandlerImpl rejectionHandler;
        //the ThreadFactory implementation to use
        ThreadFactory threadFactory;

        //Get the ThreadFactory implementation to use
        threadFactory = Executors.defaultThreadFactory();
        //RejectedExecutionHandler implementation
        rejectionHandler = new RejectedExecutionHandlerImpl();
        //creating the ThreadPoolExecutor
        this.executorPool = new ThreadPoolExecutor(this.ThreadCorePoolSize, this.MaxThreadCorePoolSize, DEFMAXIMUMPOOLSIZEKEEPALIVETIME, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(10 * this.MaxThreadCorePoolSize), threadFactory, rejectionHandler);
        // Permite que las hebras del Core tambien tengan timeout
        this.executorPool.allowCoreThreadTimeOut(true);
    }

    // Finaliza el pool de Thread de ejecucion
    private void ShutdownThreadPool() {

        try {
            if (executorPool != null) {
                executorPool.shutdown();
                if (!executorPool.awaitTermination(DEFMAXIMUMPOOLSIZEKEEPALIVETIME, TimeUnit.SECONDS)) {
                    executorPool.shutdownNow();
                    if (executorPool.awaitTermination(DEFMAXIMUMPOOLSIZEKEEPALIVETIME, TimeUnit.SECONDS)) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Forced to Finished", BTHandler.CLASSID, "024");
                    } else {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool NO Finished before TimeOut", BTHandler.CLASSID, "025");
                    }
                }
            } else {
                this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool is NULL", BTHandler.CLASSID, "026");
            }
        } catch (InterruptedException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "027");
        } catch (SecurityException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "028");
        }
    }

    @Override
    public final void run() {
        // Copia del mensaje de la cola
        BTMessage Mensaje = null;
        // Numero de mensaje que quedan en la cola
        int RemainMsg = 0;
        // Copia local de la bandera de ejecucion
        boolean LEjecutar = false;
        // Contadores de tiempo
        long StartTime, DiffTime;

        // Genera el log del inicio del handler
        if (this.Ejecutor != null) {
            StartTime = System.currentTimeMillis();
            // Inicializa el Pool de Ejecucion del Handler
            this.InitThreadPool();
            do {
                rwl.readLock().lock();
                try {
                    Mensaje = this.queue.poll(QUEUEREADTIMEOUT, TimeUnit.MILLISECONDS);
                    RemainMsg = this.queue.size();
                    LEjecutar = this.Ejecutar;
                } catch (InterruptedException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "029");
                } finally {
                    rwl.readLock().unlock();
                }
                if (Mensaje != null) {
                    if (Mensaje.Ejecutable == null) {
                        this.handleMessage(Mensaje);
                    } else {
                        this.executorPool.execute(Mensaje.Ejecutable);
                    }
                    Mensaje = null;
                }
                if (RemainMsg <= 0) {
                    try {
                        EnvironmentUtils.randomSleep(THREADSLEEP);
                    } catch (UtilsException ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "030");
                    }
                    DiffTime = System.currentTimeMillis() - StartTime;
                    if (DiffTime > BTHandler.MAX_IDLE_TIME) {
                        LEjecutar = false;
                    }
                } else {
                    StartTime = System.currentTimeMillis();
                }
            } while (LEjecutar);
            rwl.writeLock().lock();
            try {
                // Limpia la cola de mensajes
                this.queue.clear();
                // Finaliza la ejecion del Pool de Hebras de forma controlada
                this.ShutdownThreadPool();
            } finally {
                this.isFinished = true;
                this.Ejecutor = null;
                rwl.writeLock().unlock();
            }
        }
    }

    /**
     * Clase privada que maneja los ejecutores que fueron rechazados del pool de
     * ejecucion.
     */
    private final class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            int PoolSize, CorePoolSize, ActiveCount;
            long CompletedTaskCount, TaskCount;
            boolean Shutdown, Terminated;
            StringBuffer Message;

            // Recolecta la informacion del pool de ejecucion 
            PoolSize = executor.getPoolSize();
            CorePoolSize = executor.getCorePoolSize();
            ActiveCount = executor.getActiveCount();
            CompletedTaskCount = executor.getCompletedTaskCount();
            TaskCount = executor.getTaskCount();
            Shutdown = executor.isShutdown();
            Terminated = executor.isTerminated();
            Message = new StringBuffer();
            if (BTTask.class.isInstance(r)) {
                Message.append("Rejected to be executed " + BTTask.getCLASSID() + "\n");
            } else {
                Message.append("Rejected to be executed " + r.getClass().getName() + "\n");
            }
            Message.append(String.format("[%s monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s", r.getClass().getSimpleName(), PoolSize, CorePoolSize, ActiveCount, CompletedTaskCount, TaskCount, Shutdown, Terminated));
            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, Message.toString(), BTHandler.CLASSID, "031");
            // Retorna el runnable a la cola de procesamiento de mensajes de Handler
            if (BTTask.class.isInstance(r)) {
                if (Post((BTTask) r)) {
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Rescheduled to be executed by " + BTHandler.CLASSID, BTHandler.CLASSID, "032");
                } else {
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Rejected to be rescheduled by " + BTHandler.CLASSID, BTHandler.CLASSID, "033");
                }
            } else {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Rejected Task " + BTHandler.CLASSID + " IS NOT Task", BTHandler.CLASSID, "034");
            }
        }
    }

    /**
     * Clase privada que maneja la ejecucion de mensajes a ser procesados en un
     * instante de tiempo especifico.
     */
    private final class DelayedRunManager extends BTTask {

        private final static int MAXDELAY = 1000;  // Delay maximo en milisegundos  

        // Tiempo de entrega del mensaje
        private long uptimeMillis;
        // Mensaje contenido dentro del manejador de entrega diferida
        private BTMessage msg;

        // Generador de Numeros Aleatorios
        private MersenneTwisterPlus Random;

        /**
         * Constructor de inicializacion.
         *
         * El tiempo es medido en milisegundos a partir de la media noche del 1
         * de Enero de 1970.
         *
         * @param msg Mensaje a ser enviado
         * @param uptimeMillis Tiempo de envio en milisegundos
         * @param vLog Manejador de bitacoras
         */
        public DelayedRunManager(BTMessage msg, long uptimeMillis, LoggerManager vLog) {
            super(vLog);

            this.uptimeMillis = uptimeMillis;
            this.msg = msg;
            this.setPriority(msg.getPriority());
        }

        /**
         * Retorna el tiempo de envio del mensaje en milisegundos a partir de la
         * media noche del 1 de Enero de 1970.
         *
         * @return Tiempo de ejecucion en milisegundos
         */
        @SuppressWarnings("unused")
        public long getUptimeMillis() {
            return uptimeMillis;
        }

        /**
         * Establece el tiempo de envio del mensaje en milisegundos a partir de
         * la media noche del 1 de Enero de 1970.
         *
         * @param uptimeMillis Tiempo de envio en milisegundos
         */
        @SuppressWarnings("unused")
        public void setUptimeMillis(long uptimeMillis) {
            this.uptimeMillis = uptimeMillis;
        }

        /**
         * Retorna el mensaje a ser enviado en el tiempo especificado
         *
         * @return Mensaje a ser enviado
         */
        @SuppressWarnings("unused")
        public BTMessage getMsg() {
            return msg;
        }

        /**
         * Establece el mensaje a ser enviado en el tiempo especificado
         *
         * @param msg Mensaje a ser enviado
         */
        @SuppressWarnings("unused")
        public void setMsg(BTMessage msg) {
            this.msg = msg;
        }

        /**
         * Ejecutor de verificacion de tiempo para el envio del mensaje
         */
        @Override
        public void Execute(LoggerManager BTLogM) {
            long currentTime;
            long delay;
            double queueEmptyPercent;

            this.Random = new MersenneTwisterPlus();
            currentTime = System.currentTimeMillis();
            delay = this.uptimeMillis - currentTime;
            if (delay > MAXDELAY) {
                // Todavia falta mucho tiempo para enviar el mensaje
                rwl.readLock().lock();
                try {
                    delay = executorPool.getPoolSize();
                    if (delay <= 0) {
                        delay = 1;
                    }
                    queueEmptyPercent = 1 - (((double) queue.size() / (double) (queue.remainingCapacity() + queue.size())) * ((double) executorPool.getActiveCount() / (double) delay));
                } finally {
                    rwl.readLock().unlock();
                }
                if (queueEmptyPercent > 0) {
                    // Realiza un delay proporcional a que tan vacio esta la cola
                    delay = (long) (((double) MAXDELAY) * queueEmptyPercent);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "035");
                    }
                }
                // Se realiza el envio del manejador de delay a la cola para su proxima ejecucion
                while (!Post(this)) {
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Messaje Delay Manager CAN'T be send at time  " + Long.toString(currentTime) + " milliSeconds", BTHandler.CLASSID, "036");
                    // Se esperan un valor aleatorio de milisegundos hasta un maximo de 50 para intentar enviar el mensaje otra vez
                    try {
                        EnvironmentUtils.randomSleep(50);
                    } catch (UtilsException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "037");
                    }
                }
            } else {
                if (delay > 0) {
                    // Se esperan los pocos milisegundos restantes para realizar el envio
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "038");
                    }
                }
                // Se realiza el envio del mensaje
                while (!sendMessage(this.msg)) {
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Messaje CAN'T be send at time  " + Long.toString(this.uptimeMillis) + " milliSeconds", BTHandler.CLASSID, "039");
                    // Se esperan un valor aleatorio de milisegundos hasta un maximo de 50 para intentar enviar el mensaje otra vez
                    try {
                        EnvironmentUtils.randomSleep(50);
                    } catch (UtilsException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTHandler.CLASSID, "040");
                    }
                }
            }
        }
    }
}
