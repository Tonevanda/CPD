import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Game extends Communication{
    private List<Player> players;

    private Stack<Card> cards = new Stack<>();

    final private int _cardHeight = 10;
    final private int _cardWidth = 20;

    final private String CLEAR_BOARD = "\033[H\033[2J";

    public Game(List<Player> players){

        this.players = players;
        this.cards.push(new Card(1));
    }

    public List<Player> get_players(){
        return players;
    }

    public void run() throws IOException {
        System.out.println("Game started");
        for(Player player : this.players){
            write(player.getWriter(), CLEAR_BOARD);
            flush(player.getWriter());
        }
        while(true){
            Player currentPlayer = this.players.getFirst();

            String text = CLEAR_BOARD;
            text = text.concat(drawPlayers()).concat("\n");



            text = text.concat(drawPlayingCards()).concat("\n");


            drawHands();





            for(Player player : this.players){
                write(player.getWriter(), text);
                write(player.getWriter(), player.getText());

                player.setText("");
            }
            write(currentPlayer.getWriter(), "your move!");

            for(Player player : this.players){
                flush(player.getWriter());
            }




            makeMove(currentPlayer);


            this.players.add(currentPlayer);
            this.players.removeFirst();

            if(currentPlayer.getHandCardsCount() == 0){
                break;
            }


        }

        System.out.println("Game ended");

    }

    private String drawPlayers(){
        String text = "";


        text = text.concat("       ");
        for(Player player : this.players){

            text = text.concat("|").concat(player.getName()).concat("|");
            text = text.concat("                                      ");

        }
            text = text.concat("\n");

        return text;


    }

    private void drawHands(){
        for(Player player : this.players){
            player.drawHand(this._cardWidth, this._cardHeight);
        }

    }


    private String drawPlayingCards(){
        String text = "";
        Card topCard = this.cards.peek();
        for(int j = 0; j < this._cardHeight; j++){

            text = text.concat("     ");

            if(topCard != null){
                text = text.concat(topCard.draw(j, this._cardWidth, this._cardHeight));
            }

            text = text.concat("\n");

        }

        return text;

    }

    public void makeMove(Player currentPlayer) throws IOException {

        int cardNumber = 1;

        while(true) {
            String move = read(currentPlayer.getReader());
            try {
                cardNumber = Integer.parseInt(move);
                if(cardNumber <= currentPlayer.getHandCardsCount() && cardNumber > 0){
                    break;
                }



            } catch (NumberFormatException e) {

            }
            write(currentPlayer.getWriter(), "Invalid move. Please try again!\n");
            flush(currentPlayer.getWriter());
        }
        this.cards.push(currentPlayer.getCard(cardNumber-1));
        currentPlayer.discardCard(cardNumber-1);


    }
}