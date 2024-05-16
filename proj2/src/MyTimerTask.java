import java.io.PrintWriter;
import java.util.TimerTask;

public class MyTimerTask extends TimerTask {

    private int _time = 0;

    //initializes the mytimertask class
    public MyTimerTask(){
        this._time = 0;
    }

    public int getTime(){return this._time;}

    //function that is periodically run every time interval specified when scheduling this class. Increments a timer variable.
    @Override
    public void run(){
        this._time++;
        this._time = this._time % 100000000;
    }

}
