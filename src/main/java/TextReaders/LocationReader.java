package TextReaders;

import com.sun.jna.Memory;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.HashMap;
import java.util.Map;

public class LocationReader extends Reader {
    private static final int[][] locationPoints = new int[][] {
            {58, 7}, {27, 3}, {84, 9}, {1, 6}, {47, 3}, {21, 2}, {20, 8}, {76, 10},
            {80, 5}, {9, 4}, {49, 2}, {29, 7}, {54, 7}, {91, 3}, {9, 0}, {38, 4}
    };
    private static final Map<Integer, String> locationHashes = new HashMap<>();
    public LocationReader(HWND handle) {
        initialize();
        this.handle = handle;
    }

    public String read() {
        String location = locationHashes.get(getHash());
        return location == null ? "" : location;
    }

    private int getHash() {
        User32.INSTANCE.SetForegroundWindow(handle);
        Memory buffer = getBuffer(658, 33, 112, 14);
        int hash = 0;
        for (int i = 0; i < locationPoints.length; i++) {
            int pixelOffset = (locationPoints[i][1] * 112 + locationPoints[i][0]) * 4;
            int green = buffer.getByte(pixelOffset + 1) & 0xFF;
            int red = buffer.getByte(pixelOffset + 2) & 0xFF;
            if (red >= 240 && green >= 240) hash |= (1 << i);
        }
        return hash;
    }

    private void initialize() {
        if (locationHashes.isEmpty()) {
            String[] locations = new String[]{
                    "kt", "tttc", "lpm", "bkd",
                    "ktng", "bhc", "nnl", "lhc", "gn",
                    "lm", "lssl", "lstk", "tbks",
                    "tvd", "vdd", "dnd",
                    "ptv", "klh", "td", "kdn",
                    "ktdg", "tyl", "tyl-tkt", "tkt",
                    "tt", "dm", "tld", "qv", "ksl", "lho", "dvd",
                    "dps", "pvl", "vmn", "hht", "qdvmd",
                    "htt", "lth", "bhd", "ncp"
            };
            int[] hashes = new int[]{
                    36872, 34384, 11, 35208, 37128, 681, 11854, 33800, 33898, 33288,
                    8220, 8, 2068, 32800, 2601, 712, 33289, 4120, 7681, 2089,
                    37256, 2565, 2561, 4737, 4112, 56, 2752, 74, 4106, 4104,
                    4505, 4888, 34312, 908, 33290, 6280, 73, 32776, 665, 200
            };
            for (int i = 0; i < locations.length; i++) {
                locationHashes.put(hashes[i], locations[i]);
            }
        }
    }
}
