package net.leseonline.bbstat;

/**
 * Created by mvlese on 2/20/2016.
 */
public class BasketballStatistics {
    private Statistic twoPoints;
    private Statistic twoPointAtempts;
    private Statistic threePoints;
    private Statistic threePointAttempts;
    private Statistic freeThrows;
    private Statistic freeThrowAttempts;
    private Statistic assists;
    private Statistic fouls;
    private Statistic rebounds;

    public BasketballStatistics() {

    }

    public Statistic getTwoPoints() {
        return twoPoints;
    }

    public Statistic getTwoPointAtempts() {
        return twoPointAtempts;
    }

    public Statistic getThreePoints() {
        return threePoints;
    }

    public Statistic getThreePointAttempts() {
        return threePointAttempts;
    }

    public Statistic getFreeThrows() {
        return freeThrows;
    }

    public Statistic getFreeThrowAttempts() {
        return freeThrowAttempts;
    }

    public Statistic getAssists() {
        return assists;
    }

    public Statistic getFouls() {
        return fouls;
    }

    public Statistic getRebounds() {
        return rebounds;
    }

}
