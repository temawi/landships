package gg.landships;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Shell extends Thing {
    Vector2 velocity;
    Vector2 dir;
    Tank owner;

    float damage;
    float speed = 35;
    long ticks;

    Shell(Vector2 dir, float dmg, float startx, float starty, float spd, Tank ow){
        this.dir = dir;
        velocity = new Vector2();
        sprite = new Sprite(new Texture("shell.png"));

        speed = spd;
        sprite.setPosition(startx, starty);

        velocity.x = dir.x * speed;
        velocity.y = dir.y * speed;
        damage = dmg;

        owner = ow;
    }

    public void calculate() {
        velocity = new Vector2();
        velocity.x = dir.x * speed;
        velocity.y = dir.y * speed;
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
        calculate();

        if(dir != null) {
            newPos.x = sprite.getX() + velocity.x;
            newPos.y = sprite.getY() + velocity.y;
            sprite.setPosition(newPos.x, newPos.y);
        }

        if(ticks > 180) {
            dispose();
        }

        for(Tank t: new ArrayList<>(Game.tanks)) {
            if(t != owner) {
                if(sprite.getBoundingRectangle().overlaps(t.sprite.getBoundingRectangle())) {
                    dispose();

                    Game.handler.write(new NetHitMessageBuilder(
                            Game.tanks.indexOf(t),
                            damage
                    ).build());
                }
            }
        }

        sprite.draw(batch);

        if(dir != null)
            ticks++;
    }
}
