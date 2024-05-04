import java.io.PrintWriter;
import java.util.TimerTask;

public class ServerTimer extends TimerTask {

    private int _time = 0;

    private boolean _timeChanged = false;

    private boolean _verifyConnection = false;

    private int _connectionCheckInterval = 5;

    private PrintWriter _writer;





    public ServerTimer(PrintWriter writer){
        this._writer = writer;
    }

    public void setTime(int time){this._time = time;}

    public void setVerifyConnection(boolean verifyConnection, int time){
        this._verifyConnection = verifyConnection;
        this._connectionCheckInterval = time;
    }



    public int getTime(){return this._time;}

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
        if(this._verifyConnection && this._time % this._connectionCheckInterval == 0){
            System.out.println("SENT VERIFY IF CONNECTION ALIVE MESSAGE");
            _writer.println("T");
            _writer.flush();
        }
    }
}
