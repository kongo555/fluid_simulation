package com.fluid.birdson;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.fluid.FluidSolver;
import com.fluid.stam.Stam;

/**
 * Created by kongo on 06.02.16.
 */
public class BirdsonSetup {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private int width;
    private int height;
    int n = 64;

    private Vector3 mouse_position = new Vector3(0,0,0);
    private boolean drawVel;

    private BirdsonSolver fluidSolver;

    public BirdsonSetup(){
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);
        camera.update();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        fluidSolver = new BirdsonSolver(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),n, 0.5f);
    }

    public void render(){
        camera.update();
        mouse_position.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse_position);

        fluidSolver.update(mouse_position);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        if(Gdx.input.isKeyJustPressed(Input.Keys.V))
            drawVel = !drawVel;
        fluidSolver.render(shapeRenderer, drawVel);
    }
}
