package com.bolivartech.utils.lzma;

import com.bolivartech.utils.lzma.core.Base;
import com.bolivartech.utils.lzma.core.Decoder;
import com.bolivartech.utils.lzma.core.Encoder;
import com.bolivartech.utils.bits.BitsUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.files.FileManager;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.log.LoggerFormatter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
 * Copyright 2016 BolivarTech INC.</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's that implement BolivarTech's LZMA compression
 * Algoritms.</p>
 *
 * <p>
 * Implementa una clase que implementa el algoritmo de compresion LZMA.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHB"</li>
 * <li>Loc: 000-036</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @version 1.0.2 - March 25, 2016
 * @since 2016
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-02-06): Version Inicial.</li>
 * <li>v1.0.1 (2016-03-02): Se mejoro el manejo de extensiones en la version que
 * procesa archivos, para asegurar que la extension .lzma es usada por
 * defecto.</li>
 * <li>v1.0.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * </ul>
 */
public class BTLZMA implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHB";

    // Extension del archivo comprimido
    private final static String FEXT = "lzma";

    // Balores para el Match Finder
    public static final int BT2 = 0;
    public static final int BT4 = 1;
    public static final int BT4B = 2;

    // Error de Codificacion
    public static final int ERRORCODINGFAIL = -1;
    // Error que los stream son NULL
    public static final int ERRORNULLSTREAM = -2;
    // Error que el Match Finder no es valido
    public static final int ERRORINVALIDMATCHFINDER = -3;
    // Error de dimension de diccionario invalida
    public static final int ERRORINVALIDDICTIONARYSIZE = -4;
    // Error de parametros LC,LP y PB invalidos
    public static final int ERRORINVALIDLCLPPB = -5;
    // Error de Numero de Fast Bytes
    public static final int ERRORINVALIDNUMFASTBYTES = -6;
    // Error de Pb
    public static final int ERRORINVALIDPB = -7;
    // Error de Lc
    public static final int ERRORINVALIDLC = -8;
    // Error de Lp
    public static final int ERRORINVALIDLP = -9;
    // Error del Stream de Salida
    public static final int ERROROUTPUTSTREAM = -10;
    // Error del Stream de Entrada
    public static final int ERRORINPUTSTREAM = -11;
    // Error del Stream de Entrada y de Salida
    public static final int ERRORINPUTOUTPUTSTREAM = -12;
    // Error no se puede recuperar el Header del LZMA
    public static final int ERRORLZMAHEADER = -13;
    // Error Propierdades del LZMA son invalidas
    public static final int ERRORINVALIDLZMAPROPERTIES = -14;
    // Error Dimension del contenido del LZMA es invalido
    public static final int ERRORINVALIDLZMASIZE = -15;
    // Error no se puede descomprimir el Stream LZMA
    public static final int ERRORCANTUNCOMPRESSLZMA = -16;
    // Error el archivo de entrada no es archivo
    public static final int ERRORINPUTFILEISNOTFILE = -17;
    // Error al borrar archivo de salida
    public static final int ERRORDELETEOUTFILE = -18;

    // TimeOut de espera en milisegundos
    private final static long TIMEOUT = 1500;
    // Tamano por defecto del diccionario
    private final static int DEFAULDICSIZE = 1 << 23; // 8388608 bytes

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Manejador de Bitacoras
    private final LoggerFormatter BTLogF;

    // Hebra de Ejecucion
    private Thread Ejecutor = null;

    // Identificador de la hebra de ejecucion
    private long ThreadID;

    // Generador de Numeros Aleatorios
    private MersenneTwisterPlus Random;

    // Streams de entrada y salida
    @GuardedBy("rwl")
    private InputStream InS;
    @GuardedBy("rwl")
    private OutputStream OutS;
    // Si no esta en modo de marcar EoS se debe especificar el tamaño del Stream
    @GuardedBy("rwl")
    private long StreamSize;

    // Porcentaje de progreso
    @GuardedBy("rwl")
    private int Progress;
    // Bandera para indicar si se compreto la operacion
    @GuardedBy("rwl")
    private boolean Finished;
    // Bandera para indicar qu se interrumpe el proceso
    @GuardedBy("rwl")
    private boolean Interrupt;

    // Bandera para indicar si se comprime o se descomprime
    @GuardedBy("rwl")
    private boolean Compress;

    // Parametros de configuracion del LZMA
    @GuardedBy("rwl")
    private int Lc;
    @GuardedBy("rwl")
    private int Lp;
    @GuardedBy("rwl")
    private int Pb;
    // Numero de Fast Bytes
    @GuardedBy("rwl")
    private int Fb;
    // Tamano del Diccionario
    @GuardedBy("rwl")
    private int DictionarySize;
    @GuardedBy("rwl")
    private int MatchFinder;
    // Bandera para marcar End Of Stream
    @GuardedBy("rwl")
    private boolean EoS;

    /**
     * Constructor por defecto
     */
    public BTLZMA() {
        this(null);
    }

    /**
     * Constructor con inicializacion de bitacora
     *
     * @param vLog Apuntador a la bitacora
     */
    public BTLZMA(LoggerManager vLog) {

        Random = new MersenneTwisterPlus();
        this.BTLogF = LoggerFormatter.getInstance(vLog);
        this.InS = null;
        this.OutS = null;
        this.Progress = 0;
        this.Lc = 3;
        this.Lp = 0;
        this.Pb = 2;
        this.Fb = 128;
        this.DictionarySize = DEFAULDICSIZE;
        this.MatchFinder = BT4;
        this.Compress = true;
        this.Finished = false;
        this.Interrupt = false;
        this.EoS = true;
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

                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Uncaught Exception\n" + e.toString(), BTLZMA.CLASSID, "000");
                    Message = new StringBuffer();
                    if (t != null) {
                        for (StackTraceElement STE : t.getStackTrace()) {
                            Message.append(STE.toString());
                        }
                    } else {
                        Message.append("ERROR: Thread is NULL at StackTrace");
                    }
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Stack:\n" + Message.toString(), BTLZMA.CLASSID, "001");
                    StackTrace = new StringWriter();
                    e.printStackTrace(new PrintWriter(StackTrace));
                    BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "StackTrace: " + StackTrace.toString(), BTLZMA.CLASSID, "002");
                    DiffTime = 0;
                    StartTime = System.currentTimeMillis();
                    while ((t != null) && (t.getState() != Thread.State.TERMINATED) && (DiffTime < TIMEOUT)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTLZMA.CLASSID, "003");
                        } finally {
                            DiffTime = System.currentTimeMillis() - StartTime;
                        }
                    }
                    if (DiffTime >= TIMEOUT) {
                        BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Thread Finished by TimeOut", BTLZMA.CLASSID, "004");
                    }
                    if (Ejecutor != null) {
                        if (Ejecutor.getState() != Thread.State.TERMINATED) {
                            Ejecutor.interrupt();
                            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, null, "Forced Thread Finish by Interruption", BTLZMA.CLASSID, "005");
                        }
                        Ejecutor = null;
                    }
                    rwl.writeLock().lock();
                    try {
                        Finished = true;
                        Progress = ERRORCODINGFAIL;
                    } finally {
                        rwl.writeLock().unlock();
                    }
                }
            });
            if (this.EoS) {
                this.StreamSize = -1;
            }
            this.Progress = 0;
            this.Finished = false;
            this.Interrupt = false;
            this.Ejecutor.start();
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Can't start Thread because the previous still is Running", BTLZMA.CLASSID, "006");
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
    public void setProgress(int Progress) {

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
     * Retorna TRUE si se agrega el marcador de End Of Stream o FALSE si no.
     *
     * @return TRUE o FALSE para el EoS
     */
    public boolean isEoS() {
        boolean Result = false;

        this.rwl.readLock().lock();
        try {
            Result = this.EoS;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
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
     * Retorna el valor del Match Finder, que puede ser BT2, BT4 y BT4B.
     *
     * @return BT2, BT4 y BT4B.
     */
    public int getMatchFinder() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.MatchFinder;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el valor del Match Finder, que puede ser BT2, BT4 y BT4B.
     *
     * @param MatchFinder BT2, BT4 o BT4B.
     * @throws com.bolivartech.utils.exception.UtilsException
     */
    public void setMatchFinder(int MatchFinder) throws UtilsException {

        if ((MatchFinder >= BT2) && (MatchFinder <= BT4B)) {
            this.rwl.writeLock().lock();
            try {
                this.MatchFinder = MatchFinder;
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Invalid Match Finder", BTLZMA.ERRORINVALIDMATCHFINDER, BTLZMA.CLASSID + "007");
        }
    }

    /**
     * Retornala dimension del diccionario de compresion en bytes
     *
     * @return Dimension del diccionario en bytes
     */
    public int getDictionarySize() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.DictionarySize;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece la dimension del diccionario en bytes, entre un valor de 1 y
     * 536870912 bytes. Si el valor del diccionario no es potencia de dos, se
     * redondea al valor inmediato inferior que lo sea.
     *
     * @param DictionarySize entre 1 y 536870912 bytes
     */
    public void setDictionarySize(int DictionarySize) {
        int NumBitsDicSize, minDicSize, MaxDicSize;

        minDicSize = 1 << Base.kDicLogSizeMin;
        MaxDicSize = 1 << Base.kDicLogSizeMax;
        if (DictionarySize <= minDicSize) {
            DictionarySize = minDicSize;
        } else if (DictionarySize >= MaxDicSize) {
            DictionarySize = MaxDicSize;
        } else {
            NumBitsDicSize = BitsUtils.bitsRequired(DictionarySize);
            if (NumBitsDicSize <= 1) {
                DictionarySize = 1;
            } else {
                DictionarySize = 1 << (NumBitsDicSize - 1);
            }
            DictionarySize = (DictionarySize <= minDicSize ? minDicSize : DictionarySize);
            DictionarySize = (DictionarySize >= MaxDicSize ? MaxDicSize : DictionarySize);
        }
        this.rwl.writeLock().lock();
        try {
            this.DictionarySize = DictionarySize;
            //this.Logger(LoggerManager.TYPE_EVENT, LoggerManager.LEVEL_INFO, "Dictionary set to " + Integer.toString(this.DictionarySize) + " bytes");
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Retorna el parametro Lc del algoritmo LZMA.
     *
     * @return Lc
     */
    public int getLc() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.Lc;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el parametro Lc del algoritmo LZMA.
     *
     * @param Lc
     * @throws UtilsException
     */
    public void setLc(int Lc) throws UtilsException {

        if (Lc >= 0 || Lc <= Base.kNumLitContextBitsMax) {
            this.rwl.writeLock().lock();
            try {
                this.Lc = Lc;
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Invalid Lc", BTLZMA.ERRORINVALIDLC, BTLZMA.CLASSID + "008");
        }
    }

    /**
     * Retorna el parametro Lp del algoritmo LZMA.
     *
     * @return Lp
     */
    public int getLp() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.Lp;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el parametro Lp del algoritmo LZMA.
     *
     * @param Lp
     * @throws UtilsException
     */
    public void setLp(int Lp) throws UtilsException {

        if ((Lp >= 0) && (Lp <= Base.kNumLitPosStatesBitsEncodingMax)) {
            this.rwl.writeLock().lock();
            try {
                this.Lp = Lp;
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Invalid Lp", BTLZMA.ERRORINVALIDLP, BTLZMA.CLASSID + "009");
        }
    }

    /**
     * Retorna el parametro Pb del algoritmo LZMA.
     *
     * @return Pb
     */
    public int getPb() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.Pb;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el parametro Pb del algoritmo LZMA.
     *
     * @param Pb
     * @throws UtilsException
     */
    public void setPb(int Pb) throws UtilsException {

        if ((Pb >= 0) && (Pb <= Base.kNumPosStatesBitsEncodingMax)) {
            this.rwl.writeLock().lock();
            try {
                this.Pb = Pb;
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Invalid Pb", BTLZMA.ERRORINVALIDPB, BTLZMA.CLASSID + "010");
        }
    }

    /**
     * Establece el parametros Lc, Lp y Pb del algoritmo LZMA.
     *
     * @param Lc
     * @param Lp
     * @param Pb
     * @throws UtilsException
     */
    public void setLcLpPb(int Lc, int Lp, int Pb) throws UtilsException {

        this.setLc(Lc);
        this.setLp(Lp);
        this.setPb(Pb);
    }

    /**
     * Retorna el numero de FastBytes del algoritmo. Por defecto es 128
     *
     * @return Numero de FastBytes
     */
    public int getFastBytes() {
        int Result;

        this.rwl.readLock().lock();
        try {
            Result = this.Fb;
        } finally {
            this.rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Establece el numero de FastBytes del algoritmo
     *
     * @param Fb
     * @throws UtilsException
     */
    public void setFastBytes(int Fb) throws UtilsException {

        if ((Fb >= 5) && (Fb <= Base.kMatchMaxLen)) {
            this.rwl.writeLock().lock();
            try {
                this.Fb = Fb;
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: Invalid Fast Byte Numbers ", BTLZMA.ERRORINVALIDNUMFASTBYTES, BTLZMA.CLASSID + "011");
        }
    }

    /**
     * Realiza el proceso de compresion de InputStream segun los parametros del
     * algoritmo LZMA y lo guarda en el OutputStream. Este metodo no bloquea
     * durante su ejecucion.
     *
     * @param Input Stream de entrada a comprimir
     * @param Output Stream de salida comprimido
     * @throws UtilsException si los stream son NULL
     */
    public void Compress(InputStream Input, OutputStream Output) throws UtilsException {

        if ((Input != null) && (Output != null)) {
            this.rwl.writeLock().lock();
            try {
                this.InS = Input;
                this.OutS = Output;
                this.EoS = true;
                this.Compress = true;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Stream is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "012");
        }
    }

    /**
     * Realiza el proceso de compresion del arreglo de bytes Input segun los
     * parametros del algoritmo LZMA y retorna la salida comprimida o NULL si no
     * se pudo realizar el proceso. Este metodo bloquea durante su ejecucion.
     *
     * @param Input Array de bytes de entrada a comprimir
     * @return Array de bytes de salida comprimido o NULL
     * @throws UtilsException si el arreglo de entrada es NULL
     */
    public byte[] Compress(byte[] Input) throws UtilsException {
        ByteArrayInputStream InputStream;
        ByteArrayOutputStream OutputStream;
        int lProgress;
        byte[] Result = null;

        if (Input != null) {
            InputStream = new ByteArrayInputStream(Input);
            OutputStream = new ByteArrayOutputStream();
            this.rwl.writeLock().lock();
            try {
                this.InS = InputStream;
                this.OutS = OutputStream;
                this.EoS = false;
                this.StreamSize = Input.length;
                this.Compress = true;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
            while (!this.isDone()) {
                EnvironmentUtils.randomSleep(500);
            }
            lProgress = this.getProgress();
            if (lProgress < 0) {
                throw new UtilsException("ERROR: Compression Failed", BTLZMA.ERRORCODINGFAIL, BTLZMA.CLASSID + "013");
            } else {
                Result = OutputStream.toByteArray();
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Stream is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "014");
        }
        return Result;
    }

    /**
     * Realiza el proceso de compresion de los archivos segun los parametros del
     * algoritmo LZMA y lo guarda en el OutputStream. Si Output es NULL se
     * genera un archivo con el mismo nombre y path de la entrada pero con la
     * extension lzma; si es un directorio se crea el archivo con el nombre de
     * entrada y extension lzma en esa ruta. Este metodo no bloquea durante su
     * ejecucion.
     *
     * @param Input Archivo de entrada a comprimir
     * @param Output Archivo de salida comprimido o ruta donde guardarlo o NULL
     * @throws UtilsException si los stream son NULL
     */
    public void Compress(FileManager Input, FileManager Output) throws UtilsException {
        String inputFileName, inputFilePath, outputFileName, Separador;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;

        if (Input != null) {
            if (Input.isFile()) {
                inputFileName = Input.getFileName();
                inputFilePath = Input.getFilePath();
                Separador = Input.getSeparador();
                try {
                    inStream = new BufferedInputStream(new FileInputStream(new File(inputFilePath + Separador + inputFileName)));
                    if (Output == null) {
                        outputFileName = new String(inputFilePath + Separador + inputFileName + "." + FEXT);
                        Output = new FileManager(outputFileName);
                    } else if ((Output != null) && (Output.isDirectory())) {
                        outputFileName = new String(Output.getAbsoluteFilePath() + Output.getSeparador() + inputFileName + "." + FEXT);
                        Output = new FileManager(outputFileName);
                    } else {
                        if (!Output.Exists()) {
                            Output.CreateNewFile();
                        }
                        outputFileName = Output.getAbsoluteFilePath();
                        if (outputFileName == null) {
                            outputFileName = new String(inputFilePath + Separador + Output.getFileName());
                        }
                    }
                    if (Output.Exists()) {
                        if (!Output.Delete()) {
                            // Error al eliminar archivo de salida existente
                            this.Progress = ERRORDELETEOUTFILE;
                            throw new UtilsException("ERROR: CAN'T Delete Existing Output File", ERRORDELETEOUTFILE, BTLZMA.CLASSID + "015");
                        }
                    }
                    outStream = new BufferedOutputStream(new FileOutputStream(new File(outputFileName)));
                    this.rwl.writeLock().lock();
                    try {
                        this.InS = inStream;
                        this.OutS = outStream;
                        this.EoS = false;
                        this.StreamSize = Input.getFileLength();
                        this.Compress = true;
                        this.StartThread();
                    } finally {
                        this.rwl.writeLock().unlock();
                    }
                } catch (FileNotFoundException ex) {
                    throw new UtilsException(ex.getMessage(), BTLZMA.ERRORINPUTFILEISNOTFILE, BTLZMA.CLASSID + "016");
                }
            } else {
                this.Progress = BTLZMA.ERRORINPUTFILEISNOTFILE;
                throw new UtilsException("ERROR: Input File NOT IS FILE", BTLZMA.ERRORINPUTFILEISNOTFILE, BTLZMA.CLASSID + "017");
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Input File is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "018");
        }
    }

    /**
     * Realiza el proceso de decompresion de InputStream segun los parametros
     * del algoritmo LZMA contenidos en el stream y lo guarda en el
     * OutputStream. Este metodo no bloquea durante su ejecucion.
     *
     * @param Input Stream de entrada a decomprimir
     * @param Output Stream de salida decomprimido
     * @throws UtilsException si los stream son NULL
     */
    public void Decompress(InputStream Input, OutputStream Output) throws UtilsException {

        if ((Input != null) && (Output != null)) {
            this.rwl.writeLock().lock();
            try {
                this.InS = Input;
                this.OutS = Output;
                this.EoS = true;
                this.Compress = false;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Stream is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "019");
        }
    }

    /**
     * Realiza el proceso de decompresion del arreglo de bytes Input segun los
     * parametros del algoritmo LZMA y retorna la salida comprimida o NULL si no
     * se pudo realizar el proceso. Este metodo bloquea durante su ejecucion.
     *
     * @param Input Array de bytes de entrada comprimido con LZMA
     * @return Array de bytes de salida decomprimido o NULL
     * @throws UtilsException si el arreglo de entrada es NULL
     */
    public byte[] Decompress(byte[] Input) throws UtilsException {
        ByteArrayInputStream InputStream;
        ByteArrayOutputStream OutputStream;
        int lProgress;
        byte[] Result = null;

        if (Input != null) {
            InputStream = new ByteArrayInputStream(Input);
            OutputStream = new ByteArrayOutputStream();
            this.rwl.writeLock().lock();
            try {
                this.InS = InputStream;
                this.OutS = OutputStream;
                this.EoS = false;
                this.StreamSize = Input.length;
                this.Compress = false;
                this.StartThread();
            } finally {
                this.rwl.writeLock().unlock();
            }
            while (!this.isDone()) {
                EnvironmentUtils.randomSleep(500);
            }
            lProgress = this.getProgress();
            if (lProgress < 0) {
                throw new UtilsException("ERROR: Compression Failed", BTLZMA.ERRORCODINGFAIL, BTLZMA.CLASSID + "020");
            } else {
                Result = OutputStream.toByteArray();
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Stream is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "021");
        }
        return Result;
    }

    /**
     * Realiza el proceso de decompresion de los archivos segun los parametros
     * del algoritmo LZMA y lo guarda en el OutputStream. Si Output es NULL se
     * genera un archivo con el mismo nombre y path de la entrada pero con la
     * extension out; si es un directorio se crea el archivo con el nombre de
     * entrada y extension out en esa ruta. Este metodo no bloquea durante su
     * ejecucion.
     *
     * @param Input Archivo de entrada a decomprimir
     * @param Output Archivo de salida decomprimido o ruta donde guardarlo o
     * NULL
     * @throws UtilsException si los stream son NULL
     */
    public void Decompress(FileManager Input, FileManager Output) throws UtilsException {
        String inputFileName, inputFilePath, inputFileExt, outputFileName, Separador;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;

        if (Input != null) {
            if (Input.isFile()) {
                inputFileName = Input.getFileName();
                inputFilePath = Input.getFilePath();
                inputFileExt = Input.getFileExtension();
                Separador = Input.getSeparador();
                try {
                    inStream = new BufferedInputStream(new FileInputStream(new File(inputFilePath + Separador + inputFileName)));
                    if (Output == null) {
                        if (inputFileExt.equals("." + FEXT)) {
                            outputFileName = new String(inputFilePath + Separador + Input.getFileBaseName());
                        } else {
                            outputFileName = new String(inputFilePath + Separador + Input.getFileBaseName() + ".out");
                        }
                        Output = new FileManager(outputFileName);
                    } else if ((Output != null) && (Output.isDirectory())) {
                        if (inputFileExt.equals("." + FEXT)) {
                            outputFileName = new String(Output.getAbsoluteFilePath() + Output.getSeparador() + Input.getFileBaseName());
                        } else {
                            outputFileName = new String(Output.getAbsoluteFilePath() + Output.getSeparador() + Input.getFileBaseName() + ".out");
                        }
                        Output = new FileManager(outputFileName);
                    } else {
                        if (!Output.Exists()) {
                            Output.CreateNewFile();
                        }
                        outputFileName = Output.getAbsoluteFilePath();
                        if (outputFileName == null) {
                            outputFileName = new String(inputFilePath + Separador + Output.getFileName());
                        }
                    }
                    if (Output.Exists()) {
                        if (!Output.Delete()) {
                            // Error al eliminar archivo de salida existente
                            this.Progress = ERRORDELETEOUTFILE;
                            throw new UtilsException("ERROR: CAN'T Delete Existing Output File", ERRORDELETEOUTFILE, BTLZMA.CLASSID + "022");
                        }
                    }
                    outStream = new BufferedOutputStream(new FileOutputStream(new File(outputFileName)));
                    this.rwl.writeLock().lock();
                    try {
                        this.InS = inStream;
                        this.OutS = outStream;
                        this.EoS = false;
                        this.StreamSize = Input.getFileLength();
                        this.Compress = false;
                        this.StartThread();
                    } finally {
                        this.rwl.writeLock().unlock();
                    }
                } catch (FileNotFoundException ex) {
                    throw new UtilsException(ex.getMessage(), BTLZMA.ERRORINPUTFILEISNOTFILE, BTLZMA.CLASSID + "023");
                }
            } else {
                this.Progress = BTLZMA.ERRORINPUTFILEISNOTFILE;
                throw new UtilsException("ERROR: Input File NOT IS FILE", BTLZMA.ERRORINPUTFILEISNOTFILE, BTLZMA.CLASSID + "024");
            }
        } else {
            this.Progress = BTLZMA.ERRORNULLSTREAM;
            throw new UtilsException("ERROR: Input File is NULL", BTLZMA.ERRORNULLSTREAM, BTLZMA.CLASSID + "025");
        }
    }

    /**
     * Ejecutor de la hebra
     */
    @Override
    public void run() {
        boolean lCompress, lEoS;
        int i, temp, lLc, lLp, lPb, lFb, lDicSize, lMatchFinder;
        int Result;
        long lStreamSize;
        InputStream lInS;
        OutputStream lOutS;
        Encoder encoderLZMA;
        Decoder decoderLZMA;
        int LZMApropertiesSize;
        byte[] LZMAproperties;

        Result = 0;
        this.rwl.readLock().lock();
        try {
            lInS = this.InS;
            lOutS = this.OutS;
            lLc = this.Lc;
            lLp = this.Lp;
            lPb = this.Pb;
            lFb = this.Fb;
            lDicSize = this.DictionarySize;
            lCompress = this.Compress;
            lMatchFinder = this.MatchFinder;
            lEoS = this.EoS;
            lStreamSize = this.StreamSize;
        } finally {
            this.rwl.readLock().unlock();
        }
        if (lCompress) {
            // Realiza el proceso de compresion de los Stream
            encoderLZMA = new Encoder();
            if (encoderLZMA.SetMatchFinder(lMatchFinder)) {
                if (encoderLZMA.SetDictionarySize(lDicSize)) {
                    if (encoderLZMA.SetLcLpPb(lLc, lLp, lPb)) {
                        if (encoderLZMA.SetNumFastBytes(lFb)) {
                            try {
                                encoderLZMA.SetEndMarkerMode(lEoS);
                                // Escribe los parametros del LZMA en el Stream de Salida
                                encoderLZMA.WriteCoderProperties(lOutS);
                                // Escribe la dimension del stream contenido en el LZMA
                                for (i = 0; i < 8; i++) {
                                    lOutS.write((int) (lStreamSize >>> (8 * i)) & 0xFF);
                                }
                                // Realiza la compresion del InputStream en el OutputStream
                                encoderLZMA.Code(lInS, lOutS, lStreamSize, this);
                            } catch (IOException ex) {
                                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTLZMA.CLASSID, "026");
                                Result = BTLZMA.ERROROUTPUTSTREAM;
                            }
                        } else {
                            Result = BTLZMA.ERRORINVALIDNUMFASTBYTES;
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: Invalid Fast Bytes Number", BTLZMA.CLASSID, "027");
                        }
                    } else {
                        Result = BTLZMA.ERRORINVALIDLCLPPB;
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: Invalid LC, LP y PB parameters", BTLZMA.CLASSID, "028");
                    }
                } else {
                    Result = BTLZMA.ERRORINVALIDDICTIONARYSIZE;
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: Invalid Dictionary Size", BTLZMA.CLASSID, "029");
                }
            } else {
                Result = BTLZMA.ERRORINVALIDMATCHFINDER;
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: Invalid Match Finder", BTLZMA.CLASSID, "030");
            }
        } else {
            // Realiza el proceso de descompresion de los Stream
            LZMApropertiesSize = 5;
            LZMAproperties = new byte[LZMApropertiesSize];
            try {
                if (lInS.read(LZMAproperties, 0, LZMApropertiesSize) == LZMApropertiesSize) {
                    decoderLZMA = new Decoder();
                    if (decoderLZMA.SetDecoderProperties(LZMAproperties)) {
                        lStreamSize = 0;
                        for (i = 0; (i < 8); i++) {
                            temp = lInS.read();
                            if (temp >= 0) {
                                lStreamSize |= ((long) temp) << (8 * i);
                            } else {
                                i = 8;
                                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: CAN'T Recover LZMA Content Size", BTLZMA.CLASSID, "031");
                                Result = BTLZMA.ERRORINVALIDLZMASIZE;
                            }
                        }
                        if (Result == 0) {
                            if (!decoderLZMA.Decode(lInS, lOutS, lStreamSize, this)) {
                                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: CAN'T Uncompress LZMA", BTLZMA.CLASSID, "032");
                                Result = BTLZMA.ERRORCANTUNCOMPRESSLZMA;
                            }
                        }
                    } else {
                        this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: CAN'T invalid LZMA properties", BTLZMA.CLASSID, "033");
                        Result = BTLZMA.ERRORINVALIDLZMAPROPERTIES;
                    }
                } else {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "ERROR: CAN'T recover LZMA header", BTLZMA.CLASSID, "034");
                    Result = BTLZMA.ERRORLZMAHEADER;
                }
            } catch (IOException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTLZMA.CLASSID, "035");
                Result = BTLZMA.ERRORINPUTSTREAM;
            }
        }
        // Vacia y cierra el Stream de Salida y cierra el Stream de Entrada
        try {
            lOutS.flush();
            lOutS.close();
            lInS.close();
        } catch (IOException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, BTLZMA.CLASSID, "036");
            Result = BTLZMA.ERRORINPUTOUTPUTSTREAM;
        } finally {
            this.rwl.writeLock().lock();
            try {
                if (Result < 0) {
                    this.Progress = Result;
                }
                this.EoS = true;
                this.Finished = true;
                this.Ejecutor = null;
            } finally {
                this.rwl.writeLock().unlock();
            }
        }
    }
}
