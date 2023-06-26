package com.bolivartech.utils.numericalalgorith;

import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * Copyright 2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 * <p>Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's NAFunction Class.
 *
 * Esta clase abstracta establece el marco para evaluar una funcion a ser
 * analizada con los algoritmos numericos, retornando el valor de evaluar la
 * funcion en un punto determinado.
 *
 * Esta es una clase abstracta en la cual se debe implementar la evaluacion de
 * la funcion en un punto determinado
 *
 * Class ID: "35DGFHG"
 * Loc: 000-014
 * 
 * @author Julian Bolivar
 * @since 2012,  March 25 2016.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2012-07-20) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public strictfp abstract class NAFunction {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHG";

    /**
     * Los valores de entrada de la funcion estan vacios
     */
    public final static int ERROREMPTYINPUT = -1;
    /**
     * El valor de la funcion evaluada en el punto es infinito
     */
    public final static int INFINITE = -2;
    /**
     * El valor de la funcion evaluada en el punto es interminado
     */
    public final static int UNDETERMINATED = -3;
    /**
     * Error en el formato de la entrada
     */
    public final static int ERRORINPUTFORMAT = -4;
    /**
     * Error en la posicion de la Variable a Derivar
     */
    public final static int ERRORVARPOS = -5;
    /**
     * Delta de incremento para los calculos numericos
     */
    private double h;

    /**
     * Constructor por defecto de la clase
     */
    public NAFunction() {
        h = 1e-2;
    }

    /**
     * Constructor con inicializacion del valor delta usado en los calculos
     * numericos
     *
     * @param Delta para el calculo numerico
     */
    public NAFunction(double Delta) {
        h = Math.abs(Delta);
    }

    /**
     * Establece el valor delta usado en los calculos numericos
     *
     * @param Delta Valor a utilizar en los calculos
     */
    public void setDelta(double Delta) {
        h = Math.abs(Delta);
    }

    /**
     * Retorna el valor delta usado en los calculos numericos
     *
     * @return Delta
     */
    public double getDelta() {
        return h;
    }

    /**
     * Restablece el valor de delta al valor por defecto de 1e-3
     */
    public void ResetDelta() {
        h = 1e-2;
    }

    /**
     * Metodo que retorna la evaluacion de la funcion en el punto determinado
     * por x[]
     *
     * @param x punto a evaluar la funcion
     * @return valor de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public abstract double Eval(double x[]) throws UtilsException;

    /**
     * Metodo que retorna la dimension de la funcion o el numero de variables
     * que tiene la funcion.
     *
     * @return Dimension de la funcion
     */
    public abstract int Dimension();

    /**
     * Metodo que retorna la evaluacion de la derivada de la funcion en el punto
     * determinado por x[] usando metodos numericos 
     *
     * @param x punto a evaluar
     * @return valor de la derivada de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double DEval(double x[]) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"000");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            MasDelta[i] = x[i] + h;
            MenosDelta[i] = x[i] - h;
            Mas2Delta[i] = x[i] + 2 * h;
            Menos2Delta[i] = x[i] - 2 * h;
        }
        Salida = (this.Eval(Menos2Delta) - this.Eval(Mas2Delta) - 8 * this.Eval(MenosDelta) + 8 * this.Eval(MasDelta)) / (12 * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada parcial de la funcion
     * respecto a la variable VarPos en el punto determinado por x[] usando
     * metodos numericos 
     * <b>(Requerido para resolver el metodo de newton-Raphson)</b>
     *
     * @param x punto a evaluar
     * @param VarPos Posicion de la Variable a derivar
     * @return valor de la derivada de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double DPEval(double x[], int VarPos) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"001");
        }
        if ((VarPos < 0) || (VarPos >= x.length)) {
            throw new UtilsException("Variable a derivar fuera del rango", ERRORVARPOS,NAFunction.CLASSID+"002");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            if (i == VarPos) {
                MasDelta[i] = x[i] + h;
                MenosDelta[i] = x[i] - h;
                Mas2Delta[i] = x[i] + 2 * h;
                Menos2Delta[i] = x[i] - 2 * h;
            } else {
                MasDelta[i] = x[i];
                MenosDelta[i] = x[i];
                Mas2Delta[i] = x[i];
                Menos2Delta[i] = x[i];
            }
        }
        Salida = (this.Eval(Menos2Delta) - this.Eval(Mas2Delta) - 8 * this.Eval(MenosDelta) + 8 * this.Eval(MasDelta)) / (12 * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada segunda de la funcion en
     * el punto determinado por x[] 
     *
     * @param x punto a evaluar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double D2Eval(double x[]) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"003");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            MasDelta[i] = x[i] + h;
            MenosDelta[i] = x[i] - h;
            Mas2Delta[i] = x[i] + 2 * h;
            Menos2Delta[i] = x[i] - 2 * h;
        }
        Salida = (-this.Eval(Mas2Delta) + 16 * this.Eval(MasDelta) - 30 * this.Eval(x) + 16 * this.Eval(MenosDelta) - this.Eval(Menos2Delta)) / (12 * h * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada parcial segunda de la
     * funcion respecto a la variable VarPos en el punto determinado por
     * x[] 
     *
     * @param x punto a evaluar
     * @param VarPos Posicion de la Variable a derivar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double DP2Eval(double x[], int VarPos) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"004");
        }
        if ((VarPos < 0) || (VarPos >= x.length)) {
            throw new UtilsException("Variable a derivar fuera del rango", ERRORVARPOS,NAFunction.CLASSID+"005");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            if (i == VarPos) {
                MasDelta[i] = x[i] + h;
                MenosDelta[i] = x[i] - h;
                Mas2Delta[i] = x[i] + 2 * h;
                Menos2Delta[i] = x[i] - 2 * h;
            } else {
                MasDelta[i] = x[i];
                MenosDelta[i] = x[i];
                Mas2Delta[i] = x[i];
                Menos2Delta[i] = x[i];
            }
        }
        Salida = (-this.Eval(Mas2Delta) + 16 * this.Eval(MasDelta) - 30 * this.Eval(x) + 16 * this.Eval(MenosDelta) - this.Eval(Menos2Delta)) / (12 * h * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada tercera de la funcion en
     * el punto determinado por x[]
     *
     * @param x punto a evaluar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double D3Eval(double x[]) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];
        double Mas3Delta[];
        double Menos3Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"006");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        Mas3Delta = new double[NumEle];
        Menos3Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            MasDelta[i] = x[i] + h;
            MenosDelta[i] = x[i] - h;
            Mas2Delta[i] = x[i] + 2 * h;
            Menos2Delta[i] = x[i] - 2 * h;
            Mas3Delta[i] = x[i] + 3 * h;
            Menos3Delta[i] = x[i] - 3 * h;
        }
        Salida = (this.Eval(Menos3Delta) - 8 * this.Eval(Menos2Delta) + 13 * this.Eval(MenosDelta) - 13 * this.Eval(MasDelta) + 8 * this.Eval(Mas2Delta) - this.Eval(Mas3Delta)) / (8 * h * h * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada parcia tercera de la
     * funcion respecto a la variable VarPos en el punto determinado por
     * x[]
     *
     * @param x punto a evaluar
     * @param VarPos Posicion de la Variable a derivar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double DP3Eval(double x[], int VarPos) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];
        double Mas3Delta[];
        double Menos3Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"007");
        }
        if ((VarPos < 0) || (VarPos >= x.length)) {
            throw new UtilsException("Variable a derivar fuera del rango", ERRORVARPOS,NAFunction.CLASSID+"008");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        Mas3Delta = new double[NumEle];
        Menos3Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            if (i == VarPos) {
                MasDelta[i] = x[i] + h;
                MenosDelta[i] = x[i] - h;
                Mas2Delta[i] = x[i] + 2 * h;
                Menos2Delta[i] = x[i] - 2 * h;
                Mas3Delta[i] = x[i] + 3 * h;
                Menos3Delta[i] = x[i] - 3 * h;
            } else {
                MasDelta[i] = x[i];
                MenosDelta[i] = x[i];
                Mas2Delta[i] = x[i];
                Menos2Delta[i] = x[i];
                Mas3Delta[i] = x[i];
                Menos3Delta[i] = x[i];
            }
        }
        Salida = (this.Eval(Menos3Delta) - 8 * this.Eval(Menos2Delta) + 13 * this.Eval(MenosDelta) - 13 * this.Eval(MasDelta) + 8 * this.Eval(Mas2Delta) - this.Eval(Mas3Delta)) / (8 * h * h * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada cuarta de la funcion en
     * el punto determinado por x[]
     *
     * @param x punto a evaluar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double D4Eval(double x[]) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];
        double Mas3Delta[];
        double Menos3Delta[];
        double Mas4Delta[];
        double Menos4Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"009");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        Mas3Delta = new double[NumEle];
        Menos3Delta = new double[NumEle];
        Mas4Delta = new double[NumEle];
        Menos4Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            MasDelta[i] = x[i] + h;
            MenosDelta[i] = x[i] - h;
            Mas2Delta[i] = x[i] + 2 * h;
            Menos2Delta[i] = x[i] - 2 * h;
            Mas3Delta[i] = x[i] + 3 * h;
            Menos3Delta[i] = x[i] - 3 * h;
            Mas4Delta[i] = x[i] + 4 * h;
            Menos4Delta[i] = x[i] - 4 * h;
        }
        Salida = (-1 * this.Eval(Menos3Delta) + 12 * this.Eval(Menos2Delta) - 39 * this.Eval(MenosDelta) + 56 * this.Eval(x) - 39 * this.Eval(MasDelta) + 12 * this.Eval(Mas2Delta) - this.Eval(Mas3Delta)) / (6 * h * h * h * h);
        return Salida;
    }

    /**
     * Metodo que retorna la evaluacion de la derivada cuarta de la funcion en
     * el punto determinado por x[]
     *
     * @param x punto a evaluar
     * @param VarPos Posicion de la Variable a derivar
     * @return valor de la derivada segunda de la funcion evaluada en x[]
     * @throws UtilsException
     */
    public strictfp double DP4Eval(double x[], int VarPos) throws UtilsException {
        int i, NumEle;
        double Salida;
        double MasDelta[];
        double MenosDelta[];
        double Mas2Delta[];
        double Menos2Delta[];
        double Mas3Delta[];
        double Menos3Delta[];
        double Mas4Delta[];
        double Menos4Delta[];

        if ((x == null) || (x.length < 1)) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"010");
        }
        if ((VarPos < 0) || (VarPos >= x.length)) {
            throw new UtilsException("Variable a derivar fuera del rango", ERRORVARPOS,NAFunction.CLASSID+"011");
        }
        NumEle = x.length;
        MasDelta = new double[NumEle];
        MenosDelta = new double[NumEle];
        Mas2Delta = new double[NumEle];
        Menos2Delta = new double[NumEle];
        Mas3Delta = new double[NumEle];
        Menos3Delta = new double[NumEle];
        Mas4Delta = new double[NumEle];
        Menos4Delta = new double[NumEle];
        for (i = 0; i < NumEle; i++) {
            if (i == VarPos) {
                MasDelta[i] = x[i] + h;
                MenosDelta[i] = x[i] - h;
                Mas2Delta[i] = x[i] + 2 * h;
                Menos2Delta[i] = x[i] - 2 * h;
                Mas3Delta[i] = x[i] + 3 * h;
                Menos3Delta[i] = x[i] - 3 * h;
                Mas4Delta[i] = x[i] + 4 * h;
                Menos4Delta[i] = x[i] - 4 * h;
            } else {
                MasDelta[i] = x[i];
                MenosDelta[i] = x[i];
                Mas2Delta[i] = x[i];
                Menos2Delta[i] = x[i];
                Mas3Delta[i] = x[i];
                Menos3Delta[i] = x[i];
                Mas4Delta[i] = x[i];
                Menos4Delta[i] = x[i];
            }
        }
        Salida = (-1 * this.Eval(Menos3Delta) + 12 * this.Eval(Menos2Delta) - 39 * this.Eval(MenosDelta) + 56 * this.Eval(x) - 39 * this.Eval(MasDelta) + 12 * this.Eval(Mas2Delta) - this.Eval(Mas3Delta)) / (6 * h * h * h * h);
        return Salida;
    }

    // -------- Integracion por Monte Carlos -------------//
    /**
     * Realiza el calculo de integracion multidimensional por Monte Carlo
     * utilizando el valor de delta como error esperado en al
     * integracion.
     *
     * <b>V debe de ser una dupla de la forma V["Dimension"][2]</b>
     *
     * @param V hypervolumen sobre el que se realiza la integracion
     * @return Integral por Monte Carlos multidimensional
     * @throws UtilsException
     */
    public strictfp double Integration(double V[][]) throws UtilsException {
        double Salida, Sumatoria;
        int i, j, Dimension, N;
        double Punto[];
        MersenneTwisterPlus Aleatorio;

        if (V == null) {
            throw new UtilsException("Entrada Vacia", ERROREMPTYINPUT,NAFunction.CLASSID+"012");
        } else if (V[0].length != 2) {
            throw new UtilsException("Dimensionalidad de la entrada no es una dupla", ERRORINPUTFORMAT,NAFunction.CLASSID+"013");
        } else if (V.length != this.Dimension()) {
            throw new UtilsException("Dimensionalidad de la entrada no es igual a la dimensionalidad de la funcion", ERRORINPUTFORMAT,NAFunction.CLASSID+"014");
        }
        Aleatorio = new MersenneTwisterPlus();
        Dimension = V.length;
        // Calucula el volumen
        Salida = 1;
        for (i = 0; i < Dimension; i++) {
            Salida *= (V[i][1] - V[i][0]);
        }
        // Coordenadas del punto a evaluar;
        Punto = new double[Dimension];
        // Numero de puntos a calcular
        N = (int) (1 / (h * h));
        Sumatoria = 0;
        for (i = 0; i < N; i++) {
            // Calcula el punto a utilizar
            for (j = 0; j < Dimension; j++) {
                Punto[j] = (V[j][1] - V[j][0]) * Aleatorio.nextDouble() + V[j][0];
            }
            Sumatoria += this.Eval(Punto);
        }
        //Calcula la Integral
        Salida *= Sumatoria / N;
        return Salida;
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
