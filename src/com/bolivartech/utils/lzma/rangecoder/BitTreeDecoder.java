package com.bolivartech.utils.lzma.rangecoder;

public class BitTreeDecoder {

    private short[] Models;
    private int NumBitLevels;

    public BitTreeDecoder(int numBitLevels) {
        NumBitLevels = numBitLevels;
        Models = new short[1 << numBitLevels];
    }

    public void Init() {
        RangeDecoder.InitBitModels(Models);
    }

    public int Decode(RangeDecoder rangeDecoder) throws java.io.IOException {
        int m,bitIndex;
        
        m = 1;
        for (bitIndex = NumBitLevels; bitIndex != 0; bitIndex--) {
            m = (m << 1) + rangeDecoder.DecodeBit(Models, m);
        }
        return m - (1 << NumBitLevels);
    }

    public int ReverseDecode(RangeDecoder rangeDecoder) throws java.io.IOException {
        int m,symbol,bitIndex,bit;
        
        m = 1;
        symbol = 0;
        for (bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
            bit = rangeDecoder.DecodeBit(Models, m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }

    public static int ReverseDecode(short[] Models, int startIndex,RangeDecoder rangeDecoder, int NumBitLevels) throws java.io.IOException {
        int m,symbol,bitIndex,bit;
        
        m = 1;
        symbol = 0;
        for (bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
            bit = rangeDecoder.DecodeBit(Models, startIndex + m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }
}
