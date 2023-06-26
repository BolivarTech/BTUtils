package com.bolivartech.utils.communications;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2011 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's Transporter Interface Class.
 *
 *   Esta interface establece el marco para el envio y recepcion de datos desde y hacia 
 *   una conexion de comunicaciones .
 *
 *   Esta es una interface
 *   
 *
 * @author Julian Bolivar
 * @since 2011 - January 10, 2012.
 * @version 1.0.0
 */
public interface Transporter {
     
    /**
     *  Abre la conexion del transportador.
     * 
     * @throws UtilsException 
     */
    public void Open() throws UtilsException;
    
    /**
     * Cierra la conexion del transportador.
     * 
     * @throws UtilsException 
     */
    public void Close() throws UtilsException;
    
    /**
     * Metodo para enviar un bloque de datos hacia el sensor
     * 
     * @param Datos Datos enviados al puerto
     * @throws UtilsException  
     */
    public void Send(byte Datos[]) throws UtilsException;
    
    /**
     * Metodo que retorna los datos recibidos por el puerto de comunicacion
     * Este metodo no produce bloqueo si no hay datos disponibles, este metodo retorna
     * los datos recibidos por el puerto o null si no hay ninguno.
     * 
     * @return  Datos leidos del puerto o null si no existe ninguno.
     */
    public byte[] Recive();
    
    /**
     * Metodo que retorna true si hay datos disponibles en el puerto para leer, false en caso contrario.
     * 
     * @return  TRUE si hay datos disponibles y FALSE si no
     */
    public boolean DataAvailable();
    
    /**
     * Metodo que retorna el tipo de transporter que se esta usando
     * 
     * ID del Transporter     Tipo
     * 1                      TCP/IP
     * 2                      UDP/IP
     * 3                      RS232
     * 
     * @return ID del tipo de transponder
     */
    public int TransporterTypeID();
    
    /**
     * Metodo que retorna el Maximum Transmission Unit (MTU) de la interface, retornando
     * la CANTIDAD NETA de bytes que se pueden enviar por la interface tomando en cuenta los 
     * encabezados usados por el protocolo.
     * 
     * Este es el tamaño maximo del paquete de datos (en bytes) que se puede enviar por la interface
     * sin necesidad de fragmentar el paquete.
     * 
     * NOTA: Retorna -1 si no logra recuperar el valor del MTU de la interface.
     * 
     * @return Maximum Transmission Unit (MTU)
     * @throws UtilsException  
     */
    public int getMTU() throws UtilsException;;    
}
