import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.Normalizer;
import java.util.*;

import static com.sun.jna.platform.win32.WinUser.*;


public class CoLongMulti extends Thread {
    private final Tesseract tesseract;
    private final int questCount;
    private final Fight fight;
    private final int clanSkill;
    private final Clan clan;
    private final HWND handle;
    private boolean terminateFlag;
    private double scale;
    private final Object lock;
    private final int[] flag;
    private final JButton startButton;
    private final String username;
    private static final Color black = new Color(0, 0, 0);
    private static final Color white = new Color(254, 254, 254);
    private static final int[][] colorCoords = new int[][] {{1, 1}, {1, 4}, {2, 7}, {3, 1}, {5, 1}, {5, 8}};
    private static final Map<Integer, Integer> colorHashes = new HashMap<>();
    private static final int[] colorDistances = new int[] {6, 8, 8, 7, 7, 7, 7, 9, 6, 7};
    public CoLongMulti(int questCount, int skill, int newbie, int pet, int clanSkill, String clan, JButton startButton, HWND handle, String username) {
        if (colorHashes.isEmpty()) {
            int[] arr = new int[] {7, 9, 52, 16, 50, 1, 38, 4, 17, 19};
            for (int i = 0; i < 10; i++) {
                colorHashes.put(arr[i], i);
            }
        }

        this.handle = handle;
        this.username = username;
        this.questCount = questCount;
        this.fight = new Fight(skill, newbie, pet, this);
        this.clanSkill = clanSkill;
        if (clan.endsWith("ĐTK")) {
            this.clan = null;
            this.flag = new int[]{445, 417, clan.length() > 3 ? 0 : 1};
        } else {
            this.clan = new Clan(clan);
            this.flag = new int[3];
        }

        this.lock = new Object();
        this.terminateFlag = false;
        this.startButton = startButton;

        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("input/tesseract/tessdata");
        this.tesseract.setLanguage("vie");
    }

    public void run() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        try {
            Set<String> visited = new HashSet<>();
            visited.add("truong thanh tieu.");
            visited.add("thanh dan.");
            for (int j = 0; j < questCount; j++) {
                Queue<Dest> queue = new LinkedList<>();
                boolean closeInventory = false;
                if (j == 0 && getLocation().trim().equals("truong thanh tieu.")) {
                    int[] cur = getCoordinates();
                    int x = clan == null ? 18 : 24, y = clan == null ? 72 : 77;
                    if (cur[0] != x || cur[1] != y) {
                        goToTTTC(visited);
                        closeInventory = true;
                    }
                } else {
                    goToTTTC(visited);
                    closeInventory = true;
                }
                receiveQuest(queue, visited, closeInventory && clan == null, j == questCount - 1);
                traveling(queue, visited);
            }
            goToTTTC(visited);
        } catch (Exception _) {

        } finally {
            startButton.setEnabled(true);
        }
    }

    private void goToTTTC(Set<String> visited) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        if (clan == null) {
            goWithFlag();
        } else {
            goWithClan(visited);
        }
    }

    private void goWithFlag() throws InterruptedException, TesseractException {
        long start = -20000;
        do {
            Thread.sleep(500);
            if (System.currentTimeMillis() - start < 20000) {
                continue;
            }
            click(569, 586);
            rightClick(flag);
            waitForDialogueBox(20);
            if (flag[2] == 0) {
                click(348, 287); // click on toa do
                waitForDialogueBox(20);
                click(259, 286); // click take me there
            } else {
                click(321, 359);
            }
            start = System.currentTimeMillis();
        } while (!terminateFlag && !getLocation().contains("truong thanh"));
    }

    private void goWithClan(Set<String> visited) throws InterruptedException, TesseractException {
        String location = clan.getLocation();
        int[] info = clan.getInfo();
        int[] queue = clan.getQueue();
        String temp;
        do {
            rightClick(375 + clanSkill * 35, 548);
            Thread.sleep(4000);
            temp = getLocation().trim();
        } while (!terminateFlag && (!temp.contains(location) || !isAtLocation(info[0], info[1])));
        if (!visited.contains(temp)) {
            closeTutorial();
            visited.add(temp);
        }

        click(766, 183);
        if (!visited.contains("map")) {
            closeTutorial();
            visited.add("map");
        }
        click(info[2], info[3]);
        click(766, 183);

        while (!terminateFlag && !isAtLocation(queue[0], queue[1])) {
            Thread.sleep(500);
        }
        do {
            click(queue[2], queue[3]);
        } while (!terminateFlag && !waitForDialogueBox(20));
        click(256, 287);

        while (!terminateFlag && !getLocation().contains("kinh thanh")) {
            Thread.sleep(500);
        }
        if (!visited.contains("kinh thanh.")) {
            closeTutorial();
            visited.add("kinh thanh.");
        }

        click(766, 183);
        click(102, 421);
        click(766, 183);

        while (!terminateFlag && !getLocation().contains("truong thanh")){
            Thread.sleep(500);
        }
        Thread.sleep(500);
        click(171, 240);
        while (!terminateFlag && !isAtLocation(24, 77)) {
            Thread.sleep(1000);
        }
    }

    private void receiveQuest(Queue<Dest> queue, Set<String> visited, boolean closeInventory, boolean savePoints) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        if (closeInventory) click(569, 586);
        int x = clan == null ? 306 : 97, y = clan == null ? 145 : 126;
        do {
            click(x, y); // click on NPC
        } while (!terminateFlag && !waitForDialogueBox(20));
        click(272, 305); // click on van tieu ca nhan
        waitForDialogueBox(20);
        if (savePoints) savePoints();
        click(285, 344); // click on cap 2
        waitForDialogueBox(20);
        BufferedImage image = captureWindow(224, 257, 355, 40);
        click(557, 266);
        parseDestination(queue, tesseract.doOCR(image), visited);
    }

    private void parseDestination(Queue<Dest> queue, String destination, Set<String> visited) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        if (SwitchStatements.parseDestination(destination, queue)) {
            getOut(visited);
        }
        startMovement(queue, visited);
    }

    private void traveling(Queue<Dest> queue, Set<String> visited) throws InterruptedException, TesseractException {
        String location = "truong thanh tieu.";
        long stillCount = System.currentTimeMillis();
        long finalTime = -1;
        while (!terminateFlag) {
            if (isInBattle()) {
                progressMatch();
                long time = System.currentTimeMillis();
                if (finalTime > -1) {
                    finalTime = time;
                }
                stillCount = time;
            } else if (location.contains(queue.peek().dest)) {
                long time = System.currentTimeMillis();
                if (isAtLocation(queue.peek().x, queue.peek().y)) {
                    Thread.sleep(1000);
                    if (!isInBattle() && arrived(queue, visited)) return;
                } else if (queue.peek().methodId == 0) {
                    if (finalTime == -1) {
                        finalTime = time;
                    } else if (finalTime == -2) {
                        Thread.sleep(1000);
                        if (!isInBattle() && finishQuest()) return;
                    } else if (time - finalTime >= 30000) {
                        click(651, 268);
                        finalTime = -2;
                    }
                }
                stillCount = time;
            } else if (!getLocation().trim().equals(location)) {
                location = getLocation().trim();
                if (!visited.contains(location)) {
                    closeTutorial();
                    visited.add(location);
                }
                if (isAtLocation(queue.peek().x, queue.peek().y)) {
                    Thread.sleep(1000);
                    if (!isInBattle() && arrived(queue, visited)) return;
                }
                stillCount = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - stillCount >= 50000) {
                if (queue.peek().methodId == -1) {
                    click(766, 183);
                    click(queue.peek().mapX, queue.peek().mapY);
                    click(766, 183);
                } else if (queue.peek().methodId == 0) {
                    handleIdling(visited, location, queue.peek().x);
                }
                stillCount = System.currentTimeMillis();
            }
            Thread.sleep(200);
        }
    }

    private void startMovement(Queue<Dest> queue, Set<String> visited) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        Dest dest = queue.peek();
        switch (dest.methodId) {
            case -1:
                click(766, 183);
                if (!visited.contains("map")) {
                    closeTutorial();
                    visited.add("map");
                }
                click(dest.mapX, dest.mapY);
                click(766, 183);
                break;
            case 0:
                click(651, 268);
                break;
            case 2:
                goToTVD();
                dest.methodId = -1;
                break;
            case 3:
                goToHTT();
                break;
            default:
                break;
        }
    }

    private void handleIdling(Set<String> visited, String location, int x) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        // special map case because you can't open medium map
        if (location.equals("thanh y lau-tang.")) {
            click(383, 350);
            Thread.sleep(1000);
            click(651, 268);
        } else {
            click(766, 183);
            if (!visited.contains("map")) {
                closeTutorial();
                visited.add("map");
            }
            click(SwitchStatements.handleIdling(location, x));
            click(749, 268);
        }
    }

    private void progressMatch() throws InterruptedException, TesseractException {
        int turn = 0;
        while (!terminateFlag && isInBattle()) {
            // wait until the turn is started
            while (!terminateFlag && !getPixelColor(378, 90).equals(white) || getPixelColor(405, 325).equals(white)) {
                if (!isInBattle()) return;
                Thread.sleep(200);
            }
            if ((turn == 1 || turn == 2) && waitForDialogueBox(2)) {
                click(557, 266);
                waitForDefensePrompt(1, 10);
            } else {
                // move mouse out the way
                mouseMove(270, 566);
            }
            Color color = getPixelColor(231, 201);
            fight.execute(color, turn);
            // wait until the current turn is over
            while (!terminateFlag && getPixelColor(378, 90).equals(white) || getPixelColor(405, 325).equals(white)) {
                Thread.sleep(200);
            }
            turn++;
        }
    }

    public boolean waitForDefensePrompt(int person, int limit) throws TesseractException, InterruptedException {
        long start = System.currentTimeMillis();
        limit *= 1000;
        while (System.currentTimeMillis() - start < limit && !terminateFlag) {
            BufferedImage image = captureWindow(737, person == 1 ? 282 : 239, 50, 20);
            String str = removeDiacritics(tesseract.doOCR(image));
            if (str.contains("thu")) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    private boolean arrived(Queue<Dest> queue, Set<String> visited) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return true;
        }
        if (queue.peek().methodId == 0) {
            while (!terminateFlag && !finishQuest()) {
                Thread.sleep(1000);
                fixFinishQuest();
            }
            return true;
        } else {
            queue.poll();
            startMovement(queue, visited);
            return false;
        }
    }

    private void fixFinishQuest() throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        int[] cur = getCoordinates();
        click(SwitchStatements.fixFinishQuest(cur[0], cur[1]));
    }

    private boolean finishQuest() throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return true;
        }
        int[] arr = new int[]{296, 314, 278, 332};
        for (int i = 0; i < 4 && !terminateFlag; i++) {
            BufferedImage image = captureWindow(223, arr[i], 70, 20);
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, arr[i] + 10);
                waitForDialogueBox(20);
                click(557, 266); // click on final text box;
                return true;
            }
        }
        return false;
    }

    private boolean isAtLocation(int x, int y) throws TesseractException {
        int[] coords = getCoordinates();
        return coords[0] == x && coords[1] == y;
    }

    private void getOut(Set<String> visited) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        if (clan != null) {
            click(752, 512);
        } else {
            click(730, 443);
            Thread.sleep(2000);
            click(651, 432);
        }

        while (!getLocation().contains("kinh thanh") && !terminateFlag) {
            Thread.sleep(500);
        }
        if (!visited.contains("kinh thanh.")) {
            closeTutorial();
            visited.add("kinh thanh.");
        }
    }

    private void goToTVD() throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        do {
            click(131, 229);
        } while (!terminateFlag && !waitForDialogueBox(20));

        click(323, 456);
        while (!getLocation().contains("danh nhan") && !terminateFlag) {
            Thread.sleep(200);
        }
        click(787, 480);
    }

    private void goToHTT() throws InterruptedException {
        if (terminateFlag) {
            return;
        }
        Thread.sleep(1000);
        do {
            click(557, 287);
        } while (!terminateFlag && !waitForDialogueBox(20));
        click(259, 286);
    }

    private boolean isInBattle() {
        Color color = getPixelColor(778, 38);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private String getLocation() throws TesseractException {
        BufferedImage image = captureWindow(656, 32, 112, 15);
        return removeDiacritics(tesseract.doOCR(image));
    }

    private int[] getCoordinates() throws TesseractException {
        BufferedImage image = captureWindow(653, 51, 125, 18);
        char[] coords = removeDiacritics(tesseract.doOCR(image)).toCharArray();
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

    private void savePoints() {
        synchronized (lock) {
            Map<String, Integer> map = new HashMap<>();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("input/tesseract/points.ser"))) {
                map = (HashMap<String, Integer>) ois.readObject();
            } catch (Exception _) {

            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("input/tesseract/points.ser"))) {
                map.put(username, getPoints() - 10);
                oos.writeObject(map);
            } catch (Exception _) {

            }
        }
    }

    private int getPoints() {
        BufferedImage image = captureWindow(296, 309, 30, 9);
        int x = 0, res = 0;
        int hash = getHash(image, x);
        while (colorHashes.containsKey(hash)) {
            int num = colorHashes.get(hash);
            res = res * 10 + num;
            x += colorDistances[num];
            if (x > 24) break;
            hash = getHash(image, x);
        }
        return res;
    }

    private int getHash(BufferedImage image, int x) {
        int res = 0;
        for (int i = 0; i < 6; i++) {
            if (image.getRGB(x + colorCoords[i][0], colorCoords[i][1]) == -13043656) {
                res |= 1 << i;
            }
        }
        return res;
    }

    public BufferedImage captureWindow(int x, int y, int width, int height) {
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

    private void closeTutorial() throws InterruptedException {
        if (hasDialogueBox()) {
            click(557, 266);
        }
    }

    private boolean waitForDialogueBox(int limit) throws InterruptedException {
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

    private boolean hasDialogueBox() {
        Color color1 = getPixelColor(216, 304);
        Color color2 = getPixelColor(588, 317);
        return color1.equals(black) && color2.equals(black);
    }

    public String removeDiacritics(String text) {
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

    public void click(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(300);
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
            Thread.sleep(300);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONDOWN, new WPARAM(WinUser.MK_RBUTTON), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(300);
        }
    }

    private void click(int[] arr) throws InterruptedException {
        click(arr[0], arr[1]);
    }

    private void rightClick(int[] arr) throws InterruptedException {
        rightClick(arr[0], arr[1]);
    }

    private int[] getMouseLocation() throws InterruptedException {
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