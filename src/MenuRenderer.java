import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Draws all non-gameplay screens using pure Java2D text and shapes.
 * Color scheme: dark green (#1B4D1B) on light blue (#ADD8E6).
 * Font: Courier New (monospaced / retro feel), built into every JDK.
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class MenuRenderer {

    // Palette 
    static final Color COL_BG       = new Color(0xAD, 0xD8, 0xE6);  // light blue
    static final Color COL_TEXT     = new Color(0x1B, 0x4D, 0x1B);  // dark green
    static final Color COL_BTN_FILL = new Color(0xCC, 0xE8, 0xCC);  // pale green
    static final Color COL_BTN_BORD = new Color(0x1B, 0x4D, 0x1B);  // dark green border
    static final Color COL_SHADOW   = new Color(0x0A, 0x2A, 0x0A);  // drop shadow
    static final Color COL_WIN      = new Color(0x22, 0x8B, 0x22);  // forest green
    static final Color COL_LOSE     = new Color(0x8B, 0x00, 0x00);  // dark red
    static final Color COL_SUBTLE   = new Color(0x55, 0x55, 0x55);

    // Fonts 
    private static final String RETRO_FONT = "Courier New";

    //  Button rectangles { x, y, w, h }
    public static final int[] BTN_BEGIN        = {560, 490, 280, 60};
    public static final int[] BTN_INSTRUCTIONS = {480, 575, 440, 60};
    public static final int[] BTN_BACK         = {660, 730, 280, 55};
    public static final int[] BTN_PLAY_AGAIN   = {600, 550, 380, 65};
    public static final int[] BTN_WIN_AGAIN    = {600, 590, 380, 65};

    // Sprite previews for instruction screen
    private BufferedImage imgZombiePreview;
    private BufferedImage imgRobotPreview;
    private BufferedImage imgKeyPreview;
    private BufferedImage imgHeartPreview;

    /** Prepare sprite previews used on the instruction screen. */
    public void setup(BufferedImage zombieSheet, BufferedImage robotSheet,
                      BufferedImage keyImg, BufferedImage heartImg) {
        imgZombiePreview = SpriteSheet.cropAndScale(zombieSheet, 0, 0, 192, 256, 48, 64);
        imgRobotPreview  = SpriteSheet.cropAndScale(robotSheet,  0, 0, 192, 256, 48, 64);
        imgKeyPreview    = SpriteSheet.scale(keyImg,   60, 40);
        imgHeartPreview  = SpriteSheet.scale(heartImg, 30, 30);
    }

    // =========================================================================
    //  Menu
    // =========================================================================

    public void drawMenu(Graphics2D g, int w, int h, BufferedImage[] zombieFrames,
                         BufferedImage robotFrame, long frameCount) {
        fillBg(g, w, h);

        // Title with drop shadow
        Font titleFont = new Font(RETRO_FONT, Font.BOLD, 68);
        g.setFont(titleFont);
        drawCenteredString(g, "ZOMBIE MAZE", w, 130, COL_SHADOW, 4);
        drawCenteredString(g, "ZOMBIE MAZE", w, 130, COL_TEXT, 0);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 20));
        drawCenteredString(g, "by Ethan Rodrigues", w, 190, COL_TEXT, 0);

        drawSeparator(g, w, 215);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 18));
        drawCenteredString(g, "Escape the maze alive — collect keys, avoid zombies!", w, 270, COL_TEXT, 0);
        drawCenteredString(g, "Use  W A S D  to move", w, 305, COL_TEXT, 0);

        drawSeparator(g, w, 340);

        // Animated sprites on menu
        if (zombieFrames != null) {
            int zFrame = (int)(frameCount / 10) % zombieFrames.length;
            g.drawImage(zombieFrames[zFrame], 700, 370, null);
        }
        if (robotFrame != null) {
            g.drawImage(robotFrame, 820, 370, null);
        }

        drawButton(g, BTN_BEGIN,        ">> BEGIN <<");
        drawButton(g, BTN_INSTRUCTIONS, "View Instructions");
    }

    // =========================================================================
    //  Instructions
    // =========================================================================

    public void drawInstructions(Graphics2D g, int w, int h) {
        fillBg(g, w, h);

        g.setFont(new Font(RETRO_FONT, Font.BOLD, 48));
        drawCenteredString(g, "HOW TO PLAY", w, 80, COL_SHADOW, 3);
        drawCenteredString(g, "HOW TO PLAY", w, 80, COL_TEXT, 0);

        drawSeparator(g, w, 110);

        float col1 = 140, col2 = 220;
        float startY = 165, rowH = 110;
        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 17));

        // Row 1 – Controls
        drawIconBox(g, (int)col1, (int)startY, 60, 48, "WASD");
        drawWrappedText(g, "Movement: W=up, A=left, S=down, D=right", (int)col2, (int)(startY + 10), 17);
        drawWrappedText(g, "to navigate the dark maze.", (int)col2, (int)(startY + 34), 17);

        // Row 2 – Zombie
        if (imgZombiePreview != null) g.drawImage(imgZombiePreview, (int)col1, (int)(startY + rowH), null);
        drawWrappedText(g, "Zombies: 5 zombies patrol the maze.", (int)col2, (int)(startY + rowH + 10), 17);
        drawWrappedText(g, "Contact costs you a life!", (int)col2, (int)(startY + rowH + 34), 17);

        // Row 3 – Robot
        if (imgRobotPreview != null) g.drawImage(imgRobotPreview, (int)col1, (int)(startY + rowH * 2), null);
        drawWrappedText(g, "You are the Robot. Start at the left entrance", (int)col2, (int)(startY + rowH * 2 + 10), 17);
        drawWrappedText(g, "and reach the exit on the right.", (int)col2, (int)(startY + rowH * 2 + 34), 17);

        // Row 4 – Keys
        if (imgKeyPreview != null) g.drawImage(imgKeyPreview, (int)col1, (int)(startY + rowH * 3 + 14), null);
        drawWrappedText(g, "Keys: Collect all 4 keys scattered in the maze.", (int)col2, (int)(startY + rowH * 3 + 10), 17);
        drawWrappedText(g, "All 4 keys unlock the exit gate.", (int)col2, (int)(startY + rowH * 3 + 34), 17);

        // Row 5 – Hearts
        float hY = startY + rowH * 4 + 14;
        if (imgHeartPreview != null) {
            for (int i = 0; i < 1; i++) g.drawImage(imgHeartPreview, (int)col1 + i * 34, (int)hY, null);
        }
        drawWrappedText(g, "Lives: You start with 5 hearts.", (int)col2, (int)(hY + 5), 17);
        drawWrappedText(g, "Falling rocks also cost a life!", (int)col2, (int)(hY + 29), 17);

        drawSeparator(g, w, 735);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 14));
        g.setColor(COL_SUBTLE);

        drawButton(g, BTN_BACK, "<< Back to Menu");
    }

    // =========================================================================
    //  Win screen
    // =========================================================================

    public void drawWinScreen(Graphics2D g, int w, int h) {
        fillBg(g, w, h);

        g.setFont(new Font(RETRO_FONT, Font.BOLD, 82));
        drawCenteredString(g, "YOU ESCAPED!", w, 210, COL_SHADOW, 4);
        drawCenteredString(g, "YOU ESCAPED!", w, 210, COL_WIN, 0);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 26));
        drawCenteredString(g, "Congratulations, you made it through the maze!", w, 300, COL_TEXT, 0);

        drawSeparator(g, w, 335);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 20));
        drawCenteredString(g, "You collected all 4 keys and defeated the maze.", w, 390, COL_TEXT, 0);
        drawCenteredString(g, "Zombie Maze by Ethan Rodrigues", w, 430, COL_TEXT, 0);

        drawButton(g, BTN_WIN_AGAIN, ">> Play Again <<");
    }

    // =========================================================================
    //  Game Over screen
    // =========================================================================

    public void drawGameOver(Graphics2D g, int w, int h) {
        fillBg(g, w, h);

        g.setFont(new Font(RETRO_FONT, Font.BOLD, 90));
        drawCenteredString(g, "GAME OVER", w, 225, COL_SHADOW, 4);
        drawCenteredString(g, "GAME OVER", w, 225, COL_LOSE, 0);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 26));
        drawCenteredString(g, "The zombies got you...", w, 320, COL_TEXT, 0);

        drawSeparator(g, w, 360);

        g.setFont(new Font(RETRO_FONT, Font.PLAIN, 20));
        drawCenteredString(g, "Remember: collect all 4 keys to open the exit gate.", w, 420, COL_TEXT, 0);
        drawCenteredString(g, "Watch out for zombies AND falling rocks!", w, 455, COL_TEXT, 0);

        drawButton(g, BTN_PLAY_AGAIN, ">> Try Again <<");
    }

    // =========================================================================
    //  Shared helpers
    // =========================================================================

    private void fillBg(Graphics2D g, int w, int h) {
        g.setColor(COL_BG);
        g.fillRect(0, 0, w, h);
    }

    private void drawSeparator(Graphics2D g, int w, int y) {
        g.setColor(COL_BTN_BORD);
        g.fillRect((int)(w * 0.1), y, (int)(w * 0.8), 3);
    }

    public void drawButton(Graphics2D g, int[] btn, String label) {
        // Fill
        g.setColor(COL_BTN_FILL);
        g.fillRoundRect(btn[0], btn[1], btn[2], btn[3], 12, 12);
        // Border
        g.setColor(COL_BTN_BORD);
        Graphics2D g2 = (Graphics2D) g;
        Stroke old = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(btn[0], btn[1], btn[2], btn[3], 12, 12);
        g2.setStroke(old);
        // Label
        g.setFont(new Font(RETRO_FONT, Font.BOLD, 22));
        g.setColor(COL_TEXT);
        FontMetrics fm = g.getFontMetrics();
        int tx = btn[0] + (btn[2] - fm.stringWidth(label)) / 2;
        int ty = btn[1] + (btn[3] + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, tx, ty);
    }

    /** Returns true when the mouse point (mx, my) is inside the button rect. */
    public boolean isHovered(int[] btn, int mx, int my) {
        return mx >= btn[0] && mx <= btn[0] + btn[2]
            && my >= btn[1] && my <= btn[1] + btn[3];
    }

    /** Draw centered string at (cx, y) with an optional shadow offset. */
    private void drawCenteredString(Graphics2D g, String text, int canvasW, int y,
                                    Color color, int shadowOffset) {
        FontMetrics fm = g.getFontMetrics();
        int x = (canvasW - fm.stringWidth(text)) / 2;
        if (shadowOffset != 0) {
            g.setColor(color);
            g.drawString(text, x + shadowOffset, y + shadowOffset);
        } else {
            g.setColor(color);
            g.drawString(text, x, y);
        }
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int size) {
        g.setFont(new Font(RETRO_FONT, Font.PLAIN, size));
        g.setColor(COL_TEXT);
        g.drawString(text, x, y);
    }

    private void drawIconBox(Graphics2D g, int x, int y, int w, int h, String label) {
        g.setColor(COL_BTN_FILL);
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(COL_BTN_BORD);
        g.drawRoundRect(x, y, w, h, 6, 6);
        g.setFont(new Font(RETRO_FONT, Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(COL_TEXT);
        g.drawString(label, x + (w - fm.stringWidth(label)) / 2,
                            y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }
}
