package gg.landships;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class UIManager {
    static String text = "";
    static String text2 = "";

    static BitmapFont font;
    static SpriteBatch batch;

    public static void init() {
        font = new BitmapFont();
        batch = new SpriteBatch();
    }

    public static void updateUI() {
        // init the text variable
        text = "";
        text2 = "";

        // add all the UI elements for the top of the screen
        // right now, we have fps, hp, damage, traffic (in/out), dispersion,
        // top speed, and camera zoom
        text = addText("fps: " + Gdx.graphics.getFramesPerSecond(), text);
        text = addText("dmg: " + (int)Game.tank.damage + "-" + (int)Game.tank.maxDamage, text);
        text = addText("disp: " + (int)Game.tank.dispersion, text);
        text = addText("speed: " + (int)Game.tank.speed * 100, text);

        text2 = addText("zoom: " + (int)Game.camera.zoom, text2);
        text2 = addText("in: " + Game.handler.inBytes, text2);
        text2 = addText("out: " + Game.handler.outBytes, text2);
        text2 = addText("mem: " + (((int)Runtime.getRuntime().totalMemory() - (int)Runtime.getRuntime().freeMemory())/1000) + " kb", text2);
        text2 = addText("pos: " + (int)Game.netPos.x + ", " + (int)Game.netPos.y, text2);
    }

    public static void drawReloadBar() {
        // create a shape renderer
        ShapeRenderer debugRenderer = new ShapeRenderer();

        // do some MATHS to find out how much to remove from the bar each frame
        float pixPerTick = Gdx.graphics.getWidth() / (Game.tank.reloadTime * 60f);

        // find the new bar width given reload progress and pixPerTick variable
        float barWidth = pixPerTick * (Game.tank.nextShot - Game.tank.ticks);

        // draw it
        debugRenderer.setProjectionMatrix(Game.stage.getCamera().combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(Color.YELLOW);
        debugRenderer.box(0, 0, 0, barWidth, 10, 1);
        debugRenderer.end();
    }

    public static String addText(String s, String t) {
        // add text cleanly with formatting

        if(s == null)
            return null;

        return t.concat("/ " + s);
    }

    public static void drawTextUI() {
        // set its size
        font.getData().setScale(1f);

        // draw it
        batch.setProjectionMatrix(Game.stage.getCamera().combined);
        batch.begin();
        font.draw(batch, text, 0, Gdx.graphics.getHeight() - 16);
        font.draw(batch, text2, 0, Gdx.graphics.getHeight() - 32);
        batch.end();
    }
}
