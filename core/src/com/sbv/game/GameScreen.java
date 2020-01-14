package com.sbv.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
	DropGame game;
	private Texture dropImage;
	private Texture acidImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Sound endGame;
	private Music rainMusic;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private Array<Rectangle> aciddrops;
	private long lastDropTime;
	private int score;
	private String comptaGotes;
	private boolean isPaused;
	private TextureRegion backgroundTexture;
	private int dificultad;
	private double chanceAcid;
	private float speed;
	State state = State.Running;

	public GameScreen(DropGame game) {
		this.game = game;
	}

	public void show() {
		// Se inicializa el score y se arranca el juego
		score = 0;
		dificultad = 0;
		speed = 1f;
		comptaGotes = "Score: 0";
		isPaused = false;

		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		acidImage = new Texture(Gdx.files.internal("AcidDrop.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		endGame = Gdx.audio.newSound(Gdx.files.internal("GameOver.mp3"));

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

		backgroundTexture = new TextureRegion(new Texture("Background.jpg"), 0, 0, 800, 480);

		// llamada al metodo que spawnea las gotas
		raindrops = new Array<Rectangle>();
		aciddrops = new Array<Rectangle>();
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
					endGame.play();
					rainMusic.stop();
					isPaused = true;
					game.setScreen(new EndScreen(game, score, camera));
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

		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 700 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 700 * Gdx.graphics.getDeltaTime();
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800 - 64) bucket.x = 800 - 64;

		// Indica el tiempo maximo hasta que aparece una nueva gota
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
		// Movimienta de las gotas
		for (Iterator<Rectangle> iterRain = raindrops.iterator(); iterRain.hasNext(); ) {
			Rectangle raindrop = iterRain.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime() * speed;
			if (raindrop.y + 64 < 0) {
				iterRain.remove();
				this.state = State.Paused;
			}
			if (raindrop.overlaps(bucket) && raindrop.y >= 70) {
				score++;
				dificultad++;
				comptaGotes = "Score: " + score;
				dropSound.play();
				iterRain.remove();
				if (dificultad == 10) {
					dificultad = 0;
					speed = speed * 1.2f;
				}
			}
		}

		for (Iterator<Rectangle> iterAcid = aciddrops.iterator(); iterAcid.hasNext(); ) {
			Rectangle raindrop = iterAcid.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime() * speed;
			if (raindrop.y + 64 < 0) {
				iterAcid.remove();
			}
			if (raindrop.overlaps(bucket) && raindrop.y >= 70) {
				iterAcid.remove();
				this.state = State.Paused;
			}
		}
	}
	public void draw() {
		// Se pone la camara y el batch
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();
		game.batch.draw(backgroundTexture, 0, 0);

		// Se dibuja el cubo, las gotas y el score
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		game.font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		game.font.draw(game.batch, comptaGotes, Gdx.graphics.getWidth() * .025f, Gdx.graphics.getHeight() * .95f);
		game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * .90f, Gdx.graphics.getHeight() * .95f);
		for (Rectangle raindrop : raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		for (Rectangle aciddrop : aciddrops) {
			game.batch.draw(acidImage, aciddrop.x, aciddrop.y);
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
		chanceAcid = Math.random() * 100;
		if (chanceAcid < 40) {
			aciddrops.add(raindrop);
		} else {
			raindrops.add(raindrop);
		}
		lastDropTime = TimeUtils.nanoTime();
	}

	public enum State{
		Running, Paused
	}
}
