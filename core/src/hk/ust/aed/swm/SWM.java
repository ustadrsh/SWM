package hk.ust.aed.swm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import java.util.Arrays;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;

public class SWM implements ApplicationListener {
    private Color[] colors = new Color[]{Color.WHITE, Color.YELLOW, Color.BLUE, Color.GREEN, Color.PINK};
    private CustomShapeRenderer renderer;
    private Array<Box> levelBoxes = new Array<Box>();
    private Array<LittleBox> littleBoxes = new Array<LittleBox>();
    private ProgressBar bar;
    private Stage stage;
    private Stage ui;

    private Array<Boolean> correctness = new Array<Boolean>();
    private Array<Long> latencies = new Array<Long>();
    private long lastScreen = TimeUtils.millis();

    private Vector2 nextLittleBoxPos = new Vector2(10, 10);

    private int numSets = 3, numBoxes = 5, currentSet = 1;
    private float trialDuration = 20000;
    private boolean finished = false;

    public Vector2 nextLittleBoxPos() {
        Vector2 clone = nextLittleBoxPos.cpy();
        nextLittleBoxPos = nextLittleBoxPos.add(LittleBox.SHAPE_WIDTH + 10, 0);
        return clone;
    }

    public CustomShapeRenderer getRenderer() {
        return this.renderer;
    }

    @Override
    public void create() {
        stage = new Stage(new ScalingViewport(Scaling.fillX, 480, 800));
        ui = new Stage(new ScalingViewport(Scaling.fillX, 480, 800));
        renderer = new CustomShapeRenderer();
        bar = new ProgressBar(renderer, 10);
        Gdx.input.setInputProcessor(stage);
        ui.addActor(bar);

        resetGame();
    }

    public Array<Box> generateBoxes(int count, Color[] colors) {
        Array<Box> generatedBoxes = new Array<Box>();

        int candidateX = 0, candidateY = 0;
        for (int i = 0; i < count; i++) {
            boolean unique = false;
            while (!unique) {
                candidateX = MathUtils.random(0, 480 - Box.SHAPE_WIDTH);
                candidateY = MathUtils.random(100, 800 - Box.SHAPE_HEIGHT); // 100 margin from bottom.

                Rectangle candidateRectangle = new Rectangle(candidateX, candidateY, Box.SHAPE_WIDTH + 20f, Box.SHAPE_HEIGHT + 20f);

                unique = true;
                for (Box box : generatedBoxes) {
                    if (candidateRectangle.overlaps(new Rectangle(box.getX(), box.getY(), box.getWidth(), box.getHeight()))) {
                        unique = false;
                        break;
                    }
                }
            }
            Box box = new Box(this, candidateX, candidateY);

            generatedBoxes.add(box);
        }
        return generatedBoxes;
    }

    @Override
    public void resize(int width, int height) {

    }

    public void recordAttempt(boolean littleBoxUnhidden) {
        correctness.add(littleBoxUnhidden);
        System.out.println(Arrays.toString(correctness.toArray()));
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.setProjectionMatrix(stage.getBatch().getProjectionMatrix());
        renderer.setTransformMatrix(stage.getBatch().getTransformMatrix());

        if (!finished) {
            float delta = Gdx.graphics.getDeltaTime();
            stage.act(delta);
            ui.act(delta);
            stage.draw();
            ui.draw();
            if (TimeUtils.millis() - lastScreen >= trialDuration) {
                //		bar.addAction(Actions.moveTo(currentLevel / (float) (numBoxes * numSets), bar.getY(), 0.2f, Interpolation.pow2InInverse));
                boolean allUncovered = true;
                for (LittleBox box : littleBoxes) {
                    if (!box.isUncovered()) {
                        allUncovered = false;
                        break;
                    }
                }
                if ((littleBoxes.size >= numBoxes && allUncovered)) {
                    // Finished level
                } else {
                    // Didn't finish level
                }

                if (currentSet >= numSets) {
                    // Game done.
                    System.out.println("GAME DONE");
                    finish();
                } else {
                    currentSet++;
                    latencies.add((TimeUtils.millis() - lastScreen));
                    lastScreen = TimeUtils.millis();
                    resetGame();
                }
            }
        } else {
            //GRAPHIC WHEN FINISHED
        }
        bar.addAction(scaleTo(((float) (TimeUtils.millis() - lastScreen)) / trialDuration, 1f, 0.5f, Interpolation.pow2InInverse));
    }

    public void finish() {
        if (!finished) {
            finished = true;
            sendAnalytics();
        }
    }

    private class Results implements Json.Serializable {
        private String phoneNumber;
        private Array<Boolean> correctness = new Array<Boolean>();
        private Array<Long> latencies = new Array<Long>();

        public Results(String phoneNumber, Array<Boolean> correctness, Array<Long> latencies) {
            this.phoneNumber = phoneNumber;
            this.correctness = correctness;
            this.latencies = latencies;
        }

        @Override
        public void write(Json json) {
            json.writeFields(this);
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            json.readFields(this, jsonData);
        }
    }

    public void sendAnalytics() {
        Json converter = new Json();
        converter.setOutputType(JsonWriter.OutputType.json);
        String json = converter.toJson(new Results("852-9220-5256", correctness, latencies), Results.class);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        final Net.HttpRequest httpRequest = requestBuilder.newRequest()
                .method(Net.HttpMethods.POST).header("Content-Type", "application/json")
                .url("https://alzheimers-early-detection-mas.firebaseio.com/colorsArray.json")
                .content(json).build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Gdx.app.exit();
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void cancelled() {

            }
        });
    }

    public void resetGame() {
        nextLittleBoxPos = new Vector2(10, 10);

        for (Box box : levelBoxes) box.remove();
        levelBoxes.clear();

        for (LittleBox box : littleBoxes) box.remove();
        littleBoxes.clear();

        levelBoxes.addAll(generateBoxes(numBoxes, null));
        for (Box box : levelBoxes) {
            stage.addActor(box);
        }
        assignLittleBox();
    }

    public void assignLittleBox() {
        if (littleBoxes.size >= numBoxes) return;

        Box randomBox = levelBoxes.get(littleBoxes.size);

        LittleBox littleBox = new LittleBox(this, Color.YELLOW, randomBox.getX() + Box.SHAPE_WIDTH / 2, randomBox.getY() + Box.SHAPE_HEIGHT / 2);
        littleBoxes.add(littleBox);

        randomBox.setLittleBox(littleBox);

        stage.addActor(littleBox);

        littleBox.toBack();
        for (Box box : levelBoxes) box.toFront();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        renderer.dispose();
    }
}
