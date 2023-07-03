package gg.landships;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Thing {
    public Sprite sprite;
    public abstract void dispose();
    public abstract void render(SpriteBatch batch);
}
