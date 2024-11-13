package TextReaders;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;
public class PointsReader {
    private final HWND handle;
    private static final int[][] numberPoints = new int[][] {{1, 1}, {1, 4}, {2, 7}, {3, 1}, {5, 1}, {5, 8}};
    private static final Map<Integer, Integer> numberHashes = Map.of(7, 0, 9, 1, 52, 2, 16, 3, 50, 4, 1, 5, 38, 6, 4, 7, 17, 8, 19, 9);
    private static final int[] numberDistances = new int[] {6, 8, 8, 7, 7, 7, 7, 9, 6, 7};
    public PointsReader(HWND handle) {
        this.handle = handle;
    }

    public int read() {
        BufferedImage image = captureWindow();
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

    private BufferedImage captureWindow() {
        WinDef.HDC windowDC = User32.INSTANCE.GetDC(handle); // Get the window's device context (DC)
        WinDef.HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC); // Create a compatible DC in memory
        WinDef.HBITMAP memBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, 40, 9);
        GDI32.INSTANCE.SelectObject(memDC, memBitmap); // Select the bitmap into the memory DC

        // BitBlt to copy the window content to the memory DC
        GDI32.INSTANCE.BitBlt(memDC, 0, 0, 40, 9, windowDC, 293, 283, GDI32.SRCCOPY);

        // Get the bitmap info
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = 40;
        bmi.bmiHeader.biHeight = -9; // Negative to indicate top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Allocate memory for pixel data
        Memory buffer = new Memory(40 * 9 * 4); // 4 bytes per pixel (32-bit)

        // Retrieve the pixel data into the buffer
        GDI32.INSTANCE.GetDIBits(memDC, memBitmap, 0, 9, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // Release resources
        GDI32.INSTANCE.DeleteObject(memBitmap);
        GDI32.INSTANCE.DeleteDC(memDC);
        User32.INSTANCE.ReleaseDC(handle, windowDC);

        // Convert the pixel data into a BufferedImage
        BufferedImage image = new BufferedImage(40, 9, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 40; col++) {
                int pixelOffset = (row * 40 + col) * 4;
                int blue = buffer.getByte(pixelOffset) & 0xFF;
                int green = buffer.getByte(pixelOffset + 1) & 0xFF;
                int red = buffer.getByte(pixelOffset + 2) & 0xFF;
                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(col, row, rgb);
            }
        }
        return image;
    }
}
