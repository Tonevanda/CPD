import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player{
    private String _name;
    private int _rank;
    final private Socket _socket;

    final private PrintWriter _writer;

    final private BufferedReader _reader;

    private List<Card> deck;

    private List<Card> hand = new ArrayList<>();

    private List<Card> discardPile = new ArrayList<>();

    private int _maxHandSize = 5;




    private String _text = "";

    public Player(String name, int rank, Socket socket, PrintWriter writer, BufferedReader reader){
        this._name = name;
        this._rank = rank;
        this._socket = socket;
        this._writer = writer;
        this._reader = reader;
        this.deck = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            this.deck.add(new Card(i));
        }

        Collections.shuffle(this.deck);


        drawCardsAction(_maxHandSize);




    }

    public String getName(){
        return this._name;
    }

    public int getRank(){
        return this._rank;
    }

    public Socket getSocket(){ return this._socket; }

    public PrintWriter getWriter() { return this._writer; }

    public BufferedReader getReader() { return this._reader; }

    public String getText(){ return this._text; }


    public Card getCard(int cardNumber) { return this.hand.get(cardNumber); }

    public void setRank(int rank){
        this._rank = rank;
    }

    public void setText(String text){ this._text = text; }



    public int getHandCardsCount() { return this.hand.size(); }

    public void drawCardsAction(int quantity){
        for(int i = 0; i < quantity; i++){
            if(this.deck.isEmpty()) reshuffleDeck();
            this.hand.add(this.deck.getFirst());
            this.deck.removeFirst();
        }
    }

    public void reshuffleDeck(){
        this.deck = this.discardPile;
        Collections.shuffle(this.deck);
        this.discardPile = new ArrayList<>();
    }


    public void drawHand(int cardWidth, int cardHeight){

        for(int j = 0; j < cardHeight; j++){
            this._text = _text.concat("     ");
            for(int i = 0; i < this.hand.size(); i++){
                Card card = this.hand.get(i);
                this._text = this._text.concat(card.draw(j, cardWidth, cardHeight));

            }
            this._text = this._text.concat("\n");
        }


    }

    public void discardCard(int cardNumber){

        Card card = this.hand.get(cardNumber);

        this.hand.remove(cardNumber);

        if(this.hand.isEmpty()){
            drawCardsAction(this._maxHandSize);
        }
        this.discardPile.add(card);
    }

    public boolean hasLost(Card boardCard){
        for(Card card : this.hand){
            if(card.getValue() >= boardCard.getValue()) return false;
        }
        return true;
    }



}
