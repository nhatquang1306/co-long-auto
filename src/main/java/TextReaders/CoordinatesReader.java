package TextReaders;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;

public class CoordinatesReader extends Reader {
    private static final Map<Integer, Integer> numberHashes = Map.of(6, 0, 4, 1, 21, 2, 29, 3, 18, 4, 13, 5, 14, 6, 1, 7, 28, 8, 22, 9);
    private static final int[] numberDistances = new int[] {6, 8, 8, 7, 7, 7, 7, 9, 6, 7};
    private static final int[][] numberPoints = new int[][] {{1, 0}, {1, 4}, {3, 8}, {4, 3}, {5, 1}, {5, 3}};

    public CoordinatesReader(HWND handle) {
        this.handle = handle;
    }
    public int[] read() {
        try {
            int[] res = new int[2];
            BufferedImage image = captureWindow(674, 53, 75, 9);
            int x = 0;
            for (int k = 0; k < 2; k++) {
                for (int i = 0; i < 3; i++) {
                    Integer val = numberHashes.get(getHash(image, x));
                    while (val == null && i++ < 2) {
                        x += 4;
                        val = numberHashes.get(getHash(image, x));
                    }
                    x += numberDistances[val];
                    res[k] = res[k] * 10 + val;
                }
                x += 23;
            }
            return res;
        } catch (Exception _) {
            return new int[2];
        }
    }

    private static int getHash(BufferedImage image, int x) {
        int hash = 0;
        for (int i = 0; i < 5; i++) {
            if (image.getRGB(x + numberPoints[i][0], numberPoints[i][1]) == -1) {
                hash |= (1 << i);
            }
        }
        return hash;
    }
}
