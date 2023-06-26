package com.bolivartech.utils.lzma.support;

/**
 *
 * @author jbolivarg
 */
public class LiteralDecoder {

    private SubDecoder[] m_Coders;
    private int m_NumPrevBits;
    private int m_NumPosBits;
    private int m_PosMask;

    public void Create(int numPosBits, int numPrevBits) {
        if (m_Coders != null && m_NumPrevBits == numPrevBits && m_NumPosBits == numPosBits) {
            return;
        }
        m_NumPosBits = numPosBits;
        m_PosMask = (1 << numPosBits) - 1;
        m_NumPrevBits = numPrevBits;
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        m_Coders = new SubDecoder[numStates];
        for (int i = 0; i < numStates; i++) {
            m_Coders[i] = new SubDecoder();
        }
    }

    public void Init() {
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        for (int i = 0; i < numStates; i++) {
            m_Coders[i].Init();
        }
    }

    public SubDecoder GetDecoder(int pos, byte prevByte) {
        return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
    }
}
