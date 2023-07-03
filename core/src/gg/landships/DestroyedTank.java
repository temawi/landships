package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DestroyedTank extends Thing {
    DestroyedTank(float x, float y, float r) {
        sprite = new Sprite(new Texture("test_destroyed.png"));

        sprite.setPosition(x, y);
        sprite.setRotation(r);
        Game.renderList.add(new Explosion(x - 64, y - 64));
    }

    @Override
    public void dispose() {
        sprite.getTexture().dispose();
        Game.renderList.remove(this);
    }

    @Override
    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
