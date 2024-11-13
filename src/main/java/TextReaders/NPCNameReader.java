package TextReaders;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;
public class NPCNameReader {
    HWND handle;
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
        WinDef.HDC windowDC = User32.INSTANCE.GetDC(handle); // Get the window's device context (DC)
        WinDef.HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC); // Create a compatible DC in memory
        WinDef.HBITMAP memBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, 80, 14);
        GDI32.INSTANCE.SelectObject(memDC, memBitmap); // Select the bitmap into the memory DC

        // BitBlt to copy the window content to the memory DC
        GDI32.INSTANCE.BitBlt(memDC, 0, 0, 80, 14, windowDC, 492, 235, GDI32.SRCCOPY);

        // Get the bitmap info
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = 80;
        bmi.bmiHeader.biHeight = -14; // Negative to indicate top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Allocate memory for pixel data
        Memory buffer = new Memory(80 * 14 * 4); // 4 bytes per pixel (32-bit)

        // Retrieve the pixel data into the buffer
        GDI32.INSTANCE.GetDIBits(memDC, memBitmap, 0, 14, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // Release resources
        GDI32.INSTANCE.DeleteObject(memBitmap);
        GDI32.INSTANCE.DeleteDC(memDC);
        User32.INSTANCE.ReleaseDC(handle, windowDC);

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
