import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
 
    public static void main(String[] args) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        Client client = new Client();
        client.startClient();

    }

    private void startClient() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {

        System.out.println(CLEAR_SCREEN.concat("Client started"));

        State state = State.AUTHENTICATION;

        // Load client TrustStore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream("src/servertruststore.jks"), "password".toCharArray());

        // Initialize TrustManagerFactory with the TrustStore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Create SSLSocketFactory and SSLSocket
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(hostname, port)) {

            // Write information to server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Read information from server
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Reader terminalReader = new InputStreamReader(System.in);
            Scanner scanner = new Scanner(System.in);

            List<String> gameResponse = new ArrayList<>();

            while(state != State.QUIT) {
                switch (state) {
                    case AUTHENTICATION -> {
                        ArrayList<String> credentials = getCredentials(scanner, terminalReader);
                        write(writer, credentials.getFirst());
                        write(writer, credentials.getLast());
                        List<String> response = read(reader, writer);
                        System.out.println("Response: " + response);
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
                        System.out.println("Response: " + response);
                        System.out.println(response.getLast());
                        if(response.getFirst().equals("0")){
                            state = State.GAME;
                        }
                    }
                    case GAME -> {
                        System.out.print("");
                        if(reader.ready()){
                            List<String> response = read(reader);
                            System.out.println("Response: " + response);
                            if(response.getFirst().equals(Character.toString(TIMER_ENCODE))){
                                write(writer, "", ALIVE_ENCODE);
                            }
                            else if(response.getFirst().equals("0")){
                                gameResponse = response;
                                System.out.println(response.getLast());
                            }
                            else if(response.getFirst().equals("1")){
                                System.out.println(response.getLast());
                                gameResponse.clear();
                                state = State.MENU;
                                System.out.println(read(reader, writer).getLast());
                                System.out.println(read(reader, writer).getLast());
                                System.out.println(read(reader, writer).getLast());
                            }
                            else{
                                System.out.println(response.getLast());
                            }

                        }
                        if(!gameResponse.isEmpty()){

                            if(terminalReader.ready()){
                                write(writer, scanner.nextLine());
                                gameResponse.clear();
                            }
                        }
                    }
                }
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }catch (Exception ex) {
            System.out.println("An unexpected error occurred: " + ex.getMessage());
        }
    }

    private ArrayList<String> getCredentials(Scanner scanner, Reader terminalReader) throws IOException {
        ArrayList<String> credentials = new ArrayList<>();
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();

        credentials.add(name);

        System.out.println("Enter your password: ");
        while(!terminalReader.ready());

        String password = scanner.nextLine();
        System.out.println(CLEAR_SCREEN);

        credentials.add(password);

        return credentials;
    }
}
