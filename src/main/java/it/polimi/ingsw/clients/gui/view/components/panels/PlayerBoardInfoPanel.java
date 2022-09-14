package it.polimi.ingsw.clients.gui.view.components.panels;

import it.polimi.ingsw.clients.gui.view.assets.Sprite;
import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;
import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.Tower;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Display relevant infos about a player
 */
public class PlayerBoardInfoPanel extends JPanel {

    private static final Color BG_COLOR = new Color(240, 240, 240);
    private static final Color COIN_COLOR = new Color(175, 75, 0);

    private final JLabel nameLabel;
    private final JLabel towerLabel;
    private final JLabel lastPlayedAssistantLabel;
    private final JLabel[] studentsLabels;
    private final JLabel[] professorsLabels;
    private final JLabel[] hallStatus;
    private final JLabel[] entranceStatus;
    private final JLabel playerCoinsLabel;

    public PlayerBoardInfoPanel(String name, Tower tower, boolean expertMatch) {
        nameLabel = new JLabel();
        towerLabel = new JLabel();
        towerLabel.setBackground(Color.YELLOW);
        lastPlayedAssistantLabel = new JLabel();
        studentsLabels = new JLabel[Student.values().length];
        professorsLabels = new JLabel[Student.values().length];
        hallStatus = new JLabel[Student.values().length];
        entranceStatus = new JLabel[Student.values().length];
        playerCoinsLabel = new JLabel();

        setNickname(name);
        setTower(tower);
        initProfessors();
        initStudents();
        if (expertMatch)
            initPlayerCoins();

        for (Student s : Student.values()) {
            hallStatus[s.ordinal()] = createCounterLabel();
            entranceStatus[s.ordinal()] = createCounterLabel();
        }

        setLayout(new BorderLayout());
        add(createLeftPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.LINE_END);
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(400, 138));
    }

    private JLabel createCounterLabel() {
        JLabel label = new JLabel("0");
        label.setPreferredSize(new Dimension(30, 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel createRightPanel() {
        JPanel towerCoinPanel = getTowerCoinPanel();

        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        JPanel rightPanel = new JPanel(flowLayout);

        rightPanel.add(towerCoinPanel);
        rightPanel.add(lastPlayedAssistantLabel);

        rightPanel.setPreferredSize(new Dimension(200, 130));
        rightPanel.setBackground(BG_COLOR);
        return rightPanel;
    }

    private JPanel createLeftPanel() {
        JPanel studentsPanel = getStudentsPanel();
        JPanel professorsPanel = getProfessorsPanel();
        JPanel hallStatusPanel = getHallStatusPanel();
        JPanel entranceStatusPanel = getEntranceStatusPanel();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(nameLabel, Component.CENTER_ALIGNMENT);
        leftPanel.add(entranceStatusPanel, Component.CENTER_ALIGNMENT);
        leftPanel.add(studentsPanel, Component.CENTER_ALIGNMENT);
        leftPanel.add(hallStatusPanel, Component.CENTER_ALIGNMENT);
        leftPanel.add(professorsPanel, Component.CENTER_ALIGNMENT);

        leftPanel.setPreferredSize(new Dimension(200, 130));
        leftPanel.setBackground(BG_COLOR);
        return leftPanel;
    }

    private JPanel getEntranceStatusPanel() {
        JPanel entranceStatusPanel = new JPanel(new FlowLayout());
        entranceStatusPanel.setPreferredSize(new Dimension(250, 10));
        for (JLabel label : entranceStatus)
            entranceStatusPanel.add(label);
        entranceStatusPanel.setBackground(BG_COLOR);
        return entranceStatusPanel;
    }

    private JPanel getHallStatusPanel() {
        JPanel boardStatusPanel = new JPanel(new FlowLayout());
        boardStatusPanel.setPreferredSize(new Dimension(250, 10));
        for (JLabel label : hallStatus)
            boardStatusPanel.add(label);
        boardStatusPanel.setBackground(BG_COLOR);
        return boardStatusPanel;
    }

    private JPanel getStudentsPanel() {
        JPanel studentsPanel = new JPanel(new FlowLayout());
        studentsPanel.setPreferredSize(new Dimension(250, 30));
        for (JLabel label : studentsLabels)
            studentsPanel.add(label);
        studentsPanel.setBackground(BG_COLOR);
        return studentsPanel;
    }

    private JPanel getProfessorsPanel() {
        JPanel professorsPanel = new JPanel(new FlowLayout());
        professorsPanel.setPreferredSize(new Dimension(250, 30));
        for (JLabel label : professorsLabels)
            professorsPanel.add(label);
        professorsPanel.setBackground(BG_COLOR);
        return professorsPanel;
    }

    private JPanel getTowerCoinPanel() {
        JPanel towerCoinPanel = new JPanel(new FlowLayout());
        towerCoinPanel.setPreferredSize(new Dimension(100, 130));
        towerCoinPanel.add(towerLabel);
        towerCoinPanel.add(playerCoinsLabel);
        towerCoinPanel.setBackground(BG_COLOR);
        return towerCoinPanel;
    }

    private void initProfessors() {
        for (Student student : Student.values()) {
            BufferedImage img = Sprite.getProfessor(student).getSprite(30, 30);
            professorsLabels[student.ordinal()] = new JLabel(new ImageIcon(img));
            professorsLabels[student.ordinal()].setEnabled(false);
        }
    }

    private void initStudents() {
        for (Student student : Student.values()) {
            BufferedImage img = Sprite.getStudent(student).getSprite(30, 30);
            studentsLabels[student.ordinal()] = new JLabel(new ImageIcon(img));
        }
    }

    private void setTower(Tower tower) {
        BufferedImage img = Sprite.getTower(tower).getSprite(75, 75);
        towerLabel.setPreferredSize(new Dimension(100, 100));
        towerLabel.setIcon(new ImageIcon(img));
        towerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        towerLabel.setBackground(Color.RED);
    }

    private void initPlayerCoins() {
        playerCoinsLabel.setText("1 Coin");
        playerCoinsLabel.setForeground(COIN_COLOR);
        playerCoinsLabel.setFont(GuiUtils.DEFAULT_FONT);
    }

    public void setOwnedProfessors(List<Student> professors) {
        for (JLabel label : professorsLabels)
            label.setEnabled(false);
        for (Student professor : professors)
            professorsLabels[professor.ordinal()].setEnabled(true);
    }

    public void setStudents(List<Student> students) {
        for (JLabel label : studentsLabels)
            label.setEnabled(false);
        for (Student student : students)
            studentsLabels[student.ordinal()].setEnabled(true);
    }

    public void setLastPlayedAssistantName(Assistant assistant) {
        BufferedImage img = Sprite.getAssistant(assistant).getSprite(89, 130);
        lastPlayedAssistantLabel.setPreferredSize(new Dimension(100, 130));
        lastPlayedAssistantLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        lastPlayedAssistantLabel.setIcon(new ImageIcon(img));
    }

    public void setHallStatus(Map<Student, Integer> howMany) {
        for (Map.Entry<Student, Integer> t : howMany.entrySet()) {
            hallStatus[t.getKey().ordinal()].setText(Integer.toString(t.getValue()));
        }
    }

    public void setEntranceStatus(Map<Student, Integer> howMany) {
        for (Map.Entry<Student, Integer> t : howMany.entrySet()) {
            entranceStatus[t.getKey().ordinal()].setText(Integer.toString(t.getValue()));
        }
    }

    private void setNickname(String nickname) {
        nameLabel.setText(nickname);
        nameLabel.setFont(new Font("Sans Serif", Font.BOLD, 18));
    }

    public void setPlayerCoins(int amount) {
        String mex = (amount == 1) ? "Moneta" : "Monete";
        playerCoinsLabel.setText(amount + " " + mex);
    }

    public void highlight(boolean highlight) {
        if (highlight)
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 4, true));
        else
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void highlightPlayerName() {
        nameLabel.setForeground(Color.RED);
    }

}
