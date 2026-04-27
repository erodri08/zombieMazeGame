import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Falling asteroid. 
 * Renders using Asteroid.png; spins as it falls.
 * @author Ethan Rodrigues
 */
public class Rock {

    public  static final int RADIUS = 13;
    private static final int SPEED  = 1;

    // Asteroid image — loaded once, shared across all Rock instances
    private static BufferedImage asteroidImg = null;

    public float x, y;
    private float angle = 0f;        // current rotation in radians
    private float spinSpeed;         // radians per frame (randomised per rock)
    private final int screenW, screenH;
    private final Random rng;

    public Rock(float x, float y, int screenW, int screenH, Random rng) {
        this.x = x; this.y = y;
        this.screenW = screenW; this.screenH = screenH; this.rng = rng;
        this.spinSpeed = (0.02f + rng.nextFloat() * 0.05f) * (rng.nextBoolean() ? 1f : -1f);
        if (asteroidImg == null) {
            asteroidImg = SpriteSheet.scale(SpriteSheet.load("Asteroid.png"), RADIUS * 2, RADIUS * 2);
        }
    }

    public void update() {
        y += SPEED;
        angle += spinSpeed;
        if (y - RADIUS >= screenH + 50) {
            y = -(rng.nextFloat() * 1000);
            x = 60 + rng.nextFloat() * (screenW - 120);
            // Randomise spin speed on respawn for variety
            spinSpeed = (0.02f + rng.nextFloat() * 0.05f) * (rng.nextBoolean() ? 1f : -1f);
        }
    }

    public void draw(Graphics2D g) {
        int d = RADIUS * 2;
        if (asteroidImg != null) {
            // Rotate around the asteroid's centre
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(x, y);
            g.rotate(angle);
            g.drawImage(asteroidImg, -RADIUS, -RADIUS, d, d, null);
            g.setTransform(old);
        } else {
            // draw a circle if fail to load
            int ix = (int)(x - RADIUS);
            int iy = (int)(y - RADIUS);
            g.setColor(new java.awt.Color(156, 104, 50));
            g.fillOval(ix, iy, d, d);
            g.setColor(java.awt.Color.BLACK);
            g.drawOval(ix, iy, d, d);
        }
    }

    /** AABB: rock occupies [x-R, y-R, 2R, 2R]; player occupies [px, py, pw, ph]. */
    public boolean overlapsPlayer(float px, float py, int pw, int ph) {
        return CollisionChecker.aabb(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2, px, py, pw, ph);
    }
}
