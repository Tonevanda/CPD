import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Game{
    private List<Socket> _userSockets;

    List<Thread> threads = new ArrayList<>();

    public Game(int players, List<Socket> userSockets){
        this._userSockets = userSockets;
    }


    public void start() throws InterruptedException {
        for(int i = 0; i < 10; i++){
            Thread thread = Thread.startVirtualThread(()-> {
                try{

                    Thread.sleep(Duration.ofSeconds(10));
                }
                catch(InterruptedException e){

                }



            });
            threads.add(thread);

        }

        for(Thread thread : threads){
            thread.join();

        }


    }

    public static void main(String[] args) {
        System.out.println("Game started");
    }
}