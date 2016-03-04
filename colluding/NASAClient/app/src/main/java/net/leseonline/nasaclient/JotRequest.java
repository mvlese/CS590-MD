package net.leseonline.nasaclient;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JotRequest {

    public JotRequest(String method) throws JSONException {
        this.method = method;
        json = new JSONObject()
                .put("method", method);
    }

    public String getMethod() {
        return method;
    }

    public abstract String toJSONString() throws JSONException;

    protected JSONObject getJson() {
        return json;
    }

    private String method;
    private JSONObject json;
}
