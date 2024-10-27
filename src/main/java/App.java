import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.platform.win32.WinDef.HWND;


public class App {
    private static Map<String, Integer> keyMap;
    private static Set<Integer> functionKeys;
    private static JTextField[] uidFields;
    private static JTextField[] questCountFields;
    private static JButton[] skillButtons;
    private static JButton[] newbieButtons;
    private static JButton[] petButtons;
    private static JButton[] clanSkillButtons;
    private static JButton[] clanButtons;
    private static JButton[] stopButtons;
    private static JButton[] startButtons;
    private static Map<Integer, Pair> handleMap;
    private static Map<Integer, Integer> clanMemo;
    private static String[] skillHashes = new String[] {"", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};
    private static String[] clanHashes = new String[] {"ĐTK", "S-ĐTK", "LPM", "TYL", "QV", "PTV", "NCP", "", "TĐ", "LHO", "ĐM"};
    private static int clanIndex;
    private static final Font buttonFont = new Font("Verdana", Font.BOLD, 14);
    private static final Color buttonColor = new Color(0, 120, 0);
    private static final Insets buttonPadding = new Insets(2, 2, 2, 2);
    private static final Object lock = new Object();

    public static void main(String[] args) {
        clanMemo = new HashMap<>();
        try (FileInputStream fileInputStream = new FileInputStream("app/tesseract/clans.ser");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            clanMemo = (HashMap<Integer, Integer>) objectInputStream.readObject();
        } catch (Exception _) {

        }

        JFrame frame = new JFrame("Auto Vận Tiêu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(560, 255);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setLayout(new GridLayout(7, 9, 3, 3));
        frame.add(panel);

        initialize();

        String[] titles = new String[] {"UID", "Số Q", "Kỹ năng", "Tân thủ", "Trợ thủ", "Về phái", "Phái"};

        for (int i = 0; i < 7; i++) {
            JLabel label = new JLabel(titles[i]);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label);
        }
        panel.add(new JPanel());
        panel.add(new JPanel());

        JPanel clanOverlay = getClanOverlay();
        frame.getLayeredPane().add(clanOverlay, JLayeredPane.PALETTE_LAYER);
        clanOverlay.setVisible(false);

        // add components for all 5 accounts
        AtomicInteger size = new AtomicInteger(5);
        for (int i = 0; i < 5; i++) {
            addAccount(panel, i, clanOverlay);
        }

        Image plusIcon = new ImageIcon("app/tesseract/plus-icon.png").getImage();
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

        JButton pointsButton = new JButton("Points");
        JScrollPane pointsPanel = new JScrollPane();
        frame.getLayeredPane().add(pointsPanel, JLayeredPane.MODAL_LAYER);
        pointsPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        pointsPanel.setBackground(new Color(50, 50, 50));
        pointsPanel.setVisible(false);
        pointsButton.addActionListener(e -> {
            displayPoints(pointsPanel, frame.getHeight() - 45);
        });
        pointsButton.setMargin(buttonPadding);
        panel.add(pointsButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            handleMap = getAllWindows();
        });
        resetButton.setMargin(buttonPadding);
        panel.add(resetButton);

        plusButton.addActionListener(e -> {
            if (size.get() >= 10) {
                return;
            }
            panel.remove(resetButton);
            panel.remove(pointsButton);
            for (int i = 5; i >= 0; i--) {
                panel.remove(empties[i]);
            }
            panel.remove(plusButton);

            int i = size.getAndIncrement() + 1;
            addAccount(panel, i - 1, clanOverlay);
            frame.setSize(560, 33 * (i + 2) + 3 * (i + 3));
            panel.setLayout(new GridLayout(i + 2, 8, 5, 5));

            panel.add(plusButton);
            for (int j = 0; j < 6; j++) {
                panel.add(empties[j]);
            }
            panel.add(pointsButton);
            panel.add(resetButton);
        });
        frame.setVisible(true);
    }

    private static JLabel getCloseButton(JPanel overlay) {
        JLabel button = new JLabel("X");
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                overlay.setVisible(false);
            }
        });
        button.setForeground(new Color(240, 240, 240));
        button.setBorder(new EmptyBorder(0, 0, 0, 3));
        button.setHorizontalAlignment(SwingConstants.RIGHT);
        button.setVerticalAlignment(SwingConstants.TOP);
        return button;
    }

    private static JPanel getClanOverlay() {
        JPanel overlay = new JPanel();
        for (int i = 0; i < 2; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(new Color(0, 0, 0, 0));
            overlay.add(empty);
        }
        overlay.add(getCloseButton(overlay));
        overlay.setBackground(new Color(0, 0, 0, 200));
        overlay.setBorder(new EmptyBorder(2, 2, 2, 2));
        overlay.setLayout(new GridLayout(5, 4, 2, 2));
        String[] clans = new String[] {"ĐTK", "S-ĐTK", "", "LPM", "TYL", "QV", "PTV", "NCP", "", "TĐ", "LHO", "ĐM"};
        for (String clan : clans) {
            if (clan.isBlank()) {
                JPanel empty = new JPanel();
                empty.setBackground(new Color(0, 0, 0, 0));
                overlay.add(empty);
            } else {
                JButton button = new JButton(clan);
                button.setMargin(buttonPadding);
                button.addActionListener(e -> {
                    overlay.setVisible(false);
                    clanButtons[clanIndex].setText(clan);
                });
                overlay.add(button);
            }
        }
        overlay.setBounds(5, 5, 250, 130);
        return overlay;
    }

    private static void addAccount(JPanel panel, int i, JPanel overlay) {
        uidFields[i] = new JTextField();
        uidFields[i].getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    int UID = Integer.parseInt(uidFields[i].getText());
                    if (clanMemo.containsKey(UID)) {
                        int hash = clanMemo.get(UID);
                        skillButtons[i].setText(getButtonText(hash, 0));
                        newbieButtons[i].setText(getButtonText(hash, 1));
                        petButtons[i].setText(getButtonText(hash, 2));
                        clanButtons[i].setText(getButtonText(hash, 3));
                    } else {
                        skillButtons[i].setText("F1");
                        newbieButtons[i].setText("F2");
                        petButtons[i].setText("F1");
                        clanButtons[i].setText("S-ĐTK");
                    }
                } catch (NumberFormatException _) {

                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        questCountFields[i] = new JTextField("10");

        panel.add(uidFields[i]);
        panel.add(questCountFields[i]);

        JButton[] buttons = new JButton[] {new JButton("F1"), new JButton("F2"), new JButton("F1"), new JButton("F10")};
        for (int j = 0; j < 4; j++) {
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
            button.setFont(buttonFont);
            button.setForeground(buttonColor);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setMargin(buttonPadding);
        }

        skillButtons[i] = buttons[0];
        newbieButtons[i] = buttons[1];
        petButtons[i] = buttons[2];
        clanSkillButtons[i] = buttons[3];

        clanButtons[i] = new JButton("S-ĐTK");
        clanButtons[i].setMargin(buttonPadding);
        clanButtons[i].addActionListener(e -> {
            clanIndex = i;
            overlay.setVisible(true);
        });

        panel.add(skillButtons[i]);
        panel.add(newbieButtons[i]);
        panel.add(petButtons[i]);
        panel.add(clanSkillButtons[i]);
        panel.add(clanButtons[i]);

        stopButtons[i] = new JButton("Stop");
        stopButtons[i].setMargin(buttonPadding);
        startButtons[i] = new JButton("Start");
        startButtons[i].setMargin(buttonPadding);

        panel.add(stopButtons[i]);
        panel.add(startButtons[i]);

        startButtons[i].addActionListener(e -> startAccount(i));
    }

    private static void startAccount(int i) {
        String a = uidFields[i].getText();
        String b = questCountFields[i].getText();
        if (a.isBlank() || b.isBlank()) {
            return;
        }

        try {
            int UID = Integer.parseInt(a);
            int questCount = Integer.parseInt(b);

            if (!handleMap.containsKey(UID)) {
                return;
            } else if (questCount <= 0 || questCount >= 10) {
                questCount = 10;
                questCountFields[i].setText("10");
            }

            int skill = keyMap.get(skillButtons[i].getText());
            int newbie = keyMap.get(newbieButtons[i].getText());
            int pet = keyMap.get(petButtons[i].getText());
            int clanSkill = keyMap.get(clanSkillButtons[i].getText());
            String clan = clanButtons[i].getText();
            Pair pair = handleMap.get(UID);

            CoLong coLong = new CoLong(questCount, skill, newbie, pet, clanSkill, clan, startButtons[i], pair.handle, pair.username);
            startButtons[i].setEnabled(false);
            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    coLong.setTerminateFlag();
                    stopButtons[i].removeActionListener(this);
                }
            };
            stopButtons[i].addActionListener(actionListener);
            if (!clanMemo.containsKey(UID) || !clanMemo.get(UID).equals(clan)) {
                clanMemo.put(UID, getHash(skill, newbie, pet, clan));
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app/tesseract/clans.ser"))) {
                    oos.writeObject(clanMemo);
                } catch (Exception _) {

                }
            }
            new Thread(coLong::run).start();
        } catch (NumberFormatException _) {
            uidFields[i].setText("");
            questCountFields[i].setText("10");
        } catch (Exception _) {
            startButtons[i].setEnabled(true);
        }
    }

    private static void initialize() {
        keyMap = getKeyMap();
        functionKeys = Set.of(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10);
        uidFields = new JTextField[10];
        questCountFields = new JTextField[10];
        skillButtons = new JButton[10];
        newbieButtons = new JButton[10];
        petButtons = new JButton[10];
        clanSkillButtons = new JButton[10];
        clanButtons = new JButton[10];
        stopButtons = new JButton[10];
        startButtons = new JButton[10];
        handleMap = getAllWindows();
    }

    private static Map<Integer, Pair> getAllWindows() {
        User32 user32 = User32.INSTANCE;
        Map<Integer, Pair> res = new HashMap<>();
        user32.EnumWindows((hwnd, arg) -> {
            char[] text = new char[100];
            user32.GetWindowText(hwnd, text, 100);
            String title = new String(text).trim();
            if (title.startsWith("http://colongonline.com")) {
                int UID = 0;
                int index = 24;
                StringBuilder username = new StringBuilder();
                while (index < title.length() && title.charAt(index) != '[') {
                    username.append(title.charAt(index));
                    index++;
                }
                index += 6;
                while (index < title.length() && Character.isDigit(title.charAt(index))) {
                    UID = UID * 10 + Character.getNumericValue(title.charAt(index));
                    index++;
                }
                res.put(UID, new Pair(username.toString(), hwnd));
            }
            return true;
        }, null);
        return res;
    }

    private static void displayPoints(JScrollPane pointsPanel, int height) {
        if (pointsPanel.isVisible()) {
            pointsPanel.setVisible(false);
            return;
        }
        synchronized (lock) {
            pointsPanel.setBounds(5, 5, 300, height);
            Map<String, Integer> map = new HashMap<>();
            try (FileInputStream fileInputStream = new FileInputStream("app/tesseract/points.ser");
                 ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                map = (HashMap<String, Integer>) objectInputStream.readObject();
            } catch (Exception _) {

            }
            StringBuilder sb = new StringBuilder();
            List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
            list.sort(Map.Entry.comparingByValue());
            for (int i = 0; i < list.size(); i++) {
                String username = list.get(i).getKey();
                int points = list.get(i).getValue();
                sb.append(i + 1).append('\t').append(username).append(": ").append(points).append('\n');
            }
            if (!sb.isEmpty()) {
                sb.delete(sb.length() - 1, sb.length());
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setBackground(new Color(50, 50, 50));
            textArea.setForeground(new Color(224, 255, 255));
            pointsPanel.setViewportView(textArea);
            pointsPanel.setVisible(true);
        }
    }

    private static String getButtonText(int hash, int id) {
        int index = (hash >> (id * 4)) & 15;
        return id < 3 ? skillHashes[index] : clanHashes[index];
    }

    private static int getHash(int skill, int newbie, int pet, String clan) {
        int hash = skill | (newbie << 4) | (pet << 8);
        for (int i = 0; i < clanHashes.length; i++) {
            if (clan.equals(clanHashes[i])) {
                hash |= (i << 12);
                break;
            }
        }
        return hash;
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
    private static class Pair {
        String username;
        HWND handle;
        public Pair(String username, HWND handle) {
            this.username = username;
            this.handle = handle;
        }
    }

}
