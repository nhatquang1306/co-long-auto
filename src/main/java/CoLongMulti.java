import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

public class CoLongMulti {
    private static Robot robot;
    private static Tesseract tesseract;
    private static int[] enemy;

    public static void main(String[] args) throws AWTException, InterruptedException, IOException, TesseractException {
        Map<Integer, String> usernameMap = new HashMap<>();
        usernameMap.put(1841, "HiênVũ");
        usernameMap.put(3365, "LanChi");
        usernameMap.put(3366, "TuệChi");
        usernameMap.put(3367, "MaiChi");
        usernameMap.put(3372, "XĐ12");
        usernameMap.put(3373, "XĐ13");
        usernameMap.put(3374, "XĐ14");

        User32 user32 = User32.INSTANCE;

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();

        robot = new Robot();
        enemy = new int[]{222, 167};

        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("vie");

        int[] accounts = new int[]{3373, 3374};
        int n = accounts.length;
        Queue<Dest>[] queues = new LinkedList[n];
        HWND[] handles = new HWND[n];
        Rectangle[] rects = new Rectangle[n];
        Set<String>[] visited = new Set[n];
        String[] locations = new String[n];

        for (int i = 0; i < n; i++) {
            int UID = accounts[i];
            queues[i] = new LinkedList<>();
            handles[i] = user32.FindWindow(null, "http://colongonline.com " + usernameMap.get(UID) + "[UID: " + UID + "] (Minh Nguyệt-Kênh 1)");
            rects[i] = getRect(handles[i], user32, scale);
            visited[i] = new HashSet<>();
        }

        int questCount = 10;
        for (int k = 0; k < questCount; k++) {
            for (int i = 0; i < n; i++) {
                User32.INSTANCE.SetForegroundWindow(handles[i]);
                if (k == 0) setUpQuest(rects[i]);
                receiveQuest(rects[i]);
            }
            for (int i = n - 1; i >= 0; i--) {
                User32.INSTANCE.SetForegroundWindow(handles[i]);
                parseDestination(rects[i], queues[i]);
                locations[i] = getLocation(rects[i]);
            }
        }
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

    private static void setUpQuest(Rectangle rect) throws InterruptedException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        click(453, 348, rect); // click in case there's tutorial
        click(272, 142, rect); // click on unreceived quest
        click(199, 145, rect); // click on current quest
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        keyPress(KeyEvent.VK_TAB);
        closeTutorial(rect);
        keyPress(KeyEvent.VK_TAB);
    }
    private static void receiveQuest(Rectangle rect) throws InterruptedException, IOException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        rightClick(445, 417, rect); // right click on flag
        waitForCue(224, 278, 180, 20, "toa do 1", rect);
        click(348, 287, rect); // click on toa do
        waitForCue(224, 278, 120, 20, "dua ta toi do", rect);
        click(259, 286, rect); // click take me there
        while (!getLocation(rect).contains("truong thanh")) {
            Thread.sleep(100);
        }
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        click(306, 145, rect); // click on NPC
        waitForCue(223, 295, 120, 20, "van tieu", rect);
        click(272, 305, rect); // click on van tieu ca nhan
        waitForCue(223, 335, 180, 20, "cap 2", rect);
        click(285, 344, rect); // click on cap 2
        Thread.sleep(500);
        waitForCue(224, 257, 150, 20, "bach ly", rect);
        click(285, 344, rect); // click close window
    }

    private static void parseDestination(Rectangle rect, Queue<Dest> queue) throws TesseractException, IOException, InterruptedException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        Rectangle temp = new Rectangle(rect.x + 337, rect.y + 275, 310, 35);
        BufferedImage image = robot.createScreenCapture(temp);
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
                    keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                    getOut(rect);
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(true));
                    queue.offer(new Dest(57, 48, "hoang thach"));
                } else { // chuong chan seu
                    queue.offer(new Dest(37, 145, "long mon"));
                }
                break;
            case 'L': // ly than dong
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                getOut(rect);
                queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                queue.offer(new Dest(true));
                queue.offer(new Dest(623, 264, 10, 107, "luc thuy"));
                queue.offer(new Dest(30, 199, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                if (c2 == 'm') { // tram lang
                    getOut(rect);
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(false));
                    queue.offer(new Dest(74, 86, "vo danh"));
                } else if (c2 == 't') { // tiet dai han
                    getOut(rect);
                    queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                    queue.offer(new Dest(51, 161, "hao han"));
                } else if (c2 == 'n') { // trinh trung
                    getOut(rect);
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(38, 79, "dien vo"));
                } else { // thiet dien phan quan
                    getOut(rect);
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(32, 57, "tang kiem"));
                }
                break;
            case 'M': // ma khong quan
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                getOut(rect);
                queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                queue.offer(new Dest(18, 60, "quan dong"));
                break;
            case 'Đ': // duong thu thanh duong mon
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                getOut(rect);
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(14, 71, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                getOut(rect);
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
        if (queue.peek().mapX == -1) {
            click(438, 287, rect);
        } else {
            keyPress(KeyEvent.VK_TAB);
            click(queue.peek().mapX, queue.peek().mapY, rect);
        }
    }

    private static void traveling(Queue<Dest>[] queues, String[] locations, Rectangle[] rects, Set<String>[] visited, HWND[] handles, int n) throws IOException, InterruptedException, TesseractException {
        int[] stillCount = new int[n];
        int count = n;
        while (count > 0) {
            int last = 0;
            for (int i = 0; i < n; i++) {
                if (queues[i].isEmpty()) {
                    continue;
                }
                if (isInBattle(rects[i])) {
                    User32.INSTANCE.SetForegroundWindow(handles[i]);
                    progressMatch(rects[i]);
                    last = i;
                    stillCount[i] = 0;
                } else if (locations[i].contains(queues[i].peek().dest) ) {
                    int[] coords = getCoordinates(rects[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (coords[0] == x && coords[1] == y && !isInBattle(rects[i])) {
                        if (queues[i].peek().mapX == -1) {
                            Rectangle temp = new Rectangle(rects[i].x + 224, rects[i].y + 257, 150, 20);
                            BufferedImage image = robot.createScreenCapture(temp);
                            if (removeDiacritics(tesseract.doOCR(image)).contains("[")) {
                                finishQuest(rects[i]);
                                queues[i].poll();
                                count--;
                            }
                        } else {
                            queues[i].poll();
                            stillCount[i] = 0;
                        }
                    }
                } else if (!getLocation(rects[i]).equals(locations[i])) {
                    locations[i] = getLocation(rects[i]);
                    stillCount[i] = 0;
                    if (!visited[i].contains(locations[i])) {
                        closeTutorial(rects[i]);
                        visited[i].add(locations[i]);
                    }
                } else if (stillCount[i] >= 100) {

                }
                stillCount[i]++;
            }
            for (int i = last; i >= 0; i--) {
                User32.INSTANCE.SetForegroundWindow(handles[i]);
            }
            Thread.sleep(100);
            if (count >= 100) {
                int[] a = getCoordinates();
                Thread.sleep(500);
                int[] b = getCoordinates();
                if (a[0] == b[0] && a[1] == b[1] && !isInBattle()) {
                    if (mapCoords != null) {
                        keyPress(KeyEvent.VK_TAB);
                        click(mapCoords);
                        keyPress(KeyEvent.VK_TAB);
                    } else {
                        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_J);
                        click(90, 185);
                        click(165, 497);
                        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                        click(438, 287);
                        robot.mouseMove(194 + rect.x, 549 + rect.y);
                    }
                }
                count = 0;
            }


        }
    }

    private static void finishQuest(Rectangle rect) throws TesseractException, InterruptedException, IOException {
        int[] arr = new int[]{278, 296, 314, 332};
        for (int y : arr) {
            Rectangle temp = new Rectangle(rect.x + 223, rect.y + y, 70, 20);
            BufferedImage image = robot.createScreenCapture(temp);
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, y + 10, rect);
                break;
            }
        }
        Thread.sleep(500);
        while (!waitForCue(224, 257, 150, 20, "[", rect)) {
            Thread.sleep(100);
        }
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
        Thread.sleep(500);
        // ta so tro thu: 79 175 176 / 111 175 176
        // gi cung so: 239 239 15 /
        // tro thu so ta: 170 113 143 / 142 111 143 / 170 113 175 / 175 143 175
        // ta so tan thu: 143 175 111 / 143 206 100
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
        } else if (r == 143) {
            System.out.println("tan thu");
            newbieAttack(rect);
            defense();
        } else if (b == 176) {
            System.out.println("tro thu");
            defense();
            characterAttack(rect);
        } else {
            System.out.println("nhan vat");
            characterAttack(rect);
            defense();
        }
        while (isInBattle(rect)) {
            Rectangle f = new Rectangle(rect.x + 224, rect.y + 307, 180, 20);
            BufferedImage im = robot.createScreenCapture(f);
            System.out.println(removeDiacritics(tesseract.doOCR(im)));
            if (removeDiacritics(tesseract.doOCR(im)).contains("dung danh")) {
                defense();
                defense();
                while (isInBattle(rect)) {
                    Thread.sleep(100);
                }
                break;
            }
            Thread.sleep(100);
        }
    }

    private static String getLocation(Rectangle rect) throws TesseractException {
        Rectangle temp = new Rectangle(rect.x + 656, rect.y + 32, 112, 15);
        BufferedImage image = robot.createScreenCapture(temp);
        return removeDiacritics(tesseract.doOCR(image));
    }

    private static int[] getCoordinates(Rectangle rect) throws IOException, TesseractException {
        Rectangle temp = new Rectangle(rect.x + 653, rect.y + 51, 125, 18);
        BufferedImage image = robot.createScreenCapture(temp);
        String str = tesseract.doOCR(image);
        char[] coords = str.toCharArray();
        int[] res = new int[2];
        int i = 0;
        for (; i < coords.length && coords[i] != 'Y' && coords[i] != 'Ý'; i++) {
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

    private static void getOut(Rectangle rect) throws InterruptedException {
        click(730, 443, rect);
        Thread.sleep(2000);
        click(651, 432, rect);
    }

    private static void closeTutorial(Rectangle rect) throws TesseractException, InterruptedException {
        Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
        BufferedImage image = robot.createScreenCapture(temp);
        String str = removeDiacritics(tesseract.doOCR(image));
        if (str.contains("tieu mai") || str.contains("thanh nhi")) {
            click(557, 266, rect);
        }
    }

    private static void goToTVD(Rectangle rect) throws InterruptedException, TesseractException, IOException {
        click(126, 270, rect);
        while (!waitForCue(224, 257, 100, 20, "binh khi", rect)) {
            Thread.sleep(100);
        }
        click(323, 456, rect);
        while (!getLocation(rect).contains("danh nhan")) {
            Thread.sleep(100);
        }
        click(787, 480, rect);
        while (!getLocation(rect).contains("tivo")) {
            Thread.sleep(100);
        }
        Thread.sleep(200);
    }

    private static void goToHTT(Rectangle rect) throws InterruptedException, TesseractException, IOException {
        click(558, 255, rect);
        while (!waitForCue(223, 278, 150, 20, "hoang thach", rect)) {
            Thread.sleep(100);
        }
        click(259, 286, rect);
        while (!getLocation(rect).contains("hoang thach")) {
            Thread.sleep(100);
        }
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