package Main;

import TextReaders.LocationReader;
import TextReaders.NPCNameReader;
import Utilities.CoLongUtilities;
import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;



import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

import static com.sun.jna.platform.win32.WinUser.*;

public class test {
    private static double scale;
    private static HWND handle;
    private static Object lock;

    public static void main(String[] args) throws InterruptedException, IOException {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        lock = new Object();

        String username = "Baaaa";
        int UID = 551;
        handle = User32.INSTANCE.FindWindow(null, "http://colongonline.com " + username + "[UID: " + UID + "] (Minh Nguyệt-Kênh 1)");
        int[][] vtPoints = new int[][] {
                {4, 8}, {32, 6}, {25, 8}, {34, 4}, {4, 7},
                {9, 7}, {9, 4}, {26, 4}, {25, 9}, {17, 4}
        };
        BufferedImage image = ImageIO.read(new File("screenshot.png"));
        int[][] temp = new int[][] {};
        BufferedImage cut = image.getSubimage(0, 2, 10, 12);
        ImageIO.write(cut, "png", new File("screenshot.png"));
    }

    public static BufferedImage captureWindow(int x, int y, int width, int height) {
        x -= 3;
        y -= 26;
        HDC windowDC = User32.INSTANCE.GetDC(handle); // Get the window's device context (DC)
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
        User32.INSTANCE.ReleaseDC(handle, windowDC);

        // Convert the pixel data into a BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixelOffset = (row * width + col) * 4;
                int blue = buffer.getByte(pixelOffset) & 0xFF;
                int green = buffer.getByte(pixelOffset + 1) & 0xFF;
                int red = buffer.getByte(pixelOffset + 2) & 0xFF;
                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(col, row, rgb);
            }
        }
        return image;
    }

    public static void mouseMove(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, CoLongUtilities.WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
        }
    }

    public static void click(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, CoLongUtilities.WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
            User32.INSTANCE.SendMessage(handle, CoLongUtilities.WinUser.WM_LBUTTONDOWN, new WPARAM(CoLongUtilities.WinUser.MK_LBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, CoLongUtilities.WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    private static void solveCaptcha() throws IOException {
        handle = User32.INSTANCE.FindWindow(null, "http://colongonline.com (Minh Nguyệt)");
        BufferedImage image = captureWindow(145, 225, 140, 90);
        ImageIO.write(image, "png", new File("screenshot.png"));
        // 3f - c
        // 56y - a
        // 7k - b
        // 4tg - a
        // 734 - d
        // afo - c
        // sl - d
        // #7b - d
        // q1p - c
    }


    private static void removeAccountPoints() {
        Map<String, Integer> map = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("app/data/points.ser"))) {
            map = (HashMap<String, Integer>) ois.readObject();
        } catch (Exception _) {

        }
        String[] removes = new String[]{"Chơi", "iKon"};
        for (String r : removes) {
            map.remove(r);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app/data/points.ser"))) {
            oos.writeObject(map);
        } catch (Exception _) {

        }
    }

    private static void getFullScreen() throws IOException, InterruptedException {
        // get full screen
        System.out.println(Arrays.toString(getMouseLocation()));
        System.out.println(getPixelColor(86, 226));
        BufferedImage image = captureWindow(3, 26, 800, 600);
        ImageIO.write(image, "png", new File("screenshot.png"));
    }


    public static Color getPixelColor(int x, int y) {
        x -= 3;
        y -= 26;
        // Get the device context of the window
        HDC hdc = User32.INSTANCE.GetDC(handle);

        // Get the color of the specified pixel
        int pixelColor = CoLongUtilities.MyGDI32.INSTANCE.GetPixel(hdc, x, y);
        User32.INSTANCE.ReleaseDC(handle, hdc); // Release the DC

        // Return the color as a Color object
        return new Color(pixelColor & 0xFF, (pixelColor >> 8) & 0xFF, (pixelColor >> 16) & 0xFF);
    }

    private static int[] getMouseLocation() throws InterruptedException {
        Thread.sleep(2000);
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(handle, r);
        Rectangle rect = r.toRectangle();
        rect.x = (int) Math.round(rect.x / scale);
        rect.y = (int) Math.round(rect.y / scale);
        Point m = MouseInfo.getPointerInfo().getLocation();
        return new int[]{m.x - rect.x, m.y - rect.y};
    }

    private static void click(int[] arr) throws InterruptedException {
        click(arr[0], arr[1]);
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
