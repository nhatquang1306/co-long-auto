package TextReaders;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.sun.jna.platform.win32.WinDef.HWND;

public class CoordinatesReader {
    private final HWND handle;
    private static final Map<Integer, Integer> numberHashes = Map.of(6, 0, 4, 1, 21, 2, 29, 3, 18, 4, 13, 5, 14, 6, 1, 7, 28, 8, 22, 9);
    private static final int[] numberDistances = new int[] {6, 8, 8, 7, 7, 7, 7, 9, 6, 7};
    private static final int[][] numberPoints = new int[][] {{1, 0}, {1, 4}, {3, 8}, {4, 3}, {5, 1}, {5, 3}};

    public CoordinatesReader(HWND handle) {
        this.handle = handle;
    }
    public int[] read() {
        int[] res = new int[2];
        BufferedImage image = captureWindow();

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

    private BufferedImage captureWindow() {
        WinDef.HDC windowDC = User32.INSTANCE.GetDC(handle); // Get the window's device context (DC)
        WinDef.HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC); // Create a compatible DC in memory
        WinDef.HBITMAP memBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, 75, 9);
        GDI32.INSTANCE.SelectObject(memDC, memBitmap); // Select the bitmap into the memory DC

        // BitBlt to copy the window content to the memory DC
        GDI32.INSTANCE.BitBlt(memDC, 0, 0, 75, 9, windowDC, 671, 27, GDI32.SRCCOPY);

        // Get the bitmap info
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = 75;
        bmi.bmiHeader.biHeight = -9; // Negative to indicate top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Allocate memory for pixel data
        Memory buffer = new Memory(75 * 9 * 4); // 4 bytes per pixel (32-bit)

        // Retrieve the pixel data into the buffer
        GDI32.INSTANCE.GetDIBits(memDC, memBitmap, 0, 9, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // Release resources
        GDI32.INSTANCE.DeleteObject(memBitmap);
        GDI32.INSTANCE.DeleteDC(memDC);
        User32.INSTANCE.ReleaseDC(handle, windowDC);

        // Convert the pixel data into a BufferedImage
        BufferedImage image = new BufferedImage(75, 9, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 75; col++) {
                int pixelOffset = (row * 75 + col) * 4;
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
