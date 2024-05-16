import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Player{

    //TIMER

    private int _time = 0;

    private int _previousTimerTime = 0;

    private int _timerInterval;

    private int _disconnectionTimeout;

    private int _disconnectionTime;

    private boolean _timedOut = true;




    private boolean _isDisconnected = true;

    private boolean _alreadyDisconnectedOnce = false;

    //GAME
    private final String _name;

    private final String _password;

    private int _rank;

    private int _previousRank;

    private boolean _hasBeenWrittenToDB = true;


    private PrintWriter _writer = null;

    private BufferedReader _reader = null;

    private String _serverState = "MENU";

    private List<Card> hand = new ArrayList<>();

    private List<Card> storeCards = new ArrayList<>();

    private int _handWidth = 1;

    private int _maxHealth = 300;

    private int _health = 300;

    private int _gold = 5;

    private int _originalSpeed = 0;

    private int _speed = 0;

    private int _originalStrength = 0;

    private int _strength = 0;

    private int _originalArmor = 0;

    private int _armor = 0;

    private int _originalArmorBuffing = 0;

    private int _armorBuffing = 0;

    private boolean _inGame = false;

    private boolean _isFighting = false;

    private final List<Integer> _skills = new ArrayList<>();

    private Card.Type _encounter = null;

    private int _daysRemaining = 6;


    //initializes the player class
    public Player(String name, String password, int rank){
        this._name = name;
        this._password = password;
        this._rank = rank;
        this._previousRank = this._rank;
        this._previousTimerTime = -1;
        this._disconnectionTimeout = Server.DISCONNECT_TIMEOUT;
        this._disconnectionTime = this._disconnectionTimeout;
        this._timerInterval = Server.TIMER_INTERVAL/1000;

        setTimer(0);

        resetPlayerGameInfo();
    }

    //TIMER

    public void setTimer(int time){this._time = time;}

    public void setDisconnected(boolean isDisconnected){
        this._isDisconnected = isDisconnected;
    }

    public void setTimedOut(boolean timedOut){this._timedOut = timedOut;}

    public void setAlreadyDisconnectedOnce(boolean alreadyDisconnectedOnce){this._alreadyDisconnectedOnce = alreadyDisconnectedOnce;}



    //resets the timer information
    public void resetTimer(int time){
        this._previousTimerTime = time;
        this._disconnectionTime = this._disconnectionTimeout;
        this._isDisconnected = false;
        this._timedOut = false;
    }

    //checks if the time interval set on the timer has passed and if player is disconnected it will count the timeout.
    public boolean timeChanged(int currentTime) {
        if(this._previousTimerTime >= currentTime+this._timerInterval || this._previousTimerTime+this._timerInterval <= currentTime){
            this._previousTimerTime = currentTime;
            this._time++;
            this._time = this._time % 100000000;
            if(this._isDisconnected && !this._timedOut) {
                this._disconnectionTime--;
                System.out.println("TIME OUT IS ON: ".concat(Integer.toString(this._disconnectionTime)));
            }
            else this._disconnectionTime = this._disconnectionTimeout;

            if(this._disconnectionTime <= 0) {
                this._timedOut = true;
            }
            return true;
        }
        return false;
    }



    public int getTime(){return this._time;}

    public boolean getDisconnected(){return this._isDisconnected;}

    public boolean getTimedOut(){return this._timedOut;}

    public boolean getAlreadyDisconnectedOnce(){return this._alreadyDisconnectedOnce;}

    //GAME

    //resets the player game info for when he starts a new game
    public void resetPlayerGameInfo(){
        this._health = 300;
        this._maxHealth = 300;
        this._gold = 5;
        this._speed = 0;
        this._originalSpeed = 0;
        this._strength = 0;
        this._originalStrength = 0;
        this._originalArmor = 0;
        this._armor = 0;
        this._originalArmorBuffing = 0;
        this._armorBuffing = 0;
        this._handWidth = 1;
        this.hand.clear();
        this._isFighting = false;
        this._skills.clear();
        this._encounter = null;
        for(int i = 0; i < Card.BOOK_COUNT; i++){
            this._skills.add(0);
        }
        this._daysRemaining = 6;
        for(int i = 0; i < 4; i++){
            Card lock = new Card(0);
            this.hand.add(lock);
            this._handWidth += lock.getWidth();
        }
        this.storeCards.clear();

    }

    //verifies if the user needs to update his database rank
    public boolean hasRankChanged(){return this._rank != this._previousRank;}

    public String getServerState(){return this._serverState;}

    public boolean getInGame() { return this._inGame; }

    public boolean hasBeenWrittenToDB(){return this._hasBeenWrittenToDB;}

    public Card.Type getEncounter(){
        return this._encounter;
    }

    public int getDaysRemaining(){return this._daysRemaining;}
    public String getName(){
        return this._name;
    }
    public String getPassword(){return this._password;}

    public int getRank(){
        return this._rank;
    }

    public PrintWriter getWriter() { return this._writer; }

    public BufferedReader getReader() { return this._reader; }

    public boolean isFighting(){return this._isFighting;}

    public List<Card> getStoreCards(){return this.storeCards;}

    public int getStoreCardsSize(){return this.storeCards.size();}

    public Card getStoreCard(int cardIndice){return this.storeCards.get(cardIndice);}

    public Card getHandCard(int cardIndice){
        if(cardIndice < 0 || cardIndice >= getHandCardsCount()) return null;
        return this.hand.get(cardIndice);
    }

    public int getHandWidth(){return this._handWidth;}

    public int getGold(){return this._gold;}

    public int getSpeed(){return this._speed;}

    public int getOriginalSpeed(){return this._originalSpeed;}

    public int getStrength(){return this._strength;}

    public int getArmorBuffing(){return this._armorBuffing;}

    public int getOriginalArmorBuffing(){return this._originalArmorBuffing;}

    public int getOriginalStrength(){return this._originalStrength;}
    public int getHealth(){return this._health;}

    public int getMaxHealth(){return this._maxHealth;}

    public int getArmor(){return this._armor;}

    public int getOriginalArmor(){return this._originalArmor;}

    public int getHandCardsCount() { return this.hand.size(); }

    public List<Card> getHandCards(){return this.hand;}

    //updates the rank after the game has ended. Rank is updated based of the current rank that you have and the position that you finished in the game
    public void updateRank(int score, boolean isWinner){

        if(isWinner && score - this._rank/10 < 1){
            this._rank += 1;
        }
        else this._rank += score - this._rank/10;

        if(this._rank < 0) this._rank = 0;
    }

    //activates a skill that is bought from the game store
    public void activateSkill(int index){
        this._skills.set(index, this._skills.get(index)+1);
    }

    //deactivates a skill that is sold from the game store
    public void deactivateSkill(int index){
        this._skills.set(index, this._skills.get(index)-1);
    }

    //verifies if the skill is active or not
    public boolean isSkillActive(Card.BookType bookType){
        return this._skills.get(bookType.ordinal()) > 0;
    }

    public void setInGame(boolean inGame) { this._inGame = inGame; }

    public void setPreviousRank(){this._previousRank = this._rank;}

    public void setEncounter(Card.Type encounter){this._encounter = encounter;}

    public void setDaysRemaining(int daysRemaining){this._daysRemaining = daysRemaining;}

    public void setIsFighting(boolean isFighting){this._isFighting = isFighting;}

    public void setServerState(String serverState){this._serverState = serverState;}

    public void setHasBeenWrittenToDB(boolean hasBeenWrittenToDB){this._hasBeenWrittenToDB = hasBeenWrittenToDB;}

    public void setReader(BufferedReader reader){this._reader = reader;}

    public void setWriter(PrintWriter writer){
        this._writer = writer;
    }

    public void setGold(int gold){this._gold = gold;}

    //sets the players speed gained during the fight and updates all cards affected by it
    public void setSpeed(int speed){
        if(_isFighting) {
            for (Card card : this.hand) {
                int cooldown = card.getOrignalCooldown();
                if (cooldown > 0) {
                    card.setCooldown(card.getCooldown() - (Math.max(Math.min(100, speed), 0) - this._speed) * cooldown / 100);
                }
            }
        }
        this._speed = Math.min(speed, 100);
    }
    //sets the players permanent speed and updates all cards affected by it
    public void setOriginalSpeed(int speed){
        speed = Math.max(Math.min(speed, 100), 0);
        setSpeed(this._speed + speed - this._originalSpeed);
        this._originalSpeed = speed;
    }
    //sets the players strength gained during the fight and updates all cards affected by it
    public void setStrength(int strength){
        if(_isFighting) {
            for (Card card : this.hand) {
                card.setDamage(card.getDamage() + strength - this._strength);
                if(isSkillActive(Card.BookType.CONDITIONING) && card.getOrignalArmor() >= 0){
                    card.setArmor(card.getArmor() + strength - this._strength);
                }
            }
        }
        this._strength = strength;

    }
    //sets the players permanent strength and updates all cards affected by it
    public void setOriginalStrength(int strength){
        setStrength(this._strength + strength - this._originalStrength);
        this._originalStrength = strength;
    }
    //sets the players health gained during the fight and updates all cards affected by it
    public void setHealth(int health){
        if(_isFighting && this._maxHealth-this._health > 0){
            for(Card card : this.hand){
                card.triggerOnGainingHealthEffect(this);
            }
        }
        this._health = Math.min(health, this._maxHealth);
    }
    //sets the players armor buffing gained during the fight and updates all cards affected by it
    public void setArmorBuffing(int armorBuff){
        if(_isFighting){
            for(Card card : this.hand){
                if(card.getOrignalArmor() >= 0){
                    card.setArmor(card.getArmor() + armorBuff - this._armorBuffing);
                }
            }
        }
        this._armorBuffing = armorBuff;
    }
    //sets the players permanent armor buffing and updates all cards affected by it
    public void setOriginalArmorBuffing(int armorBuff){
        setArmorBuffing(this._armorBuffing+armorBuff-this._originalArmorBuffing);
        this._originalArmorBuffing = armorBuff;
    }
    //sets the players permanent max health and updates all cards affected by it
    public void setMaxHealth(int health){
        this._maxHealth = Math.max(1, health);
        setHealth(Math.min(this._health, this._maxHealth));

    }
    //sets the players armor gained during the fight and updates all cards affected by it
    public void setArmor(int armor){
        if(this._isFighting) {
            for (Card card : this.hand) {
                card.triggerOnGainingArmorEffect();
            }
        }
        this._armor = armor;
    }
    //sets the players permanent armor and updates all cards affected by it
    public void setOriginalArmor(int armor){
        setArmor(this._armor + armor - this._originalArmor);
        this._originalArmor = armor;
    }

    //clears the store
    public void resetStoreCards(){this.storeCards.clear();}

    //resets all the temporary effects gained during the fight
    public void resetEffects(){
        _gold += 5;
        this._speed = this._originalSpeed;
        this._strength = this._originalStrength;
        this._armor = this._originalArmor;
        this._armorBuffing = this._originalArmorBuffing;
        this._encounter = null;
        this._daysRemaining = 6;
        for(Card card : this.hand){
            card.resetStats();
        }

    }

    //adds a new card to the store
    public void addStoreCard(Card card){this.storeCards.add(card);}

    public void setStoreCards(List<Card> storeCards){this.storeCards = storeCards;}

    //removes a card from the store
    public void removeStoreCard(int cardIndice){
        this.storeCards.remove(cardIndice);
    }

    //removes a card from your hand
    public void removeHandCard(int cardIndice){
        this._handWidth -= this.hand.get(cardIndice).getWidth();
        this.hand.remove(cardIndice);
        if(isSkillActive(Card.BookType.REGULAR_CUSTOMER)){
            setHealth(this._health+25);
        }
    }

    //increases all locks
    public void increaseLockCosts(){
        for(Card card : this.hand){
            if(card.getType() == Card.Type.LOCK){
                card.setGold(card.getGold()+2);
            }
        }
    }

    //adds a new card to your hand
    public void addHandCard(Card card){
        int rand = card.getRand();
        Card newCard = new Card(card.getType().ordinal());
        this.hand.add(new Card(card.getType().ordinal()));
        this._handWidth += card.getWidth();
        if(rand != -1) newCard.randomize(rand);
    }

    //swaps 2 cards positions from your hand
    public void swapCards(int cardIndice1, int cardIndice2){

        Card card1 = this.hand.get(cardIndice1);
        this.hand.set(cardIndice1, this.hand.get(cardIndice2));
        this.hand.set(cardIndice2, card1);
    }

    //reorders the indices of the cards in the correct format and triggers any on moving effects that cards might have
    public void reorderCardIndices(){
        int cardIndice = 1;
        for(Card card : this.storeCards){
            card.setIndex(cardIndice);
            cardIndice++;
        }
        for(Card card : this.hand){
            if(card.getOriginalDamage() >= 0){
                card.setDamage(card.getOriginalDamage()+this._originalStrength);
            }
            if(card.getOrignalCooldown() > 0)card.setCooldown(card.getOrignalCooldown()-(this._originalSpeed+card.getSpeed())*card.getOrignalCooldown()/100);
            card.setIndex(cardIndice);
            if(card.getArmor() >= 0){
                card.setArmor(card.getOrignalArmor()+this._armorBuffing);
                if(isSkillActive(Card.BookType.CONDITIONING)){
                    card.setArmor(card.getArmor()+this._originalStrength);
                }
            }
            cardIndice++;
        }

        for(int i = 0; i < this.hand.size(); i++){
            Card card = this.hand.get(i);
            Card left;
            Card right;
            if(i == 0) left = null;
            else left = this.hand.get(i-1);
            if(i == this.hand.size()-1)right = null;
            else right = this.hand.get(i+1);
            card.triggerOnMove(this, left, right, i);
        }

    }

    //take damage
    public void takeDamage(int damage){
        if(this._armor > 0){
            this._armor = Math.max(0, this._armor - damage);
        }
        else{
            this._health -= damage;
        }
    }

    //trigger cards cooldown effects
    public void triggerCardCooldownEffects(Player enemyPlayer){
        for(int i = 0; i < this.hand.size(); i++){
            Card card = this.hand.get(i);

            card.triggerCooldownEffect(this, enemyPlayer, i);
        }
    }

    //draw the player's information on the terminal
    public String draw(boolean showStats){
        String text = "";
        text = text.concat(" |").concat(_name);
        if(!showStats){
            text = text.concat(" #").concat(Integer.toString(_rank));
            text = text.concat(" +").concat(Integer.toString(_health));
        }
        if(showStats){
            if(!_isFighting)text = text.concat(" $").concat(Integer.toString(_gold));
            if(_speed > 0)text = text.concat(" Speed: ").concat(Integer.toString(_speed));
            if(_strength > 0)text = text.concat(" Strength: ").concat(Integer.toString(_strength));
            text = text.concat(" Health: ").concat(Integer.toString(_health));
            if(!_isFighting)text = text.concat("/").concat(Integer.toString(_maxHealth));
            if(_armor > 0) text = text.concat("+").concat(Integer.toString(_armor));

            text = text.concat(" ");
            for(int i = 0; i < this._health; i+=3){
                text = text.concat("x");
            }
            for(int i = 0; i < this._armor; i+=3){
                text = text.concat("O");
            }
            for(int i = 0; i < this._maxHealth-this._health-this._armor;i+=3){
                text = text.concat("-");
            }
        }
        text = text.concat(" |");
        return text;
    }

}
