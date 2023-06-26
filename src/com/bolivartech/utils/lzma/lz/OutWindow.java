package com.bolivartech.utils.lzma.lz;

import java.io.IOException;
import java.io.OutputStream;

public class OutWindow {

    private byte[] buffer;
    private int pos;
    private int windowSize = 0;
    private int streamPos;
    private OutputStream stream;

    public void Create(int windowSize) {
        
        if (buffer == null || this.windowSize != windowSize) {
            buffer = new byte[windowSize];
        }
        this.windowSize = windowSize;
        pos = 0;
        streamPos = 0;
    }

    public void SetStream(OutputStream stream) throws IOException {
        ReleaseStream();
        this.stream = stream;
    }

    public void ReleaseStream() throws IOException {
        Flush();
        stream = null;
    }

    public void Init(boolean solid) {
        
        if (!solid) {
            streamPos = 0;
            pos = 0;
        }
    }

    public void Flush() throws IOException {
        int size;
        
        size = pos - streamPos;
        if (size == 0) {
            return;
        }
        stream.write(buffer, streamPos, size);
        if (pos >= windowSize) {
            pos = 0;
        }
        streamPos = pos;
    }

    public void CopyBlock(int distance, int len) throws IOException {
        int lpos;
        
        lpos = this.pos - distance - 1;
        if (lpos < 0) {
            lpos += windowSize;
        }
        for (; len != 0; len--) {
            if (lpos >= windowSize) {
                lpos = 0;
            }
            buffer[this.pos++] = buffer[lpos++];
            if (this.pos >= windowSize) {
                Flush();
            }
        }
    }

    public void PutByte(byte b) throws IOException {
        
        buffer[pos++] = b;
        if (pos >= windowSize) {
            Flush();
        }
    }

    public byte GetByte(int distance) {
        int lpos;
        
        lpos = this.pos - distance - 1;
        if (lpos < 0) {
            lpos += windowSize;
        }
        return buffer[lpos];
    }
}
