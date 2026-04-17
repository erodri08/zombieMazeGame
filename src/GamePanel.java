import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Core game panel. Uses a Swing Timer at ~60 fps for the game loop.
 * All rendering is done with Java2D (Graphics2D) — zero external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {

    // Screen 
    public static final int W = 1640;
    public static final int H = 840;

    // Colours 
    private static final Color WALL_COLOR = new Color(0x28, 0x42, 0x1C);
    private static final Color GATE_COLOR = new Color(0x7A, 0x51, 0x0D);
    private static final Color BG_FALLBACK = new Color(139, 163, 155);

    // Game loop 
    private final Timer timer;
    private long frameCount = 0;

    // State 
    private GameState state = GameState.MENU;

    // Input 
    private final Set<Character> keysDown = new HashSet<>();
    private int mouseX, mouseY;

    // Subsystems 
    private final Player          player   = new Player();
    private final CollisionChecker walls   = new CollisionChecker();
    private final HUD             hud      = new HUD();
    private final MenuRenderer    menu     = new MenuRenderer();
    private final Random          rng      = new Random();

    // Level state 
    private LevelData    level;
    private boolean[]    keyCollected;
    private int          keysCollected;
    private Zombie[]     zombies;
    private Rock[]       rocks;

    // Assets 
    private BufferedImage imgBackground;
    private BufferedImage imgKey;
    private BufferedImage imgFlashlight;
    private BufferedImage imgZombieSheet;
    private BufferedImage imgRobotSheet;

    // Cached animation frames for the menu screen
    private BufferedImage[] menuZombieFrames;
    private BufferedImage[] menuRobotFrames;

    // Double-buffer image (avoids flicker)
    private BufferedImage offscreen;
    private Graphics2D    offG;

    // =========================================================================
    //  Construction
    // =========================================================================

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        timer = new Timer(16, this);  // ~62.5 fps
    }

    /** Load all assets and start the game loop. Call after the panel is shown. */
    public void init() {
        offscreen = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        offG      = offscreen.createGraphics();
        offG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        offG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Load images
        imgBackground  = SpriteSheet.load("Background.png");
        imgKey         = SpriteSheet.scale(SpriteSheet.load("keyImage.png"), 100, 100);
        imgFlashlight  = SpriteSheet.load("flashlightCircle2.png");
        imgFlashlight  = SpriteSheet.scale(imgFlashlight, imgFlashlight.getWidth() * 2, imgFlashlight.getHeight() * 2);
        imgZombieSheet = SpriteSheet.load("ZombieSpriteSheet.png");
        imgRobotSheet  = SpriteSheet.load("RobotSpriteSheet.png");

        // Load HUD assets
        BufferedImage heartSrc      = SpriteSheet.load("heart.png");
        BufferedImage emptyHeartSrc = SpriteSheet.load("EmptyHeart.png");
        hud.loadAssets(heartSrc, emptyHeartSrc, SpriteSheet.load("keyImage.png"));

        // Load zombie/robot sprites for subsystems and menus
        Zombie.loadSprites(imgZombieSheet);
        player.loadSprites(imgRobotSheet);

        // Cache menu animation frames
        BufferedImage zWalkStrip = SpriteSheet.crop(imgZombieSheet, 0, 768, 192 * 3, 256);
        menuZombieFrames = SpriteSheet.sliceRow(zWalkStrip, 3, 64, 85);

        BufferedImage rWalkStrip = SpriteSheet.crop(imgRobotSheet, 0, 1024, 192 * 8, 256);
        menuRobotFrames = SpriteSheet.sliceRow(rWalkStrip, 8, 64, 85);

        menu.setup(imgZombieSheet, imgRobotSheet, SpriteSheet.load("keyImage.png"), heartSrc);

        loadLevel(0);
        timer.start();
        requestFocusInWindow();
    }

    // =========================================================================
    //  Game loop
    // =========================================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;
        update();
        renderOffscreen();
        repaint();
    }

    private void update() {
        if (state != GameState.PLAYING) return;

        player.tickInvincibility();
        player.handleMovement(keysDown, walls, keysCollected);
        for (Zombie z : zombies) z.update();
        for (Rock r : rocks)    r.update();

        checkKeyPickups();
        checkEnemyCollisions();
        checkWinCondition();
    }

    // =========================================================================
    //  Rendering
    // =========================================================================

    private void renderOffscreen() {
        Graphics2D g = offG;

        switch (state) {
            case MENU:
                BufferedImage rFrame = menuRobotFrames[(int)(frameCount / 3) % menuRobotFrames.length];
                menu.drawMenu(g, W, H, menuZombieFrames, rFrame, frameCount);
                break;
            case INSTRUCTIONS:
                menu.drawInstructions(g, W, H);
                break;
            case PLAYING:
                renderGame(g);
                break;
            case WIN:
                menu.drawWinScreen(g, W, H);
                break;
            case GAME_OVER:
                menu.drawGameOver(g, W, H);
                break;
        }
    }

    private void renderGame(Graphics2D g) {
        // Background tile
        int bgW = imgBackground.getWidth();
        int bgH = imgBackground.getHeight();
        for (int col = 0; col < W; col += bgW)
            for (int row = 0; row < H; row += bgH)
                g.drawImage(imgBackground, col, row, null);

        // Maze walls
        g.setColor(WALL_COLOR);
        for (int[] w : level.walls)
            g.fillRect(w[0], w[1], w[2], w[3]);

        // Exit gate (hidden once all keys collected)
        g.setColor(GATE_COLOR);
        if (keysCollected < level.keysRequired) {
            int[] gt = level.gate;
            g.fillRect(gt[0], gt[1], gt[2], gt[3]);
        }

        // Entry gate (closes once player enters the maze)
        if (player.x > level.entryGateCloseX) {
            int[] eg = level.entryGate;
            g.setColor(GATE_COLOR);
            g.fillRect(eg[0], eg[1], eg[2], eg[3]);
        }

        // Keys on the floor
        for (int i = 0; i < keyCollected.length; i++) {
            if (!keyCollected[i])
                g.drawImage(imgKey, level.keyPositions[i][0], level.keyPositions[i][1], null);
        }

        // Zombies
        for (Zombie z : zombies) z.draw(g, frameCount);

        // Rocks
        for (Rock r : rocks) r.draw(g);

        // Player
        player.draw(g, frameCount, player.isMoving(keysDown));

        // Flashlight overlay (darkens everything outside the circle)
        int fx = (int)(player.x - imgFlashlight.getWidth()  / 2f + 35);
        int fy = (int)(player.y - imgFlashlight.getHeight() / 2f + 50);
        g.drawImage(imgFlashlight, fx, fy, null);

        // HUD on top of everything
        hud.draw(g, player.lives, Player.MAX_LIVES, keysCollected, W, H);
    }

    /** Swing calls this; we just blit the offscreen buffer. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (offscreen != null) g.drawImage(offscreen, 0, 0, null);
    }

    // =========================================================================
    //  Gameplay logic
    // =========================================================================

    private void checkKeyPickups() {
        for (int i = 0; i < keyCollected.length; i++) {
            if (keyCollected[i]) continue;
            int kx = level.keyPositions[i][0];
            int ky = level.keyPositions[i][1];
            if (player.x > kx - 50 && player.x < kx + 100
             && player.y > ky - 50 && player.y < ky + 100) {
                keyCollected[i] = true;
                keysCollected++;
            }
        }
    }

    private void checkEnemyCollisions() {
        if (player.isInvincible()) return;
        float respawnX = level.playerStartX + 44;
        float respawnY = level.playerStartY - 1;

        for (Zombie z : zombies) {
            if (player.overlaps(z.x, z.y, Zombie.WIDTH, Zombie.HEIGHT)) {
                player.loseLife(respawnX, respawnY);
                if (player.lives <= 0) { state = GameState.GAME_OVER; return; }
            }
        }
        for (Rock r : rocks) {
            if (r.overlapsPlayer(player.x, player.y)) {
                player.loseLife(respawnX, respawnY);
                if (player.lives <= 0) { state = GameState.GAME_OVER; return; }
            }
        }
    }

    private void checkWinCondition() {
        if (player.x >= level.winTriggerX && keysCollected >= level.keysRequired) {
            state = GameState.WIN;
        }
    }

    // =========================================================================
    //  Level management
    // =========================================================================

    private void loadLevel(int idx) {
        level        = Levels.LEVELS[idx];
        keyCollected = new boolean[level.keyPositions.length];
        keysCollected = 0;

        player.reset(level.playerStartX, level.playerStartY);

        zombies = new Zombie[level.zombiePatrols.length];
        for (int i = 0; i < zombies.length; i++)
            zombies[i] = new Zombie(level.zombiePatrols[i]);

        rocks = new Rock[level.rockCount];
        for (int i = 0; i < rocks.length; i++)
            rocks[i] = new Rock(
                120 + rng.nextFloat() * (W - 120),
                rng.nextFloat() * H,
                W, H, rng);
    }

    private void startGame() {
        loadLevel(0);
        state = GameState.PLAYING;
    }

    private void resetGame() {
        loadLevel(0);
        state = GameState.MENU;
    }

    // =========================================================================
    //  Input handlers
    // =========================================================================

    @Override public void keyPressed(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());
        keysDown.add(c);
    }
    @Override public void keyReleased(KeyEvent e) {
        keysDown.remove(Character.toLowerCase(e.getKeyChar()));
    }
    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        switch (state) {
            case MENU:
                if (menu.isHovered(MenuRenderer.BTN_BEGIN,        mouseX, mouseY)) startGame();
                if (menu.isHovered(MenuRenderer.BTN_INSTRUCTIONS, mouseX, mouseY)) state = GameState.INSTRUCTIONS;
                break;
            case INSTRUCTIONS:
                if (menu.isHovered(MenuRenderer.BTN_BACK,         mouseX, mouseY)) state = GameState.MENU;
                break;
            case WIN:
                if (menu.isHovered(MenuRenderer.BTN_WIN_AGAIN,    mouseX, mouseY)) resetGame();
                break;
            case GAME_OVER:
                if (menu.isHovered(MenuRenderer.BTN_PLAY_AGAIN,   mouseX, mouseY)) resetGame();
                break;
            default: break;
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
