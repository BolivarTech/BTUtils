package com.bolivartech.utils.lzma.support;

import com.bolivartech.utils.lzma.rangecoder.RangeDecoder;
import java.io.IOException;

/**
 *
 * @author jbolivarg
 */
public class SubDecoder {

    private short[] m_Decoders = new short[0x300];

    public void Init() {
        RangeDecoder.InitBitModels(m_Decoders);
    }

    public byte DecodeNormal(RangeDecoder rangeDecoder) throws IOException {
        int symbol = 1;
        
        do {
            symbol = (symbol << 1) | rangeDecoder.DecodeBit(m_Decoders, symbol);
        } while (symbol < 0x100);
        return (byte) symbol;
    }

    public byte DecodeWithMatchByte(RangeDecoder rangeDecoder, byte matchByte) throws IOException {
        int symbol = 1;
        
        do {
            int matchBit = (matchByte >> 7) & 1;
            matchByte <<= 1;
            int bit = rangeDecoder.DecodeBit(m_Decoders, ((1 + matchBit) << 8) + symbol);
            symbol = (symbol << 1) | bit;
            if (matchBit != bit) {
                while (symbol < 0x100) {
                    symbol = (symbol << 1) | rangeDecoder.DecodeBit(m_Decoders, symbol);
                }
                break;
            }
        } while (symbol < 0x100);
        return (byte) symbol;
    }
}
