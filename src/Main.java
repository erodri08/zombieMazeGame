import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point for Zombie Maze.
 * @author Ethan Rodrigues
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zombie Maze");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setSize(1640, 840);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.init();
        });
    }
}
