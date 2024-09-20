import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.w3c.dom.css.Rect;

import javax.imageio.ImageIO;

import static com.sun.jna.platform.win32.WinUser.*;


public class CoLongMulti {
    private final Tesseract[] tesseracts;
    private final Tesseract[] numberTesseracts;
    private final int[] accounts;
    private final int[] questCount;
    private final int[] skills;
    private final int[] newbies;
    private final int[] pets;
    private HWND[] handles;
    private boolean terminateFlag;
    private double scale;
    private Object[] locks;

    // note
    // attack enemy next turn
    // check for quest when first booting up

    public CoLongMulti(List<Integer> UIDs, List<Integer> questCounts, List<Integer> skillButtons, List<Integer> newbieButtons, List<Integer> petButtons) throws Exception {
        int n = UIDs.size();
        User32 user32 = User32.INSTANCE;
        Map<Integer, HWND> handleMap = getAllWindows(user32);

        accounts = new int[n];
        questCount = new int[n];
        skills = new int[n];
        newbies = new int[n];
        pets = new int[n];
        handles = new HWND[n];


        for (int i = 0; i < n; i++) {
            if (!handleMap.containsKey(UIDs.get(i))) {
                throw new Exception("Không có UID " + UIDs.get(i) + ".");
            }
            handles[i] = handleMap.get(UIDs.get(i));
            accounts[i] = UIDs.get(i);
            questCount[i] = questCounts.get(i);
            skills[i] = skillButtons.get(i);
            newbies[i] = newbieButtons.get(i);
            pets[i] = petButtons.get(i);
        }

        tesseracts = new Tesseract[n];
        numberTesseracts = new Tesseract[n];
        locks = new Object[n];

        for (int i = 0; i < n; i++) {
            tesseracts[i] = new Tesseract();
            tesseracts[i].setDatapath("app/tesseract/tessdata");
            tesseracts[i].setLanguage("vie");

            numberTesseracts[i] = new Tesseract();
            numberTesseracts[i].setDatapath("app/tesseract/tessdata");
            numberTesseracts[i].setLanguage("eng");
            numberTesseracts[i].setTessVariable("tessedit_char_whitelist", "0123456789");

            locks[i] = new Object();
        }
        terminateFlag = false;
    }
    public void setTerminateFlag() {
        terminateFlag = true;
    }

    public void run() throws NativeHookException, TesseractException, InterruptedException {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        int n = accounts.length;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            int k = i;
            threads[i] = new Thread(() -> {
                try {
                    setUpQuest(handles[k], k);
                    Set<String> visited = new HashSet<>();
                    visited.add("truong thanh tieu.");
                    for (int j = 0; j < questCount[k]; j++) {
                        Queue<Dest> queue = new LinkedList<>();
                        goToTTTC(handles[k], k);
                        receiveQuest(handles[k], k);
                        while (!getLocation(handles[k], k).contains("truong thanh") && !terminateFlag) {
                            Thread.sleep(200);
                        }
                        parseDestination(queue, handles[k], k);
                        traveling(queue, "truong thanh tieu.", visited, handles[k], k);
                    }
                } catch (Exception _) {

                }
            });
            threads[i].start();
        }
    }

    public static BufferedImage captureWindow(HWND hwnd, int x, int y, int width, int height) {
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
                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(col, row, rgb);
            }
        }
        return image;
    }


    private void traveling(Queue<Dest> queue, String location, Set<String> visited, HWND handle, int k) throws InterruptedException, TesseractException {
        long stillCount = System.currentTimeMillis();
        while (!terminateFlag) {
            if (isInBattle(handle)) {
                progressMatch(handle, k);
                stillCount = System.currentTimeMillis();
            } else if (location.contains(queue.peek().dest)) {
                int[] coords = getCoordinates(handle, k);
                int x = queue.peek().x;
                int y = queue.peek().y;
                if (coords[0] == x && coords[1] == y && !isInBattle(handle)) {
                    if (arrived(queue, k, handle)) return;
                }
                stillCount = System.currentTimeMillis();
            } else if (!getLocation(handle, k).equals(location)) {
                location = getLocation(handle, k);
                if (!visited.contains(location)) {
                    closeTutorial(handle, k);
                    visited.add(location);
                }
                int[] coords = getCoordinates(handle, k);
                int x = queue.peek().x;
                int y = queue.peek().y;
                if (coords[0] == x && coords[1] == y && !isInBattle(handle)) {
                    if (arrived(queue, k, handle)) return;
                }
                stillCount = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - stillCount >= 15000) {
                int[] a = getCoordinates(handle, k);
                Thread.sleep(500);
                int[] b = getCoordinates(handle, k);
                if (a[0] == b[0] && a[1] == b[1] && !isInBattle(handle) && !terminateFlag) {
                    if (queue.peek().methodId == 0) {
                        click(736, 200, handle, k);
                        click(238, 194, handle, k);
                        click(238, 504, handle, k);
                        click(736, 200, handle, k);
                        click(634, 585, handle, k);
                        click(438, 287, handle, k);
                    } else if (queue.peek().methodId == -1) {
                        click(766, 183, handle, k);
                        click(queue.peek().mapX, queue.peek().mapY, handle, k);
                        click(766, 183, handle, k);
                    }
                }
                stillCount = System.currentTimeMillis();
            }
            Thread.sleep(200);
        }

    }


    private boolean arrived(Queue<Dest> queue, int k, HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return true;
        }
        if (queue.peek().methodId == 0) {
            BufferedImage image = captureWindow(handle, 224, 257, 150, 20);
            if (!tesseracts[k].doOCR(image).contains("[")) {
                Thread.sleep(2000);
                while (!fixFinishQuest(queue.peek().x, queue.peek().y, handle, k)) {
                    Thread.sleep(200);
                }
            }
            finishQuest(handle, k);
            return true;
        } else {
            queue.poll();
            startMovement(false, queue, handle, k);
            return false;
        }
    }

    private void startMovement(boolean questOpened, Queue<Dest> queue, HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        Dest dest = queue.peek();
        switch (dest.methodId) {
            case -1:
                click(766, 183, handle, k);
                click(dest.mapX, dest.mapY, handle, k);
                click(766, 183, handle, k);
                break;
            case 0:
                if (!questOpened) {
                    click(634, 585, handle, k);
                }
                click(438, 287, handle, k);
                break;
            case 2:
                goToTVD(handle, k);
                dest.methodId = -1;
                break;
            case 3:
                goToHTT(handle, k);
                break;
            default:
                break;
        }
    }

    private void finishQuest(HWND handle, int k) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        int[] arr = new int[]{296, 314, 332, 278};
        for (int i = 0; i < 4; i++) {
            BufferedImage image = captureWindow(handle, 223, arr[i], 70, 20);
            if (removeDiacritics(tesseracts[k].doOCR(image)).contains("van tieu")) {
                click(251, arr[i] + 10, handle, k);
                break;
            }
        }
        Thread.sleep(500);
        waitForPrompt(224, 257, 150, 20, "[", handle, k);
        click(557, 266, handle, k); // click on final text box;
    }

    private boolean waitForPrompt(int x, int y, int width, int height, String target, HWND handle, int k) throws TesseractException, InterruptedException {
        int timer = 0;
        while (timer++ < 50 && !terminateFlag) {
            BufferedImage image = captureWindow(handle, x, y, width, height);
            String str = removeDiacritics(tesseracts[k].doOCR(image));
            if (str.contains(target)) {
                Thread.sleep(200);
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }


    private void progressMatch(HWND handle, int k) throws InterruptedException, TesseractException {
        // gi cung so: 239 239 15
        // ta so tan thu: 143 175 111 / 143 206 100
        // ta so tro thu: 79 175 176 / 111 175 176 / 115 191 192 / 83 177 178
        // tro thu so ta: 170 113 143 / 142 111 143 / 170 113 175 / 175 143 175
        synchronized (locks[k]) {
            long x = Math.round(267 * scale);
            long y = Math.round(540 * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(200);
        }
        while (!terminateFlag) {
            Color color = getPixelColor(handle, 225, 196);
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
            if (r == 239) {
                characterAttack(handle, k);
                petAttack(handle, k);
            } else if (r == 143) {
                newbieAttack(handle, k);
                petAttack(handle, k);
            } else if (b == 176) {
                defense(handle, k);
                petAttack(handle, k);
            } else if (b == 143 || b == 175) {
                characterAttack(handle, k);
                petAttack(handle, k);
            } else {
                continue;
            }
            break;
        }
        Thread.sleep(4000);
        boolean finished = false;
        while (isInBattle(handle) && !terminateFlag) {
            if (finished) {
                continue;
            }
            Color color = getPixelColor(handle, 166, 231);
            int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
            if (r == 48 && b == 83 && (g == 79 || g == 111)) {
                while (!finished && !terminateFlag) {
                    BufferedImage image = captureWindow(handle, 337, 54, 150, 70);
                    for (char c : numberTesseracts[k].doOCR(image).toCharArray()) {
                        if (c >= '0' && c <= '9') {
                            defense(handle, k);
                            petDefense(handle, k);
                            finished = true;
                        }
                    }
                    Thread.sleep(200);
                }
            }
            Thread.sleep(200);
        }
    }

    private void parseDestination(Queue<Dest> queue, HWND handle, int k) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        click(634, 585, handle, k);
        BufferedImage image = captureWindow(handle, 337, 275, 310, 35);
        String destination = tesseracts[k].doOCR(image);
        int index = 0;
        for (int i = 2; i < destination.length(); i++) {
            if (destination.charAt(i) == 'm') {
                index = i + 2;
                break;
            }
        }
        switch (destination.charAt(index)) {
            case 'C':
                char c3 = destination.charAt(index + 1);
                if (c3 == 'u') { // cung to to
                    click(634, 585, handle, k);
                    getOut(handle, k);
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(3));
                    queue.offer(new Dest(57, 48, "hoang thach"));
                } else { // chuong chan seu
                    queue.offer(new Dest(37, 145, "long mon"));
                }
                break;
            case 'L': // ly than dong
                click(634, 585, handle, k);
                getOut(handle, k);
                queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, 10, 307, "luc thuy"));
                queue.offer(new Dest(30, 199, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                click(634, 585, handle, k);
                if (c2 == 'm') { // tram lang
                    getOut(handle, k);
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(74, 86, "vo danh"));
                } else if (c2 == 't') { // tiet dai han
                    getOut(handle, k);
                    queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                    queue.offer(new Dest(51, 161, "hao han"));
                } else if (c2 == 'n') { // trinh trung
                    getOut(handle, k);
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(38, 79, "dien vo"));
                } else { // thiet dien phan quan
                    getOut(handle, k);
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(32, 57, "tang kiem"));
                }
                break;
            case 'M': // ma khong quan
                click(634, 585, handle, k);
                getOut(handle, k);
                queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                queue.offer(new Dest(18, 60, "quan dong"));
                break;
            case 'Đ': // duong thu thanh duong mon
                click(634, 585, handle, k);
                getOut(handle, k);
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(14, 71, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                click(634, 585, handle, k);
                getOut(handle, k);
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(29, 70, "quy"));
                break;
            case 'S': // so luu huong
                queue.offer(new Dest(26, 57, "luu huong"));
                break;
            case 'H': // han thuan + hac sinh y
                char c4 = destination.charAt(index + 2);
                if (c4 == 'n') {
                    queue.offer(new Dest(29, 84, "binh khi"));
                } else {
                    queue.offer(new Dest(10, 73, "thai binh"));
                }
                break;
            case 'K': // kim phung hoang
                queue.offer(new Dest(20, 6, "kim ly"));
                break;
            case '3': // ma quan lao thai ba
            case '5':
                queue.offer(new Dest(22, 110, "ky dao"));
                break;
        }
        startMovement(true, queue, handle, k);
    }

    private boolean fixFinishQuest(int x, int y, HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return false;
        }
        switch (x) {
            case 10: // hac sinh y 10 73
                click(399, 212, handle, k);
                break;
            case 14: // duong thu thanh 14 71
                click(505, 182, handle, k);
                break;
            case 18: // ma khong quan 18 60
                click(287, 189, handle, k);
                break;
            case 20: // kim phung hoang 20 65
                click(239, 230, handle, k);
                break;
            case 22: // ma quan lao thai ba 22 110
                click(537, 401, handle, k);
                break;
            case 26: // so luu huong 26 57
                click(145, 276, handle, k);
                break;
            case 29: // ngoc linh lung 29 70, han thuan 29 84
                if (y == 70) {
                    click(400, 131, handle, k);
                } else {
                    click(286, 146, handle, k);
                }
                break;
            case 30: // ly than dong 30 199
                click(683, 308, handle, k);
                break;
            case 32: // thiet dien phan qua 32 57
                click(539, 177, handle, k);
                break;
            case 37: // chuong chan seu 37 145
                click(549, 228, handle, k);
                break;
            case 38: // trinh trung 38 79
                click(399, 125, handle, k);
                break;
            case 51: // tiet dai han 51 161
                click(128, 284, handle, k);
                break;
            case 57: // cung to to 57 48
                click(682, 260, handle, k);
                break;
            case 74: // tram lang 74 86
                click(250, 201, handle, k);
        }
        return waitForPrompt(224, 257, 150, 20, "[", handle, k);
    }

    private void closeTutorial(HWND handle, int k) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        BufferedImage image = captureWindow(handle, 224, 257, 150, 20);
        String str = removeDiacritics(tesseracts[k].doOCR(image));
        if (str.contains("tieu mai") || str.contains("thanh nhi")) {
            click(557, 266, handle, k);
        }
    }

    private void setUpQuest(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(634, 585, handle, k);
        closeTutorial(handle, k);
        click(272, 142, handle, k); // click on unreceived quest
        click(199, 145, handle, k); // click on current quest
        click(634, 585, handle, k);
    }

    private void goToTTTC(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586, handle, k);
        rightClick(445, 417, handle, k); // right click on flag
        waitForPrompt(224, 278, 180, 20, "toa do 1", handle, k);
        click(348, 287, handle, k); // click on toa do
        waitForPrompt(224, 278, 120, 20, "dua ta toi do", handle, k);
        click(259, 286, handle, k); // click take me there
        while (!getLocation(handle, k).contains("truong thanh") && !terminateFlag) {
            Thread.sleep(200);
        }
    }

    private void receiveQuest(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586, handle, k);
        click(306, 145, handle, k); // click on NPC
        waitForPrompt(223, 295, 120, 20, "van tieu", handle, k);
        click(272, 305, handle, k); // click on van tieu ca nhan
        waitForPrompt(223, 335, 180, 20, "cap 2", handle, k);
        click(285, 344, handle, k); // click on cap 2
        waitForPrompt(224, 257, 150, 20, "bach ly", handle, k);
        click(285, 344, handle, k); // click close window
    }

    private void getOut(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(730, 443, handle, k);
        Thread.sleep(2000);
        click(651, 432, handle, k);
        while (!getLocation(handle, k).contains("kinh thanh") && !terminateFlag) {
            Thread.sleep(200);
        }
    }

    private void goToTVD(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(126, 270, handle, k);
        waitForPrompt(224, 257, 100, 20, "binh khi", handle, k);
        click(323, 456, handle, k);
        while (!getLocation(handle, k).contains("danh nhan") && !terminateFlag) {
            Thread.sleep(200);
        }
        click(787, 480, handle, k);
    }

    private void goToHTT(HWND handle, int k) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(557, 287, handle, k);
        waitForPrompt(223, 278, 150, 20, "hoang thach", handle, k);
        click(259, 286, handle, k);
    }

    private String getLocation(HWND handle, int k) throws TesseractException {
        BufferedImage image = captureWindow(handle, 656, 32, 112, 15);
        return removeDiacritics(tesseracts[k].doOCR(image));
    }


    private int[] getCoordinates(HWND handle, int k) throws TesseractException {
        BufferedImage image = captureWindow(handle, 653, 51, 125, 18);
        char[] coords = removeDiacritics(tesseracts[k].doOCR(image)).toCharArray();
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

    private boolean isInBattle(HWND handle) {
        Color color = getPixelColor(handle, 778, 38);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private void defense(HWND handle, int k) throws InterruptedException {
        click(760, 292, handle, k);
    }

    private void petDefense(HWND handle, int k) throws InterruptedException {
        click(760, 246, handle, k);
    }

    private void petAttack(HWND handle, int k) throws InterruptedException {
        if (pets[k] != 0) {
            click(400 + pets[k] * 35, 548, handle, k);
        }
        Thread.sleep(200);
        click(222, 167, handle, k);
    }

    private void characterAttack(HWND handle, int k) throws InterruptedException {
        if (skills[k] != 0) {
            click(375 + skills[k] * 35, 548, handle, k);
        }
        Thread.sleep(200);
        click(222, 167, handle, k);
    }

    private void newbieAttack(HWND handle, int k) throws InterruptedException {
        click(375 + newbies[k] * 35, 548, handle, k);
        Thread.sleep(200);
        click(222, 167, handle, k);
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



    public static Color getPixelColor(HWND hwnd, int x, int y) {
        x -= 3;
        y -= 26;
        // Get the device context of the window
        HDC hdc = User32.INSTANCE.GetDC(hwnd);

        // Get the color of the specified pixel
        int pixelColor = MyGDI32.INSTANCE.GetPixel(hdc, x, y);
        User32.INSTANCE.ReleaseDC(hwnd, hdc); // Release the DC

        // Return the color as a Color object
        return new Color(pixelColor & 0xFF, (pixelColor >> 8) & 0xFF, (pixelColor >> 16) & 0xFF);
    }

    private Map<Integer, HWND> getAllWindows(User32 user32) {
        Map<Integer, HWND> res = new HashMap<>();
        user32.EnumWindows((hwnd, arg) -> {
            char[] text = new char[100];
            user32.GetWindowText(hwnd, text, 100);
            String title = new String(text).trim();
            if (title.startsWith("http://colongonline.com")) {
                int UID = 0;
                int index = 23;
                while (title.charAt(index) != ':') {
                    index++;
                }
                index += 2;
                while (Character.isDigit(title.charAt(index))) {
                    UID = UID * 10 + Character.getNumericValue(title.charAt(index));
                    index++;
                }
                res.put(UID, hwnd);
            }
            return true;
        }, null);
        return res;
    }


    public void click(int a, int b, HWND handle, int k) throws InterruptedException {
        synchronized (locks[k]) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(500);
        }

    }

    public void rightClick(int a, int b, HWND handle, int k) throws InterruptedException {
        synchronized (locks[k]) {
            long x = Math.round((a - 3) * scale);
            long y = Math.round((b - 26) * scale);
            LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
            User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
            Thread.sleep(100);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONDOWN, new WPARAM(WinUser.MK_RBUTTON), lParam);
            User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
            Thread.sleep(500);
        }
    }

    private void click(int[] arr, HWND handle, int k) throws InterruptedException {
        click(arr[0], arr[1], handle, k);
    }

    private void rightClick(int[] arr, HWND handle, int k) throws InterruptedException {
        rightClick(arr[0], arr[1], handle, k);
    }

    private int[] getMouseLocation(HWND handle) throws InterruptedException {
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
    }
}