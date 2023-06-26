package com.bolivartech.utils.lzma.lz;

import java.io.IOException;
import java.io.InputStream;

public class InWindow {

    public byte[] bufferBase; // pointer to buffer with data
    private InputStream stream;
    private int posLimit;  // offset (from _buffer) of first byte when new block reading must be done
    private boolean streamEndWasReached; // if (true) then streamPos shows real end of stream

    private int pointerToLastSafePosition;

    public int bufferOffset;

    public int blockSize;  // Size of Allocated memory block
    public int pos;             // offset (from _buffer) of curent byte
    private int keepSizeBefore;  // how many BYTEs must be kept in buffer before pos
    private int keepSizeAfter;   // how many BYTEs must be kept buffer after pos
    public int streamPos;   // offset (from _buffer) of first not read byte from Stream

    public void MoveBlock() {
        int offset,i,numBytes;
        
        offset = bufferOffset + pos - keepSizeBefore;
        // we need one additional byte, since MovePos moves on 1 byte.
        if (offset > 0) {
            offset--;
        }

        numBytes = bufferOffset + streamPos - offset;

        // check negative offset ????
        for (i = 0; i < numBytes; i++) {
            bufferBase[i] = bufferBase[offset + i];
        }
        bufferOffset -= offset;
    }

    public void ReadBlock() throws IOException {
        int size,numReadBytes;
        
        if (streamEndWasReached) {
            return;
        }
        while (true) {
            size = (0 - bufferOffset) + blockSize - streamPos;
            if (size == 0) {
                return;
            }
            numReadBytes = stream.read(bufferBase, bufferOffset + streamPos, size);
            if (numReadBytes == -1) {
                posLimit = streamPos;
                int pointerToPostion = bufferOffset + posLimit;
                if (pointerToPostion > pointerToLastSafePosition) {
                    posLimit = pointerToLastSafePosition - bufferOffset;
                }

                streamEndWasReached = true;
                return;
            }
            streamPos += numReadBytes;
            if (streamPos >= pos + keepSizeAfter) {
                posLimit = streamPos - keepSizeAfter;
            }
        }
    }

    void Free() {
        bufferBase = null;
    }

    public void Create(int keepSizeBefore, int keepSizeAfter, int keepSizeReserv) {
        int lblockSize;
        
        this.keepSizeBefore = keepSizeBefore;
        this.keepSizeAfter = keepSizeAfter;
        lblockSize = keepSizeBefore + keepSizeAfter + keepSizeReserv;
        if (bufferBase == null || this.blockSize != lblockSize) {
            Free();
            this.blockSize = lblockSize;
            bufferBase = new byte[this.blockSize];
        }
        pointerToLastSafePosition = this.blockSize - keepSizeAfter;
    }

    public void SetStream(java.io.InputStream stream) {
        this.stream = stream;
    }

    public void ReleaseStream() {
        stream = null;
    }

    public void Init() throws IOException {
        
        bufferOffset = 0;
        pos = 0;
        streamPos = 0;
        streamEndWasReached = false;
        ReadBlock();
    }

    public void MovePos() throws IOException {
        
        pos++;
        if (pos > posLimit) {
            int pointerToPostion = bufferOffset + pos;
            if (pointerToPostion > pointerToLastSafePosition) {
                MoveBlock();
            }
            ReadBlock();
        }
    }

    public byte GetIndexByte(int index) {
        
        return bufferBase[bufferOffset + pos + index];
    }

    // index + limit have not to exceed keepSizeAfter;
    public int GetMatchLen(int index, int distance, int limit) {
        int i,pby;
        
        if (streamEndWasReached) {
            if ((pos + index) + limit > streamPos) {
                limit = streamPos - (pos + index);
            }
        }
        distance++;
        // Byte *pby = _buffer + (size_t)pos + index;
        pby = bufferOffset + pos + index;
        for (i = 0; ((i < limit) && (bufferBase[pby + i] == bufferBase[pby + i - distance])); i++);
        return i;
    }

    public int GetNumAvailableBytes() {
        int result;
        
        result = streamPos - pos;
        return result;
    }

    public void ReduceOffsets(int subValue) {
        
        bufferOffset += subValue;
        posLimit -= subValue;
        pos -= subValue;
        streamPos -= subValue;
    }
}
