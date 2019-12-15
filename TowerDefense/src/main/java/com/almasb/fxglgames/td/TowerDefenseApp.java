package com.almasb.fxglgames.td;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.GameView;
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.util.BiConsumer;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;
import com.almasb.fxglgames.td.collision.BulletEnemyHandler;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import com.almasb.fxglgames.td.tower.TowerIcon;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
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

    private Music BGM;

    private Music LoseMusic;

    private ArrayList<Texture> textures = new ArrayList<Texture>();

    private ArrayList<Point2D> point2DS = new ArrayList<Point2D>();
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Tower Defense");
        settings.setVersion("0.2");
        settings.setWidth(768);
        settings.setHeight(600);
        settings.setManualResizeEnabled(true);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(true);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(true);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
        settings.setManualResizeEnabled(true);
    }

    @Override
    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Place Tower") {
            private Rectangle2D worldBounds = new Rectangle2D(32, 96, 544, 128);
            private Rectangle2D worldBounds_alt=new Rectangle2D(128, 368, 464, 48);

            @Override
            protected void onActionBegin() {
                if (worldBounds.contains(input.getMousePositionWorld())||worldBounds_alt.contains(input.getMousePositionWorld())) {
                    placeTower();
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("remove tower") {
            @Override
            protected void onAction() {
                List<Entity> list = getGameWorld().getEntitiesAt(input.getMousePositionWorld());
                if(!list.isEmpty()){
                    System.out.println(list.get(0));
                    Entity entity=list.get(0);
                    if(entity.isType(TowerDefenseType.TOWER))
                        entity.removeFromWorld();
                }
            }
        }, MouseButton.SECONDARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("numEnemies", levelEnemies);
        vars.put("playerGold",player_gold);
    }

    @Override
    protected void initGame() {
        Level level;
        var levelFile=new File("untitled.tmx");
        getGameWorld().addEntityFactory(new TowerDefenseFactory());
        try {
            level = new TMXLevelLoader().load(levelFile.toURI().toURL(), getGameWorld());
            getGameWorld().setLevel(level);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BGM=getAssetLoader().loadMusic("Epic Battle.mp3");
        getAudioPlayer().loopMusic(BGM);

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
        getEventBus().addEventHandler(BulletHitEnemy.ANY, this::onEnemyKilled);
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
    private Text gold= new Text(Double.toString(player_gold));

    @Override
    protected void initUI() {
        Rectangle uiBG = new Rectangle(getAppWidth(), 50);
        uiBG.setTranslateY(550);
        getGameScene().addUINode(uiBG);
        gold.setText(getGameState().getDouble("playerGold").toString());
        gold.setFill(Color.BLACK);
        gold.setTranslateX(20);
        gold.setTranslateY(40);
        getGameScene().addUINode(gold);
        addIntoTextureList();
        for (int i = 0; i < 4; i++) {
            int index = i + 1;
            TowerIcon icon = new TowerIcon(textures.get(i));
            icon.setTranslateX(10 + i * 120);
            icon.setTranslateY(545);
            icon.setOnMouseClicked(e -> {
                selectedIndex = index;
            });
            getGameScene().addUINode(icon);
        }
        Text scoreText = getUIFactory().newText("",Color.BLACK,24);
        scoreText.setTranslateX(600);
        scoreText.setTranslateY(100);
        scoreText.textProperty().bind(score.asString("Score: [%d]"));
        getGameScene().addUINode(scoreText);
    }

    private void spawnEnemy() {
        //
        //adjust enemy type and spawn enemy
        //
        if(getGameState().getInt("numEnemies")%5==0 && getGameState().getInt("numEnemies")!=levelEnemies) {
            enemyIndex=random(1,2);
            if(enemyIndex==1)
                selectedLevel=random(1,3);
            else
                selectedLevel=random(1,2);
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
        if(!point2DS.contains(getInput().getMousePositionWorld())) {
            getGameWorld().spawn("Tower",
                    new SpawnData(getInput().getMouseXWorld() - 25, getInput().getMouseYWorld() - 25)
                            .put("texture", textures.get(selectedIndex - 1))
                            .put("index", selectedIndex)
                            .put("Position", new Point2D(getInput().getMouseXWorld() - 25, getInput().getMouseYWorld() - 25))
            );
            gold.setText(getGameState().getDouble("playerGold").toString());
            for(int i=-25; i<35; i++)
                for(int j=-25; j<35;j++) {
                    Point2D point = new Point2D(getInput().getMouseXWorld()+i, getInput().getMouseYWorld()+j);
                    point2DS.add(point);
                }
        }else{
            try{ throw new Exception("Cannot place tower");}
            catch(Exception e){
                System.out.println(e);
            }
        }
    }

    private void onEnemyKilled(BulletHitEnemy event){
        Entity enemy = event.getEnemy();
        EnemyDataComponent enemyDataComponent = EnemyDataComponent.makeEnemy(enemyIndex, selectedLevel);
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0) {
            levelEnemies--;
            if (levelEnemies == 0)
                gameOver();
            double money = getGameState().getDouble("playerGold");
            getGameState().setValue("playerGold",
                    (money+enemy.getComponent(EnemyDataComponent.class).getGold()));
            gold.setText(getGameState().getDouble("playerGold").toString());
            Point2D position = enemy.getPosition();
            Texture coin = new Texture(getAssetLoader().loadImage("coin.png"));
            coin.setFitHeight(30);
            coin.setFitWidth(30);
            coin.setTranslateX(position.getX());
            coin.setTranslateY(position.getY() + 50);

            TranslateTransition transition = new TranslateTransition();
            transition.setNode(coin);
            transition.setByY(-40);
            transition.play();

            FadeTransition fade = new FadeTransition(Duration.millis(3000), coin);
            fade.setFromValue(1.0);
            fade.setToValue(0);
            fade.play();
            getGameScene().addGameView(new GameView(coin, 1000));
        }
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
        LoseMusic=getAssetLoader().loadMusic("game-lose.mp3");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(LoseMusic);
        getDisplay().showMessageBox("Game Over. Thanks for playing!", getGameController()::exit);
        for(int i=0; i<point2DS.size();i++)
            System.out.println("Tower " + i + " : " + point2DS.get(i));
    }

    private void addIntoTextureList(){
        Texture texture = new Texture(getAssetLoader().loadImage("small_stone_tower.png"));
        texture.setFitHeight(60);
        texture.setFitWidth(60);
        textures.add(texture);
        texture = new Texture(getAssetLoader().loadImage("big_stone_tower.png"));
        texture.setFitHeight(60);
        texture.setFitWidth(60);
        textures.add(texture);
        texture = new Texture(getAssetLoader().loadImage("metal_ball_tower.png"));
        texture.setFitHeight(60);
        texture.setFitWidth(60);
        textures.add(texture);
        texture = new Texture(getAssetLoader().loadImage("fire_tower.png"));
        texture.setFitHeight(60);
        texture.setFitWidth(60);
        textures.add(texture);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
