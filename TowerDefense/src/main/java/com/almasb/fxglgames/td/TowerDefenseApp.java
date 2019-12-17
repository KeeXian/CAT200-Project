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
import com.almasb.fxglgames.td.tower.FireTowerComponent;
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

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TowerDefenseApp extends GameApplication {

    private int numEnemies = 10;
    private int numEnemiesPerWave = 8;
    private double player_gold = 10000;
    private int score=0;
    private int selectedIndex = 1;
    private int enemyLevel = 1;
    private int enemyIndex = random(1,2);
    private boolean spawnBoss=false;
    private int enemiesOnMap=0;
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
        settings.setTitle("Minimalist Tower Defense");
        settings.setVersion("2.0");
        settings.setWidth(768);
        settings.setHeight(600);
        settings.setManualResizeEnabled(true);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(true);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(true);
        settings.setApplicationMode(ApplicationMode.RELEASE);
    }

    //initialize all the inputs
    @Override
    protected void initInput() {
        Input input = getInput();
        addListofWorldBound(list); //Get a list of valid area for putting the towers
        input.addAction(new UserAction("Place/Upgrade Tower") {
            @Override
            protected void onActionBegin() {
                if (checkValidTowerLocation(input.getMousePositionWorld())) { //Check if the area clicked can put any tower
                    placeTower(input.getMousePositionWorld()); //Place Tower
                }
                else { //Upgrade tower
                    Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld()); //Get the area upon the click
                    if(tempRect!=null){ //Area found
                        double x_coor = tempRect.getMinX() + tempRect.getWidth() / 2.0; //Get horizontal midpoint of the area
                        double y_coor = tempRect.getHeight() / 2.0 + tempRect.getMinY(); //Get vertical midpoint of the area
                        List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(x_coor - 32, y_coor - 60)); //Get the list of towers in the given area
                        if(!tower_chosen.isEmpty()){ //If there is tower in the area
                            Entity tower=tower_chosen.get(0); //Get the tower
                            TowerDataComponent towerComponent;
                            if(tower.hasComponent(FireTowerComponent.class))
                                towerComponent = tower.getComponent(FireTowerComponent.class);
                            else
                                towerComponent = tower.getComponent(TowerDataComponent.class);
                            /*
                            Check if the selected tower's level is less than 3, allows the upgrade if its level is less than 3
                            and the amount of the gold is sufficient
                            */
                            if(tower.getComponent(towerComponent.getClass()).getLevel()<3){
                                getDisplay().showConfirmationBox("Do you want to upgrade the tower to level " +
                                        (tower.getComponent(towerComponent.getClass()).getLevel()+1) + "?"+'\n'+"Cost: "+
                                        tower.getComponent(towerComponent.getClass()).getUpgradeCost()+" gold", yes -> {
                                            if(yes) {
                                                if(getGameState().getDouble("playerGold")>=tower.getComponent(towerComponent.getClass()).getUpgradeCost()) {
                                                    player_gold = getGameState().getDouble("playerGold") - tower.getComponent(towerComponent.getClass()).getUpgradeCost();
                                                    getGameState().setValue("playerGold", player_gold);
                                                    gold.setText(Double.toString(player_gold));
                                                    Text text = FXGL.getUIFactory().
                                                            newText("-" + tower.getComponent(towerComponent.getClass()).getUpgradeCost(), Color.BLACK, 10);
                                                    text.setTranslateX(20);
                                                    text.setTranslateY(30);
                                                    FadeTransition fade = new FadeTransition(Duration.millis(3000), text);
                                                    fade.setFromValue(1.0);
                                                    fade.setToValue(0);
                                                    fade.play();
                                                    FXGL.getGameScene().addUINode(text);
                                                    tower.getComponent(towerComponent.getClass()).upgradeTower();
                                                    tower.getComponent(towerComponent.getClass()).setLabel();
                                                    showMessage("The tower is upgraded to level " +
                                                            tower.getComponent(towerComponent.getClass()).getLevel());
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

        input.addAction(new UserAction("Remove Tower") {
            @Override
            protected void onAction() {
                int index;
                Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld()); //Get the area upon the click
                if(tempRect!=null) { //Area found
                    double x_coor = tempRect.getMinX() + tempRect.getWidth() / 2.0; //Get the horizontal midpoint of the area
                    double y_coor = tempRect.getHeight() / 2.0 + tempRect.getMinY(); //Get the vertical midpoint of the area
                    List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(x_coor - 32, y_coor - 60)); //Get list of towers in the area
                    index = getObjIndexOnPoint(input.getMousePositionWorld()); //Get the area's index from the list of area
                    list.get(index).setOccupied(false);//Update the status of the area to not occupied
                    /*
                    If there is a tower in the area, remove it and add half of its cost as the gold
                     */
                    if (!tower_chosen.isEmpty()) {
                        Entity tower=tower_chosen.get(0);
                        TowerDataComponent towerComponent;
                        if(tower.hasComponent(FireTowerComponent.class))
                            towerComponent = tower.getComponent(FireTowerComponent.class);
                        else
                            towerComponent = tower.getComponent(TowerDataComponent.class);
                        CoinMusic = getAssetLoader().loadMusic("coin.mp3");
                        getAudioPlayer().stopMusic(CoinMusic);
                        getAudioPlayer().playMusic(CoinMusic);
                        player_gold += tower_chosen.get(0).getComponent(towerComponent.getClass()).getPrice() / 2.0;
                        getGameState().setValue("playerGold", player_gold);
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

    /*
    Set the tower selection bar at bottom of the screen, label the tower name and its price.
    Set the score and gold display
     */
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
        Label smallStonePrice = new Label("Gold: 1000");
        smallStonePrice.setTranslateX(70);
        smallStonePrice.setTranslateY(570);
        smallStonePrice.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,8));
        Label bigStonePrice=new Label("Gold: 2000");
        bigStonePrice.setTranslateX(185);
        bigStonePrice.setTranslateY(570);
        bigStonePrice.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,8));
        Label metalBallPrice=new Label("Gold: 4000");
        metalBallPrice.setTranslateX(305);
        metalBallPrice.setTranslateY(570);
        metalBallPrice.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,8));
        Label fireTowerPrice=new Label("Gold: 7800");
        fireTowerPrice.setTranslateX(425);
        fireTowerPrice.setTranslateY(570);
        fireTowerPrice.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,8));
        Label smallStoneName=new Label("Small Stone Tower");
        smallStoneName.setTranslateX(2);
        smallStoneName.setTranslateY(530);
        smallStoneName.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,10));
        Label bigStoneName=new Label("Big Stone Tower");
        bigStoneName.setTranslateX(122);
        bigStoneName.setTranslateY(530);
        bigStoneName.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,10));
        Label metalBallName=new Label("Metal Ball Tower");
        metalBallName.setTranslateX(242);
        metalBallName.setTranslateY(530);
        metalBallName.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,10));
        Label fireTowerName=new Label("Fireball Tower");
        fireTowerName.setTranslateX(362);
        fireTowerName.setTranslateY(530);
        fireTowerName.setFont(Font.font("Verdana",FontWeight.BOLD,FontPosture.REGULAR,10));
        getGameScene().addUINodes(smallStonePrice,bigStonePrice,metalBallPrice,fireTowerPrice,smallStoneName,bigStoneName,metalBallName,fireTowerName);
    }

    //spawn enemies from the spawning point
    private void spawnEnemy() {
        //
        //adjust enemy type and spawn enemy
        //
        if(getGameState().getInt("numEnemies")==1 && getGameState().getInt("numEnemies")!=numEnemies) {
            spawnBoss=true;
        }
        else if(getGameState().getInt("numEnemies")%numEnemiesPerWave==0 && getGameState().getInt("numEnemies")!=numEnemies) {
            enemyIndex=random(1,2);
            if(enemyIndex==1)
                enemyLevel=random(1,3);
            else
                enemyLevel=random(1,2);
        }
        else {
            if(spawnBoss && numEnemies==1) { //Final boss
                enemyIndex=3;
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
        double x_coor=tempRect.getMinX()+tempRect.getWidth()/2.0;
        double y_coor = tempRect.getHeight()/2.0+tempRect.getMinY();
        //spawns a tower and sends the required information through the put function
        //the default spawn tower is of type small stone tower
            getGameWorld().spawn("Tower",
                    new SpawnData(x_coor-32, y_coor-60)
                            .put("texture", textures.get(selectedIndex - 1))
                            .put("index", selectedIndex)
                            .put("Position", new Point2D(x_coor-32, y_coor-60)));
            player_gold=getGameState().getDouble("playerGold");
            gold.setText(getGameState().getDouble("playerGold").toString());
    }

    //occurs after the event which an enemy is hit by a bullet
    private void onEnemyKilled(BulletHitEnemy event){
        Entity enemy = event.getEnemy();
        if(enemy.getComponent(EnemyDataComponent.class).getHp()<=0) {
            /**
             * decrements the number of enemies left in the game
             * note that number of enemies left in the game is different from
             * number of enemies waiting to be spawn
             */
            enemiesOnMap--;
            numEnemies--;
            if (numEnemies == 0)
                gameCleared();
            double money = getGameState().getDouble("playerGold");
            getGameState().setValue("playerGold",
                    (money+enemy.getComponent(EnemyDataComponent.class).getGold()));
            gold.setText(getGameState().getDouble("playerGold").toString());
            score+=enemy.getComponent(EnemyDataComponent.class).getScore();
            getGameState().setValue("HighScore",score);
            highscore.setText("Score: "+getGameState().getInt("HighScore").toString());
            removeEnemyFromWorld(enemy);
        }
    }

    //Game over screen
    private void gameOver() {
        LoseMusic=getAssetLoader().loadMusic("game-lose.mp3");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(LoseMusic);
        getDisplay().showMessageBox("Game Over. Thanks for playing!"+'\n'+"Score: "+score, getGameController()::exit);
    }

    //Game clear screen
    private void gameCleared(){
        SuccessMusic=getAssetLoader().loadMusic("Victory!.wav");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(SuccessMusic);
        getDisplay().showMessageBox("Game Cleared!!! Thanks for playing!"+'\n'+"Score: "+score, getGameController()::exit);
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

    //add a list of location for tower placement
    public void addListofWorldBound(LinkedList<TowerLocationInfo> linkedList){
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


   //Check if the mouse click location is inside any defined area for tower placement
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

    //Get the area for tower placement according to mouse click location
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

    //Get the index of the area in the list of area
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

    public static void main(String[] args) {
        launch(args);
    }
}
