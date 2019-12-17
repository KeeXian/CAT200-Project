package com.almasb.fxglgames.td;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.GameView;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxglgames.td.collision.BulletEnemyHandler;
import com.almasb.fxglgames.td.enemy.EnemyDataComponent;
import com.almasb.fxglgames.td.event.BulletHitEnemy;
import com.almasb.fxglgames.td.event.EnemyReachedGoalEvent;
import com.almasb.fxglgames.td.tower.TowerDataComponent;
import com.almasb.fxglgames.td.tower.TowerIcon;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
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
import java.util.*;

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
    private double player_gold = 2000;
    private int score=0;

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

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Minimalist Tower Defense");
        settings.setVersion("2.0");
        settings.setWidth(768);
        settings.setHeight(600);
        settings.setManualResizeEnabled(true);
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(true);
        settings.setProfilingEnabled(true);
        settings.setCloseConfirmation(true);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput() {
        Input input = getInput();
        addListofWorldBound(list);
        input.addAction(new UserAction("Place/Upgrade Tower") {
            @Override
            protected void onActionBegin() {
                if (checkValidTowerLocation(input.getMousePositionWorld())) {
                    placeTower(input.getMousePositionWorld());
                }
                else {
                    Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld());
                    if(tempRect!=null){
                        double x_coor = tempRect.getMinX() + tempRect.getWidth() / 2.0;
                        double y_coor = tempRect.getHeight() / 2.0 + tempRect.getMinY();
                        List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(x_coor - 32, y_coor - 60));
                        if(!tower_chosen.isEmpty()){
                            Entity tower=tower_chosen.get(0);
                            if(tower.getComponent(TowerDataComponent.class).getLevel()<3){
                                getDisplay().showConfirmationBox("Do you want to upgrade the tower to level " +
                                        (tower.getComponent(TowerDataComponent.class).getLevel()+1) + "?"+'\n'+"Cost: "+
                                        tower.getComponent(TowerDataComponent.class).getUpgradeCost()+" gold", yes -> {
                                            if(yes) {
                                                if(getGameState().getDouble("playerGold")>=tower.getComponent(TowerDataComponent.class).getUpgradeCost()) {
                                                    player_gold = getGameState().getDouble("playerGold") - tower.getComponent(TowerDataComponent.class).getUpgradeCost();
                                                    getGameState().setValue("playerGold", player_gold);
                                                    gold.setText(Double.toString(player_gold));
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

        input.addAction(new UserAction("Remove Tower") {
            @Override
            protected void onAction() {
                int index;
                Rectangle2D tempRect=getWorldBoundOnPoint(input.getMousePositionWorld());
                if(tempRect!=null) {
                    double x_coor = tempRect.getMinX() + tempRect.getWidth() / 2.0;
                    double y_coor = tempRect.getHeight() / 2.0 + tempRect.getMinY();
                    List<Entity> tower_chosen = getGameWorld().getEntitiesAt(new Point2D(x_coor - 32, y_coor - 60));
                    index = getObjIndexOnPoint(input.getMousePositionWorld());
                    list.get(index).setOccupied(false);
                    if (!tower_chosen.isEmpty()) {
                            CoinMusic = getAssetLoader().loadMusic("coin.mp3");
                            getAudioPlayer().stopMusic(CoinMusic);
                            getAudioPlayer().playMusic(CoinMusic);
                            player_gold += tower_chosen.get(0).getComponent(TowerDataComponent.class).getPrice() / 2.0;
                            getGameState().setValue("playerGold", player_gold);
                            gold.setText(getGameState().getDouble("playerGold").toString());
                            tower_chosen.get(0).removeFromWorld();
                    }
                }
            }
        }, MouseButton.SECONDARY);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("numEnemies", levelEnemies);
        vars.put("playerGold",player_gold);
        vars.put("HighScore",score);
    }

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
    private int selectedLevel = 1;
    private int enemyIndex = 1;
    private Text gold= new Text(Double.toString(player_gold));
    private Text highscore=new Text(Integer.toString(score));

    @Override
    protected void initUI() {
        Rectangle uiBG = new Rectangle(getAppWidth(), 70);
        uiBG.setTranslateY(530);
        uiBG.setFill(Color.DARKGREY);
        getGameScene().addUINode(uiBG);
        gold.setText(getGameState().getDouble("playerGold").toString());
        gold.setFill(Color.BLACK);
        gold.setTranslateX(20);
        gold.setTranslateY(40);
        getGameScene().addUINode(gold);
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

    private void placeTower(Point2D point) {
        Rectangle2D tempRect=getWorldBoundOnPoint(point);
        double x_coor=tempRect.getMinX()+tempRect.getWidth()/2.0;
        double y_coor = tempRect.getHeight()/2.0+tempRect.getMinY();
            getGameWorld().spawn("Tower",
                    new SpawnData(x_coor-32, y_coor-60)
                            .put("texture", textures.get(selectedIndex - 1))
                            .put("index", selectedIndex)
                            .put("Position", new Point2D(x_coor-32, y_coor-60)));
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
        getDisplay().showMessageBox("Game Over. Thanks for playing!"+'\n'+"Score: "+score, getGameController()::exit);
    }

    private void gameCleared(){
        SuccessMusic=getAssetLoader().loadMusic("Victory!.wav");
        getAudioPlayer().stopMusic(BGM);
        getAudioPlayer().playMusic(SuccessMusic);
        getDisplay().showMessageBox("Game Cleared!!! Thanks for playing!"+'\n'+"Score: "+score, getGameController()::exit);
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
        TowerDataComponent tower = TowerDataComponent.makeTower(selectedIndex);
        if(tower.getPrice()>getGameState().getDouble("playerGold"))
            trgInsufficientFunds();
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

    public static void main(String[] args) {
        launch(args);
    }
}
