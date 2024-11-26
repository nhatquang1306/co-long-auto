package Main;

import Utilities.CoLongUtilities;
import Utilities.SwitchStatements;
import Objects.Fight;
import Objects.Clan;
import Objects.Dest;

import com.sun.jna.platform.win32.WinDef.HWND;


import javax.swing.*;
import java.awt.*;
import java.util.*;


public class CoLong extends CoLongUtilities {
    private boolean going;
    private final Fight fight;
    private final int clanSkill;
    private final Clan clan;
    private final int[] flag;
    private final JButton startButton;


    public CoLong(int skill, int newbie, int pet, int clanSkill, double scale,
                  String clan, JButton startButton, HWND handle, String username) {
        this.scale = scale;
        this.handle = handle;
        this.username = username;
        this.going = true;
        this.fight = new Fight(skill, newbie, pet, this);
        this.clanSkill = clanSkill;
        if (clan.endsWith("ĐTK")) {
            this.clan = null;
            this.flag = new int[]{445, 417, clan.length() > 3 ? 0 : 1};
        } else {
            this.clan = new Clan(clan);
            this.flag = new int[3];
        }
        this.startButton = startButton;

        initialize();
    }

    public void run() {
        try {
            Set<String> visited = new HashSet<>();
            visited.add("tttc");
            while (going && !terminateFlag) {
                Deque<Dest> deque = new ArrayDeque<>();
                boolean closeInventory = false;
                if (getLocation().equals("tttc")) {
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
                receiveQuest(deque, visited, closeInventory && clan == null, false);
                traveling(deque, visited);
            }
            if (!questFailed() && !getLocation().equals("tttc")) {
                goToTTTC(visited);
            }
        } catch (Exception _) {

        } finally {
            startButton.setBackground(null);
            startButton.setText("Start");
        }
    }

    private void goToTTTC(Set<String> visited) throws InterruptedException {
        if (terminateFlag) return;
        if (clan == null) {
            goWithFlag(visited);
        } else {
            goWithClan(visited);
        }
    }

    private void goWithFlag(Set<String> visited) throws InterruptedException {
        long start = -20000;
        do {
            Thread.sleep(500);
            if (System.currentTimeMillis() - start < 20000) {
                continue;
            }
            click(569, 586);
            if (!visited.contains("inventory")) {
                closeTutorial();
                visited.add("inventory");
            }
            rightClick(flag);
            if (waitForDialogueBox(20)) {
                if (flag[2] == 0) {
                    click(348, 287); // click on toa do
                    waitForDialogueBox(20);
                    click(259, 286); // click take me there
                } else {
                    click(321, 359);
                }
            }
            start = System.currentTimeMillis();
        } while (!terminateFlag && !isAtLocation(18, 72, "tttc"));
    }

    private void goWithClan(Set<String> visited) throws InterruptedException {
        String location = clan.getLocation();
        int[] info = clan.getInfo();
        String temp = getLocation();
        // transport back to clan
        while (!terminateFlag && !temp.equals(location)) {
            rightClick(375 + clanSkill * 35, 548);
            Thread.sleep(4000);
            temp = getLocation();
            if (temp.equals("kdn") && isAtLocation(19, 65)) {
                goToTD();
                temp = "td";
            }
        }
        if (!visited.contains(temp)) {
            closeTutorial();
            visited.add(temp);
        }

        // go to clan npc
        long start = -20000;
        int limit = 1;
        do {
            if (!isAtLocation(info[2], info[3])) {
                if (System.currentTimeMillis() - start < 20000) continue;
                useMap(visited, info[0], info[1]);
                limit = 1;
                start = System.currentTimeMillis();
            } else {
                Thread.sleep(1000);
                clickOnNpc(info[4], info[5]);
                limit = 20;
                start = -20000;
            }
        } while (!terminateFlag && !waitForDialogueBox(limit));

        // go to KT
        if (terminateFlag) return;
        click(256, 287);
        while (!terminateFlag && !getLocation().equals("kt")) {
            Thread.sleep(500);
        }
        if (!visited.contains("kt")) {
            closeTutorial();
            visited.add("kt");
        }

        // go to tttc
        start = -50000;
        do {
            if (System.currentTimeMillis() - start < 50000) {
                continue;
            }
            useMap(visited, 102, 421);
            start = System.currentTimeMillis();
            Thread.sleep(500);
        } while (!terminateFlag && !getLocation().equals("tttc"));
        Thread.sleep(500);
        click(171, 240);
        while (!terminateFlag && !isAtLocation(24, 77)) {
            Thread.sleep(1000);
        }
    }

    private void goToTD() throws InterruptedException {
        click(395, 528);
        while (!terminateFlag && !isAtLocation(19, 90)) {
            Thread.sleep(500);
        }
        Thread.sleep(500);
        click(41, 589);
        while (!terminateFlag && !getLocation().equals("td")) {
            Thread.sleep(500);
        }
    }

    // click on npc to receive quest
    private void receiveQuest(Deque<Dest> deque, Set<String> visited, boolean closeInventory, boolean isRetry) throws InterruptedException {
        if (terminateFlag) return;
        if (closeInventory) click(569, 586);
        // where to click depends on how the user got there
        int npcX = clan == null ? 306 : 97, npcY = clan == null ? 145 : 126;
        int locationX = clan == null ? 18 : 24, locationY = clan == null ? 72 : 77;
        do {
            if (!isAtLocation(locationX, locationY)) {
                if (clan == null) {
                    goWithFlag(visited);
                    click(569, 586);
                } else goWithClan(visited);
            }
            clickOnNpc(npcX, npcY);
        } while (!terminateFlag && !waitForDialogueBox(20));
        click(272, 305); // click on van tieu ca nhan
        waitForDialogueBox(20);
        int points = pr.read();
        if ((points >= 10 && points < 20) || (points == 0 && isRetry)) {
            going = false;
        }
        click(285, 344); // click on cap 2
        if (!waitForDialogueBox(50) && !isRetry) {
            receiveQuest(deque, visited, false, true);
            return;
        }
        String NPC = nnr.read();
        if (!NPC.isBlank()) {
            savePoints(points - 10);
            click(557, 266);
            parseDestination(deque, NPC, visited);
        } else {
            savePoints(points);
            if (hasDialogueBox()) click(557, 266);
            going = false;
        }
    }

    // parse where to go using text recognition
    private void parseDestination(Deque<Dest> deque, String NPC, Set<String> visited) throws InterruptedException {
        if (terminateFlag) return;
        if (SwitchStatements.parseDestination(NPC, deque)) {
            getOut(visited);
        }
        startMovement(deque, visited);
    }

    private void traveling(Deque<Dest> deque, Set<String> visited) throws InterruptedException {
        String location = "tttc";
        long idleTime = System.currentTimeMillis();
        while (!terminateFlag && !deque.isEmpty()) {
            if (isInBattle()) { // when user is in battle
                progressMatch();
                idleTime = System.currentTimeMillis();
            } else if (location.equals(deque.peek().dest)) { // when user is at the final location
                long time = System.currentTimeMillis();
                if (deque.peek().methodId == 0) {
                    destinationMethod(deque.peek().x, deque.peek().y);
                    return;
                } else if (isAtLocation(deque.peek().x, deque.peek().y)) {
                    Thread.sleep(1000);
                    if (isInBattle()) continue;
                    Dest temp = deque.poll();
                    if (!startMovement(deque, visited)) {
                        deque.push(temp);
                    }
                } else if (time - idleTime >= 50000) {
                    useMap(visited, deque.peek().mapX, deque.peek().mapY);
                    idleTime = time;
                }
            } else if (!getLocation().equals(location)) { // when the user is at a new location
                location = getLocation();
                if (!visited.contains(location)) {
                    closeTutorial();
                    visited.add(location);
                }
                idleTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - idleTime >= 50000) { // when the user is idle for too long
                if (deque.peek().methodId == -1) {
                    useMap(visited, deque.peek().mapX, deque.peek().mapY);
                } else if (deque.peek().methodId == 0) {
                    handleIdling(visited, location, deque.peek().x);
                }
                idleTime = System.currentTimeMillis();
            }
            Thread.sleep(400);
        }
    }

    // decide next thing to do
    private boolean startMovement(Deque<Dest> deque, Set<String> visited) throws InterruptedException {
        if (terminateFlag) return true;
        Dest dest = deque.peek();
        if (dest.methodId == -1) {
            useMap(visited, dest.mapX, dest.mapY);
        } else if (dest.methodId == 0) {
            click(651, 268);
        } else if (dest.methodId == 2) {
            boolean arrived = goToTVD();
            if (arrived) dest.methodId = -1;
            return arrived;
        } else {
            goToHTT();
        }
        return true;
    }

    // method for when staying in 1 spot for too long
    private void handleIdling(Set<String> visited, String location, int x) throws InterruptedException {
        if (terminateFlag) return;
        // special map case because you can't open medium map
        if (location.equals("tyl-tkt")) {
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

    private void progressMatch() throws InterruptedException {
        int turn = 0;
        boolean clickedTutorial = false;
        while (!terminateFlag && isInBattle()) {
            // check for tutorial dialogue
            if (!clickedTutorial && (turn == 1 || turn == 2)) {
                while (!terminateFlag && !getPixelColor(378, 90).equals(white)) {
                    if (!isInBattle()) return;
                    Thread.sleep(200);
                }
                if (waitForDialogueBox(2)) {
                    click(557, 266);
                    clickedTutorial = true;
                }
            }
            // wait until the turn is started
            while (!terminateFlag && !getPixelColor(782, 380).equals(moveBar)) {
                if (!isInBattle()) return;
                Thread.sleep(200);
            }
            // move mouse out the way
            mouseMove(270, 566);
            Color color = getPixelColor(231, 201);
            fight.execute(color, turn);
            // wait until the current turn is over
            while (!terminateFlag && (getPixelColor(378, 90).equals(white) || getPixelColor(405, 325).equals(white))) {
                Thread.sleep(200);
            }
            turn++;
        }
    }

    private void destinationMethod(int x, int y) throws InterruptedException {
        long idleTime = System.currentTimeMillis();
        boolean clickedText = false;
        int limit = 30000;
        while (!terminateFlag) {
            if (isInBattle()) {
                progressMatch();
                idleTime = System.currentTimeMillis();
            } else if (hasDialogueBox() && finishQuest()) {
                if (questFailed()) going = false;
                return;
            } else if (isAtLocation(x, y)) {
                Thread.sleep(1000);
                if (!isInBattle() && !hasDialogueBox()) fixFinishQuest();
            } else if (clickedText && System.currentTimeMillis() - idleTime >= 30000) {
                clickRandomLocation(240, 380, 230, 170);
                clickedText = false;
                idleTime = System.currentTimeMillis();
            } else if (!clickedText && System.currentTimeMillis() - idleTime >= limit) {
                click(651, 268);
                clickedText = true;
                limit = 7000;
                idleTime = System.currentTimeMillis();
            }
            Thread.sleep(400);
        }
    }

    // method for when quest gets error
    private void fixFinishQuest() throws InterruptedException {
        if (terminateFlag) return;
        int[] cur = getCoordinates();
        clickOnNpc(SwitchStatements.fixFinishQuest(cur[0], cur[1]));
        waitForDialogueBox(20);
    }

    private boolean finishQuest() throws InterruptedException {
        if (terminateFlag) return true;
        int y = findVT();
        if (y >= 0) {
            click(251, y + 289);
            waitForDialogueBox(30);
            click(557, 266); // click on final text box;
        }
        return y >= 0;
    }

    // method for getting out of truong thanh tieu cuc
    private void getOut(Set<String> visited) throws InterruptedException {
        if (terminateFlag) return;
        if (clan == null) {
            click(730, 443);
            Thread.sleep(2000);
        }
        long start = System.currentTimeMillis();
        int i = 0;
        click(clan != null ? 752 : 651, clan != null ? 512 : 432);
        while (!terminateFlag && !getLocation().equals("kt")) {
            if (i > 0 || System.currentTimeMillis() - start >= 30000) {
                if (i == 0 && hasDialogueBox()) click(557, 266);
                if (i++ % 3 == 2) {
                    click(420, 515);
                } else {
                    click(779, 445);
                }
                Thread.sleep(3000);
            }
            Thread.sleep(1000);
        }
        if (!visited.contains("kt")) {
            closeTutorial();
            visited.add("kt");
        }
    }

    // go to TVD through BKP
    private boolean goToTVD() throws InterruptedException {
        if (terminateFlag) return true;
        do {
            if (!isAtLocation(173, 164)) {
                return false;
            }
            clickOnNpc(131, 229);
        } while (!terminateFlag && !waitForDialogueBox(20));

        click(323, 456);
        while (!getLocation().equals("dnd") && !terminateFlag) {
            Thread.sleep(200);
        }
        click(787, 480);
        return true;
    }

    // go to HTT through TVD
    private void goToHTT() throws InterruptedException {
        if (terminateFlag) return;
        do {
            clickOnNpc(555, 279);
        } while (!terminateFlag && !waitForDialogueBox(20));
        click(259, 286);
    }
}