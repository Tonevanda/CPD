import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class Player{
    private final String _name;
    private int _rank;

    private int _previousRank;

    private PrintWriter _writer;

    private BufferedReader _reader;

    private Timer _timer;

    private MyTimerTask _timerTask;

    private String _serverState = "MENU";

    private List<Card> hand = new ArrayList<>();
    private List<Card> storeCards = new ArrayList<>();

    private int _handWidth = 1;


    private int _health = 150;

    private int _gold = 5;

    private boolean _inGame = false;

    public Player(String name, int rank, PrintWriter writer, BufferedReader reader, int timerInterval, int connectionCheckInterval, int connectionTimeout, int disconnectionTimeout){
        this._name = name;
        this._rank = rank;
        this._previousRank = rank;
        this._writer = writer;
        this._reader = reader;
        this._timer = new Timer();
        this._timerTask = new MyTimerTask(writer, connectionCheckInterval, connectionTimeout, disconnectionTimeout);

        this._timerTask.setMode(0);

        _timer.schedule(_timerTask, 0, timerInterval);

        resetPlayerGameInfo();





    }

    public void resetPlayerGameInfo(){
        this._health = 150;
        this._gold = 5;
        this.hand.clear();
        this.storeCards.clear();
        this._handWidth = 1;
    }

    public String getServerState(){return this._serverState;}


    public boolean getInGame() { return this._inGame; }

    public boolean hasPlayerDBInfoChanged(){return this._rank != this._previousRank;}



    public String getName(){
        return this._name;
    }

    public int getRank(){
        return this._rank;
    }

    public PrintWriter getWriter() { return this._writer; }

    public BufferedReader getReader() { return this._reader; }

    public MyTimerTask getTimerTask(){return this._timerTask;}



    public List<Card> getStoreCards(){return this.storeCards;}

    public int getStoreCardsSize(){return this.storeCards.size();}

    public Card getStoreCard(int cardIndice){return this.storeCards.get(cardIndice);}

    public int getHandWidth(){return this._handWidth;}

    public int getGold(){return this._gold;}
    public int getHealth(){return this._health;}

    public Card getCard(int cardNumber) { return this.hand.get(cardNumber); }

    public int getHandCardsCount() { return this.hand.size(); }

    public List<Card> getHandCards(){return this.hand;}

    public void updateRank(int score, boolean isWinner){

        if(isWinner && score - this._rank/10 < 1){
            this._rank += 1;
        }
        else this._rank += score - this._rank/10;

        if(this._rank < 0) this._rank = 0;
    }


    public void setInGame(boolean inGame) { this._inGame = inGame; }

    public void setServerState(String serverState){this._serverState = serverState;}



    public void setReader(BufferedReader reader){this._reader = reader;}

    public void setWriter(PrintWriter writer){
        this._writer = writer;
        this.getTimerTask().setWriter(writer);
    }

    public void closeTimer(){this._timer.cancel();}


    public void resetStoreCards(){this.storeCards.clear();}
    public void addStoreCard(Card card){this.storeCards.add(card);}

    public void removeStoreCard(int cardIndice){
        this.storeCards.remove(cardIndice);
    }

    public void addHandCard(Card card){
        this.hand.add(new Card(card.getType()));
        this._handWidth += card.getWidth();
    }


    public void takeDamage(int damage){this._health -= damage;}

    public void triggerCardEffects(Player enemyPlayer, int time){
        for(Card card : this.hand){
            card.triggerEffect(this, enemyPlayer, time);
        }
    }



    /*public void drawCardsAction(int quantity){
        for(int i = 0; i < quantity; i++){
            if(this.deck.isEmpty()) reshuffleDeck();
            this.hand.add(this.deck.getFirst());
            this.deck.removeFirst();
        }
    }

    public void reshuffleDeck(){
        this.deck = this._discardPile;
        Collections.shuffle(this.deck);
        this._discardPile = new ArrayList<>();
    }


    public void discardCard(Card card){
        this._discardPile.add(card);
    }

    public void playCard(int cardNumber){

        this.hand.remove(cardNumber);

        if(this.hand.isEmpty()) drawCardsAction(this._maxHandSize);

    }

    public boolean hasLost(Card boardCard){
        for(Card card : this.hand){
            if(card.getValue() >= boardCard.getValue()) return false;
        }
        this._lives--;
        return true;
    }*/
}
