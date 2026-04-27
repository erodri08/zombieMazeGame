import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A single zombie. Supports three modes:
 *   PATROL   – bounces within its patrol rectangle (levels 1-3)
 *   CHASING  – wall-aware BFS pathfinding toward the player (final level, Pac-Man style)
 *   HUMAN    – uses the human sprite sheet (after cure collected in final level)
 *
 * Size and speed scale with the level's levelScale factor.
 *
 * @author Ethan Rodrigues
 */
public class Zombie {

    public enum Mode { PATROL, CHASING, HUMAN }

    private static final int WALK_FRAMES = 3;
    private static final int CELL_W      = 192;
    private static final int CELL_H      = 256;
    private static final int BASE_SCALE  = 3;

    public static final int BASE_FRAME_W = CELL_W / BASE_SCALE;  
    public static final int BASE_FRAME_H = CELL_H / BASE_SCALE; 

    public int frameW, frameH;
    public int width, height;

    public float x, y;
    private float speedX, speedY;
    private final float minX, maxX, minY, maxY;
    private Mode mode;

    // Chase speed — always a bit slower than the player's effective speed
    private static final float BASE_CHASE_SPEED = 1.6f;
    private float chaseSpeed;

    // Wall-hugging state
    // The zombie tries the 4 cardinal directions in priority order.
    // When it can't go toward the player it rotates through alternatives.
    private int wallHugDir   = 0;   // 0=up,1=right,2=down,3=left — current wall-hug direction
    private int blockedTicks = 0;   // consecutive ticks where preferred dir was blocked
    private static final int ROTATE_AFTER = 3; // ticks before trying next direction

    private CollisionChecker collisionChecker;

    private static BufferedImage[][] zombieFramesByLevel = new BufferedImage[4][];
    private static BufferedImage[]   humanFramesBase;

    private static BufferedImage[]   zombieFramesBase;

    private BufferedImage[] zombieFrames;
    private BufferedImage[] humanFrames;

    private int levelIndex;

    private boolean healed = false;

    public Zombie(float[] patrol, float levelScale, CollisionChecker cc, int levelIndex) {
        x      = patrol[0]; y      = patrol[1];
        minX   = patrol[2]; maxX   = patrol[3];
        minY   = patrol[4]; maxY   = patrol[5];
        speedX = patrol[6]; speedY = patrol[7];
        mode   = Mode.PATROL;
        this.collisionChecker = cc;
        this.levelIndex = levelIndex;

        // Scale zombie frame and hitbox
        frameW = Math.max(20, (int)(BASE_FRAME_W * levelScale));
        frameH = Math.max(24, (int)(BASE_FRAME_H * levelScale));
        width  = frameW - 10;
        height = frameH - 14;

        // Chase speed scales with level (zombie slower than player)
        chaseSpeed = BASE_CHASE_SPEED * levelScale;

        // Build scaled sprite frames from per-level base sheet
        BufferedImage[] baseFrames = (levelIndex >= 0 && levelIndex < zombieFramesByLevel.length
                && zombieFramesByLevel[levelIndex] != null)
                ? zombieFramesByLevel[levelIndex]
                : zombieFramesBase;
        if (baseFrames != null) {
            zombieFrames = scaleFrames(baseFrames, frameW, frameH);
        }
        if (humanFramesBase != null) {
            humanFrames = scaleFrames(humanFramesBase, frameW, frameH);
        }
    }

    /** Backward-compat constructor (levelIndex defaults to 0) */
    public Zombie(float[] patrol, float levelScale, CollisionChecker cc) {
        this(patrol, levelScale, cc, 0);
    }

    private static BufferedImage[] scaleFrames(BufferedImage[] src, int w, int h) {
        BufferedImage[] out = new BufferedImage[src.length];
        for (int i = 0; i < src.length; i++) {
            out[i] = SpriteSheet.scale(src[i], w, h);
        }
        return out;
    }

    /** Load zombie sprites for a specific level index (0-3). */
    public static void loadZombieSpritesForLevel(BufferedImage sheet, int levelIndex) {
        BufferedImage strip = SpriteSheet.crop(sheet, 0, 768, CELL_W * WALK_FRAMES, CELL_H);
        BufferedImage[] frames = SpriteSheet.sliceRow(strip, WALK_FRAMES, BASE_FRAME_W, BASE_FRAME_H);
        if (levelIndex >= 0 && levelIndex < zombieFramesByLevel.length) {
            zombieFramesByLevel[levelIndex] = frames;
        }
        // Also keep level-0 as the fallback zombieFramesBase
        if (levelIndex == 0) zombieFramesBase = frames;
    }

    public static void loadZombieSprites(BufferedImage sheet) {
        loadZombieSpritesForLevel(sheet, 0);
    }

    public static void loadHumanSprites(BufferedImage sheet) {
        BufferedImage strip = SpriteSheet.crop(sheet, 0, 768, CELL_W * WALK_FRAMES, CELL_H);
        humanFramesBase = SpriteSheet.sliceRow(strip, WALK_FRAMES, BASE_FRAME_W, BASE_FRAME_H);
    }

    public void setMode(Mode m) { this.mode = m; }
    public Mode getMode()       { return mode; }
    public boolean isHealed()   { return healed; }

    /** Called when player touches a zombie after collecting cure. */
    public void heal() {
        if (!healed) {
            healed = true;
            setMode(Mode.HUMAN);
        }
    }

    public void setCollisionChecker(CollisionChecker cc) {
        this.collisionChecker = cc;
    }

    public void update(float playerX, float playerY) {
        switch (mode) {
            case PATROL:
                x += speedX; y += speedY;
                if (x < minX || x > maxX) speedX *= -1;
                if (y < minY || y > maxY) speedY *= -1;
                break;
            case CHASING:
                chasePlayerBFS(playerX, playerY);
                break;
            case HUMAN:
                x += speedX * 0.5f; y += speedY * 0.5f;
                if (x < minX || x > maxX) speedX *= -1;
                if (y < minY || y > maxY) speedY *= -1;
                break;
        }
    }

    /**
     * Chase the player using a wall-hugging "bug" algorithm.
     * Priority: move toward player. If blocked, rotate through the 4 cardinal
     * directions until one is free. This guarantees the zombie is always moving.
     */
    private void chasePlayerBFS(float playerX, float playerY) {
        if (collisionChecker == null) {
            // No collision data — beeline directly
            float dx = playerX - x, dy = playerY - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 1) { x += (dx / dist) * chaseSpeed; y += (dy / dist) * chaseSpeed; }
            return;
        }

        float spd = chaseSpeed;

        // Build 4 candidate directions sorted by how well they aim at the player.
        // 0=up, 1=right, 2=down, 3=left
        float dx = playerX - x;
        float dy = playerY - y;

        // Primary: whichever axis has the larger component points at the player best.
        // We build a preference order: best cardinal first, then the other toward-player
        // cardinal, then the two away-from-player cardinals (as last-resort wall slides).
        int bestH = dx > 0 ? 1 : 3;   // right or left
        int bestV = dy > 0 ? 2 : 0;   // down or up
        int worstH = dx > 0 ? 3 : 1;
        int worstV = dy > 0 ? 0 : 2;

        int[] preference;
        if (Math.abs(dx) >= Math.abs(dy)) {
            preference = new int[]{ bestH, bestV, worstV, worstH };
        } else {
            preference = new int[]{ bestV, bestH, worstH, worstV };
        }

        // Try the preferred direction first; if blocked use wallHugDir rotation.
        boolean moved = false;

        // First attempt: try the ideal direction
        if (tryMove(preference[0], spd)) {
            wallHugDir   = preference[0];
            blockedTicks = 0;
            moved = true;
        }

        if (!moved) {
            // Blocked on ideal — try rotating through remaining preferences
            blockedTicks++;
            if (blockedTicks >= ROTATE_AFTER) {
                // Cycle wallHugDir through the preference list
                wallHugDir = (wallHugDir + 1) % 4;
                blockedTicks = 0;
            }
            // Try wallHugDir, then cascade through all 4 directions
            for (int attempt = 0; attempt < 4 && !moved; attempt++) {
                int dir = (wallHugDir + attempt) % 4;
                if (tryMove(dir, spd)) {
                    wallHugDir = dir;
                    moved = true;
                }
            }
        }

        // If truly trapped (surrounded), do nothing this tick — very rare.
    }

    /** Try moving one step in direction dir. Returns true if movement succeeded. */
    private boolean tryMove(int dir, float spd) {
        boolean can;
        switch (dir) {
            case 0: can = collisionChecker.canMoveUp   (x, y, width, height, spd); break;
            case 1: can = collisionChecker.canMoveRight(x, y, width, height, spd); break;
            case 2: can = collisionChecker.canMoveDown (x, y, width, height, spd); break;
            case 3: can = collisionChecker.canMoveLeft (x, y, width, height, spd); break;
            default: return false;
        }
        if (!can) return false;
        switch (dir) {
            case 0: y -= spd; break;
            case 1: x += spd; break;
            case 2: y += spd; break;
            case 3: x -= spd; break;
        }
        return true;
    }

    public void draw(Graphics2D g, long frameCount) {
        int frame = (int)(frameCount / 10) % WALK_FRAMES;
        BufferedImage[] frames = (mode == Mode.HUMAN && humanFrames != null)
                ? humanFrames : zombieFrames;
        if (frames != null) g.drawImage(frames[frame], (int) x, (int) y, null);
    }
}
