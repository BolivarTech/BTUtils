package com.bolivartech.utils.random;

import com.bolivartech.utils.bits.BitsUtils;
import com.bolivartech.utils.random.sparkers.Sparker;
import com.bolivartech.utils.math.chaos.AtractorLorenz;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright 2014 BolivarTech C.A.
 *
 * Implementacion de un generador de numeros seudo-aleatorios por medio de
 * atractores de Lorenz.
 *
 * Esta implementacion permite tener una secuencia de numeros sin un periodo
 * determinado ya que provienen de series caoticas.
 * 
 * NOTA: Esta clase es segura a la concurrencia
 *
 * @author Julian Bolivar
 * @since 2014 - December 19, 2015.
 * @version 2.2.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v2.2.0 (2015-12-19) - Se agrego el soporte para ser seguro a la concurrencia y el metodo nextGaussian
 * </li>
 * </ul>
 * <ul>
 * <li>v2.1.0 (2015-10-17) - Se agrego el metodo nextLong(long n).
 * </li>
 * </ul>
 * <ul>
 * <li>v2.0.0 (2015-07-31) - The number of attractors increased to 20. - Jump is
 * limited to 25 because more of then cause lost of time.</li>
 * </ul>
 * <ul>
 * <li>v1.1.0 (2014-06-22) - Jump is limited to 256 because more of then cause
 * lost of time.</li>
 * </ul>
 */
public strictfp class KAOSrand {

    // Lock para el manejo de concurrencia
    private ReentrantLock lock = new ReentrantLock();

    private static final int NUMATTRACTORS = 20;
    private static final int LIMIT = 25;

    private AtractorLorenz[] Atractores;
    private MersenneTwisterPlus rnd;
    private static long Mask;
    private int Jump;
    
    // Para la generacion de Ruido Gausiano
    private double nextNextGaussian = 0;
    private boolean haveNextNextGaussian = false;

    /**
     * Los atractores con coordenadas aleatorias
     */
    private void RandomIntialPoints() {
        int i, j;
        double Xo, Yo, Zo;

        if (Atractores != null) {
            Xo = 0;
            Yo = 0;
            Zo = 0;
            for (i = 0; i < Atractores.length; i++) {
                // Corre el generador de numeros aleatorios un numero aleatorio de veces
                // para que los puntos de inicio no sean consecutivos del generados seudoaleatorio
                for (j = 0; j < (int) rnd.nextByte(); j++) {
                    Xo = 255 * rnd.nextDouble();
                }
                for (j = 0; j < (int) rnd.nextByte(); j++) {
                    Yo = 255 * rnd.nextDouble();
                }
                for (j = 0; j < (int) rnd.nextByte(); j++) {
                    Zo = 255 * rnd.nextDouble();
                }
                Atractores[i].SetInitialPoint(Xo, Yo, Zo);
            }
            // Borra de forma segura las variables internas    
            i = 0;
            j = 0;
            Xo = 0;
            Yo = 0;
            Zo = 0;
        }
    }

    /**
     * Inicializa los atractores por medio del Saparker.
     *
     * @param Sprk
     */
    private void Sparkerizar(Sparker Sprk) {
        int i, j;
        double Xo, Yo, Zo;

        if (Atractores != null) {
            if (Sprk != null) {
                this.Jump = (int) (Sprk.getShortSpark() % LIMIT);
                for (i = 0; i < Atractores.length; i++) {
                    // Recupera el punto inicial desde el Sparker
                    Xo = 255 * Sprk.getDoubleSpark();
                    Yo = 255 * Sprk.getDoubleSpark();
                    Zo = 255 * Sprk.getDoubleSpark();
                    // Establece los puntos iniciales del atractor
                    Atractores[i].SetInitialPoint(Xo, Yo, Zo);
                    // Corre los atractores la cantidad de veces que especifica el salto
                    for (j = 0; j < this.Jump; j++) {
                        Atractores[i].NextPoint();
                    }
                }
                // Borra de forma segura las variables internas    
                i = 0;
                j = 0;
                Xo = 0;
                Yo = 0;
                Zo = 0;
            } else {
                this.RandomIntialPoints();
            }
        }
    }

    /**
     * Constructor por defecto
     */
    public KAOSrand() {
        int i;

        // Desactiva los saltos de muestreo fijo de los atractores
        Jump = -1;
        Mask = 0x7FFFFFFFFFFFFFFFL;
        rnd = new MersenneTwisterPlus();
        Atractores = new AtractorLorenz[NUMATTRACTORS];
        for (i = 0; i < Atractores.length; i++) {
            Atractores[i] = new AtractorLorenz();
            Atractores[i].SetAtractorType(i);
        }
        this.RandomIntialPoints();
    }

    /**
     * Constructor con inicializacion del algoritmo por medio de Spark.
     *
     * El Sparker debe de ser capaz de generar al menos 36 DoubleSparks
     * INDEPENDIENTES y un (1) ShortSpark INDEPENDIENTE MAYOR QUE 0.
     *
     *
     * NOTA: Si Sprk es NULL se inicializa de forma aleatoria como con el
     * constructor por defecto.
     *
     * @param Sprk
     */
    public KAOSrand(Sparker Sprk) {
        int i;

        // Desactiva los saltos de muestreo fijo de los atractores
        Jump = -1;
        Mask = 0x7FFFFFFFFFFFFFFFL;
        rnd = new MersenneTwisterPlus();
        Atractores = new AtractorLorenz[NUMATTRACTORS];
        for (i = 0; i < Atractores.length; i++) {
            Atractores[i] = new AtractorLorenz();
            Atractores[i].SetAtractorType(i);
        }
        if (Sprk != null) {
            this.Sparkerizar(Sprk);
        } else {
            this.RandomIntialPoints();
        }
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalor real de [0,1]
     *
     * @return double
     */
    public double nextDouble() {
        int i, j, k;
        double[] coordenada;
        long Aleatorios;

        // Espacio dimensional de las coodenadas
        k = 3;
        // Coordenadas de los puntos de los atractores
        coordenada = new double[k * Atractores.length];
        this.lock.lock();
        try {
            // Deja correr los atractores
            for (i = 0; i < Atractores.length; i++) {
                // Verifica si el algoritmo esta inicilizado de forma aleatorio o Sparkerizado
                if (this.Jump <= 0) {
                    // Deja correr el atractor un valor aleatorio entre 0 y 32767
                    for (j = 0; j < (int) (rnd.nextShort() % LIMIT); j++) {
                        Atractores[i].NextPoint();
                    }
                } else {
                    // Deja correr el atractor un valor especificado por Jump
                    for (j = 0; j < this.Jump; j++) {
                        Atractores[i].NextPoint();
                    }
                }
                // Recupera las coordenadas del atractor "i"
                coordenada[k * i] = Atractores[i].getX();
                coordenada[k * i + 1] = Atractores[i].getY();
                coordenada[k * i + 2] = Atractores[i].getZ();
            }
            // Solo deja la parte fraccionals de las coordenadas
            for (i = 0; i < k * Atractores.length; i++) {
                if (coordenada[i] < 0) {
                    coordenada[i] *= -1;
                }
                coordenada[i] = coordenada[i] - (long) coordenada[i];
            }
            // Combierte el double a su representacio binaria
            // y genera un compendio a partir de todas las coordenadas juntas
            Aleatorios = 0;
            for (i = 0; i < k * Atractores.length; i++) {
                Aleatorios <<= 12;
                Aleatorios ^= (Double.doubleToRawLongBits(coordenada[i]) & 0x000fffffffffffffL);
            }
            /**
             * // extrae los 10 digitos mas significativos de las parte
             * fraccional de las coordenadas Aleatorios = 0; // Multiplica los
             * valores de las coordendas for (i = 0; i < k * Atractores.length;
             * i++) { Aleatorios <<= 3; Aleatorios ^= (long) (10000000000L *
             * coordenada[i]); }
             */
            // Borra los valores intermedios por seguridad
            for (i = 0; i < k * Atractores.length; i++) {
                coordenada[i] = 0;
            }
            Aleatorios = (Aleatorios & 0x001FFFFFFFFFFFFFL);
            coordenada[0] = (double) Aleatorios * (1.0 / 9007199254740991.0);
            //Aleatorios = (Aleatorios & 0x1FFFFFFFFFFFFFFFL);
            //coordenada[0] = (double)  Aleatorios * (1.0 / 2305843009213693951.0); 
            //Aleatorios = (Aleatorios >>> 16) & 0xFFFFFFFFL;
            //coordenada[0] = (double)  Aleatorios * (1.0 / 4294967295.0); 
        } finally {
            this.lock.unlock();
        }
        return coordenada[0];
    }

    /**
     * Genera un numero pseudo-aleatorio en el intervalo [0, 2^64-1]
     *
     * @return numero pseudoaleatorio en el intervalo [0, 2^64-1]
     */
    public long nextLong() {

        return (long) (Mask * this.nextDouble());
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^63-1]
     *
     * @return long
     */
    public long nextLong63() {

        return (long) ((Mask >>> 1) * this.nextDouble());
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
            throw new IllegalArgumentException("Upper bound for nextInt must be positive");
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
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^32-1]
     *
     * @return int
     */
    public int nextInt() {
        return (int) ((Mask >>> 32) * this.nextDouble());
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
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }

        if ((n & -n) == n) // i.e., n is a power of 2
        {
            return (int) ((n * (long) nextInt31()) >> 31);
        }

        int bits, val;
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
        return (int) ((Mask >>> 33) * this.nextDouble());
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^16-1]
     *
     * @return short
     */
    public short nextShort() {
        return (short) ((Mask >>> 48) * this.nextDouble());
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^15-1]
     *
     * @return short
     */
    public short nextShort15() {
        return (short) ((Mask >>> 49) * this.nextDouble());
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^8-1]
     *
     * @return byte
     */
    public byte nextByte() {
        return (byte) ((Mask >>> 56) * this.nextDouble());
    }

    /**
     * Genera un numero pseudoaleatorio en el intervalo [0, 2^7-1]
     *
     * @return byte
     */
    public byte nextByte7() {
        return (byte) ((Mask >>> 57) * this.nextDouble());
    }

    /**
     * Genera numeros aleatorios que son colocados dentro del arreglo de bytes
     * proporcionado por el usuario.
     *
     * @param bytes Arreglo con los valores aleatorios generados.
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
}
