package Main;

import Utilities.AppUtilities;

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
        frame.setSize(440, 255);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setLayout(new GridLayout(7, 7, 3, 3));
        frame.add(panel);

        initialize();

        String[] titles = new String[] {"UID", "Kỹ năng", "Tân thủ", "Trợ thủ", "Về phái", "Phái"};

        for (int i = 0; i < 6; i++) {
            JLabel label = new JLabel(titles[i]);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label);
        }
        panel.add(new JPanel());

        JPanel clanOverlay = getClanOverlay();
        frame.getLayeredPane().add(clanOverlay, JLayeredPane.PALETTE_LAYER);
        clanOverlay.setVisible(false);

        // add components for all 5 accounts
        AtomicInteger size = new AtomicInteger(5);
        for (int i = 0; i < 5; i++) {
            addAccount(panel, i, clanOverlay);
        }

        Component[] bottomRowCells = new Component[9];
        bottomRowCells[0] = getPlusMinusButton('+');
        bottomRowCells[1] = getPlusMinusButton('-');
        bottomRowCells[4] = getHideButton();
        bottomRowCells[5] = getShowButton();
        bottomRowCells[6] = getPointsButton(frame);
        for (int i = 0; i < 7; i++) {
            if (bottomRowCells[i] == null) {
                bottomRowCells[i] = new JPanel();
            }
            panel.add(bottomRowCells[i]);
        }

        ((JButton)bottomRowCells[0]).addActionListener(e -> {
            if (size.get() >= 10) {
                return;
            }
            for (int i = 6; i >= 0; i--) panel.remove(bottomRowCells[i]);

            int i = size.getAndIncrement() + 1;
            addAccount(panel, i - 1, clanOverlay);
            frame.setSize(440, 33 * (i + 2) + 3 * (i + 3));
            panel.setLayout(new GridLayout(i + 2, 7, 3, 3));

            for (int j = 0; j < 7; j++) panel.add(bottomRowCells[j]);
        });

        ((JButton)bottomRowCells[1]).addActionListener(e -> {
            if (size.get() <= 5) {
                return;
            }
            int i = size.getAndDecrement() - 1;
            removeAccount(panel, i);
            frame.setSize(440, 33 * (i + 2) + 3 * (i + 3));
            panel.setLayout(new GridLayout(i + 2, 7, 3, 3));
        });

        frame.setVisible(true);
    }

    private static void addAccount(JPanel panel, int i, JPanel overlay) {
        uidFields[i] = new JTextField();
        uidFields[i].getDocument().addDocumentListener(getAutofill(i));

        panel.add(uidFields[i]);

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

        startButtons[i] = new JButton("Start");
        startButtons[i].setMargin(buttonPadding);

        panel.add(startButtons[i]);

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
            new Thread(coLong::run).start();
        } catch (NumberFormatException _) {
            uidFields[i].setText("");
        } catch (Exception _) {
            startButtons[i].setEnabled(true);
        }
    }

    private static void removeAccount(JPanel panel, int i) {
        panel.remove(startButtons[i]);
        panel.remove(clanButtons[i]);
        panel.remove(clanSkillButtons[i]);
        panel.remove(petButtons[i]);
        panel.remove(newbieButtons[i]);
        panel.remove(skillButtons[i]);
        uidFields[i].setText("");
        panel.remove(uidFields[i]);
    }
}
