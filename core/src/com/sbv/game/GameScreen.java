package com.sbv.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
	DropGame game;
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Sound endGame;
	private Music rainMusic;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;
	private int score;
	private String comptaGotes;
	private boolean isPaused;
	State state = State.Running;

	public GameScreen(DropGame game) {
		this.game = game;
	}

	public void show() {
		// Se inicializa el score y se arranca el juego
		score = 0;
		comptaGotes = "Score: 0";
		isPaused = false;

		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		endGame = Gdx.audio.newSound(Gdx.files.internal("EndGame.wav"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		// camera and batch creation
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// bucket properties
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		// llamada al metodo que spawnea las gotas
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render(float delta) {
		switch(state){
			case Running:
				update();
				break;
			case Paused:
				if (!isPaused) {
					rainMusic.stop();
					endGame.play();
					isPaused = true;
					game.setScreen(new EndScreen(game, score));
				}
				break;
		}
		draw();
	}

	public void update() {
		// Listeners para el movimiento del cubo
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800 - 64) bucket.x = 800 - 64;

		// Indica el tiempo maximo hasta que aparece una nueva gota
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
		// Movimienta de las gotas
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) {
				iter.remove();
				this.state = State.Paused;
			}
			if (raindrop.overlaps(bucket)) {
				score++;
				comptaGotes = "Score: " + score;
				dropSound.play();
				iter.remove();
			}
		}
	}
	public void draw() {
		// Se indica el color del fondo
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Se pone la camara y el batch
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();

		// Se dibuja el cubo, las gotas y el score
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		game.font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		game.font.draw(game.batch, comptaGotes, Gdx.graphics.getWidth() * .025f, Gdx.graphics.getHeight() * .95f);
		game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * .90f, Gdx.graphics.getHeight() * .95f);
		for (Rectangle raindrop : raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		game.batch.end();
	}

	// Metodo que se encarga de spawnear las gotas de forma aleatoria
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	public enum State{
		Running, Paused
	}
}
