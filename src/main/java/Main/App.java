package Main;

import Utilities.AppUtilities;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class App extends AppUtilities {

    public static void main(String[] args) {
        clanMemo = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("app/data/clans.ser"))) {
            clanMemo = (HashMap<Integer, Integer>) ois.readObject();
        } catch (Exception _) {

        }

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        scale = gc.getDefaultTransform().getScaleX();

        JFrame frame = new JFrame("Auto Vận Tiêu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(0, 0, 3, 0));
        frame.add(panel);

        initialize();

        String[] titles = new String[] {"UID", "Kỹ năng", "Tân thủ", "Trợ thủ", "Về phái", "Phái"};

        gbc.gridy = 0;
        Dimension labelDimensions = new Dimension(48, 26);
        for (int i = 0; i < 6; i++) {
            JLabel label = new JLabel(titles[i]);
            label.setPreferredSize(labelDimensions);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = i;
            panel.add(label, gbc);
        }
        gbc.gridx = 6;
        panel.add(new JPanel());

        JPanel clanOverlay = getClanOverlay();
        frame.getLayeredPane().add(clanOverlay, JLayeredPane.PALETTE_LAYER);
        clanOverlay.setVisible(false);

        // add components for all 10 accounts

        for (int i = 0; i < 10; i++) {
            gbc.gridy = i + 1;
            addAccount(panel, i, clanOverlay);
        }

        gbc.gridheight = 2;
        gbc.gridx = 7;

        gbc.gridy = 1;
        panel.add(getHideButton(), gbc);
        gbc.gridy = 3;
        panel.add(getShowButton(), gbc);
        gbc.gridy = 5;
        panel.add(getResetButton(), gbc);
        gbc.gridy = 7;
        panel.add(getPointsButton(frame), gbc);

        frame.pack();
        frame.setVisible(true);
    }

    private static void addAccount(JPanel panel, int i, JPanel overlay) {
        uidFields[i] = new JTextField();
        uidFields[i].setPreferredSize(buttonDimensions);
        uidFields[i].getDocument().addDocumentListener(getAutofill(i));

        gbc.gridx = 0;
        panel.add(uidFields[i], gbc);

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
            button.setFont(skillFont);
            button.setForeground(skillColor);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setPreferredSize(skillDimensions);
            button.setMargin(buttonPadding);
        }

        skillButtons[i] = buttons[0];
        newbieButtons[i] = buttons[1];
        petButtons[i] = buttons[2];
        clanSkillButtons[i] = buttons[3];

        clanButtons[i] = new JButton("S-ĐTK");
        clanButtons[i].setPreferredSize(buttonDimensions);
        clanButtons[i].setMargin(buttonPadding);
        clanButtons[i].addActionListener(e -> {
            clanIndex = i;
            overlay.setVisible(true);
        });

        gbc.gridx = 1;
        panel.add(skillButtons[i], gbc);
        gbc.gridx = 2;
        panel.add(newbieButtons[i], gbc);
        gbc.gridx = 3;
        panel.add(petButtons[i], gbc);
        gbc.gridx = 4;
        panel.add(clanSkillButtons[i], gbc);
        gbc.gridx = 5;
        panel.add(clanButtons[i], gbc);

        startButtons[i] = new JButton("Start");
        startButtons[i].setPreferredSize(buttonDimensions);
        startButtons[i].setMargin(buttonPadding);
        gbc.gridx = 6;
        panel.add(startButtons[i], gbc);
        startButtons[i].addActionListener(e -> startAccount(i));
    }

    private static DocumentListener getAutofill(int i) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    int UID = Integer.parseInt(uidFields[i].getText());
                    if (clanMemo.containsKey(UID)) {
                        int hash = clanMemo.get(UID);
                        skillButtons[i].setText(getButtonText(hash, 0));
                        newbieButtons[i].setText(getButtonText(hash, 1));
                        petButtons[i].setText(getButtonText(hash, 2));
                        clanSkillButtons[i].setText(getButtonText(hash, 3));
                        clanButtons[i].setText(getButtonText(hash, 4));
                    } else {
                        skillButtons[i].setText("F1");
                        newbieButtons[i].setText("F2");
                        petButtons[i].setText("F1");
                        clanSkillButtons[i].setText("F10");
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
        };
    }

    private static void startAccount(int i) {
        if (startButtons[i].getText().equals("Stop")) {
            return;
        }
        String a = uidFields[i].getText();
        if (a.isBlank()) return;

        try {
            int UID = Integer.parseInt(a);

            synchronized (lock) {
                if (!handleMap.containsKey(UID)) {
                    handleMap = getAllWindows();
                    if (!handleMap.containsKey(UID)) return;
                }
            }

            int skill = keyMap.get(skillButtons[i].getText());
            int newbie = keyMap.get(newbieButtons[i].getText());
            int pet = keyMap.get(petButtons[i].getText());
            int clanSkill = keyMap.get(clanSkillButtons[i].getText());
            String clan = clanButtons[i].getText();
            Pair pair = handleMap.get(UID);

            CoLong coLong = new CoLong(skill, newbie, pet, clanSkill, scale,
                    clan, startButtons[i], pair.handle, pair.username);
            startButtons[i].setBackground(runningColor);
            startButtons[i].setText("Stop");
            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    coLong.setTerminateFlag();
                    startButtons[i].removeActionListener(this);
                }
            };
            startButtons[i].addActionListener(actionListener);

            int hash = getHash(skill, newbie, pet, clanSkill, clan);
            if (!clanMemo.containsKey(UID) || clanMemo.get(UID) != hash) {
                clanMemo.put(UID, hash);
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("app/data/clans.ser"))) {
                    oos.writeObject(clanMemo);
                } catch (Exception _) {

                }
            }

            if (!User32.INSTANCE.IsWindowVisible(pair.handle)) {
                User32.INSTANCE.ShowWindow(pair.handle, 8);
                Thread.sleep(500);
            }
            new Thread(coLong::run).start();
        } catch (NumberFormatException _) {
            uidFields[i].setText("");
        } catch (Exception _) {
            startButtons[i].setEnabled(true);
        }
    }
}
