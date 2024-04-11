import java.net.Socket;
import java.util.List;

public class Game{
    private List<Socket> _userSockets;

    public Game(int players, List<Socket> userSockets){
        this._userSockets = userSockets;
    }

    public void start(){
        // TODO: Implement game logic
    }

    public static void main(String[] args) {
        System.out.println("Game started");
    }
}