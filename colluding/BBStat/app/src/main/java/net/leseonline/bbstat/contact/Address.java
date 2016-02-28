package net.leseonline.bbstat.contact;

/**
 * Created by mvlese on 2/21/2016.
 */
public class Address {

    public String getAdddress1() {
        return adddress1;
    }

    public void setAdddress1(String adddress1) {
        this.adddress1 = adddress1;
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

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("address1:" + adddress1);
        sb.append(",address2:" + address2);
        sb.append(",city:" + city);
        sb.append(",state:" + state);
        sb.append(",zip:" + zip);
        sb.append("}");
        return sb.toString();
    }

    private String adddress1 = "";
    private String address2 = "";
    private String city = "";
    private String state = "";
    private String zip = "";
}
