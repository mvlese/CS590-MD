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
    }

    private Address address = new Address();
    private String name = "";
    private String phoneNumber = "";
    private String email = "";

}
