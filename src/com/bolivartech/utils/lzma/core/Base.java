package com.bolivartech.utils.lzma.core;

public class Base {

    public static final int kNumRepDistances = 4;
    public static final int kNumStates = 12;
    public static final int kNumPosSlotBits = 6;
    public static final int kDicLogSizeMin = 0; // 1 bytes
    public static final int kDicLogSizeMax = 29; // 536870912 bytes
    public static final int kNumLenToPosStatesBits = 2; // it's for speed optimization
    public static final int kNumLenToPosStates = 1 << kNumLenToPosStatesBits;
    public static final int kMatchMinLen = 2;
    public static final int kNumAlignBits = 4;
    public static final int kAlignTableSize = 1 << kNumAlignBits;
    public static final int kAlignMask = (kAlignTableSize - 1);
    public static final int kStartPosModelIndex = 4;
    public static final int kEndPosModelIndex = 14;
    public static final int kNumPosModels = kEndPosModelIndex - kStartPosModelIndex;
    public static final int kNumFullDistances = 1 << (kEndPosModelIndex / 2);
    public static final int kNumLitPosStatesBitsEncodingMax = 4;
    public static final int kNumLitContextBitsMax = 8;
    public static final int kNumPosStatesBitsMax = 4;
    public static final int kNumPosStatesMax = (1 << kNumPosStatesBitsMax);
    public static final int kNumPosStatesBitsEncodingMax = 4;
    public static final int kNumPosStatesEncodingMax = (1 << kNumPosStatesBitsEncodingMax);
    public static final int kNumLowLenBits = 3;
    public static final int kNumMidLenBits = 3;
    public static final int kNumHighLenBits = 8;
    public static final int kNumLowLenSymbols = 1 << kNumLowLenBits;
    public static final int kNumMidLenSymbols = 1 << kNumMidLenBits;
    public static final int kNumLenSymbols = kNumLowLenSymbols + kNumMidLenSymbols + (1 << kNumHighLenBits);
    public static final int kMatchMaxLen = kMatchMinLen + kNumLenSymbols - 1;
    // public static final int kDicLogSizeMax = 28;
    // public static final int kDistTableSizeMax = kDicLogSizeMax * 2;

    public static final int StateInit() {
        return 0;
    }

    public static final int StateUpdateChar(int index) {
        int Result;

        if (index < 4) {
            Result = 0;
        } else if (index < 10) {
            Result = index - 3;
        } else {
            Result = index - 6;
        }
        return Result;
    }

    public static final int StateUpdateMatch(int index) {
        int Result;

        Result = (index < 7 ? 7 : 10);
        return Result;
    }

    public static final int StateUpdateRep(int index) {
        int Result;

        Result = (index < 7 ? 8 : 11);
        return Result;
    }

    public static final int StateUpdateShortRep(int index) {
        int Result;

        Result = (index < 7 ? 9 : 11);
        return Result;
    }

    public static final boolean StateIsCharState(int index) {
        boolean Result;

        Result = index < 7;
        return Result;
    }

    public static final int GetLenToPosState(int len) {
        int Result;

        len -= kMatchMinLen;
        if (len < kNumLenToPosStates) {
            Result = len;
        } else {
            Result = (kNumLenToPosStates - 1);
        }
        return Result;
    }

}
