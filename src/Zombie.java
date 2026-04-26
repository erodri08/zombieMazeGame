import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;

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
    private static final float BASE_CHASE_SPEED = 1.1f;
    private float chaseSpeed;

    //  BFS pathfinding for final level - WORK IN PROGRESS
    private static final int BFS_CELL = 40;
    private static final int BFS_INTERVAL = 30;
    private int bfsTimer = 0;
    private List<float[]> waypoints = new ArrayList<>();
    private int waypointIdx = 0;
    private int lastBfsPlayerGX = -1, lastBfsPlayerGY = -1;

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

    // WORK IN PROGRESS: BFS pathfinding toward player, with wall collision checks and periodic path refresh.   
    private void chasePlayerBFS(float playerX, float playerY) {
        if (collisionChecker == null) {
            float dx = playerX - x, dy = playerY - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 1) { x += (dx / dist) * chaseSpeed; y += (dy / dist) * chaseSpeed; }
            return;
        }

        // Convert world positions to BFS grid cells
        int myGX   = (int)(x          / BFS_CELL);
        int myGY   = (int)(y          / BFS_CELL);
        int plrGX  = (int)(playerX    / BFS_CELL);
        int plrGY  = (int)(playerY    / BFS_CELL);

        // Recalculate path periodically 
        bfsTimer--;
        if (bfsTimer <= 0 || plrGX != lastBfsPlayerGX || plrGY != lastBfsPlayerGY) {
            bfsTimer = BFS_INTERVAL;
            lastBfsPlayerGX = plrGX;
            lastBfsPlayerGY = plrGY;
            computeBFSPath(myGX, myGY, plrGX, plrGY, playerX, playerY);
        }

        // Follow the current waypoint
        if (waypoints.isEmpty()) {
            // Fallback: move directly toward player
            moveToward(playerX, playerY);
            return;
        }

        // Advance waypoint if we're close enough
        float[] wp = waypoints.get(waypointIdx);
        float wdx = wp[0] - (x + width / 2f);
        float wdy = wp[1] - (y + height / 2f);
        float wdist = (float) Math.sqrt(wdx * wdx + wdy * wdy);
        if (wdist < chaseSpeed + 2f) {
            if (waypointIdx + 1 < waypoints.size()) {
                waypointIdx++;
                wp = waypoints.get(waypointIdx);
                wdx = wp[0] - (x + width / 2f);
                wdy = wp[1] - (y + height / 2f);
                wdist = (float) Math.sqrt(wdx * wdx + wdy * wdy);
            }
        }

        if (wdist < 1) return;
        float nx = (wdx / wdist) * chaseSpeed;
        float ny = (wdy / wdist) * chaseSpeed;

        // Apply movement with wall sliding
        boolean canX, canY;
        if (nx > 0) canX = collisionChecker.canMoveRight(x, y, width, height, nx);
        else        canX = collisionChecker.canMoveLeft (x, y, width, height, -nx);
        if (ny > 0) canY = collisionChecker.canMoveDown(x, y, width, height,  ny);
        else        canY = collisionChecker.canMoveUp  (x, y, width, height, -ny);

        if (canX) x += nx;
        if (canY) y += ny;

        // If completely stuck (both axes blocked), trigger an immediate BFS refresh
        if (!canX && !canY) {
            bfsTimer = 0;
        }
    }

    private void computeBFSPath(int startGX, int startGY, int goalGX, int goalGY, float playerX, float playerY) {
        // Determine grid bounds
        int maxGX = (int)(1640 / BFS_CELL) + 1;
        int maxGY = (int)(840  / BFS_CELL) + 1;

        // Clamp start and goal
        startGX = Math.max(0, Math.min(startGX, maxGX - 1));
        startGY = Math.max(0, Math.min(startGY, maxGY - 1));
        goalGX  = Math.max(0, Math.min(goalGX,  maxGX - 1));
        goalGY  = Math.max(0, Math.min(goalGY,  maxGY - 1));

        if (startGX == goalGX && startGY == goalGY) {
            waypoints.clear();
            waypointIdx = 0;
            waypoints.add(new float[]{playerX, playerY});
            return;
        }

        // BFS
        int[][] parent = new int[maxGY][maxGX];
        for (int[] row : parent) Arrays.fill(row, -1);
        Queue<int[]> queue = new LinkedList<>();
        int startKey = startGY * maxGX + startGX;
        parent[startGY][startGX] = startKey;  // mark start visited (self-parent)
        queue.add(new int[]{startGX, startGY});

        int[] dxs = {0, 0, 1, -1};
        int[] dys = {1, -1, 0, 0};

        boolean found = false;
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == goalGX && cy == goalGY) { found = true; break; }

            for (int d = 0; d < 4; d++) {
                int nx = cx + dxs[d];
                int ny = cy + dys[d];
                if (nx < 0 || ny < 0 || nx >= maxGX || ny >= maxGY) continue;
                if (parent[ny][nx] != -1) continue;  // already visited

                // Check if movement from (cx,cy) to (nx,ny) in world space is wall-free
                float wx = cx * BFS_CELL + BFS_CELL / 2f - width / 2f;
                float wy = cy * BFS_CELL + BFS_CELL / 2f - height / 2f;
                float ndx = (nx - cx) * BFS_CELL;
                float ndy = (ny - cy) * BFS_CELL;
                boolean passable;
                if (ndx > 0)      passable = collisionChecker.canMoveRight(wx, wy, width, height, ndx);
                else if (ndx < 0) passable = collisionChecker.canMoveLeft (wx, wy, width, height, -ndx);
                else if (ndy > 0) passable = collisionChecker.canMoveDown (wx, wy, width, height, ndy);
                else              passable = collisionChecker.canMoveUp   (wx, wy, width, height, -ndy);

                if (!passable) continue;

                parent[ny][nx] = cy * maxGX + cx;  // encode parent as (cy*maxGX+cx)
                queue.add(new int[]{nx, ny});
            }
        }

        waypoints.clear();
        waypointIdx = 0;

        if (!found) {
            // BFS couldn't reach player — move directly as fallback
            waypoints.add(new float[]{playerX, playerY});
            return;
        }

        // Reconstruct path (in reverse)
        List<float[]> rev = new ArrayList<>();
        int curX = goalGX, curY = goalGY;
        while (!(curX == startGX && curY == startGY)) {
            rev.add(new float[]{curX * BFS_CELL + BFS_CELL / 2f,
                                 curY * BFS_CELL + BFS_CELL / 2f});
            int par = parent[curY][curX];
            int parX = par % maxGX;
            int parY = par / maxGX;
            curX = parX; curY = parY;
        }

        // Reverse so waypoints go from current position toward player
        Collections.reverse(rev);

        // Replace last waypoint with exact player position for smooth arrival
        if (!rev.isEmpty()) {
            rev.set(rev.size() - 1, new float[]{playerX, playerY});
        }

        waypoints = rev;
    }

    /** Simple direct movement toward a target world position (fallback). */
    private void moveToward(float targetX, float targetY) {
        float dx = targetX - x, dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist <= 1) return;
        float nx = (dx / dist) * chaseSpeed;
        float ny = (dy / dist) * chaseSpeed;
        boolean canX, canY;
        if (nx > 0) canX = collisionChecker.canMoveRight(x, y, width, height, nx);
        else        canX = collisionChecker.canMoveLeft (x, y, width, height, -nx);
        if (ny > 0) canY = collisionChecker.canMoveDown(x, y, width, height,  ny);
        else        canY = collisionChecker.canMoveUp  (x, y, width, height, -ny);
        if (canX) x += nx;
        if (canY) y += ny;
    }

    public void draw(Graphics2D g, long frameCount) {
        int frame = (int)(frameCount / 10) % WALK_FRAMES;
        BufferedImage[] frames = (mode == Mode.HUMAN && humanFrames != null)
                ? humanFrames : zombieFrames;
        if (frames != null) g.drawImage(frames[frame], (int) x, (int) y, null);
    }
}
