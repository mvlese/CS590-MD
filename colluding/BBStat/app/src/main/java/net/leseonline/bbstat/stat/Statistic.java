package net.leseonline.bbstat.stat;

/**
 * Created by mvlese on 2/20/2016.
 */
public class Statistic {
    private int value;

    public Statistic() {
        value = 0;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public int getValue() {
        return value;
    }
}
