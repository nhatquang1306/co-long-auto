package Utilities;

import TextReaders.CoordinatesReader;
import TextReaders.LocationReader;
import TextReaders.NPCNameReader;
import TextReaders.PointsReader;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static com.sun.jna.platform.win32.WinUser.*;
public abstract class CoLongUtilities {
    public HWND handle;
    public boolean terminateFlag;
    public double scale;
    public String username;
    public CoordinatesReader cr;
    public PointsReader pr;
    public LocationReader lr;
    public NPCNameReader nnr;
    public final Object lock = new Object();
    public static final Color white = new Color(254, 254, 254);
    public static final Color dialogueBoxColor = new Color(20, 17, 0);
    public static final Color moveBar = new Color(81, 71, 34);
    public static final Color petMoveBar = new Color(49, 41, 15);
    public static BufferedImage vtPicture = null;

    public void initialize() {
        if (vtPicture == null) {
            try {
                vtPicture = ImageIO.read(new File("app/data/vt.png"));
            } catch (IOException _) {

            }
        }
        this.terminateFlag = false;
        this.cr = new CoordinatesReader(handle);
        this.pr = new PointsReader(handle);
        this.lr = new LocationReader(handle);
        this.nnr = new NPCNameReader(handle);
    }

    public boolean isAtLocation(int x, int y, String location) {
        int[] coords = getCoordinates();
        return coords[0] == x && coords[1] == y && getLocation().equals(location);
    }

    public boolean isAtLocation(int x, int y) {
        int[] coords = getCoordinates();
        return coords[0] == x && coords[1] == y;
    }

    public void useMap(Set<String> visited, int a, int b) throws InterruptedException {
        click(766, 183);
        if (!visited.contains("map")) {
            closeTutorial();
            visited.add("map");
        }
        click(a, b);
        click(766, 183);
    }

    public void closeTutorial() throws InterruptedException {
        if (hasDialogueBox()) {
            click(557, 266);
        }
    }

    public boolean waitForPetPrompt(int limit) throws InterruptedException {
        long start = System.currentTimeMillis();
        limit *= 1000;
        while (System.currentTimeMillis() - start < limit && !terminateFlag) {
            if (getPixelColor(746, 229).equals(petMoveBar)) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    public boolean waitForDialogueBox(int limit) throws InterruptedException {
        long start = System.currentTimeMillis();
        limit *= 1000;
        while (System.currentTimeMillis() - start < limit && !terminateFlag) {
            if (hasDialogueBox()) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    public boolean hasDialogueBox() {
        Color color1 = getPixelColor(216, 304);
        Color color2 = getPixelColor(588, 317);
        Color color3 = getPixelColor(419, 248);
        return color1.equals(Color.BLACK) && color2.equals(Color.BLACK) && color3.equals(dialogueBoxColor);
    }

    public boolean isInBattle() {
        Color color = getPixelColor(778, 38);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    public String getLocation() {
        String location = lr.read();
        return location == null ? "" : lr.read();
    }

    public int[] getCoordinates() {
        return cr.read();
    }

    public int savePoints() {
        synchronized (lock) {
            Map<String, Integer> map = new HashMap<>();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("app/data/points.ser"))) {
                map = (HashMap<String, Integer>) ois.readObject();
            } catch (Exception _) {

            }

            int points = Math.max(pr.read() - 10, 0);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app/data/points.ser"))) {
                map.put(username, points);
                oos.writeObject(map);
            } catch (Exception _) {

            }
            return points;
        }
    }

    public int findVT() {
        Memory buffer = pr.getBuffer(227, 280, 45, 71);
        int y = 0;
        outerLoop: for (int i = 0; i < 5 && !terminateFlag; i++) {
            if (i == 4) y = 37;
            for (int row = y; row < y + 14; row++) {
                for (int col = 0; col < 45; col++) {
                    if (vtPicture.getRGB(col, row - y) != -16711936) {
                        continue;
                    }
                    int pixelOffset = (row * 45 + col) * 4;
                    int blue = buffer.getByte(pixelOffset) & 0xFF;
                    int green = buffer.getByte(pixelOffset + 1) & 0xFF;
                    int red = buffer.getByte(pixelOffset + 2) & 0xFF;
                    int rgb = (0xFF << 24) | (red << 16) | (green << 8) | blue;
                    if (rgb != -16711936) {
                        y += 19;
                        continue outerLoop;
                    }
                }
            }
            return y;
        }
        return -1;
    }

    public Color getPixelColor(int x, int y) {
        x -= 3;
        y -= 26;
        // Get the device context of the window
        HDC hdc = User32.INSTANCE.GetDC(handle);

        // Get the color of the specified pixel
        int pixelColor = MyGDI32.INSTANCE.GetPixel(hdc, x, y);
        User32.INSTANCE.ReleaseDC(handle, hdc); // Release the DC

        // Return the color as a Color object
        return new Color(pixelColor & 0xFF, (pixelColor >> 8) & 0xFF, (pixelColor >> 16) & 0xFF);
    }

    public void setTerminateFlag() {
        terminateFlag = true;
    }

    public void mouseMove(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
        }
    }

    public void clickRandomLocation(int xStart, int xLength, int yStart, int yLength) throws InterruptedException {
        int x, y;
        Color first;
        do {
            x = xStart + (int)(Math.random() * (xLength + 1));
            y = yStart + (int)(Math.random() * (yLength + 1));
            first = getPixelColor(x, y);
            mouseMove(x, y);
        } while (!terminateFlag && !getPixelColor(x, y).equals(first));
        click(x, y);
    }

    public void click(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    public void clickOnNpc(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            Color color = getPixelColor(a, b);
            int count = 0;
            do {
                User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
                Thread.sleep(200);
            } while (!terminateFlag && count++ < 5 && getPixelColor(a, b).equals(color));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    public void rightClick(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONDOWN, new WPARAM(WinUser.MK_RBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    public void click(int[] arr) throws InterruptedException {
        click(arr[0], arr[1]);
    }

    public void clickOnNpc(int[] arr) throws InterruptedException {
        clickOnNpc(arr[0], arr[1]);
    }

    public void rightClick(int[] arr) throws InterruptedException {
        rightClick(arr[0], arr[1]);
    }

    public int[] getMouseLocation() throws InterruptedException {
        Thread.sleep(2000);
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(handle, r);
        Rectangle rect = r.toRectangle();
        rect.x = (int) Math.round(rect.x / scale);
        rect.y = (int) Math.round(rect.y / scale);
        Point m = MouseInfo.getPointerInfo().getLocation();
        return new int[]{m.x - rect.x, m.y - rect.y};
    }

    public interface MyGDI32 extends StdCallLibrary {
        MyGDI32 INSTANCE = Native.load("gdi32", MyGDI32.class);

        int GetPixel(HDC hdc, int nXPos, int nYPos);
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
    }

}
