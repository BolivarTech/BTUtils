package com.bolivartech.utils.lzma.support;

public class Optimal {

    public int State;

    public boolean Prev1IsChar;
    public boolean Prev2;

    public int PosPrev2;
    public int BackPrev2;

    public int Price;
    public int PosPrev;
    public int BackPrev;

    public int Backs0;
    public int Backs1;
    public int Backs2;
    public int Backs3;

    public void MakeAsChar() {
        BackPrev = -1;
        Prev1IsChar = false;
    }

    public void MakeAsShortRep() {
        BackPrev = 0;;
        Prev1IsChar = false;
    }

    public boolean IsShortRep() {
        return (BackPrev == 0);
    }
}
