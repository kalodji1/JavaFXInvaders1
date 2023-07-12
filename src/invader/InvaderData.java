package invader;

import java.util.Random;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class InvaderData {
	public ImageView shooter;
	public Pane root;
	public ImageView enemy;
	public Random random;
	public int enemyDirection;
	public int highScore;
	public int score;
	public Text playerNameText;
	public Text scoreText;
	public Text highScoreText;
	public int level;
	public int enemySpeed;
	public int projectileSpeed;
	public Text levelIndicator;
	public String playerName;
	public long lastShotTime;

	public InvaderData(int enemyDirection, int highScore, int score, int level, long lastShotTime) {
		this.enemyDirection = enemyDirection;
		this.highScore = highScore;
		this.score = score;
		this.level = level;
		this.lastShotTime = lastShotTime;
	}
}