/**
 * Encapsulates all wall-collision logic for Level 1.
 * Each canMove* method returns true when the player may step in that direction.
 * Pure Java — no external dependencies.
 *
 * @author Ethan Rodrigues
 */
public class CollisionChecker {

    private static final int SCREEN_LEFT   = 30;
    private static final int SCREEN_TOP    = 20;
    private static final int SCREEN_BOTTOM = 724;

    public boolean canMoveUp(float px, float py) {
        if (py <= SCREEN_TOP) return false;
        if (in(px, 70,110)   && in(py,690,695)) return false;
        if (in(px,183,790)   && in(py,405,410)) return false;
        if (in(px,270,810)   && in(py,310,315)) return false;
        if (in(px,310,700)   && in(py, 80,115)) return false;
        if (in(px,365,715)   && in(py,190,205)) return false;
        if (in(px,1020,1500) && in(py,105,115)) return false;
        if (in(px,1070,1500) && in(py,195,210)) return false;
        if (in(px,975,1405)  && in(py,290,310)) return false;
        if (in(px,970,1505)  && in(py,390,410)) return false;
        if (in(px,180,915)   && in(py,495,510)) return false;
        if (in(px,270,1000)  && in(py,590,610)) return false;
        if (in(px,205,900)   && in(py,685,700)) return false;
        if (in(px,1005,1560) && in(py,500,510)) return false;
        if (in(px,1100,1400) && in(py,560,610)) return false;
        if (in(px,1173,1515) && in(py,690,710)) return false;
        if (in(px,-30,20)    && in(py,280,340)) return false;
        return true;
    }

    public boolean canMoveDown(float px, float py) {
        if (py >= SCREEN_BOTTOM) return false;
        if (in(px,75,110)    && in(py, 30, 35)) return false;
        if (in(px,183,866)   && in(py,430,435)) return false;
        if (in(px,225,790)   && in(py,330,335)) return false;
        if (in(px,265,720)   && in(py, 30, 40)) return false;
        if (in(px,270,795)   && in(py,230,260)) return false;
        if (in(px,375,670)   && in(py,125,155)) return false;
        if (in(px,970,1500)  && in(py, 30, 45)) return false;
        if (in(px,1060,1520) && in(py,130,145)) return false;
        if (in(px,1020,1420) && in(py,220,240)) return false;
        if (in(px,970,1504)  && in(py,330,350)) return false;
        if (in(px,270,980)   && in(py,525,535)) return false;
        if (in(px,205,900)   && in(py,625,645)) return false;
        if (in(px,970,1560)  && in(py,430,440)) return false;
        if (in(px,1070,1400) && in(py,530,545)) return false;
        if (in(px,1170,1480) && in(py,630,645)) return false;
        if (in(px,-30,20)    && in(py,280,340)) return false;
        return true;
    }

    public boolean canMoveRight(float px, float py, int keysCollected) {
        if (in(px,60,70)     && py<690 && py>50)  return false;
        if (in(px,160,165)   && in(py,-20,390))   return false;
        if (in(px,265,270)   && in(py, 40,200))   return false;
        if (in(px,260,270)   && in(py,240,300))   return false;
        if (in(px,160,170)   && in(py,430,740))   return false;
        if (in(px,765,780)   && in(py, 50,240))   return false;
        if (in(px,660,670)   && in(py,105,160))   return false;
        if (in(px,850,890)   && in(py, 45,440))   return false;
        if (in(px,960,970)   && in(py, 45,300))   return false;
        if (in(px,1465,1480) && in(py,160,390))   return false;
        if (in(px,964,980)   && in(py,430,690))   return false;
        if (in(px,1060,1085) && in(py,550,760))   return false;
        if (in(px,1460,1470) && in(py,470,670))   return false;
        if (in(px,1550,1560) && in(py,  0,575))   return false;
        if (in(px,1550,1560) && in(py,640,790))   return false;
        if (in(px,1550,1560) && in(py,590,640) && keysCollected < 4) return false;
        return true;
    }

    public boolean canMoveLeft(float px, float py) {
        if (px <= SCREEN_LEFT) return false;
        if (in(px,125,130)   && in(py, 40,690)) return false;
        if (in(px,825,830)   && in(py, 40,300)) return false;
        if (in(px,810,815)   && in(py,325,405)) return false;
        if (in(px,315,320)   && in(py, 85,175)) return false;
        if (in(px,715,725)   && in(py, 40,195)) return false;
        if (in(px,920,930)   && in(py, 40,505)) return false;
        if (in(px,1015,1030) && in(py, 65,270)) return false;
        if (in(px,1500,1515) && in(py, 35,100)) return false;
        if (in(px,1505,1530) && in(py,130,400)) return false;
        if (in(px,215,225)   && in(py,470,656)) return false;
        if (in(px,1005,1020) && in(py,460,700)) return false;
        if (in(px,1110,1130) && in(py,580,730)) return false;
        if (in(px,1510,1520) && in(py,490,700)) return false;
        if (in(px,220,230)   && in(py,660,760)) return false;
        if (px > 1560) return false;
        return true;
    }

    private static boolean in(float v, float lo, float hi) {
        return v > lo && v < hi;
    }
}
