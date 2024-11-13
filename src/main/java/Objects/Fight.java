package Objects;

import Main.CoLong;

import java.awt.*;

public class Fight {
    private static final Color everyColor1 = new Color(239, 239, 15);
    private static final Color everyColor2 = new Color(239, 207, 15);
    private static final Color characterColor1 = new Color(175, 143, 175);
    private static final Color characterColor2 = new Color(206, 146, 207);
    private static final Color newbieColor = new Color(143, 175, 111);
    private static final Color petColor = new Color(111, 207, 215);
    private final int skill;
    private final int newbie;
    private final int pet;
    private final CoLong parent;

    public Fight(int skill, int newbie, int pet, CoLong parent) {
        this.skill = skill;
        this.newbie = newbie;
        this.pet = pet;
        this.parent = parent;
    }
    public void execute(Color color, int turn) throws InterruptedException {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        if (color.equals(everyColor1) || color.equals(everyColor2)) {
            int count = 0;
            do {
                characterAttack();
            } while (count++ < 1 && !parent.waitForPetPrompt(7));
            petAttack();
        } else if (color.equals(newbieColor)) {
            int count = 0;
            do {
                newbieAttack();
            } while (count++ < 1 && !parent.waitForPetPrompt(7));
            petDefense();
        } else if (color.equals(petColor)) {
            defense();
            parent.waitForPetPrompt(10);
            if (turn == 0) petAttack();
            else parent.clickOnNpc(231, 201);
        } else if (color.equals(characterColor1) || color.equals(characterColor2)) {
            int count = 0;
            do {
                if (turn == 0) characterAttack();
                else parent.clickOnNpc(231, 201);
            } while (count++ < 1 && !parent.waitForPetPrompt(7));
            petDefense();
        } else if (r >= 154 && r <= 178 && g >= 191 && g <= 228 && b >= 85 && b <= 121) {
            defense();
            parent.waitForPetPrompt(10);
            parent.clickOnNpc(231, 201);
        } else {
            defense();
            parent.waitForPetPrompt(10);
            petDefense();
        }
    }


    private void characterAttack() throws InterruptedException {
        if (skill != 0) {
            parent.click(375 + skill * 35, 548);
        }
        Thread.sleep(200);
        parent.clickOnNpc(231, 201);
    }

    private void newbieAttack() throws InterruptedException {
        parent.click(375 + newbie * 35, 548);
        Thread.sleep(200);
        parent.clickOnNpc(231, 201);
    }

    private void petAttack() throws InterruptedException {
        if (pet != 0) {
            parent.click(759, 209);
            parent.click(254 + pet * 37, 290);
        }
        Thread.sleep(200);
        parent.clickOnNpc(231, 201);
    }

    private void defense() throws InterruptedException {
        parent.click(760, 292);
    }

    private void petDefense() throws InterruptedException {
        parent.click(760, 246);
    }

}
