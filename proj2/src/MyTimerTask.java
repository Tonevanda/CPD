import java.io.PrintWriter;
import java.util.TimerTask;

public class MyTimerTask extends TimerTask {



    private int _time = 0;





    public MyTimerTask(){
        this._time = 0;
    }




    public int getTime(){return this._time;}












    @Override
    public void run(){
        this._time++;
        this._time = this._time % 100000000;
    }

















}
