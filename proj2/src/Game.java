import java.io.IOException;
import java.net.SocketException;
import java.util.*;

public class Game extends Communication{
    private final List<Player> players;


    final private int CARD_HEIGHT = 18;

    private final List<Card> store;

    private List<List<Player>> fights = new ArrayList<>();


    final private int SCORE_RANGE = 20;

    private final char _gamemode;

    private int currentScore = 0;

    enum State{
        STORE,
        FIGHT,
        END
    }

    public enum MoveType{
        INVALID,
        BUY,
        SELL,
        SWAP,
        REROLL,
        ENDTURN
    }

    private State state = State.STORE;

    private Timer timer;

    private MyTimerTask timerTask;

    //private Random random = new Random();

    private final int TIMER_INTERVAL = 1000;

    private final int STORE_TIMEOUT = 120;

    private final int FIGHT_TIMEOUT = 30;

    private final int MAX_WIDTH = 156;

    private final int MIN_CARD_WIDTH = 11;

    private int finishedStatePlayersCount = 0;

    public Game(List<Player> players, List<Card> store, char gamemode){

        this.store = store;
        this.timer = new Timer();
        this.timerTask = new MyTimerTask();
        this.timerTask.setTimer(0);
        this.timerTask.setMode(0);
        this.timer.schedule(timerTask, 0, TIMER_INTERVAL);

        for(Player player : players){
            writers.add(player.getWriter());
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

        for(Player player : this.players) {
            refillStore(player);
            drawStoreState(player);
            write(player.getWriter(), "", '0');
            flush(player.getWriter());
        }


        while (this.state != State.END) {
            switch(this.state){
                case STORE -> {
                    for(Player player : this.players) {
                        if(player.getReader().ready()){
                            List<String> response = read(player.getReader());
                            storeHandler(player, response.getLast());
                        }


                        if(this.finishedStatePlayersCount == this.players.size()) {
                            this.finishedStatePlayersCount = 0;
                            this.state = State.FIGHT;
                            Collections.shuffle(this.players);
                            List<Player> fight = new ArrayList<>();
                            for(Player p : this.players){
                                fight.add(p);
                                if(fight.size() == 2){
                                    this.fights.add(fight);
                                    drawFightState(fight);
                                    fight = new ArrayList<>();
                                }
                            }

                            timerTask.setTimer(0);
                            timerTask.resetTimer();
                        }

                    }
                }
                case FIGHT -> {
                    if(timerTask.timeChanged()){
                        fightHandler(timerTask.getTime());

                        if(this.finishedStatePlayersCount == players.size() || timerTask.getTime() > 30){
                            this.finishedStatePlayersCount = 0;
                            this.state = State.STORE;
                            timerTask.setTimer(0);
                            timerTask.resetTimer();
                            for(Player player : this.players){
                                refillStore(player);
                                drawStoreState(player);
                                write(player.getWriter(), "", '0');
                                flush(player.getWriter());
                            }
                        }
                    }

                }
            }

        }
            /*

            gameLogic(currentPlayer);


        }

        Player winner = this.currentPlayers.getFirst();
        write(winner.getWriter(), "Congratulations, you won!", '1');
        flush(winner.getWriter());
        if(_gamemode == 'b')winner.updateRank(currentScore, true);
        winner.resetPlayerGameInfo();

        System.out.println("Game ended");*/
    }

    public void storeHandler(Player currentPlayer, String userInput){

        MoveType moveType = getMoveType(currentPlayer, userInput);
        makeMove(currentPlayer, moveType, userInput);



    }

    public void fightHandler(int time){
        for(int i = 0; i < this.fights.size(); i++){
            List<Player> fight = this.fights.get(i);
            Player player1 = fight.getFirst();
            Player player2 = fight.getLast();
            player1.triggerCardEffects(player2, time);
            player2.triggerCardEffects(player1, time);
            drawFightState(fight);
            if(player1.getHealth() <= 0 || player2.getHealth() <= 0){
                finishedStatePlayersCount += 2;
                this.fights.remove(i);
                i--;
            }
        }

    }

    public MoveType getMoveType(Player currentPlayer, String userInput){
        if(userInput.length() > 5) {
            write(currentPlayer.getWriter(), "Please insert a valid input!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        if(userInput.equals("done"))return MoveType.ENDTURN;
        int cardIndice;
        try {
            cardIndice = Integer.parseInt(userInput);
        }catch(NumberFormatException e){
            write(currentPlayer.getWriter(), "Input needs to be a number!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        cardIndice--;
        int maxIndice = currentPlayer.getStoreCardsSize() + currentPlayer.getHandCardsCount();
        if(cardIndice >= maxIndice || cardIndice < 0) {
            write(currentPlayer.getWriter(), "Out of bounds card indice. Please try again!!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        if(cardIndice < currentPlayer.getStoreCardsSize()){
            Card card = currentPlayer.getStoreCard(cardIndice);
            if(card.getGold() > currentPlayer.getGold()){
                write(currentPlayer.getWriter(), "You don't have enough gold to buy this card!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            else if(currentPlayer.getHandWidth()+card.getWidth() > MAX_WIDTH){
                write(currentPlayer.getWriter(), "You don't have space for this card!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            return MoveType.BUY;
        }
        return MoveType.SELL;

    }

    public void makeMove(Player currentPlayer, MoveType moveType, String userInput){
        switch(moveType){
            case BUY ->{
                int cardIndice = Integer.parseInt(userInput)-1;
                Card card = currentPlayer.getStoreCard(cardIndice);
                currentPlayer.addHandCard(card);
                currentPlayer.removeStoreCard(cardIndice);
                reorderCardIndices(currentPlayer);
                drawStoreState(currentPlayer);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SELL -> {

            }
            case ENDTURN -> {
                this.finishedStatePlayersCount++;
                if(this.finishedStatePlayersCount != this.players.size()) drawStoreState(currentPlayer);
            }
        }
    }


    private void refillStore(Player player){
        player.resetStoreCards();
        Collections.shuffle(this.store);
        int storeWidth = 1;
        int storeIndex = 0;
        while(storeWidth <= MAX_WIDTH-MIN_CARD_WIDTH || storeIndex == store.size()){
            Card card = this.store.get(storeIndex);
            storeIndex++;
            if(card.getWidth() + storeWidth <= MAX_WIDTH){
                player.addStoreCard(new Card(card.getType()));
                storeWidth += card.getWidth();
                Collections.shuffle(this.store);
                storeIndex = 0;
            }


        }
        reorderCardIndices(player);

    }

    public void reorderCardIndices(Player currentPlayer){
        int cardIndice = 1;
        for(Card card : currentPlayer.getStoreCards()){
            card.setIndex(cardIndice);
            cardIndice++;
        }
        for(Card card : currentPlayer.getHandCards()){
            card.setIndex(cardIndice);
            cardIndice++;
        }
    }


    public void drawFightState(List<Player> fight){
        Player player1 = fight.getFirst();
        Player player2 = fight.getLast();

        String text = CLEAR_SCREEN.concat("\n");
        String player1Cards = drawCards(player1.getHandCards());
        String player2Cards = drawCards(player2.getHandCards());
        write(player1.getWriter(), text.concat(player2Cards).concat(player1Cards));
        write(player2.getWriter(), text.concat(player1Cards).concat(player2Cards));
        flush(player1.getWriter());
        flush(player2.getWriter());

    }
    public void drawStoreState(Player player){
        String text = CLEAR_SCREEN.concat("\n");
        text = text.concat(drawCards(player.getStoreCards()));
        text = text.concat(drawCards(player.getHandCards()));

        write(player.getWriter(), text);
        flush(player.getWriter());

    }

    public String drawCards(List<Card> cards){
        String text = "";
        for(int j = 0; j < CARD_HEIGHT; j++){
            if(!cards.isEmpty()) {
                text = text.concat(" ");
                if (j == 0) text = text.concat(" ");
                else text = text.concat("|");
                for (Card card : cards) {
                    text = text.concat(card.draw(j, CARD_HEIGHT));
                }
            }
            text = text.concat("\n");
        }

        return text;
    }




    /*private String drawGameState(){
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





     */


        /*public boolean makeMove(Player currentPlayer) throws IOException {

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
        this.cards.peek().triggerOnPlayEffects(currentPlayer);
        return true;
    }*/
}