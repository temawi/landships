package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class TankTurret {
    Sprite sprite;
    Tank owner;

    TankTurret(String s, Tank o) {
        Texture texture = new Texture(s);

        owner = o;

        // set the turret center variable to the center of the turret itself (not the sprite)
        o.turretCenter = new Vector2(texture.getWidth() / 2f, texture.getHeight() / 4f);

        sprite = new Sprite(texture);
        sprite.setScale(2);

        // set the origin of the sprite
        // this is done so it rotates around the turret not the middle of the sprite.
        sprite.setOrigin(o.turretCenter.x, o.turretCenter.y);
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
