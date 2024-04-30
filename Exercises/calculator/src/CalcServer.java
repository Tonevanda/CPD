import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CalcServer {
    private int globalSum = 0;
    private int port;
    private ReentrantLock lock = new ReentrantLock();
    private HashMap<String, Integer> clients = new HashMap<String, Integer>();

    public CalcServer(int port) {
        this.port = port;
    }

    public void calculator(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Get data from previous sum from client 
        String clientName = clientSocket.getInetAddress().getHostName();
        int clientSum;
        this.lock.lock();
        if (this.clients.containsKey(clientName)) {
            clientSum = this.clients.get(clientName);
        } else {
            this.clients.put(clientName, 0);
            clientSum = 0;
        }
        this.lock.unlock();

        int localSum = 0;
        String inputLine;
        try{
            while ((inputLine = in.readLine()) != null) {
                int number;
                if (inputLine.equalsIgnoreCase("exit")) {
                    System.out.println("Client exited");
                    break;
                }
                System.out.println("Client sent: " + inputLine);
                number = Integer.parseInt(inputLine);
                localSum += number;
                clientSum += number;
                out.println("Your current sum is: " + clientSum);
            }
        } catch (SocketException e) {
            System.out.println("Client lost connection");
        }
        

        this.lock.lock();
        this.clients.put(clientName, clientSum);
        this.globalSum += localSum;
        out.println("Global sum is: " + this.globalSum);
        System.out.println("Client Disconnected. Global sum is: " + this.globalSum);
        this.lock.unlock();

        in.close();
        out.close();
        clientSocket.close();
    }

    public void run(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);
                System.out.println("New client connected");
                
                Thread clientThread = new Thread(() -> {
                    try {
                        this.calculator(socket);
                    } catch (IOException exception) {
                        System.out.println("Error handling client: " + exception.getMessage());
                    }
                });
                clientThread.start();
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    

    public static void main(String[] args) {
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);
    
        CalcServer server = new CalcServer(port);
        server.run();
    }
}