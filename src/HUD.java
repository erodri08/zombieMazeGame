import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws the in-game HUD: heart lives (top-left) and collected keys (bottom-right).
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class HUD {

    private static final int HEART_SIZE = 30;
    private static final int HEART_GAP  = 35;
    private static final int KEY_W      = 75;
    private static final int KEY_H      = 50;
    private static final int KEY_GAP    = 80;

    private BufferedImage imgHeart;
    private BufferedImage imgEmptyHeart;
    private BufferedImage imgSmallKey;

    public void loadAssets(BufferedImage heartSrc, BufferedImage emptyHeartSrc, BufferedImage keySrc) {
        imgHeart      = SpriteSheet.scale(heartSrc,      HEART_SIZE, HEART_SIZE);
        imgEmptyHeart = SpriteSheet.scale(emptyHeartSrc, HEART_SIZE, HEART_SIZE);
        imgSmallKey   = SpriteSheet.scale(keySrc,        KEY_W,      KEY_H);
    }

    public void draw(Graphics2D g, int lives, int maxLives, int keysCollected, int screenW, int screenH) {
        // Hearts — top left
        for (int i = 0; i < maxLives; i++) {
            BufferedImage img = (i < lives) ? imgHeart : imgEmptyHeart;
            g.drawImage(img, 10 + i * HEART_GAP, 10, null);
        }

        // Collected key icons — bottom right
        int baseY = screenH - KEY_H - 10;
        int baseX = screenW - KEY_W - 10;
        for (int i = 0; i < keysCollected; i++) {
            g.drawImage(imgSmallKey, baseX - i * KEY_GAP, baseY, null);
        }
    }
}
