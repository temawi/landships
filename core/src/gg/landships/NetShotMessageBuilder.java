package gg.landships;

import com.badlogic.gdx.math.Vector2;
import org.json.simple.JSONObject;

public class NetShotMessageBuilder {
    JSONObject object;
    Vector2 dir;
    float startx;
    float starty;
    float player;
    float spd;

    NetShotMessageBuilder(Vector2 dir, float startx, float starty, float player, float spd) {
        object = new JSONObject();

        this.dir = dir;
        this.startx = startx;
        this.starty = starty;
        this.player = player;
        this.spd = spd;
    }

    public String build() {
        object.put("dirx", this.dir.x);
        object.put("diry", this.dir.y);
        object.put("startx", this.startx);
        object.put("starty", this.starty);
        object.put("player", this.player);
        object.put("spd", this.spd);
        object.put("type", 3);

        String string = object.toJSONString();
        Game.handler.outBytes += string.length();
        return string;
    }
}
