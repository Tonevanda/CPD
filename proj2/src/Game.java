import java.io.IOException;
import java.util.*;

public class Game extends Communication{
    private final List<Player> players;

    private final List<Player> currentPlayers = new ArrayList<>();

    private final Stack<Card> cards = new Stack<>();

    final private int _cardHeight = 10;
    final private int _cardWidth = 20;



    final private int SCORE_RANGE = 20;

    public Game(List<Player> players){

        for(Player player : players){
            writers.add(player.getWriter());
            this.currentPlayers.add(player);
        }

        broadcast("Game started!", '0');

        this.players = players;
    }

    public List<Player> get_players(){
        return players;
    }

    public void run() throws IOException {
        System.out.println("Game started");
        broadcast(CLEAR_SCREEN, '2');
        int currentScore = 0;
        while(true){
            Player currentPlayer = this.currentPlayers.getFirst();

            String text = '2'+CLEAR_SCREEN;
            text = text.concat(drawPlayers()).concat("\n");

            text = text.concat(drawPlayingCards()).concat("\n");

            drawHands();

            broadcast(text);

            for(Player player : this.currentPlayers){
                write(player.getWriter(), player.getText());
                flush(player.getWriter());

                player.setText("");
            }



            if(!this.cards.isEmpty() && currentPlayer.hasLost(this.cards.peek())){
                write(currentPlayer.getWriter(), "you lost!", '1');
                flush(currentPlayer.getWriter());
                currentPlayer.updateRank(currentScore, false);
                currentScore += SCORE_RANGE/(this.players.size()-1);
            }
            else {
                write(currentPlayer.getWriter(), "your move.", '0');
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
        write(winner.getWriter(), "Congratulations, you won!", '1');
        flush(winner.getWriter());
        winner.updateRank(currentScore, true);
        //giveScore(winner.getName(), currentScore, true);

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

        int cardNumber;

        while(true) {
            String move = read(currentPlayer.getReader());
            try {
                cardNumber = Integer.parseInt(move);
                if(isValidMove(currentPlayer, cardNumber-1)){
                    break;
                }
            } catch (NumberFormatException e) {
                write(currentPlayer.getWriter(), "Input needs to be a number!", '0');
                flush(currentPlayer.getWriter());
                continue;
            }
            write(currentPlayer.getWriter(), "Invalid move! Please try again.", '0');
            flush(currentPlayer.getWriter());
        }

        this.cards.push(currentPlayer.getCard(cardNumber-1));
        currentPlayer.discardCard(cardNumber-1);
    }

    public boolean isValidMove(Player currentPlayer, int cardNumber){

        if(cardNumber < currentPlayer.getHandCardsCount() && cardNumber >= 0){
            Card card = currentPlayer.getCard(cardNumber);
            if(this.cards.isEmpty()) return true;
            Card boardCard = this.cards.peek();
            return card.getValue() >= boardCard.getValue();
        }

        return false;
    }
}