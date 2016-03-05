package net.leseonline.bbstat.contact;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mvlese on 2/20/2016.
 */
public class Contact {

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject toJason() throws JSONException {
        JSONObject contact = new JSONObject();
        contact.put("name", name);
//        contact.put("address", address.toJson());
        contact.put("phoneNumber", phoneNumber);
        contact.put("email", email);
        return contact;
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        sb.append(toQuoted("name", name));
//        sb.append(",");
//        sb.append("\"address\":" + address.toJson());
//        sb.append(",");
//        sb.append(toQuoted("phoneNumber", phoneNumber));
//        sb.append(",");
//        sb.append(toQuoted("email", email));
//        sb.append("}");
//        return sb.toString();
    }

    private String toQuoted(String name, String value) {
        return "\"" + name + "\" : \"" + value + "\"";
    }

    private Address address = new Address();
    private String name = "";
    private String phoneNumber = "";
    private String email = "";

}
