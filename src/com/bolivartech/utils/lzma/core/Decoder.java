package com.bolivartech.utils.lzma.core;

import com.bolivartech.utils.lzma.rangecoder.BitTreeDecoder;
import com.bolivartech.utils.lzma.rangecoder.RangeDecoder;
import com.bolivartech.utils.lzma.BTLZMA;
import com.bolivartech.utils.lzma.lz.OutWindow;
import com.bolivartech.utils.lzma.support.SubDecoder;
import com.bolivartech.utils.lzma.support.LenDecoder;
import com.bolivartech.utils.lzma.support.LiteralDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Decoder {

    private OutWindow m_OutWindow;
    private RangeDecoder m_RangeDecoder;
    private short[] m_IsMatchDecoders;
    private short[] m_IsRepDecoders;
    private short[] m_IsRepG0Decoders;
    private short[] m_IsRepG1Decoders;
    private short[] m_IsRepG2Decoders;
    private short[] m_IsRep0LongDecoders;
    private BitTreeDecoder[] m_PosSlotDecoder;
    private short[] m_PosDecoders;
    private BitTreeDecoder m_PosAlignDecoder;
    private LenDecoder m_LenDecoder;
    private LenDecoder m_RepLenDecoder;
    private LiteralDecoder m_LiteralDecoder;
    private int m_DictionarySize;
    private int m_DictionarySizeCheck;
    private int m_PosStateMask;

    /**
     * Constructor por defecto
     */
    public Decoder() {
        int i;

        m_OutWindow = new OutWindow();
        m_RangeDecoder = new RangeDecoder();
        m_IsMatchDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
        m_IsRepDecoders = new short[Base.kNumStates];
        m_IsRepG0Decoders = new short[Base.kNumStates];
        m_IsRepG1Decoders = new short[Base.kNumStates];
        m_IsRepG2Decoders = new short[Base.kNumStates];
        m_IsRep0LongDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
        m_PosSlotDecoder = new BitTreeDecoder[Base.kNumLenToPosStates];
        m_PosDecoders = new short[Base.kNumFullDistances - Base.kEndPosModelIndex];
        m_PosAlignDecoder = new BitTreeDecoder(Base.kNumAlignBits);
        m_LenDecoder = new LenDecoder();
        m_RepLenDecoder = new LenDecoder();
        m_LiteralDecoder = new LiteralDecoder();
        m_DictionarySize = -1;
        m_DictionarySizeCheck = -1;
        for (i = 0; i < Base.kNumLenToPosStates; i++) {
            m_PosSlotDecoder[i] = new BitTreeDecoder(Base.kNumPosSlotBits);
        }
    }

    /**
     * Establece el valor del diccionario entre 1 y 536870912 bytes
     *
     * @param dictionarySize valor del diccionario entre 1 y 536870912 bytes
     * @return TRUE si logro establecer el valor del diccionario o FALSE si no
     */
    public boolean SetDictionarySize(int dictionarySize) {

        if (dictionarySize < 0) {
            return false;
        }
        if (m_DictionarySize != dictionarySize) {
            m_DictionarySize = dictionarySize;
            m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
            m_OutWindow.Create(Math.max(m_DictionarySizeCheck, (1 << 12)));
        }
        return true;
    }

    /**
     * Establece los parametros LC,LP y PB del algoritmo de descompresion. El
     * valor de lc,lp,pb bytes ess lc + lp * 9 + pb * 9 * 5, donde:
     *
     * @param lc is the number of high bits of the previous byte to use as a
     * context for literal encoding
     * @param lp is the number of low bits of the dictionary position to include
     * in literal_pos_state
     * @param pb is the number of low bits of the dictionary position to include
     * in pos_state
     * @return TRUE si logro realizar el cambio, FALSE si no
     */
    public boolean SetLcLpPb(int lc, int lp, int pb) {

        if (lc > Base.kNumLitContextBitsMax || lp > 4 || pb > Base.kNumPosStatesBitsMax) {
            return false;
        }
        m_LiteralDecoder.Create(lp, lc);
        int numPosStates = 1 << pb;
        m_LenDecoder.Create(numPosStates);
        m_RepLenDecoder.Create(numPosStates);
        m_PosStateMask = numPosStates - 1;
        return true;
    }

    /**
     * Inicializa los parametros de descompresion
     *
     * @throws IOException
     */
    private void Init() throws IOException {
        int i;

        m_OutWindow.Init(false);

        RangeDecoder.InitBitModels(m_IsMatchDecoders);
        RangeDecoder.InitBitModels(m_IsRep0LongDecoders);
        RangeDecoder.InitBitModels(m_IsRepDecoders);
        RangeDecoder.InitBitModels(m_IsRepG0Decoders);
        RangeDecoder.InitBitModels(m_IsRepG1Decoders);
        RangeDecoder.InitBitModels(m_IsRepG2Decoders);
        RangeDecoder.InitBitModels(m_PosDecoders);
        m_LiteralDecoder.Init();
        for (i = 0; i < Base.kNumLenToPosStates; i++) {
            m_PosSlotDecoder[i].Init();
        }
        m_LenDecoder.Init();
        m_RepLenDecoder.Init();
        m_PosAlignDecoder.Init();
        m_RangeDecoder.Init();
    }

    /**
     * Realiza la descompresion del InputStream en el OutputStream con una
     * dimension de OutSize.
     *
     * @param inStream Stream de Entrada
     * @param outStream Stream de Salida
     * @param outSize Dimension del Stream de Salida
     * @param Progress Apuntado para actualizar el progreso
     * @return TRUE si logro de descompresion y FALSE si no
     * @throws IOException
     */
    public boolean Decode(InputStream inStream, OutputStream outStream, long outSize, BTLZMA Progress) throws IOException {
        int state, posState, len, distance, posSlot, numDirectBits;
        int rep0, rep1, rep2, rep3;
        long nowPos64;
        byte prevByte;
        SubDecoder decoder2;
        int lProgress;
        boolean lInterrup;

        m_RangeDecoder.SetStream(inStream);
        m_OutWindow.SetStream(outStream);
        Init();

        state = Base.StateInit();
        rep0 = 0;
        rep1 = 0;
        rep2 = 0;
        rep3 = 0;
        nowPos64 = 0;
        prevByte = 0;
        lInterrup = false;
        while ((outSize < 0 || nowPos64 < outSize) && (!lInterrup)) {
            posState = (int) nowPos64 & m_PosStateMask;
            if (m_RangeDecoder.DecodeBit(m_IsMatchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                decoder2 = m_LiteralDecoder.GetDecoder((int) nowPos64, prevByte);
                if (!Base.StateIsCharState(state)) {
                    prevByte = decoder2.DecodeWithMatchByte(m_RangeDecoder, m_OutWindow.GetByte(rep0));
                } else {
                    prevByte = decoder2.DecodeNormal(m_RangeDecoder);
                }
                m_OutWindow.PutByte(prevByte);
                state = Base.StateUpdateChar(state);
                nowPos64++;
            } else {
                if (m_RangeDecoder.DecodeBit(m_IsRepDecoders, state) == 1) {
                    len = 0;
                    if (m_RangeDecoder.DecodeBit(m_IsRepG0Decoders, state) == 0) {
                        if (m_RangeDecoder.DecodeBit(m_IsRep0LongDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                            state = Base.StateUpdateShortRep(state);
                            len = 1;
                        }
                    } else {
                        if (m_RangeDecoder.DecodeBit(m_IsRepG1Decoders, state) == 0) {
                            distance = rep1;
                        } else {
                            if (m_RangeDecoder.DecodeBit(m_IsRepG2Decoders, state) == 0) {
                                distance = rep2;
                            } else {
                                distance = rep3;
                                rep3 = rep2;
                            }
                            rep2 = rep1;
                        }
                        rep1 = rep0;
                        rep0 = distance;
                    }
                    if (len == 0) {
                        len = m_RepLenDecoder.Decode(m_RangeDecoder, posState) + Base.kMatchMinLen;
                        state = Base.StateUpdateRep(state);
                    }
                } else {
                    rep3 = rep2;
                    rep2 = rep1;
                    rep1 = rep0;
                    len = Base.kMatchMinLen + m_LenDecoder.Decode(m_RangeDecoder, posState);
                    state = Base.StateUpdateMatch(state);
                    posSlot = m_PosSlotDecoder[Base.GetLenToPosState(len)].Decode(m_RangeDecoder);
                    if (posSlot >= Base.kStartPosModelIndex) {
                        numDirectBits = (posSlot >> 1) - 1;
                        rep0 = ((2 | (posSlot & 1)) << numDirectBits);
                        if (posSlot < Base.kEndPosModelIndex) {
                            rep0 += BitTreeDecoder.ReverseDecode(m_PosDecoders, rep0 - posSlot - 1, m_RangeDecoder, numDirectBits);
                        } else {
                            rep0 += (m_RangeDecoder.DecodeDirectBits(numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits);
                            rep0 += m_PosAlignDecoder.ReverseDecode(m_RangeDecoder);
                            if (rep0 < 0) {
                                if (rep0 == -1) {
                                    break;
                                }
                                return false;
                            }
                        }
                    } else {
                        rep0 = posSlot;
                    }
                }
                if (rep0 >= nowPos64 || rep0 >= m_DictionarySizeCheck) {
                    // m_OutWindow.Flush();
                    return false;
                }
                m_OutWindow.CopyBlock(rep0, len);
                nowPos64 += len;
                prevByte = m_OutWindow.GetByte(0);
            }
            if (Progress != null) {
                if (outSize > 0) {
                    lProgress = (int) (100 * ((double) nowPos64 / (double) outSize));
                    Progress.setProgress(lProgress);
                }
                lInterrup = Progress.isInterrupted();
            }
        }
        m_OutWindow.Flush();
        m_OutWindow.ReleaseStream();
        m_RangeDecoder.ReleaseStream();
        if (Progress != null) {
            Progress.setProgress(100);
        }
        return true;
    }

    /**
     * Establece las propiedades del LZMA que se encuentran en el arreglo de
     * bytes
     *
     * @param properties Propiedades del LZMA
     * @return TRUE si se lograron establecer y FALSE si no
     */
    public boolean SetDecoderProperties(byte[] properties) {
        int i, val, lc, remainder, lp, pb, dictionarySize;

        if (properties.length < 5) {
            return false;
        }
        val = properties[0] & 0xFF;
        lc = val % 9;
        remainder = val / 9;
        lp = remainder % 5;
        pb = remainder / 5;
        dictionarySize = 0;
        for (i = 0; i < 4; i++) {
            dictionarySize += ((int) (properties[1 + i]) & 0xFF) << (i * 8);
        }
        if (!SetLcLpPb(lc, lp, pb)) {
            return false;
        }
        return SetDictionarySize(dictionarySize);
    }
}
