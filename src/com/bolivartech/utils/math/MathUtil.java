package com.bolivartech.utils.math;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014 BolivarTech C.A.
 *
 * <p>
 * Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's utils for Math.
 *
 * Realiza operaciones matematicas que no estan en la libreria estandar de
 * JAVA
 *
 * Class ID: "35DGFHC"
 * Loc: 000
 * 
 * @author Julian Bolivar
 * @since 2007 - March 25, 2016.
 * @version 2.2.2
 *
 * Change Log:
 * v2.2.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 * v2.2.0 (2015-07-22): Se corrigio un bug en el methodo roundToDecimals .
 * v2.1.0 (2014-12-19): Se agrego el metodo que convierte un decimal en un
 * racional.
 */
public strictfp class MathUtil {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHC";

    /**
     * El Numero de decimales a redondear es menor que 0.
     */
    public final static int NumDecimalsLessThanZero = 3;

    /**
     * Constructor privado
     */
    private MathUtil() {
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
     * Realiza el redondeo de un numero double a una cantidad especifica de
     * decimales
     *
     * @param d numero a redondear los decimales
     * @param numeroDecimales cantidad de decimales a redondear
     * @return Numero con los decimaneles redondeados a numeroDecimales
     * @throws UtilsException
     */
    public static double roundToDecimals(double d, int numeroDecimales) throws UtilsException {
        double decimal;

        decimal = d;
        if (numeroDecimales >= 0) {
            decimal = decimal * (java.lang.Math.pow(10, numeroDecimales));
            decimal = java.lang.Math.round(decimal);
            decimal = decimal / java.lang.Math.pow(10, numeroDecimales);
        } else {
            throw new UtilsException("ERROR: Decimals to round is less that Zero", MathUtil.NumDecimalsLessThanZero,MathUtil.CLASSID+"000");
        }
        return decimal;
    }

    /**
     * Convierte un numero decimal en un numero racional.
     *
     * La salida esta en un arreglo de 2 elementos, el numerador y el
     * denominador de la forma Salida[0]/Salida[1].
     *
     * @param x Numero decimal a convertir en racional.
     * @return Salida[0]/Salida[1]
     */
    public static long[] DoubleToRational(double x) {
        long[] Fraction = null;
        double tolerance = 1.0E-7;
        double h1, h2, k1, k2, b, a, aux;
        long Signum;

        Fraction = new long[2];
        Signum = (long) Math.signum(x);
        x = Math.abs(x);
        if (x > 0) {
            h1 = 1;
            h2 = 0;
            k1 = 0;
            k2 = 1;
            b = x;
            do {
                a = Math.floor(b);
                aux = h1;
                h1 = a * h1 + h2;
                h2 = aux;
                aux = k1;
                k1 = a * k1 + k2;
                k2 = aux;
                b = 1 / (b - a);
            } while (Math.abs(x - h1 / k1) > x * tolerance);
            Fraction[0] = (long) h1;
            Fraction[1] = (long) k1;
        } else {
            Fraction[0] = (long) 0;
            Fraction[1] = (long) 1;
        }
        Fraction[0] *= Signum;
        return Fraction;
    }

}
