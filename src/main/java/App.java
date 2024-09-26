import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class App {
    private static Set<Integer> functionKeys;
    public static void main(String[] args) {
        JFrame frame = new JFrame("Auto Vận Tiêu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 205);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 5, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.add(panel);

        List<JTextField> uidFields = new ArrayList<>();
        List<JTextField> questCountFields = new ArrayList<>();
        List<JButton> skillButtons = new ArrayList<>();
        List<JButton> newbieButtons = new ArrayList<>();
        List<JButton> petButtons = new ArrayList<>();
        List<JCheckBox> flagButtons = new ArrayList<>();

        functionKeys = Set.of(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10);

        panel.add(new JLabel("UID"));
        panel.add(new JLabel("Số Q"));
        panel.add(new JLabel("Kỹ năng"));
        panel.add(new JLabel("Tân thủ"));
        panel.add(new JLabel("Trợ thủ"));
        panel.add(new JLabel("Siêu ĐTK"));

        // add components for all 5 accounts
        for (int i = 0; i < 5; i++) {
            addAccount(panel, uidFields, questCountFields, skillButtons, newbieButtons, petButtons, flagButtons);
        }

        Image plusIcon = new ImageIcon("input/tesseract/plus-icon.png").getImage();
        ImageIcon resizedIcon = new ImageIcon(plusIcon.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH));

        JButton plusButton = new JButton(resizedIcon);
        plusButton.setBorderPainted(false);
        plusButton.setContentAreaFilled(false);
        plusButton.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(plusButton);

        JPanel empty1 = new JPanel();
        JPanel empty2 = new JPanel();
        JPanel empty3 = new JPanel();
        panel.add(empty1);
        panel.add(empty2);
        panel.add(empty3);

        JButton stopButton = new JButton("Dừng");
        JButton startButton = new JButton("Bắt đầu");
        startButton.addActionListener(e -> {
            List<Integer> UIDs = new ArrayList<>();
            List<Integer> questCounts = new ArrayList<>();
            List<Integer> skills = new ArrayList<>();
            List<Integer> newbies = new ArrayList<>();
            List<Integer> pets = new ArrayList<>();
            List<Boolean> flags = new ArrayList<>();
            parseAccounts(UIDs, questCounts, skills, newbies, pets, flags,
                    uidFields, questCountFields, skillButtons, newbieButtons, petButtons, flagButtons);
            if (UIDs.isEmpty()) {
                return;
            }
            try {
                startButton.setEnabled(false);
                CoLongMulti colong = new CoLongMulti(UIDs, questCounts, skills, newbies, pets, flags);
                ActionListener actionListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        colong.setTerminateFlag();
                        stopButton.removeActionListener(this);

                    }
                };
                stopButton.addActionListener(actionListener);
                colong.run();
                startButton.setEnabled(true);
            } catch (Exception _) {

            }
        });

        plusButton.addActionListener(e -> {
            if (uidFields.size() >= 10) {
                return;
            }
            panel.remove(startButton);
            panel.remove(stopButton);
            panel.remove(empty3);
            panel.remove(empty2);
            panel.remove(empty1);
            panel.remove(plusButton);
            addAccount(panel, uidFields, questCountFields, skillButtons, newbieButtons, petButtons, flagButtons);
            frame.setSize(600,35 * (uidFields.size() + 2) + 5 * (uidFields.size() + 1) + 10);
            panel.setLayout(new GridLayout(uidFields.size() + 2, 5, 5, 5));
            panel.add(plusButton);
            panel.add(empty1);
            panel.add(empty2);
            panel.add(empty3);
            panel.add(stopButton);
            panel.add(startButton);
        });

        panel.add(stopButton);
        panel.add(startButton);
        frame.setVisible(true);
    }
    private static void addAccount(JPanel panel, List<JTextField> uidFields, List<JTextField> questCountFields, List<JButton> skillButtons,
                                   List<JButton> newbieButtons, List<JButton> petButtons, List<JCheckBox> flagButtons) {
        int i = uidFields.size();
        uidFields.add(new JTextField());
        questCountFields.add(new JTextField("10"));

        panel.add(uidFields.get(i));
        panel.add(questCountFields.get(i));

        JButton[] buttons = new JButton[] {new JButton("F1"), new JButton("F2"), new JButton("F1")};
        for (int j = 0; j < 3; j++) {
            JButton button = buttons[j];
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

        skillButtons.add(buttons[0]);
        newbieButtons.add(buttons[1]);
        petButtons.add(buttons[2]);
        JCheckBox c = new JCheckBox("", true);
        c.setHorizontalAlignment(SwingConstants.CENTER);
        flagButtons.add(c);

        panel.add(skillButtons.get(i));
        panel.add(newbieButtons.get(i));
        panel.add(petButtons.get(i));
        panel.add(flagButtons.get(i));
    }
    private static void parseAccounts(List<Integer> UIDs, List<Integer> questCounts, List<Integer> skills,
                                      List<Integer> newbies, List<Integer> pets, List<Boolean> flags,
                                      List<JTextField> uidFields, List<JTextField> questCountFields, List<JButton> skillButtons,
                                      List<JButton> newbieButtons, List<JButton> petButtons, List<JCheckBox> flagButtons) {
        Map<String, Integer> keyMap = getKeyMap();
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
                flags.add(flagButtons.get(i).isSelected());
            } catch (NumberFormatException _) {
                uidFields.get(i).setText("");
                questCountFields.get(i).setText("10");
            }
        }
    }

    private static Map<String, Integer> getKeyMap() {
        Map<String, Integer> keyMap = new HashMap<>();
        keyMap.put("F1", 1);
        keyMap.put("F2", 2);
        keyMap.put("F3", 3);
        keyMap.put("F4", 4);
        keyMap.put("F5", 5);
        keyMap.put("F6", 6);
        keyMap.put("F7", 7);
        keyMap.put("F8", 8);
        keyMap.put("F9", 9);
        keyMap.put("F10", 10);
        keyMap.put("Chay", 0);
        return keyMap;
    }
}
