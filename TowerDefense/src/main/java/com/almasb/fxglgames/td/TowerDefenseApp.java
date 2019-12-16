package com.almasb.fxglgames.td;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.GameView;
<<<<<<< HEAD
import com.almasb.fxgl.audio.Music;
=======
import com.almasb.fxgl.audio.Audio;
import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.util.BiConsumer;
>>>>>>> shuen
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
<<<<<<< HEAD
import com.almasb.fxgl.gameplay.GameDifficulty;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxglgames.td.collision.BulletEnemyHandler;
=======
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;
import com.almasb.fxglgames.td.collision.BulletEnemyHandler;
import com.almasb.fxglgames.td.components.TowerComponent;
>>>>>>> shuen
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import com.almasb.fxglgames.td.tower.TowerIcon;
<<<<<<< HEAD
=======
import com.almasb.fxglgames.td.tower.TowerLocationInfo;
import javafx.animation.AnimationTimer;
>>>>>>> shuen
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
<<<<<<< HEAD
=======
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
>>>>>>> shuen
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
=======
import java.util.*;
import java.util.concurrent.TimeUnit;
>>>>>>> shuen

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
<<<<<<< HEAD
    private double player_gold = 8000;
=======
    private double player_gold = 4000;
    private int score=0;
>>>>>>> shuen

    private Point2D enemySpawnPoint = new Point2D(50, 0);

    private List<Point2D> waypoints = new ArrayList<>();

    public List<Point2D> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    private Music BGM;

    private Music LoseMusic;

<<<<<<< HEAD
    private ArrayList<Texture> textures = new ArrayList<Texture>();

    private ArrayList<Point2D> point2DS = new ArrayList<Point2D>();
    private ArrayList<int[]> waves = new ArrayList<int[]>();
    private int numWaves=0;

    private ImageView imageView;
=======
    private Music SuccessMusic;

    private Music CoinMusic;

    private ArrayList<Texture> textures = new ArrayList<Texture>();

    LinkedList<TowerLocationInfo> list=new LinkedList<>();

>>>>>>> shuen
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
    }

    @Override
    protected void initInput() {
        Input input = getInput();
        addListofWorldBound(list);
        input.addAction(new UserAction("Place Tower") {
<<<<<<< HEAD
            private Rectangle2D worldBounds = new Rectangle2D(32, 96, 544, 128);
            private Rectangle2D worldBounds_alt=new Rectangle2D(128, 368, 464, 48);

            @Override
            protected void onActionBegin() {
                if (worldBounds.contains(input.getMousePositionWorld())||worldBounds_alt.contains(input.getMousePositionWorld())) {
                    placeTower();
=======
            @Override
            protected void onActionBegin() {
                if (checkValidTowerLocation(input.getMousePositionWorld())) {
                    placeTower(input.getMousePositionWorld());
>>>>>>> shuen
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("remove tower") {
            @Override
            protected void onAction() {
<<<<<<< HEAD
                List<Entity> list = getGameWorld().getEntitiesAt(input.getMousePositionWorld());
                if(!list.isEmpty()){
                    System.out.println(list.get(0));
                    Entity entity=list.get(0);
                    if(entity.isType(TowerDefenseType.TOWER))
                        entity.removeFromWorld();
=======
                int index;
                Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld());
                double x_coor=tempRect.getMinX()+tempRect.getWidth()/2.0;
                double y_coor = tempRect.getHeight()/2.0+tempRect.getMinY();
                List<Entity> tower_chosen=getGameWorld().getEntitiesAt(new Point2D(x_coor-32,y_coor-40));
                index=getObjIndexOnPoint(input.getMousePositionWorld());
                list.get(index).setOccupied(false);
                if(!tower_chosen.isEmpty()) {
                    CoinMusic=getAssetLoader().loadMusic("coin.mp3");
                    getAudioPlayer().stopMusic(CoinMusic);
                    getAudioPlayer().playMusic(CoinMusic);
                    player_gold+=tower_chosen.get(0).getComponent(TowerDataComponent.class).getPrice()/2.0;
                    getGameState().setValue("playerGold",player_gold);
                    gold.setText(getGameState().getDouble("playerGold").toString());
                    tower_chosen.get(0).removeFromWorld();
>>>>>>> shuen
                }
            }
        }, MouseButton.SECONDARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("numEnemies", levelEnemies);
        vars.put("playerGold",player_gold);
<<<<<<< HEAD
=======
        vars.put("HighScore",score);
>>>>>>> shuen
    }

    @Override
    protected void initGame() {
        Level level;
<<<<<<< HEAD
        var levelFile=new File("untitled.tmx");
=======
        var levelFile=new File("map.tmx");
        Rectangle rect=new Rectangle(50, 30);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2.0);
        rect.setTranslateX(12);
        rect.setTranslateY(20);
        getGameScene().addUINode(rect);
>>>>>>> shuen
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
<<<<<<< HEAD
    private int selectedLevel = 3;
    private int enemyIndex = 1;
    private Text gold= new Text(Double.toString(player_gold));
=======
    private int selectedLevel = 1;
    private int enemyIndex = 1;
    private Text gold= new Text(Double.toString(player_gold));
    private Text highscore=new Text(Integer.toString(score));
>>>>>>> shuen

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
<<<<<<< HEAD
=======
        highscore.setText("Score: "+getGameState().getInt("HighScore").toString());
        highscore.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 16));
        highscore.setTranslateX(640);
        highscore.setTranslateY(40);
        getGameScene().addUINode(highscore);
>>>>>>> shuen
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
            TowerDataComponent tower = TowerDataComponent.makeTower(index);
            Text price = getUIFactory().newText(Double.toString(tower.getPrice()));
            price.setTranslateX(60 + i * 120);
            price.setTranslateY(575);
            getGameScene().addUINode(price);
        }

    }

    private void spawnEnemy() {
        //
        //adjust enemy type and spawn enemy
        //
<<<<<<< HEAD
        int enemy;
        if( getGameState().getInt("numEnemies")== 1 && getGameState().getInt("numEnemies")!=levelEnemies)
            enemyIndex=(3);
        else if (getGameState().getInt("numEnemies")%5==0 && getGameState().getInt("numEnemies")!=levelEnemies) {
=======
        if(getGameState().getInt("numEnemies")%5==0 && getGameState().getInt("numEnemies")!=levelEnemies) {
>>>>>>> shuen
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
<<<<<<< HEAD
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

            Text text = FXGL.getUIFactory().
                    newText("+"+enemy.getComponent(EnemyDataComponent.class).getGold(), Color.BLACK, 10);
            text.setTranslateX(20);
            text.setTranslateY(30);
            FadeTransition anotherfade = new FadeTransition(Duration.millis(3000),text);
            anotherfade.setFromValue(1.0);
            anotherfade.setToValue(0);
            anotherfade.play();
            FXGL.getGameScene().addUINode(text);
        }
        }

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
=======
    }

    private void placeTower(Point2D point) {
        Rectangle2D tempRect=getWorldBoundOnPoint(point);
        double x_coor=tempRect.getMinX()+tempRect.getWidth()/2.0;
        double y_coor = tempRect.getHeight()/2.0+tempRect.getMinY();
            getGameWorld().spawn("Tower",
                    new SpawnData(x_coor-32, y_coor-40)
                            .put("texture", textures.get(selectedIndex - 1))
                            .put("index", selectedIndex)
                            .put("Position", new Point2D(x_coor-32, y_coor-40)));
            player_gold=getGameState().getDouble("playerGold");
            gold.setText(getGameState().getDouble("playerGold").toString());
    }

    private void onEnemyKilled(BulletHitEnemy event){
        Entity enemy = event.getEnemy();
        EnemyDataComponent enemyDataComponent = EnemyDataComponent.makeEnemy(enemyIndex, selectedLevel);
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0) {
            levelEnemies--;
            if (levelEnemies == 0)
                gameCleared();

            double money = getGameState().getDouble("playerGold");
            getGameState().setValue("playerGold",
                    (money+enemy.getComponent(EnemyDataComponent.class).getGold()));
            gold.setText(getGameState().getDouble("playerGold").toString());
            score+=enemy.getComponent(EnemyDataComponent.class).getScore();
            getGameState().setValue("HighScore",score);
            highscore.setText("Score: "+getGameState().getInt("HighScore").toString());
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

    private void gameOver() {
        LoseMusic=getAssetLoader().loadMusic("game-lose.mp3");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(LoseMusic);
        getDisplay().showMessageBox("Game Over. Thanks for playing!"+'\n'+"Score: "+score, getGameController()::gotoMainMenu);
    }

    private void gameCleared(){
        SuccessMusic=getAssetLoader().loadMusic("Victory!.wav");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(SuccessMusic);
        getDisplay().showMessageBox("Game Cleared!!! Thanks for playing!"+'\n'+"Score: "+score, getGameController()::gotoMainMenu);
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

    public void addListofWorldBound(LinkedList<TowerLocationInfo> linkedList){ //add a list of location for tower placement
        linkedList.add(new TowerLocationInfo(new Rectangle2D(32,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(128,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(224,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(320,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(416,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(512,96,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(32,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(128,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(224,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(320,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(416,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(512,192,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(160,384,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(256,384,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(352,384,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(448,384,64,64)));
        linkedList.add(new TowerLocationInfo(new Rectangle2D(544,384,64,64)));
    }

    public boolean checkValidTowerLocation(Point2D point){
        boolean validLocation=false;
        for (TowerLocationInfo towerLocationInfo : list) {
            if (!towerLocationInfo.isOccupied()) {
                if (towerLocationInfo.getRect().contains(point)) {
                    validLocation = true;
                    towerLocationInfo.setOccupied(true);
                    break;
                }
            }
        }
        return validLocation;
    }

    public Rectangle2D getWorldBoundOnPoint(Point2D point){
        Rectangle2D rect=null;
        for (TowerLocationInfo towerLocationInfo : list) {
                if (towerLocationInfo.getRect().contains(point)) {
                    rect=towerLocationInfo.getRect();
                    break;
                }
        }
        return rect;
    }

    public int getObjIndexOnPoint(Point2D point){
        int index=0;
        for(int j=0;j<list.size();j++){
            if (list.get(j).getRect().contains(point)) {
                index=j;
                break;
            }
        }
        return index;
>>>>>>> shuen
    }

    /* public void configureEnemyWaves(int difficulty){
        switch (difficulty){
            case 0: {
                getGameState().gameDifficultyProperty().setValue(GameDifficulty.EASY);
                numWaves = 3;
                levelEnemies=5;
                int [] wave1 = {2,2,2,1,1};
                waves.add(wave1);
                int [] wave2 = {2,1,1,1,1};
                waves.add(wave2);
                int [] wave3 = {1,2,1,1,3};
                waves.add(wave3);
            }

        }
    }*/

    public static void main(String[] args) {
        launch(args);
    }
}
