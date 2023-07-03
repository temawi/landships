package gg.landships;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class UIManager {
    public static void drawReloadBar() {
        ShapeRenderer debugRenderer = new ShapeRenderer();

        float pixPerTick = Gdx.graphics.getWidth() / (Game.tank.reloadTime * 60f);
        float barWidth = pixPerTick * (Game.tank.nextShot - Game.tank.ticks);

        debugRenderer.setProjectionMatrix(Game.stage.getCamera().combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(Color.YELLOW);
        debugRenderer.box(0, 0, 0, barWidth, 20, 1);
        debugRenderer.end();
    }

    public static void drawTextUI() {
        SpriteBatch batch = new SpriteBatch();
        BitmapFont font = new BitmapFont();

        font.getData().setScale(1.2f);

        batch.setProjectionMatrix(Game.stage.getCamera().combined);
        batch.begin();
        font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight() - 16);
        batch.end();
    }
}
