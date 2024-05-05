import java.io.PrintWriter;
import java.util.TimerTask;

public class MyTimerTask extends TimerTask {

    private int _time = 0;

    private int _disconnectionTimeout;

    private int _disconnectionTime;

    private boolean _timeChanged = false;

    private boolean _verifyConnection = false;

    private int _connectionCheckInterval;

    private int _connectionTimeout;

    private int _connectionTime;

    private boolean _isDisconnected = false;

    private PrintWriter _writer;

    enum State{
        NORMAL,
        PING
    }

    private State _state = State.NORMAL;





    public MyTimerTask(PrintWriter writer, int connectionTimeout, int disconnectionTimeout, int connectionCheckInterval){
        this._writer = writer;
        this._connectionTimeout = connectionTimeout;
        this._connectionTime = connectionTimeout;
        this._disconnectionTimeout = disconnectionTimeout;
        this._disconnectionTime = _disconnectionTimeout;
        this._connectionCheckInterval = connectionCheckInterval;

    }

    public void resetTimer(){
        this._time = 0;
        this._timeChanged = false;
        this._disconnectionTime = this._disconnectionTimeout;
        this._connectionTime = this._connectionTimeout;
        this._isDisconnected = false;
        this._verifyConnection = false;
    }

    public void setMode(int mode){
        resetTimer();
        if(mode == 0){
            this._state = State.NORMAL;
        }
        else if(mode == 1){
            this._state = State.PING;
            this._verifyConnection = true;
        }
    }

    public void setTime(int time){this._time = time;}


    public void resetConnectionTime(){this._connectionTime = this._connectionTimeout;}



    public int getTime(){return this._time;}

    public boolean getDisconnected(){return this._isDisconnected;}

    public boolean timeChanged() {
        if(this._timeChanged){
            this._timeChanged = false;
            return true;
        }
        return false;
    }






    @Override
    public void run() {
        this._time++;
        this._timeChanged = true;



        switch(this._state){
            case NORMAL ->{

            }
            case PING -> {
                this._connectionTime--;
                if(this._connectionTime <= 0){
                    this._isDisconnected = true;
                }
                if(!this._isDisconnected && this._verifyConnection && this._time % this._connectionCheckInterval == 0){
                    this._connectionTime--;

                    System.out.println("SENT VERIFY IF CONNECTION ALIVE MESSAGE");
                    _writer.println("T");
                    _writer.flush();
                }
            }
        }


    }
}
