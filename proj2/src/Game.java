import java.io.IOException;
import java.util.*;

public class Game extends Communication{
    private List<Player> players;

    private List<Player> currentPlayers = new ArrayList<>();

    private Stack<Card> cards = new Stack<>();

    final private int _cardHeight = 10;
    final private int _cardWidth = 20;
    

    final private String CLEAR_BOARD = "\033[H\033[2J";

    public Game(List<Player> players){

        this.players = players;
    }

    public List<Player> get_players(){
        return players;
    }

    public void run() throws IOException {
        System.out.println("Game started");
        for(Player player : this.players){
            write(player.getWriter(), CLEAR_BOARD);
            flush(player.getWriter());
            this.currentPlayers.add(player);
        }
        while(true){
            Player currentPlayer = this.currentPlayers.getFirst();

            String text = CLEAR_BOARD;
            text = text.concat(drawPlayers()).concat("\n");



            text = text.concat(drawPlayingCards()).concat("\n");


            drawHands();





            for(Player player : this.currentPlayers){
                write(player.getWriter(), text);
                write(player.getWriter(), player.getText());

                player.setText("");
            }


            for(Player player : this.currentPlayers){
                flush(player.getWriter());
            }


            if(!this.cards.isEmpty() && currentPlayer.hasLost(this.cards.peek())){
                write(currentPlayer.getWriter(), "you lost!");
                flush(currentPlayer.getWriter());
            }
            else {
                write(currentPlayer.getWriter(), "your move!");
                flush(currentPlayer.getWriter());
                makeMove(currentPlayer);
                this.currentPlayers.add(currentPlayer);

            }



            this.currentPlayers.removeFirst();
            if(this.currentPlayers.size() == 1){
                break;
            }




        }

        Player winner = this.currentPlayers.getFirst();
        write(winner.getWriter(), "Congratulations, you won!");
        flush(winner.getWriter());



        System.out.println("Game ended");

    }

    private String drawPlayers(){
        String text = "";


        text = text.concat("       ");
        for(Player player : this.players){

            text = text.concat("|").concat(player.getName());
            text = text.concat(" #").concat(Integer.toString(player.getRank())).concat("|");
            text = text.concat("                                      ");

        }
            text = text.concat("\n");

        return text;


    }

    private void drawHands(){
        for(Player player : this.currentPlayers){
            player.drawHand(this._cardWidth, this._cardHeight);
        }

    }


    private String drawPlayingCards(){
        String text = "";
        Card topCard;
        if(this.cards.isEmpty()) topCard = null;
        else topCard = this.cards.peek();
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
                if(isValidMove(currentPlayer, cardNumber-1)){
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

    public boolean isValidMove(Player currentPlayer, int cardNumber){
        if(this.cards.isEmpty()) return true;
        if(cardNumber < currentPlayer.getHandCardsCount() && cardNumber >= 0){
            Card card = currentPlayer.getCard(cardNumber);
            Card boardCard = this.cards.peek();
            return card.getValue() >= boardCard.getValue();
        }
        return false;
    }
}