/**
 * Registry of all levels in Zombie Maze.
 * To add a new level, create a new LevelData entry in the LEVELS array.
 *
 * Zombie patrol format: { startX, startY, minX, maxX, minY, maxY, speedX, speedY }
 *   speedX=0  → vertical patrol,  speedY=0 → horizontal patrol
 * Wall format: { x, y, width, height }
 *
 * @author Ethan Rodrigues
 */
public class Levels {

    public static final LevelData[] LEVELS = {
        level1()
        // To add level 2:  , level2()
    };

    private static LevelData level1() {

        int[][] keys = {
            {120, 720},
            {420, 720},
            {600, 120},
            {1200, 120}
        };

        // { startX, startY, minX, maxX, minY, maxY, speedX, speedY }
        float[][] zombies = {
            {150,  420,  120,  220,   30,  720,  0,  3},  // vertical patrol
            {520,  320,  240,  860,  320,  450,  3,  0},
            {620,  720,  240, 1040,  700,  840,  3,  0},
            {920,   20,  240, 1556,   10,  140,  3,  0},
            {1220, 420,  940, 1556,  400,  600,  3,  0},
        };

        int[][] walls = {
            // Outer border
            {20,    20, 1600,  10},
            {20,    20,   10, 300},
            {20,   420,   10, 400},
            {20,   810, 1600,  10},
            {1610,  20,   10, 600},
            {1610, 720,   10, 100},
            // Interior
            {120,  120,  10, 600},
            {220,   20,  10, 400},
            {220,  520,  10, 300},
            {220,  420, 600,  10},
            {220,  520, 700,  10},
            {320,  120, 400,  10},
            {420,  220, 300,  10},
            {320,  320, 500,  10},
            {320,  620, 700,  10},
            {220,  720, 700,  10},
            {320,  120,  10, 100},
            {820,  120,  10, 210},
            {920,  120,  10, 410},
            {1020, 120,  10, 200},
            {1020, 520,  10, 200},
            {1120, 620,  10, 200},
            {1020, 120, 500,  10},
            {720,  120,  10, 110},
            {1120, 220, 400,  10},
            {1020, 320, 400,  10},
            {1520, 220,  10, 200},
            {1020, 420, 510,  10},
            {1020, 520, 600,  10},
            {1120, 620, 300,  10},
            {1220, 720, 300,  10},
            {1520, 520,  10, 210},
        };

        int[] gate      = {1610, 620, 10, 100};
        int[] entryGate = {20,   320, 10, 100};

        return new LevelData(
            -22f, 316f,
            keys, zombies, walls,
            gate, 4,
            entryGate, 20f,
            1560f,
            2
        );
    }

    /*
     * I need to update this for future levels:
     *
     * private static LevelData level2() {
     *     int[][] keys = { ... };
     *     float[][] zombies = { ... };
     *     int[][] walls = { ... };
     *     return new LevelData(startX, startY, keys, zombies, walls,
     *                          gate, keysRequired, entryGate, closeX,
     *                          winTriggerX, rockCount);
     * }
     */
}
