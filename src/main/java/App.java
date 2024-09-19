import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Auto Vận Tiêu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLayout(new GridLayout(7, 5, 5, 5));

        List<Integer> UIDs = new ArrayList<>();
        List<Integer> questCounts = new ArrayList<>();
        List<Integer> skills = new ArrayList<>();
        List<Integer> newbies = new ArrayList<>();
        List<Integer> pets = new ArrayList<>();

        List<JTextField> uidFields = new ArrayList<>();
        List<JTextField> questCountFields = new ArrayList<>();
        List<JButton> skillButtons = new ArrayList<>();
        List<JButton> newbieButtons = new ArrayList<>();
        List<JButton> petButtons = new ArrayList<>();


        Map<String, Integer> keyMap = getKeyMap();
        Set<Integer> functionKeys = Set.of(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8);

        frame.add(new JLabel("UID"));
        frame.add(new JLabel("Số Q"));
        frame.add(new JLabel("Kỹ năng"));
        frame.add(new JLabel("Tân thủ"));
        frame.add(new JLabel("Trợ thủ"));

        // add components for all 5 accounts
        for (int i = 0; i < 5; i++) {
            uidFields.add(new JTextField());
            questCountFields.add(new JTextField("10"));
            frame.add(uidFields.get(i));
            frame.add(questCountFields.get(i));

            skillButtons.add(new JButton("F1"));
            newbieButtons.add(new JButton("F2"));
            petButtons.add(new JButton("F1"));
            frame.add(skillButtons.get(i));
            frame.add(newbieButtons.get(i));
            frame.add(petButtons.get(i));
        }

        // change skill hotkey when pressing the button
        for (int i = 0; i < 15; i++) {
            JButton button;
            if (i % 3 == 0) {
                button = skillButtons.get(i / 3);
            } else if (i % 3 == 1) {
                button = newbieButtons.get((i - 1) / 3);
            } else {
                button = petButtons.get((i - 2) / 3);
            }
            button.addActionListener(e -> {
                KeyAdapter keyAdapter = new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (functionKeys.contains(e.getKeyCode())) {
                            button.setText(KeyEvent.getKeyText(e.getKeyCode()));
                        } else if (e.getKeyCode() == KeyEvent.VK_C) {
                            button.setText("Chay");
                        }
                        button.removeKeyListener(this);
                    }
                };
                button.addKeyListener(keyAdapter);
            });
        }

        frame.add(new JPanel());
        frame.add(new JPanel());
        frame.add(new JPanel());

        JButton saveButton = new JButton("Lưu");
        frame.add(saveButton);
        saveButton.addActionListener(e -> {
            UIDs.clear();
            questCounts.clear();
            for (int i = 0; i < uidFields.size(); i++) {
                String a = uidFields.get(i).getText();
                String b = questCountFields.get(i).getText();
                if (a.isBlank() || b.isBlank()) {
                    continue;
                }
                try {
                    int UID = Integer.parseInt(a);
                    int questCount = Integer.parseInt(b);
                    if (UID <= 1) {
                        uidFields.get(i).setText("");
                        questCountFields.get(i).setText("10");
                        continue;
                    } else if (questCount <= 0 || questCount >= 10) {
                        questCount = 10;
                        questCountFields.get(i).setText("10");
                    }
                    UIDs.add(UID);
                    questCounts.add(questCount);
                    skills.add(keyMap.get(skillButtons.get(i).getText()));
                    newbies.add(keyMap.get(newbieButtons.get(i).getText()));
                    pets.add(keyMap.get(petButtons.get(i).getText()));
                } catch (NumberFormatException _) {
                    uidFields.get(i).setText("");
                    questCountFields.get(i).setText("10");
                }
            }
        });

        JButton startButton = new JButton("Bắt đầu");
        startButton.addActionListener(e -> {
            if (UIDs.isEmpty()) {
                return;
            }
            try {
                CoLongMulti colong = new CoLongMulti(UIDs, questCounts, skills, newbies, pets);
                colong.run();
            } catch (Exception _) {

            }
        });
        frame.add(startButton);
        frame.setVisible(true);
    }

    private static Map<String, Integer> getKeyMap() {
        Map<String, Integer> keyMap = new HashMap<>();
        keyMap.put("F1", KeyEvent.VK_F1);
        keyMap.put("F2", KeyEvent.VK_F2);
        keyMap.put("F3", KeyEvent.VK_F3);
        keyMap.put("F4", KeyEvent.VK_F4);
        keyMap.put("F5", KeyEvent.VK_F5);
        keyMap.put("F6", KeyEvent.VK_F6);
        keyMap.put("F7", KeyEvent.VK_F7);
        keyMap.put("F8", KeyEvent.VK_F8);
        keyMap.put("Chay", 0);
        return keyMap;
    }
}
