package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class TankTurret {
    Sprite sprite;

    TankTurret(String s) {
        sprite = new Sprite(new Texture(s));
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
