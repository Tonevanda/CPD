import java.io.IOException;
import java.net.SocketException;
import java.util.*;

public class Game extends Communication{
    private final List<Player> players;

    private final List<Player> currentPlayers = new ArrayList<>();

    private final Stack<Card> cards = new Stack<>();

    final private int _cardHeight = 18;
    final private int _cardWidth = 50;




    final private int SCORE_RANGE = 20;

    private char _gamemode;

    private int currentScore = 0;

    public Game(List<Player> players, char gamemode){

        for(Player player : players){
            writers.add(player.getWriter());
            this.currentPlayers.add(player);
        }

        broadcast("Game started!", '0');

        this.players = players;
        this._gamemode = gamemode;
    }

    public List<Player> get_players(){
        return players;
    }

    public void run() throws IOException {
        System.out.println("Game started");
        broadcast(CLEAR_SCREEN);

        while(true){
            for(Player player : this.currentPlayers){
                player.setText("");
            }
            Player currentPlayer = this.currentPlayers.getFirst();

            String text = CLEAR_SCREEN;
            text = text.concat(drawGameState());

            drawHands();


            for(Player player : this.currentPlayers){
                player.setText(text.concat(player.getText()));
                write(player.getWriter(), player.getText());
                flush(player.getWriter());

            }

            gameLogic(currentPlayer);




            this.currentPlayers.removeFirst();
            if(this.currentPlayers.size() == 1){
                break;
            }
        }

        Player winner = this.currentPlayers.getFirst();
        write(winner.getWriter(), "Congratulations, you won!", '1');
        flush(winner.getWriter());
        if(_gamemode == 'b')winner.updateRank(currentScore, true);
        winner.resetPlayerGameInfo();

        System.out.println("Game ended");
    }


    private String drawGameState(){
        String text = "";

        text = text.concat("\n");

        int boardCardDistance= 70;

        Card topCard;
        if(this.cards.isEmpty()) topCard = null;
        else topCard = this.cards.peek();

        for(int j = 0; j < this._cardHeight; j++){
            String currentText = "";
            currentText = currentText.concat("    ");
            int startingIndex = 0;
            if(j < this.players.size()){
                Player player = this.players.get(j);
                currentText = currentText.concat("|").concat(player.getName());
                currentText = currentText.concat(" ").concat(Integer.toString(player.getLives())).concat("<3");
                currentText = currentText.concat(" #").concat(Integer.toString(player.getRank())).concat("|");
                startingIndex = 7+player.getName().length()+Integer.toString(player.getLives()).length()+Integer.toString(player.getRank()).length();
            }
            if(topCard != null){
                for(int i = startingIndex; i < boardCardDistance; i++){
                    currentText = currentText.concat(" ");
                }
                currentText = currentText.concat(topCard.draw(j, this._cardWidth, this._cardHeight));
            }
            currentText = currentText.concat("\n");
            text = text.concat(currentText);
        }

        return text;
    }

    private void drawHands(){
        for(Player player : this.currentPlayers){
            player.drawHand(this._cardWidth, this._cardHeight);
        }
    }




    private void gameLogic(Player currentPlayer) throws IOException {
        if (!this.cards.isEmpty() && currentPlayer.hasLost(this.cards.peek())) {
            if(currentPlayer.getLives() <= 0) {
                write(currentPlayer.getWriter(), "you lost!", '1');
                flush(currentPlayer.getWriter());
                if (_gamemode == 'b') {
                    currentPlayer.updateRank(currentScore, false);
                    currentScore += SCORE_RANGE / (this.players.size() - 1);
                }
                currentPlayer.resetPlayerGameInfo();
            }
            else{
                this.currentPlayers.add(currentPlayer);
                while(!this.cards.isEmpty()){
                    Card card = this.cards.pop();
                    for(Player player : this.currentPlayers){
                        if(player.getName().equals(card.getOwner())){
                            player.discardCard(card);
                            break;
                        }
                    }
                }
            }

        } else {
            write(currentPlayer.getWriter(), "your move.", '0');
            flush(currentPlayer.getWriter());
            if (makeMove(currentPlayer)) {
                this.currentPlayers.add(currentPlayer);
            } else if (_gamemode == 'b') {
                currentPlayer.updateRank(currentScore, false);
                currentScore += SCORE_RANGE / (this.players.size() - 1);
                currentPlayer.resetPlayerGameInfo();
            } else {
                currentPlayer.resetPlayerGameInfo();
            }


        }
    }

    public boolean makeMove(Player currentPlayer) throws IOException {

        int cardNumber;
        String move = "";

        while(true) {
            try {
                move = read(currentPlayer.getReader(), currentPlayer.getWriter()).getLast();
            }catch(SocketException e){
                currentPlayer.getTimerTask().setDisconnected(true);
                while(currentPlayer.getTimerTask().getDisconnected()){
                    System.out.print("");
                    if(currentPlayer.getTimerTask().getTimedOut()){
                        return false;
                    }
                }
                write(currentPlayer.getWriter(), currentPlayer.getText());
                flush(currentPlayer.getWriter());
                write(currentPlayer.getWriter(), "your move.", '0');
                flush(currentPlayer.getWriter());
                continue;
            }
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
        currentPlayer.playCard(cardNumber-1);
        return true;
    }

    public boolean isValidMove(Player currentPlayer, int cardNumber){

        if(cardNumber < currentPlayer.getHandCardsCount() && cardNumber >= 0){
            Card card = currentPlayer.getCard(cardNumber);
            if(!this.cards.isEmpty() && card.isCreature()) {
                Card boardCard = this.cards.peek();
                return card.getValue() >= boardCard.getValue();
            }
            return true;
        }

        return false;
    }
}