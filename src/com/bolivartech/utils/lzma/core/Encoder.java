package com.bolivartech.utils.lzma.core;

import com.bolivartech.utils.lzma.support.LiteralEncoder;
import com.bolivartech.utils.lzma.rangecoder.BitTreeEncoder;
import com.bolivartech.utils.lzma.rangecoder.RangeEncoder;
import com.bolivartech.utils.lzma.BTLZMA;
import com.bolivartech.utils.lzma.lz.BinTree;
import com.bolivartech.utils.lzma.support.SubEncoder;
import com.bolivartech.utils.lzma.support.LenPriceTableEncoder;
import com.bolivartech.utils.lzma.support.Optimal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Encoder {

    public static final int EMatchFinderTypeBT2 = 0;
    public static final int EMatchFinderTypeBT4 = 1;
    private static final int kIfinityPrice = 0xFFFFFFF;
    private static final int kDefaultDictionaryLogSize = 22;
    private static final int kNumFastBytesDefault = 0x20;
    private static byte[] g_FastPos = new byte[1 << 11];
    public static final int kNumLenSpecSymbols = Base.kNumLowLenSymbols + Base.kNumMidLenSymbols;
    private static final int kNumOpts = 1 << 12;
    public static final int kPropSize = 5;

    private int _state;
    private byte _previousByte;
    private int[] _repDistances;

    private Optimal[] _optimum;
    private BinTree _matchFinder;
    private RangeEncoder _rangeEncoder;

    private short[] _isMatch;
    private short[] _isRep;
    private short[] _isRepG0;
    private short[] _isRepG1;
    private short[] _isRepG2;
    private short[] _isRep0Long;

    private BitTreeEncoder[] _posSlotEncoder;

    private short[] _posEncoders;
    private BitTreeEncoder _posAlignEncoder;

    private LenPriceTableEncoder _lenEncoder;
    private LenPriceTableEncoder _repMatchLenEncoder;

    private LiteralEncoder _literalEncoder;

    private int[] _matchDistances;

    private int _numFastBytes;
    private int _longestMatchLength;
    private int _numDistancePairs;
    private int _additionalOffset;
    private int _optimumEndIndex;
    private int _optimumCurrentIndex;

    private boolean _longestMatchWasFound;

    private int[] _posSlotPrices;
    private int[] _distancesPrices;
    private int[] _alignPrices;
    private int _alignPriceCount;

    private int _distTableSize;

    private int _posStateBits;
    private int _posStateMask;
    private int _numLiteralPosStateBits;
    private int _numLiteralContextBits;

    private int _dictionarySize;
    private int _dictionarySizePrev;
    private int _numFastBytesPrev;

    private long nowPos64;
    private boolean _finished;
    private InputStream _inStream;

    private int _matchFinderType;
    private boolean _writeEndMark;
    private boolean _needReleaseMFStream;

    private int[] reps;
    private int[] repLens;
    private int backRes;

    private long[] processedInSize;
    private long[] processedOutSize;

    private byte[] properties;
    private int[] tempPrices;
    private int _matchPriceCount;

    // Bloque de Inicializacion Estatica de la Clase
    static {
        int kFastSlots, c, j, k, slotFast;

        kFastSlots = 22;
        c = 2;
        g_FastPos[0] = 0;
        g_FastPos[1] = 1;
        for (slotFast = 2; slotFast < kFastSlots; slotFast++) {
            k = (1 << ((slotFast >> 1) - 1));
            for (j = 0; j < k; j++, c++) {
                g_FastPos[c] = (byte) slotFast;
            }
        }
    }

    private int GetPosSlot(int pos) {
        int Result;

        if (pos < (1 << 11)) {
            Result = g_FastPos[pos];
        } else if (pos < (1 << 21)) {
            Result = (g_FastPos[pos >> 10] + 20);
        } else {
            Result = (g_FastPos[pos >> 20] + 40);
        }
        return Result;
    }

    private int GetPosSlot2(int pos) {
        int Result;

        if (pos < (1 << 17)) {
            Result = (g_FastPos[pos >> 6] + 12);
        } else if (pos < (1 << 27)) {
            Result = (g_FastPos[pos >> 16] + 32);
        } else {
            Result = (g_FastPos[pos >> 26] + 52);
        }
        return Result;
    }

    private void BaseInit() {
        int i;

        _state = Base.StateInit();
        _previousByte = 0;
        for (i = 0; i < Base.kNumRepDistances; i++) {
            _repDistances[i] = 0;
        }
    }

    private void Create() {

        if (_matchFinder == null) {
            BinTree bt = new BinTree();
            int numHashBytes = 4;
            if (_matchFinderType == EMatchFinderTypeBT2) {
                numHashBytes = 2;
            }
            bt.SetType(numHashBytes);
            _matchFinder = bt;
        }
        _literalEncoder.Create(_numLiteralPosStateBits, _numLiteralContextBits);

        if (_dictionarySize == _dictionarySizePrev && _numFastBytesPrev == _numFastBytes) {
            return;
        }
        _matchFinder.Create(_dictionarySize, kNumOpts, _numFastBytes, Base.kMatchMaxLen + 1);
        _dictionarySizePrev = _dictionarySize;
        _numFastBytesPrev = _numFastBytes;
    }

    public Encoder() {
        int i;

        _writeEndMark = false;
        _needReleaseMFStream = false;
        _posStateBits = 2;
        _posStateMask = (4 - 1);
        _numLiteralPosStateBits = 0;
        _numLiteralContextBits = 3;
        _posSlotPrices = new int[1 << (Base.kNumPosSlotBits + Base.kNumLenToPosStatesBits)];
        _distancesPrices = new int[Base.kNumFullDistances << Base.kNumLenToPosStatesBits];
        _alignPrices = new int[Base.kAlignTableSize];
        _state = Base.StateInit();
        _posSlotEncoder = new BitTreeEncoder[Base.kNumLenToPosStates];
        _repDistances = new int[Base.kNumRepDistances];
        _distTableSize = (kDefaultDictionaryLogSize * 2);
        _dictionarySize = (1 << kDefaultDictionaryLogSize);
        _dictionarySizePrev = -1;
        _numFastBytesPrev = -1;
        _optimum = new Optimal[kNumOpts];
        _matchFinder = null;
        _rangeEncoder = new RangeEncoder();
        _isMatch = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
        _isRep = new short[Base.kNumStates];
        _isRepG0 = new short[Base.kNumStates];
        _isRepG1 = new short[Base.kNumStates];
        _isRepG2 = new short[Base.kNumStates];
        _isRep0Long = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
        reps = new int[Base.kNumRepDistances];
        repLens = new int[Base.kNumRepDistances];
        processedInSize = new long[1];
        processedOutSize = new long[1];
        _posEncoders = new short[Base.kNumFullDistances - Base.kEndPosModelIndex];
        _posAlignEncoder = new BitTreeEncoder(Base.kNumAlignBits);
        _lenEncoder = new LenPriceTableEncoder();
        _repMatchLenEncoder = new LenPriceTableEncoder();
        _literalEncoder = new LiteralEncoder();
        _matchDistances = new int[Base.kMatchMaxLen * 2 + 2];
        _matchFinderType = EMatchFinderTypeBT4;
        properties = new byte[kPropSize];
        tempPrices = new int[Base.kNumFullDistances];
        _numFastBytes = kNumFastBytesDefault;
        for (i = 0; i < kNumOpts; i++) {
            _optimum[i] = new Optimal();
        }
        for (i = 0; i < Base.kNumLenToPosStates; i++) {
            _posSlotEncoder[i] = new BitTreeEncoder(Base.kNumPosSlotBits);
        }
    }

    public void SetWriteEndMarkerMode(boolean writeEndMarker) {
        _writeEndMark = writeEndMarker;
    }

    private void Init() {
        int i;

        BaseInit();
        _rangeEncoder.Init();

        RangeEncoder.InitBitModels(_isMatch);
        RangeEncoder.InitBitModels(_isRep0Long);
        RangeEncoder.InitBitModels(_isRep);
        RangeEncoder.InitBitModels(_isRepG0);
        RangeEncoder.InitBitModels(_isRepG1);
        RangeEncoder.InitBitModels(_isRepG2);
        RangeEncoder.InitBitModels(_posEncoders);

        _literalEncoder.Init();
        for (i = 0; i < Base.kNumLenToPosStates; i++) {
            _posSlotEncoder[i].Init();
        }

        _lenEncoder.Init(1 << _posStateBits);
        _repMatchLenEncoder.Init(1 << _posStateBits);

        _posAlignEncoder.Init();

        _longestMatchWasFound = false;
        _optimumEndIndex = 0;
        _optimumCurrentIndex = 0;
        _additionalOffset = 0;
    }

    private int ReadMatchDistances() throws java.io.IOException {
        int lenRes = 0;

        _numDistancePairs = _matchFinder.GetMatches(_matchDistances);
        if (_numDistancePairs > 0) {
            lenRes = _matchDistances[_numDistancePairs - 2];
            if (lenRes == _numFastBytes) {
                lenRes += _matchFinder.GetMatchLen((int) lenRes - 1, _matchDistances[_numDistancePairs - 1],
                        Base.kMatchMaxLen - lenRes);
            }
        }
        _additionalOffset++;
        return lenRes;
    }

    private void MovePos(int num) throws java.io.IOException {

        if (num > 0) {
            _matchFinder.Skip(num);
            _additionalOffset += num;
        }
    }

    private int GetRepLen1Price(int state, int posState) {
        int Result;

        Result = RangeEncoder.GetPrice0(_isRepG0[state]) + RangeEncoder.GetPrice0(_isRep0Long[(state << Base.kNumPosStatesBitsMax) + posState]);
        return Result;
    }

    private int GetPureRepPrice(int repIndex, int state, int posState) {
        int price;

        if (repIndex == 0) {
            price = RangeEncoder.GetPrice0(_isRepG0[state]);
            price += RangeEncoder.GetPrice1(_isRep0Long[(state << Base.kNumPosStatesBitsMax) + posState]);
        } else {
            price = RangeEncoder.GetPrice1(_isRepG0[state]);
            if (repIndex == 1) {
                price += RangeEncoder.GetPrice0(_isRepG1[state]);
            } else {
                price += RangeEncoder.GetPrice1(_isRepG1[state]);
                price += RangeEncoder.GetPrice(_isRepG2[state], repIndex - 2);
            }
        }
        return price;
    }

    private int GetRepPrice(int repIndex, int len, int state, int posState) {
        int price, Result;

        price = _repMatchLenEncoder.GetPrice(len - Base.kMatchMinLen, posState);
        Result = price + GetPureRepPrice(repIndex, state, posState);
        return Result;
    }

    private int GetPosLenPrice(int pos, int len, int posState) {
        int price, lenToPosState, Result;

        lenToPosState = Base.GetLenToPosState(len);
        if (pos < Base.kNumFullDistances) {
            price = _distancesPrices[(lenToPosState * Base.kNumFullDistances) + pos];
        } else {
            price = _posSlotPrices[(lenToPosState << Base.kNumPosSlotBits) + GetPosSlot2(pos)] + _alignPrices[pos & Base.kAlignMask];
        }
        Result = price + _lenEncoder.GetPrice(len - Base.kMatchMinLen, posState);
        return Result;
    }

    private int Backward(int cur) {
        int posMem, backMem, posPrev, backCur;

        _optimumEndIndex = cur;
        posMem = _optimum[cur].PosPrev;
        backMem = _optimum[cur].BackPrev;
        do {
            if (_optimum[cur].Prev1IsChar) {
                _optimum[posMem].MakeAsChar();
                _optimum[posMem].PosPrev = posMem - 1;
                if (_optimum[cur].Prev2) {
                    _optimum[posMem - 1].Prev1IsChar = false;
                    _optimum[posMem - 1].PosPrev = _optimum[cur].PosPrev2;
                    _optimum[posMem - 1].BackPrev = _optimum[cur].BackPrev2;
                }
            }
            posPrev = posMem;
            backCur = backMem;
            backMem = _optimum[posPrev].BackPrev;
            posMem = _optimum[posPrev].PosPrev;
            _optimum[posPrev].BackPrev = backCur;
            _optimum[posPrev].PosPrev = cur;
            cur = posPrev;
        } while (cur > 0);
        backRes = _optimum[0].BackPrev;
        _optimumCurrentIndex = _optimum[0].PosPrev;
        return _optimumCurrentIndex;
    }

    private int GetOptimum(int position) throws IOException {
        int i, t, lenRes, lenMain, numDistancePairs, numAvailableBytes, repMaxIndex;
        int posState, matchPrice, repMatchPrice, shortRepPrice, lenEnd, len, repLen;
        int price, curAndLenPrice, normalMatchPrice, distance, cur, newLen, posPrev;
        int state, pos, curPrice, curAnd1Price, numAvailableBytesFull, lenTest2;
        int state2, posStateNext, nextRepMatchPrice, offset, startLen, repIndex, lenTest, lenTestTemp;
        int curAndLenCharPrice, nextMatchPrice, offs, curBack;
        byte currentByte, matchByte;
        Optimal optimum, opt, nextOptimum;
        boolean nextIsChar;

        if (_optimumEndIndex != _optimumCurrentIndex) {
            lenRes = _optimum[_optimumCurrentIndex].PosPrev - _optimumCurrentIndex;
            backRes = _optimum[_optimumCurrentIndex].BackPrev;
            _optimumCurrentIndex = _optimum[_optimumCurrentIndex].PosPrev;
            return lenRes;
        }
        _optimumCurrentIndex = _optimumEndIndex = 0;

        if (!_longestMatchWasFound) {
            lenMain = ReadMatchDistances();
        } else {
            lenMain = _longestMatchLength;
            _longestMatchWasFound = false;
        }
        numDistancePairs = _numDistancePairs;

        numAvailableBytes = _matchFinder.GetNumAvailableBytes() + 1;
        if (numAvailableBytes < 2) {
            backRes = -1;
            return 1;
        }
        if (numAvailableBytes > Base.kMatchMaxLen) {
            numAvailableBytes = Base.kMatchMaxLen;
        }

        repMaxIndex = 0;
        for (i = 0; i < Base.kNumRepDistances; i++) {
            reps[i] = _repDistances[i];
            repLens[i] = _matchFinder.GetMatchLen(0 - 1, reps[i], Base.kMatchMaxLen);
            if (repLens[i] > repLens[repMaxIndex]) {
                repMaxIndex = i;
            }
        }
        if (repLens[repMaxIndex] >= _numFastBytes) {
            backRes = repMaxIndex;
            lenRes = repLens[repMaxIndex];
            MovePos(lenRes - 1);
            return lenRes;
        }

        if (lenMain >= _numFastBytes) {
            backRes = _matchDistances[numDistancePairs - 1] + Base.kNumRepDistances;
            MovePos(lenMain - 1);
            return lenMain;
        }

        currentByte = _matchFinder.GetIndexByte(0 - 1);
        matchByte = _matchFinder.GetIndexByte(0 - _repDistances[0] - 1 - 1);

        if (lenMain < 2 && currentByte != matchByte && repLens[repMaxIndex] < 2) {
            backRes = -1;
            return 1;
        }

        _optimum[0].State = _state;

        posState = (position & _posStateMask);

        _optimum[1].Price = RangeEncoder.GetPrice0(_isMatch[(_state << Base.kNumPosStatesBitsMax) + posState])
                + _literalEncoder.GetSubCoder(position, _previousByte).GetPrice(!Base.StateIsCharState(_state), matchByte, currentByte);
        _optimum[1].MakeAsChar();

        matchPrice = RangeEncoder.GetPrice1(_isMatch[(_state << Base.kNumPosStatesBitsMax) + posState]);
        repMatchPrice = matchPrice + RangeEncoder.GetPrice1(_isRep[_state]);

        if (matchByte == currentByte) {
            shortRepPrice = repMatchPrice + GetRepLen1Price(_state, posState);
            if (shortRepPrice < _optimum[1].Price) {
                _optimum[1].Price = shortRepPrice;
                _optimum[1].MakeAsShortRep();
            }
        }

        lenEnd = ((lenMain >= repLens[repMaxIndex]) ? lenMain : repLens[repMaxIndex]);

        if (lenEnd < 2) {
            backRes = _optimum[1].BackPrev;
            return 1;
        }

        _optimum[1].PosPrev = 0;

        _optimum[0].Backs0 = reps[0];
        _optimum[0].Backs1 = reps[1];
        _optimum[0].Backs2 = reps[2];
        _optimum[0].Backs3 = reps[3];

        len = lenEnd;
        do {
            _optimum[len--].Price = kIfinityPrice;
        } while (len >= 2);

        for (i = 0; i < Base.kNumRepDistances; i++) {
            repLen = repLens[i];
            if (repLen < 2) {
                continue;
            }
            price = repMatchPrice + GetPureRepPrice(i, _state, posState);
            do {
                curAndLenPrice = price + _repMatchLenEncoder.GetPrice(repLen - 2, posState);
                optimum = _optimum[repLen];
                if (curAndLenPrice < optimum.Price) {
                    optimum.Price = curAndLenPrice;
                    optimum.PosPrev = 0;
                    optimum.BackPrev = i;
                    optimum.Prev1IsChar = false;
                }
            } while (--repLen >= 2);
        }

        normalMatchPrice = matchPrice + RangeEncoder.GetPrice0(_isRep[_state]);

        len = ((repLens[0] >= 2) ? repLens[0] + 1 : 2);
        if (len <= lenMain) {
            offs = 0;
            while (len > _matchDistances[offs]) {
                offs += 2;
            }
            for (;; len++) {
                distance = _matchDistances[offs + 1];
                curAndLenPrice = normalMatchPrice + GetPosLenPrice(distance, len, posState);
                optimum = _optimum[len];
                if (curAndLenPrice < optimum.Price) {
                    optimum.Price = curAndLenPrice;
                    optimum.PosPrev = 0;
                    optimum.BackPrev = distance + Base.kNumRepDistances;
                    optimum.Prev1IsChar = false;
                }
                if (len == _matchDistances[offs]) {
                    offs += 2;
                    if (offs == numDistancePairs) {
                        break;
                    }
                }
            }
        }

        cur = 0;

        while (true) {
            cur++;
            if (cur == lenEnd) {
                return Backward(cur);
            }
            newLen = ReadMatchDistances();
            numDistancePairs = _numDistancePairs;
            if (newLen >= _numFastBytes) {

                _longestMatchLength = newLen;
                _longestMatchWasFound = true;
                return Backward(cur);
            }
            position++;
            posPrev = _optimum[cur].PosPrev;
            if (_optimum[cur].Prev1IsChar) {
                posPrev--;
                if (_optimum[cur].Prev2) {
                    state = _optimum[_optimum[cur].PosPrev2].State;
                    if (_optimum[cur].BackPrev2 < Base.kNumRepDistances) {
                        state = Base.StateUpdateRep(state);
                    } else {
                        state = Base.StateUpdateMatch(state);
                    }
                } else {
                    state = _optimum[posPrev].State;
                }
                state = Base.StateUpdateChar(state);
            } else {
                state = _optimum[posPrev].State;
            }
            if (posPrev == cur - 1) {
                if (_optimum[cur].IsShortRep()) {
                    state = Base.StateUpdateShortRep(state);
                } else {
                    state = Base.StateUpdateChar(state);
                }
            } else {
                if (_optimum[cur].Prev1IsChar && _optimum[cur].Prev2) {
                    posPrev = _optimum[cur].PosPrev2;
                    pos = _optimum[cur].BackPrev2;
                    state = Base.StateUpdateRep(state);
                } else {
                    pos = _optimum[cur].BackPrev;
                    if (pos < Base.kNumRepDistances) {
                        state = Base.StateUpdateRep(state);
                    } else {
                        state = Base.StateUpdateMatch(state);
                    }
                }
                opt = _optimum[posPrev];
                if (pos < Base.kNumRepDistances) {
                    if (pos == 0) {
                        reps[0] = opt.Backs0;
                        reps[1] = opt.Backs1;
                        reps[2] = opt.Backs2;
                        reps[3] = opt.Backs3;
                    } else if (pos == 1) {
                        reps[0] = opt.Backs1;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs2;
                        reps[3] = opt.Backs3;
                    } else if (pos == 2) {
                        reps[0] = opt.Backs2;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs1;
                        reps[3] = opt.Backs3;
                    } else {
                        reps[0] = opt.Backs3;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs1;
                        reps[3] = opt.Backs2;
                    }
                } else {
                    reps[0] = (pos - Base.kNumRepDistances);
                    reps[1] = opt.Backs0;
                    reps[2] = opt.Backs1;
                    reps[3] = opt.Backs2;
                }
            }
            _optimum[cur].State = state;
            _optimum[cur].Backs0 = reps[0];
            _optimum[cur].Backs1 = reps[1];
            _optimum[cur].Backs2 = reps[2];
            _optimum[cur].Backs3 = reps[3];
            curPrice = _optimum[cur].Price;

            currentByte = _matchFinder.GetIndexByte(0 - 1);
            matchByte = _matchFinder.GetIndexByte(0 - reps[0] - 1 - 1);

            posState = (position & _posStateMask);

            curAnd1Price = curPrice + RangeEncoder.GetPrice0(_isMatch[(state << Base.kNumPosStatesBitsMax) + posState])
                    + _literalEncoder.GetSubCoder(position, _matchFinder.GetIndexByte(0 - 2)).
                    GetPrice(!Base.StateIsCharState(state), matchByte, currentByte);

            nextOptimum = _optimum[cur + 1];

            nextIsChar = false;
            if (curAnd1Price < nextOptimum.Price) {
                nextOptimum.Price = curAnd1Price;
                nextOptimum.PosPrev = cur;
                nextOptimum.MakeAsChar();
                nextIsChar = true;
            }

            matchPrice = curPrice + RangeEncoder.GetPrice1(_isMatch[(state << Base.kNumPosStatesBitsMax) + posState]);
            repMatchPrice = matchPrice + RangeEncoder.GetPrice1(_isRep[state]);

            if (matchByte == currentByte
                    && !(nextOptimum.PosPrev < cur && nextOptimum.BackPrev == 0)) {
                shortRepPrice = repMatchPrice + GetRepLen1Price(state, posState);
                if (shortRepPrice <= nextOptimum.Price) {
                    nextOptimum.Price = shortRepPrice;
                    nextOptimum.PosPrev = cur;
                    nextOptimum.MakeAsShortRep();
                    nextIsChar = true;
                }
            }

            numAvailableBytesFull = _matchFinder.GetNumAvailableBytes() + 1;
            numAvailableBytesFull = Math.min(kNumOpts - 1 - cur, numAvailableBytesFull);
            numAvailableBytes = numAvailableBytesFull;

            if (numAvailableBytes < 2) {
                continue;
            }
            if (numAvailableBytes > _numFastBytes) {
                numAvailableBytes = _numFastBytes;
            }
            if (!nextIsChar && matchByte != currentByte) {
                // try Literal + rep0
                t = Math.min(numAvailableBytesFull - 1, _numFastBytes);
                lenTest2 = _matchFinder.GetMatchLen(0, reps[0], t);
                if (lenTest2 >= 2) {
                    state2 = Base.StateUpdateChar(state);
                    posStateNext = (position + 1) & _posStateMask;
                    nextRepMatchPrice = curAnd1Price
                            + RangeEncoder.GetPrice1(_isMatch[(state2 << Base.kNumPosStatesBitsMax) + posStateNext])
                            + RangeEncoder.GetPrice1(_isRep[state2]);
                    {
                        offset = cur + 1 + lenTest2;
                        while (lenEnd < offset) {
                            _optimum[++lenEnd].Price = kIfinityPrice;
                        }
                        curAndLenPrice = nextRepMatchPrice + GetRepPrice(
                                0, lenTest2, state2, posStateNext);
                        optimum = _optimum[offset];
                        if (curAndLenPrice < optimum.Price) {
                            optimum.Price = curAndLenPrice;
                            optimum.PosPrev = cur + 1;
                            optimum.BackPrev = 0;
                            optimum.Prev1IsChar = true;
                            optimum.Prev2 = false;
                        }
                    }
                }
            }

            startLen = 2; // speed optimization 
            for (repIndex = 0; repIndex < Base.kNumRepDistances; repIndex++) {
                lenTest = _matchFinder.GetMatchLen(0 - 1, reps[repIndex], numAvailableBytes);
                if (lenTest < 2) {
                    continue;
                }
                lenTestTemp = lenTest;
                do {
                    while (lenEnd < cur + lenTest) {
                        _optimum[++lenEnd].Price = kIfinityPrice;
                    }
                    curAndLenPrice = repMatchPrice + GetRepPrice(repIndex, lenTest, state, posState);
                    optimum = _optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.Price) {
                        optimum.Price = curAndLenPrice;
                        optimum.PosPrev = cur;
                        optimum.BackPrev = repIndex;
                        optimum.Prev1IsChar = false;
                    }
                } while (--lenTest >= 2);
                lenTest = lenTestTemp;

                if (repIndex == 0) {
                    startLen = lenTest + 1;
                }

                // if (_maxMode)
                if (lenTest < numAvailableBytesFull) {
                    t = Math.min(numAvailableBytesFull - 1 - lenTest, _numFastBytes);
                    lenTest2 = _matchFinder.GetMatchLen(lenTest, reps[repIndex], t);
                    if (lenTest2 >= 2) {
                        state2 = Base.StateUpdateRep(state);
                        posStateNext = (position + lenTest) & _posStateMask;
                        curAndLenCharPrice
                                = repMatchPrice + GetRepPrice(repIndex, lenTest, state, posState)
                                + RangeEncoder.GetPrice0(_isMatch[(state2 << Base.kNumPosStatesBitsMax) + posStateNext])
                                + _literalEncoder.GetSubCoder(position + lenTest,
                                        _matchFinder.GetIndexByte(lenTest - 1 - 1)).GetPrice(true,
                                _matchFinder.GetIndexByte(lenTest - 1 - (reps[repIndex] + 1)),
                                _matchFinder.GetIndexByte(lenTest - 1));
                        state2 = Base.StateUpdateChar(state2);
                        posStateNext = (position + lenTest + 1) & _posStateMask;
                        nextMatchPrice = curAndLenCharPrice + RangeEncoder.GetPrice1(_isMatch[(state2 << Base.kNumPosStatesBitsMax) + posStateNext]);
                        nextRepMatchPrice = nextMatchPrice + RangeEncoder.GetPrice1(_isRep[state2]);

                        // for(; lenTest2 >= 2; lenTest2--)
                        {
                            offset = lenTest + 1 + lenTest2;
                            while (lenEnd < cur + offset) {
                                _optimum[++lenEnd].Price = kIfinityPrice;
                            }
                            curAndLenPrice = nextRepMatchPrice + GetRepPrice(0, lenTest2, state2, posStateNext);
                            optimum = _optimum[cur + offset];
                            if (curAndLenPrice < optimum.Price) {
                                optimum.Price = curAndLenPrice;
                                optimum.PosPrev = cur + lenTest + 1;
                                optimum.BackPrev = 0;
                                optimum.Prev1IsChar = true;
                                optimum.Prev2 = true;
                                optimum.PosPrev2 = cur;
                                optimum.BackPrev2 = repIndex;
                            }
                        }
                    }
                }
            }

            if (newLen > numAvailableBytes) {
                newLen = numAvailableBytes;
                for (numDistancePairs = 0; newLen > _matchDistances[numDistancePairs]; numDistancePairs += 2) ;
                _matchDistances[numDistancePairs] = newLen;
                numDistancePairs += 2;
            }
            if (newLen >= startLen) {
                normalMatchPrice = matchPrice + RangeEncoder.GetPrice0(_isRep[state]);
                while (lenEnd < cur + newLen) {
                    _optimum[++lenEnd].Price = kIfinityPrice;
                }

                offs = 0;
                while (startLen > _matchDistances[offs]) {
                    offs += 2;
                }

                for (lenTest = startLen;; lenTest++) {
                    curBack = _matchDistances[offs + 1];
                    curAndLenPrice = normalMatchPrice + GetPosLenPrice(curBack, lenTest, posState);
                    optimum = _optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.Price) {
                        optimum.Price = curAndLenPrice;
                        optimum.PosPrev = cur;
                        optimum.BackPrev = curBack + Base.kNumRepDistances;
                        optimum.Prev1IsChar = false;
                    }

                    if (lenTest == _matchDistances[offs]) {
                        if (lenTest < numAvailableBytesFull) {
                            t = Math.min(numAvailableBytesFull - 1 - lenTest, _numFastBytes);
                            lenTest2 = _matchFinder.GetMatchLen(lenTest, curBack, t);
                            if (lenTest2 >= 2) {
                                state2 = Base.StateUpdateMatch(state);
                                posStateNext = (position + lenTest) & _posStateMask;
                                curAndLenCharPrice = curAndLenPrice
                                        + RangeEncoder.GetPrice0(_isMatch[(state2 << Base.kNumPosStatesBitsMax) + posStateNext])
                                        + _literalEncoder.GetSubCoder(position + lenTest,
                                                _matchFinder.GetIndexByte(lenTest - 1 - 1)).
                                        GetPrice(true,
                                                _matchFinder.GetIndexByte(lenTest - (curBack + 1) - 1),
                                                _matchFinder.GetIndexByte(lenTest - 1));
                                state2 = Base.StateUpdateChar(state2);
                                posStateNext = (position + lenTest + 1) & _posStateMask;
                                nextMatchPrice = curAndLenCharPrice + RangeEncoder.GetPrice1(_isMatch[(state2 << Base.kNumPosStatesBitsMax) + posStateNext]);
                                nextRepMatchPrice = nextMatchPrice + RangeEncoder.GetPrice1(_isRep[state2]);
                                offset = lenTest + 1 + lenTest2;
                                while (lenEnd < cur + offset) {
                                    _optimum[++lenEnd].Price = kIfinityPrice;
                                }
                                curAndLenPrice = nextRepMatchPrice + GetRepPrice(0, lenTest2, state2, posStateNext);
                                optimum = _optimum[cur + offset];
                                if (curAndLenPrice < optimum.Price) {
                                    optimum.Price = curAndLenPrice;
                                    optimum.PosPrev = cur + lenTest + 1;
                                    optimum.BackPrev = 0;
                                    optimum.Prev1IsChar = true;
                                    optimum.Prev2 = true;
                                    optimum.PosPrev2 = cur;
                                    optimum.BackPrev2 = curBack + Base.kNumRepDistances;
                                }
                            }
                        }
                        offs += 2;
                        if (offs == numDistancePairs) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean ChangePair(int smallDist, int bigDist) {
        int kDif = 7;

        return (smallDist < (1 << (32 - kDif)) && bigDist >= (smallDist << kDif));
    }

    private void WriteEndMarker(int posState) throws IOException {
        int len, posSlot, lenToPosState, footerBits, posReduced;

        if (!_writeEndMark) {
            return;
        }
        _rangeEncoder.Encode(_isMatch, (_state << Base.kNumPosStatesBitsMax) + posState, 1);
        _rangeEncoder.Encode(_isRep, _state, 0);
        _state = Base.StateUpdateMatch(_state);
        len = Base.kMatchMinLen;
        _lenEncoder.Encode(_rangeEncoder, len - Base.kMatchMinLen, posState);
        posSlot = (1 << Base.kNumPosSlotBits) - 1;
        lenToPosState = Base.GetLenToPosState(len);
        _posSlotEncoder[lenToPosState].Encode(_rangeEncoder, posSlot);
        footerBits = 30;
        posReduced = (1 << footerBits) - 1;
        _rangeEncoder.EncodeDirectBits(posReduced >> Base.kNumAlignBits, footerBits - Base.kNumAlignBits);
        _posAlignEncoder.ReverseEncode(_rangeEncoder, posReduced & Base.kAlignMask);
    }

    private void Flush(int nowPos) throws IOException {

        ReleaseMFStream();
        WriteEndMarker(nowPos & _posStateMask);
        _rangeEncoder.FlushData();
        _rangeEncoder.FlushStream();
    }

    /**
     * Codifica un bloque de datos del stream
     *
     * @param inSize
     * @param outSize
     * @param totalInSize
     * @param Progress
     * @return TRUE si finalizo o FALSE si no
     * @throws IOException
     */
    public boolean CodeOneBlock(long[] inSize, long[] outSize, long totalInSize, BTLZMA Progress) throws IOException {
        long progressPosValuePrev;
        byte curByte, matchByte;
        int len, pos, posState, complexState, distance, posSlot, lenToPosState;
        int footerBits, baseVal, posReduced, i;
        boolean finished = true;
        int lProgress;
        boolean lInterrup;

        lProgress = 0;
        inSize[0] = 0;
        outSize[0] = 0;
        if (_inStream != null) {
            _matchFinder.SetStream(_inStream);
            _matchFinder.Init();
            _needReleaseMFStream = true;
            _inStream = null;
        }
        if (_finished) {
            return finished;
        }
        _finished = true;
        progressPosValuePrev = nowPos64;
        if (nowPos64 == 0) {
            if (_matchFinder.GetNumAvailableBytes() == 0) {
                Flush((int) nowPos64);
                return finished;
            }

            ReadMatchDistances();
            posState = (int) (nowPos64) & _posStateMask;
            _rangeEncoder.Encode(_isMatch, (_state << Base.kNumPosStatesBitsMax) + posState, 0);
            _state = Base.StateUpdateChar(_state);
            curByte = _matchFinder.GetIndexByte(0 - _additionalOffset);
            _literalEncoder.GetSubCoder((int) (nowPos64), _previousByte).Encode(_rangeEncoder, curByte);
            _previousByte = curByte;
            _additionalOffset--;
            nowPos64++;
        }
        if (_matchFinder.GetNumAvailableBytes() == 0) {
            Flush((int) nowPos64);
            return finished;
        }
        lInterrup = false;
        while (!lInterrup) {
            len = GetOptimum((int) nowPos64);
            pos = backRes;
            posState = ((int) nowPos64) & _posStateMask;
            complexState = (_state << Base.kNumPosStatesBitsMax) + posState;
            if (len == 1 && pos == -1) {
                _rangeEncoder.Encode(_isMatch, complexState, 0);
                curByte = _matchFinder.GetIndexByte((int) (0 - _additionalOffset));
                SubEncoder subCoder = _literalEncoder.GetSubCoder((int) nowPos64, _previousByte);
                if (!Base.StateIsCharState(_state)) {
                    matchByte = _matchFinder.GetIndexByte((int) (0 - _repDistances[0] - 1 - _additionalOffset));
                    subCoder.EncodeMatched(_rangeEncoder, matchByte, curByte);
                } else {
                    subCoder.Encode(_rangeEncoder, curByte);
                }
                _previousByte = curByte;
                _state = Base.StateUpdateChar(_state);
            } else {
                _rangeEncoder.Encode(_isMatch, complexState, 1);
                if (pos < Base.kNumRepDistances) {
                    _rangeEncoder.Encode(_isRep, _state, 1);
                    if (pos == 0) {
                        _rangeEncoder.Encode(_isRepG0, _state, 0);
                        if (len == 1) {
                            _rangeEncoder.Encode(_isRep0Long, complexState, 0);
                        } else {
                            _rangeEncoder.Encode(_isRep0Long, complexState, 1);
                        }
                    } else {
                        _rangeEncoder.Encode(_isRepG0, _state, 1);
                        if (pos == 1) {
                            _rangeEncoder.Encode(_isRepG1, _state, 0);
                        } else {
                            _rangeEncoder.Encode(_isRepG1, _state, 1);
                            _rangeEncoder.Encode(_isRepG2, _state, pos - 2);
                        }
                    }
                    if (len == 1) {
                        _state = Base.StateUpdateShortRep(_state);
                    } else {
                        _repMatchLenEncoder.Encode(_rangeEncoder, len - Base.kMatchMinLen, posState);
                        _state = Base.StateUpdateRep(_state);
                    }
                    distance = _repDistances[pos];
                    if (pos != 0) {
                        for (i = pos; i >= 1; i--) {
                            _repDistances[i] = _repDistances[i - 1];
                        }
                        _repDistances[0] = distance;
                    }
                } else {
                    _rangeEncoder.Encode(_isRep, _state, 0);
                    _state = Base.StateUpdateMatch(_state);
                    _lenEncoder.Encode(_rangeEncoder, len - Base.kMatchMinLen, posState);
                    pos -= Base.kNumRepDistances;
                    posSlot = GetPosSlot(pos);
                    lenToPosState = Base.GetLenToPosState(len);
                    _posSlotEncoder[lenToPosState].Encode(_rangeEncoder, posSlot);

                    if (posSlot >= Base.kStartPosModelIndex) {
                        footerBits = (int) ((posSlot >> 1) - 1);
                        baseVal = ((2 | (posSlot & 1)) << footerBits);
                        posReduced = pos - baseVal;

                        if (posSlot < Base.kEndPosModelIndex) {
                            BitTreeEncoder.ReverseEncode(_posEncoders,
                                    baseVal - posSlot - 1, _rangeEncoder, footerBits, posReduced);
                        } else {
                            _rangeEncoder.EncodeDirectBits(posReduced >> Base.kNumAlignBits, footerBits - Base.kNumAlignBits);
                            _posAlignEncoder.ReverseEncode(_rangeEncoder, posReduced & Base.kAlignMask);
                            _alignPriceCount++;
                        }
                    }
                    distance = pos;
                    for (i = Base.kNumRepDistances - 1; i >= 1; i--) {
                        _repDistances[i] = _repDistances[i - 1];
                    }
                    _repDistances[0] = distance;
                    _matchPriceCount++;
                }
                _previousByte = _matchFinder.GetIndexByte(len - 1 - _additionalOffset);
            }
            _additionalOffset -= len;
            nowPos64 += len;
            if (_additionalOffset == 0) {
                // if (!_fastMode)
                if (_matchPriceCount >= (1 << 7)) {
                    FillDistancesPrices();
                }
                if (_alignPriceCount >= Base.kAlignTableSize) {
                    FillAlignPrices();
                }
                inSize[0] = nowPos64;
                outSize[0] = _rangeEncoder.GetProcessedSizeAdd();
                if (_matchFinder.GetNumAvailableBytes() == 0) {
                    Flush((int) nowPos64);
                    return finished;
                }
                if (nowPos64 - progressPosValuePrev >= (1 << 12)) {
                    _finished = false;
                    finished = false;
                    return finished;
                }
            }
            if (Progress != null) {
                if (totalInSize > 0) {
                    lProgress = (int) (100 * ((double) nowPos64 / (double) totalInSize));
                    Progress.setProgress(lProgress);
                }
                lInterrup = Progress.isInterrupted();
            }
        }
        return finished;
    }

    private void ReleaseMFStream() {

        if (_matchFinder != null && _needReleaseMFStream) {
            _matchFinder.ReleaseStream();
            _needReleaseMFStream = false;
        }
    }

    void SetOutStream(OutputStream outStream) {

        _rangeEncoder.SetStream(outStream);
    }

    void ReleaseOutStream() {

        _rangeEncoder.ReleaseStream();
    }

    void ReleaseStreams() {

        ReleaseMFStream();
        ReleaseOutStream();
    }

    void SetStreams(InputStream inStream, OutputStream outStream) {

        _inStream = inStream;
        _finished = false;
        Create();
        SetOutStream(outStream);
        Init();
        FillDistancesPrices();
        FillAlignPrices();
        _lenEncoder.SetTableSize(_numFastBytes + 1 - Base.kMatchMinLen);
        _lenEncoder.UpdateTables(1 << _posStateBits);
        _repMatchLenEncoder.SetTableSize(_numFastBytes + 1 - Base.kMatchMinLen);
        _repMatchLenEncoder.UpdateTables(1 << _posStateBits);
        nowPos64 = 0;
    }

    /**
     * Realiza la compresion del stream de entrada en el stream de salida
     *
     * @param inStream Stream de Entrada
     * @param outStream Stream de Salida
     * @param inSize Dimension de la entrada
     * @param Progress Apuntado para actualizar el progreso
     * @throws IOException
     */
    public void Code(InputStream inStream, OutputStream outStream, long inSize, BTLZMA Progress) throws IOException {
        boolean lInterrup;

        _needReleaseMFStream = false;
        lInterrup = false;
        try {
            SetStreams(inStream, outStream);
            while ((!lInterrup) && (!CodeOneBlock(processedInSize, processedOutSize, inSize, Progress))) {
                if (Progress != null) {
                    lInterrup = Progress.isInterrupted();
                }
            }
        } finally {
            ReleaseStreams();
            if (Progress != null) {
                Progress.setProgress(100);
            }
        }
    }

    /**
     * Escribe los parametros del LZMA en el OutputStream
     *
     * @param outStream OutputStream donde escribir
     * @throws IOException
     */
    public void WriteCoderProperties(OutputStream outStream) throws IOException {
        int i;

        properties[0] = (byte) ((_posStateBits * 5 + _numLiteralPosStateBits) * 9 + _numLiteralContextBits);
        for (i = 0; i < 4; i++) {
            properties[1 + i] = (byte) (_dictionarySize >> (8 * i));
        }
        outStream.write(properties, 0, kPropSize);
    }

    private void FillDistancesPrices() {
        int i, posSlot, footerBits, baseVal, lenToPosState, st, st2;

        for (i = Base.kStartPosModelIndex; i < Base.kNumFullDistances; i++) {
            posSlot = GetPosSlot(i);
            footerBits = (int) ((posSlot >> 1) - 1);
            baseVal = ((2 | (posSlot & 1)) << footerBits);
            tempPrices[i] = BitTreeEncoder.ReverseGetPrice(_posEncoders,
                    baseVal - posSlot - 1, footerBits, i - baseVal);
        }

        for (lenToPosState = 0; lenToPosState < Base.kNumLenToPosStates; lenToPosState++) {
            BitTreeEncoder encoder = _posSlotEncoder[lenToPosState];
            st = (lenToPosState << Base.kNumPosSlotBits);
            for (posSlot = 0; posSlot < _distTableSize; posSlot++) {
                _posSlotPrices[st + posSlot] = encoder.GetPrice(posSlot);
            }
            for (posSlot = Base.kEndPosModelIndex; posSlot < _distTableSize; posSlot++) {
                _posSlotPrices[st + posSlot] += ((((posSlot >> 1) - 1) - Base.kNumAlignBits) << RangeEncoder.kNumBitPriceShiftBits);
            }
            st2 = lenToPosState * Base.kNumFullDistances;
            for (i = 0; i < Base.kStartPosModelIndex; i++) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + i];
            }
            for (; i < Base.kNumFullDistances; i++) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + GetPosSlot(i)] + tempPrices[i];
            }
        }
        _matchPriceCount = 0;
    }

    void FillAlignPrices() {
        int i;

        for (i = 0; i < Base.kAlignTableSize; i++) {
            _alignPrices[i] = _posAlignEncoder.ReverseGetPrice(i);
        }
        _alignPriceCount = 0;
    }

    /**
     * Establece el valor del diccionario entre 1 y 536870912 bytes
     *
     * @param dictionarySize valor del diccionario entre 1 y 536870912 bytes
     * @return TRUE si logro establecer el valor del diccionario o FALSE si no
     */
    public boolean SetDictionarySize(int dictionarySize) {
        int dicLogSize, minDicSize, MaxDicSize;

        minDicSize = 1 << Base.kDicLogSizeMin;
        MaxDicSize = 1 << Base.kDicLogSizeMax;
        if ((dictionarySize < minDicSize) || (dictionarySize > MaxDicSize)) {
            return false;
        }
        _dictionarySize = dictionarySize;
        for (dicLogSize = 0; dictionarySize > (1 << dicLogSize); dicLogSize++);
        _distTableSize = dicLogSize * 2;
        return true;
    }

    /**
     * Establece el numero de FastBytes
     *
     * @param numFastBytes Numero de FastBytes
     * @return TRUE si lo logro establecer o FALSE si no
     */
    public boolean SetNumFastBytes(int numFastBytes) {

        if (numFastBytes < 5 || numFastBytes > Base.kMatchMaxLen) {
            return false;
        }
        _numFastBytes = numFastBytes;
        return true;
    }

    /**
     * Establece el valor del Match Finder que debe ser entre 0 y 2
     *
     * @param matchFinderIndex Valor del Match Finder [0,2]
     * @return TRUE si logro establecerlo o FALSE si no
     */
    public boolean SetMatchFinder(int matchFinderIndex) {
        int matchFinderIndexPrev;

        if (matchFinderIndex < 0 || matchFinderIndex > 2) {
            return false;
        }
        matchFinderIndexPrev = _matchFinderType;
        _matchFinderType = matchFinderIndex;
        if (_matchFinder != null && matchFinderIndexPrev != _matchFinderType) {
            _dictionarySizePrev = -1;
            _matchFinder = null;
        }
        return true;
    }

    /**
     * Establece los parametros LC,LP y PB del algoritmo de compresion. El valor
     * de lc,lp,pb bytes ess lc + lp * 9 + pb * 9 * 5, donde:
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

        if (lp < 0 || lp > Base.kNumLitPosStatesBitsEncodingMax
                || lc < 0 || lc > Base.kNumLitContextBitsMax
                || pb < 0 || pb > Base.kNumPosStatesBitsEncodingMax) {
            return false;
        }
        _numLiteralPosStateBits = lp;
        _numLiteralContextBits = lc;
        _posStateBits = pb;
        _posStateMask = ((1) << _posStateBits) - 1;
        return true;
    }

    /**
     * Establece si se agrega el marcador de final de stream
     *
     * @param endMarkerMode TRUE si se agrega o FALSE si no
     */
    public void SetEndMarkerMode(boolean endMarkerMode) {
        _writeEndMark = endMarkerMode;
    }
}
