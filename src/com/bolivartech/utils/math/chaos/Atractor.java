package com.bolivartech.utils.math.chaos;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech
 * C.A.
 *
 * This Class define the Attractor Interface.
 *
 * Define la interface para un atractor.
 *
 * @author Julian Bolivar
 * @version 1.0.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.0.0 - Initial version.</li>
 * </ul>
 */
public interface Atractor {
    
    /**
     * @param X0
     * @param Y0
     * @param Z0
     *
     * Establece el punto inicial donde se inicia el atractor
     */
    public void SetInitialPoint(double X0, double Y0, double Z0);
    
    /**
     * Calcula el proximo punto del atractor
     */
    public void NextPoint();
    
    /**
     * @return el valor de X
     */
    public double getX();

    /**
     * @return el valor de Y
     */
    public double getY();

    /**
     * @return el valor de Z
     */
    public double getZ();
}
