import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;


/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Server extends Communication{

    List<Player> simplePlayers = new LinkedList<>();
    List<Player> rankedPlayers = new ArrayList<>();
    Map<String, Player> auths = new HashMap<>();


    final int port = 8080;
    final static int NUM_PLAYERS = 2;
    final static String dbPath = "./database/database.json";

    final static int TIMER_INTERVAL = 1000;

    private final int RANK_QUEUE_TIMER_INTERVAL = 5000;

    private final int DB_WRITE_TIMER_INTERVAL = 30000;

    final static int DISCONNECT_TIMEOUT = 30;



    private Timer timer = new Timer();

    private MyTimerTask timerTask = new MyTimerTask();


    private final List<ReentrantLock> locks = new ArrayList<>();

    private final List<Card> gameStore = new ArrayList<>();

    private final List<Card> gameEncounters = new ArrayList<>();

    enum State{
        AUTHENTICATION,
        MENU,
        QUEUE,
        GAME,
        QUIT
    }

    //Initialize the locks of the server
    //First lock is for authentication and writing to database
    //Second lock is for the simple queue
    //Third lock is for the ranked queue
    public Server(int numLocks){
        for(int i = 0; i < numLocks; i++){
            this.locks.add(new ReentrantLock());
        }
    }

    //Initializes the Server Class
    public static void main(String[] args) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        Server server = new Server(3);
        server.startServer();
    }
    //Hanldes the Client by putting him through a state machine, where he has to authenticate, go to menu, queue and then play game,
    //In case of disconnections it is handled gracefully in the SocketException catch
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
                        if(state == State.QUEUE || state == State.GAME || state == State.MENU){
                            if(state == State.QUEUE || state == State.GAME){
                                socket.setSoTimeout(10);
                            }
                            gamemode = 'i';
                            player.resetTimer(timerTask.getTime());
                        }


                    }
                    case MENU -> {
                        gamemode = handleMenu(reader, writer, player);
                        if(gamemode == 'q'){
                            state = State.QUIT;
                            player.setServerState(State.MENU.toString());
                            player.setDisconnected(true);
                        }
                        else if(gamemode == 'a' || gamemode == 'b') {
                            if(gamemode == 'a')manageSimple();
                            state = State.QUEUE;
                            socket.setSoTimeout(10);
                            player.resetTimer(this.timerTask.getTime());
                            player.setTimer(0);
                        }

                    }
                    case QUEUE -> {

                        if (this.auths.get(player.getName()).getInGame()) {
                            state = State.GAME;
                            player.setServerState("GAME");
                            socket.setSoTimeout(10);
                            player.resetTimer(this.timerTask.getTime());
                            player.setTimer(0);
                        } else if (player.timeChanged(this.timerTask.getTime())){
                            write(player.getWriter(), CLEAR_SCREEN.concat("Waiting for game to start. ").concat(Integer.toString(player.getTime()).concat(" seconds have passed")), '1');
                            flush(player.getWriter());
                        }
                        readNonBlocking(player.getReader());
                    }
                    case GAME -> {
                        System.out.print("");
                        if(player.getDisconnected()){
                            throw new SocketException();
                        }
                        else if (!this.auths.get(player.getName()).getInGame()){
                            state = State.MENU;
                            socket.setSoTimeout(0);
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
                        break;
                    }
                }

            }
        }
        if(player != null)player.setTimedOut(true);




    }

    //Removes a player from a queue passed by argument
    private void removeFromQueue(String playerName, List<Player> queue){
        for(int i = 0; i < queue.size(); i++){
            if(playerName.equals(queue.get(i).getName())){
                queue.remove(i);
                break;
            }
        }
    }

    //handles the authentication of the client
    private Player handleAuthentication(BufferedReader reader, PrintWriter writer) throws IOException {
        // Authenticate the client
        int rank = -1;
        String name = "";
        try {
            while (rank == -1) {
                name = read(reader).getLast();
                String password = read(reader).getLast();
                rank = authenticateClient(name, password, writer);
            }
        }catch(SocketException e){
            throw e;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Player player = this.auths.get(name);
        if(player.getDisconnected()) {
            manageSimple();
        }
        player.setReader(reader);
        player.setWriter(writer);
        player.setDisconnected(false);

        System.out.println("New client connected: " + name);
        System.out.println("User authenticated: " + name + " with rank: " + rank);

        return player;

    }

    //handles the menu logic for the client
    private char handleMenu(BufferedReader reader, PrintWriter writer, Player player) throws IOException{
        // Ask the user which gamemode they want to play
        write(writer, "Which gamemode do you wish to play?");
        write(writer, "A -> Simple   B -> Ranked");
        write(writer, "Press Q if you want to quit");
        flush(writer);
        String response;
        try{
            response = read(reader).getLast().toLowerCase();
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

    //initializes the hashmap auths with the ones present in the database
    private void init_auths(){
        JSONArray db = loadJson();

        for(int i = 0; i < db.length(); i++){
            JSONObject user_info = db.getJSONObject(i);

            this.auths.put(user_info.getString("username"),
                    new Player(user_info.getString("username"),
                            user_info.getString("password"),
                            user_info.getInt("rank")
                    ));

        }
    }

    //main function where the server is called to start it's socket and connect to other clients
    private void startServer() throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
        init_auths();
        // Load server KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("certificates/serverkeystore.jks"), "password".toCharArray());

        // Initialize KeyManagerFactory with the KeyStore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "password".toCharArray());

        // Initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        // Create SSLServerSocketFactory and SSLServerSocket
        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        try (SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port)) {
            System.out.println("Server is listening on port " + port);



            for(int i = Card.TOKENS_COUNT; i < Card.ITEMS_COUNT+Card.TOKENS_COUNT; i++){
                this.gameStore.add(new Card(i));
            }
            for(int i = Card.ITEMS_COUNT+Card.TOKENS_COUNT; i < Card.ENCOUNTER_COUNT+Card.TOKENS_COUNT+Card.ITEMS_COUNT; i++){
                this.gameEncounters.add(new Card(i));
            }


            this.timer.schedule(this.timerTask, 0, TIMER_INTERVAL);

            Thread.startVirtualThread(()->{

                int previous_time_rank = 0;
                int previous_time_db = 0;

                while(true){
                    System.out.print("");
                    if(timerTask.getTime() >= previous_time_rank + RANK_QUEUE_TIMER_INTERVAL/1000|| timerTask.getTime()+RANK_QUEUE_TIMER_INTERVAL/1000 <= previous_time_rank  && this.rankedPlayers.size() >= NUM_PLAYERS){
                        previous_time_rank = timerTask.getTime();
                        manageRanked();
                    }
                    if(timerTask.getTime() >= previous_time_db + DB_WRITE_TIMER_INTERVAL/1000|| timerTask.getTime()+DB_WRITE_TIMER_INTERVAL/1000 <= previous_time_db){
                        previous_time_db = timerTask.getTime();
                        updateDB();
                    }

                }

            });

            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();

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

    //fetches a list of games that can be played in the ranked queue
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

    //Manages the ranked queue to see if any game is ready to be started and if it is it starts it.
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

    //Manages the Simple queue to see if any game is ready to be started, if it is it starts it.
    private void manageSimple(){
        locks.get(1).lock();
        if(this.simplePlayers.size() >= NUM_PLAYERS) {
            System.out.println("Managing simple game");
            // Get the first NUM_PLAYERS players
            List<Player> gamePlayers = new ArrayList<>();
            int playerCount = NUM_PLAYERS;
            for (int i = 0; i < Math.min(playerCount, this.simplePlayers.size()); i++) {
                Player player = this.simplePlayers.get(i);
                if(!player.getDisconnected()) {
                    gamePlayers.add(this.simplePlayers.get(i));
                    this.simplePlayers.remove(i);
                    i--;
                }
                else playerCount++;
            }

            locks.get(1).unlock();
            // Start the game
            startGame(gamePlayers, 'a');
        }
        else{
            locks.get(1).unlock();
        }
    }

    //starts a game given a list of players and a gamemode
    private void startGame(List<Player> players, char gamemode){

        Thread.startVirtualThread(()->{

            for(Player player : players){
                this.auths.get(player.getName()).setInGame(true);
            }
            Collections.shuffle(players);
            Game game = new Game(players, this.gameEncounters, this.gameStore, gamemode, this.timerTask);
            try {
                game.run();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }


    //updates the database with the information present in the auths hashmap
    private void updateDB(){


        locks.getFirst().lock();
        JSONArray db = loadJson();
        for (int i = 0; i < db.length(); i++){
            JSONObject user_info = db.getJSONObject(i);
            Player player = this.auths.get(user_info.getString("username"));
            player.setHasBeenWrittenToDB(true);
            if(player.hasRankChanged()) {
                user_info.put("rank", player.getRank());
            }
        }
        for(Player player : this.auths.values()){
            if(!player.hasBeenWrittenToDB()){
                JSONObject user = createUser(player.getName(), player.getPassword());
                db.put(user);
            }
            player.setHasBeenWrittenToDB(false);
            player.setPreviousRank();
        }
        saveJson(db);
        this.locks.getFirst().unlock();


        System.out.println("Updated Database");
    }

    //authenticates the client by checking its parameters with the ones present in the auths hashmap
    private int authenticateClient(String name, String password, PrintWriter writer) throws NoSuchAlgorithmException {
        password = hashPassword(password);
        // Load the JSON file
        if(this.auths.containsKey(name)) {
            Player player = this.auths.get(name);
            if(player.getPassword().equals(password)) {
                if(player.getTimedOut() && player.getDisconnected()){
                    write(writer, "Successfully authenticated!", '0');
                    flush(writer);
                    return player.getRank();
                }
                else if(!player.getTimedOut() && !player.getDisconnected()) {
                    write(writer, "User already authenticated", '1');
                    flush(writer);
                    return -1;
                }
                else {
                    if((!player.getInGame() && State.valueOf(player.getServerState()) == State.GAME) || (player.getInGame() && State.valueOf(player.getServerState()) == State.QUEUE)){
                        player.setServerState(State.MENU.toString());
                    }
                    write(writer, "Successfully authenticated!", player.getServerState().charAt(0));
                    flush(writer);
                    return player.getRank();
                }

            }
            else {
                write(writer, "Wrong password", '1');
                flush(writer);
                return -1;
            }
        }
        this.locks.getFirst().lock();
        if(!this.auths.containsKey(name)) {
            Player newPlayer = new Player(name, password, 0);

            this.auths.put(name, newPlayer);
        }
        else{
            write(writer, "User already authenticated", '1');
            flush(writer);
            return -1;
        }
        this.locks.getFirst().unlock();


            // If the name is not in the database, create new user account
            //JSONObject user = createUser(name, password);
            //db.put(user);
        write(writer, "New account has been created!", '0');
        flush(writer);

            // Save the new user account to the database
            //saveJson(db);

        return 0;
    }

    //creates a new user to store in the database.
    private JSONObject createUser(String username, String password){
        JSONObject user = new JSONObject();
        user.put("username", username);
        user.put("password", password);
        user.put("rank", 0);
        return user;
    }

    //hashes the password
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());

        String result = "";
        for(byte b : md.digest()){
            result = result.concat(String.format("%02x", b));
        }

        return result;
    }
    //loads the database to an array
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

    //stores the array into the database
    private void saveJson(JSONArray json) {
        try {
            Files.write(Paths.get(dbPath), json.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}