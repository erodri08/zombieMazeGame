import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Draws all non-gameplay screens.
 * Color scheme: dark green on light blue. Font: Courier New.
 * @author Ethan Rodrigues
 */
public class MenuRenderer {

    static final Color COL_BG           = new Color(0xAD, 0xD8, 0xE6);
    static final Color COL_TEXT         = new Color(0x1B, 0x4D, 0x1B);
    static final Color COL_BTN_FILL     = new Color(0xCC, 0xE8, 0xCC);
    static final Color COL_BTN_HOVER    = new Color(0x88, 0xCC, 0x88);
    static final Color COL_BTN_BORD     = new Color(0x1B, 0x4D, 0x1B);
    static final Color COL_BTN_BORD_HOV = new Color(0x0A, 0x60, 0x0A);
    static final Color COL_TEXT_HOVER   = new Color(0x0A, 0x2A, 0x0A);
    static final Color COL_SHADOW       = new Color(0x0A, 0x2A, 0x0A);
    static final Color COL_WIN          = new Color(0x22, 0x8B, 0x22);
    static final Color COL_LOSE         = new Color(0x8B, 0x00, 0x00);
    static final Color COL_SUBTLE       = new Color(0x55, 0x55, 0x55);
    static final Color COL_PURPLE       = new Color(0x8B, 0x00, 0x8B);
    static final Color COL_GOLD         = new Color(0xCC, 0x99, 0x00);

    private static final String FONT = "Courier New";

    // Buttons
    public static final int[] BTN_BEGIN        = centred(320, 58, 374);
    public static final int[] BTN_LEADERBOARD  = centred(320, 52, 446);
    public static final int[] BTN_BACK         = centred(280, 52, 730);
    public static final int[] BTN_PLAY_AGAIN   = centred(380, 62, 550);
    public static final int[] BTN_WIN_AGAIN    = centred(320, 52, 700);

    private static int[] centred(int w, int h, int y) {
        return new int[]{ (1640 - w) / 2, y, w, h };
    }

    private int hoverX = -1, hoverY = -1;

    public void setHover(int mx, int my) { hoverX = mx; hoverY = my; }

    // Sprite previews
    private BufferedImage imgZombiePreview, imgRobotPreview,
                          imgKeyPreview,    imgHeartPreview,
                          imgHumanPreview,  imgCurePreview;

    // Leaderboard reference
    private Leaderboard leaderboard;

    // Win screen name entry state
    private StringBuilder winNameInput = new StringBuilder();
    private boolean       nameSubmitted = false;
    private int           submittedRank = -1;

    public void setup(BufferedImage zombieSheet, BufferedImage robotSheet,
                      BufferedImage keyImg,      BufferedImage heartImg,
                      BufferedImage humanSheet,  BufferedImage cureImg,
                      Leaderboard lb) {
        imgZombiePreview = SpriteSheet.cropAndScale(zombieSheet, 0, 0, 192, 256, 48, 64);
        imgRobotPreview  = SpriteSheet.cropAndScale(robotSheet,  0, 0, 192, 256, 48, 64);
        imgKeyPreview    = SpriteSheet.scale(keyImg,   60, 40);
        imgHeartPreview  = SpriteSheet.scale(heartImg, 30, 30);
        imgHumanPreview  = SpriteSheet.cropAndScale(humanSheet, 0, 0, 192, 256, 48, 64);
        if (cureImg != null) imgCurePreview = SpriteSheet.scale(cureImg, 48, 48);
        this.leaderboard = lb;
    }

    /** Call before transitioning to WIN so the name field resets. */
    public void resetWinInput() {
        winNameInput.setLength(0);
        nameSubmitted = false;
        submittedRank = -1;
    }

    /** Forward keyboard events on the win screen for name entry. */
    public void handleWinKeyPress(KeyEvent e, Leaderboard lb, long timeMs) {
        if (nameSubmitted) return;
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ENTER) {
            if (winNameInput.length() > 0) {
                submittedRank = lb.addEntry(winNameInput.toString(), timeMs);
                nameSubmitted = true;
            }
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            if (winNameInput.length() > 0)
                winNameInput.deleteCharAt(winNameInput.length() - 1);
        } else {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-') {
                if (winNameInput.length() < 18) winNameInput.append(c);
            }
        }
    }

    // =========================================================================
    //  MAIN MENU
    // =========================================================================
    public void drawMenu(Graphics2D g, int w, int h,
                         BufferedImage[] zombieFrames, BufferedImage robotFrame,
                         long frameCount) {
        fillBg(g, w, h);

        // ── Title block ───────────────────────────────────────────────────────
        g.setFont(new Font(FONT, Font.BOLD, 90));
        centreStr(g, "ZOMBIE MAZE", w, 105, COL_SHADOW, 5);
        centreStr(g, "ZOMBIE MAZE", w, 105, COL_TEXT,   0);

        g.setFont(new Font(FONT, Font.ITALIC, 18));
        centreStr(g, "by Ethan Rodrigues", w, 133, COL_SUBTLE, 0);

        sep(g, w, 152);

        // ── Animated sprites ──────────────────────────────────────────────────
        if (zombieFrames != null) {
            int zf = (int)(frameCount / 10) % zombieFrames.length;
            g.drawImage(zombieFrames[zf], w / 2 - 90, 162, null);
        }
        if (robotFrame != null) g.drawImage(robotFrame, w / 2 + 30, 162, null);

        sep(g, w, 248);

        // ── Story text ────────────────────────────────────────────────────────
        String[] story = {
            "A lab test gone wrong has turned every human on Earth into a zombie.",
            "Only one hope remains: a lone robot (you) must venture deep into a collapsing cave",
            "to find the cure hidden at the heart of a dark maze filled with zombies.",
        };
        g.setFont(new Font(FONT, Font.ITALIC, 16));
        int sy = 274;
        for (String line : story) {
            centreStr(g, line, w, sy, COL_TEXT, 0);
            sy += 24;
        }

        sep(g, w, 350);

        // ── Buttons ───────────────────────────────────────────────────────────
        drawButton(g, BTN_BEGIN,      ">> BEGIN <<");
        drawButton(g, BTN_LEADERBOARD, "Leaderboard");

        sep(g, w, 510);

        // ── How to play ───────────────────────────────────────────────────────
        g.setFont(new Font(FONT, Font.BOLD, 17));
        centreStr(g, "HOW TO PLAY", w, 544, COL_TEXT, 0);

        g.setFont(new Font(FONT, Font.PLAIN, 15));
        String[] tips = {
            "Move with  WASD  or  Arrow Keys      \u2022      ESC / Q  \u2014  Quit to menu",
            "Collect ALL keys to unlock each exit gate and advance deeper into the cave.",
            "Final level: find the cure vial, then return and touch each zombie to heal them.",
            "You start with 10 lives  \u2014  avoid zombies, falling rocks, and don\u2019t get cornered!",
        };
        int hy = 572;
        for (String tip : tips) {
            centreStr(g, tip, w, hy, COL_TEXT, 0);
            hy += 24;
        }
    }

    // =========================================================================
    //  LEADERBOARD
    // =========================================================================
    public void drawLeaderboard(Graphics2D g, int w, int h) {
        fillBg(g, w, h);

        g.setFont(new Font(FONT, Font.BOLD, 52));
        centreStr(g, "LEADERBOARD", w, 90, COL_SHADOW, 4);
        centreStr(g, "LEADERBOARD", w, 90, COL_GOLD,   0);

        sep(g, w, 112);

        g.setFont(new Font(FONT, Font.BOLD, 18));
        centreStr(g, "Fastest times to escape the maze:", w, 148, COL_TEXT, 0);

        List<Leaderboard.Entry> entries = (leaderboard != null) ? leaderboard.getEntries() : java.util.Collections.emptyList();

        int boxX = (w - 560) / 2, boxY = 168, rowH = 44;
        int boxW = 560, boxH = Math.max(rowH, entries.size() * rowH) + 24;

        g.setColor(new Color(0xCC, 0xE8, 0xCC, 200));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g.setColor(COL_BTN_BORD);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        if (entries.isEmpty()) {
            g.setFont(new Font(FONT, Font.ITALIC, 18));
            g.setColor(COL_SUBTLE);
            int ty = boxY + boxH / 2 + 8;
            centreStr(g, "No entries yet — be the first to escape!", w, ty, COL_SUBTLE, 0);
        } else {
            Color[] rankColors = {
                new Color(0xCC, 0x99, 0x00),  // gold
                new Color(0x88, 0x88, 0x88),  // silver
                new Color(0x88, 0x44, 0x00),  // bronze
            };
            g.setFont(new Font(FONT, Font.BOLD, 18));
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry en = entries.get(i);
                int ry = boxY + 16 + i * rowH;
                Color rankCol = i < rankColors.length ? rankColors[i] : COL_TEXT;
                g.setColor(rankCol);
                String rank = (i + 1) + ".";
                g.drawString(rank, boxX + 20, ry + 26);
                g.setColor(COL_TEXT);
                g.drawString(en.name, boxX + 70, ry + 26);
                String time = en.timeStr;
                FontMetrics fm = g.getFontMetrics();
                g.drawString(time, boxX + boxW - fm.stringWidth(time) - 20, ry + 26);
            }
        }

        g.setFont(new Font(FONT, Font.ITALIC, 14));
        centreStr(g, "ESC / Back to return to menu", w, boxY + boxH + 40, COL_SUBTLE, 0);

        drawButton(g, BTN_BACK, "<< Back to Menu");
    }

    // =========================================================================
    //  WIN SCREEN
    // =========================================================================
    public void drawWinScreen(Graphics2D g, int w, int h, long timeMs, Leaderboard lb) {
        fillBg(g, w, h);

        g.setFont(new Font(FONT, Font.BOLD, 76));
        centreStr(g, "YOU ESCAPED!", w, 95, COL_SHADOW, 4);
        centreStr(g, "YOU ESCAPED!", w, 95, COL_WIN,    0);

        g.setFont(new Font(FONT, Font.BOLD, 20));
        centreStr(g, "Earth is saved. Every zombie has been cured.", w, 132, COL_TEXT, 0);
        g.setFont(new Font(FONT, Font.ITALIC, 17));

        sep(g, w, 196);

        // Time
        String timeStr = Leaderboard.Entry.formatTime(timeMs);
        g.setFont(new Font(FONT, Font.BOLD, 30));
        centreStr(g, "Your time:  " + timeStr, w, 238, COL_GOLD, 0);

        // Leaderboard rank preview
        if (lb != null) {
            int rank = lb.getRank(timeMs);
            if (rank > 0) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "🏅";
                String rankStr = medal + "  You are currently ranked  #" + rank + "  on the leaderboard!";
                g.setFont(new Font(FONT, Font.BOLD, 20));
                centreStr(g, rankStr, w, 272, new Color(0x22, 0x66, 0xCC), 0);
            }
        }

        sep(g, w, 292);

        // Name entry
        if (!nameSubmitted) {
            g.setFont(new Font(FONT, Font.BOLD, 19));
            centreStr(g, "Enter your name to claim your spot on the leaderboard:", w, 334, COL_TEXT, 0);

            // Input box
            String display = winNameInput.toString() + "|";
            g.setFont(new Font(FONT, Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            int bw2 = Math.max(340, fm.stringWidth(display) + 40);
            int bx = (w - bw2) / 2, by = 348;
            g.setColor(Color.WHITE);
            g.fillRoundRect(bx, by, bw2, 40, 8, 8);
            g.setColor(COL_BTN_BORD);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(bx, by, bw2, 40, 8, 8);
            g.setColor(COL_TEXT);
            g.drawString(display, bx + (bw2 - fm.stringWidth(display)) / 2, by + 28);

            g.setFont(new Font(FONT, Font.ITALIC, 14));
            centreStr(g, "Press ENTER to submit", w, 410, COL_SUBTLE, 0);
        } else {
            g.setFont(new Font(FONT, Font.BOLD, 22));
            Color rankColor = submittedRank == 1 ? COL_GOLD
                            : submittedRank <= 3  ? new Color(0x88, 0x44, 0x00)
                            : COL_WIN;
            String congrats = submittedRank == 1 ? "🏆  NEW RECORD! You're #1!"
                            : submittedRank == 2  ? "🥈  Runner-up! Rank #2!"
                            : submittedRank == 3  ? "🥉  Top 3! Rank #3!"
                            : "Saved to leaderboard — Rank #" + submittedRank + "! Well done!";
            centreStr(g, congrats, w, 388, rankColor, 0);
        }

        // Mini leaderboard
        sep(g, w, 430);
        g.setFont(new Font(FONT, Font.BOLD, 16));
        centreStr(g, "— TOP TIMES —", w, 458, COL_GOLD, 0);

        List<Leaderboard.Entry> entries = lb != null ? lb.getEntries() : java.util.Collections.emptyList();
        int showCount = Math.min(5, entries.size());
        int lbX = (w - 460) / 2;
        Color[] rankColors = {
            new Color(0xCC, 0x99, 0x00),  // gold
            new Color(0x88, 0x88, 0x88),  // silver
            new Color(0x88, 0x44, 0x00),  // bronze
        };
        for (int i = 0; i < showCount; i++) {
            Leaderboard.Entry en = entries.get(i);
            g.setFont(new Font(FONT, i < 3 ? Font.BOLD : Font.PLAIN, 16));
            Color col = i < rankColors.length ? rankColors[i] : COL_TEXT;
            // Highlight the newly submitted entry
            if (nameSubmitted && en.name.equals(winNameInput.toString()) && i + 1 == submittedRank) {
                g.setColor(new Color(0x22, 0xBB, 0x22));
            } else {
                g.setColor(col);
            }
            int ry = 478 + i * 28;
            g.drawString((i + 1) + ".  " + en.name, lbX, ry);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(en.timeStr, lbX + 460 - fm.stringWidth(en.timeStr), ry);
        }

        drawButton(g, BTN_WIN_AGAIN, ">> Play Again <<");
    }

    // =========================================================================
    //  GAME OVER
    // =========================================================================
    public void drawGameOver(Graphics2D g, int w, int h) {
        fillBg(g, w, h);
        g.setFont(new Font(FONT, Font.BOLD, 88));
        centreStr(g, "GAME OVER", w, 225, COL_SHADOW, 4);
        centreStr(g, "GAME OVER", w, 225, COL_LOSE,   0);

        g.setFont(new Font(FONT, Font.PLAIN, 26));
        centreStr(g, "The zombies got you...", w, 320, COL_TEXT, 0);
        sep(g, w, 360);
        g.setFont(new Font(FONT, Font.PLAIN, 20));
        centreStr(g, "Collect all keys to open each exit gate.", w, 420, COL_TEXT, 0);
        centreStr(g, "Watch out for zombies and falling rocks!", w, 455, COL_TEXT, 0);

        drawButton(g, BTN_PLAY_AGAIN, ">> Try Again <<");
    }

    //  HELPERS
    private void fillBg(Graphics2D g, int w, int h) {
        g.setColor(COL_BG); g.fillRect(0, 0, w, h);
    }

    private void sep(Graphics2D g, int w, int y) {
        g.setColor(COL_BTN_BORD);
        g.fillRect((int)(w * 0.08), y, (int)(w * 0.84), 3);
    }

    public void drawButton(Graphics2D g, int[] btn, String label) {
        boolean hovered = isHovered(btn, hoverX, hoverY);
        g.setColor(hovered ? COL_BTN_HOVER : COL_BTN_FILL);
        g.fillRoundRect(btn[0], btn[1], btn[2], btn[3], 12, 12);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(hovered ? 4 : 3));
        g.setColor(hovered ? COL_BTN_BORD_HOV : COL_BTN_BORD);
        g.drawRoundRect(btn[0], btn[1], btn[2], btn[3], 12, 12);
        g.setStroke(old);
        int fontSize = hovered ? 23 : 22;
        g.setFont(new Font(FONT, Font.BOLD, fontSize));
        g.setColor(hovered ? COL_TEXT_HOVER : COL_TEXT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label,
                btn[0] + (btn[2] - fm.stringWidth(label)) / 2,
                btn[1] + (btn[3] + fm.getAscent() - fm.getDescent()) / 2);
    }

    public boolean isHovered(int[] btn, int mx, int my) {
        return mx >= btn[0] && mx <= btn[0] + btn[2]
            && my >= btn[1] && my <= btn[1] + btn[3];
    }

    private void centreStr(Graphics2D g, String s, int canvasW, int y, Color c, int shadow) {
        FontMetrics fm = g.getFontMetrics();
        int x = (canvasW - fm.stringWidth(s)) / 2;
        if (shadow != 0) { g.setColor(COL_SHADOW); g.drawString(s, x + shadow, y + shadow); }
        g.setColor(c);
        g.drawString(s, x, y);
    }

    private void icon(Graphics2D g, BufferedImage img, int x, int y) {
        if (img != null) g.drawImage(img, x, y, null);
    }
}
