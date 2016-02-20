package net.leseonline.bbstat;

/**
 * Created by mvlese on 2/20/2016.
 */
public class Statistic {
    private int value;

    public Statistic() {
        value = 0;
    }

    public void add() {
        value++;
    }

    public void subtract() {
        value--;
    }

    public int getValue() {
        return value;
    }
}
