import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;


public class CoLongMulti {
    private final Robot robot;
    private final Tesseract tesseract;
    private final Tesseract numberTesseract;
    private final int[] enemy;
    private final int[] accounts;
    private final int[] questCount;
    private final int[] skills;
    private final int[] newbies;
    private final int[] pets;
    private boolean terminateFlag;
    // note
    // clear queue if any account gets an error

    public CoLongMulti(List<Integer> UIDs, List<Integer> questCounts, List<Integer> skillButtons, List<Integer> newbieButtons, List<Integer> petButtons) throws AWTException {
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

        robot = new Robot();
        enemy = new int[]{222, 167};
        terminateFlag = false;

        tesseract = new Tesseract();
        tesseract.setDatapath("app/tesseract/tessdata");
        tesseract.setLanguage("vie");

        numberTesseract = new Tesseract();
        numberTesseract.setDatapath("app/tesseract/tessdata");
        numberTesseract.setLanguage("eng");
        numberTesseract.setTessVariable("tessedit_char_whitelist", "0123456789");
    }

    public void run() throws InterruptedException, TesseractException, NativeHookException {
        initiateTerminationListener();
        User32 user32 = User32.INSTANCE;

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        double scale = gc.getDefaultTransform().getScaleX();

        int n = accounts.length;
        Map<Integer, HWND> handleMap = getAllWindows(user32);
        Queue<Dest>[] queues = new LinkedList[n];
        HWND[] handles = new HWND[n];
        Rectangle[] rects = new Rectangle[n];
        Set<String>[] visited = new Set[n];
        String[] locations = new String[n];

        for (int i = 0; i < n; i++) {
            Integer UID = accounts[i];
            queues[i] = new LinkedList<>();
            if (!handleMap.containsKey(UID)) {
                throw new InterruptedException("Không có UID này.");
            }
            handles[i] = handleMap.get(UID);
            rects[i] = getRect(handles[i], user32, scale);
            visited[i] = new HashSet<>();
        }

        for (int i = 0; i < n && !terminateFlag; i++) {
            setForeground(handles[i]);
            setUpQuest(rects[i]);
            keyPress(KeyEvent.VK_F12);
            goToTTTC(rects[i]);
            while (!getLocation(rects[i]).contains("truong thanh") && !terminateFlag) {
                Thread.sleep(100);
            }
            receiveQuest(rects[i]);
            locations[i] = getLocation(rects[i]);
            visited[i].add(locations[i]);
        }
        for (int i = n - 1; i >= 0 && !terminateFlag; i--) {
            setForeground(handles[i]);
            parseDestination(rects[i], queues[i]);
        }
        traveling(queues, locations, rects, visited, handles, questCount, n);

        GlobalScreen.unregisterNativeHook();
    }

    private void traveling(Queue<Dest>[] queues, String[] locations, Rectangle[] rects, Set<String>[] visited, HWND[] handles, int[] questCount, int n) throws InterruptedException, TesseractException {
        long[] stillCount = new long[n];
        long[] finalCount = new long[n];
        long[] startTime = new long[n];
        for (int i = 0; i < n; i++) {
            stillCount[i] = System.currentTimeMillis();
            finalCount[i] = Long.MAX_VALUE;
            startTime[i] = -1;
        }
        Arrays.fill(startTime, -1);
        int count = n;
        while (count > 0 && !terminateFlag) {
            int last = -1;
            for (int i = 0; i < n && !terminateFlag; i++) {
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
                            int timer = 0;
                            waitForNumber:
                            while (timer++ < 75 && !terminateFlag) {
                                Rectangle temp = new Rectangle(rects[i].x + 337, rects[i].y + 54, 150, 70);
                                BufferedImage image = robot.createScreenCapture(temp);
                                for (char c : numberTesseract.doOCR(image).toCharArray()) {
                                    if (c >= '0' && c <= '9') {
                                        break waitForNumber;
                                    }
                                }
                                Thread.sleep(100);
                            }
                            defense();
                            defense();
                            startTime[i] = -2;
                        }
                        last = i;
                    }
                    stillCount[i] = System.currentTimeMillis();
                } else if (isInBattle(rects[i])) {
                    setForeground(handles[i]);
                    progressMatch(rects[i], i);
                    startTime[i] = System.currentTimeMillis();
                    last = i;
                    stillCount[i] = System.currentTimeMillis();
                } else if (locations[i].contains(queues[i].peek().dest) || queues[i].peek().methodId == 4) {
                    int[] coords = getCoordinates(rects[i]);
                    int x = queues[i].peek().x;
                    int y = queues[i].peek().y;
                    if (((coords[0] == x && coords[1] == y) || queues[i].peek().methodId == 4) && !isInBattle(rects[i])) {
                        if (arrived(rects[i], queues[i], finalCount, questCount, i, handles[i])) {
                            count--;
                        }
                        last = i;
                    }
                    stillCount[i] = System.currentTimeMillis();
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
                    stillCount[i] = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - stillCount[i] >= 12000) {
                    int[] a = getCoordinates(rects[i]);
                    Thread.sleep(300);
                    int[] b = getCoordinates(rects[i]);
                    if (a[0] == b[0] && a[1] == b[1] && !isInBattle(rects[i]) && !terminateFlag) {
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
                    stillCount[i] = System.currentTimeMillis();
                }
            }
//            for (int i = last; i >= 0; i--) {
//                if (!queues[i].isEmpty()) {
//                    setForeground(handles[i]);
//                }
//            }
            Thread.sleep(100);
        }
    }


    private boolean arrived(Rectangle rect, Queue<Dest> queue, long[] finalCount, int[] questCount, int i, HWND handle) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return false;
        }
        if (queue.peek().methodId == 0) {
            if (System.currentTimeMillis() - finalCount[i] >= 7000) {
                setForeground(handle);
                if (!fixFinishQuest(queue.peek().x, queue.peek().y, rect)) {
                    return false;
                }
            } else {
                robot.mouseMove(194 + rect.x, 549 + rect.y);
                Thread.sleep(200);
                setForeground(handle);
                Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
                BufferedImage image = robot.createScreenCapture(temp);
                if (!tesseract.doOCR(image).contains("[")) {
                    finalCount[i] = System.currentTimeMillis();
                    return false;
                }
            }
            finishQuest(rect);
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
            setForeground(handle);
            if (queue.peek().methodId != 4) {
                queue.poll();
            }
            startMovement(false, rect, queue);
        }
        return false;
    }

    private void startMovement(boolean questOpened, Rectangle rect, Queue<Dest> queue) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        Dest dest = queue.peek();
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

    private void finishQuest(Rectangle rect) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        int[] arr = new int[]{278, 296, 314, 332};
        for (int i = 3; i >= 0; i--) {
            Rectangle temp = new Rectangle(rect.x + 223, rect.y + arr[i], 70, 20);
            BufferedImage image = robot.createScreenCapture(temp);
            if (removeDiacritics(tesseract.doOCR(image)).contains("van tieu")) {
                click(251, arr[i] + 10, rect);
                break;
            }
        }
        Thread.sleep(500);
        waitForPrompt(224, 257, 150, 20, "[", rect);
        click(557, 266, rect); // click on final text box;
    }

    private boolean waitForPrompt(int x, int y, int width, int height, String target, Rectangle rect) throws TesseractException, InterruptedException {
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        int timer = 0;
        while (timer++ < 30 && !terminateFlag) {
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


    private void progressMatch(Rectangle rect, int index) throws InterruptedException {
        if (terminateFlag) {
            return;
        }
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        // gi cung so: 239 239 15
        // ta so tan thu: 143 175 111 / 143 206 100
        // ta so tro thu: 79 175 176 / 111 175 176 / 115 191 192 / 83 177 178
        // tro thu so ta: 170 113 143 / 142 111 143 / 170 113 175 / 175 143 175
        Color color = robot.getPixelColor(225 + rect.x, 196 + rect.y);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
        if (r == 239) {
            characterAttack(rect, index);
            robot.mouseMove(194 + rect.x, 549 + rect.y);
            Thread.sleep(200);
            petAttack(rect, index);
        } else if (r < g && g > b) {
            newbieAttack(rect, index);
            defense();
        } else if (r < g && g < b) {
            defense();
            petAttack(rect, index);
        } else if (r > g && g < b) {
            characterAttack(rect, index);
            defense();
        } else {
            characterAttack(rect, index);
            robot.mouseMove(194 + rect.x, 549 + rect.y);
            Thread.sleep(200);
            petAttack(rect, index);
        }

    }

    private void parseDestination(Rectangle rect, Queue<Dest> queue) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        robot.mouseMove(194 + rect.x, 549 + rect.y);
        Thread.sleep(200);
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

    private boolean fixFinishQuest(int x, int y, Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return false;
        }
        switch (x) {
            case 10: // hac sinh y 10 73
                click(399, 212, rect);
                break;
            case 14: // duong thu thanh 14 71
                click(505, 182, rect);
                break;
            case 18: // ma khong quan 18 60
                click(287, 189, rect);
                break;
            case 20: // kim phung hoang 20 65
                click(239, 230, rect);
                break;
            case 22: // ma quan lao thai ba 22 110
                click(537, 401, rect);
                break;
            case 26: // so luu huong 26 57
                click(145, 276, rect);
                break;
            case 29: // ngoc linh lung 29 70, han thuan 29 84
                if (y == 70) {
                    click(400, 131, rect);
                } else {
                    click(286, 146, rect);
                }
                break;
            case 30: // ly than dong 30 199
                click(683, 308, rect);
                break;
            case 32: // thiet dien phan qua 32 57
                click(539, 177, rect);
                break;
            case 37: // chuong chan seu 37 145
                click(549, 228, rect);
                break;
            case 38: // trinh trung 38 79
                click(399, 125, rect);
                break;
            case 51: // tiet dai han 51 161
                click(128, 284, rect);
                break;
            case 57: // cung to to 57 48
                click(682, 260, rect);
                break;
            case 74: // tram lang 74 86
                click(250, 201, rect);
        }
        return waitForPrompt(224, 257, 150, 20, "[", rect);
    }

    private void closeTutorial(Rectangle rect) throws TesseractException, InterruptedException {
        if (terminateFlag) {
            return;
        }
        Rectangle temp = new Rectangle(rect.x + 224, rect.y + 257, 150, 20);
        BufferedImage image = robot.createScreenCapture(temp);
        String str = removeDiacritics(tesseract.doOCR(image));
        if (str.contains("tieu mai") || str.contains("thanh nhi")) {
            click(557, 266, rect);
        }
    }

    private void setUpQuest(Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
        closeTutorial(rect);
        click(272, 142, rect); // click on unreceived quest
        click(199, 145, rect); // click on current quest
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_Q);
    }

    private void goToTTTC(Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        rightClick(445, 417, rect); // right click on flag
        waitForPrompt(224, 278, 180, 20, "toa do 1", rect);
        click(348, 287, rect); // click on toa do
        waitForPrompt(224, 278, 120, 20, "dua ta toi do", rect);
        click(259, 286, rect); // click take me there
    }

    private void receiveQuest(Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_E);
        click(306, 145, rect); // click on NPC
        waitForPrompt(223, 295, 120, 20, "van tieu", rect);
        click(272, 305, rect); // click on van tieu ca nhan
        waitForPrompt(223, 335, 180, 20, "cap 2", rect);
        click(285, 344, rect); // click on cap 2
        Thread.sleep(500);
        waitForPrompt(224, 257, 150, 20, "bach ly", rect);
        click(285, 344, rect); // click close window
//        click(783, 228, rect); // close quest tracking
    }

    private void getOut(Rectangle rect) throws InterruptedException {
        if (terminateFlag) {
            return;
        }
        click(730, 443, rect);
        Thread.sleep(2000);
        click(651, 432, rect);
    }

    private void goToTVD(Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(126, 270, rect);
        waitForPrompt(224, 257, 100, 20, "binh khi", rect);
        click(323, 456, rect);
        while (!getLocation(rect).contains("danh nhan") && !terminateFlag) {
            Thread.sleep(100);
        }
        click(787, 480, rect);
    }

    private void goToHTT(Rectangle rect) throws InterruptedException, TesseractException {
        if (terminateFlag) {
            return;
        }
        click(557, 287, rect);
        waitForPrompt(223, 278, 150, 20, "hoang thach", rect);
        click(259, 286, rect);
    }

    private String getLocation(Rectangle rect) throws TesseractException {
        Rectangle temp = new Rectangle(rect.x + 656, rect.y + 32, 112, 15);
        BufferedImage image = robot.createScreenCapture(temp);
        return removeDiacritics(tesseract.doOCR(image));
    }

    private int[] getCoordinates(Rectangle rect) throws TesseractException {
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

    private boolean isInBattle(Rectangle rect) {
        Color color = robot.getPixelColor(778 + rect.x, 38 + rect.y);
        // 0 36 90 - in battle, 90 46 2 - in map
        return color.getRed() < color.getGreen() && color.getGreen() < color.getBlue();
    }

    private void defense() throws InterruptedException {
        keyPress(KeyEvent.VK_ALT, KeyEvent.VK_D);
    }

    private void petAttack(Rectangle rect, int index) throws InterruptedException {
        if (pets[index] != 0) {
            keyPress(pets[index]);
        }
        Thread.sleep(200);
        click(enemy, rect);
    }

    private void characterAttack(Rectangle rect, int index) throws InterruptedException {
        if (skills[index] != 0) {
            keyPress(skills[index]);
        }
        Thread.sleep(200);
        click(enemy, rect);
    }

    private void newbieAttack(Rectangle rect, int index) throws InterruptedException {
        keyPress(newbies[index]);
        Thread.sleep(200);
        click(enemy, rect);
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

    private Rectangle getRect(HWND handle, User32 user32, double scale) {
        RECT r = new RECT();
        user32.GetWindowRect(handle, r);
        Rectangle res = r.toRectangle();

        res.x = (int) Math.round(res.x / scale);
        res.y = (int) Math.round(res.y / scale);
        res.width = (int) Math.round(res.width / scale);
        res.height = (int) Math.round(res.height / scale);
        return res;
    }

    private void setForeground(HWND handle) throws InterruptedException {
        Thread.sleep(200);
        User32.INSTANCE.SetForegroundWindow(handle);
        Thread.sleep(200);
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

    private void click(int x, int y, Rectangle rect) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);
    }

    private void rightClick(int x, int y, Rectangle rect) throws InterruptedException {
        x += rect.x;
        y += rect.y;
        robot.mouseMove(x, y);
        Thread.sleep(200);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep(500);
    }

    private void click(int[] arr, Rectangle rect) throws InterruptedException {
        click(arr[0], arr[1], rect);
    }

    private void rightClick(int[] arr, Rectangle rect) throws InterruptedException {
        rightClick(arr[0], arr[1], rect);
    }

    private void keyPress(int... keyCode) throws InterruptedException {
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