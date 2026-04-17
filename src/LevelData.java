/**
 * Holds all data for a single level: maze walls, key positions, zombie patrol areas.
 * To add a new level, create a new LevelData instance with different parameters.
 * @author Ethan Rodrigues
 */
public class LevelData {

    public final float playerStartX;
    public final float playerStartY;

    /** Key positions: { {x1,y1}, {x2,y2}, … } */
    public final int[][] keyPositions;

    /** Zombie patrol zones: { startX, startY, minX, maxX, minY, maxY, speedX, speedY } */
    public final float[][] zombiePatrols;

    /** Maze wall rectangles: { x, y, w, h } */
    public final int[][] walls;

    /** Exit gate rectangle { x, y, w, h } — hidden once all keys collected */
    public final int[] gate;
    public final int   keysRequired;

    /** Entry gate { x, y, w, h } — closes once player passes entryGateCloseX */
    public final int[] entryGate;
    public final float entryGateCloseX;

    /** Player X position that triggers the win condition */
    public final float winTriggerX;

    public final int rockCount;

    public LevelData(
            float playerStartX, float playerStartY,
            int[][] keyPositions,
            float[][] zombiePatrols,
            int[][] walls,
            int[] gate, int keysRequired,
            int[] entryGate, float entryGateCloseX,
            float winTriggerX,
            int rockCount) {
        this.playerStartX    = playerStartX;
        this.playerStartY    = playerStartY;
        this.keyPositions    = keyPositions;
        this.zombiePatrols   = zombiePatrols;
        this.walls           = walls;
        this.gate            = gate;
        this.keysRequired    = keysRequired;
        this.entryGate       = entryGate;
        this.entryGateCloseX = entryGateCloseX;
        this.winTriggerX     = winTriggerX;
        this.rockCount       = rockCount;
    }
}
