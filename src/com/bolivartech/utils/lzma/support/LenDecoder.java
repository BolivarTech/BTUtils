package com.bolivartech.utils.lzma.support;

import com.bolivartech.utils.lzma.core.Base;
import com.bolivartech.utils.lzma.rangecoder.BitTreeDecoder;
import com.bolivartech.utils.lzma.rangecoder.RangeDecoder;
import java.io.IOException;

/**
 *
 * @author jbolivarg
 */
 public class LenDecoder {

        private short[] m_Choice = new short[2];
        private BitTreeDecoder[] m_LowCoder = new BitTreeDecoder[Base.kNumPosStatesMax];
        private BitTreeDecoder[] m_MidCoder = new BitTreeDecoder[Base.kNumPosStatesMax];
        private BitTreeDecoder m_HighCoder = new BitTreeDecoder(Base.kNumHighLenBits);
        private int m_NumPosStates = 0;

        public void Create(int numPosStates) {
            for (; m_NumPosStates < numPosStates; m_NumPosStates++) {
                m_LowCoder[m_NumPosStates] = new BitTreeDecoder(Base.kNumLowLenBits);
                m_MidCoder[m_NumPosStates] = new BitTreeDecoder(Base.kNumMidLenBits);
            }
        }

        public void Init() {
            RangeDecoder.InitBitModels(m_Choice);
            for (int posState = 0; posState < m_NumPosStates; posState++) {
                m_LowCoder[posState].Init();
                m_MidCoder[posState].Init();
            }
            m_HighCoder.Init();
        }

        public int Decode(RangeDecoder rangeDecoder, int posState) throws IOException {
            if (rangeDecoder.DecodeBit(m_Choice, 0) == 0) {
                return m_LowCoder[posState].Decode(rangeDecoder);
            }
            int symbol = Base.kNumLowLenSymbols;
            if (rangeDecoder.DecodeBit(m_Choice, 1) == 0) {
                symbol += m_MidCoder[posState].Decode(rangeDecoder);
            } else {
                symbol += Base.kNumMidLenSymbols + m_HighCoder.Decode(rangeDecoder);
            }
            return symbol;
        }
    }
