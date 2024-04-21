import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Server {

    List<Thread> gameThreads = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    final int port = 8080;
    final static int NUM_PLAYERS = 4;
    final static String dbPath = "./database/database.json";

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

                int rank = authenticateClient(name, socket);

                // If rank is -1, the user was not authenticated
                if(rank >= 0)
                    players.add(new Player(name, rank, socket));

                // If we have enough players, start a game
                if(players.size() == NUM_PLAYERS){

                    // Get the first NUM_PLAYERS players
                    List<Player> gamePlayers = players.subList(0, NUM_PLAYERS);

                    // Start the game
                    startGame(gamePlayers);
                    
                    // Remove those players from the list
                    players.removeAll(gamePlayers);

                    // TODO: Isto fecha as threads todas, queremos só fechar a thread do jogo que foi criado
                    closeThreads();
                }

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println("Fodasse");
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void startGame(List<Player> players) {
        Game game = new Game(players);
        Thread gameThread = Thread.startVirtualThread(game);
        gameThread.start();
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

    private int authenticateClient(String input, Socket socket) throws IOException {
        // Load the JSON file
        JSONArray db = loadJson();

        // Get name
        String name = input.split(":")[0];

        // Get password
        String password = input.split(":")[1];

        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        // Check if the name and password are in the database
        for(int i = 0; i < db.length(); i++){
            if(db.getJSONObject(i).getString("username").equals(name) && db.getJSONObject(i).getString("password").equals(password)){
                writer.println("Successfully authenticated");
                return db.getJSONObject(i).getInt("rank");
            } else if (db.getJSONObject(i).getString("username").equals(name) && !db.getJSONObject(i).getString("password").equals(password)) {
                writer.println("Wrong password");
                return -1;
            }
        }

        // If the name is not in the database, create new user account
        JSONObject user = createUser(name, password);
        db.put(user);
        writer.println("New account has been created");

        // Save the new user account to the database
        saveJson(db);
        System.out.println("Saved new user account to database");

        return 0;
    }

    private JSONObject createUser(String username, String password){
        JSONObject user = new JSONObject();
        user.put("username", username);
        user.put("password", password);
        user.put("rank", 0);
        return user;
    }

    private JSONArray loadJson() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(dbPath)));
            return new JSONArray(content);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private void saveJson(JSONArray json) {
        try {
            Files.write(Paths.get(dbPath), json.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}