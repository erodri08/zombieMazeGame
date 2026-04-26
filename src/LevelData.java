/**
 * Data for a single level: walls, keys, zombies, and display config.
 * @author Ethan Rodrigues
 */
public class LevelData {

    public final int levelIndex;           // 0-based; used for "Level N" display
    public final String backgroundFile;    // e.g. "Background1.png"
    public final String flashlightFile;    // e.g. "flashlightCircleSmaller.png"
    public final float flashlightScale;    // 1.0 = native size, >1.0 = larger circle
    public final float playerScale;        // 1.0 = normal, 0.75 = smaller

    public final float playerStartX;
    public final float playerStartY;

    /** { {x1,y1}, {x2,y2}, … } – maze-floor key locations */
    public final int[][] keyPositions;

    /** { startX, startY, minX, maxX, minY, maxY, speedX, speedY } */
    public final float[][] zombiePatrols;

    /** Walls: { x, y, w, h } */
    public final int[][] walls;

    /** Exit gate { x, y, w, h } – drawn; hidden once all keys collected */
    public final int[] gate;
    public final int   keysRequired;

    /** Entry gate { x, y, w, h } – closes once player passes entryGateCloseX */
    public final int[] entryGate;
    public final float entryGateCloseX;

    /** Player x >= this triggers level-complete */
    public final float winTriggerX;

    public final int rockCount;

    /** True for the chase/final level – drives special logic in GamePanel */
    public final boolean isFinalLevel;

    /** Heart pickup locations { {x,y}, … } – scattered in maze */
    public final int[][] heartPositions;

    /** Overall scale factor for this level (zombies, items, etc.) */
    public final float levelScale;

    /** Wall color for this level */
    public final java.awt.Color wallColor;

    /** Gate/cover color for this level (matches wall color) */
    public final java.awt.Color gateColor;

    public LevelData(int levelIndex,
                     String backgroundFile, String flashlightFile, float flashlightScale,
                     float playerScale,
                     float playerStartX, float playerStartY,
                     int[][] keyPositions, float[][] zombiePatrols, int[][] walls,
                     int[] gate, int keysRequired,
                     int[] entryGate, float entryGateCloseX,
                     float winTriggerX, int rockCount,
                     boolean isFinalLevel,
                     int[][] heartPositions,
                     float levelScale,
                     java.awt.Color wallColor,
                     java.awt.Color gateColor) {
        this.levelIndex        = levelIndex;
        this.backgroundFile    = backgroundFile;
        this.flashlightFile    = flashlightFile;
        this.flashlightScale   = flashlightScale;
        this.playerScale       = playerScale;
        this.playerStartX      = playerStartX;
        this.playerStartY      = playerStartY;
        this.keyPositions      = keyPositions;
        this.zombiePatrols     = zombiePatrols;
        this.walls             = walls;
        this.gate              = gate;
        this.keysRequired      = keysRequired;
        this.entryGate         = entryGate;
        this.entryGateCloseX   = entryGateCloseX;
        this.winTriggerX       = winTriggerX;
        this.rockCount         = rockCount;
        this.isFinalLevel      = isFinalLevel;
        this.heartPositions    = heartPositions;
        this.levelScale        = levelScale;
        this.wallColor         = wallColor;
        this.gateColor         = gateColor;
    }
}
