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
    Vector2 turretCenter;

    float hp;
    float speed;
    float turretSpeed;
    float turnSpeed;
    float dispersion;
    float damage;
    float maxDamage;
    float reloadTime;
    float shotVelocity;
    float maxZoom;

    long ticks;
    long nextShot;
    long respawnTick;

    Tank(String s, String ts) {
        // init the hull sprite and create the turret
        sprite = new Sprite(new Texture(s));
        turret = new TankTurret(ts, this);

        // set all of the statistics...
        hp = 350;
        speed = 1f;
        turretSpeed = 1.2f;
        turnSpeed = 2f;
        dispersion = 1f;
        damage = 50;
        shotVelocity = 35f;
        maxDamage = 65;
        reloadTime = 1.5f;
        maxZoom = 12.0f;

        // whew finally done with that

        // start it WAY off-screen
        sprite.setPosition(-999999999, -999999999);

        // make sure that we don't respawn the instant we start playing
        // at 60 fps, we would respawn for no reason in about:
        // 11574 hours, or 482 days, or 1.32 years. no worries.
        respawnTick = 999999999;
    }

    public void think() {
        // get the mouse pos in the world
        Vector3 mousePos = Game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        // get the player pos in Vector3
        Vector3 playerPos = new Vector3(turret.sprite.getX() + turretCenter.x, turret.sprite.getY() + turretCenter.y, 0);

        // do math to find the angle the turret should be at
        float radians = (float) Math.atan2(mousePos.y - playerPos.y, mousePos.x - playerPos.x);
        radians -= Math.PI/2;

        // convert that angle from radians to degrees
        float degrees = radians * MathUtils.radiansToDegrees;

        // flawed algorithm to figure out if we are there yet
        // this is to determine how far from the correct direction we are right now
        float turretAngleDifference = Game.netTurretRot - degrees;

        // degree of error (it can be off by 1.5 degrees max)
        if(turretAngleDifference > 1.5 || turretAngleDifference < -1.5) {
            // go the right direction
            if (turretAngleDifference < 0) {
                Game.netTurretRot += turretSpeed;
            } else {
                Game.netTurretRot -= turretSpeed;
            }
        }

        // respawn when the tick is right
        if(respawnTick < ticks) {
            respawnTick = 99999999;
            Game.netPos = new Vector2(
                    MathUtils.random(-2000, 2000),
                    MathUtils.random(-2000, 2000)
            );
        }
    }

    public void destroy() {
        Game.renderList.add(new DestroyedTank(sprite.getX(), sprite.getY(), sprite.getRotation()));
        respawnTick = ticks + 120;
    }

    @Override
    public void render(SpriteBatch batch) {
        // begin rendering
        batch.begin();

        // render the hull and turret
        sprite.draw(batch);
        turret.sprite.draw(batch);

        // render the HP text above the tank
        Game.font.draw(batch, String.valueOf(hp), sprite.getX(), sprite.getY() + 316);
        batch.end();

        // blow up if hp < 0
        if(hp < 0) {
            destroy();
            hp = 350;
        } else {
            // only do this if the tank is ALIVE
            turret.sprite.setPosition(
                    this.sprite.getX() + 32,
                    this.sprite.getY() + 96
            );

        }

        // increments ticks counter.
        ticks++;
    }

    public Vector2 getDriveDirection(boolean forward) {
        // figure out what direction we are to drive in

        // init direction Vector2
        direction = new Vector2();

        // do math based on if we are going forwards or not
        if(forward) {
            direction.x = (float) Math.cos(Math.toRadians(sprite.getRotation() + 90));
            direction.y = (float) Math.sin(Math.toRadians(sprite.getRotation() + 90));
        } else {
            direction.x = (float) Math.cos(Math.toRadians(sprite.getRotation() - 90));
            direction.y = (float) Math.sin(Math.toRadians(sprite.getRotation() - 90));
        }

        // normalize and return it
        direction.nor();
        return direction;
    }

    public void drive(boolean forward) {
        // find the driving direction
        Vector2 direction = getDriveDirection(forward);

        // get the tank's velocity
        Vector2 velocity = new Vector2();
        velocity.x = direction.x * 10;
        velocity.y = direction.y * 10;

        // make a newPos Vector2
        Vector2 newPos = new Vector2();

        // calculate the next position
        newPos.x += sprite.getX() + velocity.x * speed;
        newPos.y += sprite.getY() + velocity.y * speed;

        // apply the next position
        Game.netPos = newPos;
    }

    public void fire() {
        // shoot the gun. get the turret direction initialized
        Vector2 turretDirection = new Vector2();

        // apply dispersion to the gun
        float turretAngleInRad = (float)Math.toRadians(turret.sprite.getRotation() + 90 +
                MathUtils.random(-dispersion, dispersion));

        // calculate the correct turret direction vector
        turretDirection.x = (float)Math.cos(turretAngleInRad);
        turretDirection.y = (float)Math.sin(turretAngleInRad);
        turretDirection.nor();

        // create the shell and add it to all the lists
        Shell newShell = (new Shell(turretDirection, (float)Math.floor(MathUtils.random(damage, maxDamage)),
                sprite.getX() + 32, sprite.getY() + 64, shotVelocity, this));

        // tell everyone that we fired

        String line = new NetShotMessageBuilder(
                turretDirection,
                sprite.getX() + 32,
                sprite.getY() + 32,
                Game.tanks.indexOf(this),
                shotVelocity
        ).build();

        Game.handler.write(line);

        // add to lists finally
        Game.renderList.add(newShell);
        Game.shells.add(newShell);
    }

    public void dispose() {
        sprite.getTexture().dispose();
        turret.sprite.getTexture().dispose();
    }
}
