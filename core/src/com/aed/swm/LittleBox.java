package com.aed.swm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Administrator on 23/05/2017.
 */
public class LittleBox extends Actor {
    public static final int SHAPE_WIDTH = 25;
    public static final int SHAPE_HEIGHT = 25;

    private SWM game;
    private CustomShapeRenderer renderer;

    private boolean uncovered = false;

    public void uncover() {
        uncovered = true;
    }

    public boolean isUncovered() {
        return uncovered;
    }

    public LittleBox(final SWM game, final Color color, float x, float y) {
        this.game = game;
        this.renderer = game.getRenderer();

        super.setColor(Color.YELLOW);

        super.setBounds(x, y, SHAPE_WIDTH, SHAPE_HEIGHT);
        super.setOrigin(Align.center);
        super.setTouchable(Touchable.enabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderer.setColor(getColor());
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.rect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        //renderer.roundedRect(getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(), getHeight()/4);
        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}