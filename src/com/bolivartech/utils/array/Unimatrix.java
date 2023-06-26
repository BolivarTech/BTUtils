package com.bolivartech.utils.array;

import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.sort.Sortable;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech INC. </p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's that define the Unimatrix (Object Container)
 * to allow manage arrays of objects with a maximun capacity of
 * 4611686014132420609 grids.</p>
 *
 * <p>
 * The elements at the Unimatrix are stored in grids.</p>
 *
 * <p>
 * Define un contenedor de Objetos que permite almacenar un arreglo con una
 * capacidad maxima de 4611686014132420609 cuadriculas.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFH3"</li>
 * <li>Loc: 000-049</li>
 * <ul>
 *
 * @author Julian Bolivar
 * @since 2015 - May 19, 2016.
 * @version 1.1.2
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2015-02-01) Version Inicial.</li>
 * <li>v1.1.0 (2015-09-17) Se agrego el soporte para ordenar y realizar
 * busquedas de elementos en la Unimatrix.</li>
 * <li>v1.1.1 (2015-09-26) Se solvento un bug en condiciones de borde en la
 * busqueda de elementos en la Unimatrix.</li>
 * <li>v1.1.1 (2016-03-25) Se implemento el Class ID y el localizador de
 * posicion de errores.</li>
 * <li>v1.1.2 (2016-05-19) Se soluciono una error en la busqueda por celing y
 * floor en los extremos del arreglo.</li>
 * </ul>
 */
public class Unimatrix {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH3";

    // Mascara de capacidad maxima de un entero
    private static final int INTLENGMASK = 0x7FFFFFFF;

    // Mascara de capacidad maxima de un Long
    private static final long LONGLENGMASK = 0x7FFFFFFFFFFFFFFFL;

    // Error de sin memoria en el sistema
    public static final int ERROROUTOFMEMORY = 0x01;
    // Error el Grid esta fuera de rango
    public static final int ERRORGRIDOUTOFRANGE = 0x02;
    // Error la unimatrix es NULL
    public static final int ERRORUNIMATRIXISNULL = 0x04;
    // Error Elemento de la unimatrix no es sortable
    public static final int ERRORELEMENTNOTSORTABLE = 0x08;
    // Error Unimatrix no esta Ordenada
    public static final int ERRORUNIMATRIXNOTSORTED = 0x10;

    /**
     * Indica que no se encontro el elemento buscado
     */
    public static final int ITEMNOTFOUND = -1;

    /**
     * Indica si el orden de la Unimatrix es ascendente
     */
    public final static int ASCENDING = 0x02;
    /**
     * Indica si el orden de la Unimatrix es descendente
     */
    public final static int DESCENDING = 0x00;
    /**
     * Indica que se va a utilizar el ordenamiento Absoluto de la Unimatrix
     */
    public final static int SORTABSOLUTE = 0x01;
    /**
     * Indica que se va a utilizar el ordenamiento Relativo de la Unimatrix
     */
    public final static int SORTRELATIVE = 0x00;
    /**
     * Indica que se va a utilizar el ordenamiento usando la metrica Long de los
     * elementos de la Unimatrix
     */
    public final static int METRICLONG = 0x04;
    /**
     * Indica que se va a utilizar el ordenamiento usando la metrica Double de
     * los elementos de la Unimatrix
     */
    public final static int METRICDOUBLE = 0x00;
    /**
     * Indica que se va a utilizar el busqueda Absoluto de la Unimatrix
     */
    public final static int SEARCHABSOLUTE = 0x01;
    /**
     * Indica que se va a utilizar el busqueda Relativo de la Unimatrix
     */
    public final static int SEARCHRELATIVE = 0x00;
    /**
     * Indica que se va a utilizar el busqueda Exacta de la Unimatrix
     */
    public final static int SEARCHEXACT = 0x00;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo de la
     * Unimatrix, puede ser hacia arriba o hacia abajo
     */
    public final static int SEARCHNEAR = 0x02;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo hacia abajo
     * de la Unimatrix
     */
    public final static int SEARCHFLOOR = 0x08;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo hacia
     * arriba de la Unimatrix
     */
    public final static int SEARCHCEILING = 0x10;

    // Los lock para el manejo de concurrencia
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    // Los lock para el manejo de ordenamiento
    private final ReentrantReadWriteLock sortrwl = new ReentrantReadWriteLock();

    // Contenedor de Objetos
    @GuardedBy("rwl")
    private Object[][] UniJunctions = null;

    // Capacidad de la UniMatrix
    @GuardedBy("rwl")
    private long UniMatrixCapacity = 0;

    // Bandera para indicar si la Unimatrix esta ordenada
    @GuardedBy("sortrwl")
    private boolean Sorted;

    /**
     * Constructor con inicializacion de la capacidad de la Unimatrix
     *
     * @param Capacity de la Unimatrix
     */
    public Unimatrix(long Capacity) {

        this.Sorted = false;
        try {
            setCapacity(Capacity);
        } catch (UtilsException ex) {
            Logger.getLogger(Unimatrix.class.getName()).log(Level.SEVERE, null, ex);
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
     * Retorna la capacidad de la Unimatrix o el numero de Grids que puede
     * contener
     *
     * @return Capacida de la Unimatrix
     */
    public final long getCapacity() {
        long Capacity;

        Capacity = 0;
        rwl.readLock().lock();
        try {
            Capacity = this.UniMatrixCapacity;
        } finally {
            rwl.readLock().unlock();
        }
        return Capacity;
    }

    /**
     * Retorna TRUE si la unimatrix esta ordenada o FALSE si no
     *
     * @return TRUE Ordenada, FALSE si no.
     */
    public final boolean isSorted() {
        boolean Result = false;

        sortrwl.readLock().lock();
        try {
            Result = this.Sorted;
        } finally {
            sortrwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Metodo privado que calcula la capacidad real de la UniMatrix en Memoria.
     *
     * NOTA: Este metodo no es Thread Safe dentro de la UniMatrix.
     *
     * @return Cantidad de Grids contenidas en la UniMatrix
     */
    private long CalcCapacity() {
        long Capacity = 0;
        int NumUniJunctions, PosU;

        if (this.UniJunctions != null) {
            NumUniJunctions = this.UniJunctions.length;
            for (PosU = 0; PosU < NumUniJunctions; PosU++) {
                Capacity += this.UniJunctions[PosU].length;
            }
        }
        return Capacity;
    }

    /**
     * Establece la capacidad de la Unimatrix o el maximo numero de Grids que
     * puede contener.
     *
     * Si la UniMatrix ya contenia datos y la capacidad es menor a la anterior
     * se trunca al nuevo valor, si es mayor se redimensiona a la nueva
     * capacidad siempre manteniendo los datos almacenados el la Unimatrix
     * orignal pero ajustados a la nueva capacidad si esta fue truncada.
     *
     * @param Capacity Capacidad de la Unimatrix en Grids
     * @throws UtilsException
     */
    public final void setCapacity(long Capacity) throws UtilsException {
        Object[][] OrgUniJunctions = null;
        int NumUniJunctions, NumGrids, PosU, PosG, Temp;
        boolean Loocked = false;

        // Se asegura que la capasidad no sea negativa
        Capacity = Capacity & LONGLENGMASK;
        // Calcula el numero de UniJuntions que va a necesitar la Unimatrix
        NumUniJunctions = (int) (Capacity / (long) INTLENGMASK);
        if (((int) (Capacity % (long) INTLENGMASK)) > 0) {
            NumUniJunctions++;
        }
        try {
            Loocked = sortrwl.writeLock().tryLock();
            this.Sorted = false;
        } finally {
            if (Loocked) {
                sortrwl.writeLock().unlock();
                Loocked = false;
            }
        }
        rwl.writeLock().lock();
        try {
            if (this.UniJunctions == null) {
                // Crea una Unimatrix nueva
                this.UniJunctions = new Object[NumUniJunctions][];
                Temp = NumUniJunctions - 1;
                PosU = 0;
                while (PosU < Temp) {
                    this.UniJunctions[PosU] = new Object[INTLENGMASK];
                    // Inicializa con NULL
                    PosG = 0;
                    while (PosG < INTLENGMASK) {
                        this.UniJunctions[PosU][PosG] = null;
                        PosG++;
                    }
                    PosU++;
                }
                NumGrids = ((int) (Capacity % (long) INTLENGMASK));
                if (NumGrids == 0) {
                    NumGrids = INTLENGMASK;
                }
                this.UniJunctions[PosU] = new Object[NumGrids];
                // Inicializa con NULL
                PosG = 0;
                while (PosG < NumGrids) {
                    this.UniJunctions[PosU][PosG] = null;
                    PosG++;
                }
            } else {
                // Redimensiona la Unimatrix existente
                OrgUniJunctions = this.UniJunctions;
                this.UniJunctions = new Object[NumUniJunctions][];
                // Determina cual es la menor longitud de elementos entre el original y el nuevo
                Temp = Math.min(NumUniJunctions, OrgUniJunctions.length);
                Temp -= 1;
                // Copia los primeros UniJunctios del original al nuevo 
                PosU = 0;
                while (PosU < Temp) {
                    this.UniJunctions[PosU] = OrgUniJunctions[PosU];
                    PosU++;
                }
                if (NumUniJunctions <= OrgUniJunctions.length) {
                    // La nueva unimatrix es menor o igual a la anterior
                    // se hace el truncado de la anterior
                    NumGrids = ((int) (Capacity % (long) INTLENGMASK));
                    if (NumGrids == 0) {
                        NumGrids = INTLENGMASK;
                    }
                    this.UniJunctions[PosU] = new Object[NumGrids];
                    // Copia los grids anteriores
                    Temp = Math.min(NumGrids, OrgUniJunctions[PosU].length);
                    PosG = 0;
                    while (PosG < Temp) {
                        this.UniJunctions[PosU][PosG] = OrgUniJunctions[PosU][PosG];
                        PosG++;
                    }
                    // Completa con NULL
                    while (PosG < NumGrids) {
                        this.UniJunctions[PosU][PosG] = null;
                        PosG++;
                    }
                } else {
                    // La nueva Unimatrix es mayor a la anterior
                    this.UniJunctions[PosU] = new Object[INTLENGMASK];
                    // Copia lo valores anteriores
                    Temp = OrgUniJunctions[PosU].length;
                    PosG = 0;
                    while (PosG < Temp) {
                        this.UniJunctions[PosU][PosG] = OrgUniJunctions[PosU][PosG];
                        PosG++;
                    }
                    // Completa con NULL
                    while (PosG < INTLENGMASK) {
                        this.UniJunctions[PosU][PosG] = null;
                        PosG++;
                    }
                    // Agrega las UniJunctios Restantes
                    PosU++;
                    while (PosU < NumUniJunctions - 1) {
                        this.UniJunctions[PosU] = new Object[INTLENGMASK];
                        // Inicializa con NULL
                        PosG = 0;
                        while (PosG < INTLENGMASK) {
                            this.UniJunctions[PosU][PosG] = null;
                            PosG++;
                        }
                        PosU++;
                    }
                    // Completa la Ultima UniJunction
                    NumGrids = ((int) (Capacity % (long) INTLENGMASK));
                    if (NumGrids == 0) {
                        NumGrids = INTLENGMASK;
                    }
                    this.UniJunctions[PosU] = new Object[NumGrids];
                    // Inicializa con NULL
                    PosG = 0;
                    while (PosG < NumGrids) {
                        this.UniJunctions[PosU][PosG] = null;
                        PosG++;
                    }
                }
            }
            this.UniMatrixCapacity = this.CalcCapacity();
        } catch (OutOfMemoryError e) {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage HeapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage NoHeapUsage = memoryBean.getNonHeapMemoryUsage();
            long maxMemory = NoHeapUsage.getMax() / 1048576;
            long usedMemory = NoHeapUsage.getUsed() / 1048576;
            long HmaxMemory = HeapUsage.getMax() / 1048576;
            long HusedMemory = HeapUsage.getUsed() / 1048576;
            if (OrgUniJunctions != null) {
                // Recobra el UniMatrix anterior
                this.UniJunctions = OrgUniJunctions;
                this.UniMatrixCapacity = this.CalcCapacity();
            } else {
                this.UniJunctions = null;
                this.UniMatrixCapacity = 0;
            }
            throw new UtilsException("ERROR: UniMatrix Out of Memory\n Heap Memory: (" + Long.toString(HusedMemory) + "MB)/(" + Long.toString(HmaxMemory) + "MB)\n No Heap Memory (" + Long.toString(usedMemory) + "MB)/(" + Long.toString(maxMemory) + "MB)", Unimatrix.ERROROUTOFMEMORY, Unimatrix.CLASSID + "000");
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Alamcena "Element" en la posicion "Grid" de la Unimatrix
     *
     * @param Grid Posicion donde establece el elemento
     * @param Element Elemento a Agregar a la Unimatrix
     * @throws UtilsException
     */
    public void setElement(long Grid, Object Element) throws UtilsException {
        int PosU, PosG;
        boolean Loocked = false;

        if (this.UniJunctions != null) {
            // Se asegura que la Grid no sea negativa
            Grid = Grid & LONGLENGMASK;
            // Calcula el UniJunction donde esta el elemento
            PosU = (int) (Grid / (long) INTLENGMASK);
            PosG = ((int) (Grid % (long) INTLENGMASK));
            rwl.writeLock().lock();
            try {
                // Verifica si la UniJunction esta dentro del rango
                if (PosU < this.UniJunctions.length) {
                    if (this.UniJunctions[PosU] != null) {
                        if (PosG < this.UniJunctions[PosU].length) {
                            try {
                                Loocked = sortrwl.writeLock().tryLock();
                                this.Sorted = false;
                            } finally {
                                if (Loocked) {
                                    sortrwl.writeLock().unlock();
                                    Loocked = false;
                                }
                            }
                            this.UniJunctions[PosU][PosG] = Element;
                        } else {
                            throw new UtilsException("ERROR: Grid " + Long.toString(Grid) + " out of range at UniJunction " + Integer.toString(PosU) + " in the Unimatrix", Unimatrix.ERRORGRIDOUTOFRANGE, Unimatrix.CLASSID + "001");
                        }
                    } else {
                        throw new UtilsException("ERROR: UniJunction " + Integer.toString(PosU) + " is NULL in the Unimatrix", Unimatrix.ERRORUNIMATRIXISNULL, Unimatrix.CLASSID + "002");
                    }
                } else {
                    throw new UtilsException("ERROR: Grid " + Long.toString(Grid) + " out of range at UniJunctions level in the Unimatrix", Unimatrix.ERRORGRIDOUTOFRANGE, Unimatrix.CLASSID + "003");
                }
            } finally {
                rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: UniJunctions is NULL in the Unimatrix", Unimatrix.ERRORUNIMATRIXISNULL, Unimatrix.CLASSID + "004");
        }
    }

    /**
     * Retorna el Elemento contenido en la posicion "Grid" de la Unimatrix
     *
     * @param Grid Posicion de donde recuperar el elemento
     * @return Elemento contenido en el "Grid" especificada o NULL si esta vacio
     * @throws UtilsException
     */
    public Object getElement(long Grid) throws UtilsException {
        Object Element = null;
        int PosU, PosG;

        if (this.UniJunctions != null) {
            // Se asegura que la Grid no sea negativa
            Grid = Grid & LONGLENGMASK;
            // Calcula el UniJunction donde esta el elemento
            PosU = (int) (Grid / (long) INTLENGMASK);
            PosG = ((int) (Grid % (long) INTLENGMASK));
            rwl.readLock().lock();
            try {
                // Verifica si la UniJunction esta dentro del rango
                if (PosU < this.UniJunctions.length) {
                    if (this.UniJunctions[PosU] != null) {
                        if (PosG < this.UniJunctions[PosU].length) {
                            Element = this.UniJunctions[PosU][PosG];
                        } else {
                            throw new UtilsException("ERROR: Grid " + Long.toString(Grid) + " out of range at UniJunction " + Integer.toString(PosU) + " in the Unimatrix", Unimatrix.ERRORGRIDOUTOFRANGE, Unimatrix.CLASSID + "005");
                        }
                    } else {
                        throw new UtilsException("ERROR: UniJunction " + Integer.toString(PosU) + " is NULL in the Unimatrix", Unimatrix.ERRORUNIMATRIXISNULL, Unimatrix.CLASSID + "006");
                    }
                } else {
                    throw new UtilsException("ERROR: Grid " + Long.toString(Grid) + " out of range at UniJunctions level in the Unimatrix", Unimatrix.ERRORGRIDOUTOFRANGE, Unimatrix.CLASSID + "007");
                }
            } finally {
                rwl.readLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: UniJunctions is NULL in the Unimatrix", Unimatrix.ERRORUNIMATRIXISNULL, Unimatrix.CLASSID + "008");
        }
        return Element;
    }

    /**
     * Defragmenta la Unimatrix compactando todos los registros al comienzo de
     * la matriz.
     *
     * @return La grid con la primera posicion NULL en la Unimatrix o -1 si no
     * hay nulls
     * @throws UtilsException
     */
    public long Defrag() throws UtilsException {
        int PosU, PosG, SeekU, SeekG, NumUniJunctions, PosNumGrids, SeekNumGrids;
        long Local;

        PosU = 0;
        PosG = -1;
        if (this.UniJunctions != null) {
            rwl.writeLock().lock();
            sortrwl.writeLock().lock();
            try {
                NumUniJunctions = this.UniJunctions.length;
                // Recorre todas las UniJunctions
                SeekU = 0;
                SeekG = 0;
                SeekNumGrids = this.UniJunctions[SeekU].length;
                // Busca un null en las grids de las UniJunctions                    
                while ((SeekU < NumUniJunctions) && (SeekG < SeekNumGrids) && (this.UniJunctions[SeekU][SeekG] != null)) {
                    SeekG++;
                    if (SeekG == SeekNumGrids) {
                        SeekU++;
                        if (SeekU < NumUniJunctions) {
                            SeekNumGrids = this.UniJunctions[SeekU].length;
                            SeekG = 0;
                        }
                    }
                }
                if ((SeekU < NumUniJunctions) && (SeekG < SeekNumGrids) && (this.UniJunctions[SeekU][SeekG] == null)) {
                    PosU = SeekU;
                    PosG = SeekG;
                    PosNumGrids = this.UniJunctions[PosU].length;
                    while (SeekU < NumUniJunctions) {
                        // Se salta los nulls que consiga
                        while ((SeekU < NumUniJunctions) && (SeekG < SeekNumGrids) && (this.UniJunctions[SeekU][SeekG] == null)) {
                            SeekG++;
                            if (SeekG == SeekNumGrids) {
                                SeekU++;
                                if (SeekU < NumUniJunctions) {
                                    SeekNumGrids = this.UniJunctions[SeekU].length;
                                    SeekG = 0;
                                }
                            }
                        }
                        // Copia los elementos que estan despues del null
                        if (SeekU < NumUniJunctions) {
                            while ((SeekU < NumUniJunctions) && (SeekG < SeekNumGrids) && (this.UniJunctions[SeekU][SeekG] != null)) {
                                this.UniJunctions[PosU][PosG] = this.UniJunctions[SeekU][SeekG];
                                this.UniJunctions[SeekU][SeekG] = null;
                                PosG++;
                                if (PosG == PosNumGrids) {
                                    PosU++;
                                    if (PosU < NumUniJunctions) {
                                        PosNumGrids = this.UniJunctions[PosU].length;
                                        PosG = 0;
                                    }
                                }
                                SeekG++;
                                if (SeekG == SeekNumGrids) {
                                    SeekU++;
                                    if (SeekU < NumUniJunctions) {
                                        SeekNumGrids = this.UniJunctions[SeekU].length;
                                        SeekG = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                this.Sorted = false;
                sortrwl.writeLock().unlock();
                rwl.writeLock().unlock();
            }
        } else {
            throw new UtilsException("ERROR: UniJunctions is NULL in the Unimatrix at Defragg", Unimatrix.ERRORUNIMATRIXISNULL, Unimatrix.CLASSID + "009");
        }
        Local = (PosU * INTLENGMASK) + PosG;
        return Local;
    }

    /**
     * Realiza el Trim de la Unimatrix, eliminando todos los grids que son null
     * y realizando la redimension de la unimatrix para solo contener los
     * elementos que no son null.
     *
     * @throws UtilsException
     */
    public void Trim() throws UtilsException {
        long NewSize;

        NewSize = this.Defrag();
        if (NewSize == 0) {
            NewSize = 1;
        }
        if (NewSize > 0) {
            this.setCapacity(NewSize);
        }
    }

    /**
     * Realiza el ordenamiento de los elementos de la unimatrix.
     *
     * El orden depende de las bandera concatenadas por '|' que pueden ser:
     *
     * Unimatrix.ASCENDING | Unimatrix.SORTABSOLUTE | Unimatrix.METRICLONG
     * Unimatrix.ASCENDING | Unimatrix.SORTABSOLUTE | Unimatrix.METRICDOUBLE
     * Unimatrix.DESCENDING | Unimatrix.SORTABSOLUTE | Unimatrix.METRICLONG
     * Unimatrix.DESCENDING | Unimatrix.SORTABSOLUTE | Unimatrix.METRICDOUBLE
     * Unimatrix.ASCENDING | Unimatrix.SORTRELATIVE Unimatrix.DESCENDING |
     * Unimatrix.SORTRELATIVE
     *
     * NOTA: Los elementos DEBEN de ser instancias de la interface Sortable de
     * BolivarTech.
     *
     * @param OrderFlags Banderas de control del algotirmo de ordenamiento.
     * @throws UtilsException
     */
    public void Sort(int OrderFlags) throws UtilsException {
        long Capasidad;
        int Flag;

        this.Trim();
        Capasidad = this.getCapacity();
        if (Capasidad > 1) {
            sortrwl.writeLock().lock();
            try {
                // Verifica que sea ordenamiento por valor absoluto
                Flag = 0x01 & OrderFlags;
                if (Flag == 1) {
                    // Verifica que sea order Ascendente
                    Flag = (OrderFlags >>> 1) & 0x01;
                    if (Flag == 1) {
                        // Verifica si se utiliza la metrica Long
                        Flag = (OrderFlags >>> 2) & 0x01;
                        if (Flag == 1) {
                            AbsoluteAscendQuickSort(0, Capasidad - 1);
                        } else {
                            // Se utliza la metrica double
                            AbsoluteAscendQuickSortDouble(0, Capasidad - 1);
                        }
                    } else {
                        // Es orden Descendente
                        // Verifica si se utiliza la metrica Long
                        Flag = (OrderFlags >>> 2) & 0x01;
                        if (Flag == 1) {
                            AbsoluteDescendQuickSort(0, Capasidad - 1);
                        } else {
                            // Se utliza la metrica double
                            AbsoluteDescendQuickSortDouble(0, Capasidad - 1);
                        }
                    }
                } else {
                    // Es ordenamiento Relativo
                    // Verifica que sea order Ascendente
                    Flag = (OrderFlags >>> 1) & 0x01;
                    if (Flag == 1) {
                        RelativeAscendQuickSort(0, Capasidad - 1);
                    } else {
                        RelativeDescendQuickSort(0, Capasidad - 1);
                    }
                }
            } finally {
                this.Sorted = true;
                sortrwl.writeLock().unlock();
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Ascendente Absoluto
     */
    private void AbsoluteAscendQuickSort(long IndexLo, long IndexHi) throws UtilsException {
        long ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ElementA = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ElementA)) {
                ValorMedio = ((Sortable) ElementA).Metrica();
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).Metrica() < ValorMedio)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "010");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).Metrica() > ValorMedio)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "011");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "012");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "013");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    AbsoluteAscendQuickSort(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    AbsoluteAscendQuickSort(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "014");
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Descendent Absoluto
     */
    private void AbsoluteDescendQuickSort(long IndexLo, long IndexHi) throws UtilsException {
        long ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ElementA = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ElementA)) {
                ValorMedio = ((Sortable) ElementA).Metrica();
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).Metrica() > ValorMedio)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "015");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).Metrica() < ValorMedio)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "016");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "017");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "018");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    AbsoluteDescendQuickSort(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    AbsoluteDescendQuickSort(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "019");
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Ascendente Absoluto basado en
     * comparacion de doubles
     */
    private void AbsoluteAscendQuickSortDouble(long IndexLo, long IndexHi) throws UtilsException {
        double ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ElementA = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ElementA)) {
                ValorMedio = ((Sortable) ElementA).MetricaDouble();
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).MetricaDouble() < ValorMedio)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "020");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).MetricaDouble() > ValorMedio)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "021");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "022");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "023");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    AbsoluteAscendQuickSortDouble(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    AbsoluteAscendQuickSortDouble(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "024");
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Descendente Absoluto basado en
     * comparacion de doubles
     */
    private void AbsoluteDescendQuickSortDouble(long IndexLo, long IndexHi) throws UtilsException {
        double ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ElementA = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ElementA)) {
                ValorMedio = ((Sortable) ElementA).MetricaDouble();
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).MetricaDouble() > ValorMedio)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "025");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).MetricaDouble() < ValorMedio)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "026");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "027");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "028");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    AbsoluteDescendQuickSortDouble(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    AbsoluteDescendQuickSortDouble(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "029");
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Ascendente Relativo
     */
    private void RelativeAscendQuickSort(long IndexLo, long IndexHi) throws UtilsException {
        Object ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ValorMedio)) {
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).Order(((Sortable) ValorMedio)) < 0)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "030");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).Order(((Sortable) ValorMedio)) > 0)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "031");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "032");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "033");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    RelativeAscendQuickSort(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    RelativeAscendQuickSort(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "034");
            }
        }
    }

    /*
     * Realiza el ordenamiento por el QuickSoft Ascendente Relativo
     */
    private void RelativeDescendQuickSort(long IndexLo, long IndexHi) throws UtilsException {
        Object ValorMedio;
        long InLo;
        long InHi;
        Object ElementA, ElementB;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = this.getElement((IndexHi + IndexLo) / 2);
            if (Sortable.class.isInstance(ValorMedio)) {
                // loop through the array until indices cross
                while (InLo <= InHi) {
                    /* find the first element that is greater than or equal to
                     * the partition element starting from the left Index.
                     */
                    ElementA = this.getElement(InLo);
                    if (Sortable.class.isInstance(ElementA)) {
                        while ((InLo < IndexHi) && (((Sortable) ElementA).Order(((Sortable) ValorMedio)) > 0)) {
                            ++InLo;
                            ElementA = this.getElement(InLo);
                            if (!Sortable.class.isInstance(ElementA)) {
                                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "035");
                            }
                        }
                        /* find an element that is smaller than or equal to
                         * the partition element starting from the right Index.
                         */
                        ElementB = this.getElement(InHi);
                        if (Sortable.class.isInstance(ElementA)) {
                            while ((InHi > IndexLo) && (((Sortable) ElementB).Order(((Sortable) ValorMedio)) < 0)) {
                                --InHi;
                                ElementB = this.getElement(InHi);
                                if (!Sortable.class.isInstance(ElementB)) {
                                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "036");
                                }
                            }
                            // if the indexes have not crossed, swap
                            if (InLo <= InHi) {
                                this.setElement(InLo, ElementB);
                                this.setElement(InHi, ElementA);
                                ++InLo;
                                --InHi;
                            }
                        } else {
                            throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InHi), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "037");
                        }
                    } else {
                        throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(InLo), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "038");
                    }
                }
                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (IndexLo < InHi) {
                    RelativeDescendQuickSort(IndexLo, InHi);
                }
                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (InLo < IndexHi) {
                    RelativeDescendQuickSort(InLo, IndexHi);
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString((IndexHi + IndexLo) / 2), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "039");
            }
        }
    }

    /**
     * Realiza la busqueda de seachItem en la unimatrix, retornando la posicion
     * del elemento o ITEMNOTFOUND si no lo consiguio.
     *
     * El tipo de busqueda y el resultado depende de las bandera concatenadas
     * por '|' que pueden ser:
     *
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.LONGMETRIC | Unimatrix.SEARCHEXACT
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.LONGMETRIC | Unimatrix.SEARCHNEAR
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.LONGMETRIC | Unimatrix.SEARCHFLOOR
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.LONGMETRIC | Unimatrix.SEARCHCEILING
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.DOUBLEMETRIC | Unimatrix.SEARCHEXACT
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.DOUBLEMETRIC | Unimatrix.SEARCHNEAR
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.DOUBLEMETRIC | Unimatrix.SEARCHFLOOR
     * Unimatrix.SEARCHABSOLUTE | Unimatrix.DOUBLEMETRIC |
     * Unimatrix.SEARCHCEILING Unimatrix.SEARCHRELATIVE | Unimatrix.SEARCHEXACT
     * Unimatrix.SEARCHRELATIVE | Unimatrix.SEARCHFLOOR Unimatrix.SEARCHRELATIVE
     * | Unimatrix.SEARCHCEILING
     *
     * NOTA: en SEARCHRELATIVE la bandera SEARCHNEAR tiene el mismo
     * comportamiento que SEARCHEXACT.
     *
     * @param searchItem Item a buscar en la Unimatrix
     * @param SearchFlags Banderas de control de la busqueda en la Unimatrix
     * @return Posicion del elemento buscado o ITEMNOTFOUND si no se consiguio.
     * @throws UtilsException
     */
    public long Search(Sortable searchItem, int SearchFlags) throws UtilsException {
        int Flag;
        long Result = ITEMNOTFOUND;

        if (this.Sorted) {
            rwl.readLock().lock();
            sortrwl.readLock().lock();
            try {
                // Verifica que sea ordenamiento por valor absoluto
                Flag = 0x01 & SearchFlags;
                if (Flag == 1) {
                    // Busqueda absoluta
                    Flag = (SearchFlags >>> 2) & 0x01;
                    if (Flag == 1) {
                        // Busqueda por Long
                        Flag = (SearchFlags >>> 1) & 0x01;
                        if (Flag == 1) {
                            Result = AbsoluteSearch(searchItem, 1);
                        } else {
                            Flag = (SearchFlags >>> 3) & 0x01;
                            if (Flag == 1) {
                                Result = AbsoluteSearch(searchItem, 2);
                            } else {
                                Flag = (SearchFlags >>> 4) & 0x01;
                                if (Flag == 1) {
                                    Result = AbsoluteSearch(searchItem, 3);
                                } else {
                                    Result = AbsoluteSearch(searchItem, 0);
                                }
                            }
                        }
                    } else {
                        // Busqueda por Double
                        Flag = (SearchFlags >>> 1) & 0x01;
                        if (Flag == 1) {
                            Result = AbsoluteSearchDouble(searchItem, 1);
                        } else {
                            Flag = (SearchFlags >>> 3) & 0x01;
                            if (Flag == 1) {
                                Result = AbsoluteSearchDouble(searchItem, 2);
                            } else {
                                Flag = (SearchFlags >>> 4) & 0x01;
                                if (Flag == 1) {
                                    Result = AbsoluteSearchDouble(searchItem, 3);
                                } else {
                                    Result = AbsoluteSearchDouble(searchItem, 0);
                                }
                            }
                        }
                    }
                } else {
                    // Busqueda relativa
                    Flag = (SearchFlags >>> 3) & 0x01;
                    if (Flag == 1) {
                        Result = RelativeSearch(searchItem, 1);
                    } else {
                        Flag = (SearchFlags >>> 4) & 0x01;
                        if (Flag == 1) {
                            Result = RelativeSearch(searchItem, 2);
                        } else {
                            Result = RelativeSearch(searchItem, 0);
                        }
                    }
                }
            } finally {
                sortrwl.readLock().unlock();
                rwl.readLock().unlock();
            }
        } else {
            throw new UtilsException("Unimatrix: NOT SORTED to search element", Unimatrix.ERRORUNIMATRIXNOTSORTED, Unimatrix.CLASSID + "040");
        }
        return Result;
    }

    /*
     * Busqueda absoluta.
     * Modo: 0 Exacto, 1 mas proximo, 2 redondeo abajo, 3 redondeo arriba
     */
    private long AbsoluteSearch(Sortable searchItem, int Modo) throws UtilsException {
        long first;
        long last;
        long mid;
        long found = ITEMNOTFOUND;
        Object ValorMedio;
        boolean Ascending;
        long diff, diffP, diffM;

        first = 0;
        mid = 0;
        last = this.getCapacity() - 1;
        if (last > first) {
            // Verifica si esta ordenado de forma ascendente o descendente
            ValorMedio = this.getElement(last);
            if (Sortable.class.isInstance(ValorMedio)) {
                if (Sortable.class.isInstance(this.getElement(0))) {
                    if (((Sortable) ValorMedio).Metrica() >= ((Sortable) this.getElement(0)).Metrica()) {
                        Ascending = true;
                    } else {
                        Ascending = false;
                    }
                } else {
                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(0), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "041");
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(last), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "042");
            }
        } else {
            Ascending = true;
        }
        //Realiza un loop buscando el elemento
        while ((first <= last) && (found == ITEMNOTFOUND)) {
            //Busca el medio de la lista
            mid = (first + last) / 2;
            ValorMedio = this.getElement(mid);
            if (Sortable.class.isInstance(ValorMedio)) {
                // Compara le medio de la lista con el elemento buscado
                if (((Sortable) ValorMedio).Metrica() == searchItem.Metrica()) {
                    found = mid;
                } else if (Ascending) {
                    if (((Sortable) ValorMedio).Metrica() > searchItem.Metrica()) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (((Sortable) ValorMedio).Metrica() < searchItem.Metrica()) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(mid), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "043");
            }
        }
        // Realiza el ajuste del modo de busqueda
        if (found == ITEMNOTFOUND) {
            if (Modo == 1) {
                ValorMedio = (Sortable) this.getElement(mid);
                diff = Math.abs(((Sortable) ValorMedio).Metrica() - searchItem.Metrica());
                if (mid > 0) {
                    ValorMedio = (Sortable) this.getElement(mid - 1);
                    diffM = Math.abs(((Sortable) ValorMedio).Metrica() - searchItem.Metrica());
                } else {
                    diffM = -1;
                }
                if (mid < (this.getCapacity() - 1)) {
                    ValorMedio = (Sortable) this.getElement(mid + 1);
                    diffP = Math.abs(((Sortable) ValorMedio).Metrica() - searchItem.Metrica());
                } else {
                    diffP = -1;
                }
                if (diffP >= 0) {
                    first = Math.min(diff, diffP);
                } else {
                    first = diff;
                }
                if (diffM >= 0) {
                    last = Math.min(diff, diffM);
                } else {
                    last = diff;
                }
                first = Math.min(first, last);
                if (first == diff) {
                    found = mid;
                } else if (first == diffM) {
                    found = mid - 1;
                } else if (first == diffP) {
                    found = mid + 1;
                }
            } else if (Modo == 2) {
                if (Ascending) {
                    if (mid >= 0) {
                        ValorMedio = (Sortable) this.getElement(mid);
                        while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).Metrica() < searchItem.Metrica())) {
                            mid++;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        while ((mid > 0) && (((Sortable) ValorMedio).Metrica() > searchItem.Metrica())) {
                            mid--;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        if (((Sortable) ValorMedio).Metrica() < searchItem.Metrica()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid <= (this.getCapacity() - 1)) {
                    ValorMedio = (Sortable) this.getElement(mid);
                    while ((mid > 0) && (((Sortable) ValorMedio).Metrica() < searchItem.Metrica())) {
                        mid--;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).Metrica() > searchItem.Metrica())) {
                        mid++;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    if (((Sortable) ValorMedio).Metrica() < searchItem.Metrica()) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            } else if (Modo == 3) {
                if (Ascending) {
                    if (mid <= (this.getCapacity() - 1)) {
                        ValorMedio = (Sortable) this.getElement(mid);
                        while ((mid > 0) && (((Sortable) ValorMedio).Metrica() > searchItem.Metrica())) {
                            mid--;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).Metrica() < searchItem.Metrica())) {
                            mid++;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        if (((Sortable) ValorMedio).Metrica() > searchItem.Metrica()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid >= 0) {
                    ValorMedio = (Sortable) this.getElement(mid);
                    while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).Metrica() > searchItem.Metrica())) {
                        mid++;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    while ((mid > 0) && (((Sortable) ValorMedio).Metrica() < searchItem.Metrica())) {
                        mid--;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    if (((Sortable) ValorMedio).Metrica() > searchItem.Metrica()) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            }
        }
        return found;
    }

    /*
     * Busqueda Absoluta por parametro double.
     * Modo: 0 Exacto, 1 mas proximo, 2 redondeo abajo, 3 redondeo arriba
     */
    private long AbsoluteSearchDouble(Sortable searchItem, int Modo) throws UtilsException {
        long first;
        long last;
        long mid;
        long found = ITEMNOTFOUND;
        Object ValorMedio;
        boolean Ascending;
        double diff, diffP, diffM, TempA, TempB;

        first = 0;
        mid = 0;
        last = this.getCapacity() - 1;
        if (last > first) {
            // Verifica si esta ordenado de forma ascendente o descendente
            ValorMedio = this.getElement(last);
            if (Sortable.class.isInstance(ValorMedio)) {
                if (Sortable.class.isInstance(this.getElement(0))) {
                    if (((Sortable) ValorMedio).MetricaDouble() >= ((Sortable) this.getElement(0)).MetricaDouble()) {
                        Ascending = true;
                    } else {
                        Ascending = false;
                    }
                } else {
                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(0), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "044");
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(last), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "045");
            }
        } else {
            Ascending = true;
        }
        //Realiza un loop buscando el elemento
        while ((first <= last) && (found == ITEMNOTFOUND)) {
            //Busca el medio de la lista
            mid = (first + last) / 2;
            ValorMedio = this.getElement(mid);
            if (Sortable.class.isInstance(ValorMedio)) {
                // Compara le medio de la lista con el elemento buscado
                if (((Sortable) ValorMedio).MetricaDouble() == searchItem.MetricaDouble()) {
                    found = mid;
                } else if (Ascending) {
                    if (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble()) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble()) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(mid), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "046");
            }
        }
        // Realiza el ajuste del modo de busqueda
        if (found == ITEMNOTFOUND) {
            if (Modo == 1) {
                ValorMedio = (Sortable) this.getElement(mid);
                diff = Math.abs(((Sortable) ValorMedio).MetricaDouble() - searchItem.MetricaDouble());
                if (mid > 0) {
                    ValorMedio = (Sortable) this.getElement(mid - 1);
                    diffM = Math.abs(((Sortable) ValorMedio).MetricaDouble() - searchItem.MetricaDouble());
                } else {
                    diffM = -1;
                }
                if (mid < (this.getCapacity() - 1)) {
                    ValorMedio = (Sortable) this.getElement(mid + 1);
                    diffP = Math.abs(((Sortable) ValorMedio).MetricaDouble() - searchItem.MetricaDouble());
                } else {
                    diffP = -1;
                }
                if (diffP >= 0) {
                    TempA = Math.min(diff, diffP);
                } else {
                    TempA = diff;
                }
                if (diffM >= 0) {
                    TempB = Math.min(diff, diffM);
                } else {
                    TempB = diff;
                }
                TempA = Math.min(TempA, TempB);
                if (TempA == diff) {
                    found = mid;
                } else if (TempA == diffM) {
                    found = mid - 1;
                } else if (TempA == diffP) {
                    found = mid + 1;
                }
            } else if (Modo == 2) {
                if (Ascending) {
                    if (mid >= 0) {
                        ValorMedio = (Sortable) this.getElement(mid);
                        while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble())) {
                            mid++;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        while ((mid > 0) && (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble())) {
                            mid--;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        if (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid <= (this.getCapacity() - 1)) {
                    ValorMedio = (Sortable) this.getElement(mid);
                    while ((mid > 0) && (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble())) {
                        mid--;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble())) {
                        mid++;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    if (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble()) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            } else if (Modo == 3) {
                if (Ascending) {
                    if (mid <= (this.getCapacity() - 1)) {
                        ValorMedio = (Sortable) this.getElement(mid);
                        while ((mid > 0) && (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble())) {
                            mid--;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble())) {
                            mid++;
                            ValorMedio = (Sortable) this.getElement(mid);
                        }
                        if (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid >= 0) {
                    ValorMedio = (Sortable) this.getElement(mid);
                    while ((mid < this.getCapacity() - 1) && (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble())) {
                        mid++;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    while ((mid > 0) && (((Sortable) ValorMedio).MetricaDouble() < searchItem.MetricaDouble())) {
                        mid--;
                        ValorMedio = (Sortable) this.getElement(mid);
                    }
                    if (((Sortable) ValorMedio).MetricaDouble() > searchItem.MetricaDouble()) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            }
        }
        return found;
    }

    /*
     * Busqueda relativa.
     * Modo: 0 Exacto, 1 redondeo abajo, 2 redondeo arriba
     */
    private long RelativeSearch(Sortable searchItem, int Modo) throws UtilsException {
        long first;
        long last;
        long mid;
        long found = ITEMNOTFOUND;
        Object ValorMedio;
        boolean Ascending;
        int Order;

        first = 0;
        mid = 0;
        last = this.getCapacity() - 1;
        if (last > first) {
            // Verifica si esta ordenado de forma ascendente o descendente
            ValorMedio = this.getElement(last);
            if (Sortable.class.isInstance(ValorMedio)) {
                if (Sortable.class.isInstance(this.getElement(0))) {
                    if (((Sortable) ValorMedio).Order((Sortable) this.getElement(0)) > 0) {
                        Ascending = true;
                    } else {
                        Ascending = false;
                    }
                } else {
                    throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(0), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "047");
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(last), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "048");
            }
        } else {
            Ascending = true;
        }
        //Realiza un loop buscando el elemento
        while ((first <= last) && (found == ITEMNOTFOUND)) {
            //Busca el medio de la lista
            mid = (first + last) / 2;
            ValorMedio = this.getElement(mid);
            if (Sortable.class.isInstance(ValorMedio)) {
                // Compara le medio de la lista con el elemento buscado
                Order = ((Sortable) ValorMedio).Order(searchItem);
                if (Order == 0) {
                    found = mid;
                } else if (Ascending) {
                    if (Order > 0) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (Order < 0) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            } else {
                throw new UtilsException("Unimatrix: NOT SORTABLE element at " + Long.toString(mid), Unimatrix.ERRORELEMENTNOTSORTABLE, Unimatrix.CLASSID + "049");
            }
        }
        // Realiza el ajuste del modo de busqueda
        if (found == ITEMNOTFOUND) {
            if (Modo == 1) {
                if (Ascending) {
                    if (mid >= 0) {
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        while ((mid < this.getCapacity() - 1) && (Order < 0)) {
                            mid++;
                            Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        }
                        while ((mid > 0) && (Order > 0)) {
                            mid--;
                            Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        }
                        if (Order < 0) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid <= (this.getCapacity() - 1)) {
                    Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    while ((mid > 0) && (Order < 0)) {
                        mid--;
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    }
                    while ((mid < this.getCapacity() - 1) && (Order > 0)) {
                        mid++;
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    }
                    if (Order < 0) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            } else if (Modo == 2) {
                if (Ascending) {
                    if (mid <= (this.getCapacity() - 1)) {
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        while ((mid > 0) && (Order > 0)) {
                            mid--;
                            Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        }
                        while ((mid < this.getCapacity() - 1) && (Order < 0)) {
                            mid++;
                            Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                        }
                        if (Order > 0) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (mid >= 0) {
                    Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    while ((mid < this.getCapacity() - 1) && (Order > 0)) {
                        mid++;
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    }
                    while ((mid > 0) && (Order < 0)) {
                        mid--;
                        Order = ((Sortable) this.getElement(mid)).Order(searchItem);
                    }
                    if (Order > 0) {
                        found = mid;
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else {
                    found = ITEMNOTFOUND;
                }
            }
        }
        return found;
    }
}
