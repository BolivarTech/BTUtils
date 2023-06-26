package com.bolivartech.utils.statistics;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2015,2016 BolivarTech C.A.
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's that define Statistics data analysis
 * functions.
 *
 * Define funciones de analisis estadistico de datos.
 * 
 * Class ID: "35DGFHL"
 * Loc: 000-011
 *
 * @author Julian Bolivar
 * @since 2015, March 25, 2016.
 * @version 1.0.1
 *
 * Change Logs: 
 * v1.0.0 (2015-12-14) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public class Statistics {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHL";

    public final static int ERRORNULLINPUT = -1;

    /**
     * Calcula la media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Media
     * @return Media del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final double Mean(double[] input) throws UtilsException {
        int i;
        double Sum = 0;
        double Mean = 0;

        if (input != null) {
            if (input.length > 0) {
                for (i = 0; i < input.length; i++) {
                    Sum += input[i];
                }
                Mean = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"000");
        }
        return Mean;
    }

    /**
     * Calcula la media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Media
     * @return Media del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final long Mean(long[] input) throws UtilsException {
        int i;
        long Sum = 0;
        long Mean = 0;

        if (input != null) {
            if (input.length > 0) {
                for (i = 0; i < input.length; i++) {
                    Sum += input[i];
                }
                Mean = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"001");
        }
        return Mean;
    }

    /**
     * Calcula la media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Media
     * @return Media del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final int Mean(int[] input) throws UtilsException {
        int i;
        int Sum = 0;
        int Mean = 0;

        if (input != null) {
            if (input.length > 0) {
                for (i = 0; i < input.length; i++) {
                    Sum += input[i];
                }
                Mean = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"002");
        }
        return Mean;
    }

    /**
     * Calcula la Varianza de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Varianza
     * @return La Varianza del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final double Variance(double[] input) throws UtilsException {
        int i;
        double Sum = 0;
        double Mean2 = 0;
        double Variance = 0;

        if (input != null) {
            if (input.length > 0) {
                Mean2 = Math.pow(Mean(input),2);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.pow(input[i], 2);
                }
                Variance = (Sum / input.length);
                Variance -= Mean2;
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"003");
        }
        return Variance;
    }

    /**
     * Calcula la Varianza de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Varianza
     * @return La Varianza del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final long Variance(long[] input) throws UtilsException {
        int i;
        long Sum = 0;
        long Mean2 = 0;
        long Variance = 0;

        if (input != null) {
            if (input.length > 0) {
                Mean2 = (long) Math.pow(Mean(input),2);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.pow(input[i], 2);
                }
                Variance = (Sum / input.length);
                Variance -= Mean2;
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"004");
        }
        return Variance;
    }
       
    /**
     * Calcula la Varianza de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Varianza
     * @return La Varianza del arreglo de datos
     * @throws UtilsException si entrada es NULL
     */
    public static final int Variance(int[] input) throws UtilsException {
        int i;
        int Sum = 0;
        int Mean2 = 0;
        int Variance = 0;

        if (input != null) {
            if (input.length > 0) {
                Mean2 = (int) Math.pow(Mean(input),2);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.pow(input[i], 2);
                }
                Variance = (Sum / input.length);
                Variance -= Mean2;
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"005");
        }
        return Variance;
    }

    /**
     * Calcula la Desviación Media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviación Media
     * @return La Desviación Media del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final double averageAbsoluteDeviation(double[] input) throws UtilsException {
        int i;
        double Sum = 0;
        double cMean = 0;
        double Deviation = 0;

        if (input != null) {
            if (input.length > 0) {
                cMean = Mean(input);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.abs((input[i] - cMean));
                }
                Deviation = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"006");
        }
        return Deviation;
    }

    /**
     * Calcula la Desviación Media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviación Media
     * @return La Desviación Media del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final long averageAbsoluteDeviation(long[] input) throws UtilsException {
        int i;
        long Sum = 0;
        long cMean = 0;
        long Deviation = 0;

        if (input != null) {
            if (input.length > 0) {
                cMean = Mean(input);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.abs((input[i] - cMean));
                }
                Deviation = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"007");
        }
        return Deviation;
    }

    /**
     * Calcula la Desviación Media de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviación Media
     * @return La Desviación Media del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final int averageAbsoluteDeviation(int[] input) throws UtilsException {
        int i;
        int Sum = 0;
        int cMean = 0;
        int Deviation = 0;

        if (input != null) {
            if (input.length > 0) {
                cMean = Mean(input);
                for (i = 0; i < input.length; i++) {
                    Sum += Math.abs((input[i] - cMean));
                }
                Deviation = (Sum / input.length);
            }
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"008");
        }
        return Deviation;
    }

    /**
     * Calcula la Desviacion Estandar de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviacion Estandar
     * @return La Desviacion Estandar del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final double standardDeviation(double[] input) throws UtilsException {
        double StandardDeviation = 0;

        if (input != null) {
            StandardDeviation = Math.sqrt(Variance(input));
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"009");
        }
        return StandardDeviation;
    }

    /**
     * Calcula la Desviacion Estandar de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviacion Estandar
     * @return La Desviacion Estandar del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final double standardDeviation(long[] input) throws UtilsException {
        double StandardDeviation = 0;

        if (input != null) {
            StandardDeviation = Math.sqrt((double)Variance(input));
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"010");
        }
        return StandardDeviation;
    }

    /**
     * Calcula la Desviacion Estandar de un arreglo de datos.
     *
     * @param input Arreglo de datos a calcular la Desviacion Estandar
     * @return La Desviacion Estandar del arreglo
     * @throws UtilsException si entrada es NULL
     */
    public static final double standardDeviation(int[] input) throws UtilsException {
        double StandardDeviation = 0;

        if (input != null) {
            StandardDeviation = Math.sqrt((double)Variance(input));
        } else {
            throw new UtilsException("ERROR: Input is NULL", Statistics.ERRORNULLINPUT,Statistics.CLASSID+"011");
        }
        return StandardDeviation;
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
