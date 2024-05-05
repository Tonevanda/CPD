import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class Server extends Communication{

    List<Player> simplePlayers = new LinkedList<>();
    List<Player> rankedPlayers = new ArrayList<>();
    Map<String, Player> currentAuths = new HashMap<>();


    final int port = 8080;
    final static int NUM_PLAYERS = 2;
    final static String dbPath = "./database/database.json";

    private final int TIMER_INTERVAL = 1000;

    private final int DISCONNECT_TIMEOUT = 10;

    private final int CONNECTION_CHECK_TIMEOUT = 10;


    private final int CONNECTION_CHECK_INTERVAL = 5;


    private final List<ReentrantLock> locks = new ArrayList<>();

    enum State{
        AUTHENTICATION,
        MENU,
        QUEUE,
        GAME,
        QUIT
    }


    public Server(int numLocks){
        for(int i = 0; i < numLocks; i++){
            this.locks.add(new ReentrantLock());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(3);
        server.startServer();
    }

    private void handleClient(Socket socket, BufferedReader reader, PrintWriter writer) throws IOException, InterruptedException{
        State state = State.AUTHENTICATION;
        Player player = null;
        char gamemode = 'N';




        try {
            while (state != State.QUIT) {
                switch (state) {
                    case AUTHENTICATION -> {
                        player = handleAuthentication(reader, writer);
                        state = State.valueOf(player.getServerState());
                        if(state == State.QUEUE){
                            player.getTimerTask().setMode(1);
                        }
                    }
                    case MENU -> {
                        gamemode = handleMenu(reader, writer, player);
                        if(gamemode == 'q'){
                            state = State.QUIT;
                            this.currentAuths.remove(player.getName());
                        }
                        else if(gamemode == 'a') {
                            manageSimple();
                            state = State.QUEUE;
                            player.getTimerTask().setMode(1);
                        }
                        else if(gamemode == 'b'){
                            manageRanked();
                            state = State.QUEUE;
                            player.getTimerTask().setMode(1);

                        }

                    }
                    case QUEUE -> {
                        if(isConnectionAlive(reader, player.getTimerTask().getDisconnected())){
                            player.getTimerTask().resetConnectionTime();
                        }
                        if (this.currentAuths.get(player.getName()).getInGame()) {
                            state = State.GAME;
                            player.getTimerTask().setMode(0);
                        } else if (player.getTimerTask().timeChanged()){
                            write(player.getWriter(), CLEAR_SCREEN.concat("Waiting for game to start. ").concat(Integer.toString(player.getTimerTask().getTime()).concat(" seconds have passed")), '1');
                            flush(player.getWriter());
                        }
                    }
                    case GAME -> {
                        if (!this.currentAuths.get(player.getName()).getInGame()){
                            state = State.MENU;
                        }
                    }

                }
            }
        }catch(SocketException e){
            if(player != null){
                player.getTimerTask().setDisconnected(true);
                System.out.println("Client: ".concat(player.getName()).concat(" has disconnected"));
                player.setServerState(state.toString());

                while(player.getTimerTask().getDisconnected()){
                    System.out.print("");
                    if(player.getTimerTask().getTimedOut()){
                        System.out.println("Client: ".concat(player.getName()).concat(" has timed out"));
                        this.locks.getFirst().lock();
                        this.currentAuths.remove(player.getName());
                        this.locks.getFirst().unlock();
                        if(gamemode == 'a'){
                            this.locks.get(1).lock();
                            removeFromQueue(player.getName(), this.simplePlayers);
                            this.locks.get(1).unlock();
                        }
                        else if(gamemode == 'b'){
                            this.locks.get(2).lock();
                            removeFromQueue(player.getName(), this.rankedPlayers);
                            this.locks.get(2).unlock();
                        }
                        player.closeTimer();
                        socket.close();
                        break;
                    }
                }

            }
        }



    }

    private void removeFromQueue(String playerName, List<Player> queue){
        for(int i = 0; i < queue.size(); i++){
            if(playerName.equals(queue.get(i).getName())){
                queue.remove(i);
                break;
            }
        }
    }

    private Player handleAuthentication(BufferedReader reader, PrintWriter writer) throws IOException {
        // Authenticate the client
        int rank = -1;
        String name = "";
        try {
            while (rank == -1) {
                name = read(reader, writer).getLast();
                String password = read(reader, writer).getLast();
                rank = authenticateClient(name, password, writer);
            }
        }catch(SocketException e){
            throw e;
        }
        Player player;
        if(this.currentAuths.containsKey(name)) {
            player = this.currentAuths.get(name);
            player.setReader(reader);
            player.setWriter(writer);
        }
        else {
            player = new Player(name, rank, writer, reader, TIMER_INTERVAL, CONNECTION_CHECK_INTERVAL, CONNECTION_CHECK_TIMEOUT, DISCONNECT_TIMEOUT);
            currentAuths.put(name, player);
        }

        System.out.println("New client connected: " + name);
        System.out.println("User authenticated: " + name + " with rank: " + rank);

        return player;

    }

    private char handleMenu(BufferedReader reader, PrintWriter writer, Player player) throws IOException{
        // Ask the user which gamemode they want to play
        write(writer, "Which gamemode do you wish to play?");
        write(writer, "A -> Simple   B -> Ranked");
        write(writer, "Press Q if you want to quit");
        flush(writer);
        String response;
        try{
            response = read(reader, writer).getLast().toLowerCase();
        }catch(SocketException e){
            throw e;
        }

        char gamemode = response.charAt(0);
        if(player.getRank() >= 0){
            switch (response) {
                case "a" -> simplePlayers.add(player);
                case "b" -> rankedPlayers.add(player);
            }
        }

        return gamemode;

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

    private List<Player> getGamePlayers(List<Player> queue){
        List<Player> gamePlayers = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            gamePlayers.add(queue.getFirst());
            queue.removeFirst();
        }

        return gamePlayers;
    }
    private void manageRanked() throws InterruptedException {
        locks.get(2).lock();
        if(this.rankedPlayers.size() >= NUM_PLAYERS) {
            System.out.println("Managing ranked game");
            this.rankedPlayers.sort((p1, p2) -> (p2.getRank() - p1.getRank()));

            List<Player> gamePlayers = getGamePlayers(this.rankedPlayers);


            locks.get(2).unlock();
            startGame(gamePlayers);
        }
        else{
            locks.get(2).unlock();
        }
    }

    private void manageSimple() throws InterruptedException {
        locks.get(1).lock();
        if(this.simplePlayers.size() >= NUM_PLAYERS) {
            System.out.println("Managing simple game");
            // Get the first NUM_PLAYERS players
            List<Player> gamePlayers = getGamePlayers(this.simplePlayers);


            locks.get(1).unlock();
            // Start the game
            startGame(gamePlayers);
        }
        else{
            locks.get(1).unlock();
        }
    }

    private void startGame(List<Player> players) throws InterruptedException {

        Thread.startVirtualThread(()->{

            for(Player player : players){
                this.currentAuths.get(player.getName()).setInGame(true);
            }
            Collections.shuffle(players);
            Game game = new Game(players);
            try {
                game.run();
                for(Player player : game.get_players()){
                    this.currentAuths.get(player.getName()).setInGame(false);
                    updateRank(player.getName(), player.getRank());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }


    private void updateRank(String name, int newRank){
        JSONArray db = loadJson();

        locks.getFirst().lock();
        try {
            for(int i = 0; i < db.length(); i++){
                if(db.getJSONObject(i).getString("username").equals(name)){
    
                    db.getJSONObject(i).put("rank", newRank);
                    System.out.println(db.getJSONObject(i).getInt("rank") + db.getJSONObject(i).getString("username"));
                    saveJson(db);
                    break;
                }
            }
        } finally {
            locks.getFirst().unlock();
        }

        System.out.println("Can't update rank, because user does not exist");
    }

    private int authenticateClient(String name, String password, PrintWriter writer){
        locks.getFirst().lock();
        try {
            // Load the JSON file
            JSONArray db = loadJson();

            // Check if the name and password are in the database
            for(int i = 0; i < db.length(); i++){
                if(db.getJSONObject(i).getString("username").equals(name) && db.getJSONObject(i).getString("password").equals(password)){
                    if(currentAuths.containsKey(name)){
                        Player player = currentAuths.get(name);
                        if(!player.getTimerTask().getTimedOut() && player.getTimerTask().getDisconnected()){
                            player.getTimerTask().setMode(0);
                            write(writer, "Successfully authenticated!", player.getServerState().charAt(0));
                            flush(writer);
                            return player.getRank();
                        }
                        write(writer, "User already authenticated", '1');
                        flush(writer);
                        return -1;
                    }
                    write(writer, "Successfully authenticated!", '0');
                    flush(writer);
                    return db.getJSONObject(i).getInt("rank");
                } else if (db.getJSONObject(i).getString("username").equals(name) && !db.getJSONObject(i).getString("password").equals(password)) {
                    write(writer, "Wrong password", '1');
                    flush(writer);
                    return -1;
                }
            }
            // If the name is not in the database, create new user account
            JSONObject user = createUser(name, password);
            db.put(user);
            write(writer, "New account has been created!", '0');
            flush(writer);
        
            // Save the new user account to the database
            saveJson(db);
            System.out.println("Saved new user account to database");
        } finally {
            locks.getFirst().unlock();
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