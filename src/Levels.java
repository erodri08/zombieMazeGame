/**
 * Registry of all levels.  Each level is defined in its own file:
 *   Level1.java, Level2.java, Level3.java, LevelFinal.java
 *
 * @author Ethan Rodrigues
 */
public class Levels {

    public static final LevelData[] LEVELS = {
        Level1.create(),
        Level2.create(),
        Level3.create(),
        LevelFinal.create()
    };
}
