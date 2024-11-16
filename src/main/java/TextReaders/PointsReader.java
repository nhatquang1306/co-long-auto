package TextReaders;


import java.awt.image.BufferedImage;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;
public class PointsReader extends Reader {
    private static final int[][] numberPoints = new int[][] {{1, 1}, {1, 4}, {2, 7}, {3, 1}, {5, 1}, {5, 8}};
    private static final Map<Integer, Integer> numberHashes = Map.of(7, 0, 9, 1, 52, 2, 16, 3, 50, 4, 1, 5, 38, 6, 4, 7, 17, 8, 19, 9);
    private static final int[] numberDistances = new int[] {6, 8, 8, 7, 7, 7, 7, 9, 6, 7};

    public PointsReader(HWND handle) {
        this.handle = handle;
    }

    public int read() {
        BufferedImage image = captureWindow(296, 309, 40, 9);
        int x = 0, res = 0;
        int hash = getHash(image, x);
        while (numberHashes.containsKey(hash)) {
            int num = numberHashes.get(hash);
            res = res * 10 + num;
            x += numberDistances[num];
            if (x >= 35) break;
            hash = getHash(image, x);
        }
        return res;
    }

    private int getHash(BufferedImage image, int x) {
        int res = 0;
        for (int i = 0; i < 6; i++) {
            if (image.getRGB(x + numberPoints[i][0], numberPoints[i][1]) == -13043656) {
                res |= 1 << i;
            }
        }
        return res;
    }
}
