package com.fluid;

import com.badlogic.gdx.ApplicationAdapter;
import com.fluid.birdson.BirdsonSetup;
import com.fluid.stam.StamRenderer;

public class Main extends ApplicationAdapter {
	StamRenderer stamRenderer;
	FluidSetup fluidSetup;
	BirdsonSetup birdsonSetup;

	@Override
	public void create() {
		stamRenderer = new StamRenderer();
		fluidSetup = new FluidSetup();
		birdsonSetup = new BirdsonSetup();
	}

	@Override
	public void render() {
		stamRenderer.render();
//		fluidSetup.render();
//		birdsonSetup.render();
	}
}
