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

    private int FIGHT_TIMEOUT = 30;

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
            player.resetEffects();
            player.resetStoreCards();
            player.resetPlayerGameInfo();
            writers.add(player.getWriter());
        }

        broadcast("Game started!", '0');

        this.players = players;
        this._gamemode = gamemode;
    }


    public void run() throws IOException {
        System.out.println("Game started");

        this.players.sort((p1, p2) -> (p2.getHealth() - p1.getHealth()));

        for(Player player : this.players) {
            refillStore(player);
            drawStoreState(player, false);
            write(player.getWriter(), "", '0');
            flush(player.getWriter());
        }


        while (this.state != State.END) {
            switch(this.state){
                case STORE -> {
                    System.out.print("");
                    for(int i = 0; i < this.players.size(); i++) {
                        Player player = this.players.get(i);
                        if(player.getTimerTask().getTimedOut()){
                            leaveGame(player);
                            continue;
                        }
                        if(!player.getTimerTask().getDisconnected() && player.getTimerTask().getAlreadyDisconnectedOnce())
                            reconnectPlayer(player);

                        String move = getInputAndVerifyConnection(player);
                        if(move != null){
                            storeHandler(player, move);
                        }


                        if(this.finishedStatePlayersCount >= this.players.size()) {
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
                        fightHandler();

                        if(this.finishedStatePlayersCount >= players.size() || timerTask.getTime() > FIGHT_TIMEOUT){
                            FIGHT_TIMEOUT++;
                            this.finishedStatePlayersCount = 0;
                            this.fights.clear();
                            this.state = State.STORE;
                            this.players.sort((p1, p2) -> (p2.getHealth() - p1.getHealth()));
                            for(Player player : this.players){
                                player.resetEffects();
                                refillStore(player);
                                drawStoreState(player, false);
                                write(player.getWriter(), "", '0');
                                flush(player.getWriter());
                            }
                            timerTask.setTimer(0);
                            timerTask.resetTimer();
                            if(this.players.size() == 1) state = State.END;
                        }
                    }

                }
            }

        }



        Player winner = this.players.getFirst();
        leaveGame(winner);

        System.out.println("Game ended");
    }

    public String getInputAndVerifyConnection(Player player) throws IOException {
        if(player.getReader().ready() || (player.getTimerTask().getDisconnected() && !player.getTimerTask().getAlreadyDisconnectedOnce())){
            List<String> response;
            try {
                response = read(player.getReader());
            }catch(SocketException e){
                player.getTimerTask().setAlreadyDisconnectedOnce(true);
                return null;
            }
            player.getTimerTask().resetConnectionTime();
            if(!response.getFirst().equals(Character.toString(ALIVE_ENCODE))){
                return response.getLast();
            }

        }
        return null;
    }

    public void reconnectPlayer(Player player){
        player.getTimerTask().setAlreadyDisconnectedOnce(false);
        if(this.state == State.STORE){
            drawStoreState(player, false);
            write(player.getWriter(), "", '0');
            flush(player.getWriter());
        }


    }

    public void reconnectFight(Player player1, Player player2) throws IOException {
        if(!player1.getTimerTask().getDisconnected() && player1.getTimerTask().getAlreadyDisconnectedOnce())
            reconnectPlayer(player1);
        if(!player2.getTimerTask().getDisconnected() && player2.getTimerTask().getAlreadyDisconnectedOnce())
            reconnectPlayer(player2);

        getInputAndVerifyConnection(player1);
        getInputAndVerifyConnection(player2);
    }

    public void storeHandler(Player currentPlayer, String userInput){
        userInput = userInput.toLowerCase();
        MoveType moveType = getMoveType(currentPlayer, userInput);
        makeMove(currentPlayer, moveType, userInput);



    }

    public void fightHandler() throws IOException {
        for(int i = 0; i < this.fights.size(); i++){
            List<Player> fight = this.fights.get(i);
            Player player1 = fight.getFirst();
            Player player2 = fight.getLast();
            reconnectFight(player1, player2);

            player1.triggerCardCooldownEffects(player2);
            player2.triggerCardCooldownEffects(player1);
            drawFightState(fight);
            if(player1.getHealth() <= 0 || player2.getHealth() <= 0){
                finishedStatePlayersCount += 2;
                this.fights.remove(i);
                i--;
                if(player1.getHealth() <= 0 && player2.getHealth() <= 0 && player2.getHealth() < player1.getHealth()){
                    leaveGame(player2);
                    leaveGame(player1);
                }
                else if(player1.getHealth() <= 0) leaveGame(player1);
                else if(player2.getHealth() <= 0)leaveGame(player2);


            }
        }

    }

    public void leaveGame(Player player){

        if(_gamemode == 'b'){
            player.updateRank(currentScore, this.players.size() == 1);
            if(this.players.size() > 1)currentScore += SCORE_RANGE / (this.players.size() - 1);
        }

        for(int i = 0; i < this.players.size(); i++){
            if(this.players.get(i).getName().equals(player.getName())){
                this.players.remove(i);
                break;
            }
        }
        if(this.players.isEmpty()) write(player.getWriter(), "Congratulations, you won!", '1');
        else write(player.getWriter(), "You lost!", '1');
        flush(player.getWriter());
        player.setInGame(false);
    }

    public MoveType getMoveType(Player currentPlayer, String userInput){
        int maxIndice = currentPlayer.getStoreCardsSize() + currentPlayer.getHandCardsCount();
        if(userInput.length() > 5) {
            write(currentPlayer.getWriter(), "Please insert a valid input!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
        }
        if(userInput.equals("r")) {
            if(currentPlayer.getGold() < 1){
                write(currentPlayer.getWriter(), "You don't have enough gold to reroll!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            return MoveType.REROLL;
        }
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
        if(cardIndice == 0) return MoveType.ENDTURN;
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
            else if(currentPlayer.getHandWidth()+card.getWidth() > MAX_WIDTH && !card.isInstant()){
                write(currentPlayer.getWriter(), "You don't have space for this card!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
            return MoveType.BUY;
        }
        Card card = currentPlayer.getHandCard(cardIndice-currentPlayer.getStoreCardsSize());
        if(card.getType() == 0){
            if(currentPlayer.getGold() < card.getGold()){
                write(currentPlayer.getWriter(), "You don't have enough money to buy the lock!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
        }
        return MoveType.SELL;

    }

    public void makeMove(Player currentPlayer, MoveType moveType, String userInput){
        switch(moveType){
            case BUY ->{
                int cardIndice = Integer.parseInt(userInput)-1;
                Card card = currentPlayer.getStoreCard(cardIndice);
                if(!card.isInstant())currentPlayer.addHandCard(card);
                currentPlayer.setGold(currentPlayer.getGold()-card.getGold());
                card.triggerOnBuyEffect(currentPlayer);
                currentPlayer.removeStoreCard(cardIndice);
                currentPlayer.reorderCardIndices();
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SELL -> {
                int cardIndice = Integer.parseInt(userInput)-currentPlayer.getStoreCardsSize()-1;
                Card card = currentPlayer.getHandCard(cardIndice);
                int goldOffset = 1;
                if(card.getType() == 0) {
                    goldOffset = -card.getGold();
                    currentPlayer.increaseLockCosts();
                }
                currentPlayer.setGold(currentPlayer.getGold()+goldOffset);
                currentPlayer.removeHandCard(cardIndice);
                currentPlayer.reorderCardIndices();
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());

            }
            case REROLL -> {
                refillStore(currentPlayer);
                currentPlayer.setGold(currentPlayer.getGold()-1);
                currentPlayer.reorderCardIndices();
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SWAP -> {
                String[] splitInput = userInput.split("-");
                int cardIndice1 = Integer.parseInt(splitInput[0])-currentPlayer.getStoreCardsSize()-1;
                int cardIndice2 = Integer.parseInt(splitInput[1])-currentPlayer.getStoreCardsSize()-1;
                currentPlayer.swapCards(cardIndice1, cardIndice2);
                currentPlayer.reorderCardIndices();
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
        player.reorderCardIndices();

    }




    public void drawFightState(List<Player> fight){
        Player player1 = fight.getFirst();
        Player player2 = fight.getLast();

        String text = CLEAR_SCREEN;
        String player1info = player1.draw(true);
        String player2info = player2.draw(true);
        String player1Cards = drawCards(player1.getHandCards(), true, true);
        String player2Cards = drawCards(player2.getHandCards(), true, true);
        String timer = Integer.toString(FIGHT_TIMEOUT-timerTask.getTime()).concat("s");
        write(player1.getWriter(), text.concat(player2info).concat("         ").concat(timer).concat("\n").concat(player2Cards).concat(player1Cards).concat("\n").concat(" ").concat(player1info));
        write(player2.getWriter(), text.concat(player1info).concat("         ").concat(timer).concat("\n").concat(player1Cards).concat(player2Cards).concat("\n").concat(" ").concat(player2info));
        flush(player1.getWriter());
        flush(player2.getWriter());

    }
    public void drawStoreState(Player player, boolean hideIndex){
        String text = CLEAR_SCREEN;
        if(!hideIndex)
            text = text.concat(" (0)END TURN");
        for(Player p : this.players){
            text = text.concat(p.draw(false));
        }
        text = text.concat("\n").concat(drawCards(player.getStoreCards(), hideIndex, false));
        text = text.concat(drawCards(player.getHandCards(), hideIndex, false));
        text = text.concat("\n").concat(player.draw(true));

        write(player.getWriter(), text);
        flush(player.getWriter());

    }

    public String drawCards(List<Card> cards, boolean hideIndex, boolean hideGold){
        String text = "";
        for(int j = 0; j < CARD_HEIGHT; j++){
            if(!cards.isEmpty()) {
                text = text.concat(" ");
                if (j == 0) text = text.concat(" ");
                else text = text.concat("|");
                for (Card card : cards) {
                    text = text.concat(card.draw(j, CARD_HEIGHT, hideIndex, hideGold));
                }
            }
            text = text.concat("\n");
        }

        return text;
    }



}