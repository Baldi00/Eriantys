package it.polimi.ingsw.clients.gui.view.components.frames.game_frame;

import it.polimi.ingsw.clients.gui.GuiController;
import it.polimi.ingsw.clients.gui.view.components.panels.CommandPanel;
import it.polimi.ingsw.clients.gui.view.components.panels.playing_field.PlayingFieldPanel;
import it.polimi.ingsw.models.state.GameState;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private final PlayingFieldPanel playingFieldPanel;
    private final CommandPanel commandPanel;

    // TODO: absolutely remove controller from this class
    public GameFrame(GameState gs, int playerId, GuiController controller) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int currentWidth = (int) screenSize.getWidth();
        int currentHeight = (int) screenSize.getHeight();
        setSize(currentWidth, currentHeight);

        JPanel container = new JPanel(new BorderLayout());
        container.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        playingFieldPanel = new PlayingFieldPanel(gs, playerId, controller);
        commandPanel = new CommandPanel(gs, playerId, controller);

        container.add(playingFieldPanel, BorderLayout.CENTER);
        container.add(commandPanel, BorderLayout.LINE_END);

        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(container);
        if (currentWidth < WIDTH || currentHeight < HEIGHT) {
            jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        } else {
            jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        jScrollPane.getVerticalScrollBar().setUnitIncrement(1000);
        jScrollPane.getHorizontalScrollBar().setUnitIncrement(1000);

        add(jScrollPane);

        setBackground(Color.white);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        String os = System.getProperty("os.name");
        if (!os.contains("Windows")) {
            GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            dev.setFullScreenWindow(this);
        }
        setVisible(true);
    }

    public void updateView(GameState gameState) {
        playingFieldPanel.updateView(gameState);
        commandPanel.updateView(gameState);
    }
}
