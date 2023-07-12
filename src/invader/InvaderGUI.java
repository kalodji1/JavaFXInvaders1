package invader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class InvaderGUI {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int SHOOTER_WIDTH = 50;
    private static final int SHOOTER_HEIGHT = 20;
    private static final int SHOOTER_SPEED = 10;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int ENEMY_WIDTH = 40;
    private static final int ENEMY_HEIGHT = 20;
    private static final int EXPLOSION_RADIUS = 50;
    private static final int RESPAWN_DURATION = 2000; // Duration in milliseconds
    private static final long MIN_SHOT_INTERVAL = 400; // Minimum interval between shots in milliseconds

    private InvaderData data;

    public InvaderGUI(InvaderData data) {
        this.data = data;
    }

    public void start(Stage primaryStage) {
        data.random = new Random();
        data.root = new Pane();
        Scene scene = new Scene(data.root, WINDOW_WIDTH, WINDOW_HEIGHT);

        data.root.getStylesheets().addAll(getClass().getResource("styles.css").toExternalForm(),
                getClass().getResource("game.css").toExternalForm());

        String[] playerData = readPlayerDataFromFile();
        if (playerData == null) {
            data.playerName = askPlayerName();
            data.highScore = 0;
            savePlayerDataToFile(data.playerName, data.highScore);
        } else {
            data.playerName = playerData[0];
            data.highScore = Integer.parseInt(playerData[1]);
        }

        createShooter();
        createPlayerText();
        createScoreText();
        createHighScoreText(); // Create high score text
        createLevelIndicator();

        HashSet<KeyCode> pressedKeys = new HashSet<>();

        scene.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());

            if (pressedKeys.contains(KeyCode.LEFT) && data.shooter.getLayoutX() > 0) {
                data.shooter.setLayoutX(data.shooter.getLayoutX() - SHOOTER_SPEED);
            }

            if (pressedKeys.contains(KeyCode.RIGHT) && data.shooter.getLayoutX() + data.shooter.getFitWidth() < WINDOW_WIDTH) {
                data.shooter.setLayoutX(data.shooter.getLayoutX() + SHOOTER_SPEED);
            }

            if (pressedKeys.contains(KeyCode.SPACE)) {
                shootProjectile();
            }
        });

        scene.setOnKeyReleased(e -> {
            pressedKeys.remove(e.getCode());
        });



        data.root.getChildren().addAll(data.shooter, data.playerNameText, data.scoreText, data.highScoreText,
                data.levelIndicator);

        primaryStage.setTitle("Space Invader Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start enemy movement
        createEnemy();
        setLevelSpeeds();
        animateEnemy();

        primaryStage.setOnCloseRequest(event -> {
            savePlayerDataToFile(data.playerName, data.highScore);
        });
    }

    private String[] readPlayerDataFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("playerData/PlayerData.sav"))) {
            String playerName = reader.readLine();
            String highScore = reader.readLine();
            if (playerName != null && highScore != null) {
                return new String[] { playerName, highScore };
            }
        } catch (IOException e) {
            // File does not exist or error occurred while reading
        }
        return null;
    }

    private String askPlayerName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Player Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter your name:");
        dialog.initStyle(StageStyle.UNDECORATED);

        // Show the dialog and wait for user input
        dialog.showAndWait();

        // Retrieve the entered name or use a default value if no input was provided
        String playerName = dialog.getResult();
        return playerName != null && !playerName.isEmpty() ? playerName : "Player";
    }

    // allows us to save players data
    private void savePlayerDataToFile(String playerName, int highScore) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("playerData/PlayerData.sav"))) {
            writer.write(playerName);
            writer.newLine();
            writer.write(Integer.toString(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createShooter() {
        Image shooterImage = new Image("file:images/shooter.png");
        data.shooter = new ImageView(shooterImage);
        data.shooter.setFitWidth(SHOOTER_WIDTH);
        data.shooter.setFitHeight(SHOOTER_HEIGHT);
        data.shooter.setLayoutX(WINDOW_WIDTH / 2 - SHOOTER_WIDTH / 2);
        data.shooter.setLayoutY(WINDOW_HEIGHT - SHOOTER_HEIGHT - 10);
    }

    private void createEnemy() {
        Image enemyImage = new Image("file:images/enemy.png");
        data.enemy = new ImageView(enemyImage);
        data.enemy.setFitWidth(ENEMY_WIDTH);
        data.enemy.setFitHeight(ENEMY_HEIGHT);
        data.enemy.setLayoutX(data.random.nextDouble() * (WINDOW_WIDTH - ENEMY_WIDTH));
        data.enemy.setLayoutY(10);
        data.root.getChildren().add(data.enemy);
    }

    private void createPlayerText() {
        data.playerNameText = new Text("Player: " + data.playerName);
        data.playerNameText.getStyleClass().add("player-name");
        data.playerNameText.setLayoutX(10);
        data.playerNameText.setLayoutY(30);
    }

    private void createScoreText() {
        data.scoreText = new Text("Score: 0");
        data.scoreText.getStyleClass().add("score");
        data.scoreText.setLayoutX(10);
        data.scoreText.setLayoutY(60);
        updateScoreText();
    }

    private void createHighScoreText() {
        data.highScoreText = new Text("High Score: " + data.highScore);
        data.highScoreText.getStyleClass().add("high-score");
        data.highScoreText.setLayoutX(10);
        data.highScoreText.setLayoutY(90);
    }

    private void createLevelIndicator() {
        data.levelIndicator = new Text("Level: " + data.level);
        data.levelIndicator.getStyleClass().add("level");
        data.levelIndicator.setLayoutX(10);
        data.levelIndicator.setLayoutY(120);
    }

    private void setLevelSpeeds() {
        switch (data.level) {
            case 1:
                data.enemySpeed = 3;
                data.projectileSpeed = 5;
                break;
            case 2:
                data.enemySpeed = 5;
                data.projectileSpeed = 7;
                break;
            case 3:
                data.enemySpeed = 7;
                data.projectileSpeed = 10;
                break;
            // Add more cases for higher levels and adjust the speeds accordingly
            default:
                data.enemySpeed = 7;
                data.projectileSpeed = 10;
                break;
        }
    }

    private void animateEnemy() {
        final double[] target = { data.random.nextDouble() * (WINDOW_WIDTH - ENEMY_WIDTH),
                data.random.nextDouble() * (WINDOW_HEIGHT - ENEMY_HEIGHT - 200) + 10 }; // Store target coordinates in an array

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(16), event -> {
                    double currentX = data.enemy.getLayoutX();
                    double currentY = data.enemy.getLayoutY();

                    double deltaX = target[0] - currentX;
                    double deltaY = target[1] - currentY;

                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                    // Check for potential collision with projectile
                    boolean potentialCollision = false;

                    double projectileX = data.shooter.getLayoutX() + data.shooter.getFitWidth() / 2;
                    double projectileY = data.shooter.getLayoutY() - PROJECTILE_HEIGHT / 2;
                    double projectileSpeedX = 0;
                    double projectileSpeedY = -data.projectileSpeed;

                    // Predict the future position of the projectile
                    double timeToReachEnemy = distance / data.enemySpeed;
                    double projectileFutureX = projectileX + projectileSpeedX * timeToReachEnemy;
                    double projectileFutureY = projectileY + projectileSpeedY * timeToReachEnemy;

                    // Check if the predicted position intersects the enemy's path
                    if (projectileFutureX >= currentX && projectileFutureX <= currentX + ENEMY_WIDTH
                            && projectileFutureY >= currentY && projectileFutureY <= currentY + ENEMY_HEIGHT) {
                        potentialCollision = true;
                    }

                    // Adjust target if enemy is close to the projectile
                    if (potentialCollision) {
                        target[0] = currentX; // Enemy does not move horizontally
                        target[1] = currentY; // Enemy does not move vertically

                        // Change direction if enemy collides with projectile
                        if (data.enemy.getBoundsInParent().intersects(data.shooter.getBoundsInParent())) {
                            data.enemyDirection *= -1;
                        }
                    }

                    if (distance > data.enemySpeed) {
                        double ratio = data.enemySpeed / distance;
                        double moveX = ratio * deltaX;
                        double moveY = ratio * deltaY;

                        data.enemy.setLayoutX(currentX + moveX);
                        data.enemy.setLayoutY(currentY + moveY);
                    } else {
                        target[0] = data.random.nextDouble() * (WINDOW_WIDTH - ENEMY_WIDTH);
                        target[1] = data.random.nextDouble() * (WINDOW_HEIGHT - ENEMY_HEIGHT - 200) + 10; // Adjusted Y range
                    }

                    // Check collision with the shooter
                    if (data.enemy.getBoundsInParent().intersects(data.shooter.getBoundsInParent())) {
                        handleCollision();
                    }

                    // Reverse enemy direction if it reaches the window bounds
                    if (data.enemy.getLayoutX() <= 0 || data.enemy.getLayoutX() + ENEMY_WIDTH >= WINDOW_WIDTH) {
                        data.enemyDirection *= -1;
                    }
                }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void shootProjectile() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - data.lastShotTime < MIN_SHOT_INTERVAL) {
            return; // Don't shoot if the minimum interval hasn't passed
        }

        data.lastShotTime = currentTime; // Update the last shot time

        // Rest of the code for shooting the projectile
        Image projectileImage = new Image("file:images/projectile.png");
        ImageView projectile = new ImageView(projectileImage);
        projectile.setFitWidth(PROJECTILE_WIDTH);
        projectile.setFitHeight(PROJECTILE_HEIGHT);
        projectile.setLayoutX(data.shooter.getLayoutX() + data.shooter.getFitWidth() / 2 - PROJECTILE_WIDTH / 2);
        projectile.setLayoutY(data.shooter.getLayoutY() - PROJECTILE_HEIGHT);
        data.root.getChildren().add(projectile);


        //Audio sounds for projectiles
        String soundFile = "sounds/shoot.wav";
        try {
            File audioFile = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(16), event -> {
                    projectile.setLayoutY(projectile.getLayoutY() - data.projectileSpeed);

                    // Check collision with the enemy
                    if (data.enemy.getBoundsInParent().intersects(projectile.getBoundsInParent())) {
                        handleCollision();
                    }

                    // Remove projectile when it goes off-screen
                    if (projectile.getLayoutY() < 0) {
                        data.root.getChildren().remove(projectile);
                    }
                }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void handleCollision() {
        data.score++;
        updateScoreText();

        // Update high score if necessary
        if (data.score > data.highScore) {
            data.highScore = data.score;
            data.highScoreText.setText("High Score: " + data.highScore);
        }

        // Create explosion effect
        double explosionX = data.enemy.getLayoutX() + ENEMY_WIDTH / 2;
        double explosionY = data.enemy.getLayoutY() + ENEMY_HEIGHT / 2;

        Image explosionImage = new Image("file:images/explosion.gif");
        ImageView explosion = new ImageView(explosionImage);
        explosion.setFitWidth(EXPLOSION_RADIUS * 2);
        explosion.setFitHeight(EXPLOSION_RADIUS * 2);
        explosion.setLayoutX(explosionX - EXPLOSION_RADIUS);
        explosion.setLayoutY(explosionY - EXPLOSION_RADIUS);
        data.root.getChildren().add(explosion);

        String soundFile = "sounds/explosion.wav";
        try {
            File audioFile = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Remove enemy and explosion after a delay
        final ImageView finalExplosion = explosion; // Assign to a final variable for access in the Timeline
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(RESPAWN_DURATION), event -> {
                    data.root.getChildren().remove(finalExplosion); // Remove the explosion
                }));
        timeline.setCycleCount(1);
        timeline.play();

        // Update enemy position after the explosion
        data.enemy.setLayoutX(data.random.nextDouble() * (WINDOW_WIDTH - ENEMY_WIDTH));
        data.enemy.setLayoutY(10);

        // Check if level needs to be increased
        if (data.score % 10 == 0) {
            data.level++;
            data.levelIndicator.setText("Level: " + data.level);
            setLevelSpeeds();
        }
    }

    private void updateScoreText() {
        data.scoreText.setText("Score: " + data.score);
    }

    private int getEnemyRandomMovement(int level) {
        switch (level) {
            case 1:
                return data.random.nextInt(5) - 2; // Random movement between -2 and 2
            case 2:
                return data.random.nextInt(7) - 3; // Random movement between -3 and 3
            case 3:
                return data.random.nextInt(9) - 4; // Random movement between -4 and 4
            default:
                return 0;
        }
    }
}