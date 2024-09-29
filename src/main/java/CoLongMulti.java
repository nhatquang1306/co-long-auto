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
import java.text.Normalizer;
import java.util.*;

import static com.sun.jna.platform.win32.WinUser.*;


public class CoLongMulti extends Thread {
    private final Tesseract tesseract;
    private final int questCount;
    private int skill;
    private final int newbie;
    private final int pet;
    private final HWND handle;
    private boolean terminateFlag;
    private double scale;
    private final Object lock;
    private final int[] flag;
    private final JButton startButton;
    private static final Color everyColor1 = new Color(239, 239, 15);
    private static final Color everyColor2 = new Color(239, 207, 15);
    private static final Color characterColor1 = new Color(175, 143, 175);
    private static final Color characterColor2 = new Color(206, 146, 207);
    private static final Color newbieColor = new Color(143, 175, 111);
    private static final Color petColor = new Color(111, 207, 215);

    // note
    // attack enemy next turn
    // check for quest when first booting up

    public CoLongMulti(int UID, int questCount, int skill, int newbie, int pet, boolean flag, JButton startButton, Map<Integer, HWND> handleMap) throws Exception {
        handle = handleMap.get(UID);
        if (handle == null) {
            throw new Exception("Không có UID " + UID + ".");
        }

        this.questCount = questCount;
        this.skill = skill;
        this.newbie = newbie;
        this.pet = pet;
        this.flag = new int[]{445, 417, flag ? 0 : 1};

        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("input/tesseract/tessdata");
        this.tesseract.setLanguage("vie");

        this.lock = new Object();
        this.terminateFlag = false;
        this.startButton = startButton;
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
                goToTTTC();
                receiveQuest(queue, visited);
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

    private void receiveQuest(Queue<Dest> queue, Set<String> visited) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586);
        click(306, 145); // click on NPC
        waitForPrompt(223, 295, 120, 20, "van tieu");
        click(272, 305); // click on van tieu ca nhan
        waitForPrompt(223, 335, 180, 20, "cap 2");
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
        int index = destination.indexOf("do") + 3;
        switch (destination.charAt(index)) {
            case 'C':
                char c3 = destination.charAt(index + 1);
                if (c3 == 'u') { // cung to to
                    getOut();
                    queue.offer(new Dest(472, 227, new int[][] {{173, 164}}, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(3));
                    queue.offer(new Dest(new int[][] {{57, 48}}, "hoang thach"));
                } else { // chuong chan seu
                    queue.offer(new Dest(new int[][] {{37, 145}}, "long mon"));
                }
                break;
            case 'L': // ly than dong
                getOut();
                queue.offer(new Dest(472, 227, new int[][] {{173, 164}}, "kinh thanh"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, new int[][] {{10, 307}}, "luc thuy"));
                queue.offer(new Dest(new int[][] {{30, 199}}, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                if (c2 == 'm') { // tram lang
                    getOut();
                    queue.offer(new Dest(472, 227, new int[][] {{173, 164}}, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(new int[][] {{74, 86}}, "vo danh"));
                } else if (c2 == 't') { // tiet dai han
                    getOut();
                    queue.offer(new Dest(102, 497, new int[][] {{161, 49}}, "dieu phong"));
                    queue.offer(new Dest(new int[][] {{51, 161}}, "hao han"));
                } else if (c2 == 'n') { // trinh trung
                    getOut();
                    queue.offer(new Dest(688, 199, new int[][] {{18, 254}}, "kinh thanh dong"));
                    queue.offer(new Dest(new int[][] {{38, 79}}, "dien vo"));
                } else { // thiet dien phan quan
                    getOut();
                    queue.offer(new Dest(688, 199, new int[][] {{18, 254}}, "kinh thanh dong"));
                    queue.offer(new Dest(new int[][] {{32, 57}, {33, 58}}, "tang kiem"));
                }
                break;
            case 'M': // ma khong quan
                char c = destination.charAt(index + 3);
                if (c == 'K') {
                    getOut();
                    queue.offer(new Dest(102, 497, new int[][] {{161, 49}}, "dieu phong"));
                    queue.offer(new Dest(new int[][] {{18, 60}}, "quan dong"));
                } else { // ma quan lao thai ba
                    queue.offer(new Dest(new int[][] {{22, 110}, {23, 90}}, "ky dao"));
                }
                break;
            case 'Đ': // duong thu thanh duong mon
                getOut();
                queue.offer(new Dest(688, 199, new int[][] {{18, 254}}, "kinh thanh dong"));
                queue.offer(new Dest(new int[][] {{14, 71}}, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                getOut();
                queue.offer(new Dest(688, 199, new int[][] {{18, 254}}, "kinh thanh dong"));
                queue.offer(new Dest(new int[][] {{29, 70}}, "quy"));
                break;
            case 'S': // so luu huong
                queue.offer(new Dest(new int[][] {{26, 57}}, "luu huong"));
                break;
            case 'H': // han thuan + hac sinh y
                char c4 = destination.charAt(index + 2);
                if (c4 == 'n') {
                    queue.offer(new Dest(new int[][] {{29, 84}}, "binh khi"));
                } else {
                    queue.offer(new Dest(new int[][] {{10, 73}}, "thai binh"));
                }
                break;
            case 'K': // kim phung hoang
                queue.offer(new Dest(new int[][] {{20, 6}}, "kim ly"));
                break;
        }
        startMovement(queue, visited);
    }

    private void traveling(Queue<Dest> queue, String location, Set<String> visited) throws InterruptedException, TesseractException {
        long stillCount = System.currentTimeMillis();
        while (!terminateFlag) {
            if (isInBattle()) {
                progressMatch();
                stillCount = System.currentTimeMillis();
            } else if (location.contains(queue.peek().dest)) {
                if (isAtFinalLocation(queue.peek().coords) && !isInBattle()) {
                    if (arrived(queue, visited)) return;
                }
                stillCount = System.currentTimeMillis();
            } else if (!getLocation().equals(location)) {
                location = getLocation();
                if (!visited.contains(location)) {
                    closeTutorial();
                    visited.add(location);
                }
                if (isAtFinalLocation(queue.peek().coords) && !isInBattle()) {
                    if (arrived(queue, visited)) return;
                }
                stillCount = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - stillCount >= 50000) {
                location = handleIdling(location, visited, queue);
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

    private String handleIdling(String location, Set<String> visited, Queue<Dest> queue) throws InterruptedException, TesseractException {
        click(774, 115);
        Thread.sleep(1000);
        if (!visited.contains("channel")) {
            closeTutorial();
            visited.add("channel");
        }
        if (!getLocation().equals(location)) {
            location = getLocation();
            if (!visited.contains(location)) {
                closeTutorial();
                visited.add(location);
            }
            click(481, 477);
            if (queue.peek().methodId == 0) {
                startMovement(queue, visited);
            }
        } else if (queue.peek().methodId <= 0) {
            click(481, 477);
            startMovement(queue, visited);
        }
        return location;
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
                waitForDialogueBox(7);
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
    private boolean isAtFinalLocation(int[][] target) throws TesseractException {
        int[] cur = getCoordinates();
        for (int[] arr : target) {
            if (arr[0] == cur[0] && arr[1] == cur[1]) {
                return true;
            }
        }
        return false;
    }

    private boolean arrived(Queue<Dest> queue, Set<String> visited) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return true;
        }
        if (queue.peek().methodId == 0) {
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
        int x = cur[0];
        int y = cur[1];
        switch (x) {
            case 10: // hac sinh y 10 73
                click(399, 212);
                break;
            case 14: // duong thu thanh 14 71
                click(505, 182);
                break;
            case 18: // ma khong quan 18 60
                click(286, 198);
                break;
            case 20: // kim phung hoang 20 65
                click(239, 230);
                break;
            case 22: // ma quan lao thai ba 22 110
            case 23: // ma quan lao thai ba 23 90
                click(537, 401);
                break;
            case 26: // so luu huong 26 57
                click(145, 276);
                break;
            case 29: // ngoc linh lung 29 70, han thuan 29 84
                if (y == 70) {
                    click(400, 131);
                } else {
                    click(286, 146);
                }
                break;
            case 30: // ly than dong 30 199
                click(683, 308);
                break;
            case 32: // thiet dien phan qua 32 57
            case 33: // thiet dien phan qua 33 58
                click(539, 177);
                break;
            case 37: // chuong chan seu 37 145
                click(549, 228);
                break;
            case 38: // trinh trung 38 79
                click(399, 125);
                break;
            case 51: // tiet dai han 51 161
                click(128, 284);
                break;
            case 57: // cung to to 57 48
                click(682, 260);
                break;
            case 74: // tram lang 74 86
                click(250, 201);
        }
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

    private void getOut() throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(730, 443);
        Thread.sleep(2000);
        click(651, 432);
        while (!getLocation().contains("kinh thanh") && !terminateFlag) {
            Thread.sleep(200);
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
        waitForPrompt(223, 278, 150, 20, "hoang thach");
        click(259, 286);
    }

    private void closeTutorial() throws InterruptedException {
        if (terminateFlag) {
            return;
        }
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

    private boolean waitForDialogueBox(int limit) throws InterruptedException {
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
        Color color = getPixelColor(216, 304);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        return r == 0 && g == 0 && b == 0;
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
            Thread.sleep(200);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(500);
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
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(500);
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