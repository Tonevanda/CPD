import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client {

    final static int port = 8080;
    final static String hostname = "localhost";
 
    public static void main(String[] args) {

        Client client = new Client();
        client.startClient();

    }

    private void startClient(){

        System.out.println("Client started");

        try (Socket socket = new Socket(hostname, port)) {

            // Send credentials to server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            String credentials = getCredentials();
            writer.println(credentials);

            // Read information from server
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String response = reader.readLine();

            System.out.println(response);


        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    // TODO: Hide password input
    private String getCredentials() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your name: ");
        String name = scanner.nextLine();

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        return name + ":" + password;
    }
}
