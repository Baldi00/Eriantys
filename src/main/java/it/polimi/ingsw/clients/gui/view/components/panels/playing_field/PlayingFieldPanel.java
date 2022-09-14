package it.polimi.ingsw.clients.gui.view.components.panels.playing_field;

import it.polimi.ingsw.clients.ClientController;
import it.polimi.ingsw.clients.gui.view.assets.Sprite;
import it.polimi.ingsw.clients.gui.view.components.custom.StudentCounter;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.events.ClickActions;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.events.ClickEventHandler;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils.BoardTemplate;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils.SceneInfoManager;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils.SceneSprite;
import it.polimi.ingsw.clients.gui.view.utils.*;
import it.polimi.ingsw.models.components.*;
import it.polimi.ingsw.models.components.characters.Character;
import it.polimi.ingsw.models.components.characters.CharacterType;
import it.polimi.ingsw.models.components.characters.effects.EffectArgs;
import it.polimi.ingsw.models.constants.GameConstants;
import it.polimi.ingsw.models.state.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

public class PlayingFieldPanel extends JPanel {
    // TODO: most of these attributes can be obtained from gameState
    private static final int MAX_ISLANDS_PER_TYPE = 4;
    private static final SceneInfoManager SCENE_INFO_MANAGER = SceneInfoManager.getInstance();
    private static final ClickEventHandler CLICK_HANDLER = ClickEventHandler.getInstance();
    private final int[] islandTypes;
    private final Map<SceneSprite, List<StudentCounter>> counters;
    private final JLabel coinReserve;
    private final JLabel bagStock;
    private final int numberOfClouds;
    private final int myId;
    private final boolean isExpertMode;
    // received by the controller
    private transient GameState gameState;
    private int numberOfIslands;
    private int motherNaturePosition;
    private transient BoundingBox selectedSourceItem;
    private transient BoundingBox selectedDestinationItem;

    // TODO: absolutely remove this field and use listeners
    private transient ClientController controller;

    public PlayingFieldPanel(GameState gs, int playerId, ClientController controller) {
        this.controller = controller;

        setLayout(null);
        addMouseListener(new ActionReactor());
        setBackground(Color.WHITE);
        gameState = gs;

        myId = playerId;
        numberOfIslands = gameState.getIslands().size();
        numberOfClouds = gameState.getClouds().size();
        motherNaturePosition = gameState.getMotherNaturePosition();

        coinReserve = new JLabel();
        coinReserve.setFont(GuiUtils.DEFAULT_FONT);
        bagStock = new JLabel();
        bagStock.setFont(GuiUtils.DEFAULT_FONT);

        isExpertMode = gs.isExpertMatch();
        if (isExpertMode) {
            updateCoinStock();
        }

        updateBagStock();

        counters = new EnumMap<>(SceneSprite.class);
        islandTypes = new int[numberOfIslands];
        int[] qty = {MAX_ISLANDS_PER_TYPE, MAX_ISLANDS_PER_TYPE, MAX_ISLANDS_PER_TYPE};

        for (int i = 0; i < numberOfIslands; i++)
            islandTypes[i] = GuiUtils.limitedRand(qty, 0, 3);

        // instantiate labels for the student counters
        for (int i = 0; i < numberOfIslands; i++) {
            counters.put(SceneSprite.getIsland(i + 1), List.of(
                    new StudentCounter(Student.GREEN),
                    new StudentCounter(Student.RED),
                    new StudentCounter(Student.YELLOW),
                    new StudentCounter(Student.PINK),
                    new StudentCounter(Student.CYAN)
            ));
        }

        for (int i = 0; i < numberOfClouds; i++) {
            counters.put(SceneSprite.getCloud(i + 1), List.of(
                    new StudentCounter(Student.GREEN),
                    new StudentCounter(Student.RED),
                    new StudentCounter(Student.YELLOW),
                    new StudentCounter(Student.PINK),
                    new StudentCounter(Student.CYAN)
            ));
        }

        if (isExpertMode) {
            for (int i = 0; i < 3; i++) {
                CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();
                if (characterCardCanAcceptStudents(type)) {
                    counters.put(SceneSprite.getCharacterCard(i + 1), List.of(
                            new StudentCounter(Student.GREEN),
                            new StudentCounter(Student.RED),
                            new StudentCounter(Student.YELLOW),
                            new StudentCounter(Student.PINK),
                            new StudentCounter(Student.CYAN)
                    ));
                }
            }
        }

        updateCountersOnIslands();
        updateCountersOnClouds();
        if (isExpertMode)
            updateCountersOnCharacters();

        setToolTipText("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // for smooth text rendering
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Point boardLocation = new Point();

        if (isExpertMode)
            boardLocation.setLocation((int) Math.floor(0.05 * getWidth()), (int) Math.floor(0.625 * getHeight()));
        else
            boardLocation.setLocation((int) Math.floor(0.17 * getWidth()), (int) Math.floor(0.625 * getHeight()));

        BufferedImage img = Sprite.BOARD.getSprite(900, 390);
        g.drawImage(img, boardLocation.x, boardLocation.y, null);

        // TODO: this doesn't paint anything, try to move this outside paintComponent()
        new BoardTemplate(
                boardLocation.x,
                boardLocation.y,
                900,
                390
        );

        drawConnectionsBetweenIslands(g);
        drawIslands(g);

        drawClouds(g);

        if (isExpertMode) {
            drawCoinStock(g);
            putCoinStockLabel();
            drawCharacterCards(g);
            drawCountersOnCharacters(g);
            drawCostIncrementOnCharacters(g);
            putLabelsOnCharacters();
            disposeBlocksOnIslands(g);
        }

        drawMotherNature(g, motherNaturePosition + 1);
        drawBag(g);
        drawCountersOnIslands(g);
        drawCountersOnClouds(g);
        drawSelectedItemsBoundingBoxes(g);

        putLabelsOnIslands();
        putLabelsOnClouds();
        putLabelOnBag();

        disposeProfessorsOnTable(g);
        disposeTowers(g);
        disposeStudentsOnEntrance(g);
        disposeStudentsInHall(g);
        disposeTowerPlaceholdersOnIslands(g);
    }

    public void updateView(GameState gameState) {
        setGameState(gameState);
        updateRoutine();
        repaint();
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void setStudentCounterText(SceneSprite sceneSprite, Student color, String text) {
        for (StudentCounter label : counters.get(sceneSprite))
            if (label.getColor() == color)
                label.setText(text);
    }

    private void updateRoutine() {
        // in case of merge the number of islands decreases
        numberOfIslands = gameState.getIslands().size();
        if (isExpertMode) {
            updateCoinStock();
            updateCountersOnCharacters();
        }
        updateBagStock();
        updateMotherNaturePosition();
        updateCountersOnIslands();
        updateCountersOnClouds();
    }

    private void updateCoinStock() {
        coinReserve.setText(Integer.toString(gameState.getExpertAttrs().getNumCoinsInStock()));
    }

    private void updateMotherNaturePosition() {
        motherNaturePosition = gameState.getMotherNaturePosition();
    }

    private void updateBagStock() {
        bagStock.setText(Integer.toString(gameState.getBag().getNumStudent()));
    }

    private void updateCountersOnIslands() {
        int numStudents;
        int runner;
        int islandIndex;

        islandIndex = gameState.getIslands().get(0).getPosition();
        for (runner = 0; runner < gameState.getIslands().size(); runner++) {
            int groupDimension = gameState.getIslands().get(runner).getDimension();
            // copy info for every island in the group
            for (int i = 0; i < groupDimension; i++) {
                for (Student s : Student.values()) {
                    numStudents = gameState.getIslands().get(runner).getNumStudent(s);
                    if (numStudents > 0) {
                        setStudentCounterText(SceneSprite.getIsland(islandIndex + 1), s, Integer.toString(numStudents));
                    }
                }
                islandIndex = (islandIndex + 1) % GameConstants.NUMBER_OF_ISLANDS;
            }
        }
    }

    private void updateCountersOnClouds() {
        for (int i = 0; i < numberOfClouds; i++) {
            Cloud cloud = gameState.getClouds().get(i);
            Map<Student, Integer> howMany = new EnumMap<>(Student.class);
            if (!cloud.isEmpty()) {
                List<Student> studentsOnCloud = cloud.pickStudents();
                howMany = GuiUtils.frequenciesFromList(studentsOnCloud);
                // refill cloud
                cloud.receiveStudents(studentsOnCloud);
            } else {
                for (Student s : Student.values()) {
                    howMany.put(s, 0);
                }
            }

            for (Map.Entry<Student, Integer> t : howMany.entrySet()) {
                if (t.getValue() > 0) {
                    setStudentCounterText(SceneSprite.getCloud(i + 1), t.getKey(),
                            Integer.toString(t.getValue()));
                } else {
                    setStudentCounterText(SceneSprite.getCloud(i + 1), t.getKey(), "");
                }
            }
        }
    }

    public void updateCountersOnCharacters() {
        for (int i = 0; i < 3; i++) {
            CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();
            if (characterCardCanAcceptStudents(type)) {
                int numStudentsOnCard = gameState.getExpertAttrs().getCharacterByType(type).getStudents().size();
                if (numStudentsOnCard > 0) {
                    List<Student> studentsOnCard = gameState.getExpertAttrs().getCharacterByType(type).getStudents();
                    Map<Student, Integer> howMany = GuiUtils.frequenciesFromList(studentsOnCard);
                    for (Map.Entry<Student, Integer> t : howMany.entrySet())
                        setStudentCounterText(SceneSprite.getCharacterCard(i + 1), t.getKey(),
                                Integer.toString(t.getValue()));
                } else {
                    // empty card -> all the counters to 0
                    for (Student s : Student.values())
                        setStudentCounterText(SceneSprite.getCharacterCard(i + 1), s, "0");
                }
            }
        }
    }

    private void drawCoinStock(Graphics g) {
        int size = 100;
        addComponentToView(g, SceneSprite.COIN_STOCK_INDICATOR, Sprite.COIN,
                new Point(900, 300), new Dimension(size, size));
    }

    private void drawMotherNature(Graphics g, int islandIndex) {
        BoundingBox islandBbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getIsland(islandIndex));
        g.setColor(Color.ORANGE);
        ((Graphics2D) g).setStroke(new BasicStroke(5));
        g.drawRect(islandBbox.xOffset(), islandBbox.yOffset(),
                islandBbox.width(), islandBbox.height());
        g.setColor(Color.BLACK);
    }

    private void drawTowerPlaceholderOnIsland(Graphics g, int islandIndex, Tower tower) {
        BoundingBox islandBbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getIsland(islandIndex + 1));
        Point pt = new Point();
        final int size = 50;

        pt.setLocation(islandBbox.getCenter().x - size / 2, islandBbox.getCenter().y - size / 2);

        BufferedImage img = Sprite.getTower(tower).getSprite(size, size);
        g.drawImage(img, pt.x, pt.y, size, size, null);
    }

    private void drawBlockPlaceholderOnIsland(Graphics g, int islandIndex) {
        BoundingBox islandBbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getIsland(islandIndex + 1));
        Point pt = new Point();
        final int size = 100;

        pt.setLocation(islandBbox.getCenter().x - size / 2, islandBbox.getCenter().y - size / 2);

        BufferedImage img = Sprite.ISLAND_BLOCK.getSprite(size, size);
        g.drawImage(img, pt.x, pt.y, size, size, null);
    }

    private void drawStudentCountersOnIsland(Graphics g, Island island, int islandSpriteIndex) {
        BoundingBox islandBbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getIsland(islandSpriteIndex + 1));
        Point pt = new Point();
        final int size = 30;
        final int angleSpan = 360 / 5;
        int startingAngle = 18;

        for (int i = 0; i < Student.values().length; i++) {
            Student student = Student.values()[i];

            int numStudents = island.getNumStudent(student);
            int angle = startingAngle + (angleSpan * i);

            Arc2D arc = new Arc2D.Double(
                    islandBbox.xOffset(),
                    islandBbox.yOffset(),
                    islandBbox.width(),
                    islandBbox.height(),
                    angle,
                    angle,
                    Arc2D.OPEN);

            pt.setLocation((int) arc.getStartPoint().getX() - size / 2, (int) arc.getStartPoint().getY() - size / 2);

            if (numStudents > 0) {
                addComponentToView(
                        g,
                        SceneSprite.getIslandStudent(islandSpriteIndex + 1, student),
                        Sprite.getStudent(student),
                        pt,
                        new Dimension(size, size)
                );
            } else {
                addPlaceholderToView(SceneSprite.getIslandStudent(islandSpriteIndex + 1, student));
            }
        }
    }

    private void drawStudentCountersOnCloud(Graphics g, int cloudIndex) {
        Cloud cloud = gameState.getCloudById(cloudIndex - 1);
        BoundingBox cloudBbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getCloud(cloudIndex));
        final int size = 30;

        Map<Student, Integer> howMany = new EnumMap<>(Student.class);
        if (!cloud.isEmpty()) {
            List<Student> studentsOnCloud = cloud.pickStudents();
            howMany = GuiUtils.frequenciesFromList(studentsOnCloud);
            // refill cloud
            cloud.receiveStudents(studentsOnCloud);
        } else {
            for (Student s : Student.values()) {
                howMany.put(s, 0);
            }
        }

        final int angleSpan = 360 / Student.values().length;
        for (int i = 0; i < Student.values().length; ++i) {
            Student student = Student.values()[i];

            int angle = angleSpan * i;

            Arc2D arc = new Arc2D.Double(
                    cloudBbox.xOffset(),
                    cloudBbox.yOffset(),
                    cloudBbox.width(),
                    cloudBbox.height(),
                    angle,
                    angle,
                    Arc2D.OPEN);

            Point position = new Point(
                    (int) arc.getStartPoint().getX() - size / 2,
                    (int) arc.getStartPoint().getY() - size / 2
            );

            if (howMany.get(student) > 0) {
                addComponentToView(g, SceneSprite.getCloudStudent(cloudIndex, student),
                        Sprite.getStudent(student), position, new Dimension(size, size));
            } else {
                addPlaceholderToView(SceneSprite.getCloudStudent(cloudIndex, student));
            }
        }
    }

    private void drawStudentCountersOnCharacter(Graphics g, int charIndex) {
        BoundingBox cardBox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getCharacterCard(charIndex + 1));
        Point pt = new Point();
        final int size = 20;

        int[] angles = new int[]{350, 90, 190, 230, 310};

        for (int i = 0; i < Student.values().length; i++) {
            Student student = Student.values()[i];
            int angle = angles[i];

            Arc2D arc = new Arc2D.Double(
                    cardBox.xOffset(),
                    cardBox.yOffset(),
                    cardBox.width(),
                    cardBox.height(),
                    angle,
                    angle,
                    Arc2D.OPEN);

            pt.setLocation((int) arc.getStartPoint().getX() - size / 2, (int) arc.getStartPoint().getY() - size / 2);

            addComponentToView(
                    g,
                    SceneSprite.getCharacterCardStudent(charIndex + 1, student),
                    Sprite.getStudent(student),
                    pt,
                    new Dimension(size, size)
            );
        }
    }

    private void drawCountersOnIslands(Graphics g) {
        int runner;
        int islandIndex;
        Island island;

        islandIndex = gameState.getIslands().get(0).getPosition();
        for (runner = 0; runner < gameState.getIslands().size(); runner++) {
            int groupDimension = gameState.getIslands().get(runner).getDimension();
            for (int i = 0; i < groupDimension; i++) {
                island = gameState.getIslands().get(runner);
                drawStudentCountersOnIsland(g, island, islandIndex);
                islandIndex = (islandIndex + 1) % GameConstants.NUMBER_OF_ISLANDS;
            }
        }
    }

    private void drawCountersOnClouds(Graphics g) {
        for (int i = 0; i < numberOfClouds; i++)
            drawStudentCountersOnCloud(g, i + 1);
    }

    private boolean characterCardCanAcceptStudents(CharacterType type) {
        return type == CharacterType.APHRODITE || type == CharacterType.DIONYSUS || type == CharacterType.JESTER;
    }

    private void drawCountersOnCharacters(Graphics g) {
        for (int i = 0; i < 3; i++) {
            CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();
            if (characterCardCanAcceptStudents(type))
                drawStudentCountersOnCharacter(g, i);
        }
    }


    private void drawCostIncrementOnCharacters(Graphics g) {
        Point startLocation = new Point(1000, 820);
        double scaleFactor = 0.2;
        // all the character cards have the same size
        int w = (int) (Sprite.CHAR_DAIRYMAN.getSprite().getWidth() * scaleFactor);
        int space = 20;

        for (int i = 0; i < 3; i++) {
            if(gameState.getExpertAttrs().getCharacters().get(i).isCostIncrement()) {
                SceneSprite sprite = switch (i){
                    case 0 -> SceneSprite.CHAR_COST_INCREMENT_1;
                    case 1 -> SceneSprite.CHAR_COST_INCREMENT_2;
                    default -> SceneSprite.CHAR_COST_INCREMENT_3;
                };

                addComponentToView(
                        g,
                        sprite,
                        Sprite.CHARACTER_COST_INCREMENT,
                        new Point(startLocation.x + ((w + space) * i), startLocation.y - 35)
                );
            }
        }
    }

    private void drawSelectedItemsBoundingBoxes(Graphics g) {
        g.setColor(Color.BLUE);
        ((Graphics2D) g).setStroke(new BasicStroke(4));
        if (selectedSourceItem != null) {
            g.drawOval(selectedSourceItem.xOffset() - 5, selectedSourceItem.yOffset() - 5,
                    selectedSourceItem.width() + 10, selectedSourceItem.height() + 10);
        }
        if (selectedDestinationItem != null) {
            g.drawOval(selectedDestinationItem.xOffset() - 5, selectedDestinationItem.yOffset() - 5,
                    selectedDestinationItem.width() + 10, selectedDestinationItem.height() + 10);
        }
        g.setColor(Color.BLACK);
    }

    private void drawClouds(Graphics g) {
        int spriteW;
        int spriteH;
        int startx;
        int starty;

        spriteW = spriteH = 100;
        startx = 300;
        starty = 300;

        for (int i = 0; i < numberOfClouds; i++)
            addComponentToView(g, SceneSprite.getCloud(i + 1),
                    Sprite.CLOUD_TYPE_0,
                    new Point(startx + (50 + spriteW) * i, starty),
                    new Dimension(spriteW, spriteH));
    }

    private void drawIslands(Graphics g) {
        int spriteW = 150;
        int spriteH = 150;

        //                        1   2   3   4   5    6    7    8    9   10   11   12
        int[] angles = new int[]{90, 68, 45, 0, 315, 292, 270, 248, 225, 180, 135, 112};

        for (int i = 0; i < GameConstants.NUMBER_OF_ISLANDS; ++i) {
            int angle = angles[i];
            Arc2D arc2D = new Arc2D.Double(100, 50, 1100, 450, angle, angle, Arc2D.OPEN);
            Point2D point = arc2D.getStartPoint();

            addComponentToView(
                    g,
                    SceneSprite.getIsland(i + 1),
                    Sprite.getIslandType(islandTypes[i] + 1),
                    new Point((int) point.getX(), (int) point.getY()),
                    new Dimension(spriteW, spriteH)
            );
        }
    }

    private void drawCharacterCards(Graphics g) {
        Point startLocation = new Point(1000, 820);
        double scaleFactor = 0.2;
        // all the character cards have the same size
        int w = (int) (Sprite.CHAR_DAIRYMAN.getSprite().getWidth() * scaleFactor);
        int h = (int) (Sprite.CHAR_DAIRYMAN.getSprite().getHeight() * scaleFactor);
        int space = 20;

        for (int i = 0; i < 3; i++) {
            CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();

            addComponentToView(
                    g,
                    SceneSprite.getCharacterCard(i + 1),
                    Sprite.getCharacter(type),
                    new Point(startLocation.x + ((w + space) * i), startLocation.y),
                    new Dimension(w, h)
            );
        }
    }

    private void drawBag(Graphics g) {
        int size = 100;
        SCENE_INFO_MANAGER.addElement(SceneSprite.BAG, 1050, 300, size, size);
        addComponentToView(g, SceneSprite.BAG, Sprite.BAG,
                new Point(1050, 300), new Dimension(size, size));
    }

    private void drawConnectionsBetweenIslands(Graphics g) {
        //                        1   2   3   4   5    6    7    8    9   10   11   12
        int[] angles = new int[]{90, 68, 45, 0, 315, 292, 270, 248, 225, 180, 135, 112};

        ((Graphics2D) g).setStroke(new BasicStroke(5));
        g.setColor(new Color(209, 98, 19));
        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < numberOfIslands; i++) {
            Island island = gameState.getIslands().get(i);
            if (island.getDimension() > 1) {
                int startAngle = angles[island.getPosition()];
                int endAngle = angles[(island.getPosition() + island.getDimension() - 1) % angles.length];
                int angle = startAngle > endAngle ? endAngle - startAngle : -(360 - (endAngle - startAngle));
                Arc2D.Double arc2d = new Arc2D.Double(100.0 + 75.0, 50.0 + 75.0, 1100, 450, startAngle, angle, Arc2D.OPEN);
                ((Graphics2D) g).draw(arc2d);
            }
        }
    }

    private void drawProfessorOnTable(Graphics g, Student student) {
        BoundingBox bbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getBoardProfessor(student));
        Sprite sprite = Sprite.getProfessor(student);
        BufferedImage img = sprite.getSprite(bbox.width(), bbox.height());
        g.drawImage(img, bbox.xOffset(), bbox.yOffset(), bbox.width(), bbox.height(), null);
    }

    private void drawStudentOnEntrance(Graphics g, Student student, int pos) {
        BoundingBox bbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getBoardEntrance(pos));
        Sprite sprite = Sprite.getStudent(student);
        BufferedImage img = sprite.getSprite(bbox.width(), bbox.height());
        g.drawImage(img, bbox.xOffset(), bbox.yOffset(), bbox.width(), bbox.height(), null);
    }

    private void drawTowerInArea(Graphics g, Tower tower, int pos) {
        BoundingBox bbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getBoardTower(pos));
        Sprite sprite = Sprite.getTower(tower);
        BufferedImage img = sprite.getSprite(bbox.width(), bbox.height());
        g.drawImage(img, bbox.xOffset(), bbox.yOffset(), bbox.width(), bbox.height(), null);
    }

    private void drawStudentInHall(Graphics g, Student student, int pos) {
        BoundingBox bbox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.getBoardHall(pos, student));
        Sprite sprite = Sprite.getStudent(student);
        BufferedImage img = sprite.getSprite(bbox.width(), bbox.height());
        g.drawImage(img, bbox.xOffset(), bbox.yOffset(), bbox.width(), bbox.height(), null);
    }

    private void putCoinStockLabel() {
        Point pt = new Point();
        BoundingBox spriteBox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.COIN_STOCK_INDICATOR);
        int size = 30;

        pt.setLocation(
                spriteBox.xOffset() + (spriteBox.width() / 2) - (size / 2),
                spriteBox.yOffset() + spriteBox.height() / 2 - (size / 2)
        );
        coinReserve.setLocation(pt);
        coinReserve.setSize(new Dimension(size, size));
        add(coinReserve);
    }

    private void putLabelOnIsland(SceneSprite sceneSprite, int islandIndex, Student color) {
        BoundingBox spriteBox = SCENE_INFO_MANAGER.getBoundingBox(sceneSprite);
        int size = 30;
        Point pt = new Point(spriteBox.xOffset() + size, spriteBox.yOffset());

        StudentCounter label = null;
        for (StudentCounter l : counters.get(SceneSprite.getIsland(islandIndex)))
            if (l.getColor() == color)
                label = l;

        if (label != null) {
            label.setLocation(pt);
            label.setSize(new Dimension(size, size));
            add(label);
        }
    }

    private void putLabelOnBag() {
        Point pt = new Point();
        BoundingBox spriteBox = SCENE_INFO_MANAGER.getBoundingBox(SceneSprite.BAG);

        pt.setLocation(
                spriteBox.xOffset() + spriteBox.width() / 2,
                spriteBox.yOffset() + spriteBox.height() / 2
        );
        bagStock.setLocation(pt);
        bagStock.setSize(new Dimension(60, 30));
        add(bagStock);
    }

    private void putLabelOnCloud(SceneSprite sceneSprite, int cloudIndex, Student color) {
        Point pt = new Point();
        BoundingBox spriteBox = SCENE_INFO_MANAGER.getBoundingBox(sceneSprite);
        int size = 30;

        pt.setLocation(spriteBox.xOffset() + size, spriteBox.yOffset());
        StudentCounter label = null;
        for (StudentCounter l : counters.get(SceneSprite.getCloud(cloudIndex)))
            if (l.getColor() == color)
                label = l;

        if (label != null) {
            label.setLocation(pt);
            label.setSize(new Dimension(size, size));
            add(label);
        }
    }

    private void putLabelOnCharacter(SceneSprite sceneSprite, int charIndex, Student color) {
        Point pt = new Point();
        BoundingBox spriteBox = SCENE_INFO_MANAGER.getBoundingBox(sceneSprite);
        int size = 20;

        pt.setLocation(spriteBox.xOffset() + 4, spriteBox.yOffset() - size);
        StudentCounter label = null;
        for (StudentCounter l : counters.get(SceneSprite.getCharacterCard(charIndex)))
            if (l.getColor() == color)
                label = l;

        if (label != null) {
            label.setLocation(pt);
            label.setForeground(Color.BLACK);
            label.setSize(new Dimension(size, size));
            add(label);
        }
    }

    private void putLabelsOnIslands() {
        for (int i = 0; i < GameConstants.NUMBER_OF_ISLANDS; i++) {
            for (Student student : Student.values()) {
                int islandIndex = i + 1;
                putLabelOnIsland(SceneSprite.getIslandStudent(islandIndex, student), islandIndex, student);
            }
        }
    }

    private void putLabelsOnClouds() {
        for (int i = 0; i < numberOfClouds; i++) {
            for (Student student : Student.values()) {
                int cloudIndex = i + 1;
                putLabelOnCloud(SceneSprite.getCloudStudent(cloudIndex, student), cloudIndex, student);
            }
        }
    }

    private void putLabelsOnCharacters() {
        for (int i = 0; i < 3; i++) {
            CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();
            if (characterCardCanAcceptStudents(type)) {
                for (Student student : Student.values()) {
                    int charIndex = i + 1;
                    putLabelOnCharacter(SceneSprite.getCharacterCardStudent(charIndex, student), charIndex, student);
                }
            }
        }
    }

    private void disposeStudentsOnEntrance(Graphics g) {
        int j = 1;
        Player player = gameState.getPlayerById(myId);
        Entrance entrance = player.getBoard().getEntrance();
        for (Student color : Student.values()) {
            for (int i = 0; i < entrance.getNumStudentsByColor(color); i++) {
                SCENE_INFO_MANAGER.setEntranceColor(SceneSprite.getBoardEntrance(j), color);
                drawStudentOnEntrance(g, color, j);
                j++;
            }
        }
    }

    private void disposeStudentsInHall(Graphics g) {
        Player p = gameState.getPlayerById(myId);
        for (Student color : Student.values()) {
            int numStudents = p.getBoard().getHall().getNumStudentsByColor(color);
            for (int i = 0; i < numStudents; i++) {
                drawStudentInHall(g, color, i + 1);
            }
        }
    }

    private void disposeProfessorsOnTable(Graphics g) {
        for (Student color : Student.values()) {
            Integer owner = gameState.getProfessorOwner(color);
            if (owner != null && owner.equals(myId))
                drawProfessorOnTable(g, color);
        }
    }

    private void disposeTowers(Graphics g) {
        Player p = gameState.getPlayerById(myId);
        for (int i = 0; i < p.getBoard().getNumTowers(); i++)
            drawTowerInArea(g, p.getBoard().getTowerType(), i + 1);
    }

    private void disposeTowerPlaceholdersOnIslands(Graphics g) {
        int islandIndex = gameState.getIslands().get(0).getPosition();
        for (int i = 0; i < gameState.getIslands().size(); i++) {
            Island isle = gameState.getIslands().get(i);
            if (isle.hasTowers()) {
                int groupDimension = isle.getDimension();
                for (int j = 0; j < groupDimension; j++) {
                    Tower color = isle.getTowerType();
                    drawTowerPlaceholderOnIsland(g, islandIndex, color);
                    islandIndex = (islandIndex + 1) % GameConstants.NUMBER_OF_ISLANDS;
                }
            } else
                islandIndex = (islandIndex + 1) % GameConstants.NUMBER_OF_ISLANDS;
        }
    }

    private void disposeBlocksOnIslands(Graphics g) {
        for (int i = 0; i < gameState.getIslands().size(); i++) {
            Island isle = gameState.getIslands().get(i);
            if (gameState.getExpertAttrs().isIslandBlocked(isle)) {
                drawBlockPlaceholderOnIsland(g, isle.getPosition());
            }
        }
    }

    private class ActionReactor implements MouseListener {
        private int numSwaps;
        private boolean isBardSelected;
        private int bardId;
        private int maxSwaps;
        private final List<Student> swapsFrom;
        private final List<Student> swapsTo;

        public ActionReactor() {
            numSwaps = 0;
            maxSwaps = 3;
            isBardSelected = false;
            swapsFrom = new ArrayList<>();
            swapsTo = new ArrayList<>();
        }

        private ClickActions getActionFromHandler() {
            ClickActions action;
            if(isExpertMode)
                action = CLICK_HANDLER.getAction(gameState.getExpertAttrs().getCharacters());
            else
                action = CLICK_HANDLER.getAction(null);

            return action;
        }

        private boolean isCharacterCardAction(ClickActions action) {
            return action == ClickActions.MOVE_FROM_CHAR_STUDENT_TO_ISLAND ||
                    action == ClickActions.MOVE_FROM_CHAR_STUDENT_TO_HALL ||
                    action == ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE ||
                    action == ClickActions.MOVE_FROM_CHAR_TO_ISLAND ||
                    action == ClickActions.CHAR_CARD_SELECTION;
        }

        private boolean hasBardBeenSelected(SceneSprite source, ClickActions decodedAction) {
            return  decodedAction == ClickActions.CHAR_CARD_SELECTION &&
                    gameState.getExpertAttrs().getCharacters().get(source.getCharacterId()).getCharacterType() == CharacterType.BARD;
        }

        private boolean enoughCoinsToPlayCard(SceneSprite src) {
            Player currentPlayer = controller.getGameState().getCurrentPlayer();
            CharacterType type = gameState.getExpertAttrs().getCharacters()
                    .get(src.getCharacterId()).getCharacterType();
            Character chosenCharacter = gameState.getExpertAttrs().getCharacterByType(type);
            return chosenCharacter.getCost() <= currentPlayer.getNumCoins();
        }

        /**
         * resets the contents of a swap action,
         * regardless of whether it had been completed
         * or not.
         */
        private void resetSwapMove() {
            numSwaps = 0;
            isBardSelected = false;
            swapsFrom.clear();
            swapsTo.clear();
        }

        private EffectArgs prepareEffect(SceneSprite src, SceneSprite dst) {
            EffectArgs.Builder effectBuilder = new EffectArgs.Builder();
            Student studentColor = null;
            Island island = null;
            List<Student> toExchangeFrom = null;
            List<Student> toExchangeTo = null;

            int charId;
            if(isBardSelected)
                charId = bardId;
            else
                charId = src.getCharacterId();

            CharacterType type = gameState.getExpertAttrs().getCharacters()
                    .get(charId).getCharacterType();
            switch (type) {
                case DIONYSUS -> {
                    studentColor = src.getStudentColor();
                    island = gameState.getIslandByPosition(dst.getId());
                }
                case ORIFLAMME, CIRCE -> island = gameState.getIslandByPosition(dst.getId());
                case GOOMBA, THIEF -> {
                    int answer = GuiUtils.option(
                            "Scegli un colore",
                            "Scegli un colore",
                            Stream.of(Student.values()).map(Enum::name).toList()
                    );
                    studentColor = Student.values()[answer];
                }
                case APHRODITE -> studentColor = src.getStudentColor();
                case BARD, JESTER -> {
                    toExchangeFrom = new ArrayList<>(swapsFrom);
                    toExchangeTo = new ArrayList<>(swapsTo);
                }
                default -> {
                    // do nothing
                }
            }

            effectBuilder.setCharacter(gameState.getExpertAttrs().getCharacterByType(type));

            if (island != null)
                effectBuilder.setIsland(island);
            if (studentColor != null)
                effectBuilder.setStudent(studentColor);
            if (toExchangeFrom != null)
                effectBuilder.setSourceStudents(toExchangeFrom);
            if (toExchangeTo != null)
                effectBuilder.setDestStudents(toExchangeTo);

            return effectBuilder.build();
        }

        private void reactToEntranceIsland(SceneSprite src, SceneSprite dst) {
            Student student = SCENE_INFO_MANAGER.getEntranceColor(src);
            int islandId = dst.getId();
            controller.sendStudentToMoveFromEntranceToIslandToServer(student, islandId);
        }

        private void reactToEntranceHall(SceneSprite src) {
            Student student = SCENE_INFO_MANAGER.getEntranceColor(src);
            controller.sendStudentToMoveFromEntranceToHallToServer(student);
        }

        private void reactToMotherNatureMove(SceneSprite src, SceneSprite dst) {
            int sourceIslandId = src.getId();
            int destIslandId = dst.getId();

            if (sourceIslandId != gameState.getMotherNaturePosition()) {
                GuiUtils.alert("Il primo click deve essere su Madre Natura");
            } else {
                int steps = 0;

                try {
                    Island destinationIsland = gameState.getIslandByPosition(destIslandId);
                    Island tempIsland = gameState.getIslandByPosition(sourceIslandId);
                    while (tempIsland.getPosition() != destinationIsland.getPosition()) {
                        steps++;
                        tempIsland = gameState.getIslandByPosition((tempIsland.getPosition() + tempIsland.getDimension()) % GameConstants.NUMBER_OF_ISLANDS);
                    }
                } catch (NoSuchElementException e) {
                    // TODO: handle the exception or write a comment explaining why it's not handled
                    // Unused
                }

                controller.sendMotherNatureStepsToServer(steps);
            }
        }

        private void reactToCloudPick(SceneSprite src) {
            int cloudId = src.getId();
            controller.sendCloudToServer(cloudId);
        }

        private void reactToCharacterCard(EffectArgs effect) {
            controller.sendCharacterToServer(
                    effect.getCharacter().getCharacterType(),
                    effect.getStudent(),
                    effect.getIsland(),
                    effect.getSourceStudents(),
                    effect.getDestStudents()
            );
        }

        private void react(ClickActions action, SceneSprite src, SceneSprite dst) {
            switch (action) {
                case MOVE_STUDENT_FROM_ENTRANCE_TO_ISLAND -> reactToEntranceIsland(src, dst);
                case MOVE_STUDENT_FROM_ENTRANCE_TO_HALL -> reactToEntranceHall(src);
                case MOVE_MOTHER_NATURE -> reactToMotherNatureMove(src, dst);
                case PICK_FROM_CLOUD -> reactToCloudPick(src);
                case MOVE_FROM_CHAR_STUDENT_TO_HALL, SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE, SWAP_STUDENT_FROM_HALL_TO_ENTRANCE,
                        MOVE_FROM_CHAR_STUDENT_TO_ISLAND, MOVE_FROM_CHAR_TO_ISLAND, CHAR_CARD_SELECTION -> {
                    EffectArgs effectData = prepareEffect(src, dst);
                    reactToCharacterCard(effectData);
                }
                default -> GuiUtils.alert("unsupported action");
            }
        }

        private void manageAction(int confirmationChoice, ClickActions action, SceneSprite src, SceneSprite dst) {
            int stopSwapping;

            if (confirmationChoice == JOptionPane.YES_OPTION) {
                if (isExpertMode && hasBardBeenSelected(src, action))
                    maxSwaps = 2;

                if (CLICK_HANDLER.isSwapAction(action)) {
                    numSwaps++;
                    Student from;
                    Student to;

                    from = src.getStudentColor();
                    to = SCENE_INFO_MANAGER.getEntranceColor(dst);

                    if(from != null && to != null) {
                        swapsFrom.add(from);
                        swapsTo.add(to);
                    }

                    // ask the user if he wants to stop swapping
                    stopSwapping = GuiUtils.yesNoAlert("Vuoi continuare a scambiare?");

                    if (stopSwapping == JOptionPane.NO_OPTION || numSwaps == maxSwaps) {
                        react(action, src, dst);
                        resetSwapMove();
                    }
                } else {
                    if (!hasBardBeenSelected(src, action))
                        react(action, src, dst);
                    else {
                        isBardSelected = true;
                        bardId = src.getCharacterId();
                    }
                }
            } else if (action == ClickActions.SWAP_FROM_CHAR_STUDENT_TO_ENTRANCE) {
                resetSwapMove();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (gameState.getCurrentPlayer().getId() == myId) {
                SceneSprite clickedSprite = SceneInfoManager.getInstance().rayCast(new Point(e.getX(), e.getY()));

                if (clickedSprite == null)
                    return;

                drawSelectedItemBoundingBox(clickedSprite);
                CLICK_HANDLER.registerEvent(clickedSprite);
                if (CLICK_HANDLER.hasEnoughEvents()) {
                    ClickActions action = getActionFromHandler();

                    if (action != null) {
                        SceneSprite src = CLICK_HANDLER.getSource();
                        SceneSprite dst = CLICK_HANDLER.getDestination();

                        if (isCharacterCardAction(action) && !enoughCoinsToPlayCard(src)) {
                            GuiUtils.alert("Monete insufficienti");
                        } else {
                            int actionConfirm = GuiUtils.confirm("Confermi l'azione?");
                            manageAction(actionConfirm, action, src, dst);
                        }
                    }
                    CLICK_HANDLER.flushEventQueue();
                    eraseSelectedItemsBoundingBoxes();
                }
            } else {
                GuiUtils.alert("Non è il tuo turno");
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //Unused
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //Unused
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //Unused
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //Unused
        }

        private void drawSelectedItemBoundingBox(SceneSprite sceneSprite) {
            BoundingBox selected = SceneInfoManager.getInstance().getBoundingBox(sceneSprite);
            if (selectedSourceItem == null) {
                selectedSourceItem = selected;
            } else {
                selectedDestinationItem = selected;
            }
            repaint();
        }

        private void eraseSelectedItemsBoundingBoxes() {
            selectedSourceItem = selectedDestinationItem = null;
            repaint();
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (isExpertMode) {
            Point startLocation = new Point(1000, 820);
            double scaleFactor = 0.2;
            // all the character cards have the same size
            int w = (int) (Sprite.CHAR_DAIRYMAN.getSprite().getWidth() * scaleFactor);
            int h = (int) (Sprite.CHAR_DAIRYMAN.getSprite().getHeight() * scaleFactor);
            int space = 20;

            for (int i = 0; i < 3; i++) {
                CharacterType type = gameState.getExpertAttrs().getCharacters().get(i).getCharacterType();

                if (event.getX() >= startLocation.x + ((w + space) * i) &&
                        event.getX() <= startLocation.x + ((w + space) * i) + w &&
                        event.getY() >= startLocation.y &&
                        event.getY() <= startLocation.y + h) {

                    return switch (type) {
                        case DIONYSUS -> "DIONYSUS: Prendi uno studente dalla carta e mettilo su un isola";
                        case DAIRYMAN -> "DAIRYMAN: Prendi controllo dei professori anche in caso di parità";
                        case ORIFLAMME -> "ORIFLAMME: Scegli un'isola su cui calcolare l'influenza";
                        case ERMES -> "ERMES: Puoi muovere madre natura fino a 2 isole addizionali";
                        case CIRCE -> "CIRCE: Piazza una tessera divieto su un isola";
                        case CENTAUR -> "CENTAUR: Durante il calcolo dell'influenza le torri non sono considerate";
                        case JESTER -> "JESTER: Scambia fino a 3 studenti tra carta e ingresso";
                        case KNIGHT -> "KNIGHT: Hai 2 punti influenza addizionali";
                        case GOOMBA -> "GOOMBA: Scegli il colore di uno studente che non fornirà influenza";
                        case BARD -> "BARD: Scambia fino a 2 studenti tra sala e ingresso";
                        case APHRODITE -> "APHRODITE: Prendi uno studente dalla carta e mettilo nella sala";
                        case THIEF ->
                                "THIEF: Scegli il colore di uno studente, tutti i giocatori ne perdono 3 di quel colore";
                    };
                }
            }
        }

        return null;
    }

    /**
     * Draws the sprite on the canvas. When the sprite is drawn a bounding box is
     * associated to the sprite.
     * When a sprite should not be visible on screen use addPlaceholderToView().
     *
     * @param g           canvas on which the sprite is drawn.
     * @param sceneSprite the sceneSprite associated with this sprite.
     * @param sprite      the sprite to draw.
     * @param offset      the offset of the sprite on the canvas.
     * @param dimension   the dimension of the sprite.
     */
    public static void addComponentToView(Graphics g, SceneSprite sceneSprite, Sprite sprite, Point offset, Dimension dimension) {
        BufferedImage img = sprite.getSprite(dimension.width, dimension.height);
        g.drawImage(img, offset.x, offset.y, img.getWidth(), img.getHeight(), null);
        SceneInfoManager.getInstance().addElement(sceneSprite, offset.x, offset.y, dimension.width, dimension.height);
    }

    /**
     * Draws the sprite on the canvas. When the sprite is drawn a bounding box is
     * associated to the sprite.
     * When a sprite should not be visible on screen use addPlaceholderToView().
     *
     * @param g           canvas on which the sprite is drawn.
     * @param sceneSprite the sceneSprite associated with this sprite.
     * @param sprite      the sprite to draw.
     * @param offset      the offset of the sprite on the canvas.
     */
    public static void addComponentToView(Graphics g, SceneSprite sceneSprite, Sprite sprite, Point offset) {
        BufferedImage img = sprite.getSprite();
        g.drawImage(img, offset.x, offset.y, img.getWidth(), img.getHeight(), null);
        SceneInfoManager.getInstance().addElement(sceneSprite, offset.x, offset.y, img.getWidth(), img.getHeight());
    }

    /**
     * Use this when a sprite should not be visible on screen.
     * It's important to call this method instead of doing nothing because is
     * always expected a bounding box associated to a SceneSprite.
     *
     * @param sceneSprite the sceneSprite used to create a dummy bounding box.
     */
    public static void addPlaceholderToView(SceneSprite sceneSprite) {
        SceneInfoManager.getInstance().addElement(sceneSprite, 0, 0, 0, 0);
    }
}
