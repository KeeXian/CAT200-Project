package com.almasb.fxglgames.td;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.GameView;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.time.TimerAction;
import com.almasb.fxglgames.td.collision.BulletEnemyHandler;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import com.almasb.fxglgames.td.tower.TowerIcon;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * This is an example of a tower defense game.
 *
 * Demo:
 * 1. Enemies move using waypoints
 * 2. Player can place towers
 * 3. Towers can shoot enemies
 * 4. Game ends if enemies are dead or have reached the last waypoint
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class TowerDefenseApp extends GameApplication {

    // TODO: add HP components
    // TODO: assign bullet data from tower that shot it

    // TODO: read from level data
    private int levelEnemies = 30;
    private double player_gold = 4000;

    private Point2D enemySpawnPoint = new Point2D(50, 0);

    private List<Point2D> waypoints = new ArrayList<>();

    public List<Point2D> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Tower Defense");
        settings.setVersion("0.2");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(false);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(false);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Place Tower") {
            private Rectangle2D worldBounds = new Rectangle2D(0, 0, getAppWidth(), getAppHeight() - 100 - 40);
            @Override
            protected void onActionBegin() {
                if (worldBounds.contains(input.getMousePositionWorld())) {
                    placeTower();
                }
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("numEnemies", levelEnemies);
        vars.put("playerGold",player_gold);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new TowerDefenseFactory());

        // TODO: read this from external level data
        waypoints.addAll(Arrays.asList(
                new Point2D(700, 0),
                new Point2D(700, 300),
                new Point2D(50, 300),
                new Point2D(50, 450),
                new Point2D(700, 500)
        ));

        BooleanProperty enemiesLeft = new SimpleBooleanProperty();
        enemiesLeft.bind(getGameState().intProperty("numEnemies").greaterThan(0));

        getGameTimer().runAtIntervalWhile(this::spawnEnemy, Duration.seconds(1), enemiesLeft);

        getEventBus().addEventHandler(EnemyReachedGoalEvent.ANY, e -> gameOver());
        getEventBus().addEventHandler(BulletHitEnemy.ANY, this::onEnemyShot);
    }

    @Override
    protected void initPhysics() {
        BulletEnemyHandler a = new BulletEnemyHandler();
        a.setIndex(selectedIndex);
        getPhysicsWorld().addCollisionHandler(a);
    }

    // TODO: this should be tower data
    private Color selectedColor = Color.BLACK;
    private int selectedIndex = 1;
    // TODO: this is the enemy data
    private int selectedLevel = 5;
    private int enemyIndex = 1;
    private Text gold= new Text("4000.0");

    @Override
    protected void initUI() {
        Rectangle uiBG = new Rectangle(getAppWidth(), 100);
        uiBG.setTranslateY(500);
        getGameScene().addUINode(uiBG);
        gold.setText(getGameState().getDouble("playerGold").toString());
        gold.setTranslateX(20);
        gold.setTranslateY(20);
        getGameScene().addUINode(gold);
        for (int i = 0; i < 4; i++) {
            int index = i + 1;

            Color color = FXGLMath.randomColor();
            TowerIcon icon = new TowerIcon(color);
            icon.setTranslateX(10 + i * 100);
            icon.setTranslateY(500);
            icon.setOnMouseClicked(e -> {
                selectedColor = color;
                selectedIndex = index;
            });

            getGameScene().addUINode(icon);
        }
    }

    private boolean stop = false;
    private void spawnEnemy() {
        if(getGameState().getInt("numEnemies")%10==0 && getGameState().getInt("numEnemies")!=levelEnemies) {
            enemyIndex=random(1,5);
            stop = true;
        }
        else {
            getGameState().increment("numEnemies", -1);
            getGameWorld().spawn("Enemy",
                    new SpawnData(enemySpawnPoint.getX(), enemySpawnPoint.getY())
                            .put("index", enemyIndex)
                            .put("level", selectedLevel)
            );
        }
    }

    private void placeTower() {
        getGameWorld().spawn("Tower",
                new SpawnData(getInput().getMouseXWorld(), getInput().getMouseYWorld())
                        .put("color", selectedColor)
                        .put("index", selectedIndex)
        );
    }

    private void onEnemyShot(BulletHitEnemy event){
        Entity enemy = event.getEnemy();
        EnemyDataComponent enemyDataComponent = EnemyDataComponent.makeEnemy(enemyIndex, selectedLevel);
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0){
            levelEnemies--;
            if(levelEnemies==0)
                gameOver();
            }
            gold.setText(getGameState().getDouble("playerGold").toString());
            Point2D position = enemy.getPosition();
            Text xMark = getUIFactory().newText("X", Color.RED, 24);
            xMark.setTranslateX(position.getX() + 20);
            xMark.setTranslateY(position.getY() + 60);
            FadeTransition fade = new FadeTransition(Duration.millis(3000), xMark);
            fade.setFromValue(1.0);
            fade.setToValue(0);
            fade.play();
            getGameScene().addGameView(new GameView(xMark, 1000));
        }


  /*  private void onEnemyKilled(EnemyKilledEvent event) {
        levelEnemies--;

        if (levelEnemies == 0) {
            gameOver();
        }

        Entity enemy = event.getEnemy();
        Point2D position = enemy.getPosition();

        Text xMark = getUIFactory().newText("X", Color.RED, 24);
        xMark.setTranslateX(position.getX());
        xMark.setTranslateY(position.getY() + 20);

        getGameScene().addGameView(new GameView(xMark, 1000));
    }*/

    private void gameOver() {
        getDisplay().showMessageBox("Game Over. Thanks for playing!", getGameController()::exit);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
