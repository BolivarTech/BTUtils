package com.bolivartech.utils.numericalalgorith;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2011,2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's Matrix Class.
 *
 *   Esta clase implementa el manejo de Matrices en JAVA.
 *
 * Class ID: "35DGFHE"
 * Loc: 000-035
 *
 * @author Julian Bolivar
 * @since 2011 - March 25, 2016.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2011-12-11) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public strictfp class Matrix {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHE";

    // Valores de la matriz[row][column]
    private double values[][];
    /****** Seleccion de Causas de Error para las Exepciones de las matrices *******/
    /** La Matriz esta vacia  */
    public final static int ERROR_EMPTY_MATRIX = -1;
    /** El valor de la fila esta fuera de rango  */
    public final static int ERROR_ROW_OUT_RANGE = -2;
    /** El valor de la columna esta fuera de rango  */
    public final static int ERROR_COLUMN_OUT_RANGE = -3;
    /** El tama~no de las filas no concuerda en las dos matrices  */
    public final static int ERROR_ROWS_OF_MATRIXS_DONT_MATCH = -4;
    /** El tama~no de las columnas no concuerda en las dos matrices  */
    public final static int ERROR_COLUMNS_OF_MATRIXS_DONT_MATCH = -5;
    /** El parametro pasado es null  */
    public final static int ERROR_PARAMETER_NULL = -6;
    /** El tama~no de las columnas de matriz A no concuerda en las filas de la matriz B  */
    public final static int ERROR_COLUMNS_ROW_OF_MATRIXS_DONT_MATCH = -7;
    /** La Matriz no es cuadrada  */
    public final static int ERROR_NOT_SQUARE_MATRIX = -8;
    /** La Matriz es singular  */
    public final static int ERROR_SINGULAR_MATRIX = -9;

    /**
     *   Constructor por defecto, inicializa una matriz vacia
     */
    public Matrix() {
        this.values = null;
    }

    /**
     * Constructor con inicializacion de valores, segun la matriz pasada en el parametro 
     * 
     * @param NewValues  Valores a inicializar
     */
    public Matrix(double[][] NewValues) {
        int row, column, numrows, numcolumns;

        if (NewValues != null) {
            numrows = NewValues.length;
            numcolumns = NewValues[0].length;
            this.values = new double[numrows][numcolumns];
            for (row = 0; row < numrows; row++) {
                for (column = 0; column < numcolumns; column++) {
                    this.values[row][column] = NewValues[row][column];
                }
            }
        } else {
            this.values = null;
        }
    }

    /**
     * Constructor con inicializacion de una matriz de ceros con las dimensiones
     * pasadas en los parametros
     * 
     * @param NewRow       Cantidad de filas de la matriz
     * @param NewColumns   Cantidad de columnas de la matriz
     */
    public Matrix(int NewRow, int NewColumns) {
        int row, column, numrows, numcolumns;

        if ((NewRow > 0) && (NewColumns > 0)) {
            this.values = new double[NewRow][NewColumns];
            numrows = this.values.length;
            numcolumns = this.values[0].length;
            for (row = 0; row < NewRow; row++) {
                for (column = 0; column < NewColumns; column++) {
                    this.values[row][column] = 0;
                }
            }
        } else {
            this.values = null;
        }
    }

    /**
     * Constructor de copiado
     * 
     * @param Other  Matriz a copiar
     */
    public Matrix(Matrix Other) {
        int row, column, numrows, numcolumns;

        if (Other != null) {
            numrows = Other.values.length;
            numcolumns = Other.values[0].length;
            this.values = new double[numrows][numcolumns];
            for (row = 0; row < numrows; row++) {
                for (column = 0; column < numcolumns; column++) {
                    this.values[row][column] = Other.values[row][column];
                }
            }
        } else {
            this.values = null;
        }
    }

    /**
     * Retorna el numero de filas que tiene la matriz
     * 
     * @return   Numero de Filas
     */
    public int getRowsNumber() {
        int Salida;

        if (values != null) {
            Salida = values.length;
        } else {
            Salida = 0;
        }
        return Salida;
    }

    /**
     * Retorna el numero de columnas que tiene la matriz
     * 
     * @return   Numero de Columnas
     */
    public int getColumnNumber() {
        int Salida;

        if (values != null) {
            Salida = values[0].length;
        } else {
            Salida = 0;
        }
        return Salida;
    }

    /**
     *  Establece el numero de filas que tiene la matriz, si la matriz tiene informacion
     *  y la cantidad de filas es menor a la anterior se trunca, si la nueva cantidad de 
     *  filas es mayor a la anterior se completan los nuevos valores con cero.
     * 
     * @param NewRow   Nueva cantidad de filas
     * @throws UtilsException  
     */
    public void setRowsNumber(int NewRow) throws UtilsException {
        double NewValues[][];
        int row, column, numrows, numcolumns;

        if (NewRow > 0) {
            if (values != null) {
                numrows = values.length;
                numcolumns = values[0].length;
            } else {
                this.values = new double[1][1];
                this.values[0][0] = 0;
                numrows = values.length;
                numcolumns = values[0].length;
            }
            NewValues = new double[NewRow][numcolumns];
            for (row = 0; row < NewRow; row++) {
                for (column = 0; column < numcolumns; column++) {
                    if (row < numrows) {
                        NewValues[row][column] = this.values[row][column];
                    } else {
                        NewValues[row][column] = 0;
                    }
                }
            }
            this.values = NewValues;
        } else {
            throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"000");
        }
    }

    /**
     *  Establece el numero de columnas que tiene la matriz, si la matriz tiene informacion
     *  y la cantidad de columnas es menor a la anterior se trunca, si la nueva cantidad de 
     *  columnas es mayor a la anterior se completan los nuevos valores con cero.
     * 
     * @param NewColumns Nueva cantidad de columnas
     * @throws UtilsException  
     */
    public void setColumnsNumber(int NewColumns) throws UtilsException {
        double NewValues[][];
        int row, column, numrows, numcolumns;

        if (NewColumns > 0) {
            if (values != null) {
                numrows = values.length;
                numcolumns = values[0].length;
            } else {
                this.values = new double[1][1];
                this.values[0][0] = 0;
                numrows = values.length;
                numcolumns = values[0].length;
            }
            NewValues = new double[numrows][NewColumns];
            for (column = 0; column < NewColumns; column++) {
                for (row = 0; row < numrows; row++) {
                    if (column < numcolumns) {
                        NewValues[row][column] = this.values[row][column];
                    } else {
                        NewValues[row][column] = 0;
                    }
                }
            }
            this.values = NewValues;
        } else {
            throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"001");
        }
    }

    /**
     * Remueve una columna completa de la matriz
     * 
     * @param DelColumn  Columna a ser borrada
     * @throws UtilsException
     */
    public void removeColumn(int DelColumn) throws UtilsException {
        double NewValues[][];
        int row, column, Ncolumn, numrows, numcolumns;

        if ((DelColumn >= 0) && (DelColumn < this.values[0].length)) {
            numrows = this.values.length;
            numcolumns = this.values[0].length;
            NewValues = new double[numrows][numcolumns - 1];
            Ncolumn = 0;
            for (column = 0; column < numcolumns; column++) {
                if (column != DelColumn) {
                    for (row = 0; row < numrows; row++) {
                        NewValues[row][Ncolumn] = this.values[row][column];
                    }
                    Ncolumn++;
                }
            }
            this.values = NewValues;
        } else {
            throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"002");
        }
    }

    /**
     * Remueve una fila completa de la matriz
     * 
     * @param DelRow   Fila a ser removida
     * @throws UtilsException
     */
    public void removeRow(int DelRow) throws UtilsException {
        double NewValues[][];
        int row, column, NRow, numrows, numcolumns;

        if ((DelRow >= 0) && (DelRow < this.values.length)) {
            numrows = this.values.length;
            numcolumns = this.values[0].length;
            NewValues = new double[numrows - 1][numcolumns];
            NRow = 0;
            for (row = 0; row < numrows; row++) {
                if (row != DelRow) {
                    for (column = 0; column < numcolumns; column++) {
                        NewValues[NRow][column] = this.values[row][column];

                    }
                    NRow++;
                }
            }
            this.values = NewValues;
        } else {
            throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"003");
        }
    }

    /**
     * Remueve una fila y una columna especificadas por los parametros
     * 
     * @param DelRow     Fila a borrar
     * @param DelColumn  Columna a borrar
     * @throws UtilsException
     */
    public void removeRowColumn(int DelRow, int DelColumn) throws UtilsException {
        double NewValues[][];
        int row, column, NRow, NColumn, numrows, numcolumns;

        if ((DelRow >= 0) && (DelRow < this.values.length)) {
            if ((DelColumn >= 0) && (DelColumn < this.values[0].length)) {
                numrows = this.values.length;
                numcolumns = this.values[0].length;
                NewValues = new double[numrows - 1][numcolumns - 1];
                NRow = 0;
                for (row = 0; row < numrows; row++) {
                    if (row != DelRow) {
                        NColumn = 0;
                        for (column = 0; column < numcolumns; column++) {
                            if (column != DelColumn) {
                                NewValues[NRow][NColumn] = this.values[row][column];
                                NColumn++;
                            }
                        }
                        NRow++;
                    }
                }
                this.values = NewValues;
            } else {
                throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"004");
            }
        } else {
            throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"005");
        }
    }

    /**
     * Establece el tama~no de la matriz con los nuevos valores de filas y columnas
     * si alguno de los valores es menor al aterior realiza el truncado de la matriz
     * a esa nueva dimension, si el caso es que es mayor completa con ceros los nuevos
     * valores.
     * 
     * @param NewRow
     * @param NewColumns
     * @throws UtilsException  
     */
    public void setMatrixSize(int NewRow, int NewColumns) throws UtilsException {
        double NewValues[][];
        int row, column, numrows, numcolumns;

        if (NewRow > 0) {
            if (NewColumns > 0) {
                if (values != null) {
                    numrows = this.values.length;
                    numcolumns = this.values[0].length;
                } else {
                    this.values = new double[1][1];
                    this.values[0][0] = 0;
                    numrows = this.values.length;
                    numcolumns = this.values[0].length;
                }
                NewValues = new double[NewRow][NewColumns];
                for (row = 0; row < NewRow; row++) {
                    for (column = 0; column < NewColumns; column++) {
                        if ((row < numrows) && (column < numcolumns)) {
                            NewValues[row][column] = this.values[row][column];
                        } else {
                            NewValues[row][column] = 0;
                        }
                    }
                }
                this.values = NewValues;
            } else {
                throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"006");
            }
        } else {
            throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"007");
        }
    }

    /**
     * Retorna el valore contenido en la matriz, identificado con la fila "row" y columna "column" 
     * 
     * @param row     Fila del valor
     * @param column  Columna del valor
     * @return        Valor contenido
     * @throws UtilsException  si el valor de fila "row" o columna "column" no esta dentro de las dimensiones de la matriz
     */
    public double getValue(int row, int column) throws UtilsException {
        double Salida;

        Salida = 0;
        if (values != null) {
            if ((row >= 0) && (row < values.length)) {
                if ((column >= 0) && (column < values[0].length)) {
                    Salida = values[row][column];
                } else {
                    throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"008");
                }
            } else {
                throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"009");
            }
        } else {
            throw new UtilsException("ERROR: La matriz esta vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"010");
        }
        return Salida;
    }

    /**
     *  Establece el valor de la matriz identificado con  la fila "row" y columna "column" 
     * 
     * @param row         Posicion en las filas
     * @param column      Posicion en las columnas
     * @param value       Valor a establecer
     * @throws UtilsException  si el valor de fila "row" o columna "column" no esta dentro de las dimensiones de la matriz
     */
    public void setValue(int row, int column, double value) throws UtilsException {

        if ((row >= 0) && (row < values.length)) {
            if ((column >= 0) && (column < values[0].length)) {
                values[row][column] = value;
            } else {
                throw new UtilsException("ERROR: Valor de la Columna esta fuera de rango", ERROR_COLUMN_OUT_RANGE,Matrix.CLASSID+"011");
            }
        } else {
            throw new UtilsException("ERROR: Valor de la Fila esta fuera de rango", ERROR_ROW_OUT_RANGE,Matrix.CLASSID+"012");
        }

    }

    /**
     *  Retorna TRUE si la matriz esta vacia, en caso contrario FALSE
     * 
     * @return  TRUE si la mariz esta vacia y FALSE si no lo esta
     */
    public boolean isEmpty() {
        boolean Salida;

        Salida = false;
        if (this.values == null) {
            Salida = true;
        }
        return Salida;
    }

    /**
     * Realiza la suma de las dos matrices
     * 
     * @param Other  La otra matriz a sumar
     * @return       Suma de las matrices
     * @throws UtilsException
     */
    public Matrix Add(Matrix Other) throws UtilsException {
        Matrix Salida;
        int R, C, NumR, NumC;

        Salida = null;
        if (Other != null) {
            if (this.values != null) {
                if (Other.values != null) {
                    if (this.values.length == Other.values.length) {
                        if (this.values[0].length == Other.values[0].length) {
                            NumR = this.values.length;
                            NumC = this.values[0].length;
                            Salida = new Matrix(NumR, NumC);
                            for (R = 0; R < NumR; R++) {
                                for (C = 0; C < NumC; C++) {
                                    Salida.setValue(R, C, this.values[R][C] + Other.values[R][C]);
                                }
                            }
                        } else {
                            throw new UtilsException("ERROR: Dimensiones de las Columnas no son iguales", ERROR_COLUMNS_OF_MATRIXS_DONT_MATCH,Matrix.CLASSID+"013");
                        }
                    } else {
                        throw new UtilsException("ERROR: Dimensiones de las Filas no son iguales", ERROR_ROWS_OF_MATRIXS_DONT_MATCH,Matrix.CLASSID+"014");
                    }
                } else {
                    throw new UtilsException("ERROR: Matriz 'Other' es vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"015");
                }
            } else {
                throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"016");
            }
        } else {
            throw new UtilsException("ERROR: Matriz Other NULL", ERROR_PARAMETER_NULL,Matrix.CLASSID+"017");
        }
        return Salida;
    }

    /**
     * Realiza la resta de las dos matrices
     * 
     * @param Other  La otra matriz a restar
     * @return       Resta de las matrices
     * @throws UtilsException
     */
    public Matrix Subtrac(Matrix Other) throws UtilsException {
        Matrix Salida;
        int R, C, NumR, NumC;

        Salida = null;
        if (Other != null) {
            if (this.values != null) {
                if (Other.values != null) {
                    if (this.values.length == Other.values.length) {
                        if (this.values[0].length == Other.values[0].length) {
                            NumR = this.values.length;
                            NumC = this.values[0].length;
                            Salida = new Matrix(NumR, NumC);
                            for (R = 0; R < NumR; R++) {
                                for (C = 0; C < NumC; C++) {
                                    Salida.setValue(R, C, this.values[R][C] - Other.values[R][C]);
                                }
                            }
                        } else {
                            throw new UtilsException("ERROR: Dimensiones de las Columnas no son iguales", ERROR_COLUMNS_OF_MATRIXS_DONT_MATCH,Matrix.CLASSID+"018");
                        }
                    } else {
                        throw new UtilsException("ERROR: Dimensiones de las Filas no son iguales", ERROR_ROWS_OF_MATRIXS_DONT_MATCH,Matrix.CLASSID+"019");
                    }
                } else {
                    throw new UtilsException("ERROR: Matriz 'Other' es vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"020");
                }
            } else {
                throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"021");
            }
        } else {
            throw new UtilsException("ERROR: Matriz Other NULL", ERROR_PARAMETER_NULL,Matrix.CLASSID+"022");
        }
        return Salida;
    }

    /**
     *  Realiza la multiplicacion de la matriz por el valor escalar especificado
     * 
     * @param Scal    valor escalar a multiplicar
     * @return  Matriz multiplicada por el escalar
     * @throws UtilsException
     */
    public Matrix PScalar(double Scal) throws UtilsException {
        Matrix Salida;
        int R, C, NumR, NumC;

        Salida = null;
        if (this.values != null) {
            NumR = this.values.length;
            NumC = this.values[0].length;
            Salida = new Matrix(NumR, NumC);
            for (R = 0; R < NumR; R++) {
                for (C = 0; C < NumC; C++) {
                    Salida.setValue(R, C, Scal * this.values[R][C]);
                }
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"023");
        }
        return Salida;
    }

    /**
     * Realiza la multiplicacion de las dos matrices, en donde deben coincidir
     * el numero de columnas de la primera con el numero de filas de la segunda
     * 
     * @param   Other  La Matrix B que se va a multiplicar
     * @return  Producto Matricial de ambas matrices
     * @throws UtilsException
     */
    public Matrix Multi(Matrix Other) throws UtilsException {
        Matrix Salida;
        int R, C, NumRA, NumCA, NumRB, NumCB, P;

        Salida = null;
        if (Other != null) {
            if (this.values != null) {
                if (Other.values != null) {
                    if (this.values[0].length == Other.values.length) {
                        NumRA = this.values.length;
                        NumCA = this.values[0].length;
                        NumRB = Other.values.length;
                        NumCB = Other.values[0].length;
                        Salida = new Matrix(NumRA, NumCB);
                        for (R = 0; R < NumRA; R++) {
                            for (C = 0; C < NumCB; C++) {
                                for (P = 0; P < NumCA; P++) {
                                    Salida.setValue(R, C, Salida.getValue(R, C) + (this.values[R][P] * Other.values[P][C]));
                                }
                            }
                        }
                    } else {
                        throw new UtilsException("ERROR: Dimensiones de las Columnas de A no son iguales a las Filas de B", ERROR_COLUMNS_ROW_OF_MATRIXS_DONT_MATCH,Matrix.CLASSID+"024");
                    }
                } else {
                    throw new UtilsException("ERROR: Matriz 'Other' es vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"025");
                }
            } else {
                throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"026");
            }
        } else {
            throw new UtilsException("ERROR: Matriz Other NULL", ERROR_PARAMETER_NULL,Matrix.CLASSID+"027");
        }
        return Salida;
    }

    /**
     * Calcula el determinante de la matriz cuadrada
     * 
     * @return  Determinante
     * @throws UtilsException  
     */
    public double Det() throws UtilsException {
        double Salida;
        Matrix MT;
        int C, NumColumns;

        Salida = 0;
        if (this.values != null) {
            if (this.values.length == this.values[0].length) {
                NumColumns = this.values.length;
                if (NumColumns == 2) {
                    Salida = this.values[0][0] * this.values[1][1] - this.values[0][1] * this.values[1][0];
                } else if (NumColumns == 3) {
                    Salida = (this.values[0][0] * this.values[1][1] * this.values[2][2])
                            + (this.values[0][1] * this.values[1][2] * this.values[2][0])
                            + (this.values[0][2] * this.values[1][0] * this.values[2][1])
                            - (this.values[0][2] * this.values[1][1] * this.values[2][0])
                            - (this.values[0][1] * this.values[1][0] * this.values[2][2])
                            - (this.values[0][0] * this.values[1][2] * this.values[2][1]);
                } else {
                    Salida = 0;
                    for (C = 0; C < NumColumns; C++) {
                        MT = new Matrix(this);
                        MT.removeRowColumn(0, C);
                        Salida = Salida + Math.pow(-1, C) * this.values[0][C] * MT.Det();
                    }
                }
            } else {
                throw new UtilsException("ERROR: Matriz no es cuadrada", ERROR_NOT_SQUARE_MATRIX,Matrix.CLASSID+"028");
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"029");
        }
        return Salida;
    }

    /**
     * Calcula la adjunta de la matriz
     * 
     * @return  Matriz Adjunta
     * @throws UtilsException
     */
    public Matrix Adjn() throws UtilsException {
        Matrix Salida;
        Matrix MT;
        int R, C, NumRowsColumns;

        Salida = null;
        if (this.values != null) {
            if (this.values.length == this.values[0].length) {
                NumRowsColumns = this.values.length;
                Salida = new Matrix(NumRowsColumns, NumRowsColumns);
                if (NumRowsColumns == 2) {
                    Salida.setValue(0, 0, this.values[1][1]);
                    Salida.setValue(0, 1, (-1 * this.values[1][0]));
                    Salida.setValue(1, 0, (-1 * this.values[0][1]));
                    Salida.setValue(1, 1, this.values[0][0]);
                } else {
                    for (R = 0; R < NumRowsColumns; R++) {
                        for (C = 0; C < NumRowsColumns; C++) {
                            MT = new Matrix(this);
                            MT.removeRowColumn(R, C);
                            Salida.setValue(R, C, Math.pow(-1, R + C) * MT.Det());
                        }
                    }
                }
            } else {
                throw new UtilsException("ERROR: Matriz no es cuadrada", ERROR_NOT_SQUARE_MATRIX,Matrix.CLASSID+"030");
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"031");
        }
        return Salida.Trans();
    }

    /**
     * Retorna la transpuesta de la matriz
     * 
     * @return   Transpuesta de la matriz
     * @throws UtilsException
     */
    public Matrix Trans() throws UtilsException {
        Matrix Salida;
        int R, C, NumR, NumC;

        Salida = null;
        if (this.values != null) {
            NumR = this.values.length;
            NumC = this.values[0].length;
            Salida = new Matrix(NumC, NumR);
            for (R = 0; R < NumR; R++) {
                for (C = 0; C < NumC; C++) {
                    Salida.setValue(C, R, this.values[R][C]);
                }
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"032");
        }
        return Salida;
    }

    /**
     * Calcuala la inversa de la matriz, si esta es invertible
     * 
     * @return   Matriz inversa
     * @throws UtilsException
     */
    public Matrix Inv() throws UtilsException {
        Matrix Salida;
        double Determinante;

        Salida = null;
        if (this.values != null) {
            Determinante = this.Det();
            if (Determinante != 0) {
                Salida = this.Adjn();
                Salida = Salida.PScalar(1/Determinante);
            } else {
                throw new UtilsException("ERROR: Matriz Singular", ERROR_SINGULAR_MATRIX,Matrix.CLASSID+"033");
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"034");
        }
        return Salida;

    }
    
    /**
     * Inicializa la matriz como una matriz identidad, si la matriz no es cuadrada
     * la convierte en una cuadrada con el valor mayor entre las filas y las columnas
     * 
     * @throws UtilsException
     */
    public void InitIdentidad() throws UtilsException {
         int row, column, numrows, numcolumns;

         if(this.values!=null){
            numrows = this.values.length;
            numcolumns = this.values[0].length;
            if(numrows!=numcolumns){
                 row = Math.max(numrows,numcolumns);
                 numrows= row;
                 numcolumns= row;
                 this.values = new double[numrows][numcolumns];
            }
            for (row = 0; row < numrows; row++) {
                for (column = 0; column < numcolumns; column++) {
                    if(row==column){
                        this.values[row][column] = 1;
                    } else {
                    this.values[row][column] = 0;
                    }
                }
            }
        } else {
            throw new UtilsException("ERROR: Matriz vacia", ERROR_EMPTY_MATRIX,Matrix.CLASSID+"035");
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
