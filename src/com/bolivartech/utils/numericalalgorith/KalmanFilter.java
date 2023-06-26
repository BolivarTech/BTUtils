package com.bolivartech.utils.numericalalgorith;

import com.bolivartech.utils.exception.UtilsException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2014,2015,2016 BolivarTech C.A.
 *
 * Implementa el filtro de Kalman
 *
 * Ecuaciones Canonicas de Kalman:
 * 
 *
 * Xn = A*Xn-1 + B*Un + Wt
 * Zn = H*Xn + Vt
 *
 * 
 * Wt: Vector que contiene el ruido del sistema
 * Vt: Vector que contiene el ruido de la Medicion
 *
 * 
 * -----------------------------------------
 * Ecuaciones computacionales
 *
 * Xp = A*Xn-1 + B*Un 
 * Pp = A*Pn-1*At+Q 
 * Y = Zn - H*Xp 
 * S = H*Pp*Ht + R 
 * K = Pp*Ht*S^-1 
 * Xn = Xp + K*Y 
 * Pn = (I-K*H)*Pp 
 *
 * 
 * ------------------------------------------
 * Entradas:
 * Un: Vector de Control 
 * Zn: Vector de Mediciones 
 * 
 *
 * Salidas:
 * Xn: Vector de Estados Estimados
 * Pn: Error estimado de la Salida
 * 
 *
 * Constantes:
 * A: Matriz de transicion del estado anterior al siguiente
 * B: Matriz de control del sistema
 * H: Matrix de Observacion que mapea el estado del sistema a mediciones
 * Q: Covarianza del Error estimado del proceso
 * R: Covarianza del Error estimado de la medicion
 * I: Matriz identidad
 * 
 *
 * Variables Intermedias:
 * Xp: Prediccion de Estado
 * Pp: Prediccion de la Covarianza
 * Y: Innovacion entre la prediccion y el estado del sistema
 * S: Covarianza de la innovacion
 * K: Ganancia de Kalman
 * 
 * Class ID: "35DGFHI"
 * Loc: 000
 * 
 * @author Julian Bolivar
 * @since 2014 - March 25, 2016.
 * @version 1.0.1
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.0.0 (2014-03-23) Implementacion del filtro de Kalman.</li>
 * <li>v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.</li>
 * </ul>
 */
public strictfp class KalmanFilter {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHI";

    /**
     * **** Seleccion de Causas de Error para las Exepciones de las matrices
     * ******
     */
    /**
     * La Matriz esta vacia
     */
    public final static int ERROR_EMPTY_MATRIX = -1;
    /**
     * El valor de la fila esta fuera de rango
     */
    public final static int ERROR_ROW_OUT_RANGE = -2;
    /**
     * El valor de la columna esta fuera de rango
     */
    public final static int ERROR_COLUMN_OUT_RANGE = -3;
    /**
     * El tama~no de las filas no concuerda en las dos matrices
     */
    public final static int ERROR_ROWS_OF_MATRIXS_DONT_MATCH = -4;
    /**
     * El tama~no de las columnas no concuerda en las dos matrices
     */
    public final static int ERROR_COLUMNS_OF_MATRIXS_DONT_MATCH = -5;
    /**
     * El parametro pasado es null
     */
    public final static int ERROR_PARAMETER_NULL = -6;
    /**
     * El tamaño de las columnas de matriz A no concuerda en las filas de la
     * matriz B
     */
    public final static int ERROR_COLUMNS_ROW_OF_MATRIXS_DONT_MATCH = -7;
    /**
     * La Matriz no es cuadrada
     */
    public final static int ERROR_NOT_SQUARE_MATRIX = -8;
    /**
     * La Matriz es singular
     */
    public final static int ERROR_SINGULAR_MATRIX = -9;

    // Constantes del Algoritmo de Kalman
    Matrix A, B, H, Q, R, I;
    // Entradas
    //Matrix Un, Zn;
    // Salidas
    Matrix Xn, Pn;

    /**
     * Constructor por defecto
     */
    public KalmanFilter() {
        this.A = null;
        this.B = null;
        this.H = null;
        this.Q = null;
        this.R = null;
        this.I = null;
        //this.Un = null;
        //this.Zn = null;
        this.Xn = null;
        this.Pn = null;
    }

    /**
     * Constructor con inicializacion completa.
     *
     * @param A Matriz de transicion del estado anterior al siguiente
     * @param B Matriz de control del sistema
     * @param H Matrix de Observacion que mapea el estado del sistema a
     * mediciones
     * @param Q Covarianza del Error estimado del proceso
     * @param R Covarianza del Error estimado de la medicion
     */
    public KalmanFilter(Matrix A, Matrix B, Matrix H, Matrix Q, Matrix R) {
        this.A = new Matrix(A);
        this.B = new Matrix(B);
        this.H = new Matrix(H);
        this.Q = new Matrix(Q);
        this.R = new Matrix(R);
        this.I = new Matrix(H.getRowsNumber(), H.getColumnNumber());
        try {
            this.I.InitIdentidad();
        } catch (UtilsException ex) {
            Logger.getLogger(KalmanFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.Pn = new Matrix(I);
       // this.Un = null;
       // this.Zn = null;
        this.Xn = null;
        
    }

    /**
     * Retorna la matriz A de transicion del estado anterior al siguiente.
     *
     * @return Matrix A
     */
    public Matrix getA() {
        return new Matrix(this.A);
    }

    /**
     * Establece la matriz A de transicion del estado anterior al siguiente.
     *
     * @param A Matriz de transicion del estado anterior al siguiente
     */
    public void setA(Matrix A) {
        this.A = new Matrix(A);
    }

    /**
     * Retorna la matriz B de control del sistema
     *
     * @return Matriz B
     */
    public Matrix getB() {
        return new Matrix(this.B);
    }

    /**
     * Establece la matriz B de control del sistema.
     *
     * @param B Matriz de control del sistema
     */
    public void setB(Matrix B) {
        this.B = new Matrix(B);
    }

    /**
     * Retorna la matriz H de Observacion que mapea el estado del sistema a
     * mediciones
     *
     * @return Matriz H
     */
    public Matrix getH() {
        return new Matrix(this.H);
    }

    /**
     * Establece la matriz H de Observacion que mapea el estado del sistema a
     * mediciones
     *
     * @param H Matriz de Observacion que mapea el estado del sistema a
     * mediciones
     */
    public void setH(Matrix H) {
        this.H = new Matrix(H);
    }

    /**
     * Retorna la matriz Q de covarianza del Error estimado del proceso
     *
     * @return Matriz Q de covarianza del Error estimado del proceso
     */
    public Matrix getQ() {
        return new Matrix(this.Q);
    }

    /**
     * Establece la matriz Q de covarianza del Error estimado del proceso
     *
     * @param Q Matrix de covarianza del Error estimado del proceso
     */
    public void setQ(Matrix Q) {
        this.Q = new Matrix(Q);
    }

    /**
     * Retorna la matriz R de Covarianza del Error estimado de la medicion.
     *
     * @return Matriz de covarianza del Error estimado de la medicion
     */
    public Matrix getR() {
        return new Matrix(this.R);
    }

    /**
     * Establece la matriz R de Covarianza del Error estimado de la medicion
     *
     * @param R Matriz de covarianza del Error estimado de la medicion
     */
    public void setR(Matrix R) {
        this.R = new Matrix(R);
    }

    /**
     * Realiza la estimacion del estado basado en las entradas Zn y Un, retornando
     * el vector de estados estimados y en Pn el Error estimado de las Salidas del estado.
     * 
     * @param Zn Vector de Mediciones.
     * @param Un Vector de Control.
     * @return Vector de Estados Estimados Xn
     * @throws UtilsException 
     */
    public Matrix Estimate(Matrix Zn, Matrix Un) throws UtilsException {
        Matrix Xp;
        Matrix Pp;
        Matrix Y;
        Matrix S;
        Matrix K;
        Matrix Temp;

        if (I == null) {
            if (H != null) {
                this.I = new Matrix(H.getRowsNumber(), H.getColumnNumber());
                this.I.InitIdentidad();
            } else {
                throw new UtilsException("ERROR: La Matriz H es nula", ERROR_PARAMETER_NULL,KalmanFilter.CLASSID+"000");
            }
        }
        // Calcula la prediccion del estado
        Xp = this.A.Multi(this.Xn);
        Temp = this.B.Multi(Un);
        Xp = Xp.Add(Temp);
        // Calcula la prediccion de la Covarianza
        Pp = this.A.Multi(this.Pn);
        Pp = Pp.Multi(this.A.Trans());
        Pp = Pp.Add(this.Q);
        // Innovacion en base a la medicion
        Y = Zn.Subtrac(H.Multi(Xp));
        // Covarianza de la Innovacion
        S = this.H.Multi(Pp);
        S = S.Multi(this.H.Trans());
        S = S.Add(this.R);
        // Ganancia de Kalman
        K = Pp.Multi(this.H.Trans());
        K = K.Multi(S.Inv());
        // Calcula el Vector del estados estimados
        Temp = K.Multi(Y);
        this.Xn = Xp.Add(Temp);
        // Calcula el Error estimado de los estimados
        Temp = this.I.Subtrac(K.Multi(this.H));
        this.Pn = Temp.Multi(Pp);
        return new Matrix(this.Xn);
    }

    /**
     * Retorna el vector Pn de Errores estimados de la Salida
     * 
     * @return vector Pn de Errores estimados de la Salida
     */
    public Matrix getPn() {
        
        if(this.Pn!=null){
           return new Matrix(this.Pn);
        } else {
            return null;
        }
    }

    /**
     * Establece el vector Pn de Errores estimados de la Salida.
     * 
     * NOTA: Se usa para establecer los valores iniciales de los errores de la prediccion.
     * 
     * @param Pn 
     */
    public void setPn(Matrix Pn){
        
        this.Pn = new Matrix(Pn);
    }

    /**
     * Retorna el Vector de Estados Estimados Xn
     * 
     * @return Vector de Estados Estimados Xn
     */
    public Matrix getXn() {
        
        if(this.Xn!=null){
           return new Matrix(this.Xn);
        } else {
            return null;
        }
    }

    /**
     * Establece el Vector de Estados Estimados Xn
     * 
     * NOTA: Se usa para establecer los valores iniciales de la prediccion
     * 
     * @param Xn 
     */
    public void setXn(Matrix Xn) {
        
        this.Xn = new Matrix(Xn);
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
