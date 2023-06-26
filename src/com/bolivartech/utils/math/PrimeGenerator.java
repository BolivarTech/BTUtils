package com.bolivartech.utils.math;

import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech C.A.
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's that generate and verify long and integer prime numbers.
 *
 * Esta clase calcula y verifica numeros primos long e integer
 *
 * @author Julian Bolivar
 * @since 2015
 * @date November 24, 2015.
 * @version 1.0.0
 *
 * Change Logs: v1.0.0 (2015/11/24) Version Inicial.
 */
public class PrimeGenerator {

    /**
     * Retorna un numero primo long aleatorio.
     * 
     * @return Numero primo long
     */
    public static long getLongPrime() {
        MersenneTwisterPlus Random;
        long P, K, M, N;
        boolean NoOk = true;

        P = 0;
        Random = new MersenneTwisterPlus();
        do {
            M = Random.nextLong63();
            N = Random.nextLong(20);
            try {
                K = M * (N + 1) - (Factorial.longFactorial((int) N) + 1);
                P = (((N - 1) * (Math.abs((K * K) - 1) - ((K * K) - 1))) / 2) + 2;
                if ((P > 2) && isPrime(P)) {
                    NoOk = false;
                }
            } catch (UtilsException ex) {
                NoOk = true;
            }
        } while (NoOk);
        return P;
    }

    /**
     * Retorna un numero primo integer aleatorio.
     * 
     * @return Numero primo integer
     */
    public static int getIntegerPrime() {
        MersenneTwisterPlus Random;
        int P, K, M, N;
        boolean NoOk = true;

        P = 0;
        Random = new MersenneTwisterPlus();
        do {
            M = Random.nextInt31();
            N = Random.nextInt(11);
            try {
                K = M * (N + 1) - (Factorial.integerFactorial(N) + 1);
                P = (((N - 1) * (Math.abs((K * K) - 1) - ((K * K) - 1))) / 2) + 2;
                if ((P > 2) && isPrime(P)) {
                    NoOk = false;
                }
            } catch (UtilsException ex) {
                NoOk = true;
            }
        } while (NoOk);
        return P;
    }

    /**
     * Retorna un numero long aleatorio que NO es primo
     * 
     * @return Numero NO Primo
     */
    public static long getLongNotPrime() {
        MersenneTwisterPlus Random;
        long nP;
        
        Random = new MersenneTwisterPlus();
        nP = ((long)Random.nextInt31() * (long)Random.nextInt31());
        return nP;
    }
    
    /**
     * Retorna un numero Integer aleatorio que NO es primo
     * 
     * @return Numero NO Primo
     */
    public static int getIntegerNotPrime() {
        MersenneTwisterPlus Random;
        int nP;
        
        Random = new MersenneTwisterPlus();
        nP = ((int)Random.nextShort15() * (int)Random.nextShort15());
        return nP;
    }
    
    /**
     * Prueba que 'n' sea un numero primo. Esta prueba realiza una verificacion
     * extensiva y unequivoca de la primalidad de 'n'.
     *
     * @param n Numero a probar si es primo
     * @return TRUE si n es primo o FALSE si no lo es
     */
    public static boolean isPrime(long n) {
        long i;

        if (n <= 3) {
            return n > 1;
        } else if (n % 2 == 0 || n % 3 == 0) {
            return false;
        } else {
            i = 5;
            while ((i * i) <= n) {
                if ((n % i == 0) || (n % (i + 2) == 0)) {
                    return false;
                }
                i += 6;
            }
            return true;
        }
    }

    /**
     * Prueba que 'n' sea un numero primo. Esta prueba realiza una verificacion
     * extensiva y unequivoca de la primalidad de 'n'.
     *
     * @param n Numero a probar si es primo
     * @return TRUE si n es primo o FALSE si no lo es
     */
    public static boolean isPrime(int n) {
        int i;

        if (n <= 3) {
            return n > 1;
        } else if (n % 2 == 0 || n % 3 == 0) {
            return false;
        } else {
            i = 5;
            while ((i * i) <= n) {
                if ((n % i == 0) || (n % (i + 2) == 0)) {
                    return false;
                }
                i += 6;
            }
            return true;
        }
    }
}
