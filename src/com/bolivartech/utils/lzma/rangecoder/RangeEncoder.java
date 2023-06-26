package com.bolivartech.utils.lzma.rangecoder;

import java.io.IOException;
import java.io.OutputStream;

public class RangeEncoder {

    private static final int kNumMoveReducingBits = 2;
    public static final int kNumBitPriceShiftBits = 6;
    private static final int kTopMask = ~((1 << 24) - 1);
    private static final int kNumBitModelTotalBits = 11;
    private static final int kBitModelTotal = (1 << kNumBitModelTotalBits);
    private static final int kNumMoveBits = 5;

    private OutputStream Stream;

    private long Low;
    private int Range;
    private int cacheSize;
    private int cache;
    private long position;

    private static int[] ProbPrices = new int[kBitModelTotal >>> kNumMoveReducingBits];

    // Bloque de Inicializacion Estatica de la Clase
    static {
        int i, j, kNumBits, start, end;

        kNumBits = (kNumBitModelTotalBits - kNumMoveReducingBits);
        for (i = kNumBits - 1; i >= 0; i--) {
            start = 1 << (kNumBits - i - 1);
            end = 1 << (kNumBits - i);
            for (j = start; j < end; j++) {
                ProbPrices[j] = (i << kNumBitPriceShiftBits) + (((end - j) << kNumBitPriceShiftBits) >>> (kNumBits - i - 1));
            }
        }
    }

    public void SetStream(OutputStream stream) {
        Stream = stream;
    }

    public void ReleaseStream() {
        Stream = null;
    }

    public void Init() {
        position = 0;
        Low = 0;
        Range = -1;
        cacheSize = 1;
        cache = 0;
    }

    public void FlushData() throws IOException {
        int i;

        for (i = 0; i < 5; i++) {
            ShiftLow();
        }
    }

    public void FlushStream() throws IOException {
        Stream.flush();
    }

    public void ShiftLow() throws IOException {
        int LowHi;
        int temp;

        LowHi = (int) (Low >>> 32);
        if (LowHi != 0 || Low < 0xFF000000L) {
            position += cacheSize;
            temp = cache;
            do {
                Stream.write(temp + LowHi);
                temp = 0xFF;
            } while (--cacheSize != 0);
            cache = (((int) Low) >>> 24);
        }
        cacheSize++;
        Low = (Low & 0xFFFFFF) << 8;
    }

    public void EncodeDirectBits(int v, int numTotalBits) throws IOException {
        int i;

        for (i = numTotalBits - 1; i >= 0; i--) {
            Range >>>= 1;
            if (((v >>> i) & 1) == 1) {
                Low += Range;
            }
            if ((Range & RangeEncoder.kTopMask) == 0) {
                Range <<= 8;
                ShiftLow();
            }
        }
    }

    public long GetProcessedSizeAdd() {
        long result;

        result = cacheSize + position + 4;
        return result;
    }

    public static void InitBitModels(short[] probs) {
        int i;

        for (i = 0; i < probs.length; i++) {
            probs[i] = (kBitModelTotal >>> 1);
        }
    }

    public void Encode(short[] probs, int index, int symbol) throws IOException {
        int prob;
        int newBound;

        prob = probs[index];
        newBound = (Range >>> kNumBitModelTotalBits) * prob;
        if (symbol == 0) {
            Range = newBound;
            probs[index] = (short) (prob + ((kBitModelTotal - prob) >>> kNumMoveBits));
        } else {
            Low += (newBound & 0xFFFFFFFFL);
            Range -= newBound;
            probs[index] = (short) (prob - ((prob) >>> kNumMoveBits));
        }
        if ((Range & kTopMask) == 0) {
            Range <<= 8;
            ShiftLow();
        }
    }

    static public int GetPrice(int Prob, int symbol) {
        int result;

        result = ProbPrices[(((Prob - symbol) ^ ((-symbol))) & (kBitModelTotal - 1)) >>> kNumMoveReducingBits];
        return result;
    }

    static public int GetPrice0(int Prob) {
        int result;

        result = ProbPrices[Prob >>> kNumMoveReducingBits];
        return result;
    }

    static public int GetPrice1(int Prob) {
        int result;

        result = ProbPrices[(kBitModelTotal - Prob) >>> kNumMoveReducingBits];
        return result;
    }
}
