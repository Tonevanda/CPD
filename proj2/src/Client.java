import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client {

    final static int port = 8080;
    final static String hostname = "localhost";

    enum State{
        AUTHENTICATION,
        MENU,
        GAME,
        QUIT
    }
 
    public static void main(String[] args) {

        Client client = new Client();
        client.startClient();

    }

    private void startClient(){

        System.out.println("Client started");

        State state = State.AUTHENTICATION;

        try (Socket socket = new Socket(hostname, port)) {

            // Write information to server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Read information from server
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Scanner scanner = new Scanner(System.in);

            while(state != State.QUIT) {
                switch (state) {
                    case AUTHENTICATION -> {
                        ArrayList<String> credentials = getCredentials(scanner);
                        writer.println(credentials.get(0));
                        writer.println(credentials.get(1));
                        String response = reader.readLine();
                        System.out.println(response);
                        if(response.endsWith("!")) {
                            state = State.MENU;
                            System.out.println(reader.readLine());
                            System.out.println(reader.readLine());
                        }
                    }
                    case MENU -> {
                        String action = scanner.nextLine();
                        if(action.equals("A") || action.equals("B")){
                            writer.println(action);
                            state = State.GAME;
                        } else if (action.equals("Q")) {
                            writer.println(action);
                            state = State.QUIT;
                        }
                        else System.out.println("Invalid input! Please Submit A or B.");
                    }
                    case GAME -> {
                        String response = reader.readLine();
                        System.out.println(response);
                        if(response.endsWith(".")){
                            String move = scanner.nextLine();
                            writer.println(move);
                        }
                        else if(response.endsWith("!")) state = State.MENU;
                    }
                }
            }

            writer.close();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    private ArrayList<String> getCredentials(Scanner scanner) {
        ArrayList<String> credentials = new ArrayList<>();
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();

        credentials.add(name);

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        credentials.add(password);

        return credentials;
    }
}
