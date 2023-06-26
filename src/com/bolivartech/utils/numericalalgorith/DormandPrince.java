package com.bolivartech.utils.numericalalgorith;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 * <p>Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's Dormand-Prince Class.
 *
 * Esta clase implementa el metodo de Dormant-Prince para solucionar sistemas de
 * ecuaciones diferenciales.
 *
 * La ecuacion debe estar representada de la forma y'=F(x,y) de la forma siendo
 * esta NAFunction de dimensionalidad dos, es decir de dos variables x &amp; y
 *
 * y' = f(x, y) 
 * 
 * Con una condicion inicial de: y(x0) = y0
 * 
 * Class ID: "35DGFHF"
 * Loc: 000-002
 *
 * @author Julian Bolivar
 * @since 2012 - March 25, 2016.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2012-07-20) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public strictfp class DormandPrince {

    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHF";
    
    // Funcion diferencial
    private NAFunction Fxt;
    // Time interval
    private double h;
    // Error aceptable para la resolucion de una iteracion del algoritmo
    private double IterationTargetError;
    // Valor final para el calculo de x
    private double xMax;
    // Condiciones iniciales y(x0) = y0 de la ecuacion diferencial  
    private double x0, y0;
    
    /**
     * El error generico no catalogado
     */
    public final static int ERRORGENERIC = -1;
    /**
     * El error de iteracion no pudo ser alcanzado
     */
    public final static int ERRORITERATIONTARGETERRORNOTREACHED = -2;
    /**
     * Error de memoria insuficiente
     */
    public final static int ERRORNOTENOUGHMEMORY = -3;

    /**
     * Constructor con inicializacion de la equacion diferencial a resolver
     *
     * @param Fxt
     */
    public DormandPrince(NAFunction Fxt) {
        this.Fxt = Fxt;
        this.IterationTargetError = 1e-6;
        this.x0 = 0;
        this.y0 = 0;
        this.h = 1e-6;
        this.xMax = 1 / h;
    }

    /**
     * Retorna el valor maximo que puede tener X
     *
     * @return XMax
     */
    public double getXMax() {
        return xMax;
    }

    /**
     * Establece le valor maximo que puede alcanzar X
     *
     * @param xMax es el valor maximo de X
     */
    public void setXMax(double xMax) {
        this.xMax = xMax;
    }

    /**
     * Establece los valores iniciales de la ecuacion diferencial
     *
     * @param x0
     * @param y0
     */
    public void setInitialConditions(double x0, double y0) {
        this.x0 = x0;
        this.y0 = y0;
    }

    /**
     * Retorna el incremento de intervalo de tiempo usado para la resolucion de
     * la ecuacion diferencial
     *
     * @return
     */
    public double getTimeInterval() {
        return h;
    }

    /**
     * Establece el incremento de intervalo de tiempo para la resolucion de la
     * ecuacion diferencial.
     *
     * @param h Time interval
     */
    public void setTimeInterval(double h) {
        this.h = h;
    }

    /**
     * Retorna el error esperado durante las iteraciones del algoritmo
     *
     * @return Iteration Terget Error
     */
    public double getTargetError() {
        return IterationTargetError;
    }

    /**
     * Establece el error esperado durante las iteraciones del algoritmo
     *
     * @param IterationTargetError
     */
    public void setTargetError(double IterationTargetError) {
        this.IterationTargetError = IterationTargetError;
    }

    /**
     * Calcula los parametros usando la tabla de Butcher
     *
     * @param k Coeficientes de dimension 7
     * @param x Valor de la evaluacion de 'x'
     * @param y Valor de la evaluacion en 'y'
     * @throws UtilsException
     */
    private void ButcherTableau(double k[], double x, double y) throws UtilsException {
        double t[];

        t = new double[2];
        t[0] = x;
        t[1] = y;
        k[0] = h * Fxt.Eval(t);
        t[0] = x + h / 5;
        t[1] = y + k[0] / 5;
        k[1] = h * Fxt.Eval(t);
        t[0] = x + (3 * h / 10);
        t[1] = y + (3 * k[0] / 40) + (9 * k[1] / 40);
        k[2] = h * Fxt.Eval(t);
        t[0] = x + (4 * h / 5);
        t[1] = y + (44 * k[0] / 45) - (56 * k[1] / 15) + (39 * k[2] / 2);
        k[3] = h * Fxt.Eval(t);
        t[0] = x + (8 * h / 9);
        t[1] = y + (19372 * k[0] / 6561) - (25360 * k[1] / 2187) + (64448 * k[2] / 6561) - (212 * k[3] / 729);
        k[4] = h * Fxt.Eval(t);
        t[0] = x + h;
        t[1] = y + (9017 * k[0] / 3168) - (355 * k[1] / 33) - (46732 * k[2] / 5247) + (49 * k[3] / 176) - (5103 * k[4] / 18656);
        k[5] = h * Fxt.Eval(t);
        t[0] = x + h;
        t[1] = y + (35 * k[0] / 384) + (500 * k[2] / 1113) + (125 * k[3] / 192) - (2187 * k[4] / 6784) + (11 * k[5] / 84);
        k[6] = h * Fxt.Eval(t);
    }

    /**
     * Cambia la dimension del vector de salida, si el nuevo tamaño es menor al
     * anterior trunca el nuevo vector, si es mayor lo complementa con ceros.
     * 
     * @param X   Vector de salida
     * @param length  nueva longitud
     * @return  Vector ajustado a la nueva longitud
     * @throws UtilsException 
     */
    private double[][] VectorResize(double[][] X, int length) throws UtilsException {
        int i, max;
        double[][] Y;


        Y = null;
        try {
            Y = new double[length][2];
        } catch (Exception e) {
            throw new UtilsException("Generic Error: " + e.getMessage(), DormandPrince.ERRORGENERIC,DormandPrince.CLASSID+"000");
        } catch (OutOfMemoryError e) {
            throw new UtilsException("ERROR: NOT Enough Memory, try to reduce Iteration Target Error...", DormandPrince.ERRORNOTENOUGHMEMORY,DormandPrince.CLASSID+"001");
        }
        for (i = 0; i < length; i++) {
            Y[i][0] = 0;
            Y[i][1] = 0;
        }
        if (X != null) {
            max = Math.min(X.length, length);
            for (i = 0; i < max; i++) {
                Y[i][0] = X[i][0];
                Y[i][1] = X[i][1];
            }
        }
        return Y;
    }

    /**
     * Resuelve la ecuacion diferencia y retorna la salida de la forma.
     * 
     *  Salida[x][y]
     * 
     * Conteniendo el valor x -&gt; y respectivamente.
     * 
     * @return @throws UtilsException
     */
    public double[][] Resolve() throws UtilsException {
        double k[];
        double Error;
        double x, y, s;
        //double z;
        double hnext;
        double Salida[][];
        int tamano, NumIteraciones, NewTamano;

        // Verifica si la salida X0 es igual al valor maximo de X
        //if (this.xMax == this.x0) {
        if ( Math.abs(this.xMax - this.x0) < .0000001 ){
            Salida = new double[1][2];
            Salida[0][0] = this.x0;
            Salida[0][1] = this.y0;
            return Salida;
        }
        // Inicial la resolucion de la ecuacion diferencial
        Salida = VectorResize(null, (int) (1000.0 * (Math.abs(this.xMax - this.x0)) / Math.abs(this.h)));
        k = new double[7];
        x = this.x0;
        y = this.y0;
        tamano = 0;
        NumIteraciones = 0;
        while (x <= this.xMax) {
            this.ButcherTableau(k, x, y);
            Error = Math.abs((71 * k[0] / 57600) - (71 * k[2] / 16695) + (71 * k[3] / 1920) - (17253 * k[4] / 339200) + (22 * k[5] / 525) - (1 * k[6] / 40));
            // Verifica si el error es igual o menor al error esperado por iteracion
            if (Error < this.IterationTargetError) {
                //z = y + (5179 * k[0] / 57600) + (7571 * k[2] / 16695) + (393 * k[3] / 640) - (92097 * k[4] / 339200) + (187 * k[5] / 2100) + (1 * k[6] / 40);
                y = y + (35 * k[0] / 384) + (500 * k[2] / 1113) + (125 * k[3] / 192) - (2187 * k[4] / 6784) + (11 * k[5] / 84);
                Salida[tamano][0] = x;
                Salida[tamano][1] = y;
                x += this.h;
                tamano++;
                if (tamano == Salida.length) {
                    Salida = VectorResize(Salida, 10 * tamano);
                }
                NumIteraciones = 0;
            } else {
                // Ajusta el error hasta el esperado para las iteraciones
                s = (double) (this.IterationTargetError * this.h) / (2 * Error);
                s = Math.pow(s, ((double) 1.0 / 5.0));
                hnext = s * this.h;
                this.h = hnext;
                // Verifica si se llego a alcanzar el maximo numero de intentos par alcanzar el error esperado durante una iteracion
                if (NumIteraciones > 10000 * Salida.length) {
                    throw new UtilsException("ERROR: Target Error NOT reached", DormandPrince.ERRORITERATIONTARGETERRORNOTREACHED,DormandPrince.CLASSID+"002");
                }
                NumIteraciones++;
                // Ajusta el tamaño de la salida para contener la nueva cantidad de puntos
                NewTamano = tamano + (int) Math.abs((int) ((Math.abs(this.xMax - x)) / Math.abs(this.h)));
                if (NewTamano > Salida.length) {
                    Salida = VectorResize(Salida, NewTamano);
                }
            }
        }
        Salida = VectorResize(Salida, tamano);
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
