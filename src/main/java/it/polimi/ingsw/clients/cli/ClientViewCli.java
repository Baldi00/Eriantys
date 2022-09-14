package it.polimi.ingsw.clients.cli;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.*;

public class ClientViewCli {

    private final Scanner in;

    public ClientViewCli() {
        in = new Scanner(System.in);
    }

    public void println(String string) {
        out.println(string);
    }

    public void print(String string) {
        out.print(string);
    }

    public int nextInt() {
        return in.nextInt();
    }

    public String nextLine() {
        return in.nextLine();
    }

    public void clear(){
        try{
            String operatingSystem = System.getProperty("os.name");

            if(operatingSystem.contains("Windows")){
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "cls");
                Process startProcess = pb.inheritIO().start();
                startProcess.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder("clear");
                Process startProcess = pb.inheritIO().start();

                startProcess.waitFor();
            }
        }catch(InterruptedException | IOException e){
            Thread.currentThread().interrupt();
        }
    }

}
