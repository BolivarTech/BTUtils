package com.bolivartech.utils.communications.utils;

import com.bolivartech.utils.btthreads.annotations.ThreadSafe;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.array.ArrayUtils;

/**
 * Copyright 2011 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's Syncronized Byte Buffer .
 * 
 *   This Class is Thread Safe
 *
 *   Esta clase implmenta un buffer de Bytes sincronizado para la comunicacion entre hebras en el programa
 *   manteniendo la coherencia y que no se corrompan los datos .
 * 
 *   El tamaño del buffer se ajusta automaticamente a la cantidad de datos presentes
 *
 *   
 *
 * @author Julian Bolivar
 * @since 2011 - January 10, 2012.
 * @version 1.0.0
 */
@ThreadSafe
public class SyncroBuffer {

    @GuardedBy("this")
    private byte buffer[];

    /**
     *  Constructor por defecto con inicializacion de un buffer vacio
     */
    public SyncroBuffer() {

        buffer = null;
    }

    /**
     * Introduce un byte en la cola del buffer.
     * 
     * @param in  Byte a ser introducido en el buffer
     */
    public synchronized void put(byte in) {
        int leng;

        if (buffer != null) {
            leng = buffer.length;
            buffer = (byte[]) ArrayUtils.resizeArray(buffer, leng + 1);
            buffer[leng] = in;
        } else {
            buffer = new byte[1];
            buffer[0] = in;
        }
    }

    /**
     * Introduce un arreglo de bytes en la cola del buffer 
     * 
     * @param in  arreglo de bytes a introducir en el buffer
     */
    public synchronized void put(byte[] in) {
        int nleng, oldleng;

        if (buffer != null) {
            nleng = in.length;
            oldleng = buffer.length;
            buffer = (byte[]) ArrayUtils.resizeArray(buffer, oldleng + nleng);
            ArrayUtils.arrayCopy(in, 0, buffer, oldleng, nleng);
        } else {
            nleng = in.length;
            buffer = new byte[nleng];
            ArrayUtils.arrayCopy(in, 0, buffer, 0, nleng);
        }
    }

    /**
     * Introduce la representacion en bytes de un string en la cola del buffer
     * 
     * @param in String a ser indroducido en la cola del buffer
     */
    public synchronized void put(String in) {
        int nleng, oldleng;
        byte[] StringArray;

        if (buffer != null) {
            StringArray = in.getBytes();
            nleng = StringArray.length;
            oldleng = buffer.length;
            buffer = (byte[]) ArrayUtils.resizeArray(buffer, oldleng + nleng);
            ArrayUtils.arrayCopy(in, 0, buffer, oldleng, nleng);
        } else {
            buffer = in.getBytes();
        }
    }

    /**
     * Retorna todos los bytes contenidos en el buffer y procede a vaciar el buffer 
     * 
     * @return  Bytes contenidos en el buffer
     */
    public synchronized byte[] getall() {
        byte OutBuffer[];

        OutBuffer = null;
        if (buffer != null) {
            OutBuffer = new byte[buffer.length];
            ArrayUtils.arrayCopy(buffer, 0, OutBuffer, 0, buffer.length);
            buffer = null;
        }
        return OutBuffer;
    }

    /**
     * Retorna el primer Byte contenido en el buffer 
     * 
     * @return Primer byte contenido en el buffer
     */
    public synchronized byte getByte() {
        byte Valor;
        int OldLeng;

        Valor = 0x00;
        if (buffer != null) {
            Valor = buffer[0];
            OldLeng = buffer.length;
            if (OldLeng > 1) {
                buffer = (byte[]) ArrayUtils.subArray(buffer, 1, OldLeng - 1);
            } else {
                buffer = null;
            }
        }
        return Valor;
    }

    /**
     * Retorna el numero de bytes especificados del buffer, si la cantidad de bytes
     * solicitados en mayor al tamaño del buffer retorna el contenido del buffer completo
     * 
     * @param NumBytes a ser retornados
     * @return  Arreglo de bytes recuperado del buffer
     */
    public synchronized byte[] getBytes(int NumBytes) {
        byte OutBuffer[];
        int OldLeng;

        OutBuffer = null;
        if (buffer != null) {
            OldLeng = buffer.length;
            if (NumBytes > OldLeng) {
                NumBytes = OldLeng;
            }
            if (NumBytes > 0) {
                OutBuffer = (byte[]) ArrayUtils.subArray(buffer, 0, NumBytes);
                if (NumBytes == OldLeng) {
                    buffer = null;
                } else {
                    buffer = (byte[]) ArrayUtils.subArray(buffer, NumBytes, OldLeng - NumBytes);
                }
            }
        }
        return OutBuffer;
    }

    /**
     * Retorna la cantidad de bytes almacenados en el buffer
     * 
     * NOTA: Aunque se garantiza la atomicidad de esta funcion y esta bloqueado
     * el buffer durante su ejecucion, al finalizar el bloqueo es liberado permitiendo
     * que cualquier otra hebra modifique el contenido del buffer con lo cual este valor
     * seria invalido, por esto se recomienda utilizar las tecnicas necesarias para 
     * asegurar confiabilidad de los datos como por ejemplo recuperar una copia local
     * del contenido del buffer y usar esta copia para las operaciones requeridas.
     * 
     * @return  Cantidad de bytes almacenados en el buffer
     */
    public synchronized int length() {
        int Longitud;

        Longitud = 0;
        if (buffer != null) {
            Longitud = buffer.length;
        }
        return Longitud;
    }
}
