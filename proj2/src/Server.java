import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Server {

    List<Thread> gameThreads = new ArrayList<>();
    Queue<Player> players = new LinkedList<>();
    final int port = 8080;
    final static int NUM_PLAYERS = 1;
    final static String dbPath = "./database/database.json";

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    private void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                for(Player player : players){
                    System.out.println(player.getName());
                }
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
                if(players.size() >= NUM_PLAYERS){

                    // Get the first NUM_PLAYERS players
                    List<Player> gamePlayers = new ArrayList<>();
                    for (int i  = 0; i < NUM_PLAYERS; i++){
                        gamePlayers.add(players.poll());
                    }

                    // Start the game
                    startGame(gamePlayers);

                    // TODO: Isto fecha as threads todas, queremos só fechar a thread do jogo que foi criado
                    //closeThreads();
                }
                System.out.println("Server running");

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println("Fodasse");

            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startGame(List<Player> players) throws InterruptedException {
        Thread.startVirtualThread(()->{
            Game game = new Game(players);
            game.run();
            List<Player> game_players = game.get_players();
            for (int i  = 0; i < NUM_PLAYERS; i++){
                this.players.add(game_players.get(i));
            }
        });
    }

    private void joinThreads() throws InterruptedException {
        for(Thread thread : gameThreads){
            thread.join();
        }
    }

    private void closeThreads(){
        System.out.println("Closing threads");
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