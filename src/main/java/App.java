import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

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
import com.sun.jna.platform.win32.WinDef.HWND;


public class App {
    private static Map<String, Integer> keyMap;
    private static Set<Integer> functionKeys;
    private static List<JTextField> uidFields;
    private static List<JTextField> questCountFields;
    private static List<JButton> skillButtons;
    private static List<JButton> newbieButtons;
    private static List<JButton> petButtons;
    private static List<JCheckBox> flagButtons;
    private static List<JButton> stopButtons;
    private static List<JButton> startButtons;
    private static Map<Integer, HWND> handleMap;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Auto Vận Tiêu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 285);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 8, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        frame.add(panel);

        initialize();

        panel.add(new JLabel("UID"));
        panel.add(new JLabel("Số Q"));
        panel.add(new JLabel("Kỹ năng"));
        panel.add(new JLabel("Tân thủ"));
        panel.add(new JLabel("Trợ thủ"));
        panel.add(new JLabel("Siêu ĐTK"));
        panel.add(new JPanel());
        panel.add(new JPanel());

        // add components for all 5 accounts
        for (int i = 0; i < 5; i++) {
            addAccount(panel);
        }

        Image plusIcon = new ImageIcon("input/tesseract/plus-icon.png").getImage();
        ImageIcon resizedIcon = new ImageIcon(plusIcon.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH));

        JButton plusButton = new JButton(resizedIcon);
        plusButton.setBorderPainted(false);
        plusButton.setContentAreaFilled(false);
        plusButton.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(plusButton);

        JPanel[] empties = new JPanel[6];
        for (int i = 0; i < 6; i++) {
            empties[i] = new JPanel();
            panel.add(empties[i]);
        }

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            handleMap = getAllWindows();
        });
        panel.add(resetButton);

        plusButton.addActionListener(e -> {
            if (uidFields.size() >= 10) {
                return;
            }
            panel.remove(resetButton);
            for (int i = 5; i >= 0; i--) {
                panel.remove(empties[i]);
            }
            panel.remove(plusButton);

            addAccount(panel);
            frame.setSize(600,35 * (uidFields.size() + 2) + 5 * (uidFields.size() + 1) + 10);
            panel.setLayout(new GridLayout(uidFields.size() + 2, 8, 5, 5));

            panel.add(plusButton);
            for (int i = 0; i < 6; i++) {
                panel.add(empties[i]);
            }
            panel.add(resetButton);
        });

        frame.setVisible(true);
    }
    private static void addAccount(JPanel panel) {
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

        JCheckBox checkbox = new JCheckBox("", true);
        checkbox.setHorizontalAlignment(SwingConstants.CENTER);
        flagButtons.add(checkbox);

        panel.add(skillButtons.get(i));
        panel.add(newbieButtons.get(i));
        panel.add(petButtons.get(i));
        panel.add(flagButtons.get(i));

        stopButtons.add(new JButton("Stop"));
        startButtons.add(new JButton("Start"));

        panel.add(stopButtons.get(i));
        panel.add(startButtons.get(i));

        startButtons.get(i).addActionListener(e -> {
            String a = uidFields.get(i).getText();
            String b = questCountFields.get(i).getText();
            if (a.isBlank() || b.isBlank()) {
                return;
            }

            try {
                int UID = Integer.parseInt(a);
                int questCount = Integer.parseInt(b);

                if (UID <= 1) {
                    uidFields.get(i).setText("");
                    questCountFields.get(i).setText("10");
                    return;
                } else if (questCount <= 0 || questCount >= 10) {
                    questCount = 10;
                    questCountFields.get(i).setText("10");
                }

                int skill = keyMap.get(skillButtons.get(i).getText());
                int newbie = keyMap.get(newbieButtons.get(i).getText());
                int pet = keyMap.get(petButtons.get(i).getText());
                boolean flag = flagButtons.get(i).isSelected();

                startButtons.get(i).setEnabled(false);
                CoLongMulti colong = new CoLongMulti(UID, questCount, skill, newbie, pet, flag, startButtons.get(i), handleMap);
                ActionListener actionListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        colong.setTerminateFlag();
                        stopButtons.get(i).removeActionListener(this);
                    }
                };
                stopButtons.get(i).addActionListener(actionListener);
                colong.start();
            } catch (NumberFormatException e1) {
                uidFields.get(i).setText("");
                questCountFields.get(i).setText("10");
            } catch (Exception _) {
                startButtons.get(i).setEnabled(true);
            }
        });
    }

    private static void initialize() {
        keyMap = getKeyMap();
        functionKeys = Set.of(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10);
        uidFields = new ArrayList<>();
        questCountFields = new ArrayList<>();
        skillButtons = new ArrayList<>();
        newbieButtons = new ArrayList<>();
        petButtons = new ArrayList<>();
        flagButtons = new ArrayList<>();
        stopButtons = new ArrayList<>();
        startButtons = new ArrayList<>();
        handleMap = getAllWindows();
    }

    private static Map<Integer, HWND> getAllWindows() {
        User32 user32 = User32.INSTANCE;
        Map<Integer, HWND> res = new HashMap<>();
        user32.EnumWindows((hwnd, arg) -> {
            char[] text = new char[100];
            user32.GetWindowText(hwnd, text, 100);
            String title = new String(text).trim();
            if (title.startsWith("http://colongonline.com")) {
                int UID = 0;
                int index = 23;
                while (index < title.length() && title.charAt(index) != ':') {
                    index++;
                }
                index += 2;
                while (index < title.length() && Character.isDigit(title.charAt(index))) {
                    UID = UID * 10 + Character.getNumericValue(title.charAt(index));
                    index++;
                }
                res.put(UID, hwnd);
            }
            return true;
        }, null);
        return res;
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
