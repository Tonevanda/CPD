import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

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
    final int port = 8080;
    final static int NUM_PLAYERS = 2;
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

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, false);

                String response = reader.readLine();

                String name = response.split(":")[0];

                // Get password
                String password = response.split(":")[1];

                System.out.println("New client connected: " + name);

                int rank = authenticateClient(name, password, writer);

                Player player = new Player(name, rank, writer, reader);

                writer.println("Which gamemode do you wish to play?");
                writer.println("A -> Simple   B -> Ranked");
                writer.flush();

                response = reader.readLine();


                if(rank >= 0){
                    if(response.equals("A")) manageSimple(player);
                    else if(response.equals("B")) manageRanked(player);
                }


                // If rank is -1, the user was not authenticated

                System.out.println("Server running");





            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void manageRanked(Player player) throws InterruptedException {
        this.rankedPlayers.add(player);


        this.rankedPlayers.sort((p1, p2) -> (p2.getRank() - p1.getRank()));



        if(this.rankedPlayers.size() >= NUM_PLAYERS){
            List<Player> gamePlayers = new ArrayList<>();
            for (int i  = 0; i < NUM_PLAYERS; i++){
                gamePlayers.add(this.rankedPlayers.getFirst());
                this.rankedPlayers.removeFirst();
            }

            startGame(gamePlayers);
        }
    }

    private void manageSimple(Player player) throws InterruptedException {
        this.simplePlayers.add(player);


        // If we have enough players, start a game
        if(this.simplePlayers.size() >= NUM_PLAYERS){

            // Get the first NUM_PLAYERS players
            List<Player> gamePlayers = new ArrayList<>();
            for (int i  = 0; i < NUM_PLAYERS; i++){
                gamePlayers.add(this.simplePlayers.poll());
            }

            // Start the game
            startGame(gamePlayers);


        }
    }

    private void startGame(List<Player> players) throws InterruptedException {
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


    private synchronized void updateRank(String name, int newRank){
        JSONArray db = loadJson();

        for(int i = 0; i < db.length(); i++){
            if(db.getJSONObject(i).getString("username").equals(name)){

                db.getJSONObject(i).put("rank", newRank);
                System.out.println(db.getJSONObject(i).getInt("rank") + db.getJSONObject(i).getString("username"));
                saveJson(db);
                return;
            }
        }



        System.out.println("Can't update rank, because user does not exist");
    }

    private synchronized int authenticateClient(String name, String password, PrintWriter writer) throws IOException {
        // Load the JSON file
        JSONArray db = loadJson();





        // Check if the name and password are in the database
        for(int i = 0; i < db.length(); i++){
            if(db.getJSONObject(i).getString("username").equals(name) && db.getJSONObject(i).getString("password").equals(password)){
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