package com.bolivartech.utils.lzma.lz;

import java.io.IOException;

public class BinTree extends InWindow {

    private int cyclicBufferPos;
    private int cyclicBufferSize = 0;
    private int matchMaxLen;

    private int[] son;
    private int[] hash;

    private int cutValue = 0xFF;
    private int hashMask;
    private int hashSizeSum = 0;

    private boolean HASH_ARRAY = true;

    private static final int kHash2Size = 1 << 10;
    private static final int kHash3Size = 1 << 16;
    private static final int kBT2HashSize = 1 << 16;
    private static final int kStartMaxLen = 1;
    private static final int kHash3Offset = kHash2Size;
    private static final int kEmptyHashValue = 0;
    private static final int kMaxValForNormalize = (1 << 30) - 1;

    private int kNumHashDirectBytes = 0;
    private int kMinMatchCheck = 4;
    private int kFixHashSize = kHash2Size + kHash3Size;
    
    private static final int[] CrcTable = new int[256];

    // Bloque de Inicializacion Estatica de la Clase
    static {
        int i,r,j;
        for (i = 0; i < 256; i++) {
            r = i;
            for (j = 0; j < 8; j++) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ 0xEDB88320;
                } else {
                    r >>>= 1;
                }
            }
            CrcTable[i] = r;
        }
    }

    public void SetType(int numHashBytes) {
        
        HASH_ARRAY = (numHashBytes > 2);
        if (HASH_ARRAY) {
            kNumHashDirectBytes = 0;
            kMinMatchCheck = 4;
            kFixHashSize = kHash2Size + kHash3Size;
        } else {
            kNumHashDirectBytes = 2;
            kMinMatchCheck = 2 + 1;
            kFixHashSize = 0;
        }
    }

    @Override
    public void Init() throws IOException {
        super.Init();
        int i;
        
        for (i = 0; i < hashSizeSum; i++) {
            hash[i] = kEmptyHashValue;
        }
        cyclicBufferPos = 0;
        ReduceOffsets(-1);
    }

    @Override
    public void MovePos() throws IOException {
        
        if (++cyclicBufferPos >= cyclicBufferSize) {
            cyclicBufferPos = 0;
        }
        super.MovePos();
        if (pos == kMaxValForNormalize) {
            Normalize();
        }
    }

    public boolean Create(int historySize, int keepAddBufferBefore,int matchMaxLen, int keepAddBufferAfter) {
        int windowReservSize;
        int lcyclicBufferSize;
        int hs;
        
        if (historySize > kMaxValForNormalize - 256) {
            return false;
        }
        cutValue = 16 + (matchMaxLen >> 1);

        windowReservSize = (historySize + keepAddBufferBefore + matchMaxLen + keepAddBufferAfter) / 2 + 256;

        super.Create(historySize + keepAddBufferBefore, matchMaxLen + keepAddBufferAfter, windowReservSize);

        this.matchMaxLen = matchMaxLen;

        lcyclicBufferSize = historySize + 1;
        if (this.cyclicBufferSize != lcyclicBufferSize) {
            son = new int[(this.cyclicBufferSize = lcyclicBufferSize) * 2];
        }

        hs = kBT2HashSize;

        if (HASH_ARRAY) {
            hs = historySize - 1;
            hs |= (hs >> 1);
            hs |= (hs >> 2);
            hs |= (hs >> 4);
            hs |= (hs >> 8);
            hs >>= 1;
            hs |= 0xFFFF;
            if (hs > (1 << 24)) {
                hs >>= 1;
            }
            hashMask = hs;
            hs++;
            hs += kFixHashSize;
        }
        if (hs != hashSizeSum) {
            hash = new int[hashSizeSum = hs];
        }
        return true;
    }

    public int GetMatches(int[] distances) throws IOException {
        int lenLimit, offset, matchMinPos, cur, maxLen, hashValue, hash2Value, count;
        int hash3Value, temp, curMatch, curMatch2, curMatch3, ptr0, ptr1, len0, len1;
        int delta, cyclicPos, pby1, len;
        
        if (pos + matchMaxLen <= streamPos) {
            lenLimit = matchMaxLen;
        } else {
            lenLimit = streamPos - pos;
            if (lenLimit < kMinMatchCheck) {
                MovePos();
                return 0;
            }
        }

        offset = 0;
        matchMinPos = (pos > cyclicBufferSize) ? (pos - cyclicBufferSize) : 0;
        cur = bufferOffset + pos;
        maxLen = kStartMaxLen; // to avoid items for len < hashSize;
        hashValue = 0;
        hash2Value = 0;
        hash3Value = 0;

        if (HASH_ARRAY) {
            temp = CrcTable[bufferBase[cur] & 0xFF] ^ (bufferBase[cur + 1] & 0xFF);
            hash2Value = temp & (kHash2Size - 1);
            temp ^= ((int) (bufferBase[cur + 2] & 0xFF) << 8);
            hash3Value = temp & (kHash3Size - 1);
            hashValue = (temp ^ (CrcTable[bufferBase[cur + 3] & 0xFF] << 5)) & hashMask;
        } else {
            hashValue = ((bufferBase[cur] & 0xFF) ^ ((int) (bufferBase[cur + 1] & 0xFF) << 8));
        }

        curMatch = hash[kFixHashSize + hashValue];
        if (HASH_ARRAY) {
            curMatch2 = hash[hash2Value];
            curMatch3 = hash[kHash3Offset + hash3Value];
            hash[hash2Value] = pos;
            hash[kHash3Offset + hash3Value] = pos;
            if (curMatch2 > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch2] == bufferBase[cur]) {
                    distances[offset++] = maxLen = 2;
                    distances[offset++] = pos - curMatch2 - 1;
                }
            }
            if (curMatch3 > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch3] == bufferBase[cur]) {
                    if (curMatch3 == curMatch2) {
                        offset -= 2;
                    }
                    distances[offset++] = maxLen = 3;
                    distances[offset++] = pos - curMatch3 - 1;
                    curMatch2 = curMatch3;
                }
            }
            if (offset != 0 && curMatch2 == curMatch) {
                offset -= 2;
                maxLen = kStartMaxLen;
            }
        }

        hash[kFixHashSize + hashValue] = pos;

        ptr0 = (cyclicBufferPos << 1) + 1;
        ptr1 = (cyclicBufferPos << 1);

        len0 = kNumHashDirectBytes;
        len1 = kNumHashDirectBytes;

        if (kNumHashDirectBytes != 0) {
            if (curMatch > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch + kNumHashDirectBytes]
                        != bufferBase[cur + kNumHashDirectBytes]) {
                    distances[offset++] = maxLen = kNumHashDirectBytes;
                    distances[offset++] = pos - curMatch - 1;
                }
            }
        }

        count = cutValue;

        while (true) {
            if (curMatch <= matchMinPos || count-- == 0) {
                son[ptr0] = son[ptr1] = kEmptyHashValue;
                break;
            }
            delta = pos - curMatch;
            cyclicPos = ((delta <= cyclicBufferPos) ? (cyclicBufferPos - delta)
                    : (cyclicBufferPos - delta + cyclicBufferSize)) << 1;

            pby1 = bufferOffset + curMatch;
            len = Math.min(len0, len1);
            if (bufferBase[pby1 + len] == bufferBase[cur + len]) {
                while (++len != lenLimit) {
                    if (bufferBase[pby1 + len] != bufferBase[cur + len]) {
                        break;
                    }
                }
                if (maxLen < len) {
                    distances[offset++] = maxLen = len;
                    distances[offset++] = delta - 1;
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos];
                        son[ptr0] = son[cyclicPos + 1];
                        break;
                    }
                }
            }
            if ((bufferBase[pby1 + len] & 0xFF) < (bufferBase[cur + len] & 0xFF)) {
                son[ptr1] = curMatch;
                ptr1 = cyclicPos + 1;
                curMatch = son[ptr1];
                len1 = len;
            } else {
                son[ptr0] = curMatch;
                ptr0 = cyclicPos;
                curMatch = son[ptr0];
                len0 = len;
            }
        }
        MovePos();
        return offset;
    }

    public void Skip(int num) throws IOException {
        int lenLimit, matchMinPos, cur, hashValue, temp, hash2Value, hash3Value;
        int curMatch, ptr0, ptr1, len0, len1, count;
        int delta, cyclicPos, pby1, len;
            
        do {    
            if (pos + matchMaxLen <= streamPos) {
                lenLimit = matchMaxLen;
            } else {
                lenLimit = streamPos - pos;
                if (lenLimit < kMinMatchCheck) {
                    MovePos();
                    continue;
                }
            }
            matchMinPos = (pos > cyclicBufferSize) ? (pos - cyclicBufferSize) : 0;
            cur = bufferOffset + pos;
            if (HASH_ARRAY) {
                temp = CrcTable[bufferBase[cur] & 0xFF] ^ (bufferBase[cur + 1] & 0xFF);
                hash2Value = temp & (kHash2Size - 1);
                hash[hash2Value] = pos;
                temp ^= ((int) (bufferBase[cur + 2] & 0xFF) << 8);
                hash3Value = temp & (kHash3Size - 1);
                hash[kHash3Offset + hash3Value] = pos;
                hashValue = (temp ^ (CrcTable[bufferBase[cur + 3] & 0xFF] << 5)) & hashMask;
            } else {
                hashValue = ((bufferBase[cur] & 0xFF) ^ ((int) (bufferBase[cur + 1] & 0xFF) << 8));
            }

            curMatch = hash[kFixHashSize + hashValue];
            hash[kFixHashSize + hashValue] = pos;

            ptr0 = (cyclicBufferPos << 1) + 1;
            ptr1 = (cyclicBufferPos << 1);
            len0 = kNumHashDirectBytes;
            len1 = kNumHashDirectBytes;
            count = cutValue;
            while (true) {
                if (curMatch <= matchMinPos || count-- == 0) {
                    son[ptr0] = son[ptr1] = kEmptyHashValue;
                    break;
                }
                delta = pos - curMatch;
                cyclicPos = ((delta <= cyclicBufferPos) ? (cyclicBufferPos - delta)
                        : (cyclicBufferPos - delta + cyclicBufferSize)) << 1;
                pby1 = bufferOffset + curMatch;
                len = Math.min(len0, len1);
                if (bufferBase[pby1 + len] == bufferBase[cur + len]) {
                    while (++len != lenLimit) {
                        if (bufferBase[pby1 + len] != bufferBase[cur + len]) {
                            break;
                        }
                    }
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos];
                        son[ptr0] = son[cyclicPos + 1];
                        break;
                    }
                }
                if ((bufferBase[pby1 + len] & 0xFF) < (bufferBase[cur + len] & 0xFF)) {
                    son[ptr1] = curMatch;
                    ptr1 = cyclicPos + 1;
                    curMatch = son[ptr1];
                    len1 = len;
                } else {
                    son[ptr0] = curMatch;
                    ptr0 = cyclicPos;
                    curMatch = son[ptr0];
                    len0 = len;
                }
            }
            MovePos();
        } while (--num != 0);
    }

    void NormalizeLinks(int[] items, int numItems, int subValue) {
        int i;
        
        for (i = 0; i < numItems; i++) {
            int value = items[i];
            if (value <= subValue) {
                value = kEmptyHashValue;
            } else {
                value -= subValue;
            }
            items[i] = value;
        }
    }

    void Normalize() {
        int subValue;
        
        subValue = pos - cyclicBufferSize;
        NormalizeLinks(son, cyclicBufferSize * 2, subValue);
        NormalizeLinks(hash, hashSizeSum, subValue);
        ReduceOffsets(subValue);
    }

    public void SetCutValue(int cutValue) {
        this.cutValue = cutValue;
    }
}
