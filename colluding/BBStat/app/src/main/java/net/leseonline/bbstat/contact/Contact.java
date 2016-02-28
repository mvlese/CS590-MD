package net.leseonline.bbstat.contact;

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

    public String toJason() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("name:" + name);
        sb.append(",address:" + address.toJson());
        sb.append(",phoneNumber:" + phoneNumber);
        sb.append(",email:" + email);
        sb.append("}");
        return sb.toString();
    }

    private Address address = new Address();
    private String name = "";
    private String phoneNumber = "";
    private String email = "";

}
