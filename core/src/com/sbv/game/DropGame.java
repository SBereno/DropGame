package com.sbv.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DropGame extends Game {
    // Generamos el SpriteBatch y el BitmapFont que utilizaremos en todas las pantallas del juego
    SpriteBatch batch;
    BitmapFont font;

    // En el metodo create() se declaran el SpriteBatch y el BitmapFont, y se llama a la pantalla del menu principal
    @Override
    public void create () {
        batch = new SpriteBatch();
        font = new BitmapFont();
        setScreen(new TitleScreen(this));
    }

    // Al extender la clase DropGame de Game, este metodo dispose() se llama automaticamente al cerrar la aplicacion
    @Override
    public void dispose () {
        batch.dispose();
        font.dispose();
    }
}
