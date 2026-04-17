import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A single zombie that patrols a bounded area.
 * Patrol data: { startX, startY, minX, maxX, minY, maxY, speedX, speedY }
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class Zombie {

    private static final int WALK_FRAMES = 3;
    private static final int CELL_W_PX  = 192;
    private static final int CELL_H_PX  = 256;
    private static final int SCALE      = 3;
    private static final int FRAME_W    = CELL_W_PX / SCALE;
    private static final int FRAME_H    = CELL_H_PX / SCALE;

    public static final int WIDTH  = 50;
    public static final int HEIGHT = 60;

    public float x, y;
    private float speedX, speedY;
    private final float minX, maxX, minY, maxY;

    /** Frames are shared across all Zombie instances (loaded once). */
    private static BufferedImage[] sharedFrames;

    public Zombie(float[] patrol) {
        x      = patrol[0];
        y      = patrol[1];
        minX   = patrol[2];
        maxX   = patrol[3];
        minY   = patrol[4];
        maxY   = patrol[5];
        speedX = patrol[6];
        speedY = patrol[7];
    }

    /** Call once with the full ZombieSpriteSheet image. */
    public static void loadSprites(BufferedImage sheet) {
        // Walking animation is at row 3 (y=768) in the sprite sheet
        BufferedImage walkStrip = SpriteSheet.crop(sheet, 0, 768, CELL_W_PX * WALK_FRAMES, CELL_H_PX);
        sharedFrames = SpriteSheet.sliceRow(walkStrip, WALK_FRAMES, FRAME_W, FRAME_H);
    }

    public void update() {
        x += speedX;
        y += speedY;
        if (x < minX || x > maxX) speedX *= -1;
        if (y < minY || y > maxY) speedY *= -1;
    }

    public void draw(Graphics2D g, long frameCount) {
        int frame = (int)(frameCount / 10) % WALK_FRAMES;
        g.drawImage(sharedFrames[frame], (int) x, (int) y, null);
    }
}
