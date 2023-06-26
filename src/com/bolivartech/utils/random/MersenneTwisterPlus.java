package com.bolivartech.utils.random;

import com.bolivartech.utils.bits.BitsUtils;
import com.bolivartech.utils.exception.UtilsException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright 2007,2009,2010 BolivarTech C.A.
 *
 * Implementacion de un generador de numeros seudo-aleatorios por medio de
 * algoritmo Mersenne Twister para 64 bits de logintud, con un periodo de
 * 2^19937-1.
 *
 * Si se inicializa con una semilla el algoritmo se comporta de la misma forma
 * que el Mersenne Twister original.
 *
 * Si se permite la inicializacion automatica a partir del numero de
 * nanosegundos del sistema como semilla, este valor tambien es utilizado para
 * determinar el numero de ciclos de 'NN' elementos que se utilizaran antes de
 * seleccionar otra semilla con el mismo metodo del numermo de nanosegundos.
 *
 * NOTA: Esta clase es segura a la concurrencia
 *
 * @author Julian Bolivar
 * @since 2007 - December 19, 2015.
 * @version 3.1.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v3.1.0 (2015-12-19) - Se agrego el soporte para ser seguro a la
 * concurrencia y el metodo nextGaussian
 * </li>
 * </ul>
 * <ul>
 * <li>v2.0.0 (2015-10-17) - Se agrego el metodo nextLong(long n).
 * </li>
 * </ul>
 * <ul>
 * <li>v1.0.0 (2007-06-22) - Version Inicial.</li>
 * </ul>
 */
public strictfp class MersenneTwisterPlus {

    // Lock para el manejo de concurrencia
    private ReentrantLock lock = new ReentrantLock();

    // totally anti-symmetric quasigroup matrix
    private static final int[][] antisymmetric
            = {{0, 3, 1, 7, 5, 9, 8, 6, 4, 2},
            {7, 0, 9, 2, 1, 5, 4, 8, 6, 3},
            {4, 2, 0, 6, 8, 7, 1, 3, 5, 9},
            {1, 7, 5, 0, 9, 8, 3, 4, 2, 6},
            {6, 1, 2, 3, 0, 4, 5, 9, 7, 8},
            {3, 6, 7, 4, 2, 0, 9, 5, 8, 1},
            {5, 8, 6, 9, 7, 2, 0, 1, 3, 4},
            {8, 9, 4, 5, 3, 6, 2, 0, 1, 7},
            {9, 4, 3, 8, 6, 1, 7, 2, 0, 5},
            {2, 5, 8, 1, 4, 3, 6, 7, 9, 0}};

    // Constantes de Entorno para el generador de numero aleatorios
    private int NN = 312;
    private int MM = 156;
    private long MATRIX_A = 0xB5026F5AA96619E9L;
    private long UM = 0xFFFFFFFF80000000L;
    /* Most significant 33 bits */

    private long LM = 0x7FFFFFFFL;
    /* Least significant 31 bits */

 /* The array for the state vector */
    private long[] mt;
    private int mti;
    private long seed;
    private boolean FixedSeed;
    private int MaxCiclo;
    private int Ciclo;

    // Para la generacion de Ruido Gausiano
    private double nextNextGaussian = 0;
    private boolean haveNextNextGaussian = false;

    /**
     * Genera la Semilla del generador de numeros aleatorios
     *
     * @return Semilla a usar para generar los numeros aleatorios
     */
    private long GenSeed() {
        long Result, Temp;
        int Shift;

        try {
            Result = System.currentTimeMillis();
            Shift = ((int) (((6 * calculateDamm(Result)) + (Result & 0xFL)) % 64));
            Result = BitsUtils.LongRightRotation(Result, Shift);
            Temp = (~System.nanoTime());
            Shift = ((int) (((6 * calculateDamm(Temp)) + (Temp & 0xFL)) % 64));
            Temp = BitsUtils.LongLeftRotation(Temp, Shift);
            Result = Result ^ Temp;
        } catch (UtilsException ex) {
            Result = (System.currentTimeMillis() ^ (System.nanoTime()));
        }
        return Result;
    }

    /*
     * Rutina de inicializacion de algoritmo generador de numero psuedo-aleatorios
     * utilizando el valore de la semilla almacenada en la clase
     */
    private void init_genrand64() {
        long Temp;
        int Shift;

        try {
            Temp = System.nanoTime();
            Shift = ((int) (((7 * calculateDamm(Temp)) + (Temp & 0xFL)) % 64));
            Temp = BitsUtils.LongRightRotation(Temp, Shift);
        } catch (UtilsException ex) {
            Temp = (~System.nanoTime());
        }
        Temp = ((Temp ^ (~this.seed)) % 990);
        Temp = (Temp + calculateDamm(Temp));
        Temp = ((Temp & this.LM) % 1000);
        this.MaxCiclo = (int) Temp;
        this.Ciclo = 0;
        this.mt = new long[this.NN];
        this.mt[0] = this.seed;
        for (this.mti = 1; this.mti < this.NN; this.mti++) {
            this.mt[this.mti] = (0x5851F42D4C957F2DL * (this.mt[this.mti - 1] ^ (this.mt[this.mti - 1] >>> 62)) + this.mti);
        }
        /* mti==NN+1 indica que mt[NN] no esta inicializado */
        this.mti = this.NN + 1;
    }

    /**
     * Calcula el Damm del numero "number"
     *
     * @param number Numero a ser calculado el Damm
     * @return Damm de "number"
     */
    private Long calculateDamm(Long number) {
        Long Result;
        String strNumber;
        int[] Digits;
        int interim, i;

        Result = null;
        number = Math.abs(number);
        strNumber = number.toString();
        Digits = new int[strNumber.length()];
        for (i = 0; i < Digits.length; i++) {
            Digits[i] = Integer.parseInt(strNumber.substring(i, i + 1));
        }
        interim = 0;
        for (i = 0; i < Digits.length; i++) {
            interim = antisymmetric[interim][Digits[i]];
        }
        Result = new Long(interim);
        return Result;
    }

    /**
     * Constructor por defecto, el cual inicializa el generador de numeros
     * aleatorios con el numero de nanosegundos de sistema.
     */
    public MersenneTwisterPlus() {
        FixedSeed = false;
        //this.seed = new Date().getTime() ;
        this.seed = GenSeed();
        init_genrand64();
    }

    /**
     * Constructor con inicializacion por medio de una semilla
     *
     * @param seed
     */
    public MersenneTwisterPlus(long seed) {
        FixedSeed = true;
        this.seed = seed;
        init_genrand64();
    }

    /**
     * Genera un numero pseudo-aleatorio en el intervalo [0, 2^64-1]
     *
     * @return numero pseudoaleatorio en el intervalo [0, 2^64-1]
     */
    public long nextLong() {
        int i;
        long x;
        long magVN[] = {0L, MATRIX_A};

        this.lock.lock();
        try {
            /* Verifica si la semilla del algoritmo es fija o variable */
            if (!FixedSeed) {
                /* Verifica si se recorrio todo el arreglo y lo inicializa a partir
             de una nueva semilla aleatoria */
                if (Ciclo >= MaxCiclo) {
                    //this.seed=new Date().getTime();
                    this.seed = GenSeed();
                    init_genrand64();
                }
            }
            /* Genera NN palabras de una sola vez */
            if (mti >= NN) {
                for (i = 0; i < NN - MM; i++) {
                    x = (mt[i] & UM) | (mt[i + 1] & LM);
                    mt[i] = mt[i + MM] ^ (x >>> 1) ^ magVN[(int) (x & 1L)];
                }
                for (; i < NN - 1; i++) {
                    x = (mt[i] & UM) | (mt[i + 1] & LM);
                    mt[i] = mt[i + (MM - NN)] ^ (x >>> 1) ^ magVN[(int) (x & 1L)];
                }
                x = (mt[NN - 1] & UM) | (mt[0] & LM);
                mt[NN - 1] = mt[MM - 1] ^ (x >>> 1) ^ magVN[(int) (x & 1L)];
                mti = 0;
                if (!FixedSeed) {
                    Ciclo++;
                }
            }
            x = mt[mti++];
            x ^= (x >>> 29) & 0x5555555555555555L;
            x ^= (x << 17) & 0x71D67FFFEDA60000L;
            x ^= (x << 37) & 0xFFF7EEE000000000L;
            x ^= (x >>> 43);
        } finally {
            this.lock.unlock();
        }
        return x;
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^63-1]
     *
     * @return long
     */
    public long nextLong63() {
        return (long) (nextLong() >>> 1);
    }

    /**
     * <p>
     * Returns a pseudorandom, uniformly distributed long value between
     * <code>0</code> (inclusive) and the specified value (exclusive).</p>
     *
     * @param n the specified exclusive max-value
     * @return the random long
     * @throws IllegalArgumentException when <code>n &lt;= 0</code>
     */
    public long nextLong(long n) {
        long val;
        long bits;

        if (n <= 0) {
            throw new IllegalArgumentException(
                    "Upper bound for nextInt must be positive"
            );
        }
        // Code adapted from Harmony Random#nextInt(int)
        if ((n & -n) == n) { // n is power of 2
            // dropping lower order bits improves behaviour for low values of n
            return nextLong63() >> (63 - BitsUtils.bitsRequired(n - 1)); // drop all the bits except the ones we need
        }
        // Not a power of two
        do { // reject some values to improve distribution
            bits = nextLong63();
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalor real de [0,1]
     *
     * @return double
     */
    public double nextDouble() {
        return (double) (nextLong() >>> 11) * (1.0 / 9007199254740991.0);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalor real de [0,1]
     *
     * @return double
     */
    public double nextReal() {
        return (double) (nextLong() >>> 11) * (1.0 / 9007199254740991.0);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo real de [0,1)
     *
     * @return double
     */
    public double nextReal2() {
        return (double) (nextLong() >>> 11) * (1.0 / 9007199254740992.0);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalor real (0,1)
     *
     * @return double
     */
    public double nextReal3() {
        return (double) ((nextLong() >>> 12) + 0.5) * (1.0 / 4503599627370496.0);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^32-1]
     *
     * @return int
     */
    public int nextInt() {
        return (int) (nextLong() >>> 32);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, n)
     *
     * @param n - the bound on the random number to be returned. Must be
     * positive.
     * @return int the next pseudorandom, uniformly distributed int value
     * between 0 (inclusive) and n (exclusive) from this random number
     * generator's sequence
     */
    public int nextInt(int n) {
        int bits, val;

        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }
        if ((n & -n) == n) // i.e., n is a power of 2
        {
            return (int) ((n * (long) nextInt31()) >> 31);
        }
        do {
            bits = nextInt31();
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^31-1]
     *
     * @return int
     */
    public int nextInt31() {
        return (int) (nextLong() >>> 33);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^16-1]
     *
     * @return short
     */
    public short nextShort() {
        return (short) (nextLong() >>> 48);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^15-1]
     *
     * @return short
     */
    public short nextShort15() {
        return (short) (nextLong() >>> 49);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^8-1]
     *
     * @return byte
     */
    public byte nextByte() {
        return (byte) (nextLong() >>> 56);
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^7-1]
     *
     * @return byte
     */
    public byte nextByte7() {
        return (byte) (nextLong() >>> 57);
    }

    /**
     * Genera numeros aleatorios que son colocados dentro del arreglo de bytes
     * proporcionado por el usuario.
     *
     * @param bytes
     */
    public void nextBytes(byte[] bytes) {
        int i;

        if (bytes != null) {
            for (i = 0; i < bytes.length; i++) {
                bytes[i] = this.nextByte();
            }
        }
    }

    /**
     * Retorna Numeros Pseudoaleatorios con una distribucion Gaussiana con media
     * 0 y desviacion estandar de 1.
     *
     * @return Numero Pseudoaleatorios con distribucion Gaussiana
     */
    public double nextGaussian() {
        double v1, v2, s;
        double multiplier;

        synchronized (this) {
            if (haveNextNextGaussian) {
                haveNextNextGaussian = false;
                return nextNextGaussian;
            } else {
                do {
                    v1 = 2 * this.nextDouble() - 1;   // between -1.0 and 1.0
                    v2 = 2 * this.nextDouble() - 1;   // between -1.0 and 1.0
                    s = v1 * v1 + v2 * v2;
                } while (s >= 1 || s == 0);
                multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
                nextNextGaussian = v2 * multiplier;
                haveNextNextGaussian = true;
                return v1 * multiplier;
            }
        }
    }

    /**
     * Retorna un valor de -1 o 1 de forma aleatoria.
     *
     * @return -1 o 1 de forma aleatoria
     */
    public double nextDoubleSign() {
        double Result;

        Result = (this.nextDouble() < 0.5 ? -1.0 : 1.0);
        return Result;
    }

    /**
     * Retorna un valor de -1 o 1 de forma aleatoria.
     *
     * @return -1 o 1 de forma aleatoria
     */
    public int nextIntegerSign() {
        int Result;

        Result = (this.nextDouble() < 0.5 ? -1 : 1);
        return Result;
    }

    /**
     * Retorna un valor de -1 o 1 de forma aleatoria.
     *
     * @return -1 o 1 de forma aleatoria
     */
    public long nextLongSign() {
        long Result;

        Result = (this.nextDouble() < 0.5 ? -1 : 1);
        return Result;
    }
}
