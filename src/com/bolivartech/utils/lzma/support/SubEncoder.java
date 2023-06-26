package com.bolivartech.utils.lzma.support;

import com.bolivartech.utils.lzma.rangecoder.RangeEncoder;
import java.io.IOException;

public class SubEncoder {

    private short[] m_Encoders = new short[0x300];

    public void Init() {
        RangeEncoder.InitBitModels(m_Encoders);
    }

    public void Encode(RangeEncoder rangeEncoder, byte symbol) throws IOException {
        int context = 1;
        for (int i = 7; i >= 0; i--) {
            int bit = ((symbol >> i) & 1);
            rangeEncoder.Encode(m_Encoders, context, bit);
            context = (context << 1) | bit;
        }
    }

    public void EncodeMatched(RangeEncoder rangeEncoder, byte matchByte, byte symbol) throws IOException {
        int context = 1;
        boolean same = true;
        for (int i = 7; i >= 0; i--) {
            int bit = ((symbol >> i) & 1);
            int state = context;
            if (same) {
                int matchBit = ((matchByte >> i) & 1);
                state += ((1 + matchBit) << 8);
                same = (matchBit == bit);
            }
            rangeEncoder.Encode(m_Encoders, state, bit);
            context = (context << 1) | bit;
        }
    }

    public int GetPrice(boolean matchMode, byte matchByte, byte symbol) {
        int price = 0;
        int context = 1;
        int i = 7;
        if (matchMode) {
            for (; i >= 0; i--) {
                int matchBit = (matchByte >> i) & 1;
                int bit = (symbol >> i) & 1;
                price += RangeEncoder.GetPrice(m_Encoders[((1 + matchBit) << 8) + context], bit);
                context = (context << 1) | bit;
                if (matchBit != bit) {
                    i--;
                    break;
                }
            }
        }
        for (; i >= 0; i--) {
            int bit = (symbol >> i) & 1;
            price += RangeEncoder.GetPrice(m_Encoders[context], bit);
            context = (context << 1) | bit;
        }
        return price;
    }
}
