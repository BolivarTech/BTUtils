package com.bolivartech.utils.statistics;

import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.files.FileManager;
import java.nio.charset.Charset;

/**
 * Copyright 2014,2015,2016 BolivarTech C.A.
 *
 * Implementacion de un algoritmo generador de una distribucion en un intervalor
 * especifico.
 *
 * Class ID: "35DGFHM"
 * Loc: 000-001
 * 
 * @author Julian Bolivar
 * @since 2014 - March 25, 2016.
 * @version 1.0.2
 * 
 * Change Log:
 * v1.0.1 (2014-12-26): SaveToFile was adapted to use BolivarTech's FileManger class
 * v1.0.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public class FastDistributionCalc {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHM";

    private double X[];
    private long Distribution[];
    private double Jump;

    /**
     * El valor que se esta tratando de agregar esta fuera del rango de la
     * distribucion.
     */
    public final static int ERRORVALUEOUTOFDISTRIBUTION = -1;

    /**
     * El Valor NO fue encontrado en la distribucion
     */
    public final static int ERRORVALUENOTINDISTRIBUTION = -2;
    
    /**
     * Error al crear el archivo de salida para guardar la distribucion
     */
    public final static int ERRORCREATEFILE = -3;

    /**
     * Constructor con inicialiacion de la distribucion a generar
     *
     * @param Min Valor inferior del rango de la distribucion
     * @param Max Valor superior del rango de la distribucion
     * @param NumSegmens Numero de Segmentos en los que se divide el rango de la
     * distribucion
     */
    public FastDistributionCalc(double Min, double Max, int NumSegmens) {
        int i;

        Jump = (Max - Min) / NumSegmens;
        this.X = new double[NumSegmens + 1];
        this.Distribution = new long[NumSegmens + 1];
        // Rellena el Eje X de la distribucion y lleva los valores en 0
        this.X[0] = Min;
        this.Distribution[0] = 0;
        for (i = 1; i < NumSegmens + 1; i++) {
            this.X[i] = this.X[i - 1] + Jump;
            this.Distribution[i] = 0;
        }
    }

    public void AddToDistribution(double Value) throws UtilsException {
        boolean Encontrado;
        int Pibote, Low, Hight;

        if ((Value >= this.X[0]) && (Value <= this.X[this.X.length - 1])) {
            Low = 0;
            Hight = this.X.length;
            Encontrado = false;
            while (Low < Hight) {
                Pibote = ((Hight - Low) / 2) + Low;
                if ((Value >= this.X[Pibote]) && (Value < (this.X[Pibote] + Jump))) {
                    this.Distribution[Pibote]++;
                    Encontrado = true;
                    Low = Hight;
                } else if (Value < this.X[Pibote]) {
                    Hight = Pibote;
                } else {
                    Low = Pibote;
                }
            }
            if (!Encontrado) {
                throw new UtilsException("ERROR: " + Double.toString(Value) + " NOT found in the Distribution Range", FastDistributionCalc.ERRORVALUENOTINDISTRIBUTION,FastDistributionCalc.CLASSID+"000");
            }
        } else {
            throw new UtilsException("WARNING: " + Double.toString(Value) + " Out of Distribution Range", FastDistributionCalc.ERRORVALUEOUTOFDISTRIBUTION,FastDistributionCalc.CLASSID+"001");
        }
    }

    /**
     * Salva en Archivo la distribucion generada
     *
     * @param Archivo
     */
    	public void SaveToFile(FileManager Archivo) throws UtilsException {
		int i;
		String Salida;
		long Max;
		Charset charset;

		charset = Charset.forName("UTF-16");
		Archivo.Open(FileManager.WRITE,false);
		// Busca el Maximo
		Max = 0;
		for (i = 0; i < this.X.length; i++) {
			if (this.Distribution[i] > Max) {
				Max = this.Distribution[i];
			}
		}
		// Escribe en el archivo el valor Normalizado
		for (i = 0; i < this.X.length; i++) {
			Salida = String.valueOf(this.X[i])
					+ ","
					+ String.valueOf((double) ((double) this.Distribution[i])
							/ Max);
			Salida += "\n";
			Archivo.Write(Salida.getBytes(charset));

		}
		Archivo.Close();
	}


    /**
     * Retorna la distribucion generada de forma normalizada.
     *
     * @return Distribucion Normalizada
     */
    public double[][] GetDistribution() {
        double[][] Salida;
        long Max;
        int i;

        Salida = new double[this.X.length][2];
        // Busca el Maximo
        Max = 0;
        for (i = 0; i < this.X.length; i++) {
            if (this.Distribution[i] > Max) {
                Max = this.Distribution[i];
            }
        }
        // Genera la distribucion Normalizada
        for (i = 0; i < this.X.length; i++) {
            Salida[i][0] = this.X[i];
            Salida[i][1] = ((double) ((double) this.Distribution[i]) / Max);
        }
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
