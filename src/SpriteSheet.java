import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility helpers for loading and slicing sprite-sheet images.
 * Standard Java only — no external dependencies.
 * @author Ethan Rodrigues
 */
public class SpriteSheet {

    private SpriteSheet() {}

    public static BufferedImage load(String filename) {
        try {
            BufferedImage raw = ImageIO.read(new File(filename));
            if (raw == null) throw new IOException("ImageIO returned null for: " + filename);
            BufferedImage argb = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(raw, 0, 0, null);
            g.dispose();
            return argb;
        } catch (IOException e) {
            throw new RuntimeException("Could not load image: " + filename, e);
        }
    }

    public static BufferedImage crop(BufferedImage src, int x, int y, int w, int h) {
        int cx = Math.max(0, Math.min(x, src.getWidth()  - 1));
        int cy = Math.max(0, Math.min(y, src.getHeight() - 1));
        int cw = Math.max(1, Math.min(w, src.getWidth()  - cx));
        int ch = Math.max(1, Math.min(h, src.getHeight() - cy));
        return src.getSubimage(cx, cy, cw, ch);
    }

    public static BufferedImage scale(BufferedImage src, int targetW, int targetH) {
        if (targetW <= 0 || targetH <= 0) return src;
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    public static BufferedImage cropAndScale(BufferedImage src,
                                             int x, int y, int w, int h,
                                             int targetW, int targetH) {
        return scale(crop(src, x, y, w, h), targetW, targetH);
    }

    /** Slice a horizontal strip into 'count' equal-width frames. */
    public static BufferedImage[] sliceRow(BufferedImage strip, int count,
                                           int targetFrameW, int targetFrameH) {
        int cellW = strip.getWidth() / count;
        int cellH = strip.getHeight();
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = cropAndScale(strip, cellW * i, 0, cellW, cellH, targetFrameW, targetFrameH);
        }
        return frames;
    }
}
