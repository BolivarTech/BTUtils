package com.bolivartech.utils.math;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech C.A.
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's that define n! function.
 *
 * Define la funcion n!.
 * 
 * Class ID: "35DGFHD"
 * Loc: 000-004
 *
 * @author Julian Bolivar
 * @since 2015
 * @date march 25, 2016.
 * @version 1.0.1
 *
 * Change Logs: 
 * v1.0.0 (2015-11-24) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public class Factorial {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHD";

    // Codigos de Error
    public static final int ERRORNEGATIVE = -1;
    public static final int ERROROVERRANGE = -2;

    private static long N;

    private Factorial() {
    }

    /**
     * Retorna el n! representado en un string.
     *
     * @param n Numero a calcular el factoria
     * @return String con la representacion de n!
     * @throws UtilsException Excepcion en el calculo del n!
     */
    public static final String stringFactorial(int n) throws UtilsException {
        DecInteger p, r;
        String Result = null;
        int h, shift, high;
        int log2n;

        p = new DecInteger(1);
        r = new DecInteger(1);
        if (n >= 0) {
            if (n > 1) {
                N = 1;
                h = 0;
                shift = 0;
                high = 1;
                log2n = (int) Math.floor(Math.log(n) / Math.log(2));
                while (h != n) {
                    shift += h;
                    h = n >> log2n--;
                    int len = high;
                    high = (h & 1) == 1 ? h : h - 1;
                    len = (high - len) / 2;
                    if (len > 0) {
                        p = p.multiply(product(len));
                        r = r.multiply(p);
                    }
                }
                r = r.multiply(DecInteger.pow2(shift));
                Result = r.toString();
            } else {
                Result = "1";
            }
        } else {
            throw new UtilsException("Factorial: n has to be >= 0, but was " + n, ERRORNEGATIVE,Factorial.CLASSID+"000");
        }
        return Result;
    }

    /**
     * Retorna el n! representado en un Long. n debe de ser menor o igual a 20
     * para evitar overflow en el long.
     *
     * @param n Numero a calcular el factoria
     * @return Long con la representacion de n!
     * @throws UtilsException Excepcion en el calculo del n!
     */
    public static final long longFactorial(int n) throws UtilsException {
        DecInteger p, r;
        long Result = 0;
        int h, shift, high;
        int log2n;

        p = new DecInteger(1);
        r = new DecInteger(1);
        if (n >= 0) {
            if (n < 21) {
                if (n > 1) {
                    N = 1;
                    h = 0;
                    shift = 0;
                    high = 1;
                    log2n = (int) Math.floor(Math.log(n) / Math.log(2));
                    while (h != n) {
                        shift += h;
                        h = n >> log2n--;
                        int len = high;
                        high = (h & 1) == 1 ? h : h - 1;
                        len = (high - len) / 2;
                        if (len > 0) {
                            p = p.multiply(product(len));
                            r = r.multiply(p);
                        }
                    }
                    r = r.multiply(DecInteger.pow2(shift));
                    Result = r.toLong();
                } else {
                    Result = 1;
                }
            } else {
                throw new UtilsException("Factorial: n has to be <= 20, but was " + n, ERROROVERRANGE,Factorial.CLASSID+"001");
            }
        } else {
            throw new UtilsException("Factorial: n has to be >= 0, but was " + n, ERRORNEGATIVE,Factorial.CLASSID+"002");
        }
        return Result;
    }

    /**
     * Retorna el n! representado en un Integer. n debe de ser menor o igual a
     * 12 para evitar overflow en el long.
     *
     * @param n Numero a calcular el factoria
     * @return Integer con la representacion de n!
     * @throws UtilsException Excepcion en el calculo del n!
     */
    public static final int integerFactorial(int n) throws UtilsException {
        DecInteger p, r;
        int Result = 0;
        int h, shift, high;
        int log2n;

        p = new DecInteger(1);
        r = new DecInteger(1);
        if (n >= 0) {
            if (n < 13) {
                if (n > 1) {
                    N = 1;
                    h = 0;
                    shift = 0;
                    high = 1;
                    log2n = (int) Math.floor(Math.log(n) / Math.log(2));
                    while (h != n) {
                        shift += h;
                        h = n >> log2n--;
                        int len = high;
                        high = (h & 1) == 1 ? h : h - 1;
                        len = (high - len) / 2;
                        if (len > 0) {
                            p = p.multiply(product(len));
                            r = r.multiply(p);
                        }
                    }
                    r = r.multiply(DecInteger.pow2(shift));
                    Result = r.toInteger();
                } else {
                    Result = 1;
                }
            } else {
                throw new UtilsException("Factorial: n has to be <= 12, but was " + n, ERROROVERRANGE,Factorial.CLASSID+"003");
            }
        } else {
            throw new UtilsException("Factorial: n has to be >= 0, but was " + n, ERRORNEGATIVE,Factorial.CLASSID+"004");
        }
        return Result;
    }

    private static DecInteger product(int n) {
        int m = n / 2;
        if (m == 0) {
            return new DecInteger(N += 2);
        }
        if (n == 2) {
            return new DecInteger((N += 2) * (N += 2));
        }
        return product(n - m).multiply(product(m));
    }

    /**
     * Clase privada que representa un Integer digito a digito
     */
    private static class DecInteger {

        private final long mod = 100000000L;
        private int[] digits;
        private int digitsLength;

        public DecInteger(long value) {
            digits = new int[]{(int) value, (int) (value >>> 32)};
            digitsLength = 2;
        }

        private DecInteger(int[] digits, int length) {
            this.digits = digits;
            digitsLength = length;
        }

        static public DecInteger pow2(int e) {
            if (e < 31) {
                return new DecInteger((int) Math.pow(2, e));
            }
            return pow2(e / 2).multiply(pow2(e - e / 2));
        }

        public DecInteger multiply(DecInteger b) {
            int alen = this.digitsLength, blen = b.digitsLength;
            int clen = alen + blen;
            int[] digit = new int[clen];

            for (int i = 0; i < alen; i++) {
                long temp = 0;
                for (int j = 0; j < blen; j++) {
                    temp = temp + ((long) this.digits[i]) * ((long) b.digits[j]) + digit[i + j];
                    digit[i + j] = (int) (temp % mod);
                    temp = temp / mod;
                }
                digit[i + blen] = (int) temp;
            }
            int k = clen - 1;
            while (digit[k] == 0) {
                k--;
            }
            return new DecInteger(digit, k + 1);
        }

        public int toInteger() {
            int Result = 0;

            Result = Integer.decode(toString());
            return Result;
        }

        public long toLong() {
            long Result = 0;

            Result = Long.decode(toString());
            return Result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(digitsLength * 10);
            sb = sb.append(digits[digitsLength - 1]);
            for (int j = digitsLength - 2; j >= 0; j--) {
                sb = sb.append(Integer.toString(digits[j] + (int) mod).substring(1));
            }
            return sb.toString();
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
}
