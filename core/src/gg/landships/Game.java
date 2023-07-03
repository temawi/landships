package gg.landships;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Game extends ApplicationAdapter implements InputProcessor {
	static SpriteBatch batch;
	static OrthographicCamera camera;

	static ArrayList<Tank> tanks;
	static ArrayList<Thing> renderList;
	static ArrayList<Shell> shells;

	static Tank tank;
	static ShapeRenderer debugRenderer;
	static BitmapFont font;
	static HashMap<Tank, Integer> tankHashMap;

	static Vector2 netPos;
	static float netRot;
	static float netTurretRot;
	static Stage stage;

	NetHandler handler;

	public void controls() {
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			netRot = tank.sprite.getRotation() + tank.turnSpeed;

			// do this so that we can always have the right direction
			tank.getDriveDirection(true);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			netRot = tank.sprite.getRotation() - tank.turretSpeed;

			// read above comment :D
			tank.getDriveDirection(true);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			tank.drive(true);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			tank.drive(false);
		}

		if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			if(tank.ticks > tank.nextShot) {
				tank.nextShot = (long) (tank.ticks + tank.reloadTime * 60);

				System.out.println("Tank: Gun fired! Next shot @ tick " + tank.nextShot + " or in " +
						(tank.nextShot - tank.ticks) / 60 + " sec");

				Vector2 turretDirection = new Vector2();

				// apply dispersion to the gun
				float turretAngleInRad = (float)Math.toRadians(tank.turret.sprite.getRotation() + 90 +
						MathUtils.random(-tank.dispersion, tank.dispersion));

				// calculate the correct turret direction vector
				turretDirection.x = (float)Math.cos(turretAngleInRad);
				turretDirection.y = (float)Math.sin(turretAngleInRad);
				turretDirection.nor();

				// create the shell and add it to all the lists
				Shell newShell = (new Shell(turretDirection, (float)Math.floor(MathUtils.random(tank.damage, tank.maxDamage)),
						tank.sprite.getX() + 32, tank.sprite.getY() + 64));

				Game.renderList.add(newShell);
				Game.shells.add(newShell);
			} else {
				System.out.println("Tank: Reloading! Time left " + (tank.nextShot - tank.ticks) / 60 + " sec");
			}
		}
	}

	@Override
	public void create () {
		batch = new SpriteBatch();

		tanks = new ArrayList<>();
		renderList = new ArrayList<>();
		shells = new ArrayList<>();
		tankHashMap = new HashMap<>();

		stage = new Stage();

		netPos = new Vector2();
		debugRenderer = new ShapeRenderer();

		camera = new OrthographicCamera(640, 480);
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.zoom = 3.0f;
		camera.update();

		font = new BitmapFont();
		font.getData().setScale(3.0f);

		for(int i = 0; i < 11; i++) {
			Tank t = new Tank("test_hull.png", "test_turret.png");
			tanks.add(t);
			tankHashMap.put(t, i);
		}

		handler = new NetHandler("127.0.0.1", 27015);
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);

		if(tank != null) {
			camera.position.set(netPos.x, netPos.y, 0);
			tank.think();

			String msg = new NetMessageBuilder(
					netPos.x,
					netPos.y,
					netRot,
					netTurretRot
			).build();

			handler.write(msg);
		}

		camera.update();
		UIManager.drawTextUI();
		batch.setProjectionMatrix(camera.combined);

		// handle all keys and buttons
		controls();

		// render all tanks and render other objects
		for(Tank t: tanks) {
			t.render(batch);
		}

		// render all the other non-tank sprites
		batch.begin();
		for(Thing t: new ArrayList<>(renderList)) {
			t.render(batch);
		}
		batch.end();

		for(Shell s: new ArrayList<>(shells)) {
			for(Tank t: tanks) {
				if(s.sprite.getBoundingRectangle().overlaps(t.sprite.getBoundingRectangle())) {
					handler.write(new NetHitMessageBuilder(tanks.indexOf(t), s.damage).build());
					s.dispose();
				}
			}
		}


		if (Game.tank != null) {
			UIManager.drawReloadBar();

			Vector2 mousePos = new Vector2();
			Vector3 mousePos3;

			mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			mousePos.x = mousePos3.x;
			mousePos.y = mousePos3.y;

			Gdx.gl.glLineWidth(2);
			debugRenderer.setProjectionMatrix(camera.combined);
			debugRenderer.begin(ShapeRenderer.ShapeType.Line);
			debugRenderer.setColor(Color.YELLOW);
			debugRenderer.line(new Vector2(tank.sprite.getX() + 64, tank.sprite.getY() + 128), mousePos);
			debugRenderer.end();
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();

		for(Thing t: tanks) {
			t.dispose();
		}

		try {
			handler.dispose();
		} catch (IOException ignored) {}
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		if(camera.zoom + amountY > 1.0f && camera.zoom + amountY < 10.0f) {
			camera.zoom += amountY;
			font.getData().setScale(font.getScaleX() + amountY);
		}
		return false;
	}
}
