package com.bolivartech.utils.kerneltasks;

import com.bolivartech.utils.converters.Base64;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Copyright 2015,2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's that implement the BolivarTech Kernel
 * execution tasks.</p>
 *
 * <p>
 * Implementa el Algoritmo del nucleo de ejecucion de tareas de BolivarTech.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHP"</li>
 * <li>Loc: 000-039</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2015 - April 20, 2016.
 * @version 1.9.0
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 Version Inicial.</li>
 * <li>v1.0.1 (2015-06-16) Se agrego el metodo para limpiar la cola de tareas
 * sin ejecutar y los mensajes del log para los cambios signigicativos de estado
 * del kernel.</li>
 * <li>v1.5.0 (2015-07-04) Se implemento el manejo de prioridades en las
 * tareas.</li>
 * <li>v1.6.0 (2015-07-12) Se agrego la LLave de Control del Handler para poder
 * hacer shutdown o limpiar la cola de mensajes de manera segura.</li>
 * <li>v1.7.0 (2015-09-27): Se optimizo el desempeño general en la ejecucion de
 * tareas en el Kernel.</li>
 * <li>v1.8.0 (2015-12-02): Se mejoro el manejo de excepciones no
 * capturadas.</li>
 * <li>v1.8.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.8.2 (2016-03-28) Se implemento el uso del LoggerFormatter para la
 * bitacora.</li>
 * <li>v1.9.0 (2016-04-20) Se implemento el uso de hebra de monitoreo dinamica
 * para optimizar los recursos de memoria y procesador cuando el KernelTask
 * excede un TIMEOUT sin recibir mensajes a procesar.</li>
 * </ul>
 */
public class KernelTasks implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHP";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Tiempo maximo en milisegundos que puede estar el handler sin usarse antes de apagar la hebra de monitoreo
    private static final long MAX_IDLE_TIME = 60000; // 1 minutos

    private static final int THREADSLEEP = 500;     // Milisegundos que duerme la hebra

    // Maximo numero de tareas que puede manejar el kernel
    private static final int MAXQUEUESIZE = 65536;
    // Minimo numero de tareas que puede manejar el kernel
    private static final int MINQUEUESIZE = 128;
    // Tiempo de espera de la hebra principal
    private final static int MAINTHREADSLEEP = 10;  // en Millisegundos
    // Maximo tiempo que una habra adicional al core puede estar idle
    private final static int DEFMAXIMUMPOOLSIZEKEEPALIVETIME = 30; // en Segundos
    // Tiempo maximo de espera para agregar una tarea a la cola
    private static final long QUEUEWRITETIMEOUT = 100;  // Milisegundos
    // Tiempo maximo de espera para obtener una tarea de la cola
    private static final long QUEUEREADTIMEOUT = 50;  // Milisegundos

    // Manejador de Bitacoras
    private LoggerFormatter BTLogF;

    // Numeros Aleatorios
    private MersenneTwisterPlus Random;

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Hebra de Ejecucion
    private Thread Ejecutor;

    // Parametro del ThreadPoolExecutor
    // Tamano del nucleo de Threads
    private int NumCPUs;
    private int ThreadCorePoolSize;
    private int MaximumPoolSize;
    private int MaximumPoolSizekeepAliveTime; // en Segundos

    // Tamaño de la cola de tareas
    private int FIFOSize;

    // Cola FIFO de mensajes
    @GuardedBy("rwl")
    private PriorityBlockingQueue<BTTask> FIFO = null;

    // Bandera de finalizacion del proceso
    @GuardedBy("rwl")
    private boolean isFinished = true;
    // Bandera de Interrupcion del proceso del proceso
    @GuardedBy("rwl")
    private boolean Ejecutar;
    // Bandera para indicar que el kernel arranco
    @GuardedBy("rwl")
    private boolean isStarted;

    // Pool de Thread que van a ejecutar las tareas
    private ThreadPoolExecutor executorPool;
    // LLave de contro del KernelTask
    private String ControlKey = null;

    public KernelTasks(LoggerManager vLog) {

        // Inicializador de Numeros Aleatorios
        this.Random = new MersenneTwisterPlus();
        this.BTLogF = LoggerFormatter.getInstance(vLog);
        this.NumCPUs = Runtime.getRuntime().availableProcessors();
        this.MaximumPoolSize = 2 * (this.NumCPUs);
        this.ThreadCorePoolSize = this.NumCPUs + 2;
        this.ThreadCorePoolSize = (this.ThreadCorePoolSize > 0 ? this.ThreadCorePoolSize : 1);
        this.MaximumPoolSizekeepAliveTime = DEFMAXIMUMPOOLSIZEKEEPALIVETIME;
        this.FIFOSize = (int) (((double) Runtime.getRuntime().freeMemory()) * 0.0001);
        this.FIFOSize = (this.FIFOSize > MAXQUEUESIZE ? MAXQUEUESIZE : this.FIFOSize);
        this.FIFOSize = (this.FIFOSize > MINQUEUESIZE ? this.FIFOSize : MINQUEUESIZE);
        this.FIFO = new PriorityBlockingQueue<BTTask>(this.FIFOSize);
        this.Ejecutar = false;
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
        this.executorPool = new ThreadPoolExecutor(ThreadCorePoolSize, MaximumPoolSize, MaximumPoolSizekeepAliveTime, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(4 * MaximumPoolSize), threadFactory, rejectionHandler);
        // Permite que las hebras del Core tambien tengan timeout
        this.executorPool.allowCoreThreadTimeOut(true);
    }

    /**
     * Inicia el Kernel de Tareas
     */
    private final void StartThread() {
        boolean LisStarted = false;

        if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
            this.Ejecutor = new Thread(this);
            this.Ejecutor.setName(this.CLASSID + "[" + Integer.toHexString(Random.nextInt31()) + "]");
            this.Ejecutor.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    StringWriter StackTrace;
                    long StartTime, DiffTime;
                    LoggerFormatter BTLogF;
                    StringBuffer Stack;

                    BTLogF = LoggerFormatter.getInstance(null);
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Uncaught Exception\n" + e.toString(), KernelTasks.CLASSID, "000");
                    Stack = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Stack.append(STE.toString() + "\n");
                        }
                    } else {
                        Stack.append("ERROR: Thread is NULL at StackTrace");
                    }
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Stack:\n" + Stack.toString(), KernelTasks.CLASSID, "001");
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "StackTrace:\n" + StackTrace.toString(), KernelTasks.CLASSID, "002");
                    // Finaliza el Pool de ejecucion de tareas
                    rwl.writeLock().lock();
                    try {
                        if (executorPool != null) {
                            executorPool.shutdown();
                            if (executorPool.awaitTermination(MaximumPoolSizekeepAliveTime, TimeUnit.SECONDS)) {
                                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Finished", KernelTasks.CLASSID, "003");
                            } else {
                                executorPool.shutdownNow();
                                if (executorPool.awaitTermination(MaximumPoolSizekeepAliveTime, TimeUnit.SECONDS)) {
                                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Forced to Finished", KernelTasks.CLASSID, "004");
                                } else {
                                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_INFO, false, null, "Thread Pool NO Finished before TimeOut", KernelTasks.CLASSID, "005");
                                }
                            }
                        } else {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool is NULL", KernelTasks.CLASSID, "006");
                        }
                    } catch (InterruptedException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "007");
                    } catch (SecurityException ex) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "008");
                    } finally {
                        Ejecutar = false;
                        DiffTime = 0;
                        StartTime = System.currentTimeMillis();
                        while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                            try {
                                EnvironmentUtils.randomSleep(THREADSLEEP);
                            } catch (UtilsException ex) {
                                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "009");
                            } finally {
                                DiffTime = System.currentTimeMillis() - StartTime;
                            }
                        }
                        if (DiffTime >= TIMEOUT) {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Finished by TimeOut", KernelTasks.CLASSID, "010");
                        }
                        if (Ejecutor != null) {
                            if (Ejecutor.getState() != Thread.State.TERMINATED) {
                                Ejecutor.interrupt();
                                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Forced Thread Finish by Interruption", KernelTasks.CLASSID, "011");
                            }
                            Ejecutor = null;
                            executorPool = null;
                            isFinished = true;
                            rwl.writeLock().unlock();
                        }
                    }
                }
            });
            this.isFinished = false;
            this.Ejecutar = true;
            this.isStarted = false;
            this.Ejecutor.start();
            do {
                try {
                    EnvironmentUtils.randomSleep(THREADSLEEP);
                } catch (UtilsException ex) {
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "012");
                }
                rwl.readLock().lock();
                try {
                    LisStarted = this.isStarted;
                } finally {
                    rwl.readLock().unlock();
                }
            } while (!LisStarted);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Can't start Thread because the previous still is Running", KernelTasks.CLASSID, "013");
        }
    }

    /**
     * Metodo privado que genera la llave aleatoria de control del Handler, la
     * cual se debe usar para hacerle Shutdown o Clear de la cola de mensajes.
     *
     * @return
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
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "014");
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
     * Retorna la cantidad (aproximada) de tareas que estan esperando a ser
     * ejecutadas por el kernel de tareas.
     *
     * @return Cantidad de tareas a la espera de ser ejecutadas
     */
    public int waitingTasks() {
        int Size = 0;

        rwl.readLock().lock();
        try {
            Size = this.FIFO.size();
            if (this.executorPool != null) {
                if (this.executorPool.getQueue() != null) {
                    Size += this.executorPool.getQueue().size();
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
        return Size;
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
     * Retorna TRUE si se termino de ejecutar el troceso de Stress y FALSE si no
     * se ha terminado.
     *
     * @return TRUE si se termino y FALSE si NO
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
     * Finaliza la ejecucion del Kernel de Tareas
     *
     * @param CtrlKey Llave de control del KernelTask
     */
    public void Shutdown(String CtrlKey) {

        this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Shutdown Started", KernelTasks.CLASSID, "015");
        if ((this.ControlKey == null) || (this.ControlKey.equals(CtrlKey))) {
            rwl.writeLock().lock();
            try {
                this.Ejecutar = false;
            } finally {
                rwl.writeLock().unlock();
            }
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Shutdown Failed due Control Key MISMATCH", KernelTasks.CLASSID, "016");
        }
    }

    @Override
    protected void finalize() throws Throwable {

        rwl.writeLock().lock();
        try {
            this.Ejecutar = false;
        } finally {
            rwl.writeLock().unlock();
            super.finalize();
        }
    }

    /**
     * Agraga la tarea a la cola de ejecucion retornando TRUE si lo logro
     * agregar o FALSE si no.
     *
     * @param task Tarea a agregar a la cola
     * @return TRUE si lo logro agregar o FALSE si no.
     */
    public final boolean addTask(BTTask task) {
        boolean Salida = false;

        if (task != null) {
            try {
                if ((this.Ejecutor == null) || (this.Ejecutor.getState() == Thread.State.TERMINATED)) {
                    // Inicia la hebra de monitoreo
                    this.StartThread();
                }
                Salida = this.FIFO.offer(task, QUEUEWRITETIMEOUT, TimeUnit.MILLISECONDS);
            } catch (ClassCastException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "017");
            } catch (NullPointerException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "018");
            } catch (OutOfMemoryError ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, ex.getMessage(), KernelTasks.CLASSID, "019");
            }
        }
        return Salida;
    }

    /**
     * Elimina todas las tareas que esten en cola de ejecucion, pero no afecta
     * las que ya se estan ejecutando.
     *
     * @param CtrlKey Llave de control del KernelTask
     */
    public final void clearQueueTask(String CtrlKey) {

        if ((this.ControlKey == null) || (this.ControlKey.equals(CtrlKey))) {
            rwl.writeLock().lock();
            try {
                this.FIFO.clear();
                this.executorPool.getQueue().clear();
                this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Tasks Queue Cleaned", KernelTasks.CLASSID, "020");
            } finally {
                rwl.writeLock().unlock();
            }
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Message Queue Clean Failed due Control Key MISMATCH", KernelTasks.CLASSID, "021");
        }
    }

    // Finaliza el pool de Thread de ejecucion
    private void ShutdownThreadPool() {

        try {
            if (this.executorPool != null) {
                this.executorPool.shutdown();
                if (!this.executorPool.awaitTermination(MaximumPoolSizekeepAliveTime, TimeUnit.SECONDS)) {
                    this.executorPool.shutdownNow();
                    if (this.executorPool.awaitTermination(MaximumPoolSizekeepAliveTime, TimeUnit.SECONDS)) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, false, null, "Thread Pool Forced to Finished", KernelTasks.CLASSID, "022");
                    } else {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool NO Finished before TimeOut", KernelTasks.CLASSID, "023");
                    }
                }
            } else {
                this.BTLogF.LogMsg(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_WARNING, false, null, "Thread Pool is NULL", KernelTasks.CLASSID, "024");
            }
        } catch (InterruptedException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "025");
        } catch (SecurityException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "026");
        }
    }

    @Override
    public void run() {
        // Copia locar la de bandera de ejecucion
        boolean lEjecutar;
        // Copia local del Pool de Execicion
        ThreadPoolExecutor lexecutorPool;
        // Tarea a ser agregada a la cola de ejecucion
        Runnable Tarea = null;
        // Numero de Tareas que quedan en la cola
        int RemainTasks = 0;
        // Contadores de tiempo
        long StartTime, DiffTime;
        // Variables de entorno del pool
        int MaximumPoolSize, CorePoolSize, ActiveThreadCount;

        this.InitThreadPool();
        rwl.writeLock().lock();
        try {
            lexecutorPool = this.executorPool;
            lEjecutar = this.Ejecutar;
            this.isStarted = true;
        } finally {
            rwl.writeLock().unlock();
        }
        MaximumPoolSize = lexecutorPool.getMaximumPoolSize();
        CorePoolSize = lexecutorPool.getCorePoolSize();
        StartTime = System.currentTimeMillis();
        do {
            ActiveThreadCount = lexecutorPool.getActiveCount();
            if (ActiveThreadCount < MaximumPoolSize) {
                try {
                    Tarea = this.FIFO.poll(QUEUEREADTIMEOUT, TimeUnit.MILLISECONDS);
                    RemainTasks = this.FIFO.size();
                } catch (InterruptedException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "027");
                }
                if (Tarea != null) {
                    try {
                        lexecutorPool.execute(Tarea);
                    } catch (OutOfMemoryError ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, ex.getMessage(), KernelTasks.CLASSID, "028");
                        try {
                            this.FIFO.put((BTTask) Tarea);
                        } catch (ClassCastException exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, exx, null, KernelTasks.CLASSID, "029");
                        } catch (NullPointerException exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, exx, null, KernelTasks.CLASSID, "030");
                        } catch (OutOfMemoryError exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, exx.getMessage(), KernelTasks.CLASSID, "031");
                        }
                    } catch (RejectedExecutionException ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "032");
                        try {
                            this.FIFO.put((BTTask) Tarea);
                        } catch (ClassCastException exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, exx, null, KernelTasks.CLASSID, "033");
                        } catch (NullPointerException exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, exx, null, KernelTasks.CLASSID, "034");
                        } catch (OutOfMemoryError exx) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, exx.getMessage(), KernelTasks.CLASSID, "035");
                        }
                    } finally {
                        Tarea = null;
                    }
                }
            } else {
                // Reduce la introduccion de proceso en la cola de ejecusion para no sobrecargar el procesador
                while (ActiveThreadCount > CorePoolSize) {
                    try {
                        EnvironmentUtils.randomSleep(MAINTHREADSLEEP);
                    } catch (UtilsException ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "036");
                    }
                }
            }
            rwl.readLock().lock();
            try {
                lEjecutar = this.Ejecutar;
            } finally {
                rwl.readLock().unlock();
            }
            if ((RemainTasks <= 0) && (ActiveThreadCount <= 0)) {
                try {
                    EnvironmentUtils.randomSleep(THREADSLEEP);
                } catch (UtilsException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "037");
                }
                DiffTime = System.currentTimeMillis() - StartTime;
                if (DiffTime > KernelTasks.MAX_IDLE_TIME) {
                    lEjecutar = false;
                }
            } else {
                StartTime = System.currentTimeMillis();
            }
        } while (lEjecutar);
        rwl.writeLock().lock();
        try {
            // Se eliminan todos los procesos que se esten esperando en la cola del ThreadPool a ser ejecutandos
            lexecutorPool.getQueue().clear();
            // Finaliza la ejecion del Pool de Hebras
            lexecutorPool = null;
            this.ShutdownThreadPool();
        } finally {
            this.Ejecutor = null;
            this.isFinished = true;
            rwl.writeLock().unlock();
        }
    }

    /**
     * Clase privada que maneja los ejecutores que fueron rechazados del pool de
     * ejecucion.
     */
    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            int PoolSize, CorePoolSize, ActiveCount;
            long CompletedTaskCount, TaskCount;
            boolean Shutdown, Terminated;
            StringBuffer Message;

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
            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, Message.toString(), KernelTasks.CLASSID, "038");
            try {
                Thread.sleep(Random.nextInt(MAINTHREADSLEEP));
            } catch (InterruptedException ex) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, KernelTasks.CLASSID, "039");
            }
            executor.execute(r);
        }
    }
}
