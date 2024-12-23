package Utilities;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

public abstract class AppUtilities {
    public static double scale = -1;
    public static final GridBagConstraints gbc = new GridBagConstraints();
    public static Map<String, Integer> keyMap;
    public static Set<Integer> functionKeys;
    public static JTextField[] uidFields;
    public static JButton[] skillButtons, newbieButtons, petButtons, clanButtons, clanSkillButtons, startButtons;
    public static Map<Integer, Pair> handleMap;
    public static Map<Integer, Integer> clanMemo;
    public static final String[] skillHashes = new String[] {"Chay", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};
    public static final String[] clanHashes = new String[] {"ĐTK", "S-ĐTK", "LPM", "TYL", "QV", "PTV", "NCP", "", "TĐ", "LHO", "ĐM"};
    public static int clanIndex;
    public static final Dimension skillDimensions = new Dimension(50, 28);
    public static final Font skillFont = new Font("Verdana", Font.BOLD, 14);
    public static final Color skillColor = new Color(0, 120, 0);
    public static final Color runningColor = new Color(144, 238, 144);
    public static final Dimension buttonDimensions = new Dimension(53, 28);
    public static final Insets buttonPadding = new Insets(2, 2, 2, 2);
    public static final Object lock = new Object();
    public static final Object pointLock = new Object();

    public static JPanel getClanOverlay() {
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
        overlay.setBounds(3, 3, 250, 130);
        return overlay;
    }

    public static JButton getShowButton() {
        Image scaledImg = new ImageIcon("app/data/sun.png").getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaledImg));

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            synchronized (lock) {
                handleMap = getAllWindows();
                for (Pair pair : handleMap.values()) {
                    User32.INSTANCE.ShowWindow(pair.handle, 8);
                }
            }
        });
        return button;
    }

    public static JButton getHideButton() {
        Image scaledImg = new ImageIcon("app/data/moon.png").getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaledImg));

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            synchronized (lock) {
                handleMap = getAllWindows();
                for (JTextField uidField : uidFields) {
                    try {
                        int UID = Integer.parseInt(uidField.getText());
                        if (handleMap.containsKey(UID)) {
                            User32.INSTANCE.ShowWindow(handleMap.get(UID).handle, 0);
                        }
                    } catch (Exception _) {

                    }
                }
            }
        });
        return button;
    }

    public static JLabel getCloseButton(JPanel overlay) {
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

    public static JButton getResetButton() {
        Image scaledImg = new ImageIcon("app/data/reset.png").getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(scaledImg));

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            handleMap = getAllWindows();
        });
        return button;
    }

    public static JButton getPointsButton(JFrame frame) {
        Image scaledImg = new ImageIcon("app/data/points.png").getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        JButton pointsButton = new JButton(new ImageIcon(scaledImg));

        JScrollPane pointsPanel = new JScrollPane();
        frame.getLayeredPane().add(pointsPanel, JLayeredPane.MODAL_LAYER);
        pointsPanel.setBorder(new EmptyBorder(2, 2, 2, 0));
        pointsPanel.setBackground(new Color(50, 50, 50));

        JTextArea pointsText = new JTextArea();
        pointsText.setBackground(new Color(50, 50, 50));

        Color textColor = new Color(224, 255, 255);
        pointsText.setForeground(textColor);
        pointsText.setCaretColor(textColor);
        pointsPanel.setViewportView(pointsText);
        pointsPanel.setVisible(false);

        pointsButton.setBorderPainted(false);
        pointsButton.setContentAreaFilled(false);
        pointsButton.addActionListener(e -> {
            displayPoints(pointsPanel, pointsText);
        });
        return pointsButton;
    }

    public static void displayPoints(JScrollPane pointsPanel, JTextArea pointsText) {
        if (pointsPanel.isVisible()) {
            readPointsPanel(pointsPanel, pointsText);
            return;
        }
        synchronized (pointLock) {
            pointsPanel.setBounds(3, 3, 270, 336);
            Map<String, Integer> map = new HashMap<>();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("app/data/points.ser"))) {
                map = (HashMap<String, Integer>) ois.readObject();
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
            pointsText.setText(sb.toString());
            pointsPanel.setVisible(true);
        }
    }

    public static void readPointsPanel(JScrollPane pointsPanel, JTextArea pointsText) {
        Map<String, Integer> map = new HashMap<>();
        String[] accounts = pointsText.getText().split("\n");
        for (String account : accounts) {
            int a = account.indexOf('\t');
            int b = account.lastIndexOf(':');
            if (a == -1 || b == -1) {
                continue;
            }
            int points = 0;
            for (int i = b + 2; i < account.length(); i++) {
                points = points * 10 + (account.charAt(i) - '0');
            }
            map.put(account.substring(a + 1, b), points);
        }
        synchronized (pointLock) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app/data/points.ser"))) {
                oos.writeObject(map);
            } catch (Exception _) {

            }
        }
        pointsPanel.setVisible(false);
    }

    public static Map<Integer, Pair> getAllWindows() {
        User32 user32 = User32.INSTANCE;
        Map<Integer, Pair> res = new HashMap<>();
        user32.EnumWindows((hwnd, arg) -> {
            char[] text = new char[100];
            user32.GetWindowText(hwnd, text, 100);
            String title = new String(text).trim();
            if (title.startsWith("http://colongonline.com") && title.endsWith("Kênh 1)")) {
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

    public static String getButtonText(int hash, int id) {
        int index = (hash >> (id * 4)) & 15;
        return id < 4 ? skillHashes[index] : clanHashes[index];
    }

    public static int getHash(int skill, int newbie, int pet, int clanSkill, String clan) {
        int hash = skill | (newbie << 4) | (pet << 8) | (clanSkill << 12);
        for (int i = 0; i < clanHashes.length; i++) {
            if (clan.equals(clanHashes[i])) {
                hash |= (i << 16);
                break;
            }
        }
        return hash;
    }

    public static Map<String, Integer> getKeyMap() {
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

    public static void initialize() {
        keyMap = getKeyMap();
        functionKeys = Set.of(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
                KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10);
        uidFields = new JTextField[10];
        skillButtons = new JButton[10];
        newbieButtons = new JButton[10];
        petButtons = new JButton[10];
        clanSkillButtons = new JButton[10];
        clanButtons = new JButton[10];
        startButtons = new JButton[10];
        handleMap = getAllWindows();
        gbc.insets = new Insets(3, 3, 0, 0);
    }

    public static class Pair {
        public String username;
        public WinDef.HWND handle;
        public Pair(String username, WinDef.HWND handle) {
            this.username = username;
            this.handle = handle;
        }
    }
}
