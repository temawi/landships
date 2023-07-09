package gg.landships;

import org.json.simple.JSONObject;

public class NetUpgradeMessageBuilder {
    JSONObject object;
    int uFirepower;
    int uArmor;
    int uHandling;
    int uMobility;
    int uOptics;

    NetUpgradeMessageBuilder(int f, int a, int h, int m, int o) {
        object = new JSONObject();

        uFirepower = f;
        uArmor = a;
        uHandling = h;
        uMobility = m;
        uOptics = o;
    }

    public String build() {
        object.put("upgrade_firepower", uFirepower);
        object.put("upgrade_armor", uArmor);
        object.put("upgrade_handling", uHandling);
        object.put("upgrade_mobility", uMobility);
        object.put("upgrade_optics", uOptics);
        object.put("type", 4);

        String string = object.toJSONString();
        Game.handler.outBytes += string.length();
        return string;
    }
}
