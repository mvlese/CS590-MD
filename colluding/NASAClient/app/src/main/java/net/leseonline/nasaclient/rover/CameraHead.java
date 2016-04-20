package net.leseonline.nasaclient.rover;

/**
 * Created by mlese on 4/20/2016.
 */
public class CameraHead extends Camera {
    private int id;
    private int rover_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRover_id() {
        return rover_id;
    }

    public void setRover_id(int rover_id) {
        this.rover_id = rover_id;
    }
}
