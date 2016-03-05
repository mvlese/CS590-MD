package net.leseonline.bbstat.contact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mvlese on 2/21/2016.
 */
public class Address {

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject contact = new JSONObject();
        contact.put("address1", address1);
        contact.put("address2", address2);
        contact.put("city", city);
        contact.put("state", state);
        contact.put("zip", zip);
        return contact;
    }

    private String address1 = "";
    private String address2 = "";
    private String city = "";
    private String state = "";
    private String zip = "";
}
