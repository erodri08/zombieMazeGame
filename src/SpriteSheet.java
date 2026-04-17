import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility class for loading and slicing sprite sheet images.
 * Uses only Java standard library (javax.imageio, java.awt).
 *
 * @author Ethan Rodrigues
 */
public class SpriteSheet {

    private SpriteSheet() {}

    /**
     * Load a BufferedImage from the assets folder (working directory).
     */
    public static BufferedImage load(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not load image: " + filename, e);
        }
    }

    /**
     * Crop a sub-region from a source image.
     */
    public static BufferedImage crop(BufferedImage src, int x, int y, int w, int h) {
        // Clamp to actual image bounds to avoid out-of-bounds
        int cx = Math.min(x, src.getWidth()  - 1);
        int cy = Math.min(y, src.getHeight() - 1);
        int cw = Math.min(w, src.getWidth()  - cx);
        int ch = Math.min(h, src.getHeight() - cy);
        return src.getSubimage(cx, cy, cw, ch);
    }

    /**
     * Scale a BufferedImage to the given dimensions using bilinear interpolation.
     */
    public static BufferedImage scale(BufferedImage src, int targetW, int targetH) {
        if (targetW <= 0 || targetH <= 0) return src;
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    /**
     * Crop then scale: shorthand for crop() followed by scale().
     */
    public static BufferedImage cropAndScale(BufferedImage src,
                                             int x, int y, int w, int h,
                                             int targetW, int targetH) {
        return scale(crop(src, x, y, w, h), targetW, targetH);
    }

    /**
     * Slice a horizontal strip into 'count' equal frames.
     * Returns an array of scaled frame images.
     */
    public static BufferedImage[] sliceRow(BufferedImage strip, int count,
                                           int targetFrameW, int targetFrameH) {
        int cellW = strip.getWidth() / count;
        int cellH = strip.getHeight();
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = cropAndScale(strip, cellW * i, 0, cellW, cellH,
                                     targetFrameW, targetFrameH);
        }
        return frames;
    }
}
