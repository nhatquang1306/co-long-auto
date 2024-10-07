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
    private final int skill;
    private final int newbie;
    private final int pet;
    private final HWND handle;
    private boolean terminateFlag;
    private double scale;
    private final Object lock;
    private final int[] flag;
    private final JButton startButton;
    private final String username;
    private static final Color everyColor1 = new Color(239, 239, 15);
    private static final Color everyColor2 = new Color(239, 207, 15);
    private static final Color characterColor1 = new Color(175, 143, 175);
    private static final Color characterColor2 = new Color(206, 146, 207);
    private static final Color newbieColor = new Color(143, 175, 111);
    private static final Color petColor = new Color(111, 207, 215);
    private static final Color black = new Color(0, 0, 0);

    public CoLongMulti(int questCount, int skill, int newbie, int pet, boolean flag, JButton startButton, HWND handle, String username) throws Exception {
        this.handle = handle;
        this.username = username;
        this.questCount = questCount;
        this.skill = skill;
        this.newbie = newbie;
        this.pet = pet;
        this.flag = new int[]{445, 417, flag ? 0 : 1};

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
                boolean closeInventory = true;
                if (j == 0 && getLocation().trim().equals("truong thanh tieu.")) {
                    int[] cur = getCoordinates();
                    if (cur[0] != 18 || cur[1] != 72) {
                        goToTTTC();
                    } else {
                        closeInventory = false;
                    }
                } else {
                    goToTTTC();
                }
                receiveQuest(queue, visited, closeInventory, j == questCount - 1);
                traveling(queue, "truong thanh tieu.", visited);
            }
        } catch (Exception _) {

        } finally {
            startButton.setEnabled(true);
        }
    }

    private void goToTTTC() throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586);
        rightClick(flag); // right click on flag
        if (flag[2] == 0) {
            waitForPrompt(224, 278, 180, 20, "toa do 1");
            click(348, 287); // click on toa do
            waitForPrompt(224, 278, 120, 20, "dua ta toi do");
            click(259, 286); // click take me there
        } else {
            waitForPrompt(223, 351, 80, 20, "bach ly");
            click(321, 359);
        }
        while (!getLocation().contains("truong thanh") && !terminateFlag) {
            Thread.sleep(200);
        }
    }

    private void receiveQuest(Queue<Dest> queue, Set<String> visited, boolean closeInventory, boolean savePoints) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        if (closeInventory) {
            click(569, 586);
        }
        click(306, 145); // click on NPC
        waitForPrompt(223, 295, 120, 20, "van tieu");
        click(272, 305); // click on van tieu ca nhan
        waitForPrompt(223, 335, 180, 20, "cap 2");
        if (savePoints) {
            savePoints();
        }
        click(285, 344); // click on cap 2
        waitForDialogueBox(50);
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

    private void traveling(Queue<Dest> queue, String location, Set<String> visited) throws InterruptedException, TesseractException {
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
                int[] cur = getCoordinates();
                long time = System.currentTimeMillis();
                boolean isInBattle = isInBattle();
                if (cur[0] == queue.peek().x && cur[1] == queue.peek().y && !isInBattle) {
                    if (arrived(queue, visited)) return;
                } else if (queue.peek().methodId == 0 && !isInBattle) {
                    if (finalTime == -1) {
                        finalTime = time;
                    } else if (finalTime == -2) {
                        if (finishQuest()) return;
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
                int[] cur = getCoordinates();
                if (cur[0] == queue.peek().x && cur[1] == queue.peek().y && !isInBattle()) {
                    if (arrived(queue, visited)) return;
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
        // gi cung so: 239 239 15 / 239 207 15
        // tro thu so ta: 175 143 175 / 206 146 207
        // ta so tan thu: 143 175 111
        // ta so tro thu: 111 207 215
        int turn = 0;
        while (!terminateFlag && isInBattle()) {
            // wait until timer shows up
            while (!terminateFlag && (!isWhite(378, 90) || isWhite(405, 325))) {
                if (!isInBattle()) {
                    return;
                }
                Thread.sleep(200);
            }
            if (turn == 1 || turn == 2) {
                waitForDialogueBox(5);
                click(557, 266);
            } else {
                // move mouse out the way
                synchronized (lock) {
                    long x = Math.round(267 * scale);
                    long y = Math.round(540 * scale);
                    LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
                    User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
                    Thread.sleep(200);
                }
            }
            Color color = getPixelColor(231, 201);
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
            if (color.equals(everyColor1) || color.equals(everyColor2)) {
                characterAttack();
                waitForDefensePrompt();
                petAttack();
            } else if (color.equals(newbieColor)) {
                newbieAttack();
                waitForDefensePrompt();
                petDefense();
            } else if (color.equals(petColor)) {
                defense();
                waitForDefensePrompt();
                if (turn == 0) {
                    petAttack();
                } else {
                    click(222, 167);
                }
            } else if (color.equals(characterColor1) || color.equals(characterColor2)) {
                if (turn == 0) {
                    characterAttack();
                } else {
                    click(222, 167);
                }
                waitForDefensePrompt();
                petDefense();
            } else if (r >= 154 && r <= 178 && g >= 191 && g <= 228 && b >= 85 && b <= 121) {
                defense();
                waitForDefensePrompt();
                click(222, 167);
            } else {
                defense();
                waitForDefensePrompt();
                petDefense();
            }
            // wait until turn is over
            while (!terminateFlag && (isWhite(378, 90) || isWhite(405, 325))) {
                Thread.sleep(200);
            }
            turn++;
        }
    }

    private boolean arrived(Queue<Dest> queue, Set<String> visited) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return true;
        }
        if (queue.peek().methodId == 0) {
            Thread.sleep(1000);
            while (!terminateFlag && !finishQuest()) {
                if (isInBattle()) {
                    return false;
                }
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
                waitForDialogueBox(50);
                click(557, 266); // click on final text box;
                return true;
            }
        }
        return false;
    }

    private void getOut(Set<String> visited) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(730, 443);
        Thread.sleep(2000);
        click(651, 432);
        while (!getLocation().contains("kinh thanh") && !terminateFlag) {
            Thread.sleep(200);
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
        Thread.sleep(1000);
        click(131, 229);
        waitForPrompt(224, 257, 100, 20, "binh khi");
        click(323, 456);
        while (!getLocation().contains("danh nhan") && !terminateFlag) {
            Thread.sleep(200);
        }
        click(787, 480);
    }

    private void goToHTT() throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        Thread.sleep(1000);
        click(557, 287);
        waitForDialogueBox(50);
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

    private void characterAttack() throws InterruptedException {
        if (skill != 0) {
            click(375 + skill * 35, 548);
        }
        Thread.sleep(200);
        click(222, 167);
    }

    private void newbieAttack() throws InterruptedException {
        click(375 + newbie * 35, 548);
        Thread.sleep(200);
        click(222, 167);
    }

    private void petAttack() throws InterruptedException {
        if (pet != 0) {
            click(759, 209);
            click(254 + pet * 37, 290);
        }
        Thread.sleep(200);
        click(222, 167);
    }

    private void defense() throws InterruptedException {
        click(760, 292);
    }

    private void petDefense() throws InterruptedException {
        click(760, 246);
    }

    private void savePoints() {
        synchronized (lock) {
            Map<String, Integer> map = new HashMap<>();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("input/tesseract/points.ser"))) {
                map = (HashMap<String, Integer>) ois.readObject();
            } catch (Exception _) {

            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("input/tesseract/points.ser"))) {
                BufferedImage image = captureWindow(295, 307, 50, 18);
                int points = 0;
                for (char c : tesseract.doOCR(image).toCharArray()) {
                    if (Character.isDigit(c)) {
                        points = points * 10 + Character.getNumericValue(c);
                    }
                }
                map.put(username, points - 10);
                oos.writeObject(map);
            } catch (Exception _) {

            }
        }
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

    private boolean isWhite(int x, int y) {
        Color color = getPixelColor(x, y);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        return r == 254 && g == 254 && b == 254;
    }

    private void closeTutorial() throws InterruptedException {
        if (hasDialogueBox()) {
            click(557, 266);
        }
    }

    private boolean waitForPrompt(int x, int y, int width, int height, String target) throws TesseractException, InterruptedException {
        int timer = 0;
        while (timer++ < 50 && !terminateFlag && !isInBattle()) {
            BufferedImage image = captureWindow(x, y, width, height);
            String str = removeDiacritics(tesseract.doOCR(image));
            if (str.contains(target)) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    private void waitForDefensePrompt() throws TesseractException, InterruptedException {
        int timer = 0;
        while (timer++ < 50 && !terminateFlag) {
            BufferedImage image = captureWindow(737, 239, 50, 20);
            String str = removeDiacritics(tesseract.doOCR(image));
            if (str.contains("thu")) {
                return;
            }
            Thread.sleep(200);
        }
    }

    private boolean waitForDialogueBox(int limit) throws InterruptedException {
        Thread.sleep(400);
        int timer = 0;
        while (timer++ < limit && !terminateFlag) {
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

    public void click(int a, int b) throws InterruptedException {
        synchronized (lock) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(300);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(400);
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
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(400);
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