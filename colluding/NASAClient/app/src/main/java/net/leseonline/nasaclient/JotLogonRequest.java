package net.leseonline.nasaclient;

import org.json.JSONException;
import org.json.JSONObject;

public class JotLogonRequest extends JotRequest {

    public JotLogonRequest(String username, String password) throws JSONException {
        super("logon");
        this.username = username;
        this.password = password;
    }

    public String toJSONString() throws JSONException {
        JSONObject master = this.getJson();
        JSONObject local;
        local = new JSONObject()
                .put("username", username)
                .put("password", password);
        master.put("args", local);

        return master.toString();
    }

    private String username;
    private String password;
}
