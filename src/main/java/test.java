import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.Normalizer;
import java.util.*;

import javax.imageio.ImageIO;

import static com.sun.jna.platform.win32.WinUser.*;

public class test {
    private static double scale;
    private static HWND hwnd;
    private static Tesseract tesseract;
    private static Object lock;
    private static Color hair = new Color(41, 40, 24);

    public static void main(String[] args) throws InterruptedException, IOException, TesseractException {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        tesseract = new Tesseract();
        tesseract.setDatapath("input/tesseract/tessdata");
        tesseract.setLanguage("vie");

        lock = new Object();

        String username = "Nezumi";
        int UID = 411;
        hwnd = User32.INSTANCE.FindWindow(null, "http://colongonline.com " + username + "[UID: " + UID + "] (Minh Nguyệt-Kênh 1)");

        BufferedImage image = captureWindow(737, 282, 50, 20);
        ImageIO.write(image, "png", new File("screenshot.png"));
        System.out.println(removeDiacritics(tesseract.doOCR(image)));
    }

    // 41 40 24
    // 3f - c
    // 56y - a
    // 7k - b
    // 4tg - a
    // 734 - d

    private static void removeAccountPoints() {
        Map<String, Integer> map = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("input/tesseract/points.ser"))) {
            map = (HashMap<String, Integer>) ois.readObject();
        } catch (Exception _) {

        }
        Set<String> removes = new HashSet<>();
        for (String s : map.keySet()) {
            if (s.equals("Zen") || s.equals("Mạc") || s.contains("•")) {
                removes.add(s);
            } else if (s.startsWith("XĐ")) {
                int num = 0;
                for (int i = 2; i < s.length(); i++) {
                    num = num * 10 + Character.getNumericValue(s.charAt(i));
                }
                if (num >= 31 && num <= 65) {
                    removes.add(s);
                }
            }
        }
        for (String s : removes) {
            map.remove(s);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("input/tesseract/points.ser"))) {
            oos.writeObject(map);
        } catch (Exception _) {

        }
    }

    private static void comments() {
        // get full screen
//        System.out.println(Arrays.toString(getMouseLocation(hwnd)));
//        System.out.println(getPixelColor(hwnd, 86, 226));
//        BufferedImage image = captureWindow(3, 26, 800, 600);
//        ImageIO.write(image, "png", new File("screenshot.png"));
    }
    private static int[] getCoordinates() throws TesseractException {
        BufferedImage image = captureWindow(653, 51, 125, 18);
        char[] coords = removeDiacritics(tesseract.doOCR(image)).toCharArray();
        System.out.println(tesseract.doOCR(image));
        int[] res = new int[2];
        int i = 0;
        for (; i < coords.length && coords[i] != 'y'; i++) {
            if (coords[i] >= '0' && coords[i] <= '9') {
                res[0] = res[0] * 10 + (coords[i] - '0');
            }
        }
        for (; i < coords.length; i++) {
            if (coords[i] >= '0' && coords[i] <= '9') {
                res[1] = res[1] * 10 + (coords[i] - '0');
            }
        }
        return res;
    }

    public static void click(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(hwnd, CoLongMulti.WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(300);
            User32.INSTANCE.SendMessage(hwnd, CoLongMulti.WinUser.WM_LBUTTONDOWN, new WPARAM(CoLongMulti.WinUser.MK_LBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(hwnd, CoLongMulti.WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    public static Color getPixelColor(HWND hwnd, int x, int y) {
        x -= 3;
        y -= 26;
        // Get the device context of the window
        HDC hdc = User32.INSTANCE.GetDC(hwnd);

        // Get the color of the specified pixel
        int pixelColor = CoLongMulti.MyGDI32.INSTANCE.GetPixel(hdc, x, y);
        User32.INSTANCE.ReleaseDC(hwnd, hdc); // Release the DC

        // Return the color as a Color object
        return new Color(pixelColor & 0xFF, (pixelColor >> 8) & 0xFF, (pixelColor >> 16) & 0xFF);
    }

    private static int[] getMouseLocation(HWND handle) throws InterruptedException {
        Thread.sleep(2000);
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(handle, r);
        Rectangle rect = r.toRectangle();
        rect.x = (int) Math.round(rect.x / scale);
        rect.y = (int) Math.round(rect.y / scale);
        Point m = MouseInfo.getPointerInfo().getLocation();
        return new int[]{m.x - rect.x, m.y - rect.y};
    }
    private static String getLocation() throws TesseractException {
        BufferedImage image = captureWindow(656, 32, 112, 15);
        return removeDiacritics(tesseract.doOCR(image));
    }
    public static String removeDiacritics(String text) {
        StringBuilder res = new StringBuilder();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        for (char c : normalized.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS) {
                continue;
            }
            if (c == 'đ' || c == 'Đ') {
                res.append('d');
            } else {
                res.append(Character.toLowerCase(c));
            }
        }
        return res.toString();
    }


    public static BufferedImage captureWindow(int x, int y, int width, int height) {
        x -= 3;
        y -= 26;
        HDC windowDC = User32.INSTANCE.GetDC(hwnd); // Get the window's device context (DC)
        HDC memDC = GDI32.INSTANCE.CreateCompatibleDC(windowDC); // Create a compatible DC in memory
        HBITMAP memBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(windowDC, width, height);
        GDI32.INSTANCE.SelectObject(memDC, memBitmap); // Select the bitmap into the memory DC

        // BitBlt to copy the window content to the memory DC
        GDI32.INSTANCE.BitBlt(memDC, 0, 0, width, height, windowDC, x, y, GDI32.SRCCOPY);

        // Get the bitmap info
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // Negative to indicate top-down drawing
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        // Allocate memory for pixel data
        Memory buffer = new Memory(width * height * 4); // 4 bytes per pixel (32-bit)

        // Retrieve the pixel data into the buffer
        GDI32.INSTANCE.GetDIBits(memDC, memBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        // Release resources
        GDI32.INSTANCE.DeleteObject(memBitmap);
        GDI32.INSTANCE.DeleteDC(memDC);
        User32.INSTANCE.ReleaseDC(hwnd, windowDC);

        // Convert the pixel data into a BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixelOffset = (row * width + col) * 4;
                int blue = buffer.getByte(pixelOffset) & 0xFF;
                int green = buffer.getByte(pixelOffset + 1) & 0xFF;
                int red = buffer.getByte(pixelOffset + 2) & 0xFF;

//                if (red == 240 && green == 248 && blue == 0) {
                    int rgb = (red << 16) | (green << 8) | blue;
                    image.setRGB(col, row, rgb);
//                }
                // 240 248 0




            }
        }
        return image;
    }
    public interface WinUser {
        int WM_LBUTTONDOWN = 0x0201; // Left mouse button down
        int WM_LBUTTONUP = 0x0202; // Left mouse button up
        int MK_LBUTTON = 0x0001; // Left button state
        int WM_RBUTTONDOWN = 0x0204; // Right mouse button down
        int WM_RBUTTONUP = 0x0205;
        int MK_RBUTTON = 0x0002;
        int WM_MOUSEMOVE = 0x0200;
        int WM_KEYDOWN = 0x0100;
        int WM_KEYUP = 0x0101;
        int WM_SYSKEYDOWN = 0x0104;
    }
}
