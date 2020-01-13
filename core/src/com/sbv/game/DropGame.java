package com.sbv.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DropGame extends Game {

    SpriteBatch batch;
    BitmapFont font;

    @Override
    public void create () {
        batch = new SpriteBatch();
        font = new BitmapFont();
        setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose () {
        batch.dispose();
        font.dispose();
    }
}
