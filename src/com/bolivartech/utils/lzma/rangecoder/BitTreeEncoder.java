package com.bolivartech.utils.lzma.rangecoder;

import java.io.IOException;

public class BitTreeEncoder {

    private short[] Models;
    private int NumBitLevels;

    public BitTreeEncoder(int numBitLevels) {
        
        NumBitLevels = numBitLevels;
        Models = new short[1 << numBitLevels];
    }

    public void Init() {
        RangeDecoder.InitBitModels(Models);
    }

    public void Encode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m,bitIndex,bit;
        
        m = 1;
        for (bitIndex = NumBitLevels; bitIndex != 0;) {
            bitIndex--;
            bit = (symbol >>> bitIndex) & 1;
            rangeEncoder.Encode(Models, m, bit);
            m = (m << 1) | bit;
        }
    }

    public void ReverseEncode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m,i,bit;
        
        m = 1;
        for (i = 0; i < NumBitLevels; i++) {
            bit = symbol & 1;
            rangeEncoder.Encode(Models, m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }

    public int GetPrice(int symbol) {
        int price,m,bitIndex,bit;
        
        price = 0;
        m = 1;
        for (bitIndex = NumBitLevels; bitIndex != 0;) {
            bitIndex--;
            bit = (symbol >>> bitIndex) & 1;
            price += RangeEncoder.GetPrice(Models[m], bit);
            m = (m << 1) + bit;
        }
        return price;
    }

    public int ReverseGetPrice(int symbol) {
        int price,m,i,bit;
        
        price = 0;
        m = 1;
        for (i = NumBitLevels; i != 0; i--) {
            bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.GetPrice(Models[m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    public static int ReverseGetPrice(short[] Models, int startIndex,int NumBitLevels, int symbol) {
        int price,m,i,bit;
        
        price = 0;
        m = 1;
        for (i = NumBitLevels; i != 0; i--) {
            bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.GetPrice(Models[startIndex + m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    public static void ReverseEncode(short[] Models, int startIndex,RangeEncoder rangeEncoder, int NumBitLevels, int symbol) throws IOException {
        int m,i,bit;
        
        m = 1;
        for(i = 0; i < NumBitLevels; i++) {
            bit = symbol & 1;
            rangeEncoder.Encode(Models, startIndex + m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }
}
