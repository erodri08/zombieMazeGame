import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * The player character (robot).
 * Sprite size and hitbox scale with the level's playerScale factor.
 *
 * Hitbox is offset from the sprite origin to align with the robot's actual
 * visible pixels.  The sprite sheet cell (192×256) has ~29% transparent
 * padding at the top and ~16% on each side, so without an offset the hitbox
 * sat far above and wider than the visible character, causing the asymmetric
 * wall gaps (too much gap above, clipping below).
 *
 * hitOffX / hitOffY: pixels from the sprite top-left to the hitbox top-left.
 * All collision and overlap calls use (x + hitOffX, y + hitOffY) as the
 * hitbox origin.
 *
 * @author Ethan Rodrigues
 */
public class Player {

    private static final int CELL_W      = 192;
    private static final int CELL_H      = 256;
    private static final int BASE_SCALE  = 3;
    private static final int WALK_FRAMES = 8;

    // Transparent-padding fractions measured from the sprite sheet
    private static final float TOP_PAD_FRAC  = 74f / 256f; 
    private static final float SIDE_PAD_FRAC = 31f / 192f; 

    // Extra inset so the hitbox is a couple px inside the visible silhouette
    private static final int INSET = 2;

    public static final int MAX_LIVES = 10;

    // Rendered sprite dimensions
    public int frameW, frameH;

    // Hitbox offset from sprite origin, and hitbox size
    public int hitOffX, hitOffY;
    public int hitW, hitH;

    public float x, y;
    public float speed = 5; 
    public int lives = MAX_LIVES;

    private int invincibleTicks = 0;
    private static final int INVINCIBLE_DURATION = 90;

    private BufferedImage imgStill;
    private BufferedImage[] walkFrames;

    private CollisionChecker collisionChecker;

    public Player() {}

    /** Load sprites and compute hitbox geometry at a given scale (1.0 = normal). */
    public void loadSprites(BufferedImage sheet, float scale) {
        frameW = Math.max(20, (int)(CELL_W / BASE_SCALE * scale));
        frameH = Math.max(24, (int)(CELL_H / BASE_SCALE * scale));

        int topPad  = Math.round(TOP_PAD_FRAC  * frameH);
        int sidePad = Math.round(SIDE_PAD_FRAC * frameW);

        hitOffX = sidePad + INSET;
        hitOffY = topPad  + INSET;
        hitW    = frameW - sidePad * 2 - INSET * 2;
        hitH    = frameH - topPad      - INSET * 2; 

        // Speed scales with the player size: bigger = faster, smaller = slower
        // Base speed 5 at scale 1.0; range roughly 3.5–5.5
        speed = Math.max(2.5f, 4.5f * scale);

        imgStill = SpriteSheet.cropAndScale(sheet, 0, 0, CELL_W, CELL_H, frameW, frameH);
        BufferedImage walkStrip = SpriteSheet.crop(sheet, 0, 1024, CELL_W * WALK_FRAMES, CELL_H);
        walkFrames = SpriteSheet.sliceRow(walkStrip, WALK_FRAMES, frameW, frameH);
    }

    /** World-space X of the hitbox left edge. */
    public float hitX() { return x + hitOffX; }
    /** World-space Y of the hitbox top edge. */
    public float hitY() { return y + hitOffY; }

    public void draw(Graphics2D g, long frameCount, boolean moving) {
        if (invincibleTicks > 0 && (invincibleTicks / 6) % 2 == 0) return;
        BufferedImage frame = moving
                ? walkFrames[(int)(frameCount / 3) % WALK_FRAMES]
                : imgStill;
        g.drawImage(frame, (int) x, (int) y, null);
    }

    public void handleMovement(Set<Character> keysDown, CollisionChecker cc) {
        this.collisionChecker = cc;
        if (keysDown.contains('w') || keysDown.contains('\u2191')) {
            if (cc.canMoveUp(hitX(), hitY(), hitW, hitH, speed))    y -= speed;
        }
        if (keysDown.contains('s') || keysDown.contains('\u2193')) {
            if (cc.canMoveDown(hitX(), hitY(), hitW, hitH, speed))  y += speed;
        }
        if (keysDown.contains('a') || keysDown.contains('\u2190')) {
            if (cc.canMoveLeft(hitX(), hitY(), hitW, hitH, speed))  x -= speed;
        }
        if (keysDown.contains('d') || keysDown.contains('\u2192')) {
            if (cc.canMoveRight(hitX(), hitY(), hitW, hitH, speed)) x += speed;
        }
    }

    public boolean isMoving(Set<Character> keysDown) {
        return keysDown.contains('w') || keysDown.contains('a')
            || keysDown.contains('s') || keysDown.contains('d')
            || keysDown.contains('\u2190') || keysDown.contains('\u2191')
            || keysDown.contains('\u2192') || keysDown.contains('\u2193');
    }

    public boolean overlaps(float ex, float ey, float ew, float eh) {
        return CollisionChecker.aabb(hitX(), hitY(), hitW, hitH, ex, ey, ew, eh);
    }

    public void loseLife(float respawnX, float respawnY) {
        if (invincibleTicks > 0) return;
        lives--;
        x = respawnX;
        y = respawnY;
        if (collisionChecker != null) {
            float[] safe = collisionChecker.pushOut(hitX(), hitY(), hitW, hitH);
            x = safe[0] - hitOffX;
            y = safe[1] - hitOffY;
        }
        invincibleTicks = INVINCIBLE_DURATION;
    }

    public void tickInvincibility() {
        if (invincibleTicks > 0) invincibleTicks--;
    }

    public boolean isInvincible() { return invincibleTicks > 0; }

    /** Make the player briefly invincible (used when touching a zombie to heal it). */
    public void forceInvincible(int ticks) {
        invincibleTicks = Math.max(invincibleTicks, ticks);
    }

    public void reset(float startX, float startY, CollisionChecker cc) {
        this.collisionChecker = cc;
        x = startX; y = startY;
        lives = MAX_LIVES;
        invincibleTicks = 0;
        if (cc != null) {
            float[] safe = cc.pushOut(hitX(), hitY(), hitW, hitH);
            x = safe[0] - hitOffX;
            y = safe[1] - hitOffY;
        }
    }

    public void resetPosition(float startX, float startY, CollisionChecker cc) {
        this.collisionChecker = cc;
        x = startX; y = startY;
        invincibleTicks = 0;
        if (cc != null) {
            float[] safe = cc.pushOut(hitX(), hitY(), hitW, hitH);
            x = safe[0] - hitOffX;
            y = safe[1] - hitOffY;
        }
    }

    public int getFrameW() { return frameW; }
    public int getFrameH() { return frameH; }
}
