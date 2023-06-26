package com.bolivartech.utils.fec;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's abstract Class for FEC coding and decoding over a byte array.
 *
 *   Define la clase abstracta para realiza la codificacion y decodificacion FEC de una cadena de bytes
 *
 * @author Julian Bolivar
 * @version 2.0.0
 */
public strictfp abstract class AcFEC implements FEC {
    
    /*
     * Multiplicador de la longitud para los datos de entrada en el algoritmo FEC
     */
    private double CodeLengtMultiplier;

    /**
     * Retorna la cantidad de bytes de redundancia que generaria el algoritmo durante la codificacion
     * 
     * @param InputLength longitud del paquete original
     * @return Bytes de redundancia a ser añadidos al paquete original
     */
    @Override
    public long getCodeRedundancyLength(long InputLength){
        long Salida;
        
        Salida = ((long) Math.ceil(((double)InputLength)*this.CodeLengtMultiplier))-InputLength;
        return Salida;
    }
    
    /**
     * Retorna la cantidad de bytes de redundancia que contiene el paquete recibido 
     * 
     * @param InputLength Longitud del paquete codificado
     * @return Bytes de redundancia que contiene el paquete
     */
    @Override
    public long getDeCodeRedundancyLength(long InputLength){
        long Salida;
        
        Salida = InputLength-((long) Math.floor(((double)InputLength)/this.CodeLengtMultiplier));
        return Salida;
    }

    /**
     * Constructor con la inicializacion de la taza de redundancia en el paquete
     * para la correccion de errores
     * 
     * @param CodeLengtMultiplier Taza de redundancia del codigo FEC
     */
    public AcFEC(double CodeLengtMultiplier) {
        this.CodeLengtMultiplier = CodeLengtMultiplier;
    }
    
    /**
     * Metodo abstracto para el Codificador FEC
     * 
     * @param Input Cadena de entrada de datos
     * @return Cadena de entrada con codigo de redundancia
     * @throws UtilsException
     */
    @Override
    public abstract byte[] encode(byte[] Input) throws UtilsException;

    /**
     * Metodo abstracto para el Decodificador FEC
     * 
     * @param Input Cadena de entrada con codigo de redundancia
     * @return Cadena de entrada con los errores corregidos por el algoritmo
     * @throws UtilsException Manejador de errores en el algoritmo FEC 
     */
     @Override
    public abstract byte[] decode(byte[] Input) throws UtilsException;
}
