package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Shell extends Thing {
    Vector2 velocity;
    float damage;
    long ticks;

    Shell(Vector2 dir, float dmg, float startx, float starty) {
        velocity = new Vector2();
        sprite = new Sprite(new Texture("shell.png"));

        sprite.setPosition(startx, starty);

        velocity.x = dir.x * 20;
        velocity.y = dir.y * 20;
        damage = dmg;
    }

    @Override
    public void dispose() {
        Game.renderList.remove(this);
        Game.shells.remove(this);
        sprite.getTexture().dispose();
    }

    @Override
    public void render(SpriteBatch batch) {
        Vector2 newPos = new Vector2();
        newPos.x = sprite.getX() + velocity.x * 20;
        newPos.y = sprite.getY() + velocity.y * 20;
        sprite.setPosition(newPos.x, newPos.y);

        if(ticks > 180) {
            dispose();
        }

        sprite.draw(batch);
        ticks++;
    }
}
