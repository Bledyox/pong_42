import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

public class Pong extends Canvas implements Runnable, KeyListener {

    private static final String TITLE = "Pong 42";
    private static final int WIDTH = 1600, HEIGHT = 900;
    private final JFrame frame;
    private final Thread thread;
    private final int BALL_DIAMETER = (int) (WIDTH * 0.01);
    private final int PADDLE_WIDTH = (int) (WIDTH * 0.01);
    private final int PADDLE_HEIGHT = (int) (HEIGHT * 0.2);
    private final int PADDLE_OFFSET = (int) (WIDTH * 0.01);
    private final Font font = new Font("Consolas", Font.PLAIN, (int) (HEIGHT * 0.2));
    private boolean runFlag, pauseFlag;
    private int scoreLeft, scoreRight;
    private double ballX, ballY, ballSpeedX, ballSpeedY;
    private double paddleLeftY, paddleRightY, paddleLeftSpeedY, paddleRightSpeedY;

    public Pong() {
        this.frame = new JFrame(TITLE);
        this.frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this);
        this.frame.addKeyListener(this);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);
        this.frame.setIgnoreRepaint(true);
        this.thread = new Thread(this, TITLE);
    }



    public void start() {
        if (runFlag) return;
        runFlag = true;
        frame.setVisible(true);
        thread.start();
    }

    public void stop() {
        if (!runFlag) return;
        runFlag = false;
        frame.setVisible(false);
    }

    public void pause() {
        pauseFlag = !pauseFlag;
    }

    @Override
    public void keyPressed(KeyEvent key) {
        double PADDLE_V = 1;
        if (key.getKeyCode() == KeyEvent.VK_W) paddleLeftSpeedY = -PADDLE_V;
        if (key.getKeyCode() == KeyEvent.VK_S) paddleLeftSpeedY = PADDLE_V;
        if (key.getKeyCode() == KeyEvent.VK_UP) paddleRightSpeedY = -PADDLE_V;
        if (key.getKeyCode() == KeyEvent.VK_DOWN) paddleRightSpeedY = PADDLE_V;
        if (key.getKeyCode() == KeyEvent.VK_ESCAPE) pause();
    }

    @Override
    public void keyReleased(KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_W) paddleLeftSpeedY = 0;
        if (key.getKeyCode() == KeyEvent.VK_S) paddleLeftSpeedY = 0;
        if (key.getKeyCode() == KeyEvent.VK_UP) paddleRightSpeedY = 0;
        if (key.getKeyCode() == KeyEvent.VK_DOWN) paddleRightSpeedY = 0;
    }

    @Override
    public void keyTyped(KeyEvent key) {
    }

    @Override
    public void run() {
        resetBall();
        resetPaddles();
        while (runFlag) {
            try {
                if (pauseFlag) {
                    render();
                    int PAUSE_SLEEP_TIME = 100;
                    Thread.sleep(PAUSE_SLEEP_TIME);
                } else {
                    updatePaddleLeft();
                    updatePaddleRight();
                    updateBall();
                    render();
                    int SLEEP_TIME = 1;
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException interrupt) {
                interrupt.printStackTrace();
            }
        }
    }

    public void resetBall() {
        ballX = ((double) (getWidth() - BALL_DIAMETER) / 2);
        ballY = ((double) (getHeight() - BALL_DIAMETER) / 2);
        ballSpeedX = 1;
        ballSpeedY = 1;
    }

    public void resetPaddles() {
        double paddleY = ((double) (getHeight() - PADDLE_HEIGHT) / 2);
        paddleLeftY = paddleRightY = paddleY;
        paddleLeftSpeedY = paddleRightSpeedY = 0;
    }

    public void resetScores() {
        scoreLeft = scoreRight = 0;
    }

    public void updateBall() {
        double ballDeltaX = ballX + ballSpeedX;
        double ballDeltaY = ballY + ballSpeedY;
        double leftLine = PADDLE_OFFSET + PADDLE_WIDTH;
        double rightLine = getWidth() - PADDLE_OFFSET - PADDLE_WIDTH;

        if (ballDeltaY < 0) {
            ballDeltaY *= -1;
            ballSpeedY *= -1;
        } else if ((ballDeltaY + BALL_DIAMETER) > getHeight()) {
            ballDeltaY -= 2 * ((ballDeltaY + BALL_DIAMETER) - getHeight());
            ballSpeedY *= -1;
        }

        double MAX_BOUNCE_ANGLE = (Math.PI / 12);
        if (ballDeltaX < leftLine && ballX >= leftLine) {
            double intersectY = ballY - ((ballX - leftLine) * (ballY - ballDeltaY)) / (ballX - ballDeltaX);
            if (intersectY >= paddleLeftY && intersectY <= paddleLeftY + PADDLE_HEIGHT) {
                double relativeIntersectY = (paddleLeftY + ((double) PADDLE_HEIGHT / 2)) - intersectY;
                double bounceAngle = (relativeIntersectY / ((double) PADDLE_HEIGHT / 2)) * (Math.PI / 2 - MAX_BOUNCE_ANGLE);
                double ballSpeed = Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                double ballTravelLeft = (ballDeltaY - intersectY) / (ballDeltaY - ballY);
                ballSpeedX = ballSpeed * Math.cos(bounceAngle);
                ballSpeedY = ballSpeed * -Math.sin(bounceAngle);
                ballDeltaX = leftLine + ballTravelLeft * ballSpeed * Math.cos(bounceAngle);
                ballDeltaY = intersectY + ballTravelLeft * ballSpeed * Math.sin(bounceAngle);
            }
        }

        if ((ballDeltaX + BALL_DIAMETER) > rightLine && (ballX + BALL_DIAMETER) <= rightLine) {
            double intersectY = ballY - (((ballX + BALL_DIAMETER) - rightLine) * (ballY - ballDeltaY))
                    / ((ballX + BALL_DIAMETER) - (ballDeltaX + BALL_DIAMETER));
            if (intersectY >= paddleRightY && intersectY <= paddleRightY + PADDLE_HEIGHT) {
                double relativeIntersectY = (paddleRightY + ((double) PADDLE_HEIGHT / 2)) - intersectY;
                double bounceAngle = (relativeIntersectY / ((double) PADDLE_HEIGHT / 2)) * (Math.PI / 2 - MAX_BOUNCE_ANGLE);
                double ballSpeed = Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                double ballTravelLeft = (ballDeltaY - intersectY) / (ballDeltaY - ballY);
                ballSpeedX = ballSpeed * Math.cos(bounceAngle) * -1;
                ballSpeedY = ballSpeed * Math.sin(bounceAngle) * -1;
                ballDeltaX = rightLine - ballTravelLeft * ballSpeed * Math.cos(bounceAngle);
                ballDeltaY = intersectY - ballTravelLeft * ballSpeed * Math.sin(bounceAngle);
            }
        }

        if (ballDeltaX < 0) {
            scoreRight++;
            resetBall();
            return;
        } else if ((ballDeltaX + BALL_DIAMETER) > getWidth()) {
            scoreLeft++;
            resetBall();
            return;
        }

        ballX = ballDeltaX;
        ballY = ballDeltaY;
    }

    public void updatePaddleLeft() {
        double paddleLeftDeltaY = paddleLeftY + paddleLeftSpeedY;
        if (paddleLeftDeltaY < 0) paddleLeftSpeedY = 0;
        else if ((paddleLeftDeltaY + PADDLE_HEIGHT) > getHeight()) paddleLeftSpeedY = 0;
        paddleLeftY += paddleLeftSpeedY;
    }

    public void updatePaddleRight() {
        double paddleRightDeltaY = paddleRightY + paddleRightSpeedY;
        if (paddleRightDeltaY < 0) paddleRightSpeedY = 0;
        else if ((paddleRightDeltaY + PADDLE_HEIGHT) > getHeight()) paddleRightSpeedY = 0;
        paddleRightY += paddleRightSpeedY;
    }

    public void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics2D gfx = (Graphics2D) buffer.getDrawGraphics();
        gfx.setColor(Color.BLACK);
        gfx.fillRect(0, 0, WIDTH, HEIGHT);
        gfx.setColor(Color.WHITE);
        for (int i = 0; i < 15; i++) {
            if (i % 2 == 0) gfx.fillRect(((getWidth() - 2) / 2), (i * (getHeight() / 15)), 2, (getHeight() / 15));
        }
        gfx.fillOval((int) ballX, (int) ballY, BALL_DIAMETER, BALL_DIAMETER);
        gfx.fillRect((PADDLE_OFFSET), (int) paddleLeftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        gfx.fillRect(((getWidth() - PADDLE_WIDTH) - PADDLE_OFFSET), (int) paddleRightY, PADDLE_WIDTH, PADDLE_HEIGHT);
        gfx.setFont(font);
        gfx.drawString(String.valueOf(scoreLeft), (getWidth() / 5), (getHeight() / 5));
        gfx.drawString(String.valueOf(scoreRight),
                (getWidth() - gfx.getFontMetrics(font).stringWidth(String.valueOf(scoreRight))) - (getWidth() / 5),
                (getHeight() / 5));
        if (pauseFlag) {
            String PAUSE_TEXT = "Pause";
            gfx.drawString(PAUSE_TEXT, ((getWidth() - gfx.getFontMetrics(font).stringWidth(PAUSE_TEXT)) / 2), (getHeight() / 2));
        }
        gfx.dispose();
        buffer.show();
    }

}

