import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Server {

    List<Thread> gameThreads = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    final static int NUM_PLAYERS = 4;
    final static int port = 8080;

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    private void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String name = reader.readLine();

                System.out.println("New client connected: " + name);

                boolean authenticated = authenticateClient(name);

                if(authenticated)
                    players.add(new Player(name, 0));

                if(players.size() == NUM_PLAYERS){

                    addGameThread();

                    // Get the first NUM_PLAYERS players
                    List<Player> gamePlayers = players.subList(0, NUM_PLAYERS);
                    startGame(gamePlayers);
                    
                    // Remove those players from the list
                    players.removeAll(gamePlayers);
                    // TODO: Isto fecha as threads todas, queremos só fechar a thread do jogo que foi criado
                    closeThreads();
                }

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println(new Date());
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void startGame(List<Player> players) {
        Game game = new Game(players);
        game.run();
    }

    // TODO: Need to implement database to authenticate clients
    private boolean authenticateClient(String input){
        // Get name
        String name = input.split(":")[0];

        // Get password
        String password = input.split(":")[1];

        return true;
    }

    private void addGameThread(){
        Thread thread = Thread.startVirtualThread(()-> {
            try{
                Thread.sleep(Duration.ofSeconds(1));
            }
            catch(InterruptedException e){
                System.out.println("Error starting game thread");
            }
        });
        gameThreads.add(thread);
    }

    private void joinThreads() throws InterruptedException {
        for(Thread thread : gameThreads){
            thread.join();
        }
    }

    private void closeThreads(){
        for(Thread thread : gameThreads){
            thread.interrupt();
        }
    }

}