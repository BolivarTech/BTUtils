package com.bolivartech.utils.fec;

/**
 * Copyright 2016 BolivarTech INC
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * This Class is the BolivarTech's that define the ReedSolomon redundancy factors.
 *
 *
 * Implementa una clase que define los factores de redundancia del ReedSolomon.
 *
 *
 * @author Julian Bolivar
 * @version 1.0.0 - February 19, 2016
 * @since 2016
 *
 * Change Logs: 
 * v1.0.0 (2016/02/19): Version Inicial.
 */
public interface RedundancyFactors {
    
    /********** Taza de redundancia para la codificacion del algoritmo de correcion de errores **************/
    /** (7/8) De cada 8 bits 7 son datos y 1 redundancia. */
    public final static double AC7OF8 = 1.1428571428571428571428571428571;
    /** (3/4) De cada 4 bits 3 son datos y 1 redundancia. */
    public final static double BC3OF4 = 1.3333333333333333333333333333333;
    /** (5/7) De cada 7 bits 5 son datos y 2 redundancia. */
    public final static double CC5OF7 = 1.4;
    /** (2/3) De cada 3 bits 2 son datos y 1 redundancia. */
    public final static double DC2OF3 = 1.5;
    /** (8/13) De cada 13 bits 8 son datos y 5 redundancia. */
    public final static double EC8OF13 = 1.625;
    /** (4/7) De cada 7 bits 4 son datos y 3 redundancia. */
    public final static double FC4OF7 = 1.75;
    /** (8/15) De cada 15 bits 8 son datos y 7 redundancia. */
    public final static double GC8OF15 = 1.875;
    /** (1/2) De cada 2 bits 1 son datos y 1 redundancia. */
    public final static double HC1OF2 = 2;
    
}
