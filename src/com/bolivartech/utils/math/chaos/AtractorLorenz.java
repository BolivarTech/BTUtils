package com.bolivartech.utils.math.chaos;

import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech
 * C.A.
 *
 * This Class define the Lorenz attractor system.
 *
 * Define un atractor de Lorenz.
 *
 * Los atractores de lorenz fueron determinador usando Chaoscope 0.3.
 *
 * @author Julian Bolivar
 * @version 3.0.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v3.0.0 - Ten attractors more added.</li>
 * </ul>
 * <ul>
 * <li>v2.0.2 - Mayor bug fixed at the default configuration in the
 * SetAtractorType method correcting the DeltaT.</li>
 * </ul>
 * <ul>
 * <li>v2.0.1 - Mayor bug fixed at the default configuration in the
 * SetAtractorType method.</li>
 * </ul>
 * <ul>
 * <li>v2.0.0 - The Random generator is initializated using the new class
 * MersenneTwisterPlus that was implemente by BolivarTech.</li>
 * <li> - Implemented with strictfp keyword to accomplish the IEEE-754
 * floating-point specification .</li>
 * </ul>
 */
public strictfp class AtractorLorenz implements Atractor {

    private double X, Y, Z, Beta, Theta, Gamma, DeltaT;
    private MersenneTwisterPlus rnd;

    /**
     * Constructor por defecto
     */
    public AtractorLorenz() {

        rnd = new MersenneTwisterPlus();
        Gamma = 6.828;
        Theta = 9.165;
        Beta = 15.026;
        DeltaT = 0.114;
        do {
            X = rnd.nextDouble();
            if (X != 0) {
                X = 1 / X;
            }
            Y = rnd.nextDouble();
            if (Y != 0) {
                Y = 1 / Y;
            }
            Z = rnd.nextDouble();
            if (Z != 0) {
                Z = 1 / Z;
            }
        } while (VerifyEquilibriumPoint(X, Y, Z));

    }

    /**
     * @param Xp
     * @param Yp
     * @param Zp
     * @return true si el punto es de equilibrio y false si no
     */
    private boolean VerifyEquilibriumPoint(double Xp, double Yp, double Zp) {
        boolean resultado;
        double Xe, Ye, Ze;

        resultado = false;
        if ((Xp == 0) && (Yp == 0) && (Zp == 0)) {
            resultado = true;
        } else {
            Ze = Theta - 1;
            Xe = Math.sqrt(Beta * Ze);
            Ye = Xe;
            if ((Xp == Xe) && (Yp == Ye) && (Zp == Ze)) {
                resultado = true;
            } else {
                Xe = -Xe;
                Ye = Xe;
                if ((Xp == Xe) && (Yp == Ye) && (Zp == Ze)) {
                    resultado = true;
                }
            }
        }
        return resultado;
    }

    /**
     * @param X0
     * @param Y0
     * @param Z0
     *
     * Establece el punto inicial donde se inicia el atractor
     */
    @Override
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
    public int getNumAtractorTypes(){
        
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
                Gamma = 6.59;
                Theta = 11.786;
                Beta = 18.221;
                DeltaT = 0.095;
                break;
            case 1:
                Gamma = 6.828;
                Theta = 9.165;
                Beta = 15.026;
                DeltaT = 0.114;
                break;
            case 2:
                Gamma = 0.809;
                Theta = 18.829;
                Beta = 8.121;
                DeltaT = 0.099;
                break;
            case 3:
                Gamma = 8.474;
                Theta = 10.71;
                Beta = 18.602;
                DeltaT = 0.092;
                break;
            case 4:
                Gamma = 7.922;
                Theta = 5.877;
                Beta = 3.537;
                DeltaT = 0.158;
                break;
            case 5:
                Gamma = 3.715;
                Theta = 10.253;
                Beta = 15.055;
                DeltaT = 0.119;
                break;
            case 6:
                Gamma = 6.526;
                Theta = 4.926;
                Beta = 14.138;
                DeltaT = 0.15;
                break;
            case 7:
                Gamma = 0.64;
                Theta = 10.369;
                Beta = 7.046;
                DeltaT = 0.169;
                break;
            case 8:
                Gamma = 0.857;
                Theta = 7.938;
                Beta = 5.852;
                DeltaT = 0.222;
                break;
            case 9:
                Gamma = 16.23;
                Theta = 10.249;
                Beta = 6.669;
                DeltaT = 0.079;
                break;
            case 10:
                Gamma = 9.851;
                Theta = 6.467;
                Beta = 14.491;
                DeltaT = 0.121;
                break;
            case 11:
                Gamma = 4.118;
                Theta = 13.165;
                Beta = 16.705;
                DeltaT = 0.098;
                break;
            case 12:
                Gamma = 7.924;
                Theta = 7.757;
                Beta = 13.565;
                DeltaT = 0.124;
                break;
            case 13:
                Gamma = 8.939;
                Theta = 5.713;
                Beta = 2.194;
                DeltaT = 0.151;
                break;
            case 14:
                Gamma = 12.286;
                Theta = 14.222;
                Beta = 4.263;
                DeltaT = 0.041;
                break;
            case 15:
                Gamma = 8.034;
                Theta = 6.607;
                Beta = 3.268;
                DeltaT = 0.137;
                break;
            case 16:
                Gamma = 11.092;
                Theta = 5.897;
                Beta = 2.887;
                DeltaT = 0.132;
                break;
            case 17:
                Gamma = 2.675;
                Theta = 5.639;
                Beta = 1.403;
                DeltaT = 0.181;
                break;
            case 18:
                Gamma = 4.939;
                Theta = 4.324;
                Beta = 1.923;
                DeltaT = 0.253;
                break;
            case 19:
                Gamma = 9.124;
                Theta = 8.905;
                Beta = 17.614;
                DeltaT = 0.101;
                break;
            default:
                Gamma = 10;
                Theta = 28;
                Beta = 2.6666666667;
                DeltaT = 0.01;
        }
    }

    /**
     * Calcula el proximo punto del atractor
     */
    @Override
    public void NextPoint() {
        double Xo;
        double Yo;
        double Zo;

        Xo = X;
        Yo = Y;
        Zo = Z;
        X = Xo + (Gamma * (Yo - Xo)) * DeltaT;
        Y = Yo + (Xo * (Theta - Zo) - Yo) * DeltaT;
        Z = Zo + (Xo * Yo - Beta * Zo) * DeltaT;
        // Codigo agregado para solucionar atractores Divergentes
        if (Math.abs(X) > 100) {
            X = 1 / X;
        }
        if (Math.abs(Y) > 100) {
            Y = 1 / Y;
        }
        if (Math.abs(Z) > 100) {
            Z = 1 / Z;
        }
    }

    /**
     * @return el valor de Beta
     */
    public double getBeta() {
        return Beta;
    }

    /**
     * @param beta Establece el valor de Beta
     */
    public void setBeta(double beta) {
        Beta = beta;
    }

    /**
     * @return Retorna el valor de DeltaT
     */
    public double getDeltaT() {
        return DeltaT;
    }

    /**
     * @param deltaT Establece el valor de DeltaT
     */
    public void setDeltaT(double deltaT) {
        DeltaT = deltaT;
    }

    /**
     * @return El valor de Gamma
     */
    public double getGamma() {
        return Gamma;
    }

    /**
     * @param gamma Establece el valor de Gamma
     */
    public void setGamma(double gamma) {
        Gamma = gamma;
    }

    /**
     * @return Retorna el valor de Theta
     */
    public double getTheta() {
        return Theta;
    }

    /**
     * @param theta Establece el valor de Theta
     */
    public void setTheta(double theta) {
        Theta = theta;
    }

    /**
     * @return el valor de X
     */
    @Override
    public double getX() {
        return X;
    }

    /**
     * @return el valor de Y
     */
    @Override
    public double getY() {
        return Y;
    }

    /**
     * @return el valor de Z
     */
    @Override
    public double getZ() {
        return Z;
    }
}
