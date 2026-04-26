import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Core game panel — drives the game loop, rendering, and input.
 * @author Ethan Rodrigues
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    public static final int W = 1640;
    public static final int H = 840;

    // Debug mode (REMOVE ONCE GAME IS DONE)
    private static final boolean SHOW_DEBUG_BUTTON = true;
    private boolean debugMode = false;

    private boolean debugFlashlightOn   = false;
    private boolean debugCollisionsOn   = false;
    private boolean debugGatesAutoOpen  = true;

    private static final int[] DBG_BTN = { W - 100, H - 42, 90, 22 };
    private static final int DBG_PANEL_X  = W - 200;
    private static final int DBG_PANEL_Y  = H - 110;
    private static final int DBG_BTN_W    = 185;
    private static final int DBG_BTN_H    = 20;
    private static final int DBG_BTN_GAP  = 26;

    private final int[] DBG_FLASHLIGHT_BTN = { DBG_PANEL_X, DBG_PANEL_Y,                   DBG_BTN_W, DBG_BTN_H };
    private final int[] DBG_COLLISION_BTN  = { DBG_PANEL_X, DBG_PANEL_Y + DBG_BTN_GAP,     DBG_BTN_W, DBG_BTN_H };
    private final int[] DBG_GATES_BTN      = { DBG_PANEL_X, DBG_PANEL_Y + DBG_BTN_GAP * 2, DBG_BTN_W, DBG_BTN_H };

    // Game loop 
    private final Timer timer;
    private long frameCount = 0;

    // Timer 
    private long gameStartTimeMs  = 0;
    private long gameFinishTimeMs = 0;

    // State machine 
    private GameState state = GameState.MENU;
    private int currentLevelIndex = 0;

    // Input
    private final Set<Character> keysDown = new HashSet<>();
    private int mouseX, mouseY;

    // Subsystems
    private final Player          player      = new Player();
    private final CollisionChecker walls       = new CollisionChecker();
    private final HUD             hud         = new HUD();
    private final MenuRenderer    menu        = new MenuRenderer();
    private final Random          rng         = new Random();
    private final Leaderboard     leaderboard = new Leaderboard();

    // Level state 
    private LevelData    level;
    private boolean[]    keyCollected;
    private int          keysCollected;
    private Zombie[]     zombies;
    private Rock[]       rocks;

    // Heart pickups
    private boolean[] heartCollected;
    private static final int HEART_PICKUP_W = 28;
    private static final int HEART_PICKUP_H = 28;
    private BufferedImage imgHeartPickup;

    // Banner state
    private int gateOpenBannerFrames    = 0;
    private int curePickupBannerFrames  = 0;
    private int allHealedBannerFrames   = 0;
    private int entranceOpenBannerFrames = 0;
    private static final int BANNER_DURATION = 240;

    // Final-level / cure state
    private boolean cureCollected    = false;
    private float   cureX, cureY;
    private static final int CURE_W = 48, CURE_H = 48;
    private int     zombiesHealedCount = 0;
    private boolean allZombiesHealed   = false;
    private int  levelZombiesHealedCount = 0;
    private boolean healBackMode = false; 

    // Assets
    private BufferedImage[] imgZombieSheets;
    private BufferedImage   imgRobotSheet;
    private BufferedImage   imgHumanSheet;
    private BufferedImage   imgKey;
    private BufferedImage   imgCure;

    private BufferedImage currentBg;
    private BufferedImage currentFlashlight;

    private BufferedImage[][] menuZombieFrames;
    private BufferedImage[] menuRobotFrames;

    private VolatileImage offscreen;
    private Graphics2D    offG;
    private GraphicsConfiguration currentGC;

    // =========================================================================
    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        timer = new Timer(16, this);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    || (e.getChangeFlags() & java.awt.event.HierarchyEvent.PARENT_CHANGED) != 0) {
                recreateOffscreenIfNeeded();
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentMoved(java.awt.event.ComponentEvent e) {
                recreateOffscreenIfNeeded();
            }
        });
    }

    private void createOffscreen(GraphicsConfiguration gc) {
        if (offG != null) { offG.dispose(); offG = null; }
        offscreen = gc.createCompatibleVolatileImage(W, H, Transparency.OPAQUE);
        offG = offscreen.createGraphics();
        offG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        offG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Called when the window may have moved to a different screen. */
    private void recreateOffscreenIfNeeded() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null && gc != currentGC) {
            currentGC = gc;
            createOffscreen(gc);
        }
    }

    public void init() {
        currentGC = getGraphicsConfiguration();
        createOffscreen(currentGC);

        // Load all 4 zombie sprite sheets for per-level use
        String[] zombieSheetNames = {
            "ZombieSpriteSheet.png",
            "ZombieSpriteSheet2.png",
            "ZombieSpriteSheet3.png",
            "ZombieSpriteSheetFinal.png"
        };
        imgZombieSheets = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
            imgZombieSheets[i] = SpriteSheet.load(zombieSheetNames[i]);
            Zombie.loadZombieSpritesForLevel(imgZombieSheets[i], i);
        }

        imgHumanSheet = SpriteSheet.load("HumanSpriteSheet.png");
        imgRobotSheet = SpriteSheet.load("RobotSpriteSheet.png");
        imgKey        = SpriteSheet.scale(SpriteSheet.load("keyImage.png"), 70, 70);
        imgCure       = SpriteSheet.load("cure.png");

        Zombie.loadHumanSprites(imgHumanSheet);

        BufferedImage heartSrc = SpriteSheet.load("heart.png");
        imgHeartPickup = SpriteSheet.scale(heartSrc, HEART_PICKUP_W, HEART_PICKUP_H);

        hud.loadAssets(heartSrc,
                       SpriteSheet.load("EmptyHeart.png"),
                       SpriteSheet.load("keyImage.png"));

        // Main menu cycles through all zombie sprite sheets
        menu.setup(imgZombieSheets[3], imgRobotSheet,
                   SpriteSheet.load("keyImage.png"),
                   heartSrc, imgHumanSheet, imgCure, leaderboard);

        menuZombieFrames = new BufferedImage[imgZombieSheets.length][];
        for (int i = 0; i < imgZombieSheets.length; i++) {
            BufferedImage zStrip = SpriteSheet.crop(imgZombieSheets[i], 0, 768, 192*3, 256);
            menuZombieFrames[i] = SpriteSheet.sliceRow(zStrip, 3, 64, 85);
        }
        BufferedImage rStrip = SpriteSheet.crop(imgRobotSheet, 0, 1024, 192*8, 256);
        menuRobotFrames = SpriteSheet.sliceRow(rStrip, 8, 64, 85);

        loadLevel(0);
        timer.start();
        requestFocusInWindow();
    }

    // =========================================================================
    //  Game loop
    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;
        update();
        renderOffscreen();
        repaint();
    }

    private void update() {
        if (state != GameState.PLAYING) return;

        hud.tickHealthRestored();
        player.tickInvincibility();
        player.handleMovement(keysDown, walls);

        float px = player.x, py = player.y;
        for (Zombie z : zombies) z.update(px, py);
        for (Rock r   : rocks)   r.update();

        if (!cureCollected && player.x > level.entryGateCloseX) {
            walls.setEntryGateClosed(true);
        }

        if (level.isFinalLevel) checkCurePickup();
        if (!cureCollected)     checkKeyPickups();
        if (cureCollected && (!allZombiesHealed || (healBackMode && !level.isFinalLevel))) checkHealZombies();

        checkHeartPickups();

        if (gateOpenBannerFrames    > 0) gateOpenBannerFrames--;
        if (curePickupBannerFrames  > 0) curePickupBannerFrames--;
        if (allHealedBannerFrames   > 0) allHealedBannerFrames--;
        if (entranceOpenBannerFrames > 0) entranceOpenBannerFrames--;

        checkEnemyCollisions();
        checkWinCondition();
    }

    // =========================================================================
    //  Rendering
    private void renderOffscreen() {
        Graphics2D g = offG;
        menu.setHover(mouseX, mouseY);
        switch (state) {
            case MENU:
                BufferedImage rf = menuRobotFrames[(int)(frameCount/3) % menuRobotFrames.length];
                int zombieTypeIdx = (int)(frameCount / 90) % menuZombieFrames.length;
                menu.drawMenu(g, W, H, menuZombieFrames[zombieTypeIdx], rf, frameCount);
                if (SHOW_DEBUG_BUTTON) drawDebugButton(g);
                break;
            case LEADERBOARD:
                menu.drawLeaderboard(g, W, H);
                break;
            case PLAYING:
                renderGame(g);
                break;
            case WIN:
                menu.drawWinScreen(g, W, H, gameFinishTimeMs - gameStartTimeMs, leaderboard);
                break;
            case GAME_OVER:
                menu.drawGameOver(g, W, H);
                break;
        }
    }

    private void drawDebugButton(Graphics2D g) {
        int bx = DBG_BTN[0], by = DBG_BTN[1], bw = DBG_BTN[2], bh = DBG_BTN[3];
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(debugMode ? new Color(0x22, 0xBB, 0x22) : new Color(0xBB, 0x22, 0x22));
        g.fillRoundRect(bx, by, bw, bh, 6, 6);
        g.setColor(new Color(0xCC, 0xE8, 0xCC));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, by, bw, bh, 6, 6);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String label = debugMode ? "DBG: ON" : "DBG: OFF";
        g.setColor(Color.WHITE);
        g.drawString(label, bx + (bw - fm.stringWidth(label)) / 2,
                            by + bh / 2 + fm.getAscent() / 2 - 2);
        g.setComposite(old);
    }

    private void drawDebugPanel(Graphics2D g) {
        int p = 6;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(DBG_PANEL_X - p, DBG_PANEL_Y - p, DBG_BTN_W + p*2, DBG_BTN_GAP*2 + DBG_BTN_H + p*2, 8, 8);
        g.setColor(new Color(0xFF, 0x66, 0x00, 200));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(DBG_PANEL_X - p, DBG_PANEL_Y - p, DBG_BTN_W + p*2, DBG_BTN_GAP*2 + DBG_BTN_H + p*2, 8, 8);
        g.setComposite(old);
        drawToggleBtn(g, DBG_FLASHLIGHT_BTN, "Flashlight",  debugFlashlightOn);
        drawToggleBtn(g, DBG_COLLISION_BTN,  "Collisions",  debugCollisionsOn);
        drawToggleBtn(g, DBG_GATES_BTN,      "Auto-Gates",  debugGatesAutoOpen);
    }

    private void drawToggleBtn(Graphics2D g, int[] btn, String label, boolean on) {
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(on ? new Color(0x18, 0x88, 0x18) : new Color(0x88, 0x18, 0x18));
        g.fillRoundRect(btn[0], btn[1], btn[2], btn[3], 4, 4);
        g.setColor(new Color(0xCC, 0xE8, 0xCC, 160));
        g.setStroke(new BasicStroke(0.8f));
        g.drawRoundRect(btn[0], btn[1], btn[2], btn[3], 4, 4);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String full = label + ": " + (on ? "ON" : "OFF");
        g.setColor(Color.WHITE);
        g.drawString(full, btn[0] + (btn[2] - fm.stringWidth(full)) / 2,
                           btn[1] + btn[3] / 2 + fm.getAscent() / 2 - 2);
        g.setComposite(old);
    }

    private void renderGame(Graphics2D g) {
        int bgW = currentBg.getWidth(), bgH = currentBg.getHeight();
        for (int col = 0; col < W; col += bgW)
            for (int row = 0; row < H; row += bgH)
                g.drawImage(currentBg, col, row, null);

        g.setColor(level.wallColor);
        for (int[] w : level.walls) g.fillRect(w[0], w[1], w[2], w[3]);

        // Draw entry gate cover
        boolean entryGateVisible = (!cureCollected && player.x > level.entryGateCloseX)
                || (healBackMode && !level.isFinalLevel && levelZombiesHealedCount < zombies.length);
        if (entryGateVisible) {
            g.setColor(level.gateColor);
            int[] eg = level.entryGate;
            g.fillRect(eg[0], eg[1], eg[2], eg[3]);
        }

        boolean gateOpen = (debugMode && debugGatesAutoOpen) ? true
                : level.isFinalLevel ? allZombiesHealed
                : (keysCollected >= level.keysRequired);
        // In heal-back mode: block the right exit gate so player can't re-enter already-healed levels
        boolean rightGateBlocked = healBackMode && !level.isFinalLevel;
        if (!gateOpen || rightGateBlocked) {
            g.setColor(level.gateColor);
            int[] gt = level.gate;
            g.fillRect(gt[0], gt[1], gt[2], gt[3]);
        }

        if (!level.isFinalLevel) {
            int kSize = Math.max(20, (int)(70 * level.levelScale));
            for (int i = 0; i < keyCollected.length; i++) {
                if (!keyCollected[i]) {
                    float kPulse = 1f + 0.08f * (float)Math.sin(frameCount * 0.10 + i * 1.3);
                    int kpw = (int)(kSize * kPulse), kph = (int)(kSize * kPulse);
                    g.drawImage(imgKey,
                                level.keyPositions[i][0] - (kpw - kSize) / 2,
                                level.keyPositions[i][1] - (kph - kSize) / 2,
                                kpw, kph, null);
                }
            }
        } else if (!cureCollected) {
            int cSize = Math.max(20, (int)(CURE_W * level.levelScale));
            float cPulse = 1f + 0.12f * (float)Math.sin(frameCount * 0.11);
            int cpw = (int)(cSize * cPulse), cph = (int)(cSize * cPulse);
            g.drawImage(imgCure, (int)cureX - (cpw - cSize) / 2, (int)cureY - (cph - cSize) / 2, cpw, cph, null);
        }

        if (imgHeartPickup != null && level.heartPositions != null) {
            int hSize = Math.max(16, (int)(HEART_PICKUP_W * level.levelScale));
            for (int i = 0; i < level.heartPositions.length; i++) {
                if (!heartCollected[i]) {
                    float pulse = 1f + 0.1f * (float)Math.sin(frameCount * 0.12 + i);
                    int pw = (int)(hSize * pulse), ph = (int)(hSize * pulse);
                    g.drawImage(imgHeartPickup,
                                level.heartPositions[i][0] - (pw - hSize) / 2,
                                level.heartPositions[i][1] - (ph - hSize) / 2,
                                pw, ph, null);
                }
            }
        }

        for (Zombie z : zombies) {
            z.draw(g, frameCount);
        }

        for (Rock r : rocks) r.draw(g);
        player.draw(g, frameCount, player.isMoving(keysDown));

        boolean showFlashlight = debugMode ? debugFlashlightOn : true;
        if (showFlashlight) {
            int flW = (int)(currentFlashlight.getWidth()  * level.flashlightScale);
            int flH = (int)(currentFlashlight.getHeight() * level.flashlightScale);
            g.drawImage(currentFlashlight,
                        (int)(player.x - flW / 2f + player.getFrameW() / 2f),
                        (int)(player.y - flH / 2f + player.getFrameH() / 2f),
                        flW, flH, null);
        }

        hud.draw(g, player.lives, Player.MAX_LIVES,
                 keysCollected, level.keysRequired,
                 level.levelIndex, level.isFinalLevel, cureCollected,
                 allZombiesHealed, zombiesHealedCount, zombies.length,
                 W, H, frameCount, gameStartTimeMs);

        if (debugMode) {
            g.setFont(new Font("Courier New", Font.BOLD, 13));
            String dbgLabel = "[ DEBUG MODE ]";
            FontMetrics fm = g.getFontMetrics();
            int dx = (W - fm.stringWidth(dbgLabel)) / 2;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(dx - 6, 34, fm.stringWidth(dbgLabel) + 12, 22, 5, 5);
            g.setColor(new Color(0xFF, 0x66, 0x00));
            g.drawString(dbgLabel, dx, 50);
            drawDebugPanel(g);
        }

        if (gateOpenBannerFrames    > 0) drawBanner(g, "Gate Open. Find the exit!",
                new Color(0x0A, 0x2A, 0x0A, 210), new Color(0xCC, 0xE8, 0xCC), gateOpenBannerFrames);
        if (curePickupBannerFrames  > 0) drawBanner(g, "Cure collected. Go back and heal the zombies!",
                new Color(0x1A, 0x00, 0x2A, 220), new Color(0xDD, 0x88, 0xFF), curePickupBannerFrames);
        if (allHealedBannerFrames   > 0) drawBanner(g, "All zombies healed. Reach the exit!",
                new Color(0x00, 0x2A, 0x0A, 220), new Color(0x88, 0xFF, 0xAA), allHealedBannerFrames);
        if (entranceOpenBannerFrames > 0) drawBanner(g, "Entrance open. Continue going back!",
                new Color(0x00, 0x1A, 0x2A, 220), new Color(0x88, 0xDD, 0xFF), entranceOpenBannerFrames);
    }

    private void drawBanner(Graphics2D g, String msg, Color bg, Color fg, int framesLeft) {
        g.setFont(new Font("Courier New", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int msgW = fm.stringWidth(msg), msgH = fm.getAscent();
        int bx = (W - msgW) / 2 - 20, by = 52, bw = msgW + 40, bh = msgH + 22;
        float alpha = framesLeft < 60 ? framesLeft / 60f : 1f;
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(bg);  g.fillRoundRect(bx, by, bw, bh, 16, 16);
        g.setColor(fg);  g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bx, by, bw, bh, 16, 16);
        g.drawString(msg, bx + 20, by + msgH + 6);
        g.setComposite(old);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (offscreen == null) return;

        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (offscreen.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            currentGC = gc;
            createOffscreen(gc);
            renderOffscreen();
        }

        int pw = getWidth(), ph = getHeight();
        double scaleX = (double) pw / W;
        double scaleY = (double) ph / H;
        double scale  = Math.min(scaleX, scaleY);
        int drawW = (int)(W * scale);
        int drawH = (int)(H * scale);
        int ox = (pw - drawW) / 2;
        int oy = (ph - drawH) / 2;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, pw, ph);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(offscreen, ox, oy, drawW, drawH, null);
    }

    /**
     * Maps a raw panel mouse coordinate to the logical game coordinate (W x H space).
     */
    private int[] toLogical(int px, int py) {
        int pw = getWidth(), ph = getHeight();
        double scale = Math.min((double) pw / W, (double) ph / H);
        int ox = (pw - (int)(W * scale)) / 2;
        int oy = (ph - (int)(H * scale)) / 2;
        int lx = (int)((px - ox) / scale);
        int ly = (int)((py - oy) / scale);
        return new int[]{ lx, ly };
    }

    // =========================================================================
    //  Gameplay logic
    private void checkKeyPickups() {
        for (int i = 0; i < keyCollected.length; i++) {
            if (keyCollected[i]) continue;
            if (player.overlaps(level.keyPositions[i][0], level.keyPositions[i][1], 70, 70)) {
                keyCollected[i] = true;
                keysCollected++;
                if (keysCollected >= level.keysRequired) {
                    walls.setGateOpen(true);
                    gateOpenBannerFrames = BANNER_DURATION;
                }
            }
        }
    }

    private void checkHeartPickups() {
        if (level.heartPositions == null) return;
        int hSize = Math.max(16, (int)(HEART_PICKUP_W * level.levelScale));
        for (int i = 0; i < level.heartPositions.length; i++) {
            if (heartCollected[i]) continue;
            if (player.overlaps(level.heartPositions[i][0], level.heartPositions[i][1], hSize, hSize)) {
                heartCollected[i] = true;
                player.lives++;
                hud.showHealthRestored();
            }
        }
    }

    private void checkCurePickup() {
        int cSize = Math.max(20, (int)(CURE_W * level.levelScale));
        if (!cureCollected && player.overlaps(cureX, cureY, cSize, cSize)) {
            cureCollected = true;
            player.lives = Player.MAX_LIVES;
            hud.showHealthRestored();
            curePickupBannerFrames = BANNER_DURATION * 2;
            walls.setEntryGateClosed(false);
        }
    }

    private void checkHealZombies() {
        for (Zombie z : zombies) {
            if (z.isHealed()) continue;
            if (player.overlaps(z.x, z.y, z.width, z.height)) {
                z.heal();
                zombiesHealedCount++;
                levelZombiesHealedCount++;
                player.forceInvincible(30);
            }
        }
        if (zombiesHealedCount >= zombies.length && !allZombiesHealed) {
            allZombiesHealed = true;
            walls.setGateOpen(true);
            allHealedBannerFrames = BANNER_DURATION;
        }

        if (healBackMode && !level.isFinalLevel
                && levelZombiesHealedCount >= zombies.length) {
            walls.setEntryGateClosed(false);
            if (entranceOpenBannerFrames == 0) {
                entranceOpenBannerFrames = BANNER_DURATION;
            }
        }
    }

    private void checkEnemyCollisions() {
        if (debugMode && !debugCollisionsOn) return;
        if (player.isInvincible()) return;
        for (Zombie z : zombies) {
            if (z.getMode() == Zombie.Mode.HUMAN) continue;
            if (cureCollected) continue; 
            if (player.overlaps(z.x, z.y, z.width, z.height)) {
                int before = player.lives;
                player.loseLife(level.playerStartX, level.playerStartY);
                if (before - 1 <= 0) { state = GameState.GAME_OVER; return; }
            }
        }
        for (Rock r : rocks) {
            if (r.overlapsPlayer(player.hitX(), player.hitY(), player.hitW, player.hitH)) {
                int before = player.lives;
                player.loseLife(level.playerStartX, level.playerStartY);
                if (before - 1 <= 0) { state = GameState.GAME_OVER; return; }
            }
        }
    }

    private void checkWinCondition() {
        boolean canExitRight = (debugMode && debugGatesAutoOpen) ? true
                : level.isFinalLevel ? allZombiesHealed
                : (keysCollected >= level.keysRequired)
                  && !healBackMode;  // In heal-back mode, right exit is always locked

        // Right exit: advance to next level (or win)
        if (canExitRight && player.hitX() + player.hitW >= level.winTriggerX) {
            int next = currentLevelIndex + 1;
            if (next < Levels.LEVELS.length) {
                currentLevelIndex = next;
                if (Levels.LEVELS[next].isFinalLevel && cureCollected) {
                    loadLevelForHealBack(next);
                } else {
                    loadLevel(next);
                }
            } else {
                gameFinishTimeMs = System.currentTimeMillis();
                menu.resetWinInput();
                state = GameState.WIN;
            }
            return;
        }

        // Left exit (cure collected, heal-back mode) go back to previous level
        if (cureCollected && player.hitX() <= 30) {
            if (currentLevelIndex == 0) {
                // Exited the first level — game complete
                gameFinishTimeMs = System.currentTimeMillis();
                menu.resetWinInput();
                state = GameState.WIN;
            } else {
                currentLevelIndex--;
                loadLevelForHealBack(currentLevelIndex);
            }
        }
    }

    // =========================================================================
    //  Level management
    private void loadLevel(int idx) {
        level = Levels.LEVELS[idx];
        keyCollected       = new boolean[level.keyPositions.length];
        keysCollected      = 0;
        cureCollected      = false;
        zombiesHealedCount = 0;
        allZombiesHealed   = false;
        levelZombiesHealedCount = 0;
        healBackMode       = false;
        gateOpenBannerFrames    = 0;
        curePickupBannerFrames  = 0;
        allHealedBannerFrames   = 0;
        entranceOpenBannerFrames = 0;
        heartCollected = new boolean[level.heartPositions != null ? level.heartPositions.length : 0];

        currentBg         = SpriteSheet.load(level.backgroundFile);
        currentFlashlight = SpriteSheet.load(level.flashlightFile);

        player.loadSprites(imgRobotSheet, level.playerScale);
        player.resetPosition(level.playerStartX, level.playerStartY, walls);

        walls.setWalls(level.walls, level.gate, level.entryGate);
        walls.setGateOpen((debugMode && debugGatesAutoOpen)
                || (level.keysRequired == 0 && !level.isFinalLevel));

        // Load per-level zombie sprites
        int sheetIdx = Math.min(idx, imgZombieSheets.length - 1);
        Zombie.loadZombieSpritesForLevel(imgZombieSheets[sheetIdx], idx);

        zombies = new Zombie[level.zombiePatrols.length];
        for (int i = 0; i < zombies.length; i++) {
            zombies[i] = new Zombie(level.zombiePatrols[i], level.levelScale, walls, idx);
            if (level.isFinalLevel) zombies[i].setMode(Zombie.Mode.CHASING);
        }

        rocks = new Rock[level.rockCount];
        for (int i = 0; i < level.rockCount; i++) {
            rocks[i] = new Rock(60 + rng.nextFloat() * (W - 120), rng.nextFloat() * H, W, H, rng);
        }

        if (level.isFinalLevel) { cureX = 1510; cureY = 50; }
    }

    private void loadLevelForHealBack(int idx) {
        level = Levels.LEVELS[idx];
        healBackMode = true;

        // Mark all keys collected so the right exit gate stays open for traversal
        keyCollected  = new boolean[level.keyPositions.length];
        for (int i = 0; i < keyCollected.length; i++) keyCollected[i] = true;
        keysCollected = level.keysRequired;

        // cureCollected / allZombiesHealed / zombiesHealedCount carry over (global totals)
        // Reset per-level counter so entry gate opens only after this level's zombies are healed
        levelZombiesHealedCount = 0;

        gateOpenBannerFrames    = 0;
        entranceOpenBannerFrames = 0;
        heartCollected = new boolean[level.heartPositions != null ? level.heartPositions.length : 0];
        for (int i = 0; i < heartCollected.length; i++) heartCollected[i] = true;

        currentBg         = SpriteSheet.load(level.backgroundFile);
        currentFlashlight = SpriteSheet.load(level.flashlightFile);

        player.loadSprites(imgRobotSheet, level.playerScale);

        // Player enters from the right (came from the level to the right)
        float startX = level.winTriggerX - player.getFrameW() - 20;
        float startY = level.playerStartY + (idx == 2 ? 150f : 0f);
        player.resetPosition(startX, startY, walls);

        walls.setWalls(level.walls, level.gate, level.entryGate);
        walls.setGateOpen(true);        
        boolean noZombies = level.zombiePatrols.length == 0;
        walls.setEntryGateClosed(!level.isFinalLevel && !noZombies);

        int sheetIdx = Math.min(idx, imgZombieSheets.length - 1);
        Zombie.loadZombieSpritesForLevel(imgZombieSheets[sheetIdx], idx);

        zombies = new Zombie[level.zombiePatrols.length];
        for (int i = 0; i < zombies.length; i++) {
            zombies[i] = new Zombie(level.zombiePatrols[i], level.levelScale, walls, idx);
            if (level.isFinalLevel) zombies[i].setMode(Zombie.Mode.CHASING);
        }

        rocks = new Rock[level.rockCount];
        for (int i = 0; i < level.rockCount; i++) {
            rocks[i] = new Rock(60 + rng.nextFloat() * (W - 120), rng.nextFloat() * H, W, H, rng);
        }

        // Cure is in the final level only; not shown in prior levels
        if (level.isFinalLevel) { cureX = 1510; cureY = 50; }
    }

    private void startGame() {
        currentLevelIndex = 0;
        player.lives = Player.MAX_LIVES;
        loadLevel(0);
        gameStartTimeMs = System.currentTimeMillis();
        state = GameState.PLAYING;
    }

    private void startDebugGame() {
        debugMode = true;
        debugFlashlightOn = false; debugCollisionsOn = false; debugGatesAutoOpen = true;
        currentLevelIndex = 0;
        player.lives = Player.MAX_LIVES;
        loadLevel(0);
        gameStartTimeMs = System.currentTimeMillis();
        state = GameState.PLAYING;
    }

    private void resetGame() {
        debugMode = false;
        currentLevelIndex = 0;
        state = GameState.MENU;
    }

    // =========================================================================
    //  Input
    @Override
    public void keyPressed(KeyEvent e) {
        int  code = e.getKeyCode();
        char c    = Character.toLowerCase(e.getKeyChar());

        if      (code == KeyEvent.VK_UP)    keysDown.add('\u2191');
        else if (code == KeyEvent.VK_DOWN)  keysDown.add('\u2193');
        else if (code == KeyEvent.VK_LEFT)  keysDown.add('\u2190');
        else if (code == KeyEvent.VK_RIGHT) keysDown.add('\u2192');
        else keysDown.add(c);

        // quit
        if (c == 'q' || code == KeyEvent.VK_ESCAPE) {
            if      (state == GameState.PLAYING)     resetGame();
            else if (state == GameState.LEADERBOARD) state = GameState.MENU;
            else if (state == GameState.WIN || state == GameState.GAME_OVER) resetGame();
        }

        // Forward key events to win screen name entry
        if (state == GameState.WIN) {
            menu.handleWinKeyPress(e, leaderboard, gameFinishTimeMs - gameStartTimeMs);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if      (code == KeyEvent.VK_UP)    keysDown.remove('\u2191');
        else if (code == KeyEvent.VK_DOWN)  keysDown.remove('\u2193');
        else if (code == KeyEvent.VK_LEFT)  keysDown.remove('\u2190');
        else if (code == KeyEvent.VK_RIGHT) keysDown.remove('\u2192');
        else keysDown.remove(Character.toLowerCase(e.getKeyChar()));
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int[] lp = toLogical(e.getX(), e.getY());
        mouseX = lp[0]; mouseY = lp[1];
        menu.setHover(mouseX, mouseY);
        switch (state) {
            case MENU:
                if (menu.isHovered(MenuRenderer.BTN_BEGIN,       mouseX, mouseY)) startGame();
                if (menu.isHovered(MenuRenderer.BTN_LEADERBOARD, mouseX, mouseY)) state = GameState.LEADERBOARD;
                if (SHOW_DEBUG_BUTTON && hitTest(DBG_BTN, mouseX, mouseY)) startDebugGame();
                break;
            case LEADERBOARD:
                if (menu.isHovered(MenuRenderer.BTN_BACK, mouseX, mouseY)) state = GameState.MENU;
                break;
            case WIN:
                if (menu.isHovered(MenuRenderer.BTN_WIN_AGAIN, mouseX, mouseY)) resetGame();
                break;
            case GAME_OVER:
                if (menu.isHovered(MenuRenderer.BTN_PLAY_AGAIN, mouseX, mouseY)) resetGame();
                break;
            case PLAYING:
                if (debugMode) handleDebugClick(mouseX, mouseY);
                break;
            default: break;
        }
    }

    private void handleDebugClick(int mx, int my) {
        if (hitTest(DBG_FLASHLIGHT_BTN, mx, my)) { debugFlashlightOn = !debugFlashlightOn; return; }
        if (hitTest(DBG_COLLISION_BTN,  mx, my)) { debugCollisionsOn = !debugCollisionsOn; return; }
        if (hitTest(DBG_GATES_BTN,      mx, my)) {
            debugGatesAutoOpen = !debugGatesAutoOpen;
            walls.setGateOpen(debugGatesAutoOpen || keysCollected >= level.keysRequired || allZombiesHealed);
            return;
        }
        float nx = mx - player.getFrameW() / 2f;
        float ny = my - player.getFrameH() / 2f;
        player.x = Math.max(0, Math.min(nx, W - player.getFrameW()));
        player.y = Math.max(0, Math.min(ny, H - player.getFrameH()));
    }

    private boolean hitTest(int[] btn, int mx, int my) {
        return mx >= btn[0] && mx <= btn[0] + btn[2] && my >= btn[1] && my <= btn[1] + btn[3];
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    @Override public void mouseMoved(MouseEvent   e) { int[] lp = toLogical(e.getX(), e.getY()); mouseX = lp[0]; mouseY = lp[1]; }
    @Override public void mouseDragged(MouseEvent e) { int[] lp = toLogical(e.getX(), e.getY()); mouseX = lp[0]; mouseY = lp[1]; }
}
