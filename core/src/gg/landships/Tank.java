package gg.landships;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Tank extends Thing {
    TankTurret turret;
    Sprite sprite;
    Vector2 direction;

    boolean dead = false;

    float hp;
    float speed;
    float turretSpeed;
    float turnSpeed;
    float dispersion;
    float damage;
    float maxDamage;
    float reloadTime;

    long ticks;
    long nextShot;
    long respawnTick;

    int upgradePoints;

    Tank(String s, String ts) {
        sprite = new Sprite(new Texture(s));
        turret = new TankTurret(ts);

        hp = 350;
        speed = 0.6f;
        turretSpeed = 1.2f;
        turnSpeed = 2f;
        dispersion = 1.0f;
        damage = 50;
        maxDamage = 65;
        reloadTime = 2.0f;
    }

    public void think() {
        Vector3 mousePos = Game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        Vector3 playerPos = new Vector3(turret.sprite.getX(), turret.sprite.getY(), 0);
        float radians = (float) Math.atan2(mousePos.y - playerPos.y, mousePos.x - playerPos.x);
        radians -= Math.PI/2;

        float degrees = radians * MathUtils.radiansToDegrees;

        if (degrees < turret.sprite.getRotation()) {
            Game.netTurretRot = (turret.sprite.getRotation() - turretSpeed);
        } else {
            Game.netTurretRot = (turret.sprite.getRotation() + turretSpeed);
        }
    }

    public void destroy() {
        Game.renderList.add(new DestroyedTank(sprite.getX(), sprite.getY(), sprite.getRotation()));
        respawnTick = ticks + 120;
        hp = 350;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        sprite.draw(batch);
        turret.sprite.draw(batch);
        Game.font.draw(batch, String.valueOf(hp), sprite.getX(), sprite.getY() + 316);
        batch.end();

        if(hp < 0) {
            destroy();
            hp = 350;
            Game.netPos = new Vector2((float) Math.floor(MathUtils.random(-1000, 640)), (float) Math.floor(MathUtils.random(-1000, 640)));
        } else {
            // only do this if the tank is ALIVE
            turret.sprite.setPosition(
                    this.sprite.getX(),
                    this.sprite.getY() + 64
            );

        }

        if(respawnTick < ticks) {
            respawnTick = 99999999;
            Game.netPos = new Vector2(
                    MathUtils.random(-1000, 1000),
                    MathUtils.random(-1000, 1000)
            );
        }

        ticks++;
    }

    public Vector2 getDriveDirection(boolean forward) {
        direction = new Vector2();

        if(forward) {
            direction.x = (float) Math.cos(Math.toRadians(sprite.getRotation() + 90));
            direction.y = (float) Math.sin(Math.toRadians(sprite.getRotation() + 90));
        } else {
            direction.x = (float) Math.cos(Math.toRadians(sprite.getRotation() - 90));
            direction.y = (float) Math.sin(Math.toRadians(sprite.getRotation() - 90));
        }

        direction.nor();
        return direction;
    }

    public void drive(boolean forward) {
        Vector2 direction = getDriveDirection(forward);

        Vector2 velocity = new Vector2();
        velocity.x = direction.x * 10;
        velocity.y = direction.y * 10;

        Vector2 newPos = new Vector2();

        newPos.x += sprite.getX() + velocity.x * speed;
        newPos.y += sprite.getY() + velocity.y * speed;

        Game.netPos = newPos;
    }

    public void dispose() {
        sprite.getTexture().dispose();
        turret.sprite.getTexture().dispose();
    }
}
