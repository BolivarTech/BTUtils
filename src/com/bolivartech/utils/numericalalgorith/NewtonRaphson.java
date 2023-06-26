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
 * This Class is the BolivarTech's NewtonRaphson Class.
 *
 * Esta clase implementa el metodo de NewtonRapson para solucionar sistemas de
 * ecuaciones no lineales.
 *
 * La ecuaciones deben estar representadas de la forma de un campo vectoria F(x)
 * de la forma 
 *
 *        |-     -|   |- -| 
 *        | f1(x) |   | 0 | 
 *        | f2(x) |   | 0 | 
 * F(x) = | f3(x) | = | 0 | 
 *        |  ...  |   | . | 
 *        | fn(x) |   | 0 |
 *        |-     -|   |- -|
 *
 *  
 * De esta forma se resuelven las raices que satisfacen el sistema de
 * ecuaciones.
 * 
 * Class ID: "35DGFHH"
 * Loc: 000
 *
 * @author Julian Bolivar
 * @since 2012 @date March 25, 2016.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2012-07-20) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public strictfp class NewtonRaphson {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHH";

    // Sistema de ecuaciones a resolver con el metodo de NewtonRaphson
    private NAFunction Fx[];
    // Error aceptable para la resolucion del sistema de ecuaciones
    private double TargetError;
    // Inicializado el punto de inicio para la resolucion del sistema de ecuaciones
    private boolean InitialPoint;
    // Solucion del sistema de ecuaciones;
    private double x[];
    // Mejor Solucion encontrada durante las iteraciones
    private double BestX[];
    // Maximo numero de iteraciones del algoritmo
    private long MaxNumIterations;
    // Error Minimo encontrado durante las iteraciones
    private double MinError;
    // Autmenta el error para el caso de diferencias pequeñas
    private boolean ErrorMagnifier;
    // Cantidad de Magnificacion del error, por defecto 3
    private double Magnifier;
    
    /**
     * La dimension de la funcion no es concordante
     */
    public final static int ERRORFUNTIONDIMENSIONNOTMATCH = -1;

    /**
     * Constructor de inicializacion del algoritmo, la dimension de las
     * funciones debe de ser igual para todas, es decir deben de contener el
     * mismo numero de variables.
     *
     * @param Fx Vector conteniendo el sistema de ecuaciones en objetos del tipo
     * NAFunction
     */
    public NewtonRaphson(NAFunction Fx[]) {
        this.Fx = Fx;
        this.TargetError = 1e-6;
        this.InitialPoint = false;
        this.x = null;
        this.MinError = 1e20;
        this.MaxNumIterations = 100000000;
        this.ErrorMagnifier = false;
        this.Magnifier=3;
    }

    /**
     * Retorna el MAXIMO numero de iteraciones que puede realizar el algoritmo
     * si no encuentra una solucion al sistema de ecuaciones.
     *
     * @return Maximo numero de iteraciones si no encuentra una solicion
     */
    public long getMaxNumIterations() {
        return MaxNumIterations;
    }

    /**
     * Establece el MAXIMO numero de iteraciones que realizara el algoritmo para
     * tratar de encontra una solucion al sistema de ecuaciones.
     *
     * @param MaxNumIterations
     */
    public void setMaxNumIterations(long MaxNumIterations) {
        this.MaxNumIterations = MaxNumIterations;
    }

    /**
     * Retorna el Error minimo encontrado durante al busqueda de la solucion del
     * sistema de ecuaciones.
     *
     * @return Error minimo encontrado
     */
    public double getMinError() {
        return MinError;
    }

    /**
     * Retorna el Error permitido en la resolucion del sistema de ecuaciones
     *
     * @return Error permitido
     */
    public double getError() {
        return TargetError;
    }

    /**
     * Establece el Error permitido en la resolucion del sistema de ecuaciones
     *
     * @param TargetError Error permitido
     */
    public void setError(double TargetError) {
        this.TargetError = TargetError;
    }

    /**
     * Retorna el TRUE si la funcion de Magnificacion de error esta habilitada y FALSE
     * si no.
     * 
     * @return  TRUE si la funcion de magnificacion esta habilida y FALSE si no
     */
    public boolean isErrorMagnifier() {
        return ErrorMagnifier;
    }

    /**
     * Establece la funcion de Magnificacion de Error.
     * 
     * Esta funcion permite el diferenciar entre errores muy pequeños de la solucion,
     * evitando que el algoritmo termine prematuranente en el caso de soluciones muy
     * proximas entre ellas.
     * 
     * @param ErrorMagnifier  TRUE habilita la funcion y FALSE la desabilita.
     */
    public void setErrorMagnifier(boolean ErrorMagnifier) {
        this.ErrorMagnifier = ErrorMagnifier;
    }

    /**
     * Retorna el nivel de magnificacion de error actual en el algoritmo.
     * 
     * @return  Nivel de magnificacion del error actual
     */
    public int getMagnifier() {
        return (int)Magnifier;
    }

    /**
     * Establece el nivel de magnificacion del error en el algoritmo, este valor
     * es usado para magnificar el error y poder discriminar entre soluciones
     * muy cercanas entre ellas
     *
     * El valor por defecto de magnificacion es 3; se debe tener cuidado con
     * este parametros porque si se incrementa mucho podria producir errores
     * matematicos por redondeo en las soluciones y/o comportamientos no
     * determinados en el algoritmo.
     * 
     * El valor del Magnifier debe ser mayor o igual a 1, si se especifica un valor menor
     * a 1 se establece 1 como valor por defecto. 
     *
     * @param Magnifier Grado de magnificacion del error.
     */
    public void setMagnifier(int Magnifier) {
        if (Magnifier > 0) {
            this.Magnifier = (double) Magnifier;
        } else {
            this.Magnifier = 1;
        }
    }
 
    /*
     * Calcula el error del sistema de ecuaciones en un punto determinado
     */
    private double Error(double Punto[]) throws UtilsException {
        double NewError, ParcialError;
        int i, NumEcuaciones;

        // Calcula el error inicial
        NumEcuaciones = Fx.length;
        NewError = 0;
        for (i = 0; i < NumEcuaciones; i++) {
            ParcialError = Math.abs(Fx[i].Eval(Punto));
            if (this.ErrorMagnifier) {
                // Multiplicador de Error, para manejar diferencias de errores pequeños
                if (ParcialError >= 1) {
                    ParcialError = Math.pow(ParcialError, this.Magnifier);
                } else {
                    ParcialError = Math.pow(ParcialError, ((double) 1.0) / this.Magnifier);
                }
            }
            NewError += ParcialError;
            //System.out.println(String.valueOf(NewError));
        }
        return NewError;
    }

    /**
     * Establece una estimacion inicial de la solicion del sistema de ecuaciones
     *
     * @param InicialEstimation Estimacion inicial de la solucion
     * @throws UtilsException
     */
    public void setInicialEstimation(double InicialEstimation[]) throws UtilsException {
        int Dimension, i;

        Dimension = Fx[0].Dimension();
        if (Dimension == InicialEstimation.length) {
            x = new double[Dimension];
            for (i = 0; i < Dimension; i++) {
                x[i] = InicialEstimation[i];
            }
            InitialPoint = true;
        } else {
            throw new UtilsException("ERROR: Initial Estimation don't match equation's dimension", NewtonRaphson.ERRORFUNTIONDIMENSIONNOTMATCH,NewtonRaphson.CLASSID+"000");
        }
    }

    /**
     * Resuelve el sistema de ecuaciones por medio del metodo Newton-Raphson
     *
     * Utiliza la estimacion inicial proporcionada al algoritmo y si
     *
     * @return Raiz resultado del sistema de ecuaciones
     * @throws UtilsException
     */
    public double[] Resolve() throws UtilsException {
        MersenneTwisterPlus Aleatorio;
        Matrix Jacobiana, Funcion, Delta;
        double Error;
        int i, j, Dimension, NumFuncions;
        long Iteraciones;

        Dimension = Fx[0].Dimension();
        //Inicializa el generador de numero aleatorios
        Aleatorio = new MersenneTwisterPlus();
        if (!InitialPoint) {
            // Establece un valor aleatorio para la estimacion inicial de la solucion 
            x = new double[Dimension];
            for (i = 0; i < Dimension; i++) {
                x[i] = Aleatorio.nextDouble() * Aleatorio.nextByte();
            }
        }
        // Calcula el error inicial
        Error = Math.abs(this.Error(x));
        // Inicializa la Mejor Solucion encontrada hasta el momento
        BestX = new double[Dimension];
        for (i = 0; i < Dimension; i++) {
            BestX[i] = x[i];
        }
        this.MinError = Error;
        // Inicia la itaracion del metodo de NewtonRaphson
        Iteraciones = 0;
        Jacobiana = new Matrix(Dimension, Dimension);
        Funcion = new Matrix(Dimension, 1);
        NumFuncions = Fx.length;
        while (Error > TargetError) {
            // Llena la matrix Jacobiana y el vector de valores
            // para calcular:
            //                 dx=-([J(x)]^-1)*F(x)
            //
            for (i = 0; i < NumFuncions; i++) {
                for (j = 0; j < Dimension; j++) {
                    Jacobiana.setValue(i, j, Fx[i].DPEval(x, j));
                }
                Funcion.setValue(i, 0, Fx[i].Eval(x));
            }
            Jacobiana = Jacobiana.Inv();
            Jacobiana = Jacobiana.PScalar(-1);
            Delta = Jacobiana.Multi(Funcion);
            for (i = 0; i < Dimension; i++) {
                x[i] += Delta.getValue(i, 0);
            }
            // Calcula en nuevo error
            Error = Math.abs(this.Error(x));
            // Verifca si el nuevo error es mejor que la mejor solucion encontrada
            if (Error < this.MinError) {
                this.MinError = Error;
                for (i = 0; i < Dimension; i++) {
                    BestX[i] = x[i];
                }
            }
            // Si se alcanzo el maximo numero de iteraciones finaliza el algoritmo
            if (Iteraciones > this.MaxNumIterations) {
                Error = TargetError;
            }
            Iteraciones++;
        }
        // Retorna la mejor solucion encontrada
        return BestX;
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
