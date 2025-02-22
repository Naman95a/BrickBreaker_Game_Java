import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements ActionListener {
    private final int WIDTH = 600;
    private final int HEIGHT = 400;
    private final int PADDLE_HEIGHT = 10;
    private final int BALL_SIZE = 15;
    private final int BRICK_ROWS = 5;
    private final int BRICK_COLS = 7;

    private Timer timer;
    private int paddleX;
    private int paddleWidth;
    private int ballX, ballY;
    private int ballXDir, ballYDir;
    private Brick[][] bricks;
    private int score;
    private int lives;
    private int level;
    private boolean gameOver;
    private JButton retryButton;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null); // Use null layout for custom positioning
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                movePaddle(e);
            }
        });

        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;

        initializeLevel();
        timer = new Timer(10, this);

        // Initialize the Retry button
        retryButton = new JButton("Retry");
        retryButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 + 50, 100, 30);
        retryButton.setVisible(false); // Initially hidden
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame(); // Reset the game when Retry is clicked
            }
        });
        add(retryButton);
    }

    private void initializeLevel() {
        paddleWidth = 100 - (level * 10); // Reduce paddle size with each level
        paddleX = (WIDTH - paddleWidth) / 2;
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;
        ballXDir = -1 * level; // Increase ball speed with each level
        ballYDir = -2 * level;

        // Initialize bricks
        bricks = new Brick[BRICK_ROWS][BRICK_COLS];
        int brickWidth = WIDTH / BRICK_COLS;
        int brickHeight = 20;
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                bricks[i][j] = new Brick(j * brickWidth, i * brickHeight, brickWidth, brickHeight);
            }
        }
    }

    public void startGame() {
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw paddle
        g.setColor(Color.GREEN);
        g.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT - 10, paddleWidth, PADDLE_HEIGHT);

        // Draw ball
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw bricks
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                if (!bricks[i][j].isDestroyed()) {
                    bricks[i][j].draw(g);
                }
            }
        }

        // Draw score, lives, and level
        g.setColor(Color.green);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, WIDTH - 100, 20);
        g.drawString("Level: " + level, WIDTH / 2 - 40, 20);

        // Draw game over message
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(Color.RED);
            g.drawString("Game Over", WIDTH / 2 - 100, HEIGHT / 2);
            retryButton.setVisible(true); // Show the Retry button when game is over
        }
    }

    private void movePaddle(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && paddleX > 0) {
            paddleX -= 20;
        }
        if (key == KeyEvent.VK_RIGHT && paddleX < WIDTH - paddleWidth) {
            paddleX += 20;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveBall();
            checkCollisions();
            checkLevelCompletion();
        }
        repaint();
    }

    private void moveBall() {
        ballX += ballXDir;
        ballY += ballYDir;

        if (ballX < 0 || ballX > WIDTH - BALL_SIZE) {
            ballXDir = -ballXDir;
        }
        if (ballY < 0) {
            ballYDir = -ballYDir;
        }
        if (ballY > HEIGHT - BALL_SIZE) {
            lives--;
            if (lives > 0) {
                // Reset ball and paddle position
                ballX = WIDTH / 2;
                ballY = HEIGHT / 2;
                ballXDir = -1 * level;
                ballYDir = -2 * level;
                paddleX = (WIDTH - paddleWidth) / 2;
            } else {
                gameOver = true;
                timer.stop();
            }
        }
    }

    private void checkCollisions() {
        if (new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE).intersects(new Rectangle(paddleX, HEIGHT - PADDLE_HEIGHT - 10, paddleWidth, PADDLE_HEIGHT))) {
            ballYDir = -ballYDir;
        }

        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                Brick brick = bricks[i][j];
                if (!brick.isDestroyed() && new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE).intersects(brick.getBounds())) {
                    brick.setDestroyed(true);
                    ballYDir = -ballYDir;
                    score += 10;
                }
            }
        }
    }

    private void checkLevelCompletion() {
        boolean levelCleared = true;
        for (int i = 0; i < BRICK_ROWS; i++) {
            for (int j = 0; j < BRICK_COLS; j++) {
                if (!bricks[i][j].isDestroyed()) {
                    levelCleared = false;
                    break;
                }
            }
        }

        if (levelCleared) {
            level++;
            initializeLevel(); // Set up the next level
        }
    }

    private void resetGame() {
        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;
        retryButton.setVisible(false); // Hide the Retry button
        initializeLevel();
        timer.start(); // Restart the timer
    }
}
