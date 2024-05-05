import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client extends Communication{

    final static int port = 8080;
    final static String hostname = "localhost";

    enum State{
        AUTHENTICATION,
        MENU,

        QUEUE,
        GAME,
        QUIT
    }
 
    public static void main(String[] args) {

        Client client = new Client();
        client.startClient();

    }

    private void startClient(){

        System.out.println(CLEAR_SCREEN.concat("Client started"));

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
                        write(writer, credentials.getFirst());
                        write(writer, credentials.getLast());
                        List<String> response = read(reader, writer);
                        System.out.println(response.getLast());

                        if(response.getFirst().equals("0") || response.getFirst().equals("M")) {
                            state = State.MENU;
                            System.out.println(read(reader, writer).getLast());
                            System.out.println(read(reader, writer).getLast());
                            System.out.println(read(reader, writer).getLast());
                        }
                        else if(response.getFirst().equals("Q")){
                            state = State.QUEUE;

                        }
                        else if(response.getFirst().equals("G")){
                            state = State.GAME;
                        }
                    }
                    case MENU -> {
                        String action = scanner.nextLine();
                        if(action.equalsIgnoreCase("a") || action.equalsIgnoreCase("b")){
                            write(writer, action);
                            state = State.QUEUE;
                        } else if (action.equalsIgnoreCase("q")) {
                            write(writer, action);
                            state = State.QUIT;
                        }
                        else System.out.println("Invalid input! Please Submit A, B or Q.");
                    }
                    case QUEUE -> {
                        List<String> response = read(reader, writer);
                        System.out.println(response.getLast());
                        if(response.getFirst().equals("0")){
                            state = State.GAME;
                        }
                    }
                    case GAME -> {
                        List<String> response = read(reader, writer);
                        System.out.println(response.getLast());
                        if(response.getFirst().equals("0")){
                            String move = scanner.nextLine();
                            write(writer, move);
                        }
                        else if(response.getFirst().equals("1")) {
                            state = State.MENU;
                            System.out.println(read(reader, writer).getLast());
                            System.out.println(read(reader, writer).getLast());
                            System.out.println(read(reader, writer).getLast());
                        }
                    }
                }
            }


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
