package com.bolivartech.utils.random.sparkers;

/**
 * Copyright 2014 BolivarTech C.A.
 *
 * Interface que Implementacion de un generador de semillas para generadores de
 * Numeros aleatorios
 *
 * La implementacion debe permitir tener una secuencia de numeros que seran
 * utilizados como inicilizadores del algoritmo.
 *
 * NOTA: Si se va a implementar un nuevo Sparker para una generados de numeros
 * aleatorios. SE DEBE TENER CUIDADO de que el sparker requerido GENERE LA
 * CANTIDAD DE SEMILLAS REQUERIDAS PARA EL ALGORITMO; esto esta especificado en
 * el constructor del algorimo que recibe el Sparker.
 *
 * @author Julian Bolivar
 * @since 2014 - January 30, 2014.
 * @version 1.0.0
 */
public interface Sparker {

    /**
     * Genera Sparks del tipo double
     *
     * @return double
     */
    double getDoubleSpark();

    /**
     * Genera Sparks del tipo float
     *
     * @return float
     */
    float getFloatSpark();

    /**
     * Genera Sparks del tipo long
     *
     * @return long
     */
    long getLongSpark();

    /**
     * Genera Sparks del tipo int
     *
     * @return int
     */
    int getIntegerSpark();

    /**
     * Genera Sparks del tipo short
     *
     * @return short
     */
    short getShortSpark();

    /**
     * Genera Sparks del tipo byte
     *
     * @return byte
     */
    byte getByteSparl();

}
