package com.bolivartech.utils.fec.reedsolomon;

import com.bolivartech.utils.array.ArrayUtils;
import com.bolivartech.utils.converters.Converter;
import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2010,2011,2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 * <p>Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's Reed-Solomon decoding Class.
 *
 * Esta clase implementa el metodo Reed-Solomon decoding.
 * 
 * Class ID: "35DGFH1"
 * Loc: 000-003
 *
 * @author Julian Bolivar
 * @since 2010 - March 25, 2016.
 * @version 1.0.1
 * 
 * Change Logs: 
 * v1.0.0 (2010-01-26) Version Inicial.
 * v1.0.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion.
 */
public strictfp final class ReedSolomonDecoder {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFH1";
    
    // Codigos de Errores
    public static final int ERRORBADLOCATION = -1;
    public static final int ERRORRI1WASZERO = -2;
    public static final int ERRORSIGMATILDEWASZERO = -3;
    public static final int ERRORLOCDEGNUMROOTS = -4;
    // Campo de Galua
    private final GenericGF field;

    /**
     * Constructor por defecto con inicializacion de campo de Galois
     *
     * @param GField Campo de Galois
     */
    protected ReedSolomonDecoder(GenericGF GField) {
        if (!GenericGF.QR_CODE_FIELD_256.equals(GField)) {
            throw new IllegalArgumentException("Only QR Code is supported at this time");
        }
        this.field = GField;
    }

    /**
     * <p>Decodifica el mensaje y corrige los errores basados en el algoritmo de
     * Reed-Solomon.</p>
     *
     * @param Input Mensaje a ser decodificado
     * @param ErrorCorrectionBytes Numero de bytes de correcion en el bloque
     * @return Mensaje con los errores corregidos
     * @throws UtilsException Manejador de fallas en la recuperacion de errores.
     */
    protected byte[] decode(byte[] Input, int ErrorCorrectionBytes) throws UtilsException {
        byte[] Output;
        int[] received;
        int i;
        int position;
        GenericGFPoly poly;
        int[] syndromeCoefficients;
        boolean dataMatrix;
        boolean noError;
        int eval;
        int[] errorLocations;
        int[] errorMagnitudes;

        received = new int[Input.length];
        for (i = 0; i < received.length; i++) {
            received[i] = ((int) Input[i]) & 0x000000FF;
        }
        poly = new GenericGFPoly(field, received);
        syndromeCoefficients = new int[ErrorCorrectionBytes];
        dataMatrix = field.equals(GenericGF.DATA_MATRIX_FIELD_256);
        noError = true;
        for (i = 0; i < ErrorCorrectionBytes; i++) {
            eval = poly.evaluateAt(field.exp(dataMatrix ? i + 1 : i));
            syndromeCoefficients[syndromeCoefficients.length - 1 - i] = eval;
            if (eval != 0) {
                noError = false;
            }
        }
        Output = null;
        if (noError) {
            Output = new byte[Input.length - ErrorCorrectionBytes];
            ArrayUtils.arrayCopy(Input, 0, Output, 0, Input.length - ErrorCorrectionBytes);
        } else {
            GenericGFPoly syndrome = new GenericGFPoly(field, syndromeCoefficients);
            GenericGFPoly[] sigmaOmega = runEuclideanAlgorithm(field.buildMonomial(ErrorCorrectionBytes, 1), syndrome, ErrorCorrectionBytes);
            GenericGFPoly sigma = sigmaOmega[0];
            GenericGFPoly omega = sigmaOmega[1];
            errorLocations = findErrorLocations(sigma);
            errorMagnitudes = findErrorMagnitudes(omega, errorLocations, dataMatrix);
            for (i = 0; i < errorLocations.length; i++) {
                position = received.length - 1 - field.log(errorLocations[i]);
                if (position < 0) {
                    throw new UtilsException("Bad error location",ReedSolomonDecoder.ERRORBADLOCATION,ReedSolomonDecoder.CLASSID+"000");
                }
                received[position] = GenericGF.addOrSubtract(received[position], errorMagnitudes[i]);
            }
            Output = Converter.int2byte(received);
            for (i = 0; i < (Output.length / 4); i++) {
                Output[i] = Output[3 + (i * 4)];
            }
            Output = (byte[]) ArrayUtils.resizeArray(Output,(Output.length/4)-ErrorCorrectionBytes);
        }
        return Output;
    }

    /*
     * Algoritmo para generar los polinomios de Galois
     */
    private GenericGFPoly[] runEuclideanAlgorithm(GenericGFPoly a, GenericGFPoly b, int R) throws UtilsException {
        // Assume a's degree is >= b's
        if (a.getDegree() < b.getDegree()) {
            GenericGFPoly temp = a;
            a = b;
            b = temp;
        }

        GenericGFPoly rLast = a;
        GenericGFPoly r = b;
        GenericGFPoly sLast = field.getOne();
        GenericGFPoly s = field.getZero();
        GenericGFPoly tLast = field.getZero();
        GenericGFPoly t = field.getOne();

        // Run Euclidean algorithm until r's degree is less than R/2
        while (r.getDegree() >= R / 2) {
            GenericGFPoly rLastLast = rLast;
            GenericGFPoly sLastLast = sLast;
            GenericGFPoly tLastLast = tLast;
            rLast = r;
            sLast = s;
            tLast = t;

            // Divide rLastLast by rLast, with quotient in q and remainder in r
            if (rLast.isZero()) {
                // Euclidean algorithm already terminated?
                throw new UtilsException("r_{i-1} was zero",ReedSolomonDecoder.ERRORRI1WASZERO,ReedSolomonDecoder.CLASSID+"001");
            }
            r = rLastLast;
            GenericGFPoly q = field.getZero();
            int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
            int dltInverse = field.inverse(denominatorLeadingTerm);
            while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
                int degreeDiff = r.getDegree() - rLast.getDegree();
                int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
                q = q.addOrSubtract(field.buildMonomial(degreeDiff, scale));
                r = r.addOrSubtract(rLast.multiplyByMonomial(degreeDiff, scale));
            }

            s = q.multiply(sLast).addOrSubtract(sLastLast);
            t = q.multiply(tLast).addOrSubtract(tLastLast);
        }

        int sigmaTildeAtZero = t.getCoefficient(0);
        if (sigmaTildeAtZero == 0) {
            throw new UtilsException("sigmaTilde(0) was zero",ReedSolomonDecoder.ERRORSIGMATILDEWASZERO,ReedSolomonDecoder.CLASSID+"002");
        }

        int inverse = field.inverse(sigmaTildeAtZero);
        GenericGFPoly sigma = t.multiply(inverse);
        GenericGFPoly omega = r.multiply(inverse);
        return new GenericGFPoly[]{sigma, omega};
    }

    /*
     * Encuentra la posicion del error encontrado
     */
    private int[] findErrorLocations(GenericGFPoly errorLocator) throws UtilsException {
        // This is a direct application of Chien's search
        int numErrors = errorLocator.getDegree();
        if (numErrors == 1) { // shortcut
            return new int[]{errorLocator.getCoefficient(1)};
        }
        int[] result = new int[numErrors];
        int e = 0;
        for (int i = 1; i < field.getSize() && e < numErrors; i++) {
            if (errorLocator.evaluateAt(i) == 0) {
                result[e] = field.inverse(i);
                e++;
            }
        }
        if (e != numErrors) {
            throw new UtilsException("Error locator degree does not match number of roots",ReedSolomonDecoder.ERRORLOCDEGNUMROOTS,ReedSolomonDecoder.CLASSID+"003");
        }
        return result;
    }

    /*
     * Determina la magnitud del error en una determinada locacion
     */
    private int[] findErrorMagnitudes(GenericGFPoly errorEvaluator, int[] errorLocations, boolean dataMatrix) {
        // This is directly applying Forney's Formula
        int s = errorLocations.length;
        int[] result = new int[s];
        for (int i = 0; i < s; i++) {
            int xiInverse = field.inverse(errorLocations[i]);
            int denominator = 1;
            for (int j = 0; j < s; j++) {
                if (i != j) {
                    //denominator = field.multiply(denominator,
                    //    GenericGF.addOrSubtract(1, field.multiply(errorLocations[j], xiInverse)));
                    // Above should work but fails on some Apple and Linux JDKs due to a Hotspot bug.
                    // Below is a funny-looking workaround from Steven Parkes
                    int term = field.multiply(errorLocations[j], xiInverse);
                    int termPlus1 = (term & 0x1) == 0 ? term | 1 : term & ~1;
                    denominator = field.multiply(denominator, termPlus1);
                }
            }
            result[i] = field.multiply(errorEvaluator.evaluateAt(xiInverse),
                    field.inverse(denominator));
            // Thanks to sanfordsquires for this fix:
            if (dataMatrix) {
                result[i] = field.multiply(result[i], xiInverse);
            }
        }
        return result;
    }
    
    /**
     * Retorna el identificador de la Clase
     * 
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }
}
