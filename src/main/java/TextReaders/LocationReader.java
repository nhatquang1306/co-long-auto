package TextReaders;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;

public class LocationReader {
    private final HWND handle;
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
        return locationHashes.get(getHash());
    }

    private int getHash() {
        WinDef.HDC windowDC = User32.INSTANCE.GetDC(handle); // Get the window's device context (DC)
        WinDef.HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC); // Create a compatible DC in memory
        WinDef.HBITMAP memBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, 112, 14);
        GDI32.INSTANCE.SelectObject(memDC, memBitmap); // Select the bitmap into the memory DC

        // BitBlt to copy the window content to the memory DC
        GDI32.INSTANCE.BitBlt(memDC, 0, 0, 112, 14, windowDC, 655, 7, GDI32.SRCCOPY);

        // Get the bitmap info
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = 112;
        bmi.bmiHeader.biHeight = -14; // Negative to indicate top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Allocate memory for pixel data
        Memory buffer = new Memory(112 * 14 * 4); // 4 bytes per pixel (32-bit)

        // Retrieve the pixel data into the buffer
        GDI32.INSTANCE.GetDIBits(memDC, memBitmap, 0, 14, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // Release resources
        GDI32.INSTANCE.DeleteObject(memBitmap);
        GDI32.INSTANCE.DeleteDC(memDC);
        User32.INSTANCE.ReleaseDC(handle, windowDC);

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
