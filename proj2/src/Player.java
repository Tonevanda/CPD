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

    private int _handCardsCount = 2;


    private String _text = "";

    public Player(String name, int rank, Socket socket, PrintWriter writer, BufferedReader reader){
        this._name = name;
        this._rank = rank;
        this._socket = socket;
        this._writer = writer;
        this._reader = reader;
        this.deck = new ArrayList<Card>();

        this.deck.add(new Card(3));
        this.deck.add(new Card(4));

        Collections.shuffle(this.deck);
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

    public Card getCard(int cardNumber) { return this.deck.get(cardNumber); }

    public void setRank(int rank){
        this._rank = rank;
    }

    public void setText(String text){ this._text = text; }



    public int getHandCardsCount() { return this._handCardsCount; }


    public void drawHand(int cardWidth, int cardHeight){

        for(int j = 0; j < cardHeight; j++){
            this._text = _text.concat("     ");
            for(int i = 0; i < Math.min(this.deck.size(), 5); i++){
                Card card = this.deck.get(i);
                this._text = this._text.concat(card.draw(j, cardWidth, cardHeight));

            }
            this._text = this._text.concat("\n");
        }


    }

    public void discardCard(int cardNumber){
        this.deck.remove(cardNumber);
        this._handCardsCount -= 1;
    }



}
