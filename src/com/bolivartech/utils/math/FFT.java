package com.bolivartech.utils.math;

/**
 * Copyright 2007,2009,2010 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's FFT util for Math.
 *
 *   Realiza operaciones matematicas para le calculo de la FFT que no esta en la libreria estandar de JAVA
 *
 *   Compute the FFT and inverse FFT of a length N complex sequence.
 *   Bare bones implementation that runs in O(N log N) time. Our goal
 *   is to optimize the clarity of the code, rather than performance.
 *
 *   Note:
 *     Freq. Resolution = Fs/N
 *     where Fs Frequency of Sampling
 *           N  number of points
 *
 *   Example to sample one singnal formed by two tones:
 *     FT1 is the Tone 1 Frequency
 *     FT2 is the Tone 2 Frequency
 *     Fs  is the sample frequency
 *     N   number of point taken
 *
 *     In this example FFT frequency resolution is Fr = Fs/N
 *
 * for (int i = 0; i &lt; N; i++) {
 *           x[i] = new Complex(Math.sin((2*Math.PI*i*FT1)/Fs)+Math.sin((2*Math.PI*i*FT2)/Fs), 0);
 *       }
 *
 *  Dependencies: com.bolivartech.utils.math.Complex.class
 *
 *  Limitations
 *  -----------
 *   -  assumes N is a power of 2
 *
 *   -  not the most memory efficient algorithm (because it uses
 *      an object type for representing complex numbers and because
 *      it re-allocates memory for the subarray, instead of doing
 *      in-place or reusing a single temporary array)
 *
 * @author Julian Bolivar
 * @since 2007 - December 11, 2010.
 * @version 1.0.0
 *
 *************************************************************************/
public strictfp class FFT {

    /**
     * Compute the FFT of x[], assuming its length is a power of 2
     * 
     * @param x
     * @return  FFT of x[]
     */
    public static Complex[] fft(Complex[] x) {
        int N;
        int k;

        N = x.length;
        // base case
        if (N == 1) {
            return new Complex[]{x[0]};
        }
        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }
        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);
        // fft of odd terms
        Complex[] odd = even;  // reuse the array
        for (k = 0; k < N / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);
        // combine
        Complex[] y = new Complex[N];
        for (k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + N / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    /**
     * Compute the inverse FFT of x[], assuming its length is a power of 2
     *
     * @param x
     * @return Inverse FFT of x[]
     */
    public static Complex[] ifft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];
        int i;

        // take conjugate
        for (i = 0; i < N; i++) {
            y[i] = x[i].conjugate();
        }
        // compute forward FFT
        y = fft(y);
        // take conjugate again
        for (i = 0; i < N; i++) {
            y[i] = y[i].conjugate();
        }
        // divide by N
        for (i = 0; i < N; i++) {
            y[i] = y[i].times(1.0 / N);
        }
        return y;

    }

    /**
     * Compute the circular convolution of x and y
     *
     * @param x 
     * @param y 
     * @return circular convolution of x and y
     */
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {
        int N;

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new RuntimeException("Dimensions don't agree");
        }
        N = x.length;
        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);
        // point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++) {
            c[i] = a[i].times(b[i]);
        }
        // compute inverse FFT
        return ifft(c);
    }

    /**
     * Compute the linear convolution of x and y
     * 
     * @param x
     * @param y
     * @return linear convolution of x and y
     */
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);
        int i;

        Complex[] a = new Complex[2 * x.length];
        for (i = 0; i < x.length; i++) {
            a[i] = x[i];
        }
        for (i = x.length; i < 2 * x.length; i++) {
            a[i] = ZERO;
        }
        Complex[] b = new Complex[2 * y.length];
        for (i = 0; i < y.length; i++) {
            b[i] = y[i];
        }
        for (i = y.length; i < 2 * y.length; i++) {
            b[i] = ZERO;
        }
        return cconvolve(a, b);
    }
}
