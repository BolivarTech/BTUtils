package com.bolivartech.utils.converters;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010 BolivarTech C.A.
 *
 * <p>Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>A collection of utility methods used for converting from variables to
 * arrays.</p>
 *
 * Class ID: "35DGFH8"
 * Loc: 000-003
 *
 * @author Julian Bolivar
 * @since 2007 - December 11, 2007.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2007-12-11) Version Inicial.
 * v1.0.1 (2016-03-25) Se implemento el uso del codigo de ubicacion unico 
 */
public class Converter {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFH8";
    
    // Codigo de Error de Conversion
    public static final int ERRORCONVERTINGNUM = -1;

    /** 
     * Constructor privado
     */
    private Converter() {
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
     * Realiza la convercion de un arreglo de bytes en un arreglo de long, el
     * numero de elementos del arreglo de bytes debe ser multiplo de 8 sino
     * regresa null.
     *
     * @param Entrada
     * @return Arreglo de long
     * @throws UtilsException
     */
    public static long[] byte2long(byte[] Entrada) throws UtilsException {
        long[] Salida;
        int i, j;
        int base = 8;
        int multi;
        long temp;

        multi = 64 / base;
        Salida = null;
        if ((Entrada.length % multi) == 0) {
            Salida = new long[Entrada.length / multi];
            for (i = 0; i < Salida.length; i++) {
                for (j = 0; j < multi; j++) {
                    temp = (long) Entrada[(i * multi) + j];
                    temp = temp << (64 - base);
                    temp = temp >>> (64 - base);
                    temp = (temp << (64 - base) - (base * j));
                    Salida[i] = Salida[i] | temp;
                }
            }
        } else {
            throw new UtilsException("ERROR(byte2long): Numero de Bytes no es multiplo de " + multi,Converter.ERRORCONVERTINGNUM,Converter.CLASSID+"000");
        }
        return Salida;
    }

    /**
     * Realiza la conversion de un arreglo de longs en un arreglo de bytes
     *
     * @param Entrada
     * @return Arreglo de bytes
     */
    public static byte[] long2byte(long[] Entrada) {
        byte[] Salida;
        int i, j;
        int multi;
        int base = 8;
        long temp;

        multi = 64 / base;
        Salida = null;
        Salida = new byte[multi * Entrada.length];
        for (i = 0; i < Entrada.length; i++) {
            for (j = 0; j < multi; j++) {
                temp = Entrada[i] << (base * j);
                Salida[multi * i + j] = (byte) (temp >>> (64 - base));
            }
        }
        return Salida;
    }

    /**
     * Realiza la convercion de un arreglo de bytes en un arreglo de int, el
     * numero de elementos del arreglo de bytes debe ser multiplo de 4 sino
     * regresa null.
     *
     * @param Entrada es un arreglo de bytes
     * @return Arreglo de int
     * @throws UtilsException
     */
    public static int[] byte2int(byte[] Entrada) throws UtilsException {
        int[] Salida;
        int i, j;
        int base = 8;
        int multi;
        int temp;

        multi = 32 / base;
        Salida = null;
        if ((Entrada.length % multi) == 0) {
            Salida = new int[Entrada.length / multi];
            for (i = 0; i < Salida.length; i++) {
                for (j = 0; j < multi; j++) {
                    temp = (int) Entrada[(i * multi) + j];
                    temp = temp << (32 - base);
                    temp = temp >>> (32 - base);
                    temp = (temp << (32 - base) - (base * j));
                    Salida[i] = Salida[i] | temp;
                }
            }
        } else {
            throw new UtilsException("ERROR(byte2int): Numero de Bytes no es multiplo de " + multi,Converter.ERRORCONVERTINGNUM,Converter.CLASSID+"001");
        }
        return Salida;
    }

    /**
     * Realiza la conversion de un arreglo de int en un arreglo de bytes
     *
     * @param Entrada
     * @return Arreglo de bytes
     */
    public static byte[] int2byte(int[] Entrada) {
        byte[] Salida;
        int i, j;
        int multi;
        int base = 8;
        long temp;

        multi = 32 / base;
        Salida = null;
        Salida = new byte[multi * Entrada.length];
        for (i = 0; i < Entrada.length; i++) {
            for (j = 0; j < multi; j++) {
                temp = Entrada[i] << (base * j);
                Salida[multi * i + j] = (byte) (temp >>> (32 - base));
            }
        }
        return Salida;
    }

    /**
     * Realiza la convercion de un arreglo de enteros en un arreglo de long, el
     * numero de elementos del arreglo de longs debe ser multiplo de 2 sino
     * regresa null
     *
     * @param Entrada
     * @return Arreglo de longs
     * @throws UtilsException
     */
    public static long[] int2long(int[] Entrada) throws UtilsException {
        long[] Salida;
        int i, j;
        int multi;
        int base = 32;
        long temp;

        multi = 64 / base;
        Salida = null;
        if ((Entrada.length % multi) == 0) {
            Salida = new long[Entrada.length / multi];
            for (i = 0; i < Salida.length; i++) {
                for (j = 0; j < multi; j++) {
                    temp = (long) Entrada[(i * multi) + j];
                    temp = temp << (64 - base);
                    temp = temp >>> (64 - base);
                    temp = (temp << (64 - base) - (base * j));
                    Salida[i] = Salida[i] | temp;
                }
            }
        } else {
            throw new UtilsException("ERROR(int2long): Numero de Bytes no es multiplo de " + multi,Converter.ERRORCONVERTINGNUM,Converter.CLASSID+"002");
        }
        return Salida;
    }

    /**
     * Realiza la conversion de un arreglo de longs en un arreglo de enteros
     *
     * @param Entrada
     * @return Arreglo de enteros
     */
    public static int[] long2int(long[] Entrada) {
        int[] Salida;
        int i, j;
        int multi;
        int base = 32;
        long temp;

        multi = 64 / base;
        Salida = null;
        Salida = new int[multi * Entrada.length];
        for (i = 0; i < Entrada.length; i++) {
            for (j = 0; j < multi; j++) {
                temp = Entrada[i] << (base * j);
                Salida[multi * i + j] = (int) (temp >>> (64 - base));
            }
        }
        return Salida;
    }

    /**
     * Realiza la convercion de un arreglo de short en un arreglo de long, el
     * numero de elementos del arreglo de longs debe ser multiplo de 2 sino
     * regresa null
     *
     * @param Entrada
     * @return Arreglo de longs
     * @throws UtilsException
     */
    public static long[] short2long(short[] Entrada) throws UtilsException {
        long[] Salida;
        int i, j;
        int multi;
        int base = 16;
        long temp;

        multi = 64 / base;
        Salida = null;
        if ((Entrada.length % multi) == 0) {
            Salida = new long[Entrada.length / multi];
            for (i = 0; i < Salida.length; i++) {
                for (j = 0; j < multi; j++) {
                    temp = (long) Entrada[(i * multi) + j];
                    temp = temp << (64 - base);
                    temp = temp >>> (64 - base);
                    temp = (temp << (64 - base) - (base * j));
                    Salida[i] = Salida[i] | temp;
                }
            }
        } else {
            throw new UtilsException("ERROR(int2long): Numero de Bytes no es multiplo de " + multi,Converter.ERRORCONVERTINGNUM,Converter.CLASSID+"003");
        }
        return Salida;
    }

    /**
     * Realiza la conversion de un arreglo de longs en un arreglo de short
     *
     * @param Entrada
     * @return Arreglo de enteros
     */
    public static short[] long2short(long[] Entrada) {
        short[] Salida;
        int i, j;
        int multi;
        int base = 16;
        long temp;

        multi = 64 / base;
        Salida = null;
        Salida = new short[multi * Entrada.length];
        for (i = 0; i < Entrada.length; i++) {
            for (j = 0; j < multi; j++) {
                temp = Entrada[i] << (base * j);
                Salida[multi * i + j] = (short) (temp >>> (64 - base));
            }
        }
        return Salida;
    }

    /**
     * Convierte un arreglo de bytes en su representacion hexadecimal en un
     * string
     *
     * @param raw Arreglo de bytes a convertir en un string hexadecimal.
     * @return String con la representacion hexadecimal del arreglo de bytes
     */
    public static String byte2StringHex(byte[] raw) {
        String HEXES = "0123456789ABCDEF";
        StringBuilder hex;

        if (raw == null) {
            return null;
        }
        hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    //......................................................................
    /**
     * Returns a byte array from a string of hexadecimal digits.
     *
     * @param hex string of hex characters
     * @return byte array of binary data corresponding to hex string input
     */
    public static byte[] StringHex2bytes(String hex) {
        int len = hex.length();
        byte[] buf = new byte[((len + 1) / 2)];

        int i = 0, j = 0;
        if ((len % 2) == 1) {
            buf[j++] = (byte) hexDigit(hex.charAt(i++));
        }
        while (i < len) {
            buf[j++] = (byte) ((hexDigit(hex.charAt(i++)) << 4)
                    | hexDigit(hex.charAt(i++)));
        }
        return buf;
    }

    //......................................................................
    /**
     * Returns true if the string consists ONLY of valid hex characters
     *
     * @param hex string of hex characters
     * @return true if a valid hex string
     */
    public static boolean isHex(String hex) {
        int len = hex.length();
        int i = 0;
        char ch;

        while (i < len) {
            ch = hex.charAt(i++);
            if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')
                    || (ch >= 'a' && ch <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    //......................................................................
    /**
     * Returns the number from 0 to 15 corresponding to the hex digit <i>ch</i>.
     *
     * @param ch hex digit character (must be 0-9A-Fa-f)
     * @return numeric equivalent of hex digit (0-15)
     */
    private static int hexDigit(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'A' && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if (ch >= 'a' && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return (0);	// any other char is treated as 0
    }
}
