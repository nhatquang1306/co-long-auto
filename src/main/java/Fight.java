import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;

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
    private final CoLongMulti parent;

    public Fight(int skill, int newbie, int pet, CoLongMulti parent) {
        this.skill = skill;
        this.newbie = newbie;
        this.pet = pet;
        this.parent = parent;
    }
    public void execute(Color color, int turn) throws InterruptedException, TesseractException {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        if (color.equals(everyColor1) || color.equals(everyColor2)) {
            characterAttack();
            if (!parent.waitForDefensePrompt(2, 7)) {
                parent.click(222, 167);
                parent.waitForDefensePrompt(2, 7);
            }
            petAttack();
        } else if (color.equals(newbieColor)) {
            newbieAttack();
            if (!parent.waitForDefensePrompt(2, 7)) {
                parent.click(222, 167);
                parent.waitForDefensePrompt(2, 7);
            }
            petDefense();
        } else if (color.equals(petColor)) {
            defense();
            parent.waitForDefensePrompt(2, 10);
            if (turn == 0) petAttack();
            else parent.click(222, 167);
        } else if (color.equals(characterColor1) || color.equals(characterColor2)) {
            if (turn == 0) characterAttack();
            else parent.click(222, 167);
            if (!parent.waitForDefensePrompt(2, 7)) {
                parent.click(222, 167);
                parent.waitForDefensePrompt(2, 7);
            }
            petDefense();
        } else if (r >= 154 && r <= 178 && g >= 191 && g <= 228 && b >= 85 && b <= 121) {
            defense();
            parent.waitForDefensePrompt(2, 10);
            parent.click(222, 167);
        } else {
            defense();
            parent.waitForDefensePrompt(2, 10);
            petDefense();
        }
    }


    private void characterAttack() throws InterruptedException {
        if (skill != 0) {
            parent.click(375 + skill * 35, 548);
        }
        Thread.sleep(200);
        parent.click(222, 167);
    }

    private void newbieAttack() throws InterruptedException {
        parent.click(375 + newbie * 35, 548);
        Thread.sleep(200);
        parent.click(222, 167);
    }

    private void petAttack() throws InterruptedException {
        if (pet != 0) {
            parent.click(759, 209);
            parent.click(254 + pet * 37, 290);
        }
        Thread.sleep(200);
        parent.click(222, 167);
    }

    private void defense() throws InterruptedException {
        parent.click(760, 292);
    }

    private void petDefense() throws InterruptedException {
        parent.click(760, 246);
    }

}
