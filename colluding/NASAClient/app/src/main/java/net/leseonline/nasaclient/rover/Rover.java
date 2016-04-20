package net.leseonline.nasaclient.rover;

/**
 * Created by mlese on 4/20/2016.
 */
public class Rover {
    private int id;
    private String name;
    private String landing_date;
    private int max_sol;
    private String max_date;
    private int total_photos;
    private Camera[] cameras;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanding_date() {
        return landing_date;
    }

    public void setLanding_date(String landing_date) {
        this.landing_date = landing_date;
    }

    public int getMax_sol() {
        return max_sol;
    }

    public void setMax_sol(int max_sol) {
        this.max_sol = max_sol;
    }

    public String getMax_date() {
        return max_date;
    }

    public void setMax_date(String max_date) {
        this.max_date = max_date;
    }

    public int getTotal_photos() {
        return total_photos;
    }

    public void setTotal_photos(int total_photos) {
        this.total_photos = total_photos;
    }

    public Camera[] getCameras() {
        return cameras;
    }

    public void setCameras(Camera[] cameras) {
        this.cameras = cameras;
    }
}
