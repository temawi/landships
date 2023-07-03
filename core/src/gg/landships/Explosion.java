package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Explosion extends Thing {
    private long tick;

    Explosion(float x, float y) {
        sprite = new Sprite(new Texture("boom.png"));
        sprite.setPosition(x, y);
    }

    @Override
    public void dispose() {
        Game.renderList.remove(this);
        sprite.getTexture().dispose();
    }

    @Override
    public void render(SpriteBatch batch) {
        tick++;

        sprite.draw(batch);

        if(tick > 120) {
            dispose();
        }
    }
}
