import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Draws the in-game HUD:
 *   • Hearts (top-left)
 *   • Collected key icons (bottom-right)
 *   • Level indicator (top-right)
 *   • Control legend strip (very bottom)
 *   • Health-restored message (top-centre, timed)
 * @author Ethan Rodrigues
 */
public class HUD {

    private static final int HEART_SIZE = 30;
    private static final int HEART_GAP  = 35;
    private static final int KEY_W      = 55;
    private static final int KEY_H      = 37;
    private static final int KEY_GAP    = 60;

    // Low-life threshold: flicker hearts when at or below this many lives
    public static final int LOW_LIFE_THRESHOLD = 2;

    private static final int RESTORE_MSG_DURATION = 180; 

    private static final Color COL_TEXT     = new Color(0x1B, 0x4D, 0x1B);
    private static final Color COL_BG_STRIP = new Color(0, 0, 0, 140);
    private static final Color COL_KEY_BOX  = new Color(0xCC, 0xE8, 0xCC, 200);
    private static final String FONT = "Courier New";

    private BufferedImage imgHeart;
    private BufferedImage imgEmptyHeart;
    private BufferedImage imgSmallKey;

    private int healthRestoredFrames = 0;

    public void loadAssets(BufferedImage heartSrc, BufferedImage emptyHeartSrc, BufferedImage keySrc) {
        imgHeart      = SpriteSheet.scale(heartSrc,      HEART_SIZE, HEART_SIZE);
        imgEmptyHeart = SpriteSheet.scale(emptyHeartSrc, HEART_SIZE, HEART_SIZE);
        imgSmallKey   = SpriteSheet.scale(keySrc,        KEY_W,      KEY_H);
    }

    public void tickHealthRestored() {
        if (healthRestoredFrames > 0) healthRestoredFrames--;
    }

    public void showHealthRestored() {
        healthRestoredFrames = RESTORE_MSG_DURATION;
    }

    public void draw(Graphics2D g, int lives, int maxLives,
                     int keysCollected, int keysRequired,
                     int levelIndex, boolean isFinalLevel,
                     boolean cureCollected, boolean allZombiesHealed,
                     int zombiesHealed, int zombieTotal,
                     int screenW, int screenH,
                     long frameCount,
                     long gameStartTimeMs) {

        boolean lowHealth = lives <= LOW_LIFE_THRESHOLD;
        boolean heartVisible = !lowHealth || ((frameCount / 10) % 3 != 2);

        // Hearts — top left 
        // Draw up to maxLives heart slots (filled or empty)
        int displayMax = Math.max(maxLives, lives);  // show extra slots if lives > maxLives
        for (int i = 0; i < displayMax; i++) {
            boolean filled = i < lives;
            boolean isExtra = i >= maxLives;          // lives beyond MAX_LIVES
            if (isExtra && filled) {
                g.drawImage(imgHeart, 10 + i * HEART_GAP, 10, null);
                g.setFont(new Font(FONT, Font.BOLD, 10));
                g.setColor(new Color(0xFF, 0xD7, 0x00));
                g.drawString("+", 10 + i * HEART_GAP + HEART_SIZE - 8, 10 + 10);
            } else if (filled && lowHealth) {
                if (heartVisible) {
                    g.drawImage(imgHeart, 10 + i * HEART_GAP, 10, null);
                }
            } else {
                g.drawImage(filled ? imgHeart : imgEmptyHeart, 10 + i * HEART_GAP, 10, null);
            }
        }

        //  live timer — below hearts, top-left
        {
            long elapsed = System.currentTimeMillis() - gameStartTimeMs;
            long secs    = elapsed / 1000;
            long ms      = (elapsed % 1000) / 10;
            String timeStr = String.format("%d:%02d.%02d", secs / 60, secs % 60, ms);
            String hudLine  = timeStr;



            g.setFont(new Font(FONT, Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            int padX = 8, padY = 4;
            int bw = fm.stringWidth(hudLine) + padX * 2;
            int bh = fm.getAscent() + padY * 2;
            int bx = 10, by = HEART_SIZE + 14;

            g.setColor(new Color(0, 0, 0, 130));
            g.fillRoundRect(bx, by, bw, bh, 8, 8);
            g.setColor(new Color(0xCC, 0xE8, 0xCC));
            g.drawString(hudLine, bx + padX, by + fm.getAscent() + padY - 2);
        }

        //  Level indicator — top right 
        String lvlText = isFinalLevel ? "FINAL LEVEL" : "Level " + (levelIndex + 1);
        g.setFont(new Font(FONT, Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int lx = screenW - fm.stringWidth(lvlText) - 15;
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(lx - 6, 6, fm.stringWidth(lvlText) + 12, 30, 8, 8);
        g.setColor(new Color(0xCC, 0xE8, 0xCC));
        g.drawString(lvlText, lx, 26);

        // Keys or cure status — bottom right
        if (isFinalLevel) {
            String cureText;
            Color cureCol;
            if (allZombiesHealed) {
                cureText = "ALL HEALED, Find the exit!";
                cureCol  = new Color(0x22, 0xBB, 0x22);
            } else if (cureCollected) {
                cureText = "Healed: " + zombiesHealed + "/" + zombieTotal + " — touch each zombie!";
                cureCol  = new Color(0xDD, 0x88, 0xFF);
            } else {
                cureText = "Find the cure!";
                cureCol  = new Color(0xFF, 0xDD, 0x44);
            }
            g.setFont(new Font(FONT, Font.BOLD, 18));
            fm = g.getFontMetrics();
            int cx = screenW - fm.stringWidth(cureText) - 15;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(cx - 6, screenH - 60, fm.stringWidth(cureText) + 12, 28, 8, 8);
            g.setColor(cureCol);
            g.drawString(cureText, cx, screenH - 42);
        } else {
            int baseY = screenH - KEY_H - 32;
            int baseX = screenW - KEY_W - 10;
            for (int i = 0; i < keysCollected; i++) {
                g.drawImage(imgSmallKey, baseX - i * KEY_GAP, baseY, null);
            }
        }

        //  Control legend — bottom strip 
        int stripH = 20;
        g.setColor(COL_BG_STRIP);
        g.fillRect(0, screenH - stripH, screenW, stripH);

        g.setFont(new Font(FONT, Font.PLAIN, 13));
        g.setColor(new Color(200, 230, 200));
        String legend  = "  WASD / Arrow keys: Move, Q / ESC: Quit to menu";
        g.drawString(legend,  10, screenH - stripH + 12);

        //  Health-restored banner — top centre 
        if (healthRestoredFrames > 0) {
            String msg = "Health Restored!";
            Font bannerFont = new Font(FONT, Font.BOLD, 30);
            g.setFont(bannerFont);
            fm = g.getFontMetrics();
            int msgW = fm.stringWidth(msg);
            int msgH = fm.getAscent();
            int bx = (screenW - msgW) / 2 - 20;
            int by = 52;  
            int bw = msgW + 40;
            int bh = msgH + 22;

            float alpha = healthRestoredFrames < 60 ? healthRestoredFrames / 60f : 1f;
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(new Color(0x0A, 0x2A, 0x0A, 210));
            g.fillRoundRect(bx, by, bw, bh, 16, 16);
            g.setColor(new Color(0xCC, 0xE8, 0xCC));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bx, by, bw, bh, 16, 16);

            g.setColor(new Color(0x22, 0xBB, 0x22));
            g.drawString(msg, bx + 20, by + msgH + 6);

            g.setComposite(old);
        }
    }
}
