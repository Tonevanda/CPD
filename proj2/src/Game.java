import java.util.List;

public class Game{
    private List<Player> players;

    public Game(List<Player> players){
        this.players = players;
    }

    public void run(){
        System.out.println("Game started");
    }
}