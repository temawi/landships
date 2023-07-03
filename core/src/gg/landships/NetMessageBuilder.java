package gg.landships;

import org.json.simple.JSONObject;

public class NetMessageBuilder {
    JSONObject object;
    float x;
    float y;
    float r;
    float tr;

    NetMessageBuilder(float x, float y, float r, float tr) {
        object = new JSONObject();

        this.x = x;
        this.y = y;
        this.r = r;
        this.tr = tr;
    }

    public String build() {
        object.put("posx", x);
        object.put("posy", y);
        object.put("rot", r);
        object.put("trot", tr);
        object.put("type", 1);
        return object.toJSONString();
    }
}
