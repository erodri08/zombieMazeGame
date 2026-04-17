import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point for Zombie Maze.
 * Uses Java Swing 
 *
 * @author Ethan Rodrigues
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zombie Maze");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);  // center on screen
            frame.setVisible(true);

            panel.init();  // load assets and start game loop
        });
    }
}
