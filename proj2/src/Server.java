import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Server {

    Queue<Player> simplePlayers = new LinkedList<>();
    List<Player> rankedPlayers = new ArrayList<>();
    Map<String, Player> currentAuths = new HashMap<>();

    final int port = 8080;
    final static int NUM_PLAYERS = 2;
    final static String dbPath = "./database/database.json";
    private final ReentrantLock lock = new ReentrantLock();


    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    private void handleClient(Socket socket, BufferedReader reader, PrintWriter writer) throws IOException, InterruptedException{
        Player player = handleAuthentication(reader, writer);
        handleMenu(socket, reader, writer, player);
    }

    private Player handleAuthentication(BufferedReader reader, PrintWriter writer) throws IOException {
        // Authenticate the client
        int rank = -1;
        String response;
        String name = "";
        while(rank == -1){
            response = reader.readLine();
            name = response;
            String password = reader.readLine();
            rank = authenticateClient(name, password, writer);
        }
        Player player = new Player(name, rank, writer, reader);
        currentAuths.put(name, player);
        System.out.println("New client connected: " + name);
        System.out.println("User authenticated: " + name + " with rank: " + rank);
        return player;
    }

    private void handleMenu(Socket socket, BufferedReader reader, PrintWriter writer, Player player) throws IOException, InterruptedException {
        // Ask the user which gamemode they want to play
        writer.println("Which gamemode do you wish to play?");
        writer.println("A -> Simple   B -> Ranked");
        writer.println("Press Q if you want to quit");
        writer.flush();

        String response = reader.readLine();
        if(player.getRank() >= 0){
            if(response.equals("A")) simplePlayers.add(player);
            else if(response.equals("B")) rankedPlayers.add(player);
            else if (response.equals("Q")) {
                writer.println("Goodbye!");
                writer.flush();
                socket.close();
            }
        }

        if(this.simplePlayers.size() >= NUM_PLAYERS) manageSimple();
        else if(this.rankedPlayers.size() >= NUM_PLAYERS) manageRanked();
    }

    private void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, false);

                Thread.startVirtualThread(()->{
                    try {
                        handleClient(socket, reader, writer);
                    } catch (IOException e) {
                        System.out.println("Error handling authentication: " + e.getMessage());
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                System.out.println("Server running");
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void manageRanked() throws InterruptedException {
        System.out.println("Managing ranked game");
        this.rankedPlayers.sort((p1, p2) -> (p2.getRank() - p1.getRank()));

        List<Player> gamePlayers = new ArrayList<>();
        for (int i  = 0; i < NUM_PLAYERS; i++){
            gamePlayers.add(this.rankedPlayers.getFirst());
            this.rankedPlayers.removeFirst();
        }

        startGame(gamePlayers);
    }

    private void manageSimple() throws InterruptedException {
        System.out.println("Managing simple game");

        // Get the first NUM_PLAYERS players
        List<Player> gamePlayers = new ArrayList<>();
        for (int i  = 0; i < NUM_PLAYERS; i++){
            gamePlayers.add(this.simplePlayers.poll());
        }

        // Start the game
        startGame(gamePlayers);
    }

    private void startGame(List<Player> players) {
        Thread.startVirtualThread(()->{
            Collections.shuffle(players);
            Game game = new Game(players);
            try {
                game.run();
                for(Player player : game.get_players()){
                    updateRank(player.getName(), player.getRank());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }


    private void updateRank(String name, int newRank){
        JSONArray db = loadJson();

        lock.lock();
        try {
            for(int i = 0; i < db.length(); i++){
                if(db.getJSONObject(i).getString("username").equals(name)){
    
                    db.getJSONObject(i).put("rank", newRank);
                    System.out.println(db.getJSONObject(i).getInt("rank") + db.getJSONObject(i).getString("username"));
                    saveJson(db);
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        System.out.println("Can't update rank, because user does not exist");
    }

    private int authenticateClient(String name, String password, PrintWriter writer) throws IOException {
        lock.lock();
        try {
            // Load the JSON file
            JSONArray db = loadJson();

            // Check if the name and password are in the database
            for(int i = 0; i < db.length(); i++){
                if(db.getJSONObject(i).getString("username").equals(name) && db.getJSONObject(i).getString("password").equals(password)){
                    if(currentAuths.containsKey(name)){
                        Player player = currentAuths.get(name);
                        if(player.getTimedOut()){
                            player.setTimedOut(false);
                            return player.getRank();
                        }
                        writer.println("User already authenticated");
                        writer.flush();
                        return -1;
                    }
                    writer.println("Successfully authenticated!");
                    writer.flush();
                    return db.getJSONObject(i).getInt("rank");
                } else if (db.getJSONObject(i).getString("username").equals(name) && !db.getJSONObject(i).getString("password").equals(password)) {
                    writer.println("Wrong password");
                    writer.flush();
                    return -1;
                }
            }
            // If the name is not in the database, create new user account
            JSONObject user = createUser(name, password);
            db.put(user);
            writer.println("New account has been created!");
            writer.flush();
        
            // Save the new user account to the database
            saveJson(db);
            System.out.println("Saved new user account to database");
        } finally {
            lock.unlock();
        }

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