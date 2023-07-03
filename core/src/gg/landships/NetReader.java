package gg.landships;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class NetReader implements Runnable {
    NetHandler handler;

    NetReader(NetHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while(true) {
            try {
                String line = handler.reader.readLine();
                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(line);

                int msgType = ((Long)object.get("type")).intValue();
                int msgId = ((Long)object.get("id")).intValue();

                switch (msgType) {
                    case 0:
                        if(Game.tank == null) {
                            Game.tank = Game.tanks.get(msgId);
                            System.out.println("NetReader: Set Game.tank");
                        }
                        break;
                    case 1:
                        float posX = ((Double)object.get("posx")).floatValue();
                        float posY = ((Double)object.get("posy")).floatValue();
                        float rot = ((Double)object.get("rot")).floatValue();
                        float trot = ((Double)object.get("trot")).floatValue();

                        Tank t = Game.tanks.get(msgId);
                        t.sprite.setPosition(posX, posY);
                        t.sprite.setRotation(rot);
                        t.turret.sprite.setRotation(trot);

                        break;
                    case 2:
                        int hitId = ((Double)object.get("hit_id")).intValue();
                        float damage = ((Double)object.get("damage")).floatValue();

                        Game.tanks.get(hitId).hp -= damage;
                }
            } catch (IOException e) {
                System.out.println("NetReader: IOException");
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
