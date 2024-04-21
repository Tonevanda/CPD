import java.net.Socket;

public class Player {
    private String _name;
    private int _rank;
    Socket _socket;
    private boolean _isReady;

    public Player(String name, int rank, Socket socket){
        this._name = name;
        this._rank = rank;
        this._socket = socket;
        this._isReady = false;
    }

    public String getName(){
        return this._name;
    }

    public int getRank(){
        return this._rank;
    }

    public void setRank(int rank){
        this._rank = rank;
    }

    public boolean getIsReady(){
        return this._isReady;
    }

    public void setIsReady(boolean isReady){
        this._isReady = isReady;
    }
}
