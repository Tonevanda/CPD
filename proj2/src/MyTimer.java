import java.io.PrintWriter;
import java.util.TimerTask;

public class MyTimer extends TimerTask {

    private final int time = 0;

    private PrintWriter writer;


    public MyTimer(PrintWriter writer){
        this.writer = writer;
    }
    @Override
    public void run() {

    }
}
