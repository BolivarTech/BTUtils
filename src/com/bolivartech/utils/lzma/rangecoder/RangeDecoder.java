package com.bolivartech.utils.lzma.rangecoder;

import java.io.IOException;
import java.io.InputStream;

public class RangeDecoder {

    private static final int kTopMask = ~((1 << 24) - 1);
    private static final int kNumBitModelTotalBits = 11;
    private static final int kBitModelTotal = (1 << kNumBitModelTotalBits);
    private static final int kNumMoveBits = 5;

    private int Range;
    private int Code;

    private InputStream Stream;

    public final void SetStream(InputStream stream) {
        Stream = stream;
    }

    public final void ReleaseStream() {
        Stream = null;
    }

    public final void Init() throws IOException {
        int i;
        
        Code = 0;
        Range = -1;
        for (i = 0; i < 5; i++) {
            Code = (Code << 8) | Stream.read();
        }
    }

    public final int DecodeDirectBits(int numTotalBits) throws IOException {
        int i;
        int result = 0;
        
        for (i = numTotalBits; i != 0; i--) {
            Range >>>= 1;
            int t = ((Code - Range) >>> 31);
            Code -= Range & (t - 1);
            result = (result << 1) | (1 - t);
            if ((Range & kTopMask) == 0) {
                Code = (Code << 8) | Stream.read();
                Range <<= 8;
            }
        }
        return result;
    }

    public int DecodeBit(short[] probs, int index) throws IOException {
        int prob;
        int newBound;
        int result;
        
        prob = probs[index];
        newBound = (Range >>> kNumBitModelTotalBits) * prob;
        if ((Code ^ 0x80000000) < (newBound ^ 0x80000000)) {
            Range = newBound;
            probs[index] = (short) (prob + ((kBitModelTotal - prob) >>> kNumMoveBits));
            if ((Range & kTopMask) == 0) {
                Code = (Code << 8) | Stream.read();
                Range <<= 8;
            }
            result = 0;
        } else {
            Range -= newBound;
            Code -= newBound;
            probs[index] = (short) (prob - ((prob) >>> kNumMoveBits));
            if ((Range & kTopMask) == 0) {
                Code = (Code << 8) | Stream.read();
                Range <<= 8;
            }
            result = 1;
        }
        return result;
    }

    public static void InitBitModels(short[] probs) {
        int i;
                
        for (i = 0; i < probs.length; i++) {
            probs[i] = (kBitModelTotal >>> 1);
        }
    }
}
