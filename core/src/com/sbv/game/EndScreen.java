package com.sbv.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.awt.Color;

public class EndScreen extends ScreenAdapter {

    DropGame game;
    Sprite skin;
    Texture texture;
    int score;
    OrthographicCamera camera;
    TextureRegion backgroundTexture;


    public EndScreen(DropGame game, int score, OrthographicCamera camera) {
        this.game = game;
        this.score = score;
        this.camera = camera;
    }

    @Override
    public void show() {
        texture = new Texture(Gdx.files.internal("Refresh.png"));
        skin = new Sprite(texture); // your image
        skin.setPosition(Gdx.graphics.getWidth() * .60f, Gdx.graphics.getHeight() * .15f);
        skin.setSize(80, 80);
        backgroundTexture = new TextureRegion(new Texture("GameOverBackground.jpg"), 55, 0, 800, 480);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0);
        game.font.setColor(0, 0, 0, 1);
        game.font.draw(game.batch, "Tu puntuacion ha sido: " + score, Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * .25f);
        skin.draw(game.batch);
        game.batch.end();

        if (Gdx.input.isTouched()) {
            Vector3 tmp=new Vector3(Gdx.input.getX(),Gdx.input.getY(), 0);
            camera.unproject(tmp);
            Rectangle textureBounds= new Rectangle(skin.getX(),skin.getY(),skin.getWidth(),skin.getHeight());
            if(textureBounds.contains(tmp.x,tmp.y)) {
                game.setScreen(new GameScreen(game));
            }
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
