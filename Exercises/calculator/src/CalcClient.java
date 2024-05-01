import java.net.*;
import java.util.Scanner;
import java.io.*;
 
/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class CalcClient {
 
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Scanner scanner = new Scanner(System.in);
            String number;
            while (true) {
                System.out.println("Enter a number (or 'exit' to quit):");
                number = scanner.nextLine();

                if (number.equalsIgnoreCase("exit")) {
                    writer.println(number);
                    break;
                }
                writer.println(number);
                String response = reader.readLine();
                System.out.println(response);
            }
            String response = reader.readLine();
            System.out.println(response);
            scanner.close();
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}