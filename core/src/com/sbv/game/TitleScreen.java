package com.sbv.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

public class TitleScreen extends ScreenAdapter {

    DropGame game;

    // Constructor
    public TitleScreen(DropGame game) {
        this.game = game;
    }

    // Metodo que se llama al iniciar la Screen
    @Override
    public void show(){
        // Si se pulsa la barra espaciadora del teclado, se llama a la Screen del juego
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keyCode) {
                if (keyCode == Input.Keys.SPACE) {
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }
        });
    }

    // Metodo que se ejecuta tantas veces por segundo como FPS
    @Override
    public void render(float delta) {
        // Se declara el color de fondo para la pantalla
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // se dibujan los textos de saludo e instrucciones
        game.batch.begin();
        game.font.draw(game.batch, "Bienvenido a DropGame", Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * .75f);
        game.font.draw(game.batch, "Usa las flechas o haz click en la pantalla para recoger las gotas. \nEvita las gotas de lluvia acida.", Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * .50f);
        game.font.draw(game.batch, "Pulsa espacio o haz click para empezar", Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * .25f);
        game.batch.end();

        // En caso de pulsar la pantalla, tambien se inicia el juego
        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
        }
    }

    // Metodo que detiene el proceso al esconderse la Screen
    @Override
    public void hide(){
        Gdx.input.setInputProcessor(null);
    }
}
