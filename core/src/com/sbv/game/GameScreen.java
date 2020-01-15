package com.sbv.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

	// Constructor
	public GameScreen(DropGame game) {
		this.game = game;
	}

	// Metodo que se llama al iniciar la Screen
	public void show() {
		// Se inicializa el score, el contador para aumentar la velocidad y se arranca el juego
		score = 0;
		dificultad = 0;
		speed = 1f;
		comptaGotes = "Score: 0";
		isPaused = false;

		// Se cargan las imagenes del cubo y las dos gotas
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		acidImage = new Texture(Gdx.files.internal("AcidDrop.png"));

		// Se cargan la musica y los sonidos
		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		endGame = Gdx.audio.newSound(Gdx.files.internal("GameOver.mp3"));

		// Se reproduce la musica de fondo el bucle
		rainMusic.setLooping(true);
		rainMusic.play();

		// Se declara la camara y su altura y anchura
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// Declaramos las propiedades del cubo
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		// Se declara y asigna la imagen de fondo
		backgroundTexture = new TextureRegion(new Texture("Background.jpg"), 0, 0, 800, 480);

		// Se declaran los dos Arrays que contendran los dos tipos de gota y se llama al metodo que spawnea las gotas
		raindrops = new Array<Rectangle>();
		aciddrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	// Metodo que se ejecuta tantas veces por segundo como FPS
	@Override
	public void render(float delta) {
		// Dependiendo del estado del juego, se recarga la Screen o se pasa a la de Game Over
		switch(state){
			case Running:
				// El metodo que calcula los movimientos de las gotas y del cubo
				update();
				break;
			case Paused:
				// IF que se ejecuta una vez como precaucion. Detiene la musica, reproduce el sonido de derrota,
				// pausa el juego y inicia la Screen de Game Over
				if (!isPaused) {
					endGame.play();
					rainMusic.stop();
					isPaused = true;
					game.setScreen(new EndScreen(game, score, camera));
				}
				break;
		}
		// Metodo que dibuja los elementos de la pantalla
		draw();
	}

	// Metodo que calcula los movimientos de las gotas y del cubo
	public void update() {
		// Listeners para el movimiento del cubo
		if (Gdx.input.isTouched()) {
			// Se calcula la posicion pulsada y se mueve el cubo a ella
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}

		// En caso de pulsar las flechas de direccion del teclado, el cubo tambien se mueve
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 700 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 700 * Gdx.graphics.getDeltaTime();

		// Limitacion para que el cubo no pueda salir de los bordes
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800 - 64) bucket.x = 800 - 64;

		// Indica el tiempo maximo hasta que aparece una nueva gota
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		// Movimienta de las gotas de lluvia
		for (Iterator<Rectangle> iterRain = raindrops.iterator(); iterRain.hasNext(); ) {
			// Se indica la velocidad de la gota
			Rectangle raindrop = iterRain.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime() * speed;

			// En caso de caer al suelo, el juego se pausa y se pierde la partida
			if (raindrop.y + 64 < 0) {
				iterRain.remove();
				this.state = State.Paused;
			}

			// Si la gota cae en el cubo por la parte de arriba, sube la puntuacion y el contador de dificultad
			if (raindrop.overlaps(bucket) && raindrop.y >= 70) {
				score++;
				dificultad++;
				comptaGotes = "Score: " + score;

				// Se reproduce el sonido de recoger la gota y se borra
				dropSound.play();
				iterRain.remove();

				// En caso de que el contador de la dificultad llegue a 10, se resetea y la velocidad aumenta
				if (dificultad == 10) {
					dificultad = 0;
					speed = speed * 1.2f;
				}
			}
		}

		// Este iterador se encarga de las gotas de acido
		for (Iterator<Rectangle> iterAcid = aciddrops.iterator(); iterAcid.hasNext(); ) {
			// Se indica la velocidad de la gota de acido
			Rectangle raindrop = iterAcid.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime() * speed;

			// En este caso, si cae al suelo se borra, pero si se recoge con el cubo se pausa la partida y se pierde
			if (raindrop.y + 64 < 0) {
				iterAcid.remove();
			}

			if (raindrop.overlaps(bucket) && raindrop.y >= 70) {
				iterAcid.remove();
				this.state = State.Paused;
			}
		}
	}

	// Metodo que dibuja los diferentes elementos de la pantalla
	public void draw() {
		// Se pone la camara y el batch
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();

		// Se dibuja el fondo de pantalla
		game.batch.draw(backgroundTexture, 0, 0);

		// Se dibuja el cubo
		game.batch.draw(bucketImage, bucket.x, bucket.y);

		// Se asigna color a la fuente y se dibuja el contador de puntuacion y los FPS
		game.font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		game.font.draw(game.batch, comptaGotes, Gdx.graphics.getWidth() * .025f, Gdx.graphics.getHeight() * .95f);
		game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), Gdx.graphics.getWidth() * .90f, Gdx.graphics.getHeight() * .95f);

		// Se dibujan las gotas de los arrays de gotas de lluvia y acido
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
		// Primero se indican las propiedades de la gota
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;

		// Luego se elige una gota al azar y se asigna a su array
		chanceAcid = Math.random() * 100;

		if (chanceAcid < 40) {
			aciddrops.add(raindrop);
		} else {
			raindrops.add(raindrop);
		}

		// Se da valor al contador que indica el tiempo desde la ultima gota asignada
		lastDropTime = TimeUtils.nanoTime();
	}

	// Enumeracion de los dos posibles estados del juego, Running y Paused
	public enum State{
		Running, Paused
	}
}
