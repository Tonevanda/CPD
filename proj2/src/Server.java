import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private final int RANK_QUEUE_TIMER_INTERVAL = 5000;

    private final int DISCONNECT_TIMEOUT = 30;

    private final int CONNECTION_CHECK_TIMEOUT = 6;


    private final int CONNECTION_CHECK_INTERVAL = 2;

    private Timer timer = new Timer();

    private MyTimerTask timerTask = new MyTimerTask();


    private final List<ReentrantLock> locks = new ArrayList<>();

    private final List<Card> gameStore = new ArrayList<>();

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
        char gamemode = 'n';

        try {
            while (state != State.QUIT) {
                switch (state) {
                    case AUTHENTICATION -> {
                        player = handleAuthentication(reader, writer);
                        state = State.valueOf(player.getServerState());
                        if(state == State.QUEUE || state == State.GAME){
                            gamemode = 'i';
                            player.resetTimer(timerTask.getTime());
                        }
                    }
                    case MENU -> {
                        gamemode = handleMenu(reader, writer, player);
                        if(gamemode == 'q'){
                            state = State.QUIT;
                            this.currentAuths.remove(player.getName());
                            updateRank(player);
                        }
                        else if(gamemode == 'a' || gamemode == 'b') {
                            manageSimple();
                            state = State.QUEUE;
                            player.resetTimer(this.timerTask.getTime());
                            player.setTimer(0);
                        }

                    }
                    case QUEUE -> {

                        if (this.currentAuths.get(player.getName()).getInGame()) {
                            state = State.GAME;
                            player.setServerState("GAME");
                            player.resetTimer(this.timerTask.getTime());
                            player.setTimer(0);
                        } else if (player.timeChanged(this.timerTask.getTime())){
                            player.ping();
                            write(player.getWriter(), CLEAR_SCREEN.concat("Waiting for game to start. ").concat(Integer.toString(player.getTime()).concat(" seconds have passed")), '1');
                            flush(player.getWriter());
                        }
                        if(isConnectionAlive(reader, player.getDisconnected())){
                            player.resetConnectionTime();
                        }
                    }
                    case GAME -> {
                        if(player.getTimedOut()){
                            this.locks.getFirst().lock();
                            this.currentAuths.remove(player.getName());
                            this.locks.getFirst().unlock();
                            socket.close();
                            state = State.QUIT;
                            updateRank(player);

                        }
                        else if (!this.currentAuths.get(player.getName()).getInGame()){
                            state = State.MENU;
                            player.resetTimer(this.timerTask.getTime());
                        }

                    }

                }
            }
        }catch(SocketException e){
            if(player != null){
                player.setDisconnected(true);
                System.out.println("Client: ".concat(player.getName()).concat(" has disconnected"));
                player.setServerState(state.toString());

                while(player.getDisconnected()){
                    player.timeChanged(timerTask.getTime());
                    System.out.print("");
                    if(player.getTimedOut()){
                        System.out.println("Client: ".concat(player.getName()).concat(" has timed out"));
                        this.locks.getFirst().lock();
                        this.currentAuths.remove(player.getName());
                        this.locks.getFirst().unlock();
                        if(gamemode == 'a' || gamemode == 'i'){
                            this.locks.get(1).lock();
                            removeFromQueue(player.getName(), this.simplePlayers);
                            this.locks.get(1).unlock();
                        }
                        if(gamemode == 'b' || gamemode == 'i'){
                            this.locks.get(2).lock();
                            removeFromQueue(player.getName(), this.rankedPlayers);
                            this.locks.get(2).unlock();
                        }

                        socket.close();
                        updateRank(player);
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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Player player;
        if(this.currentAuths.containsKey(name)) {
            player = this.currentAuths.get(name);
            player.setReader(reader);
            player.setWriter(writer);
            manageSimple();
        }
        else {
            player = new Player(name, rank, writer, reader, TIMER_INTERVAL/1000, CONNECTION_CHECK_INTERVAL, CONNECTION_CHECK_TIMEOUT, DISCONNECT_TIMEOUT, this.timerTask.getTime());
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

            for(int i = 1; i <= Card.getCardsCount(); i++){
                this.gameStore.add(new Card(i));
            }

            this.timer.schedule(this.timerTask, 0, TIMER_INTERVAL);

            Thread.startVirtualThread(()->{

                int previous_time = 0;

                while(true){
                    System.out.print("");
                    if(timerTask.getTime() >= previous_time + RANK_QUEUE_TIMER_INTERVAL/1000|| timerTask.getTime()+RANK_QUEUE_TIMER_INTERVAL/1000 <= previous_time  && this.rankedPlayers.size() >= NUM_PLAYERS){
                        previous_time = timerTask.getTime();
                        manageRanked();
                    }
                }

            });

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

    private List<List<Player>> getRankedPlayers(){
        List<List<Player>> games = new ArrayList<>();

        for(int j = 0; j < this.rankedPlayers.size(); j++) {
            this.rankedPlayers.sort((p1, p2) -> (p2.getTime() - p1.getTime()));
            System.out.println("THE CURRENT RANKED PLAYERS LIST IS:");
            for(Player player : this.rankedPlayers){
                System.out.println(player.getName().concat(": ").concat(Integer.toString(player.getRank())));
            }
            Player player = this.rankedPlayers.get(j);
            List<Player> gamePlayers = new ArrayList<>();
            gamePlayers.add(player);
            this.rankedPlayers.remove(j);

            if (!player.getDisconnected()){
                this.rankedPlayers.sort(Comparator.comparingInt(p ->
                        Math.abs(player.getRank() - p.getRank())
                ));
                for (int i = 0; i < NUM_PLAYERS-1; i++) {
                    Player p = this.rankedPlayers.getFirst();
                    int rankDistance = Math.abs(player.getRank() - p.getRank());
                    if (p.getDisconnected() ||
                            rankDistance - player.getTime() > 0 ||
                            rankDistance - p.getTime() > 0) {
                        break;
                    }
                    gamePlayers.add(p);
                    this.rankedPlayers.removeFirst();
                }
            }

            if(gamePlayers.size() == NUM_PLAYERS) {
                j += NUM_PLAYERS-1;
                games.add(gamePlayers);
            }
            else this.rankedPlayers.addAll(gamePlayers);

        }

        return games;
    }

    private void manageRanked(){
        locks.get(2).lock();
        if(this.rankedPlayers.size() >= NUM_PLAYERS) {
            System.out.println("Managing ranked game");

            List<List<Player>> games = getRankedPlayers();

            locks.get(2).unlock();
            if(!games.isEmpty()){
                for(List<Player> gamePlayers : games){
                    startGame(gamePlayers, 'b');
                }

            }
        }
        else{
            locks.get(2).unlock();
        }
    }

    private void manageSimple(){
        locks.get(1).lock();
        if(this.simplePlayers.size() >= NUM_PLAYERS) {
            System.out.println("Managing simple game");
            // Get the first NUM_PLAYERS players
            List<Player> gamePlayers = new ArrayList<>();
            for (int i = 0; i < NUM_PLAYERS; i++) {
                gamePlayers.add(this.simplePlayers.getFirst());
                this.simplePlayers.removeFirst();
            }

            locks.get(1).unlock();
            // Start the game
            startGame(gamePlayers, 'a');
        }
        else{
            locks.get(1).unlock();
        }
    }

    private void startGame(List<Player> players, char gamemode){

        Thread.startVirtualThread(()->{

            for(Player player : players){
                this.currentAuths.get(player.getName()).setInGame(true);
            }
            Collections.shuffle(players);
            Game game = new Game(players, this.gameStore, gamemode, this.timerTask);
            try {
                game.run();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }


    private void updateRank(Player player){

        if(player.hasPlayerDBInfoChanged()) {

            locks.getFirst().lock();
            String name = player.getName();
            int newRank = player.getRank();
            JSONArray db = loadJson();
            try {
                for (int i = 0; i < db.length(); i++) {
                    if (db.getJSONObject(i).getString("username").equals(name)) {

                        db.getJSONObject(i).put("rank", newRank);
                        System.out.println(db.getJSONObject(i).getInt("rank") + db.getJSONObject(i).getString("username"));
                        saveJson(db);
                        return;
                    }
                }
            } finally {
                locks.getFirst().unlock();
            }

            System.out.println("Can't update rank, because user does not exist");
        }
    }

    private int authenticateClient(String name, String password, PrintWriter writer) throws NoSuchAlgorithmException {
        password = hashPassword(password);
        locks.getFirst().lock();
        try {
            // Load the JSON file
            JSONArray db = loadJson();

            // Check if the name and password are in the database
            for(int i = 0; i < db.length(); i++){
                if(db.getJSONObject(i).getString("username").equals(name) && db.getJSONObject(i).getString("password").equals(password)){
                    if(currentAuths.containsKey(name)){
                        Player player = currentAuths.get(name);
                        if(!player.getTimedOut() && player.getDisconnected()){
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

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());

        String result = "";
        for(byte b : md.digest()){
            result = result.concat(String.format("%02x", b));
        }

        return result;
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