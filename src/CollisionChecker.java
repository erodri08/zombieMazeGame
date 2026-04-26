/**
 * Collision checker that tests the player hitbox against the current level's
 * wall rectangles.
 *
 * All canMove* methods and pushOut() operate on the HITBOX rectangle
 * (x + hitOffX, y + hitOffY, hitW, hitH) — Player.hitX() / hitY() must be
 * passed in, not the raw sprite origin.  Player.java handles the conversion.
 *
 * Sliding: the perpendicular axis is inset by SLIDE_FRAC so a tiny overlap
 * on one axis doesn't freeze movement on the other.
 *
 * @author Ethan Rodrigues
 */
public class CollisionChecker {

    private static final int SCREEN_LEFT   = 30;
    private static final int SCREEN_TOP    = 30;
    private static final int SCREEN_BOTTOM = 810;
    private static final int SCREEN_RIGHT  = 1630;

    private static final float SLIDE_FRAC = 0.18f;

    private int[][] walls       = new int[0][];
    private int[]   gate        = null;
    private int[]   entryGate   = null;
    private boolean gateOpen         = false;
    private boolean entryGateClosed  = false;

    public void setWalls(int[][] walls, int[] gate, int[] entryGate) {
        this.walls           = walls;
        this.gate            = gate;
        this.entryGate       = entryGate;
        this.gateOpen        = false;
        this.entryGateClosed = false;
    }

    public void setGateOpen(boolean open)          { this.gateOpen = open; }
    public void setEntryGateClosed(boolean closed) { this.entryGateClosed = closed; }

    //  Movement API  (hx, hy = hitbox origin = player.hitX(), player.hitY())

    public boolean canMoveUp(float hx, float hy, int hw, int hh, float step) {
        float ny = hy - step;
        if (ny <= SCREEN_TOP) return false;
        int margin = Math.max(1, (int)(hw * SLIDE_FRAC));
        return !overlapsAnyWall(hx + margin, ny, hw - margin * 2, hh);
    }

    public boolean canMoveDown(float hx, float hy, int hw, int hh, float step) {
        float ny = hy + step;
        if (ny + hh >= SCREEN_BOTTOM) return false;
        int margin = Math.max(1, (int)(hw * SLIDE_FRAC));
        return !overlapsAnyWall(hx + margin, ny, hw - margin * 2, hh);
    }

    public boolean canMoveLeft(float hx, float hy, int hw, int hh, float step) {
        float nx = hx - step;
        // When entry gate is closed (or default), the left screen edge is the hard boundary.
        // When entry gate is open, allow the player to walk through it (past SCREEN_LEFT).
        if (entryGateClosed && nx <= SCREEN_LEFT) return false;
        if (!entryGateClosed && nx + hw <= 0) return false;  
        int margin = Math.max(1, (int)(hh * SLIDE_FRAC));
        return !overlapsAnyWall(nx, hy + margin, hw, hh - margin * 2);
    }

    public boolean canMoveRight(float hx, float hy, int hw, int hh, float step) {
        float nx = hx + step;
        if (nx + hw >= SCREEN_RIGHT) return false;
        int margin = Math.max(1, (int)(hh * SLIDE_FRAC));
        return !overlapsAnyWall(nx, hy + margin, hw, hh - margin * 2);
    }

    //  Push-out: call with hitbox coordinates; returns corrected hitbox (hx, hy).
    //  Caller must subtract hitOffX/hitOffY to get the sprite origin back.
    public float[] pushOut(float hx, float hy, int hw, int hh) {
        float x = hx, y = hy;

        for (int pass = 0; pass < 8; pass++) {
            boolean stuck = false;
            for (int[] w : walls) {
                float[] fix = resolveOverlap(x, y, hw, hh, w[0], w[1], w[2], w[3]);
                if (fix != null) { x = fix[0]; y = fix[1]; stuck = true; }
            }
            if (!gateOpen && gate != null) {
                float[] fix = resolveOverlap(x, y, hw, hh,
                        gate[0], gate[1], gate[2], gate[3]);
                if (fix != null) { x = fix[0]; y = fix[1]; stuck = true; }
            }
            if (entryGateClosed && entryGate != null) {
                float[] fix = resolveOverlap(x, y, hw, hh,
                        entryGate[0], entryGate[1], entryGate[2], entryGate[3]);
                if (fix != null) { x = fix[0]; y = fix[1]; stuck = true; }
            }
            if (!stuck) break;
        }

        x = Math.max(SCREEN_LEFT,  Math.min(x, SCREEN_RIGHT  - hw));
        y = Math.max(SCREEN_TOP,   Math.min(y, SCREEN_BOTTOM - hh));
        return new float[]{ x, y };
    }

    private float[] resolveOverlap(float px, float py, int pw, int ph,
                                    float wx, float wy, float ww, float wh) {
        float ox1 = (px + pw) - wx;
        float ox2 = (wx + ww) - px;
        float oy1 = (py + ph) - wy;
        float oy2 = (wy + wh) - py;
        if (ox1 <= 0 || ox2 <= 0 || oy1 <= 0 || oy2 <= 0) return null;

        float minX = Math.min(ox1, ox2);
        float minY = Math.min(oy1, oy2);
        if (minX < minY) {
            return ox1 < ox2 ? new float[]{ wx - pw, py } : new float[]{ wx + ww, py };
        } else {
            return oy1 < oy2 ? new float[]{ px, wy - ph } : new float[]{ px, wy + wh };
        }
    }

    private boolean overlapsAnyWall(float px, float py, float pw, float ph) {
        for (int[] w : walls) {
            if (aabb(px, py, pw, ph, w[0], w[1], w[2], w[3])) return true;
        }
        if (entryGateClosed && entryGate != null) {
            if (aabb(px, py, pw, ph,
                     entryGate[0], entryGate[1], entryGate[2], entryGate[3])) return true;
        }
        if (!gateOpen && gate != null) {
            if (aabb(px, py, pw, ph,
                     gate[0], gate[1], gate[2], gate[3])) return true;
        }
        return false;
    }

    public static boolean aabb(float ax, float ay, float aw, float ah,
                                float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
