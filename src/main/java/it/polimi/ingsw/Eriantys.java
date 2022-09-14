package it.polimi.ingsw;

import it.polimi.ingsw.clients.cli.ClientControllerCli;
import it.polimi.ingsw.clients.gui.GuiController;
import it.polimi.ingsw.server.Server;

import javax.swing.*;

public class Eriantys {
    public static void main(String[] args) {
        if (args.length == 0) {
            startClientGui();
        } else if (args[0].equals("--server")) {
            if (args.length == 3 && args[1].equals("--port")) {
                int port = Integer.parseInt(args[2]);
                startServer(port);
            } else {
                startServer();
            }
        } else if (args[0].equals("--client")) {
            if (args.length == 2 && args[1].equals("--cli"))
                startClientCli();
            else
                startClientGui();
        }
    }

    private static void startServer() {
        new Thread(new Server()).start();
    }

    private static void startServer(int port) {
        new Thread(new Server(port)).start();
    }

    private static void startClientCli() {
        new Thread(
                new ClientControllerCli()
        ).start();
    }

    private static void startClientGui() {
        SwingUtilities.invokeLater(GuiController::new);
    }
}
