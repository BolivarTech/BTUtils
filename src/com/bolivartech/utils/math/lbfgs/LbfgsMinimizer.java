package com.bolivartech.utils.math.lbfgs;

import com.bolivartech.utils.math.lbfgs.liblbfgs.Function;
import com.bolivartech.utils.math.lbfgs.liblbfgs.Lbfgs;
import com.bolivartech.utils.math.lbfgs.liblbfgs.LbfgsConstant;
import com.bolivartech.utils.math.lbfgs.liblbfgs.LbfgsConstant.LBFGS_Param;
import com.bolivartech.utils.math.lbfgs.liblbfgs.LbfgsConstant.LBFGS_Evaluate;
import com.bolivartech.utils.math.lbfgs.liblbfgs.LbfgsConstant.LBFGS_Progress;
import com.bolivartech.utils.math.lbfgs.liblbfgs.MutableDouble;
import static com.bolivartech.utils.math.lbfgs.liblbfgs.LbfgsConstant.ReturnValue.LBFGS_SUCCESS;
import static java.lang.System.out;

/**
 * Optimization solver using LibLBFGS
 */
public strictfp class LbfgsMinimizer {

    // Parameters for the L-BFGS optimization
    private LBFGS_Param param;
    private boolean verbose;

    public LbfgsMinimizer() {
        this(Lbfgs.defaultParams(), true);
    }

    /**
     * Minimize the function using OWL-QN
     *
     * @param c
     */
    public LbfgsMinimizer(double c) {
        this(c, true);
    }

    public LbfgsMinimizer(LBFGS_Param param) {
        this(param, true);
    }

    public LbfgsMinimizer(boolean verbose) {
        this(Lbfgs.defaultParams(), verbose);
    }

    public LbfgsMinimizer(double c, boolean verbose) {
        this(Lbfgs.defaultParams(), verbose);

        if (c < 0) {
            throw new IllegalArgumentException("c must be zero or positive");
        }
        this.param.orthantwise_c = c;
    }

    public LbfgsMinimizer(LBFGS_Param param, boolean verbose) {
        this.param = param;
        this.verbose = verbose;
    }

    public double[] minimize(Function f) {
        int dim = f.getDimension();
        double[] x = new double[dim];

        MutableDouble fx = new MutableDouble();
        Evaluate eval = new Evaluate(f);
        Progress progress = verbose ? new Progress() : null;

        // Start the L-BFGS optimization; this will invoke 
        // the callback functions evaluate() and progress() when necessary.
        Lbfgs.lbfgs(dim, x, fx, eval, progress, null, param);

        return x;
    }

    /**
     * Function evaluator
     */
    private strictfp class Evaluate implements LBFGS_Evaluate {

        private Function func;

        public Evaluate(Function func) {
            this.func = func;
        }

        public double eval(
                Object instance,
                double[] x,
                double[] g,
                int n,
                double step
        ) {
            // Get function's gradient at x 
            double[] grad = func.gradientAt(x);
            System.arraycopy(grad, 0, g, 0, grad.length);

            // Return function's value at x 
            return func.valueAt(x);
        }
    }

    /**
     * Solving progress report
     */
    private class Progress implements LBFGS_Progress {

        public LbfgsConstant.ReturnValue eval(
                Object instance,
                double[] x,
                double[] g,
                double fx,
                double xnorm,
                double gnorm,
                double step,
                int n,
                int k,
                int ls
        ) {
            out.printf("Iteration %d:\n", k);
            out.printf("\tfx = %f, xnorm = %f, gnorm = %f, step = %f\n", fx, xnorm, gnorm, step);
            out.printf("\tn = %d, k = %d, ls = %d\n\n", n, k, ls);
            return LBFGS_SUCCESS;
        }
    }
}
