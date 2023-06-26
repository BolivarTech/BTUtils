package com.bolivartech.utils.fec.reedsolomon;

import com.bolivartech.utils.array.ArrayUtils;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.fec.AcFEC;
import com.bolivartech.utils.files.FileManager;
import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015,2016 BolivarTech INC.</p>
 *
 * <p>
 * Homepage:
 * <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage:
 * <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's Class for Reed-Solomon coding and decoding
 * over a byte array.</p>
 *
 * <p>
 * Define la clase para realiza la codificacion y decodificacion Reed-Solomon de
 * una cadena de bytes.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFH2"</li>
 * <li>Loc: 000-035</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @version 4.0.1
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v3.0.0 (2016-02-20): Se elimino la restriccion de que la entrada solo
 * puede ser de 128 bytes.</li>
 * <li>v4.0.0 (2016-03-06): Agrego la capasidad de procesar Archivos y Stream
 * directamente.</li>
 * <li>v4.0.1 (2016-03-25) Se agrego el codigo de localizacion para la
 * excepcion.</li>
 * </ul>
 */
public strictfp class ReedSolomon extends AcFEC implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH2";

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;

    // Cantidad de bytes que maneja el buffer porque el campo de Galois es de 256 
    private final static int BUFFERSIZE = 64;

    // Tareas que puede ejecutar la Hebra
    private final static int CODECSTREAM = 1;
    private final static int DECODECSTREAM = 2;
    private final static int CODECFILE = 3;
    private final static int DECODECFILE = 4;

    // Entrada vacia
    public static final int ERROREMPTYINPUT = -1;
    // Entrada Nula
    public static final int ERRORNULLINPUT = -2;
    // Salida Nula
    public static final int ERRORNULLOUTPUT = -3;

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Manejador de Bitacoras
    private final LoggerFormatter BTLogF;

    // Hebra de Ejecucion
    private Thread Ejecutor = null;

    // Identificador de la hebra de ejecucion
    private long ThreadID;

    // Bandera para indicar si se compreto la operacion
    @GuardedBy("rwl")
    private boolean Finished;
    // Bandera para indicar qu se interrumpe el proceso
    @GuardedBy("rwl")
    private boolean Interrupt;

    /* Encoder y decoder de Reed-Solomon */
    @GuardedBy("rwl")
    private ReedSolomonEncoder RSE;
    @GuardedBy("rwl")
    private ReedSolomonDecoder RSD;

    // Generador de Numeros Aleatorios
    private MersenneTwisterPlus Random;

    // Archivos de entrada y salida
    @GuardedBy("rwl")
    private FileManager InFM;
    @GuardedBy("rwl")
    private FileManager OutFM;

    // Streams de entrada y salida
    @GuardedBy("rwl")
    private InputStream InS;
    @GuardedBy("rwl")
    private OutputStream OutS;

    // Porcentaje de progreso
    @GuardedBy("rwl")
    private int Progress;

    // Identificador de la tarea a Ejecutar
    private int Task;

    /**
     * Constructor con la inicializacion de la taza de redundancia en el paquete
     * para la correccion de errores utilizando Reed-Solomon.
     *
     * @param CodeLengtMultiplier Taza de redundancia de Reed-Solomon
     */
    public ReedSolomon(double CodeLengtMultiplier) {
        this(CodeLengtMultiplier, null);
    }

    /**
     * Constructor con la inicializacion de la taza de redundancia en el paquete
     * para la correccion de errores utilizando Reed-Solomon y de la bitacora.
     *
     * @param CodeLengtMultiplier Taza de redundancia de Reed-Solomon
     * @param vLogM Manejador de bitacora
     */
    public ReedSolomon(double CodeLengtMultiplier, LoggerManager vLogM) {
        super(CodeLengtMultiplier);

        this.BTLogF = LoggerFormatter.getInstance(vLogM);
        this.Random = new MersenneTwisterPlus();
        this.RSE = new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256);
        this.RSD = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);
        this.InFM = null;
        this.OutFM = null;
        this.InS = null;
        this.OutS = null;
        this.Finished = false;
        this.Interrupt = false;
        this.Task = 0;
        this.Progress = 0;
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
                    StringBuffer Message;

                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Uncaught Exception\n" + e.toString(), ReedSolomon.CLASSID, "000");
                    Message = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Message.append(STE.toString());
                        }
                    } else {
                        Message.append("ERROR: Thread is NULL at StackTrace");
                    }
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Stack:\n" + Message.toString(), ReedSolomon.CLASSID, "001");
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "StackTrace: " + StackTrace.toString(), ReedSolomon.CLASSID, "002");
                    DiffTime = 0;
                    StartTime = System.currentTimeMillis();
                    while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "003");
                        } finally {
                            DiffTime = System.currentTimeMillis() - StartTime;
                        }
                    }
                    if (DiffTime >= TIMEOUT) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Finished by TimeOut", ReedSolomon.CLASSID, "004");
                    }
                    if (Ejecutor != null) {
                        if (Ejecutor.getState() != Thread.State.TERMINATED) {
                            Ejecutor.interrupt();
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Forced Thread Finish by Interruption", ReedSolomon.CLASSID, "005");
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
            this.Progress = 0;
            this.Finished = false;
            this.Interrupt = false;
            this.Ejecutor.start();
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Can't start Thread because the previous still is Running", ReedSolomon.CLASSID, "006");
        }
    }

    /**
     * Retorna el porcentaje de progreso del procesamiento del archivo o los
     * Codigos de Error (menores a 0) si el proceso fallo.
     *
     * @return Progress Percent o codigos de error
     */
    public int getProgress() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.Progress;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el nivel de progreso al valor especificado
     *
     * @param Progress Nuevo nivel de progreso
     */
    private void setProgress(int Progress) {

        this.rwl.writeLock().lock();
        try {
            this.Progress = Progress;
        } finally {
            this.rwl.writeLock().unlock();
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
     * Verifica si se termino de ejecutar el proceso de codificacion o si no.
     *
     * @return TRUE si se termino de ejecutar el proceso de codificacion o FALSE
     * si no
     */
    public boolean isDone() {
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
     * Interrumpe el proceso del algoritmo LZMA
     */
    public void Interrupt() {

        this.rwl.writeLock().lock();
        try {
            this.Interrupt = true;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna el estado de la bandera de interrupcion
     *
     * @return TRUE si es interrumpido y FALSE si no
     */
    public boolean isInterrupted() {
        boolean Result = false;

        this.rwl.readLock().lock();
        try {
            Result = this.Interrupt;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Metodo para el Codificacion utilizando el algoritmo de Reed-Solomon
     *
     * @param Input Cadena de entrada de datos
     * @return Cadena de entrada con codigo de redundancia
     * @throws UtilsException
     */
    @Override
    public byte[] encode(byte[] Input) throws UtilsException {
        byte[] Buffer, Result;
        int i, j, NumBlocks, Remain;
        int RedundanciLength;

        Result = null;

        if (Input != null) {
            if (Input.length > 0) {
                NumBlocks = Input.length / BUFFERSIZE;
                Remain = Input.length % BUFFERSIZE;
                RedundanciLength = (int) this.getCodeRedundancyLength(Input.length);
                Result = new byte[Input.length + (2 * RedundanciLength)];
                RedundanciLength = (int) this.getCodeRedundancyLength(BUFFERSIZE);
                j = 0;
                // Copia los bloques de BUFFERSIZE bytes
                this.rwl.writeLock().lock();
                try {
                    for (i = 0; i < NumBlocks; i++) {
                        Buffer = (byte[]) ArrayUtils.subArray(Input, BUFFERSIZE * i, BUFFERSIZE);
                        Buffer = this.RSE.encode(Buffer, RedundanciLength);
                        ArrayUtils.arrayCopy(Buffer, 0, Result, j, Buffer.length);
                        j += Buffer.length;
                    }
                    // Realiza la codificacion de lo remanente en el buffer
                    if (Remain > 0) {
                        Buffer = (byte[]) ArrayUtils.subArray(Input, BUFFERSIZE * i, Remain);
                        Buffer = this.RSE.encode(Buffer, (int) this.getCodeRedundancyLength(Buffer.length));
                        ArrayUtils.arrayCopy(Buffer, 0, Result, j, Buffer.length);
                        j += Buffer.length;
                    }
                    if (j < Result.length) {
                        Result = (byte[]) ArrayUtils.resizeArray(Result, j);
                    }
                } finally {
                    this.rwl.writeLock().unlock();
                }
            } else {
                this.setProgress(ReedSolomon.ERROREMPTYINPUT);
                throw new UtilsException("ERROR: ReedSolomon Encoder Input is EMPTY", ReedSolomon.ERROREMPTYINPUT, ReedSolomon.CLASSID + "007");
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
            throw new UtilsException("ERROR: ReedSolomon Encoder Input is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "008");
        }
        return Result;
    }

    /**
     * Metodo para la Decodificacion utilizando el algoritmo de Reed-Solomon
     *
     * @param Input Cadena de entrada con codigo de redundancia
     * @return Cadena de entrada con los errores corregidos por el algoritmo
     * @throws UtilsException Manejador de errores en el algoritmo FEC
     */
    @Override
    public byte[] decode(byte[] Input) throws UtilsException {
        byte[] Buffer, Result;
        int i, j, NumBlocks, Remain;
        int RedundanciLength, BlockLenght;

        Result = null;
        if (Input != null) {
            if (Input.length > 0) {
                RedundanciLength = (int) this.getCodeRedundancyLength(BUFFERSIZE);
                BlockLenght = BUFFERSIZE + RedundanciLength;
                NumBlocks = Input.length / BlockLenght;
                Remain = Input.length % BlockLenght;
                Result = new byte[Input.length];
                j = 0;
                // Copia los bloques de BUFFERSIZE bytes
                this.rwl.writeLock().lock();
                try {
                    for (i = 0; i < NumBlocks; i++) {
                        Buffer = (byte[]) ArrayUtils.subArray(Input, BlockLenght * i, BlockLenght);
                        Buffer = this.RSD.decode(Buffer, RedundanciLength);
                        ArrayUtils.arrayCopy(Buffer, 0, Result, j, Buffer.length);
                        j += Buffer.length;
                    }
                    // Realiza la codificacion de lo remanente en el buffer
                    if (Remain > 0) {
                        Buffer = (byte[]) ArrayUtils.subArray(Input, BlockLenght * i, BlockLenght);
                        Buffer = this.RSD.decode(Buffer, (int) this.getDeCodeRedundancyLength(Buffer.length));
                        ArrayUtils.arrayCopy(Buffer, 0, Result, j, Buffer.length);
                        j += Buffer.length;
                    }
                    if (j < Result.length) {
                        Result = (byte[]) ArrayUtils.resizeArray(Result, j);
                    }
                } finally {
                    this.rwl.writeLock().unlock();
                }
            } else {
                this.setProgress(ReedSolomon.ERROREMPTYINPUT);
                throw new UtilsException("ERROR: ReedSolomon Dencoder Input is EMPTY", ReedSolomon.ERROREMPTYINPUT, ReedSolomon.CLASSID + "009");
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
            throw new UtilsException("ERROR: ReedSolomon Dencoder Input is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "010");
        }
        return Result;
    }

    /**
     * Realiza la codificacion del archivo de entrada retornandolo en el archivo
     * de salida, si el archivo de salida es null se genera un archivo en la
     * misma ruta del de entrada, con el mismo nombre y la extension ".rs". Si
     * el archivo de salida es un directorio, ahi se crea el archivo de salida,
     * con el mismo nombre del de la entrada pero con la extension ".rs"
     *
     * @param Input Archivo de entrada a codificar
     * @param Output Archivo de salida codificado
     * @throws UtilsException
     */
    public void encode(FileManager Input, FileManager Output) throws UtilsException {

        if (Input != null) {
            this.rwl.writeLock().lock();
            try {
                this.InS = null;
                this.OutS = null;
                this.InFM = Input;
                this.OutFM = Output;
                this.Task = ReedSolomon.CODECFILE;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Input File is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "011");
        }
    }

    /**
     * Realiza la decodificacion del archivo de entrada retornandolo en el
     * archivo de salida, si el archivo de salida es null se genera un archivo
     * en la misma ruta del de entrada, si la extension es ".rs" se remueve. Si
     * el archivo de salida es un directorio, ahi se crea el archivo de salida,
     * con el mismo nombre del de la entrada,si la extension es ".rs" se
     * remueve, si no se agrega la extension ".out"
     *
     * @param Input Archivo de entrada a decodificar
     * @param Output Archivo de salida decodificado
     * @throws UtilsException
     */
    public void decode(FileManager Input, FileManager Output) throws UtilsException {

        if (Input != null) {
            this.rwl.writeLock().lock();
            try {
                this.InS = null;
                this.OutS = null;
                this.InFM = Input;
                this.OutFM = Output;
                this.Task = ReedSolomon.DECODECFILE;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Input File is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "012");
        }
    }

    /**
     * Realiza la codificacion del Stream de entrada retornandolo en el Stream
     * de salida
     *
     * @param Input Stream de entrada a codificar
     * @param Output Stream de salida codificado
     * @throws UtilsException
     */
    public void encode(InputStream Input, OutputStream Output) throws UtilsException {

        if (Input != null) {
            if (Output != null) {
                this.rwl.writeLock().lock();
                try {
                    this.InS = Input;
                    this.OutS = Output;
                    this.InFM = null;
                    this.OutFM = null;
                    this.Task = ReedSolomon.CODECSTREAM;
                    this.StartThread();
                } finally {
                    this.rwl.writeLock().unlock();
                }
            } else {
                throw new UtilsException("ERROR: Output Stream is NULL", ReedSolomon.ERRORNULLOUTPUT, ReedSolomon.CLASSID + "013");
            }
        } else {
            throw new UtilsException("ERROR: Input Stream is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "014");
        }
    }

    /**
     * Realiza la decodificacion del Stream de entrada retornandolo en el Stream
     * de salida
     *
     * @param Input Stream de entrada a decodificar
     * @param Output Stream de salida decodificado
     * @throws UtilsException
     */
    public void decode(InputStream Input, OutputStream Output) throws UtilsException {

        if (Input != null) {
            if (Output != null) {
                this.rwl.writeLock().lock();
                try {
                    this.InS = Input;
                    this.OutS = Output;
                    this.InFM = null;
                    this.OutFM = null;
                    this.Task = ReedSolomon.DECODECSTREAM;
                    this.StartThread();
                } finally {
                    this.rwl.writeLock().unlock();
                }
            } else {
                throw new UtilsException("ERROR: Output Stream is NULL", ReedSolomon.ERRORNULLOUTPUT, ReedSolomon.CLASSID + "015");
            }
        } else {
            throw new UtilsException("ERROR: Input Stream is NULL", ReedSolomon.ERRORNULLINPUT, ReedSolomon.CLASSID + "016");
        }
    }

    /**
     * Codifica el Stream de datos, y si se conoce el tamaño (mayor a cero)
     * calcula el porcentaje de progreso, en caso contratio se pasa un -1 para
     * indicar que se es desconocido.
     */
    private void codecStream(InputStream lInS, OutputStream lOutS, long Size) {
        boolean lInterrupt;
        byte[] InputBuffer, OutputBuffer;
        int Pos, NumBytesRead, lProgress;
        long NumBlocks = 0;
        long BlockProcessed = 0;

        if (lInS != null) {
            if (lOutS != null) {
                this.rwl.readLock().lock();
                try {
                    lInterrupt = this.Interrupt;
                } finally {
                    this.rwl.readLock().unlock();
                }
                InputBuffer = new byte[BUFFERSIZE];
                // Calcual el numero de bloques a procesar
                if (Size > 0) {
                    NumBlocks = Size / InputBuffer.length;
                    if ((Size % InputBuffer.length) != 0) {
                        NumBlocks++;
                    }
                    BlockProcessed = 0;
                    lProgress = 0;
                }
                NumBytesRead = 0;
                Pos = 0;
                while ((NumBytesRead >= 0) && (!lInterrupt)) {
                    try {
                        NumBytesRead = lInS.read(InputBuffer, Pos, InputBuffer.length - Pos);
                        if (NumBytesRead > 0) {
                            Pos += NumBytesRead;
                        }
                    } catch (IOException ex) {
                        // Error de lectura del Stream de entrada
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't read from InputStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "017");
                    }
                    // Verifica si se lleno el buffer
                    if (Pos == InputBuffer.length) {
                        try {
                            OutputBuffer = this.encode(InputBuffer);
                            // Escribe la data codificada en el OutStream
                            lOutS.write(OutputBuffer, 0, OutputBuffer.length);
                            // Actualiza el procentaje de progreso
                            if (Size > 0) {
                                BlockProcessed++;
                                lProgress = (int) ((100 * BlockProcessed) / NumBlocks);
                                this.setProgress(lProgress);
                            }
                        } catch (IOException ex) {
                            // Error de escritura del OutStream
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "018");
                        } catch (UtilsException ex) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "019");
                        }
                        Pos = 0;
                    }
                    this.rwl.readLock().lock();
                    try {
                        lInterrupt = this.Interrupt;
                    } finally {
                        this.rwl.readLock().unlock();
                    }
                }
                // Procesa lo remanente en el buffer
                if (Pos > 0) {
                    InputBuffer = (byte[]) ArrayUtils.resizeArray(InputBuffer, Pos);
                    try {
                        OutputBuffer = this.encode(InputBuffer);
                        // Escribe la data codificada en el OutStream
                        lOutS.write(OutputBuffer, 0, OutputBuffer.length);
                        // Actualiza el procentaje de progreso
                        if (Size > 0) {
                            BlockProcessed++;
                            lProgress = (int) ((100 * BlockProcessed) / NumBlocks);
                            this.setProgress(lProgress);
                        }
                    } catch (IOException ex) {
                        // Error de escritura del OutStream
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "020");
                    } catch (UtilsException ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "021");
                    }
                    Pos = 0;
                }
                // Vacia y cierra el Stream de Salida y cierra el Stream de Entrada
                try {
                    lOutS.flush();
                    lOutS.close();
                    lInS.close();
                    // Actualiza el procentaje de progreso
                    this.setProgress(100);
                } catch (IOException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "022");
                }
            } else {
                this.setProgress(ReedSolomon.ERRORNULLOUTPUT);
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
        }

    }

    /**
     * Decodifica el Stream de datos, y si se conoce el tamaño (mayor a cero)
     * calcula el porcentaje de progreso, en caso contratio se pasa un -1 para
     * indicar que se es desconocido.
     */
    private void decodecStream(InputStream lInS, OutputStream lOutS, long Size) {
        boolean lInterrupt;
        byte[] InputBuffer, OutputBuffer;
        int Pos, NumBytesRead, RedundanciLength, lProgress;
        long NumBlocks = 0;
        long BlockProcessed = 0;

        if (lInS != null) {
            if (lOutS != null) {
                this.rwl.readLock().lock();
                try {
                    lInterrupt = this.Interrupt;
                } finally {
                    this.rwl.readLock().unlock();
                }
                RedundanciLength = (int) this.getCodeRedundancyLength(ReedSolomon.BUFFERSIZE);
                InputBuffer = new byte[ReedSolomon.BUFFERSIZE + RedundanciLength];
                // Calcual el numero de bloques a procesar
                if (Size > 0) {
                    NumBlocks = Size / InputBuffer.length;
                    if ((Size % InputBuffer.length) != 0) {
                        NumBlocks++;
                    }
                    BlockProcessed = 0;
                    lProgress = 0;
                }
                OutputBuffer = null;
                NumBytesRead = 0;
                Pos = 0;
                while ((NumBytesRead >= 0) && (!lInterrupt)) {
                    try {
                        NumBytesRead = lInS.read(InputBuffer, Pos, InputBuffer.length - Pos);
                        if (NumBytesRead > 0) {
                            Pos += NumBytesRead;
                        }
                    } catch (IOException ex) {
                        // Error de lectura del Stream de entrada
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't read from InputStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "023");
                    }
                    // Verifica si se lleno el buffer
                    if (Pos == InputBuffer.length) {
                        try {
                            // Decodifica los datos
                            OutputBuffer = this.decode(InputBuffer);
                        } catch (UtilsException ex) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Decoding ReedSolomon[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "024");
                            OutputBuffer = null;
                        } finally {
                            if (OutputBuffer != null) {
                                try {
                                    // Escribe la data decodificada en el OutStream
                                    lOutS.write(OutputBuffer, 0, OutputBuffer.length);
                                } catch (IOException ex) {
                                    // Error de escritura del OutStream
                                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "025");
                                }
                            } else {
                                try {
                                    // Escribe la data decodificada en el OutStream
                                    lOutS.write(InputBuffer, 0, InputBuffer.length);
                                } catch (IOException ex) {
                                    // Error de escritura del OutStream
                                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "026");
                                }
                            }
                            // Actualiza el procentaje de progreso
                            if (Size > 0) {
                                BlockProcessed++;
                                lProgress = (int) ((100 * BlockProcessed) / NumBlocks);
                                this.setProgress(lProgress);
                            }
                        }
                        Pos = 0;
                    }
                    this.rwl.readLock().lock();
                    try {
                        lInterrupt = this.Interrupt;
                    } finally {
                        this.rwl.readLock().unlock();
                    }
                }
                // Procesa lo remanente en el buffer
                if (Pos > 0) {
                    InputBuffer = (byte[]) ArrayUtils.resizeArray(InputBuffer, Pos);
                    try {
                        // Decodifica los datos
                        OutputBuffer = this.decode(InputBuffer);
                    } catch (UtilsException ex) {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Decoding ReedSolomon[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "027");
                        OutputBuffer = null;
                    } finally {
                        if (OutputBuffer != null) {
                            try {
                                // Escribe la data decodificada en el OutStream
                                lOutS.write(OutputBuffer, 0, OutputBuffer.length);
                            } catch (IOException ex) {
                                // Error de escritura del OutStream
                                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "028");
                            }
                        } else {
                            try {
                                // Escribe la data decodificada en el OutStream
                                lOutS.write(InputBuffer, 0, InputBuffer.length);
                            } catch (IOException ex) {
                                // Error de escritura del OutStream
                                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Can't write in the OutStream[" + ex.getMessage() + "]", ReedSolomon.CLASSID, "029");
                            }
                        }
                        // Actualiza el procentaje de progreso
                        if (Size > 0) {
                            BlockProcessed++;
                            lProgress = (int) ((100 * BlockProcessed) / NumBlocks);
                            this.setProgress(lProgress);
                        }
                    }
                    Pos = 0;
                }
                // Vacia y cierra el Stream de Salida y cierra el Stream de Entrada
                try {
                    lOutS.flush();
                    lOutS.close();
                    lInS.close();
                    // Actualiza el procentaje de progreso
                    this.setProgress(100);
                } catch (IOException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "030");
                }
            } else {
                this.setProgress(ReedSolomon.ERRORNULLOUTPUT);
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
        }
    }

    /**
     * Codifica el archivo
     */
    private void codecFile(FileManager lInFM, FileManager lOutFM) {
        FileInputStream lFiS;
        FileOutputStream lFoS;
        long FileSize;

        if (lInFM != null) {
            if ((lInFM.Exists()) && (lInFM.isFile())) {
                FileSize = lInFM.getFileLength();
                try {
                    lFiS = new FileInputStream(lInFM.getAbsoluteFilePath());
                    if (lOutFM == null) {
                        lFoS = new FileOutputStream(lInFM.getAbsoluteFilePath() + ".rs", false);
                    } else if (lOutFM.isDirectory()) {
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath() + lOutFM.getSeparador() + lInFM.getFileName() + ".rs", false);
                    } else if (!lOutFM.Exists()) {
                        lOutFM.CreateNewFile();
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath(), false);
                    } else if (lOutFM.isFile()) {
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath(), false);
                    } else {
                        lFoS = new FileOutputStream(lInFM.getAbsoluteFilePath() + ".rs", false);
                    }
                    this.codecStream(lFiS, lFoS, FileSize);
                } catch (FileNotFoundException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "031");
                    this.setProgress(ReedSolomon.ERROREMPTYINPUT);
                } catch (UtilsException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "032");
                    this.setProgress(ReedSolomon.ERRORNULLOUTPUT);
                }
            } else {
                this.setProgress(ReedSolomon.ERROREMPTYINPUT);
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
        }
    }

    /**
     * Decodifica el archivo
     */
    private void decodecFile(FileManager lInFM, FileManager lOutFM) {
        FileInputStream lFiS;
        FileOutputStream lFoS;
        long FileSize;
        String OutFileName;

        if (lInFM != null) {
            if ((lInFM.Exists()) && (lInFM.isFile())) {
                FileSize = lInFM.getFileLength();
                try {
                    lFiS = new FileInputStream(lInFM.getAbsoluteFilePath());
                    if (lInFM.getFileExtension().equals(".rs")) {
                        OutFileName = lInFM.getFileBaseName();
                    } else {
                        OutFileName = lInFM.getFileName() + ".out";
                    }
                    if (lOutFM == null) {
                        lFoS = new FileOutputStream(lInFM.getFilePath() + lInFM.getSeparador() + OutFileName, false);
                    } else if (lOutFM.isDirectory()) {
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath() + lOutFM.getSeparador() + OutFileName, false);
                    } else if (!lOutFM.Exists()) {
                        lOutFM.CreateNewFile();
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath(), false);
                    } else if (lOutFM.isFile()) {
                        lFoS = new FileOutputStream(lOutFM.getAbsoluteFilePath(), false);
                    } else {
                        lFoS = new FileOutputStream(lInFM.getFilePath() + lInFM.getSeparador() + OutFileName, false);
                    }
                    this.decodecStream(lFiS, lFoS, FileSize);
                } catch (FileNotFoundException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "033");
                    this.setProgress(ReedSolomon.ERROREMPTYINPUT);
                } catch (UtilsException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, ReedSolomon.CLASSID, "034");
                    this.setProgress(ReedSolomon.ERRORNULLOUTPUT);
                }
            } else {
                this.setProgress(ReedSolomon.ERROREMPTYINPUT);
            }
        } else {
            this.setProgress(ReedSolomon.ERRORNULLINPUT);
        }
    }

    @Override
    public void run() {
        FileManager lInFM, lOutFM;
        InputStream lInS;
        OutputStream lOutS;
        int lTask;

        this.rwl.readLock().lock();
        try {
            lInFM = this.InFM;
            lOutFM = this.OutFM;
            lInS = this.InS;
            lOutS = this.OutS;
            lTask = this.Task;
        } finally {
            this.rwl.readLock().unlock();
        }
        switch (lTask) {
            case CODECSTREAM:
                codecStream(lInS, lOutS, -1);
                break;
            case DECODECSTREAM:
                decodecStream(lInS, lOutS, -1);
                break;
            case CODECFILE:
                codecFile(lInFM, lOutFM);
                break;
            case DECODECFILE:
                decodecFile(lInFM, lOutFM);
                break;
            default:
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_FATAL, false, null, "Unknown Operation", ReedSolomon.CLASSID, "035");
                break;
        }
        this.rwl.writeLock().lock();
        try {
            this.Finished = true;
            this.InFM = null;
            this.OutFM = null;
            this.InS = null;
            this.OutS = null;
        } finally {
            this.rwl.writeLock().unlock();
            this.Ejecutor = null;
        }
    }
}
