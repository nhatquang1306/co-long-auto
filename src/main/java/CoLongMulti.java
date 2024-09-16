
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;


public class CoLongMulti {
    private static Robot robot;
    private static Tesseract tesseract;
    private static Tesseract numberTesseract;
    private static int[] enemy;
    // note
    // detect all windows
    // do fix finish quest method
    // clear queue if any account gets an error

    public static void main(String[] args) throws AWTException, InterruptedException, IOException, TesseractException, NativeHookException {
        Map<Integer, String> usernameMap = getUsernameMap();
        initiateTerminationListener();

        User32 user32 = User32.INSTANCE;

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();

        robot = new Robot();
        enemy = new int[]{222, 167};

        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("vie");

        numberTesseract = new Tesseract();
        numberTesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        numberTesseract.setLanguage("eng");
        numberTesseract.setTessVariable("tessedit_char_whitelist", "0123456789");

        int[] accounts = new int[]{3375, 3373, 3374, 3372};
        int n = accounts.length;
        Queue<Dest>[] queues = new LinkedList[n];
        HWND[] handles = new HWND[n];
        Rectangle[] rects = new Rectangle[n];
        Set<String>[] visited = new Set[n];
        String[] locations = new String[n];
        int[] questCount = new int[]{5, 4, 6, 3};

        for (int i = 0; i < n; i++) {
            int UID = accounts[i];
            queues[i] = new LinkedList<>();
            handles[i] = user32.FindWindow(null, "http://colongonline.com " + usernameMap.get(UID) + "[UID: " + UID + "] (Minh Nguyệt-Kênh 1)");
            rects[i] = getRect(handles[i], user32, scale);
            visited[i] = new HashSet<>();
        }

        for (int i = 0; i < n; i++) {
            setForeground(handles[i]);
            setUpQuest(rects[i]);
            keyPress(KeyEvent.VK_F12);
            goToTTTC(rects[i]);
            while (!getLocation(rects[i]).contains("truong thanh")) {
                Thread.sleep(100);
            }
            receiveQuest(rects[i]);
            locations[i] = getLocation(rects[i]);
            visited[i].add(locations[i]);
        }
        for (int i = n - 1; i >= 0; i--) {
            setForeground(handles[i]);
            parseDestination(rects[i], queues[i]);
        }
        traveling(queues, locations, rects, visited, handles, questCount, n);

        GlobalScreen.unregisterNativeHook();
    }

    private static void traveling(Queue<Dest>[] queues, String[] locations, Rectangle[] rects, Set<String>[] visited, HWND[] handles, int[] questCount, int n) throws IOException, InterruptedException, TesseractException {
        int[] stillCount = new int[n];
        int[] finalCount = new int[n];
        long[] startTime = new long[n];
        Arrays.fill(startTime, -1);
        int count = n;
        Queue<Integer> line = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            line.offer(i);
        }
        while (count > 0) {
            int last = -1;
            for (int i = 0; i < n; i++) {
                if (queues[i].isEmpty()) {
                    continue;
                }
                if (startTime[i] != -1) {
                    if (!isInBattle(rects[i])) {
                        startTime[i] = -1;
                    } else if (startTime[i] != -2 && System.currentTimeMillis() - startTime[i] >= 4000) {
                        setForeground(handles[i]);
                        robot.mouseMove(194 + rects[i].x, 549 + rects[i].y);
                        Thread.sleep(200);
                        Color color = robot.getPixelColor(166 + rects[i].x, 231 + rects[i].y);
                        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                        if (r == 48 && b == 83 && (g == 79 || g == 111)) {
                            waitForNumber:
                            while (true) {
                                Rectangle temp = new Rectangle(rects[i].x + 337, rects[i].y + 54, 150, 70);
                                BufferedImage image = robot.createScreenCapture(temp);
                                for (char c : numberTesseract.doOCR(image).toCharArray()) {
                                    if (c >= '0' && c <= '9') {
                                        break waitForNumber;
                                    }
                                }
                            }
                            defense();
                            defense();
                            startTime[i] = -2;
                        }
                        last = i;
                    }
                    stillCount[i] = 0;
                } else if (isInBattle(rects[i])) {
                    setForeground(handles[i]);
                    progressMatch(rects[i]);
                    startTime[i] = System.currentTimeMillis();
                    last = i;
                    stillCount[i] = 0;
                } else if (locations[i].contains(queues[i].peek().dest) || queues[i].peek().methodId == 4) {
                    int[] coords = getCoordinates(rects[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (((coords[0] == x && coords[1] == y) || finalCount[i] >= 10 || queues[i].peek().methodId == 4) && !isInBattle(rects[i])) {
                        if (arrived(rects[i], queues[i], finalCount, questCount, i, handles[i])) {
                            count--;
                        }
                        last = i;
                        stillCount[i] = 0;
                    }
                } else if (!getLocation(rects[i]).equals(locations[i])) {
                    locations[i] = getLocation(rects[i]);
                    if (!visited[i].contains(locations[i])) {
                        setForeground(handles[i]);
                        closeTutorial(rects[i]);
                        visited[i].add(locations[i]);
                        last = i;
                    }
                    int[] coords = getCoordinates(rects[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (coords[0] == x && coords[1] == y && !isInBattle(rects[i])) {
                        if (arrived(rects[i], queues[i], finalCount, questCount, i, handles[i])) {
                            count--;
                        }
                        last = i;
                    }
                    stillCount[i] = 0;
                } else if (stillCount[i] >= 25) {
                    int[] a = getCoordinates(rects[i]);
                    Thread.sleep(300);
                    int[] b = getCoordinates(rects[i]);
                    if (a[0] == b[0] && a[1] == b[1] && !isInBattle(rects[i])) {
                        setForeground(handles[i]);
                        if (queues[i].peek().methodId == 0) {
                            keyPress(KeyEvent.VK_ALT, KeyEvent.VK_J);
                            click(238, 194, rects[i]);
                            click(238, 504, rects[i]);
                            keyPress(KeyEvent.VK_ALT, KeyEvent.VK_J);
                            keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                            click(438, 287, rects[i]);
                        } else if (queues[i].peek().methodId == -1) {
                            keyPress(KeyEvent.VK_TAB);
                            click(queues[i].peek().mapX, queues[i].peek().mapY, rects[i]);
                            keyPress(KeyEvent.VK_TAB);
                        }
                    }
                    stillCount[i] = 0;
                }
                stillCount[i]++;

            }
//            for (int i = last; i >= 0; i--) {
//                if (!queues[i].isEmpty()) {
//                    setForeground(handles[i]);
//                }
//            }
            Thread.sleep(100);
        }
    }


    private static boolean arrived(Rectangle rect, Queue<Dest> queue, int[] finalCount, int[] questCount, int i, HWND handle) throws TesseractException, InterruptedException, IOException {
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        setForeground(handle);
        if (queue.peek().methodId == 0) {
            if (finalCount[i] >= 10) {
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                click(438, 287, rect);
                finalCount[i] = Integer.MIN_VALUE;
            } else {
                Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
                BufferedImage image = robot.createScreenCapture(temp);
                System.out.println(tesseract.doOCR(image));
                if (tesseract.doOCR(image).contains("[")) {
                    finishQuest(rect);
                    queue.poll();
                    if (questCount[i] > 1) {
                        queue.offer(new Dest(4));
                        queue.offer(new Dest(5));
                        queue.offer(new Dest(6));
                        questCount[i]--;
                    } else {
                        return true;
                    }
                }
            }
            finalCount[i]++;
        } else {
            if (queue.peek().methodId != 4) {
                queue.poll();
            }
            startMovement(false, rect, queue);
        }
        return false;
    }

    private static void startMovement(boolean questOpened, Rectangle rect, Queue<Dest> queue) throws InterruptedException, TesseractException, IOException {
        Dest dest = queue.peek();
        System.out.println(dest.methodId);
        switch (dest.methodId) {
            case -1:
                keyPress(KeyEvent.VK_TAB);
                click(dest.mapX, dest.mapY, rect);
                keyPress(KeyEvent.VK_TAB);
                break;
            case 0:
                if (!questOpened) {
                    keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                }
                click(438, 287, rect);
                break;
            case 1:
                getOut(rect);
                break;
            case 2:
                goToTVD(rect);
                dest.methodId = -1;
                break;
            case 3:
                goToHTT(rect);
                break;
            case 4:
                goToTTTC(rect);
                dest.methodId = 5;
                break;
            case 5:
                receiveQuest(rect);
                break;
            case 6:
                queue.poll();
                parseDestination(rect, queue);
                break;
            default:
                break;
        }
    }

    private static void finishQuest(Rectangle rect) throws TesseractException, InterruptedException, IOException {
        int[] arr = new int[]{278, 296, 314, 332};
        for (int y : arr) {
            Rectangle temp = new Rectangle(rect.x + 223, rect.y + y, 70, 20);
            BufferedImage image = robot.createScreenCapture(temp);
            System.out.println(removeDiacritics(tesseract.doOCR(image)));
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, y + 10, rect);
                break;
            }
        }
        Thread.sleep(500);
        waitForCue(224, 257, 150, 20, "[", rect);
        click(557, 266, rect); // click on final text box;
    }

    private static boolean waitForCue(int x, int y, int width, int height, String target, Rectangle rect) throws TesseractException, InterruptedException {
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        int timer = 0;
        while (timer++ < 30) {
            Rectangle temp = new Rectangle(rect.x + x, rect.y + y, width, height);
            BufferedImage image = robot.createScreenCapture(temp);
            String str = removeDiacritics(tesseract.doOCR(image));
            if (str.contains(target)) {
                Thread.sleep(200);
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }

    private static void progressMatch(Rectangle rect) throws InterruptedException, TesseractException {
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        // gi cung so: 239 239 15
        // ta so tan thu: 143 175 111 / 143 206 100
        // ta so tro thu: 79 175 176 / 111 175 176 / 115 191 192 / 83 177 178
        // tro thu so ta: 170 113 143 / 142 111 143 / 170 113 175 / 175 143 175
        Color color = robot.getPixelColor(225 + rect.x, 196 + rect.y);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        System.out.println(r + " " + g + " " + b);
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        if (r == 239) {
            System.out.println("gi cung so");
            characterAttack(rect);
            robot.mouseMove(194 + rect.x, 549 + rect.y);
            Thread.sleep(200);
            characterAttack(rect);
        } else if (r < g && g > b) {
            System.out.println("tan thu");
            newbieAttack(rect);
            defense();
        } else if (r < g && g < b) {
            System.out.println("tro thu");
            defense();
            characterAttack(rect);
        } else if (r > g && g < b) {
            System.out.println("nhan vat");
            characterAttack(rect);
            defense();
        } else {
            System.out.println("???");
            characterAttack(rect);
            robot.mouseMove(194 + rect.x, 549 + rect.y);
            Thread.sleep(200);
            characterAttack(rect);
        }

    }


    private static void parseDestination(Rectangle rect, Queue<Dest> queue) throws TesseractException, IOException, InterruptedException {
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        Rectangle temp = new Rectangle(rect.x + 337, rect.y + 275, 310, 35);
        BufferedImage image = robot.createScreenCapture(temp);
        String destination = tesseract.doOCR(image);
        System.out.println(destination);
        int index = 0;
        for (int i = 2; i < destination.length(); i++) {
            if (destination.charAt(i) == 'm') {
                index = i + 2;
                break;
            }
        }
        System.out.println(destination.substring(index));
        switch (destination.charAt(index)) {
            case 'C':
                char c3 = destination.charAt(index + 1);
                if (c3 == 'u') { // cung to to
                    keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
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
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                queue.offer(new Dest(1));
                queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, 10, 307, "luc thuy"));
                queue.offer(new Dest(30, 199, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
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
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                queue.offer(new Dest(1));
                queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                queue.offer(new Dest(18, 60, "quan dong"));
                break;
            case 'Đ': // duong thu thanh duong mon
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                queue.offer(new Dest(1));
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(14, 71, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
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
        startMovement(true, rect, queue);
    }
    private static void fixFinishQuest(int sum) {
        switch (sum) {
            case 132: // ma quan lao thai ba 22 110
                break;
            case 26: // kim phung hoang 20 65
                break;
            case 113: // han thuan 29 84
                break;
            case 83: // hac sinh y 10 73
                break;
        }

    }

    private static void closeTutorial(Rectangle rect) throws TesseractException, InterruptedException {
        Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
        BufferedImage image = robot.createScreenCapture(temp);
        String str = removeDiacritics(tesseract.doOCR(image));
        if (str.contains("tieu mai") || str.contains("thanh nhi")) {
            click(557, 266, rect);
        }
    }

    private static void setUpQuest(Rectangle rect) throws InterruptedException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        click(453, 348, rect); // click in case there's tutorial
        click(272, 142, rect); // click on unreceived quest
        click(199, 145, rect); // click on current quest
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
    }

    private static void goToTTTC(Rectangle rect) throws InterruptedException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        rightClick(445, 417, rect); // right click on flag
        waitForCue(224, 278, 180, 20, "toa do 1", rect);
        click(348, 287, rect); // click on toa do
        waitForCue(224, 278, 120, 20, "dua ta toi do", rect);
        click(259, 286, rect); // click take me there
    }

    private static void receiveQuest(Rectangle rect) throws InterruptedException, IOException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        click(306, 145, rect); // click on NPC
        waitForCue(223, 295, 120, 20, "van tieu", rect);
        click(272, 305, rect); // click on van tieu ca nhan
        waitForCue(223, 335, 180, 20, "cap 2", rect);
        click(285, 344, rect); // click on cap 2
        Thread.sleep(500);
        waitForCue(224, 257, 150, 20, "bach ly", rect);
        click(285, 344, rect); // click close window
//        click(783, 228, rect); // close quest tracking
    }

    private static void getOut(Rectangle rect) throws InterruptedException, TesseractException {
        click(730, 443, rect);
        Thread.sleep(2000);
        click(651, 432, rect);
    }

    private static void goToTVD(Rectangle rect) throws InterruptedException, TesseractException, IOException {
        click(126, 270, rect);
        waitForCue(224, 257, 100, 20, "binh khi", rect);
        click(323, 456, rect);
        while (!getLocation(rect).contains("danh nhan")) {
            Thread.sleep(100);
        }
        click(787, 480, rect);
    }

    private static void goToHTT(Rectangle rect) throws InterruptedException, TesseractException, IOException {
        click(557, 287, rect);
        waitForCue(223, 278, 150, 20, "hoang thach", rect);
        click(259, 286, rect);
    }

    private static String getLocation(Rectangle rect) throws TesseractException {
        Rectangle temp = new Rectangle(rect.x + 656, rect.y + 32, 112, 15);
        BufferedImage image = robot.createScreenCapture(temp);
        return removeDiacritics(tesseract.doOCR(image));
    }

    private static int[] getCoordinates(Rectangle rect) throws TesseractException {
        Rectangle temp = new Rectangle(rect.x + 653, rect.y + 51, 125, 18);
        BufferedImage image = robot.createScreenCapture(temp);
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

    private static boolean isInBattle(Rectangle rect) {
        Color color = robot.getPixelColor(778 + rect.x, 38 + rect.y);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private static void defense() throws InterruptedException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_D);
    }

    private static void characterAttack(Rectangle rect) throws InterruptedException {
        keyPress(KeyEvent.VK_F1);
        Thread.sleep(200);
        click(enemy, rect);
    }

    private static void newbieAttack(Rectangle rect) throws InterruptedException {
        keyPress(KeyEvent.VK_F2);
        Thread.sleep(200);
        click(enemy, rect);
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

    private static Rectangle getRect(HWND handle, User32 user32, double scale) {
        RECT r = new RECT();
        user32.GetWindowRect(handle, r);
        Rectangle res = r.toRectangle();

        res.x = (int) Math.round(res.x / scale);
        res.y = (int) Math.round(res.y / scale);
        res.width = (int) Math.round(res.width / scale);
        res.height = (int) Math.round(res.height / scale);
        return res;
    }

    private static void setForeground(HWND handle) throws InterruptedException {
        Thread.sleep(200);
        User32.INSTANCE.SetForegroundWindow(handle);
        Thread.sleep(200);
    }

    private static Map<Integer, String> getUsernameMap() {
        Map<Integer, String> usernameMap = new HashMap<>();
        usernameMap.put(1841, "HiênVũ");
        usernameMap.put(3365, "LanChi");
        usernameMap.put(3366, "TuệChi");
        usernameMap.put(3367, "MaiChi");
        usernameMap.put(3372, "XĐ12");
        usernameMap.put(3373, "XĐ13");
        usernameMap.put(3374, "XĐ14");
        usernameMap.put(3375, "XĐ15");
        usernameMap.put(3390, "XĐ18");
        usernameMap.put(3391, "XĐ19");
        usernameMap.put(3392, "XĐ20");
        usernameMap.put(3432, "Trường");
        usernameMap.put(3434, "Giáo");
        usernameMap.put(3433, "Mẫu");
        usernameMap.put(3304, "Takemi");
        usernameMap.put(411, "Nezumi");
        usernameMap.put(415, "Khang5");
        usernameMap.put(3301, "Saemi");
        usernameMap.put(3303, "Nozomi");
        usernameMap.put(625, "Khanh3");
        usernameMap.put(618, "Khanh4");
        usernameMap.put(617, "Khanh5");
        usernameMap.put(3304, "Takemi");
        usernameMap.put(626, "Khanh2");
        return usernameMap;
    }

    private static void initiateTerminationListener() throws NativeHookException {
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
                if (e.getKeyCode() == NativeKeyEvent.VC_F6) {
                    System.exit(0); // Terminate the application
                }
            }
        });
    }

    private static void click(int x, int y, Rectangle rect) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);
    }

    private static void rightClick(int x, int y, Rectangle rect) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep(500);
    }

    private static void click(int[] arr, Rectangle rect) throws InterruptedException {
        click(arr[0], arr[1], rect);
    }

    private static void rightClick(int[] arr, Rectangle rect) throws InterruptedException {
        rightClick(arr[0], arr[1], rect);
    }

    private static void keyPress(int... keyCode) throws InterruptedException {
        for (int k : keyCode) {
            robot.keyPress(k);
        }
        Thread.sleep(200);
        for (int k : keyCode) {
            robot.keyRelease(k);
        }
        Thread.sleep(500);
    }
}