import java.util.List;

public class Game implements Runnable{
    private List<Player> players;

    public Game(List<Player> players){

        this.players = players;
    }

    public List<Player> get_players(){
        return players;
    }

    public void run(){
        System.out.println("Game started");
    }
}