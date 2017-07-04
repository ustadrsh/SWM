package com.aed.swm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Administrator on 23/05/2017.
 */
public class Box extends Actor {
    public static final int SHAPE_WIDTH = 50;
    public static final int SHAPE_HEIGHT = 50;

    private SWM game;
    private CustomShapeRenderer renderer;

    private LittleBox littleBox;

    private float originalX, originalY;

    public void setLittleBox(LittleBox littleBox) {
        this.littleBox = littleBox;
    }

    public Box(final SWM game, float x, float y) {
        this.game = game;
        this.renderer = game.getRenderer();
        super.setColor(Color.RED);

        this.originalX = x;
        this.originalY = y;

        super.setBounds(x, y, SHAPE_WIDTH, SHAPE_HEIGHT);
        super.setOrigin(Align.center);
        super.setTouchable(Touchable.enabled);

//        addAction(sequence(moveTo((float) (x + (2 * Math.random() - 1) * getWidth() * 5), (float) (y + (2 * Math.random() - 1) * getHeight() * 5)), delay(1), alpha(0), scaleTo(.1f, .1f),
//                parallel(fadeIn(2f, Interpolation.pow2),
//                        scaleTo(1f, 1f, 2.5f, Interpolation.pow5),
//                        moveTo(x, y, 2f, Interpolation.swing))));

        addListener(new DragListener() {
            public void drag(InputEvent event, float x, float y, int pointer) {
                moveBy(x - getWidth() / 2, y - getHeight() / 2);
            }
        });
    }

    private boolean animating = false;

    @Override
    protected void positionChanged() {
        if (!new Rectangle(originalX, originalY, getWidth(), getHeight()).contains(getX(), getY())) {
            if (!animating) {
                animating = true;

                addAction(Actions.sequence(Actions.delay(0.2f), Actions.moveTo(originalX, originalY, 0.5f, Interpolation.circleOut), Actions.after(new Action() {
                    @Override
                    public boolean act(float delta) {
                        animating = false;
                        if (littleBox != null) {
                            final boolean uncovered = littleBox.isUncovered();
                            if (!uncovered) {
                                littleBox.uncover();
                                game.assignLittleBox();

                                Vector2 nextPos = game.nextLittleBoxPos();
                                littleBox.addAction(Actions.sequence(Actions.moveTo(nextPos.x, nextPos.y, 0.2f, Interpolation.circleOut)));
                            }

                            game.recordAttempt(!uncovered);
                        }
                        return true;
                    }
                })));
            }
        }
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