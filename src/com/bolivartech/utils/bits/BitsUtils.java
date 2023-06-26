package com.bolivartech.utils.bits;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2014 BolivarTech C.A.
 *
 * <p>
 * Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's utils for Bits Manipulation.
 *
 * Realiza operaciones de bits que no estan en la libreria estandar de
 * JAVA
 * 
 * Class ID: "35DGFH4"
 * Loc: 000-027
 *
 * @author Julian Bolivar
 * @since 2014 - October 17, 2015.
 * @version 1.1.1
 * 
 * Change Logs: 
 * v1.0.0 (2014-08-17) Version Inicial.
 * v1.1.0 (2015-10-17) Bug Solventado
 * v1.1.1 (2016-03-25) Uso del codigo de ubicacion unico 
 */
public class BitsUtils {

    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFH4";
    
    /**
     * El valor del Shift es mayor que la cantidad de bits que tiene el tipo de
     * dato
     */
    public final static int ShiftBiggerSizeBits = 1;

    /**
     * El valor del Shift menor que 0.
     */
    public final static int ShiftLessThanZero = 2;

    /**
     * La posicion del Bit esta fuera del rango del dato
     */
    public final static int BitPositionOutsideRange = 4;

    private BitsUtils() {
    }

    /**
     * Realiza la rotacion de los bits a la Izquierda.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=8
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static byte ByteLeftRotation(byte value, int shift) throws UtilsException {
        int NumBits;
        int Tvalue;

        if (shift >= 0) {
            NumBits = 8;
            if (shift <= NumBits) {
                Tvalue = value;
                Tvalue = Tvalue & 0x000000FF;
                return (byte) (((Tvalue << shift) | (Tvalue >>> (NumBits - shift))) & 0x000000FF);
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"000");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"001");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Derecha.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=8
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static byte ByteRightRotation(byte value, int shift) throws UtilsException {
        int NumBits;
        int Tvalue;

        if (shift >= 0) {
            NumBits = 8;
            if (shift <= NumBits) {
                Tvalue = value;
                Tvalue = Tvalue & 0x000000FF;
                return (byte) (((Tvalue >>> shift) | (Tvalue << (NumBits - shift))) & 0x000000FF);
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"002");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"003");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Izquierda.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=16
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static short ShortLeftRotation(short value, int shift) throws UtilsException {
        int NumBits;
        int Tvalue;

        if (shift >= 0) {
            NumBits = 16;
            if (shift <= NumBits) {
                Tvalue = value;
                Tvalue = Tvalue & 0x0000FFFF;
                return (short) (((Tvalue << shift) | (Tvalue >>> (NumBits - shift))) & 0x0000FFFF);
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"004");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"005");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Derecha.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=16
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static short ShortRightRotation(short value, int shift) throws UtilsException {
        int NumBits;
        int Tvalue;

        if (shift >= 0) {
            NumBits = 16;
            if (shift <= NumBits) {
                Tvalue = value;
                Tvalue = Tvalue & 0x0000FFFF;
                return (short) (((Tvalue >>> shift) | (Tvalue << (NumBits - shift))) & 0x0000FFFF);
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"006");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"007");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Izquierda.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=32
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static int IntLeftRotation(int value, int shift) throws UtilsException {
        int NumBits;

        if (shift >= 0) {
            NumBits = 32;
            if (shift <= NumBits) {
                return (int) ((value << shift) | (value >>> (NumBits - shift)));
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"008");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"009");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Derecha.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=32
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static int IntRightRotation(int value, int shift) throws UtilsException {
        int NumBits;

        if (shift >= 0) {
            NumBits = 32;
            if (shift <= NumBits) {
                return (int) ((value >>> shift) | (value << (NumBits - shift)));
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"010");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"011");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Izquierda.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=64
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static long LongLeftRotation(long value, int shift) throws UtilsException {
        int NumBits;

        if (shift >= 0) {
            NumBits = 64;
            if (shift <= NumBits) {
                return (long) ((value << shift) | (value >>> (NumBits - shift)));
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"012");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"013");
        }
    }

    /**
     * Realiza la rotacion de los bits a la Derecha.
     *
     * @param value Valor a rotar
     * @param shift Cantidad de bits a rotar 0&lt;= shift &lt;=64
     * @return Valor rotado la candidad de bits especificada
     * @throws UtilsException
     */
    public static long LongRightRotation(long value, int shift) throws UtilsException {
        int NumBits;

        if (shift >= 0) {
            NumBits = 64;
            if (shift <= NumBits) {
                return (long) ((value >>> shift) | (value << (NumBits - shift)));
            } else {
                throw new UtilsException("ERROR: Bits to rotate is bigger that data bits size", BitsUtils.ShiftBiggerSizeBits,BitsUtils.CLASSID+"014");
            }
        } else {
            throw new UtilsException("ERROR: Bits to rotate is less that Zero", BitsUtils.ShiftLessThanZero,BitsUtils.CLASSID+"015");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en UNO dentro de un
     * byte.
     *
     * @param Input Byte donde se va a establecer el bit
     * @param BitPosition Position del bit a ser establecido en UNO 0&lt;=
     * BitPosition &lt;= 7
     * @return Byte con el bit esblecido en UNO
     * @throws UtilsException
     */
    public static byte ByteSetBit(byte Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 8)) {
            return (byte) ((Input | (0x01 << BitPosition)) & 0x000000FF);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"016");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en CERO dentro de un
     * byte.
     *
     * @param Input Byte donde se va a establecer el bit en cero
     * @param BitPosition Position del bit a ser establecido en CERO 0&lt;=
     * BitPosition &lt;= 7
     * @return Byte con el bit esblecido en CERO
     * @throws UtilsException
     */
    public static byte ByteUnsetBit(byte Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 8)) {
            return (byte) ((Input & (~(0x01 << BitPosition))) & 0x000000FF);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"017");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en UNO dentro de un
     * Short.
     *
     * @param Input Short donde se va a establecer el bit
     * @param BitPosition Position del bit a ser establecido en UNO 0&lt;=
     * BitPosition &lt;= 15
     * @return Short con el bit esblecido en UNO
     * @throws UtilsException
     */
    public static short ShortSetBit(short Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 16)) {
            return (short) ((Input | (0x01 << BitPosition)) & 0x0000FFFF);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"018");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en CERO dentro de un
     * Short.
     *
     * @param Input Short donde se va a establecer el bit en cero
     * @param BitPosition Position del bit a ser establecido en CERO 0&lt;=
     * BitPosition &lt;= 15
     * @return Short con el bit esblecido en CERO
     * @throws UtilsException
     */
    public static short ShortUnsetBit(short Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 16)) {
            return (short) ((Input & (~(0x01 << BitPosition))) & 0x0000FFFF);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"019");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en UNO dentro de un
     * Int.
     *
     * @param Input Int donde se va a establecer el bit
     * @param BitPosition Position del bit a ser establecido en UNO 0&lt;=
     * BitPosition &lt;= 31
     * @return Int con el bit esblecido en UNO
     * @throws UtilsException
     */
    public static int IntSetBit(int Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 32)) {
            return (int) (Input | (0x01 << BitPosition));
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"020");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en CERO dentro de un
     * Int.
     *
     * @param Input Int donde se va a establecer el bit en cero
     * @param BitPosition Position del bit a ser establecido en CERO 0&lt;=
     * BitPosition &lt;= 31
     * @return Int con el bit esblecido en CERO
     * @throws UtilsException
     */
    public static int IntUnsetBit(int Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 32)) {
            return (int) (Input & (~(0x01 << BitPosition)));
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"021");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en UNO dentro de un
     * Long.
     *
     * @param Input Long donde se va a establecer el bit
     * @param BitPosition Position del bit a ser establecido en UNO 0&lt;=
     * BitPosition &lt;= 63
     * @return Long con el bit esblecido en UNO
     * @throws UtilsException
     */
    public static long LongSetBit(long Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 64)) {
            return (long) (Input | (0x01L << BitPosition));
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"022");
        }
    }

    /**
     * Establece el bit especificado por "BitPosition" en CERO dentro de un
     * Long.
     *
     * @param Input Long donde se va a establecer el bit en cero
     * @param BitPosition Position del bit a ser establecido en CERO 0&lt;=
     * BitPosition &lt;= 63
     * @return Long con el bit esblecido en CERO
     * @throws UtilsException
     */
    public static long LongUnsetBit(long Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 64)) {
            return (long) (Input & (~(0x01L << BitPosition)));
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"023");
        }
    }

    /**
     * Calcula la paridad de un byte usando el metodo XOR de Eric
     * Sosman.
     *
     * Retorna 0 si es impar y 1 si es par.
     *
     * @param Input
     * @return 0 si es impar y 1 si es par.
     */
    public static int ByteParity(byte Input) {
        int bb;
        int parity;

        bb = Input & 0xFF;
        parity = bb ^ (bb >> 4);
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (~parity & 0x01);
    }

    /**
     * Calcula la paridad de un byte usando el metodo XOR de Eric Sosman
     *
     * @param Input
     * @return TRUE si es impar y FALSE si es par.
     */
    public static boolean isByteOddParity(byte Input) {
        int bb;
        int parity;

        bb = Input & 0xFF;
        parity = bb ^ (bb >> 4);
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (parity & 0x01) != 0;
    }

    /**
     * Calcula la paridad de un short usando el metodo XOR de Eric
     * Sosman.
     *
     * Retorna 0 si es impar y 1 si es par.
     *
     * @param Input
     * @return 0 si es impar y 1 si es par.
     */
    public static int ShortParity(short Input) {
        int bb;
        int parity;

        bb = Input & 0xFFFF;
        parity = bb ^ (bb >> 8);
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (~parity & 0x0001);
    }

    /**
     * Calcula la paridad de un short usando el metodo XOR de Eric Sosman.
     *
     * @param Input
     * @return TRUE si es impar y FALSE si es par.
     */
    public static boolean isShortOddParity(short Input) {
        int bb;
        int parity;

        bb = Input & 0xFFFF;
        parity = bb ^ (bb >> 8);
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (parity & 0x0001) != 0;
    }

    /**
     * Calcula la paridad de un int usando el metodo XOR de Eric
     * Sosman.
     *
     * Retorna 0 si es impar y 1 si es par.
     *
     * @param Input
     * @return 0 si es impar y 1 si es par.
     */
    public static int IntParity(int Input) {
        int bb;
        int parity;

        bb = Input & 0xFFFFFFFF;
        parity = bb ^ (bb >> 16);
        parity ^= parity >> 8;
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (~parity & 0x00000001);
    }

    /**
     * Calcula la paridad de un int usando el metodo XOR de Eric Sosman.
     *
     * @param Input
     * @return TRUE si es impar y FALSE si es par.
     */
    public static boolean isIntOddParity(int Input) {
        int bb;
        int parity;

        bb = Input & 0xFFFFFFFF;
        parity = bb ^ (bb >> 16);
        parity ^= parity >> 8;
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (parity & 0x00000001) != 0;
    }

    /**
     * Calcula la paridad de un long usando el metodo XOR de Eric
     * Sosman
     *
     * Retorna 0 si es impar y 1 si es par.
     *
     * @param Input
     * @return 0 si es impar y 1 si es par.
     */
    public static long LongParity(long Input) {
        long bb;
        long parity;

        bb = Input & 0xFFFFFFFFFFFFFFFFL;
        parity = bb ^ (bb >> 32);
        parity ^= parity >> 16;
        parity ^= parity >> 8;
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (~parity & 0x0000000000000001L);
    }

    /**
     * Calcula la paridad de un long usando el metodo XOR de Eric Sosman
     *
     * @param Input
     * @return TRUE si es impar y FALSE si es par.
     */
    public static boolean isLongOddParity(long Input) {
        long bb;
        long parity;

        bb = Input & 0xFFFFFFFFFFFFFFFFL;
        parity = bb ^ (bb >> 32);
        parity ^= parity >> 16;
        parity ^= parity >> 8;
        parity ^= parity >> 4;
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (parity & 0x0000000000000001L) != 0;
    }

    /**
     * Calcula la cantidad de "1" en Input.
     *
     * NOTA: Es importante que la entrada sea un LONG porque sino se pueden
     * producir resultados erroneos.
     *
     * @param Input entrada
     * @return Cantidad de "1" en Input.
     */
    public static long LongPopCount(long Input) {
        long c; // store the total here
        long S[] = {1, 2, 4, 8, 16, 32}; // Magic Binary Numbers
        /*
         B[0] = 0x5555555555555555 = 01010101 01010101 01010101 01010101 01010101 01010101 01010101 01010101
         B[1] = 0x3333333333333333 = 00110011 00110011 00110011 00110011 00110011 00110011 00110011 00110011
         B[2] = 0x0F0F0F0F0F0F0F0F = 00001111 00001111 00001111 00001111 00001111 00001111 00001111 00001111
         B[3] = 0x00FF00FF00FF00FF = 00000000 11111111 00000000 11111111 00000000 11111111 00000000 11111111
         B[4] = 0x0000FFFF0000FFFF = 00000000 00000000 11111111 11111111 00000000 00000000 11111111 11111111
         B[5] = 0x00000000FFFFFFFF = 00000000 00000000 00000000 00000000 11111111 11111111 11111111 11111111
         */
        long B[] = {0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL, 0x00000000FFFFFFFFL};

        c = Input - ((Input >>> 1) & B[0]);
        c = ((c >>> S[1]) & B[1]) + (c & B[1]);
        c = ((c >>> S[2]) + c) & B[2];
        c = ((c >>> S[3]) + c) & B[3];
        c = ((c >>> S[4]) + c) & B[4];
        return ((c >>> S[5]) + c) & B[5];
    }

    /**
     * Calcula la cantidad de "1" en Input.
     *
     * NOTA: Es importante que la entrada sea un INT porque sino se pueden
     * producir resultados erroneos.
     *
     * @param Input entrada
     * @return Cantidad de "1" en Input.
     */
    public static int IntPopCount(int Input) {
        int c; // store the total here
        int S[] = {1, 2, 4, 8, 16}; // Magic Binary Numbers
        /*
         B[0] = 0x55555555 = 01010101 01010101 01010101 01010101
         B[1] = 0x33333333 = 00110011 00110011 00110011 00110011
         B[2] = 0x0F0F0F0F = 00001111 00001111 00001111 00001111
         B[3] = 0x00FF00FF = 00000000 11111111 00000000 11111111
         B[4] = 0x0000FFFF = 00000000 00000000 11111111 11111111
         */
        int B[] = {0x55555555, 0x33333333, 0x0F0F0F0F, 0x00FF00FF, 0x0000FFFF};

        c = Input - ((Input >>> 1) & B[0]);
        c = ((c >>> S[1]) & B[1]) + (c & B[1]);
        c = ((c >>> S[2]) + c) & B[2];
        c = ((c >>> S[3]) + c) & B[3];
        return ((c >>> S[4]) + c) & B[4];
    }

    /**
     * Calcula la cantidad de "1" en Input.
     *
     * NOTA: Es importante que la entrada sea un Short porque sino se pueden
     * producir resultados erroneos.
     *
     * @param Input entrada
     * @return Cantidad de "1" en Input.
     */
    public static short ShortPopCount(short Input) {
        short c; // store the total here
        short S[] = {1, 2, 4, 8}; // Magic Binary Numbers
        /*
         B[0] = 0x5555 = 01010101 01010101
         B[1] = 0x3333 = 00110011 00110011
         B[2] = 0x0F0F = 00001111 00001111
         B[3] = 0x00FF = 00000000 11111111
         */
        short B[] = {0x5555, 0x3333, 0x0F0F, 0x00FF};

        c = (short) (Input - ((Input >>> 1) & B[0]));
        c = (short) ((short) ((c >>> S[1]) & B[1]) + (c & B[1]));
        c = (short) ((short) ((c >>> S[2]) + c) & B[2]);
        return (short) ((short) ((c >>> S[3]) + c) & B[3]);
    }

    /**
     * Calcula la cantidad de "1" en Input.
     *
     * NOTA: Es importante que la entrada sea un BYTE porque sino se pueden
     * producir resultados erroneos.
     *
     * @param Input entrada
     * @return Cantidad de "1" en Input.
     */
    public static byte BytePopCount(byte Input) {
        byte c; // store the total here
        byte S[] = {1, 2, 4}; // Magic Binary Numbers
        /*
         B[0] = 0x55 = 01010101
         B[1] = 0x33 = 00110011
         B[2] = 0x0F = 00001111
         */
        byte B[] = {0x55, 0x33, 0x0F};

        c = (byte) (Input - ((Input >>> 1) & B[0]));
        c = (byte) (((c >>> S[1]) & B[1]) + (c & B[1]));
        return (byte) (((c >>> S[2]) + c) & B[2]);
    }

    /**
     * Retorna el valor del bit "BitPosition" dentro de "Input"
     *
     * @param Input Byte de entrada
     * @param BitPosition Posicion del Bit a recuperar 0&lt;= BitPosition &lt;=7
     * @return Bit en la posicion especificada
     * @throws UtilsException
     */
    public static byte ByteGetBit(byte Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 8)) {
            return (byte) ((Input >>> BitPosition) & 0x01);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"024");
        }
    }

    /**
     * Retorna el valor del bit "BitPosition" dentro de "Input"
     *
     * @param Input Short de entrada
     * @param BitPosition Posicion del Bit a recuperar 0&lt;= BitPosition
     * &lt;=15
     * @return Bit en la posicion especificada
     * @throws UtilsException
     */
    public static byte ShortGetBit(short Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 16)) {
            return (byte) ((Input >>> BitPosition) & 0x01);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"025");
        }
    }

    /**
     * Retorna el valor del bit "BitPosition" dentro de "Input"
     *
     * @param Input Int de entrada
     * @param BitPosition Posicion del Bit a recuperar 0&lt;= BitPosition
     * &lt;=31
     * @return Bit en la posicion especificada
     * @throws UtilsException
     */
    public static byte IntGetBit(int Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 32)) {
            return (byte) ((Input >>> BitPosition) & 0x01);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"026");
        }
    }

    /**
     * Retorna el valor del bit "BitPosition" dentro de "Input"
     *
     * @param Input Long de entrada
     * @param BitPosition Posicion del Bit a recuperar 0&lt;= BitPosition
     * &lt;=63
     * @return Bit en la posicion especificada
     * @throws UtilsException
     */
    public static byte LongGetBit(long Input, int BitPosition) throws UtilsException {

        if ((BitPosition >= 0) && (BitPosition < 64)) {
            return (byte) ((Input >>> BitPosition) & 0x01L);
        } else {
            throw new UtilsException("ERROR: Bits Position Outside Range", BitsUtils.BitPositionOutsideRange,BitsUtils.CLASSID+"027");
        }
    }

    /**
     * Retorna el numero de bits requeridos para representar un numero long.
     *
     * @param num Numero long a representar.
     * @return Numero de bits requeridos
     */
    public static int bitsRequired(long num) {
        // Derived from Hacker's Delight, Figure 5-9
        long y = num; // for checking right bits
        int n = 0; // number of leading zeros found
        while (true) {
            // 64 = number of bits in a long
            if (num < 0) {
                return 64 - n; // no leading zeroes left
            }
            if (y == 0) {
                return n; // no bits left to check
            }
            n++;
            num = num << 1; // check leading bits
            y = y >> 1; // check trailing bits
        }
    }
    
    /**
     * Retorna el numero de bits requeridos para representar un numero int.
     *
     * @param num Numero int a representar.
     * @return Numero de bits requeridos
     */
    public static int bitsRequired(int num) {
        // Derived from Hacker's Delight, Figure 5-9
        int y = num; // for checking right bits
        int n = 0; // number of leading zeros found
        while (true) {
            // 32 = number of bits in a integer
            if (num < 0) {
                return 32 - n; // no leading zeroes left
            }
            if (y == 0) {
                return n; // no bits left to check
            }
            n++;
            num = num << 1; // check leading bits
            y = y >> 1; // check trailing bits
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
