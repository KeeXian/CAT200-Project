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
import com.almasb.fxgl.core.util.Consumer;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
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
import com.almasb.fxglgames.td.components.TowerComponent;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import com.almasb.fxglgames.td.tower.TowerIcon;
import com.almasb.fxglgames.td.tower.TowerLocationInfo;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getGameState;

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
    private int numEnemies = 20;
    private double player_gold = 3000;
    private int score=0;
    private int selectedIndex = 1;
    private int enemyLevel = 1;
    private int enemyIndex = random(1,2);
    private Text gold= new Text(Double.toString(player_gold));
    private Text highscore=new Text(Integer.toString(score));

    private Point2D enemySpawnPoint = new Point2D(50, 0);

    private List<Point2D> waypoints = new ArrayList<>();

    public List<Point2D> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    private Music BGM;

    private Music LoseMusic;

    private Music SuccessMusic;

    private Music CoinMusic;

    private ArrayList<Texture> textures = new ArrayList<Texture>();

    LinkedList<TowerLocationInfo> list=new LinkedList<>();

    //Initial game settings
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

    //intialize all the inputs
    @Override
    protected void initInput() {
        Input input = getInput();
        addListofWorldBound(list);
        input.addAction(new UserAction("Place Tower") {
            @Override
            protected void onActionBegin() {
                if (checkValidTowerLocation(input.getMousePositionWorld())) {
                    placeTower(input.getMousePositionWorld());
                }
                else {
                    Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld());
                    if(tempRect!=null){
                        double[] coor=get_center(tempRect);
                        List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(coor[0] - 32, coor[1] - 60));
                        if(!tower_chosen.isEmpty()){
                            Entity tower=tower_chosen.get(0);
                            if(tower.getComponent(TowerDataComponent.class).getLevel()<3){
                                getDisplay().showConfirmationBox("Do you want to upgrade the tower to level " +
                                        (tower.getComponent(TowerDataComponent.class).getLevel()+1) + "?"+'\n'+"Cost: "+
                                        tower.getComponent(TowerDataComponent.class).getUpgradeCost()+" gold", yes -> {
                                            if(yes) {
                                                if(getGameState().getDouble("playerGold")>=tower.getComponent(TowerDataComponent.class).getUpgradeCost()) {
                                                    getGameState().increment("playerGold", - tower.getComponent(TowerDataComponent.class).getUpgradeCost());
                                                    gold.setText(getGameState().getDouble("playerGold").toString());
                                                    Text text = FXGL.getUIFactory().
                                                            newText("-" + tower.getComponent(TowerDataComponent.class).getUpgradeCost(), Color.BLACK, 10);
                                                    text.setTranslateX(20);
                                                    text.setTranslateY(30);
                                                    FadeTransition fade = new FadeTransition(Duration.millis(3000), text);
                                                    fade.setFromValue(1.0);
                                                    fade.setToValue(0);
                                                    fade.play();
                                                    FXGL.getGameScene().addUINode(text);
                                                    tower.getComponent(TowerDataComponent.class).upgradeTower();
                                                    tower.getComponent(TowerDataComponent.class).setLabel();
                                                    showMessage("The tower is upgraded to level " +
                                                            tower.getComponent(TowerDataComponent.class).getLevel());
                                                }
                                                else
                                                    trgInsufficientFunds();
                                            }
                                        });
                            }
                        }
                    }
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("remove tower") {
            @Override
            protected void onAction() {

                Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld());
                if(tempRect!=null) {
                    int index;
                    double[] coor=get_center(tempRect);
                    List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(coor[0] - 32, coor[1] - 60));
                    index = getObjIndexOnPoint(input.getMousePositionWorld());
                    list.get(index).setOccupied(false);
                    if (!tower_chosen.isEmpty()) {
                            CoinMusic = getAssetLoader().loadMusic("coin.mp3");
                            getAudioPlayer().stopMusic(CoinMusic);
                            getAudioPlayer().playMusic(CoinMusic);
                            getGameState().increment("playerGold",
                                    tower_chosen.get(0).getComponent(TowerDataComponent.class).getPrice() / 2.0);
                            gold.setText(getGameState().getDouble("playerGold").toString());
                            tower_chosen.get(0).removeFromWorld();
                    }
                }
            }
        }, MouseButton.SECONDARY);
    }

    /**
     * this functions assign values to in-game variables that can be used throughout the games
     * called through the global FXGL function:
     * getGameState().getObj("String name")
     * the value will be assign to the String and can be used throughout the game
     */
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("numEnemies", numEnemies);
        vars.put("playerGold",player_gold);
        vars.put("HighScore",score);
    }

    //initialize game map, set up icons and the map
    //programme the path for enemies to move
    @Override
    protected void initGame() {
        Level level;
        var levelFile=new File("map.tmx");
        Rectangle rect=new Rectangle(50, 30);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2.0);
        rect.setTranslateX(12);
        rect.setTranslateY(20);
        getGameScene().addUINode(rect);
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

    //handle physics such as when entities collide with each other
    @Override
    protected void initPhysics() {
        BulletEnemyHandler a = new BulletEnemyHandler();
        a.setIndex(selectedIndex);
        getPhysicsWorld().addCollisionHandler(a);
    }

    @Override
    protected void initUI() {
        Rectangle uiBG = new Rectangle(getAppWidth(), 70);
        uiBG.setTranslateY(530);
        uiBG.setFill(Color.DARKGREY);
        getGameScene().addUINode(uiBG);
        //assigns values to the game state "playerGold"
        gold.setText(getGameState().getDouble("playerGold").toString());
        gold.setFill(Color.BLACK);
        gold.setTranslateX(20);
        gold.setTranslateY(40);
        getGameScene().addUINode(gold);
        //the score is get from the game state "HighScore"
        highscore.setText("Score: "+getGameState().getInt("HighScore").toString());
        highscore.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 16));
        highscore.setTranslateX(640);
        highscore.setTranslateY(40);
        getGameScene().addUINode(highscore);
        addIntoTextureList();
        String[] tower_name_array ={"Small Stone Tower","Big Stone Tower","Metal Ball Tower","Fireball Tower"};
        for (int i = 0; i < 4; i++) {
            int index = i + 1;
            TowerIcon icon = new TowerIcon(textures.get(i));
            icon.setTranslateX(10 + i * 120);
            icon.setTranslateY(545);
            icon.setOnMouseClicked(e -> selectedIndex = index);
            Label Tower_Price = new Label(Double.toString(TowerDataComponent.makeTower(i+1).getPrice()));
            Tower_Price.setTranslateX(70+i*120);
            Tower_Price.setTranslateY(570);
            Tower_Price.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,8));
            Label Tower_Name = new Label(tower_name_array[i]);
            Tower_Name.setTranslateX(2+i*120);
            Tower_Name.setTranslateY(530);
            Tower_Name.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,10));
            getGameScene().addUINodes(icon,Tower_Price,Tower_Name);
        }
    }

    //spawn enemies from the spawning point
    private void spawnEnemy() {
        //
        //adjust enemy type and spawn enemy
        //
        if(getGameState().getInt("numEnemies")==1 && getGameState().getInt("numEnemies")!=numEnemies) {
            enemyIndex = 3;
        }
        else if(getGameState().getInt("numEnemies")%5==0 && getGameState().getInt("numEnemies")!=numEnemies) {
            enemyIndex=random(1,2);
            if(enemyIndex==1)
                enemyLevel=random(1,3);
            else
                enemyLevel=random(1,2);
        }
        else {
            if(enemyIndex==3) {
                if(BGM==getAssetLoader().loadMusic("Epic Battle.mp3")) {
                    getAudioPlayer().stopMusic(BGM);
                    BGM = getAssetLoader().loadMusic("Boss.mp3");
                    getAudioPlayer().loopMusic(BGM);
                }
            }
            //decrement the number of enemies waiting to be spawn
            getGameState().increment("numEnemies", -1);
            //spawns a enemy and sends the required information through the put function
            getGameWorld().spawn("Enemy",
                    new SpawnData(enemySpawnPoint.getX(), enemySpawnPoint.getY())
                            .put("index", enemyIndex)
                            .put("level", enemyLevel)
            );
        }
    }

    private void placeTower(Point2D point) {
        Rectangle2D tempRect=getWorldBoundOnPoint(point);
        double[] coor =get_center(tempRect);
        //spawns a tower and sends the required information through the put function
        //the default spawn tower is of type small stone tower
            getGameWorld().spawn("Tower",
                    new SpawnData(coor[0]-32, coor[1]-60)
                            .put("texture", textures.get(selectedIndex - 1))
                            .put("index", selectedIndex)
                            .put("Position", new Point2D(coor[0]-32, coor[1]-60)));
            player_gold=getGameState().getDouble("playerGold");
            gold.setText(getGameState().getDouble("playerGold").toString());
    }

    //occurs after the event which an enemy is hit by a bullet
    private void onEnemyKilled(BulletHitEnemy event){
        Entity enemy = event.getEnemy();
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0) {
            /*
             * decrements the number of enemies left in the game
             * note that number of enemies left in the game is different from
             * number of enemies waiting to be spawn
             */
            numEnemies--;
            if (numEnemies == 0)
                gameCleared();
            getGameState().increment("playerGold",
                    (enemy.getComponent(EnemyDataComponent.class).getGold()));
            gold.setText(getGameState().getDouble("playerGold").toString());
            getGameState().increment("HighScore",enemy.getComponent(EnemyDataComponent.class).getScore());
            highscore.setText("Score: "+getGameState().getInt("HighScore").toString());
            removeEnemyFromWorld(enemy);
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

    //adds the tower textures into a list for display purposes
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
        TowerDataComponent tower = TowerDataComponent.makeTower(selectedIndex);
        if(tower.getPrice()>getGameState().getDouble("playerGold")) {
            //if user does not have insufficient gold to spawn selected tower
            trgInsufficientFunds();
        }
        else
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
    }

    //error message for insufficient funds
    public void trgInsufficientFunds(){
        Text text = FXGL.getUIFactory().newText("Insufficient Funds", Color.RED, 24);
        text.setTranslateX(280);
        text.setTranslateY(20);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(3000), text);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0);
        fadeTransition.play();
        FXGL.getGameScene().addUINode(text);
    }

    //remove enemy entity and UI for enemy removal and coin spawning
    protected void removeEnemyFromWorld(Entity enemy){
        Point2D position = enemy.getPosition();
        //The coin that appears when enemy is killed
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

        Music music = FXGL.getAssetLoader().loadMusic("coin.mp3");
        FXGL.getAudioPlayer().stopMusic(music);
        FXGL.getAudioPlayer().playMusic(music);
        enemy.removeFromWorld();
        Text text = FXGL.getUIFactory().
                newText("+"+enemy.getComponent(EnemyDataComponent.class).getGold(), Color.BLACK, 10);
        text.setTranslateX(20);
        text.setTranslateY(30);
        fade = new FadeTransition(Duration.millis(3000),text);
        fade.setFromValue(1.0);
        fade.setToValue(0);
        fade.play();
        FXGL.getGameScene().addUINode(text);
    }

    private  double[] get_center(@NotNull Rectangle2D tempRect){
        return new double[]{
                tempRect.getMinX() + tempRect.getWidth() / 2.0,
                tempRect.getHeight() / 2.0 + tempRect.getMinY()
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}
