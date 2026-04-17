import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * The player character (robot). Handles movement, animation, lives, and invincibility.
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class Player {

    // Sprite sheet constants (matches original sprite sheet layout)
    private static final int WALK_FRAMES  = 8;
    private static final int CELL_W_PX   = 192;  // raw cell width in sheet
    private static final int CELL_H_PX   = 256;
    private static final int SCALE        = 3;
    private static final int FRAME_W      = CELL_W_PX / SCALE;  // 64
    private static final int FRAME_H      = CELL_H_PX / SCALE;  // 85

    // Hitbox (slightly smaller than sprite to be fair)
    public static final int WIDTH  = 50;
    public static final int HEIGHT = 60;

    public static final int MAX_LIVES = 5;

    public float x, y;
    public final float speed = 5;
    public int lives = MAX_LIVES;

    // Invincibility window after a hit (~1.5 s at 60 fps)
    private int invincibleTicks = 0;
    private static final int INVINCIBLE_DURATION = 90;

    // Sprites
    private BufferedImage imgStill;
    private BufferedImage[] walkFrames;

    public Player() {}

    /** Load and slice the robot sprite sheet. Called once during game setup. */
    public void loadSprites(BufferedImage sheet) {
        // Still frame: row 0, column 0
        imgStill = SpriteSheet.cropAndScale(sheet,
                0, 0, CELL_W_PX, CELL_H_PX,
                FRAME_W, FRAME_H);

        // Walking strip: row 4 (y=1024), 8 frames across
        BufferedImage walkStrip = SpriteSheet.crop(sheet, 0, 1024, CELL_W_PX * WALK_FRAMES, CELL_H_PX);
        walkFrames = SpriteSheet.sliceRow(walkStrip, WALK_FRAMES, FRAME_W, FRAME_H);
    }

    /**
     * Draw the player. frameCount drives the walk animation.
     */
    public void draw(Graphics2D g, long frameCount, boolean moving) {
        BufferedImage frame = moving
                ? walkFrames[(int)(frameCount / 3) % WALK_FRAMES]
                : imgStill;
        // Flash during invincibility: skip every other 6 frames
        if (invincibleTicks > 0 && (invincibleTicks / 6) % 2 == 0) return;
        g.drawImage(frame, (int) x, (int) y, null);
    }

    /**
     * Apply movement based on currently held keys and wall checks.
     * keysDown is the set of currently pressed key chars (lowercased).
     */
    public void handleMovement(Set<Character> keysDown, CollisionChecker walls, int keysCollected) {
        if (keysDown.contains('w') && walls.canMoveUp(x, y))                   y -= speed;
        if (keysDown.contains('s') && walls.canMoveDown(x, y))                 y += speed;
        if (keysDown.contains('a') && walls.canMoveLeft(x, y))                 x -= speed;
        if (keysDown.contains('d') && walls.canMoveRight(x, y, keysCollected)) x += speed;
    }

    public boolean isMoving(Set<Character> keysDown) {
        return keysDown.contains('w') || keysDown.contains('a')
            || keysDown.contains('s') || keysDown.contains('d');
    }

    /** AABB overlap test against an enemy rectangle. */
    public boolean overlaps(float ex, float ey, float ew, float eh) {
        return x < ex + ew && x + WIDTH > ex && y < ey + eh && y + HEIGHT > ey;
    }

    /** Lose a life and teleport back to the start position. */
    public void loseLife(float startX, float startY) {
        if (invincibleTicks > 0) return;
        lives--;
        x = startX;
        y = startY;
        invincibleTicks = INVINCIBLE_DURATION;
    }

    public void tickInvincibility() {
        if (invincibleTicks > 0) invincibleTicks--;
    }

    public boolean isInvincible() { return invincibleTicks > 0; }

    public void reset(float startX, float startY) {
        x = startX;
        y = startY;
        lives = MAX_LIVES;
        invincibleTicks = 0;
    }
}
