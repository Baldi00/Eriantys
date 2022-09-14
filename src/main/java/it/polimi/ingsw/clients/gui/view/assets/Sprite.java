package it.polimi.ingsw.clients.gui.view.assets;

import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;
import it.polimi.ingsw.models.components.Assistant;
import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.components.Tower;
import it.polimi.ingsw.models.components.Wizard;
import it.polimi.ingsw.models.components.characters.CharacterType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a list with all the sprites of the game.
 */
public enum Sprite {
    ISLAND_TYPE_1("wooden_pieces/island1.png"),
    ISLAND_TYPE_2("wooden_pieces/island2.png"),
    ISLAND_TYPE_3("wooden_pieces/island3.png"),

    CLOUD_TYPE_0("wooden_pieces/cloud_card.png"),
    CLOUD_TYPE_1("wooden_pieces/cloud_card_1.png"),
    CLOUD_TYPE_2("wooden_pieces/cloud_card_2.png"),
    CLOUD_TYPE_3("wooden_pieces/cloud_card_3.png"),
    CLOUD_TYPE_4("wooden_pieces/cloud_card_4.png"),

    BLACK_TOWER("wooden_pieces/black_tower.png"),
    WHITE_TOWER("wooden_pieces/white_tower.png"),
    GREY_TOWER("wooden_pieces/grey_tower.png"),

    CYAN_STUDENT("wooden_pieces/student_blue.png"),
    GREEN_STUDENT("wooden_pieces/student_green.png"),
    PINK_STUDENT("wooden_pieces/student_pink.png"),
    RED_STUDENT("wooden_pieces/student_red.png"),
    YELLOW_STUDENT("wooden_pieces/student_yellow.png"),

    CYAN_PROFESSOR("wooden_pieces/teacher_blue.png"),
    GREEN_PROFESSOR("wooden_pieces/teacher_green.png"),
    PINK_PROFESSOR("wooden_pieces/teacher_pink.png"),
    RED_PROFESSOR("wooden_pieces/teacher_red.png"),
    YELLOW_PROFESSOR("wooden_pieces/teacher_yellow.png"),

    WIZARD_KING("wizards/king.png"),
    WIZARD_WITCH("wizards/witch.png"),
    WIZARD_SAGE("wizards/sage.png"),
    WIZARD_DRUID("wizards/druid.png"),

    BOARD("board.jpg"),
    ISLAND_BLOCK("wooden_pieces/deny_island_icon.png"),
    COIN("wooden_pieces/coin.png"),
    BAG("bag.png"),

    ASSISTANT_TURTLE("assistants/assistant_1.png"),
    ASSISTANT_ELEPHANT("assistants/assistant_2.png"),
    ASSISTANT_DOG("assistants/assistant_3.png"),
    ASSISTANT_OCTOPUS("assistants/assistant_4.png"),
    ASSISTANT_SNAKE("assistants/assistant_5.png"),
    ASSISTANT_FOX("assistants/assistant_6.png"),
    ASSISTANT_EAGLE("assistants/assistant_7.png"),
    ASSISTANT_CAT("assistants/assistant_8.png"),
    ASSISTANT_OSTRICH("assistants/assistant_9.png"),
    ASSISTANT_LEOPARD("assistants/assistant_10.png"),

    CHARACTER_COST_INCREMENT("wooden_pieces/character_cost_increment.png"),
    CHAR_DIONYSUS("characters/character_1.jpg"),
    CHAR_ORIFLAMME("characters/character_2.jpg"),
    CHAR_ERMES("characters/character_3.jpg"),
    CHAR_CIRCE("characters/character_4.jpg"),
    CHAR_CENTAUR("characters/character_5.jpg"),
    CHAR_JESTER("characters/character_6.jpg"),
    CHAR_KNIGHT("characters/character_7.jpg"),
    CHAR_GOOMBA("characters/character_8.jpg"),
    CHAR_BARD("characters/character_9.jpg"),
    CHAR_APHRODITE("characters/character_10.jpg"),
    CHAR_THIEF("characters/character_11.jpg"),
    CHAR_DAIRYMAN("characters/character_12.jpg");

    private static final ClassLoader cl = Sprite.class.getClassLoader();
    private static final Map<Sprite, BufferedImage> cachedSprites =
            Collections.synchronizedMap(new EnumMap<>(Sprite.class));
    private static final Map<Sprite, Map<Dimension, BufferedImage>> cachedResizedSprites =
            Collections.synchronizedMap(new EnumMap<>(Sprite.class));
    private final String path;

    Sprite(String path) {
        this.path = path;
    }

    public static Sprite getTower(Tower tower) {
        return valueOf(tower.name() + "_TOWER");
    }

    public static Sprite getStudent(Student student) {
        return valueOf(student.name() + "_STUDENT");
    }

    public static Sprite getIslandType(int islandType) {
        return valueOf("ISLAND_TYPE_" + islandType);
    }

    public static Sprite getCharacter(CharacterType characterType) {
        return valueOf("CHAR_" + characterType);
    }

    public static Sprite getProfessor(Student student) {
        return valueOf(student.name() + "_PROFESSOR");
    }

    public static Sprite getWizard(Wizard wizard) {
        return valueOf("WIZARD_" + wizard.name());
    }

    public static Sprite getAssistant(Assistant assistant) {
        return valueOf("ASSISTANT_" + assistant.name());
    }

    /**
     * @return a BufferedImage object with sprite.
     * @throws MissingResourceException if the resource cannot be found
     * @throws UncheckedIOException     if an error occurred while loading the sprite.
     */
    public BufferedImage getSprite() {
        BufferedImage cachedSprite = getFromCache();
        if (cachedSprite != null)
            return cachedSprite;

        try (InputStream stream = cl.getResourceAsStream(path)) {
            if (stream == null)
                throw new MissingResourceException("Resource cannot be found",
                        Sprite.class.getName(), path);
            BufferedImage bufferedImage = ImageIO.read(stream);
            cacheSprite(bufferedImage);
            return bufferedImage;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param width  the width of the image.
     * @param height the height of the image.
     * @return the sprite with specified dimensions.
     * @throws MissingResourceException if the resource cannot be found
     * @throws UncheckedIOException     if an error occurred while loading the sprite.
     */
    public BufferedImage getSprite(int width, int height) {
        Dimension dimension = new Dimension(width, height);
        BufferedImage cachedSprite = getFromCache(dimension);
        if (cachedSprite != null)
            return cachedSprite;

        BufferedImage bufferedImage = GuiUtils.resize(getSprite(), width, height);
        cacheSprite(dimension, bufferedImage);
        return bufferedImage;
    }

    private BufferedImage getFromCache() {
        return cachedSprites.get(this);
    }

    private BufferedImage getFromCache(Dimension dimension) {
        Map<Dimension, BufferedImage> cachedDimensions = cachedResizedSprites.get(this);
        if (cachedDimensions != null)
            return cachedDimensions.get(dimension);
        return null;
    }

    private void cacheSprite(BufferedImage bufferedImage) {
        cachedSprites.put(this, bufferedImage);
    }

    private void cacheSprite(Dimension dimension, BufferedImage bufferedImage) {
        Map<Dimension, BufferedImage> cachedDimensions = cachedResizedSprites.get(this);
        if (cachedDimensions == null)
            cachedDimensions = new ConcurrentHashMap<>();
        cachedDimensions.put(dimension, bufferedImage);
        cachedResizedSprites.put(this, cachedDimensions);
    }

}
