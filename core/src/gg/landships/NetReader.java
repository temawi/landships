package gg.landships;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
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
                handler.inBytes += line.length();

                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(line);

                int msgType = ((Long)object.get("type")).intValue();
                final int msgId = ((Long)object.get("id")).intValue();

                switch (msgType) {
                    case 0:
                        if(Game.tank == null) {
                            Game.tank = Game.tanks.get(msgId);
                            System.out.println("NetReader: Set Game.tank");

                            // init UI now that the Game.tank is set
                            UIManager.updateUI();
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
                        break;
                    case 3:
                        final Vector2 dir = new Vector2(
                                ((Double)object.get("dirx")).floatValue(),
                                ((Double)object.get("diry")).floatValue()
                        );

                        final float startx = ((Double)object.get("startx")).floatValue();
                        final float starty = ((Double)object.get("starty")).floatValue();
                        final int player = ((Double)object.get("player")).intValue();
                        final float spd = ((Double)object.get("spd")).floatValue();

                        if (Game.tanks.get(player) != Game.tank) {
                            // run this in the main thread with the GL context
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    Game.renderList.add(new Shell(dir, 0, startx, starty, spd, Game.tanks.get(msgId)));
                                }
                            });
                        }

                        break;
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
