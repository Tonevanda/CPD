import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class Player{
    private final String _name;
    private int _rank;

    private PrintWriter _writer;

    private BufferedReader _reader;

    private Timer _timer;

    private MyTimerTask _timerTask;

    private String _serverState = "MENU";

    private List<Card> deck;

    private List<Card> hand;

    private List<Card> _discardPile;

    private final int _maxHandSize = 5;

    private String _text = "";

    private boolean _inGame = false;

    public Player(String name, int rank, PrintWriter writer, BufferedReader reader, int timerInterval, int connectionCheckInterval, int connectionTimeout, int disconnectionTimeout){
        this._name = name;
        this._rank = rank;
        this._writer = writer;
        this._reader = reader;
        this._timer = new Timer();
        this._timerTask = new MyTimerTask(writer, connectionCheckInterval, connectionTimeout, disconnectionTimeout);

        this._timerTask.setMode(0);

        _timer.schedule(_timerTask, 0, timerInterval);

        resetPlayerGameInfo();





    }

    public void resetPlayerGameInfo(){
        this.deck = new ArrayList<>();
        this._discardPile = new ArrayList<>();
        this.hand = new ArrayList<>();
        this._text = "";
        for(int i = 0; i < 10; i++){
            this.deck.add(new Card(i));
        }
        Collections.shuffle(this.deck);
        drawCardsAction(_maxHandSize);
    }

    public String getServerState(){return this._serverState;}


    public boolean getInGame() { return this._inGame; }



    public String getName(){
        return this._name;
    }

    public int getRank(){
        return this._rank;
    }

    public PrintWriter getWriter() { return this._writer; }

    public BufferedReader getReader() { return this._reader; }

    public MyTimerTask getTimerTask(){return this._timerTask;}

    public String getText(){ return this._text; }

    public Card getCard(int cardNumber) { return this.hand.get(cardNumber); }

    public int getHandCardsCount() { return this.hand.size(); }

    public void updateRank(int score, boolean isWinner){

        if(isWinner && score - this._rank/10 < 1){
            this._rank += 1;
        }
        else this._rank += score - this._rank/10;

        if(this._rank < 0) this._rank = 0;
    }


    public void setInGame(boolean inGame) { this._inGame = inGame; }

    public void setServerState(String serverState){this._serverState = serverState;}



    public void setText(String text){ this._text = text; }

    public void setReader(BufferedReader reader){this._reader = reader;}

    public void setWriter(PrintWriter writer){
        this._writer = writer;
        this.getTimerTask().setWriter(writer);
    }

    public void closeTimer(){this._timer.cancel();}


    public void drawCardsAction(int quantity){
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

    public void drawHand(int cardWidth, int cardHeight){

        for(int j = 0; j < cardHeight; j++){
            this._text = _text.concat("     ");
            for (Card card : this.hand) {
                this._text = this._text.concat(card.draw(j, cardWidth, cardHeight));
            }
            this._text = this._text.concat("\n");
        }
    }

    public void discardCard(int cardNumber){
        Card card = this.hand.get(cardNumber);

        this.hand.remove(cardNumber);

        if(this.hand.isEmpty()) drawCardsAction(this._maxHandSize);

        this._discardPile.add(card);
    }

    public boolean hasLost(Card boardCard){
        for(Card card : this.hand){
            if(card.getValue() >= boardCard.getValue()) return false;
        }
        return true;
    }
}
