package com.bolivartech.utils.lzma.support;

import com.bolivartech.utils.lzma.core.Base;
import com.bolivartech.utils.lzma.rangecoder.BitTreeEncoder;
import com.bolivartech.utils.lzma.rangecoder.RangeEncoder;
import java.io.IOException;

public class LenEncoder {

    private short[] _choice = new short[2];
    private BitTreeEncoder[] _lowCoder = new BitTreeEncoder[Base.kNumPosStatesEncodingMax];
    private BitTreeEncoder[] _midCoder = new BitTreeEncoder[Base.kNumPosStatesEncodingMax];
    private BitTreeEncoder _highCoder = new BitTreeEncoder(Base.kNumHighLenBits);

    public LenEncoder() {
        for (int posState = 0; posState < Base.kNumPosStatesEncodingMax; posState++) {
            _lowCoder[posState] = new BitTreeEncoder(Base.kNumLowLenBits);
            _midCoder[posState] = new BitTreeEncoder(Base.kNumMidLenBits);
        }
    }

    public void Init(int numPosStates) {
        RangeEncoder.InitBitModels(_choice);

        for (int posState = 0; posState < numPosStates; posState++) {
            _lowCoder[posState].Init();
            _midCoder[posState].Init();
        }
        _highCoder.Init();
    }

    public void Encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        if (symbol < Base.kNumLowLenSymbols) {
            rangeEncoder.Encode(_choice, 0, 0);
            _lowCoder[posState].Encode(rangeEncoder, symbol);
        } else {
            symbol -= Base.kNumLowLenSymbols;
            rangeEncoder.Encode(_choice, 0, 1);
            if (symbol < Base.kNumMidLenSymbols) {
                rangeEncoder.Encode(_choice, 1, 0);
                _midCoder[posState].Encode(rangeEncoder, symbol);
            } else {
                rangeEncoder.Encode(_choice, 1, 1);
                _highCoder.Encode(rangeEncoder, symbol - Base.kNumMidLenSymbols);
            }
        }
    }

    public void SetPrices(int posState, int numSymbols, int[] prices, int st) {
        int a0 = RangeEncoder.GetPrice0(_choice[0]);
        int a1 = RangeEncoder.GetPrice1(_choice[0]);
        int b0 = a1 + RangeEncoder.GetPrice0(_choice[1]);
        int b1 = a1 + RangeEncoder.GetPrice1(_choice[1]);
        int i = 0;
        for (i = 0; i < Base.kNumLowLenSymbols; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = a0 + _lowCoder[posState].GetPrice(i);
        }
        for (; i < Base.kNumLowLenSymbols + Base.kNumMidLenSymbols; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = b0 + _midCoder[posState].GetPrice(i - Base.kNumLowLenSymbols);
        }
        for (; i < numSymbols; i++) {
            prices[st + i] = b1 + _highCoder.GetPrice(i - Base.kNumLowLenSymbols - Base.kNumMidLenSymbols);
        }
    }
}
