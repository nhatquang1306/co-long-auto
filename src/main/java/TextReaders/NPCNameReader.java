package TextReaders;

import com.sun.jna.Memory;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;

public class NPCNameReader extends Reader {
    private static final int[][] npcPoints = new int[][] {
            {15, 6}, {42, 4}, {57, 10}, {27, 10}, {73, 13},
            {46, 0}, {0, 13}, {51, 4}, {23, 8}, {13, 11}
    };
    private static final Map<Integer, String> npcHashes = new HashMap<>();

    public NPCNameReader(HWND handle) {
        initialize();
        this.handle = handle;
    }

    public String read() {
        String NPC = npcHashes.get(getHash());
        return NPC == null ? "" : NPC;
    }

    private int getHash() {
        Memory buffer = getBuffer(495, 261, 80, 14);
        int hash = 0;
        for (int i = 0; i < npcPoints.length; i++) {
            int pixelOffset = (npcPoints[i][1] * 80 + npcPoints[i][0]) * 4;
            int blue = buffer.getByte(pixelOffset) & 0xFF;
            int green = buffer.getByte(pixelOffset + 1) & 0xFF;
            int red = buffer.getByte(pixelOffset + 2) & 0xFF;
            int rgb = (0xFF << 24) | (red << 16) | (green << 8) | blue;
            if (rgb == -16713488) hash |= (1 << i);
        }
        return hash;
    }
    private void initialize() {
        if (npcHashes.isEmpty()) {
            String[] npcs = new String[] {
                    "tl", "mkq", "nll", "tt", "ht",
                    "kph", "hsy", "ltd", "mqltb", "ccs",
                    "dtt", "ctt", "tdpq", "slh", "tdh"};
            int[] hashes = new int[] {
                    7, 130, 256, 129, 128,
                    5, 512, 4, 2, 138,
                    396, 266, 137, 8, 13
            };
            for (int i = 0; i < hashes.length; i++) {
                npcHashes.put(hashes[i], npcs[i]);
            }
        }
    }
}
