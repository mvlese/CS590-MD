package net.leseonline.bbstat.contact;

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

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(toQuoted("address1", address1));
        sb.append(",");
        sb.append(toQuoted("address2", address2));
        sb.append(",");
        sb.append(toQuoted("city", city));
        sb.append(",");
        sb.append(toQuoted("state", state));
        sb.append(",");
        sb.append(toQuoted("zip", zip));
        sb.append("}");
        return sb.toString();
    }

    private String toQuoted(String name, String value) {
        return "\"" + name + "\" : \"" + value + "\"";
    }

    private String address1 = "";
    private String address2 = "";
    private String city = "";
    private String state = "";
    private String zip = "";
}
