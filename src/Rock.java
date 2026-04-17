import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * A falling rock hazard. Falls vertically and resets to a random X at the top.
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class Rock {

    public static final float RADIUS = 20f;
    private static final int  SPEED  = 2;

    public float x, y;
    private final int screenW, screenH;
    private final Random rng;

    public Rock(float x, float y, int screenW, int screenH, Random rng) {
        this.x       = x;
        this.y       = y;
        this.screenW = screenW;
        this.screenH = screenH;
        this.rng     = rng;
    }

    public void update() {
        y += SPEED;
        if (y >= screenH + 50) {
            y = -(rng.nextFloat() * 1000);
            x = 120 + rng.nextFloat() * (screenW - 120);
        }
    }

    public void draw(Graphics2D g) {
        int ix = (int)(x - RADIUS);
        int iy = (int)(y - RADIUS);
        int d  = (int)(RADIUS * 2);
        g.setColor(new Color(156, 104, 50));
        g.fillOval(ix, iy, d, d);
        g.setColor(Color.BLACK);
        g.drawOval(ix, iy, d, d);
    }

    public boolean overlapsPlayer(float px, float py) {
        return Math.abs(px - x) < RADIUS * 2 && Math.abs(py - y) < RADIUS * 2;
    }
}
