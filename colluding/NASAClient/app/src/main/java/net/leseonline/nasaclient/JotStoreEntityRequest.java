package net.leseonline.nasaclient;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class JotStoreEntityRequest extends JotRequest {

    public JotStoreEntityRequest(String token, String key, String entityText) throws JSONException {
        super("storeEntity");
        this.token = token;
        this.key = key;
        this.entityText = entityText;
    }

    @Override
    public String toJSONString() throws JSONException {
        JSONObject master = this.getJson();

        JSONArray arr = new JSONArray()
                .put(new JSONObject()
                                .put("itemid", "1")
                                .put("itemtype", "text")
                                .put("annotation", entityText)
                );

        JSONObject entity = new JSONObject()
                .put("key", key)
                .put("items", arr);

        JSONObject local;
        local = new JSONObject()
                .put("token", token)
                .put("entity", entity);
        master.put("args", local);

        return master.toString();
    }

    private String token;
    private String key;
    private String entityText;
}
