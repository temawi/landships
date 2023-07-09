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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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

	static TiledMap map;
	static OrthogonalTiledMapRenderer renderer;
	static NetHandler handler;

	public void controls() {
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			netRot = tank.sprite.getRotation() + tank.turnSpeed;

			// do this so that the hull's direction variable is always updated
			tank.getDriveDirection(true);
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			netRot = tank.sprite.getRotation() - tank.turretSpeed;

			// read above comment :D
			tank.getDriveDirection(true);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			// drive forwards
			tank.drive(true);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			// drive in reverse
			tank.drive(false);
		}

		// the reason this is set to use isButtonPressed() and not
		// isButtonJustPressed() is because we want to have the player shoot
		// on the very first frame that he can.
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			// if we are reloaded...
			if(tank.ticks > tank.nextShot) {
				// set the next time we can fire
				tank.nextShot = (long) (tank.ticks + tank.reloadTime * 60);

				// shoot the gun
				tank.fire();
			}
			// not yet reloaded
		}
	}

	@Override
	public void create () {
		batch = new SpriteBatch();

		// init all the lists and arrays
		tanks = new ArrayList<>();
		renderList = new ArrayList<>();
		shells = new ArrayList<>();
		tankHashMap = new HashMap<>();

		// used to draw text anchored to the screen
		// and not the world
		stage = new Stage();

		// init the networked local position
		// as well as the renderer used to draw the aim line
		netPos = new Vector2();
		debugRenderer = new ShapeRenderer();

		// initialize the camera and all of its settings
		camera = new OrthographicCamera(640, 480);
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.zoom = 3.0f;
		camera.update();

		// initialize the map and its renderer
		map = new TmxMapLoader().load("level.tmx");
		renderer = new OrthogonalTiledMapRenderer(map);

		// create the font and set its scale
		font = new BitmapFont();
		font.getData().setScale(3.0f);

		// fill the tank list with a bunch of puppet tanks
		// these are controlled by the net commands from the server
		for(int i = 0; i < 10; i++) {
			Tank t = new Tank("test_hull.png", "test_turret.png");
			tanks.add(t);
			tankHashMap.put(t, i);
		}

		// establish server connection
		handler = new NetHandler("127.0.0.1", 27015);

		// start handling controls
		Gdx.input.setInputProcessor(this);
		UIManager.init();

		// this method is over
		// now we pass control over to render()
	}

	@Override
	public void render () {
		// make a background outside any map the game may have
		ScreenUtils.clear(1, 0, 0, 1);

		renderer.setView(camera);
		renderer.render();

		// make sure this code here is only run if
		// the local tank is initialized to prevent NullPointerException
		if(tank != null) {
			// update the camera's position onto the tank
			camera.position.set(netPos.x, netPos.y, 0);

			// handle turret turning or whatever the tank wants to do
			tank.think();

			// tell the server what is happening
			// this means position, rotation, turret angles.
			String msg = new NetMessageBuilder(
					netPos.x,
					netPos.y,
					netRot,
					netTurretRot
			).build();

			// send the message
			handler.write(msg);
		}

		// update the camera, draw text
		camera.update();
		UIManager.drawTextUI();

		// set the batch to use the camera
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

		// process shell hit detection
		for(Shell s: new ArrayList<>(shells)) {
			// for each tank:
			for(Tank t: tanks) {
				// if it hits...
				if(s.sprite.getBoundingRectangle().overlaps(t.sprite.getBoundingRectangle())) {
					// and it doesn't hit your tank...
					if(t != tank) {
						// tell the server and delete the shell
						handler.write(new NetHitMessageBuilder(tanks.indexOf(t), s.damage).build());
						s.dispose();
					}
				}
			}
		}


		if (Game.tank != null) {
			// update reload progress at the bottom of the screen
			UIManager.drawReloadBar();

			// update UI text at the top of the screen
			UIManager.updateUI();

			// reset the NetHandler's traffic byte counters
			handler.resetByteCounter();

			// initialize some mouse variables
			Vector2 mousePos = new Vector2();
			Vector3 mousePos3;

			// get the mouse pos in the world using the camera
			mousePos3 = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			mousePos.x = mousePos3.x;
			mousePos.y = mousePos3.y;

			// draw the aim line
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
		debugRenderer.dispose();
		map.dispose();
		renderer.dispose();

		try {
			for (Thing t : renderList) {
				t.dispose();
			}

			for (Tank t : tanks) {
				t.dispose();
			}
		} catch (ConcurrentModificationException ignored) {}

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
		if(camera.zoom + amountY > 1.0f && camera.zoom + amountY <= tank.maxZoom) {
			camera.zoom += amountY;
			font.getData().setScale(font.getScaleX() + amountY);
		}
		return false;
	}
}
