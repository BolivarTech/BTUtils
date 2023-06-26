package com.bolivartech.utils.fec;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's interface for FEC coding and decoding over a byte array.
 *
 *   Define la Interface para realiza la codificacion y decodificacion FEC de una cadena de bytes
 *
 * @author Julian Bolivar
 * @version 2.0.1
 * 
 * Change Logs: 
 * v2.0.1 (2016/02/19): Se separaron los factores de redundancia en una interface aparte
 */
public strictfp interface FEC extends RedundancyFactors {
    
    /**
     * Retorna la cantidad de bytes de redundancia que generaria el algoritmo durante la codificacion
     * 
     * <b>NOTA: Este metodo es implementado por la clase AcFEC</b>
     * 
     * @param InputLength longitud del paquete original
     * @return Bytes de redundancia a ser añadidos al paquete original
     */
    public long getCodeRedundancyLength(long InputLength);
    
    /**
     * Retorna la cantidad de bytes de redundancia que contiene el paquete recibido 
     * 
     * <b>NOTA: Este metodo es implementado por la clase AcFEC</b>
     * 
     * @param InputLength Longitud del paquete codificado
     * @return Bytes de redundancia que contiene el paquete
     */
    public long getDeCodeRedundancyLength(long InputLength);
    
    /**
     * Metodo para el Codificador FEC
     * 
     * @param Input Cadena de entrada de datos
     * @return Cadena de entrada con codigo de redundancia
     * @throws UtilsException
     */
    public byte[] encode(byte[] Input) throws UtilsException;

    /**
     * Metodo para el Decodificador FEC
     * 
     * @param Input Cadena de entrada con codigo de redundancia
     * @return Cadena de entrada con los errores corregidos por el algoritmo
     * @throws UtilsException Manejador de errores en el algoritmo FEC 
     */
    public byte[] decode(byte[] Input) throws UtilsException;
    
}
