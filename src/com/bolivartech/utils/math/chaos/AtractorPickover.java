package com.bolivartech.utils.math.chaos;

import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech
 * C.A.
 *
 * This Class define the Pickover attractor system.
 *
 * Define un atractor de Pickover.
 *
 * Implemented with strictfp keyword to accomplish the IEEE-754 floating-point
 * specification
 *
 * Los atractores de Pickover fueron determinador usando Chaoscope
 * 0.3.
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
public strictfp class AtractorPickover implements Atractor {

    private double X, Y, Z, Alpha, Beta, Gamma, Delta;
    private MersenneTwisterPlus rnd;

    /**
     * Constructor por defecto
     */
    public AtractorPickover() {

        rnd = new MersenneTwisterPlus();
        Alpha = -0.759494;
        Beta = 2.449367;
        Gamma = 1.253165;
        Delta = 1.5;
        X = rnd.nextDouble();
        Y = rnd.nextDouble();
        Z = rnd.nextDouble();
    }

    /**
     * @param X0
     * @param Y0
     * @param Z0
     *
     * Establece el punto inicial donde se inicia el atractor
     */
    public void SetInitialPoint(double X0, double Y0, double Z0) {
        X = X0;
        Y = Y0;
        Z = Z0;
    }

    /**
     * Retorna el numero de atractaroes predefinidos en AttractorType
     *
     * @return Numero de atractores predefinidos
     */
    public int getNumAtractorTypes() {

        return 20;
    }

    /**
     * @param N
     *
     * Establece el tipo de atractor en uno de los 20 predefinidos el valor de N
     * debe ser entre 0 y 19. Si el valor esta fuera de este rango se utilizan
     * los valores canonicos de Lorenz para el atractor
     */
    public void SetAtractorType(int N) {
        switch (N) {
            case 0:
                Alpha = 11.786;
                Beta = 18.221;
                Gamma = 6.59;
                Delta = 0.095;
                break;
            case 1:
                Alpha = 0.666;
                Beta = 0.705;
                Gamma = -0.225;
                Delta =  2.934;
                break;
            case 2:
                Alpha = 1.508;
                Beta = 0.24;
                Gamma = -2.247;
                Delta =  2.715;
                break;
            case 3:
                Alpha = 2.293;
                Beta = 2.317;
                Gamma = 2.612; 
                Delta =  1.424;
                break;
            case 4:
                Alpha = -1.07;
                Beta =  0.119; 
                Gamma = 0.224; 
                Delta =  2.751;
                break;
            case 5:
                Alpha = 2.857;
                Beta =  2.146; 
                Gamma = -2.894; 
                Delta = 0.088;
                break;
            case 6:
                Alpha = -2.916;
                Beta =  0.507;
                Gamma = -1.38; 
                Delta = 0.756;
                break;
            case 7:
                Alpha = 2.231;
                Beta =  1.064; 
                Gamma = 1.523; 
                Delta = 1.445;
                break;
            case 8:
                Alpha = -0.566;
                Beta =  1.865; 
                Gamma = 1.928;
                Delta = 1.967;
                break;
            case 9:
                Alpha = 0.509;
                Beta =  0.616; 
                Gamma = -0.101; 
                Delta = 2.135;
                break;
            case 10:
                Alpha = 2.859; 
                Beta =  1.552; 
                Gamma = 2.753; 
                Delta = 2.494;
                break;
            case 11:
                Alpha = 2.176;
                Beta =  0.688; 
                Gamma = -1.864; 
                Delta = 1.363;
                break;
            case 12:
                Alpha = -2.224;
                Beta =  0.031;
                Gamma = 0.274; 
                Delta = 2.062;
                break;
            case 13:
                Alpha = 2.571;
                Beta =  2.369; 
                Gamma = -1.755; 
                Delta = 0.546;
                break;
            case 14:
                Alpha = 2.979;
                Beta =  0.524; 
                Gamma = -1.536; 
                Delta = 0.271;
                break;
            case 15:
                Alpha = -1.464;
                Beta =  2.752;
                Gamma = 1.93;
                Delta = 1.241;
                break;
            case 16:
                Alpha = 2.921;
                Beta =  1.759; 
                Gamma = -2.104; 
                Delta = 0.126;
                break;
            case 17:
                Alpha = -2.834;
                Beta =  2.407;
                Gamma = -1.851; 
                Delta = 0.496;
                break;
            case 18:
                Alpha = -2.383;
                Beta =  1.339; 
                Gamma = 0.841; 
                Delta = 2.749;
                break;
            case 19:
                Alpha = -2.643;
                Beta =  0.373;
                Gamma = 0.945; 
                Delta = 2.414;
                break;
            default:
                Alpha = -0.759494;
                Beta = 2.449367;
                Gamma = 1.253165;
                Delta = 1.5;
        }
    }

    /**
     * Calcula el proximo punto del atractor
     */
    public void NextPoint() {
        double Xo;
        double Yo;
        double Zo;

        Xo = X;
        Yo = Y;
        Zo = Z;
        X = Math.sin(Alpha * Yo) - Zo * Math.cos(Beta * Xo);
        Y = Zo * Math.sin(Gamma * Xo) - Math.cos(Delta * Yo);
        Z = Math.sin(Xo);
    }

    /**
     * @return el valor de Beta
     */
    public double getBeta() {
        return this.Beta;
    }

    /**
     * @param beta Establece el valor de Beta
     */
    public void setBeta(double beta) {
        this.Beta = beta;
    }

    /**
     * @return Retorna el valor de Delta
     */
    public double getDelta() {
        return this.Delta;
    }

    /**
     * @param delta Establece el valor de Delta
     */
    public void setDelta(double delta) {
        this.Delta = delta;
    }

    /**
     * @return El valor de Gamma
     */
    public double getGamma() {
        return this.Gamma;
    }

    /**
     * @param gamma Establece el valor de Gamma
     */
    public void setGamma(double gamma) {
        this.Gamma = gamma;
    }

    /**
     * @return Retorna el valor de Alpha
     */
    public double getAlpha() {
        return this.Alpha;
    }

    /**
     * @param alpha Establece el valor de Alpha
     */
    public void setAlpha(double alpha) {
        this.Alpha = alpha;
    }

    /**
     * @return el valor de X
     */
    public double getX() {
        return X;
    }

    /**
     * @return el valor de Y
     */
    public double getY() {
        return Y;
    }

    /**
     * @return el valor de Z
     */
    public double getZ() {
        return Z;
    }
}
