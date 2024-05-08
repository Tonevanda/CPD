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

        this.players.sort((p1, p2)-> (p2.getHealth() - p1.getHealth()));

        for(Player player : this.players) {
            refillStore(player);
            drawStoreState(player, false);
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
                    System.out.print("");
                    if(timerTask.timeChanged()){
                        fightHandler(timerTask.getTime());

                        if(this.finishedStatePlayersCount == players.size() || timerTask.getTime() > 30){
                            this.finishedStatePlayersCount = 0;
                            this.fights.clear();
                            this.state = State.STORE;
                            for(Player player : this.players){
                                refillStore(player);
                                drawStoreState(player, false);
                                write(player.getWriter(), "", '0');
                                flush(player.getWriter());
                            }
                            this.players.sort((p1, p2)-> (p2.getHealth() - p1.getHealth()));
                            timerTask.setTimer(0);
                            timerTask.resetTimer();
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
        userInput = userInput.toLowerCase();
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
        int maxIndice = currentPlayer.getStoreCardsSize() + currentPlayer.getHandCardsCount();
        if(userInput.length() > 5) {
            write(currentPlayer.getWriter(), "Please insert a valid input!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        else if(userInput.equals("done"))return MoveType.ENDTURN;
        else if(userInput.equals("r")) return MoveType.REROLL;
        else if(userInput.contains("-")){
            String[] splittedInput = userInput.split("-");
            if(splittedInput.length > 2){
                write(currentPlayer.getWriter(), "Your swapping contains too many \"-\"!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            int cardIndice1;
            int cardIndice2;
            try{
                cardIndice1 = Integer.parseInt(splittedInput[0]);
            }catch(NumberFormatException e){
                write(currentPlayer.getWriter(), "Input before \"-\" needs to be a number!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            try{
                cardIndice2 = Integer.parseInt(splittedInput[1]);
            }catch(NumberFormatException e){
                write(currentPlayer.getWriter(), "Input after \"-\" needs to be a number!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            cardIndice1--;
            cardIndice2--;
            if(cardIndice1 >= maxIndice || cardIndice1 < maxIndice-currentPlayer.getHandCardsCount()){
                write(currentPlayer.getWriter(), "First card indice is out of bounds. Please try again!!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            else if(cardIndice2 >= maxIndice || cardIndice2 < maxIndice-currentPlayer.getHandCardsCount()){
                write(currentPlayer.getWriter(), "Second card indice is out of bounds. Please try again!!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            else if(cardIndice1 == cardIndice2){
                write(currentPlayer.getWriter(), "You can't swap the same card!!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            return MoveType.SWAP;
        }
        int cardIndice;
        try {
            cardIndice = Integer.parseInt(userInput);
        }catch(NumberFormatException e){
            write(currentPlayer.getWriter(), "Input needs to be a number!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        cardIndice--;
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
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SELL -> {
                int cardIndice = Integer.parseInt(userInput)-currentPlayer.getStoreCardsSize()-1;
                currentPlayer.removeHandCard(cardIndice);
                reorderCardIndices(currentPlayer);
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());

            }
            case REROLL -> {
                refillStore(currentPlayer);
                currentPlayer.setGold(currentPlayer.getGold()-1);
                reorderCardIndices(currentPlayer);
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SWAP -> {
                String[] splitInput = userInput.split("-");
                int cardIndice1 = Integer.parseInt(splitInput[0])-currentPlayer.getStoreCardsSize()-1;
                int cardIndice2 = Integer.parseInt(splitInput[1])-currentPlayer.getStoreCardsSize()-1;
                currentPlayer.swapCards(cardIndice1, cardIndice2);
                reorderCardIndices(currentPlayer);
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case ENDTURN -> {
                this.finishedStatePlayersCount++;
                if(this.finishedStatePlayersCount != this.players.size()) drawStoreState(currentPlayer, true);
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
                if(player.getStoreCardsSize() == 3) break;
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

        String text = CLEAR_SCREEN;
        String player1info = player1.draw();
        String player2info = player2.draw();
        String player1Cards = drawCards(player1.getHandCards(), true);
        String player2Cards = drawCards(player2.getHandCards(), true);
        write(player1.getWriter(), text.concat(player2info).concat("\n").concat(player2Cards).concat(player1Cards).concat(" ").concat(player1info));
        write(player2.getWriter(), text.concat(player1info).concat("\n").concat(player1Cards).concat(player2Cards).concat(" ").concat(player2info));
        flush(player1.getWriter());
        flush(player2.getWriter());

    }
    public void drawStoreState(Player player, boolean hideIndex){
        String text = CLEAR_SCREEN;
        for(Player p : this.players){
            text = text.concat(p.draw());
        }
        text = text.concat("\n").concat(drawCards(player.getStoreCards(), hideIndex));
        text = text.concat(drawCards(player.getHandCards(), hideIndex));

        write(player.getWriter(), text);
        flush(player.getWriter());

    }

    public String drawCards(List<Card> cards, boolean hideIndex){
        String text = "";
        for(int j = 0; j < CARD_HEIGHT; j++){
            if(!cards.isEmpty()) {
                text = text.concat(" ");
                if (j == 0) text = text.concat(" ");
                else text = text.concat("|");
                for (Card card : cards) {
                    text = text.concat(card.draw(j, CARD_HEIGHT, hideIndex));
                }
            }
            text = text.concat("\n");
        }

        return text;
    }



}