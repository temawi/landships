package gg.landships;

import org.json.simple.JSONObject;

public class NetHitMessageBuilder {
    JSONObject object;
    float hitId;
    float damage;

    NetHitMessageBuilder(float hitId, float damage) {
        object = new JSONObject();

        this.hitId = hitId;
        this.damage = damage;
    }

    public String build() {
        object.put("hit_id", this.hitId);
        object.put("damage", this.damage);
        object.put("type", 2);
        return object.toJSONString();
    }
}
