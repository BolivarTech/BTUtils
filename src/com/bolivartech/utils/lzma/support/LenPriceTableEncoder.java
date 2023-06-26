package com.bolivartech.utils.lzma.support;

import com.bolivartech.utils.lzma.core.Base;
import com.bolivartech.utils.lzma.rangecoder.RangeEncoder;
import java.io.IOException;

public class LenPriceTableEncoder extends LenEncoder {

    private int[] prices = new int[Base.kNumLenSymbols << Base.kNumPosStatesBitsEncodingMax];
    private int tableSize;
    private int[] counters = new int[Base.kNumPosStatesEncodingMax];

    public void SetTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    public int GetPrice(int symbol, int posState) {
        return prices[posState * Base.kNumLenSymbols + symbol];
    }

    void UpdateTable(int posState) {
        SetPrices(posState, this.tableSize, this.prices, posState * Base.kNumLenSymbols);
        this.counters[posState] = this.tableSize;
    }

    public void UpdateTables(int numPosStates) {
        int posState;
        
        for (posState = 0; posState < numPosStates; posState++) {
            UpdateTable(posState);
        }
    }

    @Override
    public void Encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        super.Encode(rangeEncoder, symbol, posState);
        
        if (--counters[posState] == 0) {
            UpdateTable(posState);
        }
    }
}
