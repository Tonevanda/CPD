import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;

public class Game extends Communication{
    private final List<Player> players;

    final private int CARD_HEIGHT = 18;

    private final List<Card> items;

    private final List<Card> encounters;

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
        ENDTURN
    }

    private State state = State.STORE;

    private MyTimerTask timerTask;

    private final int TIMER_INTERVAL = 1000;

    private final int STORE_TIMEOUT = 120;

    private int FIGHT_TIMEOUT = 30;

    private final int MAX_WIDTH = 156;

    private int finishedStatePlayersCount = 0;

    private Player nonPlayingPlayer = null;

    //initializes the class Game
    public Game(List<Player> players, List<Card> encounters, List<Card> items, char gamemode, MyTimerTask timerTask){

        this.encounters = encounters;
        this.items = items;
        this.timerTask = timerTask;

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

    //runs the game putting it through a state machine
    public void run() throws IOException {
        System.out.println("Game started");

        this.players.sort((p1, p2) -> (p2.getHealth() - p1.getHealth()));

        for(Player player : this.players) {
            refillStore(player);
            drawStoreState(player, false);
            write(player.getWriter(), "", '0');
            flush(player.getWriter());
        }
        int time = FIGHT_TIMEOUT;
        int previous_time = timerTask.getTime();
        while (this.state != State.END) {
            switch(this.state){
                case STORE -> {
                    System.out.print("");
                    if(this.players.size() <= 1) state = State.END;
                    for(int i = 0; i < this.players.size(); i++) {
                        Player player = this.players.get(i);
                        if(player.getTimedOut()){
                            leaveGame(player);
                            i--;
                            continue;
                        }
                        if(!player.getDisconnected() && player.getAlreadyDisconnectedOnce())
                            reconnectPlayer(player);

                        String move = getInputAndVerifyConnection(player);

                        if(move != null){
                            storeHandler(player, move);
                        }

                        if(this.finishedStatePlayersCount >= this.players.size() && this.players.size() > 1) {
                            time = FIGHT_TIMEOUT;
                            previous_time = timerTask.getTime();
                            this.finishedStatePlayersCount = 0;
                            this.state = State.FIGHT;
                            Collections.shuffle(this.players);
                            List<Player> fight = new ArrayList<>();
                            if(nonPlayingPlayer != null){
                                fight.add(nonPlayingPlayer);
                            }
                            for(Player p : this.players){
                                if(nonPlayingPlayer != null && p.getName().equals(nonPlayingPlayer.getName())) continue;
                                p.setIsFighting(true);
                                fight.add(p);
                                if(fight.size() == 2){
                                    this.fights.add(fight);
                                    drawFightState(fight, time);
                                    fight = new ArrayList<>();
                                }
                            }
                            if(!fight.isEmpty()){
                                nonPlayingPlayer = fight.getFirst();
                                nonPlayingPlayer.setIsFighting(false);
                            }
                        }
                    }
                }
                case FIGHT -> {
                    System.out.print("");
                    if(timerTask.getTime() >= previous_time+TIMER_INTERVAL/1000 || timerTask.getTime()+TIMER_INTERVAL/1000 <= previous_time){
                        previous_time = timerTask.getTime();
                        time--;
                        if(nonPlayingPlayer != null){
                            if(nonPlayingPlayer.timeChanged(this.timerTask.getTime()))getInputAndVerifyConnection(nonPlayingPlayer);
                            if(!nonPlayingPlayer.getDisconnected() && nonPlayingPlayer.getAlreadyDisconnectedOnce())
                                reconnectPlayer(nonPlayingPlayer);
                            if(nonPlayingPlayer.getTimedOut()){
                                leaveGame(nonPlayingPlayer);
                                nonPlayingPlayer = null;
                            }
                        }
                        fightHandler(time);

                        if(this.finishedStatePlayersCount >= players.size() || time <= 0){
                            FIGHT_TIMEOUT++;
                            this.finishedStatePlayersCount = 0;
                            this.fights.clear();
                            this.state = State.STORE;
                            this.players.sort((p1, p2) -> (p2.getHealth() - p1.getHealth()));
                            for(Player player : this.players){
                                if(nonPlayingPlayer != null && nonPlayingPlayer.getName().equals(player.getName())){
                                    player.setGold(player.getGold()+3);
                                }
                                else player.resetEffects();

                                player.setIsFighting(false);
                                refillStore(player);
                                drawStoreState(player, false);
                                write(player.getWriter(), "", '0');
                                flush(player.getWriter());
                            }
                            if(this.players.size() <= 1) state = State.END;
                        }
                    }
                }
            }
        }

        if(!this.players.isEmpty()) {
            Player winner = this.players.getFirst();
            leaveGame(winner);
        }

        System.out.println("Game ended");
    }

    //gets the user input and also verifies connection by verifying if socket is still open for connection.
    public String getInputAndVerifyConnection(Player player) throws IOException {
        if(!player.getDisconnected()) {
            List<String> response;
            try {
                response = readNonBlocking(player.getReader());
            } catch (SocketException | SSLException e) {
                System.out.println("DISCONNECTION: ".concat(player.getName()));
                player.setAlreadyDisconnectedOnce(true);
                player.setDisconnected(true);
                return null;
            }


            if (response != null) {
                return response.getLast();
            }
        }

        return null;
    }

    //reconnects player back to the game after a disconnection
    public void reconnectPlayer(Player player){
        player.setAlreadyDisconnectedOnce(false);
        if(this.state == State.STORE){
            drawStoreState(player, false);
            write(player.getWriter(), "", '0');
            flush(player.getWriter());
        }
    }

    //reconnects a fight whenever at least one of the players disconnects from it
    public void reconnectFight(Player player1, Player player2){
        if(!player1.getDisconnected() && player1.getAlreadyDisconnectedOnce())
            reconnectPlayer(player1);
        if(!player2.getDisconnected() && player2.getAlreadyDisconnectedOnce())
            reconnectPlayer(player2);



    }

    //handles the store phase logic part of the game
    public void storeHandler(Player currentPlayer, String userInput){
        userInput = userInput.toLowerCase();
        MoveType moveType = getMoveType(currentPlayer, userInput);
        makeMove(currentPlayer, moveType, userInput);
    }

    //handles the fighting phase logic part of the game
    public void fightHandler(int time) throws IOException {

        for(int i = 0; i < this.fights.size(); i++){
            List<Player> fight = this.fights.get(i);
            Player player1 = fight.getFirst();
            Player player2 = fight.getLast();
            if(player1.timeChanged(this.timerTask.getTime())) getInputAndVerifyConnection(player1);
            if(player2.timeChanged(this.timerTask.getTime()))getInputAndVerifyConnection(player2);
            reconnectFight(player1, player2);

            player1.triggerCardCooldownEffects(player2);
            player2.triggerCardCooldownEffects(player1);
            drawFightState(fight, time);
            if(player1.getHealth() <= 0 || player2.getHealth() <= 0){
                finishedStatePlayersCount += 2;
                this.fights.remove(i);
                i--;
                if(player1.getHealth() <= 0 && player2.getHealth() <= 0 && player2.getHealth() < player1.getHealth()){
                    leaveGame(player2);
                    leaveGame(player1);
                }
                else if(player1.getHealth() <= 0) leaveGame(player1);
                if(player2.getHealth() <= 0)leaveGame(player2);
            }
        }
    }

    //makes a player leave the game, updating its rank and removing him from the list of game players.
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
        if(this.nonPlayingPlayer != null && this.nonPlayingPlayer.getName().equals(player.getName())) this.nonPlayingPlayer = null;
        if(this.players.isEmpty()) write(player.getWriter(), "Congratulations, you won!", '1');
        else write(player.getWriter(), "You lost!", '1');
        flush(player.getWriter());
        player.setInGame(false);
    }

    //gets the move type defined by the user's input or an invalid move if the move is not valid
    public MoveType getMoveType(Player currentPlayer, String userInput){
        int maxIndice = currentPlayer.getStoreCardsSize() + currentPlayer.getHandCardsCount();
        if(userInput.length() > 5) {
            write(currentPlayer.getWriter(), "Please insert a valid input!", '0');
            flush(currentPlayer.getWriter());
            return MoveType.INVALID;
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
        if(card.getType() == Card.Type.LOCK){
            if(currentPlayer.getGold() < card.getGold()){
                write(currentPlayer.getWriter(), "You don't have enough money to buy the lock!", '0');
                flush(currentPlayer.getWriter());
                return MoveType.INVALID;
            }
        }
        return MoveType.SELL;
    }

    //the user makes a move given an already processed user input
    public void makeMove(Player currentPlayer, MoveType moveType, String userInput){
        switch(moveType){
            case BUY ->{
                int cardIndice = Integer.parseInt(userInput)-1;
                Card card = currentPlayer.getStoreCard(cardIndice);
                for(Card c : currentPlayer.getHandCards()){
                    c.triggerAfterBuyingEffect(card);
                }
                if(!card.isInstant())currentPlayer.addHandCard(card);
                currentPlayer.setGold(currentPlayer.getGold()-card.getGold());
                card.triggerOnBuyEffect(currentPlayer);

                //currentPlayer.removeStoreCard(cardIndice);
                refillStore(currentPlayer);
                currentPlayer.reorderCardIndices();
                drawStoreState(currentPlayer, false);
                write(currentPlayer.getWriter(), "", '0');
                flush(currentPlayer.getWriter());
            }
            case SELL -> {
                int cardIndice = Integer.parseInt(userInput)-currentPlayer.getStoreCardsSize()-1;
                Card card = currentPlayer.getHandCard(cardIndice);
                int goldOffset = 1;
                if(card.getType() == Card.Type.LOCK) {
                    goldOffset = -card.getGold();
                    currentPlayer.increaseLockCosts();
                }
                currentPlayer.setGold(currentPlayer.getGold()+goldOffset);
                card.triggerOnSellEffect(currentPlayer);
                currentPlayer.removeHandCard(cardIndice);
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


    //refills the content of the store
    private void refillStore(Player player){
        player.resetStoreCards();
        int storeWidth = 1;
        int storeIndex = 0;
        int MIN_CARD_WIDTH = 11;
        if(player.getEncounter() == null){
            player.setDaysRemaining(player.getDaysRemaining()-1);
            if(player.getDaysRemaining() > 0) {
                Collections.shuffle(this.encounters);
                while (storeWidth <= MAX_WIDTH - MIN_CARD_WIDTH || storeIndex == encounters.size()) {
                    Card card = this.encounters.get(storeIndex);
                    storeIndex++;
                    if (card.getWidth() + storeWidth <= MAX_WIDTH) {
                        player.addStoreCard(new Card(card.getType().ordinal()));
                        storeWidth += card.getWidth();
                        Collections.shuffle(this.encounters);
                        storeIndex = 0;
                        if (player.getStoreCardsSize() == 3) break;
                    }
                }
            }
        }
        else {

            switch (player.getEncounter()) {
                case MERCHANT -> {
                    Card coinCard = new Card(Card.Type.COIN.ordinal());
                    player.addStoreCard(coinCard);
                    storeWidth += coinCard.getWidth();
                    Collections.shuffle(this.items);
                    while (storeWidth <= MAX_WIDTH - MIN_CARD_WIDTH || storeIndex == items.size()) {
                        Card card = this.items.get(storeIndex);
                        storeIndex++;
                        if (card.getWidth() + storeWidth <= MAX_WIDTH && (card.getGold() <= player.getGold() || player.getGold() == 0 || player.getStoreCardsSize() == 3)) {
                            player.addStoreCard(new Card(card.getType().ordinal()));
                            storeWidth += card.getWidth();
                            Collections.shuffle(this.items);
                            storeIndex = 0;
                            if (player.getStoreCardsSize() == 4) break;
                        }
                    }
                }

            }
        }





        player.reorderCardIndices();
    }

    //draws the fighting state
    public void drawFightState(List<Player> fight, int time){
        Player player1 = fight.getFirst();
        Player player2 = fight.getLast();

        String text = CLEAR_SCREEN;
        String player1info = player1.draw(true);
        String player2info = player2.draw(true);
        String player1Cards = drawCards(player1, player1.getHandCards(), true, true);
        String player2Cards = drawCards(player2, player2.getHandCards(), true, true);
        String timer = Integer.toString(time).concat("s");
        if(!player1.getTimedOut()) {
            write(player1.getWriter(), text.concat(player2info).concat("         ").concat(timer).concat("\n").concat(player2Cards).concat(player1Cards).concat("\n").concat(" ").concat(player1info));
            flush(player1.getWriter());
        }
        else if(player1.getInGame()){
            player1.setInGame(false);
            leaveGame(player1);
        }
        if(!player2.getTimedOut()) {
            write(player2.getWriter(), text.concat(player1info).concat("         ").concat(timer).concat("\n").concat(player1Cards).concat(player2Cards).concat("\n").concat(" ").concat(player2info));
            flush(player2.getWriter());
        }
        else if(player2.getInGame()){
            player2.setInGame(false);
            leaveGame(player2);
        }

    }

    //draws the store state
    public void drawStoreState(Player player, boolean hideIndex){
        String text = CLEAR_SCREEN;
        if(!hideIndex)
            text = text.concat(" END TURN(0) DAYS REMAINING: ").concat(Integer.toString(player.getDaysRemaining()));
        for(Player p : this.players){
            text = text.concat(p.draw(false));
        }
        text = text.concat("\n").concat(drawCards(null, player.getStoreCards(), hideIndex, false));
        text = text.concat(drawCards(player, player.getHandCards(), hideIndex, false));
        text = text.concat("\n").concat(player.draw(true));


        write(player.getWriter(), text);
        flush(player.getWriter());
    }

    //draws a list of cards
    public String drawCards(Player player, List<Card> cards, boolean hideIndex, boolean hideGoldAndLocks){
        String text = "";
        boolean emptyCards = true;
        for(Card card : cards) {
            if (card.getType() != Card.Type.LOCK || !hideGoldAndLocks){
                emptyCards = false;
                break;
            }
        }
        for(int j = 0; j < CARD_HEIGHT; j++){
            if(!emptyCards && !cards.isEmpty()) {
                text = text.concat(" ");
                if (j == 0) text = text.concat(" ");
                else text = text.concat("|");
                for (Card card : cards) {
                    if(!(hideGoldAndLocks && card.getType() == Card.Type.LOCK)) {
                        int cooldownLinesCount = -1;
                        if (player != null && card.getOrignalCooldown() > 0) {
                            cooldownLinesCount = (CARD_HEIGHT-2)-(card.getCooldown()*(CARD_HEIGHT-2)/(card.getOrignalCooldown()-(player.getSpeed()+card.getSpeed())*card.getOrignalCooldown()/100))-(j-1);
                        }
                        text = text.concat(card.draw(j, CARD_HEIGHT, cooldownLinesCount, hideIndex, hideGoldAndLocks));
                    }
                }
            }
            text = text.concat("\n");
        }
        return text;
    }
}