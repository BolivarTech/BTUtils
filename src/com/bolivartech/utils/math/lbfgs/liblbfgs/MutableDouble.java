package com.bolivartech.utils.math.lbfgs.liblbfgs;

public strictfp class MutableDouble {

    public double val;

    public MutableDouble() {
        this(0);
    }

    public MutableDouble(double val) {
        this.val = val;
    }
}
