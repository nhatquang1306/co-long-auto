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


public class test {
    private final Tesseract tesseract;
    private final Tesseract numberTesseract;
    private final int[] enemy;
    private final int[] accounts;
    private final int[] questCount;
    private final int[] skills;
    private final int[] newbies;
    private final int[] pets;
    private boolean terminateFlag;
    private double scale;

    // note
    // attack enemy next turn
    // check for quest when first booting up

    public test(List<Integer> UIDs, List<Integer> questCounts, List<Integer> skillButtons, List<Integer> newbieButtons, List<Integer> petButtons) throws AWTException {
        int n = UIDs.size();
        accounts = new int[n];
        questCount = new int[n];
        skills = new int[n];
        newbies = new int[n];
        pets = new int[n];

        for (int i = 0; i < n; i++) {
            accounts[i] = UIDs.get(i);
            questCount[i] = questCounts.get(i);
            skills[i] = skillButtons.get(i);
            newbies[i] = newbieButtons.get(i);
            pets[i] = petButtons.get(i);
        }

        enemy = new int[]{222, 167};
        terminateFlag = false;

        tesseract = new Tesseract();
        tesseract.setDatapath("input/tesseract/tessdata");
        tesseract.setLanguage("vie");

        numberTesseract = new Tesseract();
        numberTesseract.setDatapath("input/tesseract/tessdata");
        numberTesseract.setLanguage("eng");
        numberTesseract.setTessVariable("tessedit_char_whitelist", "0123456789");
    }

    public void run() throws NativeHookException, InterruptedException, TesseractException, IOException {
        initiateTerminationListener();
        User32 user32 = User32.INSTANCE;

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        int n = accounts.length;
        Map<Integer, HWND> handleMap = getAllWindows(user32);
        Queue<Dest>[] queues = new LinkedList[n];
        Set<String>[] visited = new Set[n];
        HWND[] handles = new HWND[n];
        String[] locations = new String[n];

        for (int i = 0; i < n; i++) {
            Integer UID = accounts[i];
            queues[i] = new LinkedList<>();
            if (!handleMap.containsKey(UID)) {
                throw new InterruptedException("Không có UID này.");
            }
            handles[i] = handleMap.get(UID);
            visited[i] = new HashSet<>();
        }

        for (int i = 0; i < n; i++) {
            setUpQuest(handles[i]);
            goToTTTC(handles[i]);
            while (!getLocation(handles[i]).contains("truong thanh") && !terminateFlag) {
                Thread.sleep(100);
            }
            receiveQuest(handles[i]);
            locations[i] = getLocation(handles[i]);
            visited[i].add(locations[i]);
        }
        for (int i = n - 1; i >= 0; i--) {

        }
        traveling(queues, locations, visited, handles, n);
        System.out.println("finished");

        GlobalScreen.unregisterNativeHook();
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


    private void traveling(Queue<Dest>[] queues, String[] locations, Set<String>[] visited, HWND[] handles, int n) throws InterruptedException, TesseractException {
        long[] stillCount = new long[n];
        long[] finalCount = new long[n];
        long[] startTime = new long[n];
        for (int i = 0; i < n; i++) {
            stillCount[i] = System.currentTimeMillis();
            finalCount[i] = Long.MAX_VALUE;
            startTime[i] = -1;
        }
        int count = n;
        while (count > 0 && !terminateFlag) {
            for (int i = 0; i < n && !terminateFlag; i++) {
                if (queues[i].isEmpty()) {
                    continue;
                }
                if (startTime[i] != -1) {
                    if (!isInBattle(handles[i])) {
                        startTime[i] = -1;
                    } else if (startTime[i] != -2 && System.currentTimeMillis() - startTime[i] >= 4000) {
                        Color color = getPixelColor(handles[i], 166, 231);
                        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                        if (r == 48 && b == 83 && (g == 79 || g == 111)) {
                            int timer = 0;
                            waitForNumber:
                            while (timer++ < 75 && !terminateFlag) {
                                BufferedImage image = captureWindow(handles[i], 337, 54, 150, 70);
                                for (char c : numberTesseract.doOCR(image).toCharArray()) {
                                    if (c >= '0' && c <= '9') {
                                        break waitForNumber;
                                    }
                                }
                                Thread.sleep(100);
                            }
                            defense(handles[i]);
                            petDefense(handles[i]);
                            startTime[i] = -2;
                        }
                    }
                    stillCount[i] = System.currentTimeMillis();
                } else if (isInBattle(handles[i])) {
                    progressMatch(handles[i], i);
                    startTime[i] = System.currentTimeMillis();
                    stillCount[i] = System.currentTimeMillis();
                } else if (locations[i].contains(queues[i].peek().dest) || queues[i].peek().methodId == 4) {
                    int[] coords = getCoordinates(handles[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (((coords[0] == x && coords[1] == y) || queues[i].peek().methodId == 4) && !isInBattle(handles[i])) {
                        if (arrived(queues[i], finalCount, i, handles[i])) {
                            count--;
                        }
                    }
                    stillCount[i] = System.currentTimeMillis();
                } else if (!getLocation(handles[i]).equals(locations[i])) {
                    locations[i] = getLocation(handles[i]);
                    if (!visited[i].contains(locations[i])) {
                        closeTutorial(handles[i]);
                        visited[i].add(locations[i]);
                    }
                    int[] coords = getCoordinates(handles[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (coords[0] == x && coords[1] == y && !isInBattle(handles[i])) {
                        if (arrived(queues[i], finalCount, i, handles[i])) {
                            count--;
                        }
                    }
                    stillCount[i] = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - stillCount[i] >= 14000) {
                    int[] a = getCoordinates(handles[i]);
                    Thread.sleep(300);
                    int[] b = getCoordinates(handles[i]);
                    if (a[0] == b[0] && a[1] == b[1] && !isInBattle(handles[i]) && !terminateFlag) {
                        if (queues[i].peek().methodId == 0) {
                            click(736, 200, handles[i]);
                            click(238, 194, handles[i]);
                            click(238, 504, handles[i]);
                            click(736, 200, handles[i]);
                            click(634, 585, handles[i]);
                            click(438, 287, handles[i]);
                        } else if (queues[i].peek().methodId == -1) {
                            click(766, 183, handles[i]);
                            click(queues[i].peek().mapX, queues[i].peek().mapY, handles[i]);
                            click(766, 183, handles[i]);
                        }
                    }
                    stillCount[i] = System.currentTimeMillis();
                }
            }

        }
        Thread.sleep(100);
    }


    private boolean arrived(Queue<Dest> queue, long[] finalCount, int i, HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return false;
        }
        if (queue.peek().methodId == 0) {
            if (System.currentTimeMillis() - finalCount[i] >= 5000) {
                if (!fixFinishQuest(queue.peek().x, queue.peek().y, handle)) {
                    return false;
                }
            } else if (finalCount[i] == Long.MAX_VALUE) {
                BufferedImage image = captureWindow(handle, 224, 257, 150, 20);
                if (!tesseract.doOCR(image).contains("[")) {
                    finalCount[i] = System.currentTimeMillis();
                    return false;
                }
            } else {
                return false;
            }
            finishQuest(handle);
            finalCount[i] = Long.MAX_VALUE;
            queue.poll();
            if (questCount[i] > 1) {
                queue.offer(new Dest(4));
                queue.offer(new Dest(5));
                queue.offer(new Dest(6));
                questCount[i]--;
            } else {
                return true;
            }
        } else {
            if (queue.peek().methodId != 4) {
                queue.poll();
            }
            startMovement(false, queue, handle);
        }
        return false;
    }

    private void startMovement(boolean questOpened, Queue<Dest> queue, HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        Dest dest = queue.peek();
        switch (dest.methodId) {
            case -1:
                click(766, 183, handle);
                click(dest.mapX, dest.mapY, handle);
                click(766, 183, handle);
                break;
            case 0:
                if (!questOpened) {
                    click(634, 585, handle);
                }
                click(438, 287, handle);
                break;
            case 1:
                getOut(handle);
                break;
            case 2:
                goToTVD(handle);
                dest.methodId = -1;
                break;
            case 3:
                goToHTT(handle);
                break;
            case 4:
                goToTTTC(handle);
                dest.methodId = 5;
                break;
            case 5:
                receiveQuest(handle);
                break;
            case 6:
                queue.poll();
                parseDestination(queue, handle);
                break;
            default:
                break;
        }
    }

    private void finishQuest(HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        int[] arr = new int[] {296, 314, 332, 278};
        for (int i = 0; i < 4; i++) {
            BufferedImage image = captureWindow(handle, 223, arr[i], 70, 20);
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, arr[i] + 10, handle);
                break;
            }
        }
        Thread.sleep(500);
        waitForPrompt(224, 257, 150, 20, "[", handle);
        click(557, 266, handle); // click on final text box;
    }

    private boolean waitForPrompt(int x, int y, int width, int height, String target, HWND handle) throws TesseractException, InterruptedException {
        int timer = 0;
        while (timer++ < 30 && !terminateFlag) {
            BufferedImage image = captureWindow(handle, x, y, width, height);
            String str = removeDiacritics(tesseract.doOCR(image));
            if (str.contains(target)) {
                Thread.sleep(200);
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }


    private void progressMatch(HWND handle, int index) throws InterruptedException {
        if (terminateFlag) {
            return;
        }
        // gi cung so: 239 239 15
        // ta so tan thu: 143 175 111 / 143 206 100
        // ta so tro thu: 79 175 176 / 111 175 176 / 115 191 192 / 83 177 178
        // tro thu so ta: 170 113 143 / 142 111 143 / 170 113 175 / 175 143 175
        Color color = getPixelColor(handle, 225, 196);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        if (r == 239) {
            characterAttack(handle, index);
            petAttack(handle, index);
        } else if (r < g && g > b) {
            newbieAttack(handle, index);
            petDefense(handle);
        } else if (r < g && g < b) {
            defense(handle);
            petAttack(handle, index);
        } else if (r > g && g < b) {
            characterAttack(handle, index);
            petDefense(handle);
        } else {
            characterAttack(handle, index);
            petAttack(handle, index);
        }
    }

    private void parseDestination(Queue<Dest> queue, HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        click(634, 585, handle);
        BufferedImage image = captureWindow(handle, 337, 275, 310, 35);
        String destination = tesseract.doOCR(image);
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
                    click(634, 585, handle);
                    queue.offer(new Dest(1));
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(3));
                    queue.offer(new Dest(57, 48, "hoang thach"));
                } else { // chuong chan seu
                    queue.offer(new Dest(37, 145, "long mon"));
                }
                break;
            case 'L': // ly than dong
                click(634, 585, handle);
                queue.offer(new Dest(1));
                queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, 10, 307, "luc thuy"));
                queue.offer(new Dest(30, 199, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                click(634, 585, handle);
                if (c2 == 'm') { // tram lang
                    queue.offer(new Dest(1));
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(74, 86, "vo danh"));
                } else if (c2 == 't') { // tiet dai han
                    queue.offer(new Dest(1));
                    queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                    queue.offer(new Dest(51, 161, "hao han"));
                } else if (c2 == 'n') { // trinh trung
                    queue.offer(new Dest(1));
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(38, 79, "dien vo"));
                } else { // thiet dien phan quan
                    queue.offer(new Dest(1));
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(32, 57, "tang kiem"));
                }
                break;
            case 'M': // ma khong quan
                click(634, 585, handle);
                queue.offer(new Dest(1));
                queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                queue.offer(new Dest(18, 60, "quan dong"));
                break;
            case 'Đ': // duong thu thanh duong mon
                click(634, 585, handle);
                queue.offer(new Dest(1));
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(14, 71, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                click(634, 585, handle);
                queue.offer(new Dest(1));
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
        startMovement(true, queue, handle);
    }

    private boolean fixFinishQuest(int x, int y, HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return false;
        }
        switch (x) {
            case 10: // hac sinh y 10 73
                click(399, 212, handle);
                break;
            case 14: // duong thu thanh 14 71
                click(505, 182, handle);
                break;
            case 18: // ma khong quan 18 60
                click(287, 189, handle);
                break;
            case 20: // kim phung hoang 20 65
                click(239, 230, handle);
                break;
            case 22: // ma quan lao thai ba 22 110
                click(537, 401, handle);
                break;
            case 26: // so luu huong 26 57
                click(145, 276, handle);
                break;
            case 29: // ngoc linh lung 29 70, han thuan 29 84
                if (y == 70) {
                    click(400, 131, handle);
                } else {
                    click(286, 146, handle);
                }
                break;
            case 30: // ly than dong 30 199
                click(683, 308, handle);
                break;
            case 32: // thiet dien phan qua 32 57
                click(539, 177, handle);
                break;
            case 37: // chuong chan seu 37 145
                click(549, 228, handle);
                break;
            case 38: // trinh trung 38 79
                click(399, 125, handle);
                break;
            case 51: // tiet dai han 51 161
                click(128, 284, handle);
                break;
            case 57: // cung to to 57 48
                click(682, 260, handle);
                break;
            case 74: // tram lang 74 86
                click(250, 201, handle);
        }
        return waitForPrompt(224, 257, 150, 20, "[", handle);
    }

    private void closeTutorial(HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        BufferedImage image = captureWindow(handle, 224, 257, 150, 20);
        String str = removeDiacritics(tesseract.doOCR(image));
        if (str.contains("tieu mai") || str.contains("thanh nhi")) {
            click(557, 266, handle);
        }
    }

    private void setUpQuest(HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(634, 585, handle);
        closeTutorial(handle);
        click(272, 142, handle); // click on unreceived quest
        click(199, 145, handle); // click on current quest
        click(634, 585, handle);
    }

    private void goToTTTC(HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586, handle);
        rightClick(445, 417, handle); // right click on flag
        waitForPrompt(224, 278, 180, 20, "toa do 1", handle);
        click(348, 287, handle); // click on toa do
        waitForPrompt(224, 278, 120, 20, "dua ta toi do", handle);
        click(259, 286, handle); // click take me there
    }

    private void receiveQuest(HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(569, 586, handle);
        click(306, 145, handle); // click on NPC
        waitForPrompt(223, 295, 120, 20, "van tieu", handle);
        click(272, 305, handle); // click on van tieu ca nhan
        waitForPrompt(223, 335, 180, 20, "cap 2", handle);
        click(285, 344, handle); // click on cap 2
        Thread.sleep(500);
        waitForPrompt(224, 257, 150, 20, "bach ly", handle);
        click(285, 344, handle); // click close window
    }

    private void getOut(HWND handle) throws InterruptedException {
        if (terminateFlag) {
            return;
        }
        click(730, 443, handle);
        Thread.sleep(2000);
        click(651, 432, handle);
    }

    private void goToTVD(HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(126, 270, handle);
        waitForPrompt(224, 257, 100, 20, "binh khi", handle);
        click(323, 456, handle);
        while (!getLocation(handle).contains("danh nhan") && !terminateFlag) {
            Thread.sleep(100);
        }
        click(787, 480, handle);
    }

    private void goToHTT(HWND handle) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(557, 287, handle);
        waitForPrompt(223, 278, 150, 20, "hoang thach", handle);
        click(259, 286, handle);
    }

    private String getLocation(HWND handle) throws TesseractException {
        BufferedImage image = captureWindow(handle, 656, 32, 112, 15);
        return removeDiacritics(tesseract.doOCR(image));
    }


    private int[] getCoordinates(HWND handle) throws TesseractException {
        BufferedImage image = captureWindow(handle, 653, 51, 125, 18);
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

    private boolean isInBattle(HWND handle) {
        Color color = getPixelColor(handle, 778, 38);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private void defense(HWND handle) throws InterruptedException {
        click(760, 292, handle);
    }
    private void petDefense(HWND handle) throws InterruptedException {
        click(760, 246, handle);
    }

    private void petAttack(HWND handle, int index) throws InterruptedException {
        if (pets[index] != 0) {
            click(400 + pets[index] * 35, 548, handle);
        }
        Thread.sleep(200);
        click(enemy, handle);
    }

    private void characterAttack(HWND handle, int index) throws InterruptedException {
        if (skills[index] != 0) {
            click(375 + skills[index] * 35, 548, handle);
        }
        Thread.sleep(200);
        click(enemy, handle);
    }

    private void newbieAttack(HWND handle, int index) throws InterruptedException {
        click(375 + newbies[index] * 35, 548, handle);
        Thread.sleep(200);
        click(enemy, handle);
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

    private void initiateTerminationListener() throws NativeHookException {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_F9) {
                    terminateFlag = true; // Terminate the application
                }
            }
        });
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


    public void click(int a, int b, HWND handle) throws InterruptedException {
        long x = Math.round((a - 3) * scale);
        long y = Math.round((b - 26) * scale);
        LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
        User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
        Thread.sleep(100);
        User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONDOWN, new WPARAM(WinUser.MK_LBUTTON), lParam);
        User32.INSTANCE.SendMessage(handle, WinUser.WM_LBUTTONUP, new WPARAM(0), lParam);
        Thread.sleep(500);
    }

    public void rightClick(int a, int b, HWND handle) throws InterruptedException {
        long x = Math.round((a - 3) * scale);
        long y = Math.round((b - 26) * scale);
        LPARAM lParam = new LPARAM((y << 16) | (x & 0xFFFF));
        User32.INSTANCE.SendMessage(handle, WinUser.WM_MOUSEMOVE, new WPARAM(0), lParam);
        Thread.sleep(100);
        User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONDOWN, new WPARAM(WinUser.MK_RBUTTON), lParam);
        User32.INSTANCE.SendMessage(handle, WinUser.WM_RBUTTONUP, new WPARAM(0), lParam);
        Thread.sleep(500);
    }

    private void click(int[] arr, HWND handle) throws InterruptedException {
        click(arr[0], arr[1], handle);
    }

    private void rightClick(int[] arr, HWND handle) throws InterruptedException {
        rightClick(arr[0], arr[1], handle);
    }
    private int[] getMouseLocation(HWND handle) throws InterruptedException {
        Thread.sleep(2000);
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(handle, r);
        Rectangle rect = r.toRectangle();
        rect.x = (int)Math.round(rect.x / scale);
        rect.y = (int)Math.round(rect.y / scale);
        Point m = MouseInfo.getPointerInfo().getLocation();
        return new int[] {m.x - rect.x, m.y - rect.y};
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