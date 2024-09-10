
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;


import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class CoLong {
    private static Robot robot;
    private static Rectangle rect;
    private static int UID;
    private static Tesseract tesseract;
    private static int[] enemy;
    private static Set<String> visited;
    private static Map<Integer, String> usernameMap;
    private static boolean firstIteration;


    public static void main(String[] args) throws AWTException, InterruptedException, IOException, TesseractException {
        usernameMap = new HashMap<>();
        usernameMap.put(1841, "HiênVũ");
        usernameMap.put(3365, "LanChi");
        usernameMap.put(3366, "TuệChi");
        usernameMap.put(3367, "MaiChi");
        usernameMap.put(3372, "XĐ12");
        usernameMap.put(3373, "XĐ13");
        usernameMap.put(3374, "XĐ14");
        usernameMap.put(411, "Nezumi");

        UID = 411;

        User32 user32 = User32.INSTANCE;
        HWND hwnd = user32.FindWindow(null, "http://colongonline.com " + usernameMap.get(UID) + "[UID: " + UID + "] (Minh Nguyệt-Kênh 1)");
        User32.INSTANCE.SetForegroundWindow(hwnd);

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();

        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("vie");


        RECT r = new RECT();
        user32.GetWindowRect(hwnd, r);

        rect = r.toRectangle();
        rect.x = (int) Math.round(rect.x / scale);
        rect.y = (int) Math.round(rect.y / scale);
        rect.width = (int) Math.round(rect.width / scale);
        rect.height = (int) Math.round(rect.height / scale);

        robot = new Robot();
        // bltt tren dtk: 353 360
        enemy = new int[]{222, 167};

        visited = new HashSet<>();
        firstIteration = true;




//        Thread.sleep(2000);
//        Point location = MouseInfo.getPointerInfo().getLocation();
//        System.out.println(location.x - rect.x);
//        System.out.println(location.y - rect.y);

//        Rectangle temp = new Rectangle(rect.x + 223, rect.y + 314, 70, 20);
//        BufferedImage image = robot.createScreenCapture(temp);
//        ImageIO.write(image, "png", new File("screenshot.png"));
//        System.out.println(removeDiacritics(tesseract.doOCR(image)));

        setUpQuest();
        int questCount = 10;
        for (int i = 0; i < questCount; i++) {
            startQuest();
        }
    }

    private static void setUpQuest() throws InterruptedException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        click(453, 348); // click in case there's tutorial
        click(272, 142); // click on unreceived quest
        click(199, 145); // click on current quest
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
    }

    private static void startQuest() throws TesseractException, IOException, InterruptedException {
        receiveQuest();
        goToDestination();
    }

    private static void receiveQuest() throws InterruptedException, IOException, TesseractException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        rightClick(445, 417); // right click on flag
        while (!waitForCue(224, 278, 180, 20, "toa do 1")) {
            Thread.sleep(100);
        }
        click(348, 287); // click on toa do
        while (!waitForCue(224, 278, 120, 20, "dua ta toi do")) {
            Thread.sleep(100);
        }
        click(259, 286); // click take me there
        while (!getLocation().contains("truong thanh")) {
            Thread.sleep(100);
        }
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        click(306, 145); // click on NPC
        while (!waitForCue(223, 295, 120, 20, "van tieu")) {
            Thread.sleep(100);
        }
        click(272, 305); // click on van tieu ca nhan
        while (!waitForCue(223, 335, 180, 20, "cap 2")) {
            Thread.sleep(100);
        }
        click(285, 344); // click on cap 2
        Thread.sleep(500);
        while (!waitForCue(224, 257, 150, 20, "bach ly")) {
            Thread.sleep(100);
        }
        click(285, 344); // click close window
    }

    private static void goToDestination() throws TesseractException, IOException, InterruptedException {
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
        int[] target = new int[2];
        String location = "";
        switch (destination.charAt(index)) {
            case 'C':
                char c3 = destination.charAt(index + 1);
                if (c3 == 'u') { // cung to to
                    location = "hoang thach";
                    target[0] = 57;
                    target[1] = 48;
                    keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                    goToHTT();
                    keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                } else { // chuong chan seu
                    location = "long mon";
                    target[0] = 37;
                    target[1] = 145;
                }
                break;
            case 'L': // ly than dong
                location = "ngan cau";
                target[0] = 30;
                target[1] = 199;
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                goToHTT();
                traveling(new int[]{623, 264}, new int[]{10, 307}, "luc thuy");
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                if (c2 == 'm') { // tram lang
                    location = "vo danh";
                    target[0] = 74;
                    target[1] = 86;
                    goToTVD();
                } else if (c2 == 't') { // tiet dai han
                    location = "hao han";
                    target[0] = 51;
                    target[1] = 161;
                    goToDPS();
                } else if (c2 == 'n') { // trinh trung
                    location = "dien vo";
                    target[0] = 38;
                    target[1] = 79;
                    goToKTDG();
                } else { // thiet dien phan quan
                    location = "tang kiem";
                    target[0] = 32;
                    target[1] = 57;
                    goToKTDG();
                }
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                break;
            case 'M': // ma khong quan
                location = "quan dong";
                target[0] = 18;
                target[1] = 60;
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                goToDPS();
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                break;
            case 'Đ': // duong thu thanh duong mon
                location = "thoi luyen";
                target[0] = 14;
                target[1] = 71;
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                goToKTDG();
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                break;
            case 'N': // ngoc linh lung quy vuc
                location = "quy";
                target[0] = 29;
                target[1] = 70;
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                goToKTDG();
                keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                break;
            case 'S': // so luu huong
                location = "luu huong";
                target[0] = 26;
                target[1] = 57;
                break;
            case 'H': // han thuan + hac sinh y
                char c4 = destination.charAt(index + 2);
                if (c4 == 'n') {
                    location = "binh khi";
                    target[0] = 29;
                    target[1] = 84;
                } else {
                    location = "thai binh";
                    target[0] = 10;
                    target[1] = 73;
                }
                break;
            case 'K': // kim phung hoang
                location = "kim ly";
                target[0] = 20;
                target[1] = 6;
                break;
            case '3': // ma quan lao thai ba
            case '5':
                location = "ky dao";
                target[0] = 22;
                target[1] = 110;
                break;
        }
        click(438, 287);
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        traveling(null, target, location); // travel to final npc by clicking on quest
        finishQuest(); // find location of finish quest button and press it
    }

    private static void finishQuest() throws TesseractException, InterruptedException, IOException {
        int[] arr = new int[]{278, 296, 314, 332};
        for (int y : arr) {
            Rectangle temp = new Rectangle(rect.x + 223, rect.y + y, 70, 20);
            BufferedImage image = robot.createScreenCapture(temp);
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, y + 10);
                break;
            }
        }
        Thread.sleep(500);
        while (!waitForCue(224, 257, 150, 20, "[")) {
            Thread.sleep(100);
        }
        click(557, 266); // click on final text box;
    }

    private static void traveling(int[] mapCoords, int[] target, String location) throws IOException, InterruptedException, TesseractException {
        if (mapCoords != null) {
            keyPress(KeyEvent.VK_TAB);
            if (!visited.contains("medium map")) { // check for first use of small map
                Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
                BufferedImage image = robot.createScreenCapture(temp);
                String str = removeDiacritics(tesseract.doOCR(image));
                if (str.contains("tieu mai") || str.contains("thanh nhi")) {
                    click(557, 266);
                }
                visited.add("medium map");
            }
            click(mapCoords);
            keyPress(KeyEvent.VK_TAB);
        }
        int count = 0;
        String currentLocation = getLocation();
        while (true) {
            if (isInBattle()) {
                progressMatch();
                count = 0;
            } else if (currentLocation.contains(location)) {
                int[] coords = getCoordinates();
                if (coords[0] == target[0] && coords[1] == target[1] && !isInBattle()) {
                    int stillCount = 0;
                    while (mapCoords == null && !waitForCue(224, 257, 150, 20, "[")) {
                        if (stillCount >= 50) {
                            keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
                            click(438, 287);
                            robot.mouseMove(194 + rect.x, 549 + rect.y);
                            stillCount = Integer.MIN_VALUE;
                        }
                        stillCount++;
                        Thread.sleep(100);
                    }
                    Thread.sleep(500);
                    break;
                }
                count = 0;
            } else if (!getLocation().equals(currentLocation)) {
                currentLocation = getLocation();
                count = 0;
                if (visited.contains(currentLocation)) {
                    continue;
                }
                Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
                BufferedImage image = robot.createScreenCapture(temp);
                String str = removeDiacritics(tesseract.doOCR(image));
                if (str.contains("tieu mai") || str.contains("thanh nhi")) {
                    click(557, 266);
                }
                visited.add(currentLocation);
            } else if (count >= 100) {
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
            count++;
            Thread.sleep(100);
        }
    }

    private static void progressMatch() throws IOException, InterruptedException, TesseractException {
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
            characterAttack();
            robot.mouseMove(194 + rect.x, 549 + rect.y);
            Thread.sleep(200);
            characterAttack();
        } else if (r == 143) {
            System.out.println("tan thu");
            newbieAttack();
            defense();
        } else if (b == 176) {
            System.out.println("tro thu");
            defense();
            characterAttack();
        } else {
            System.out.println("nhan vat");
            characterAttack();
            defense();
        }
//        if (r == 143) {
//            newbieAttack();
//        } else {
//            characterAttack();
//        }
//        robot.mouseMove(194 + rect.x, 549 + rect.y);
//        Thread.sleep(200);
//        characterAttack();
        while (isInBattle()) {
            Rectangle f = new Rectangle(rect.x + 224, rect.y + 307, 180, 20);
            BufferedImage im = robot.createScreenCapture(f);
            System.out.println(removeDiacritics(tesseract.doOCR(im)));
            if (removeDiacritics(tesseract.doOCR(im)).contains("dung danh")) {
                defense();
                defense();
                while (isInBattle()) {
                    Thread.sleep(100);
                }
                break;
            }
            Thread.sleep(100);
        }
    }

    private static void goToKTDG() throws InterruptedException, TesseractException, IOException {
        click(730, 443);
        Thread.sleep(2000);
        click(651, 432);
        Thread.sleep(2000);
        traveling(new int[]{688, 199}, new int[]{18, 254}, "kinh thanh dong");
    }

    private static void goToDPS() throws InterruptedException, TesseractException, IOException {
        click(730, 443);
        Thread.sleep(2000);
        click(651, 432);
        Thread.sleep(2000);
        traveling(new int[]{102, 497}, new int[]{161, 49}, "dieu phong");
    }

    private static void goToTVD() throws InterruptedException, TesseractException, IOException {
        click(730, 443);
        Thread.sleep(2000);
        click(651, 432);
        Thread.sleep(2000);
        traveling(new int[]{472, 227}, new int[]{173, 164}, "kinh thanh");
        click(126, 270);
        while (!waitForCue(224, 257, 100, 20, "binh khi")) {
            Thread.sleep(100);
        }
        click(323, 456);
        while (!getLocation().contains("danh nhan")) {
            Thread.sleep(100);
        }
        click(787, 480);
        while (!getLocation().contains("tivo")) {
            Thread.sleep(100);
        }
        Thread.sleep(200);
    }

    private static void goToHTT() throws InterruptedException, TesseractException, IOException {
        goToTVD();
        click(558, 255);
        while (!waitForCue(223, 278, 150, 20, "hoang thach")) {
            Thread.sleep(100);
        }
        click(259, 286);
        while (!getLocation().contains("hoang thach")) {
            Thread.sleep(100);
        }
    }

    private static String getLocation() throws TesseractException {
        Rectangle temp = new Rectangle(rect.x + 656, rect.y + 32, 112, 15);
        BufferedImage image = robot.createScreenCapture(temp);
        return removeDiacritics(tesseract.doOCR(image));
    }

    private static int[] getCoordinates() throws IOException, TesseractException {
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

    private static boolean isInBattle() {
        Color color = robot.getPixelColor(778 + rect.x, 38 + rect.y);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private static void defense() throws InterruptedException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_D);
    }

    private static void characterAttack() throws InterruptedException {
        keyPress(KeyEvent.VK_F1);
        Thread.sleep(200);
        click(enemy);
    }

    private static void newbieAttack() throws InterruptedException {
        keyPress(KeyEvent.VK_F2);
        Thread.sleep(200);
        click(enemy);
    }

    private static boolean waitForCue(int x, int y, int width, int height, String target) throws TesseractException, InterruptedException, IOException {
        if (firstIteration) {
            robot.mouseMove(194 + rect.x, 549 + rect.y);
        }
        Rectangle temp = new Rectangle(rect.x + x, rect.y + y, width, height);
        BufferedImage image = robot.createScreenCapture(temp);
        String str = removeDiacritics(tesseract.doOCR(image));
        if (str.contains(target)) {
            Thread.sleep(200);
            firstIteration = true;
            return true;
        }
        firstIteration = false;
        return false;
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

    private static void click(int x, int y) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);
    }

    private static void rightClick(int x, int y) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep(500);
    }

    private static void click(int[] arr) throws InterruptedException {
        click(arr[0], arr[1]);
    }

    private static void rightClick(int[] arr) throws InterruptedException {
        rightClick(arr[0], arr[1]);
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