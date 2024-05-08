import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Player{
    private final String _name;
    private int _rank;

    private int _previousRank;

    private PrintWriter _writer;

    private BufferedReader _reader;

    private Timer _timer;

    private MyTimerTask _timerTask;

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

    private boolean _inGame = false;

    public Player(String name, int rank, PrintWriter writer, BufferedReader reader, int timerInterval, int connectionCheckInterval, int connectionTimeout, int disconnectionTimeout){
        this._name = name;
        this._rank = rank;
        this._previousRank = rank;
        this._writer = writer;
        this._reader = reader;
        this._timer = new Timer();
        this._timerTask = new MyTimerTask(writer, connectionCheckInterval, connectionTimeout, disconnectionTimeout);

        this._timerTask.setMode(0);

        _timer.schedule(_timerTask, 0, timerInterval);

        resetPlayerGameInfo();





    }

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
        this._handWidth = 1;
        this.hand.clear();
        for(int i = 0; i < 4; i++){
            Card lock = new Card(0);
            this.hand.add(lock);
            this._handWidth += lock.getWidth();
        }
        this.storeCards.clear();

    }

    public String getServerState(){return this._serverState;}


    public boolean getInGame() { return this._inGame; }

    public boolean hasPlayerDBInfoChanged(){return this._rank != this._previousRank;}



    public String getName(){
        return this._name;
    }

    public int getRank(){
        return this._rank;
    }

    public PrintWriter getWriter() { return this._writer; }

    public BufferedReader getReader() { return this._reader; }

    public MyTimerTask getTimerTask(){return this._timerTask;}



    public List<Card> getStoreCards(){return this.storeCards;}

    public int getStoreCardsSize(){return this.storeCards.size();}

    public Card getStoreCard(int cardIndice){return this.storeCards.get(cardIndice);}

    public Card getHandCard(int cardIndice){return this.hand.get(cardIndice);}

    public int getHandWidth(){return this._handWidth;}

    public int getGold(){return this._gold;}

    public int getSpeed(){return this._speed;}

    public int getOriginalSpeed(){return this._originalSpeed;}

    public int getStrength(){return this._strength;}

    public int getOriginalStrength(){return this._originalStrength;}
    public int getHealth(){return this._health;}

    public int getMaxHealth(){return this._maxHealth;}

    public int getArmor(){return this._armor;}

    public int getOriginalArmor(){return this._originalArmor;}


    public int getHandCardsCount() { return this.hand.size(); }

    public List<Card> getHandCards(){return this.hand;}

    public void updateRank(int score, boolean isWinner){

        if(isWinner && score - this._rank/10 < 1){
            this._rank += 1;
        }
        else this._rank += score - this._rank/10;

        if(this._rank < 0) this._rank = 0;
    }


    public void setInGame(boolean inGame) { this._inGame = inGame; }

    public void setServerState(String serverState){this._serverState = serverState;}



    public void setReader(BufferedReader reader){this._reader = reader;}

    public void setWriter(PrintWriter writer){
        this._writer = writer;
        this.getTimerTask().setWriter(writer);
    }

    public void setGold(int gold){this._gold = gold;}

    public void setSpeed(int speed){
        this._speed = Math.min(speed, 100);
        for(Card card : this.hand){
            int cooldown = card.getOrignalCooldown();
            if(cooldown > 0){
                card.setCooldown(cooldown - this._speed*cooldown/100);
            }
        }
    }

    public void setOriginalSpeed(int speed){
        speed = Math.max(Math.min(speed, 100), 0);
        setSpeed(this._speed + speed - this._originalSpeed);
        this._originalSpeed = speed;
    }

    public void setStrength(int strength){
        for(Card card : this.hand){
            card.setDamage(card.getDamage() + strength-this._strength);
        }
        this._strength = strength;

    }

    public void setOriginalStrength(int strength){
        setStrength(this._strength + strength - this._originalStrength);
        this._originalStrength = strength;
    }

    public void setHealth(int health){this._health = Math.min(health, this._maxHealth);}

    public void setMaxHealth(int health){
        this._maxHealth = Math.max(1, health);
        setHealth(Math.min(this._health, this._maxHealth));

    }

    public void setArmor(int armor){this._armor = armor;}

    public void setOriginalArmor(int armor){
        setArmor(this._armor + armor - this._originalArmor);
        this._originalArmor = armor;
    }

    public void closeTimer(){this._timer.cancel();}


    public void resetStoreCards(){this.storeCards.clear();}

    public void resetEffects(){
        _gold += 5;
        this._speed = this._originalSpeed;
        this._strength = this._originalStrength;
        this._armor = this._originalArmor;
        for(Card card : this.hand){
            card.resetStats();
        }

    }
    public void addStoreCard(Card card){this.storeCards.add(card);}



    public void removeStoreCard(int cardIndice){
        this.storeCards.remove(cardIndice);
    }

    public void removeHandCard(int cardIndice){
        this._handWidth -= this.hand.get(cardIndice).getWidth();
        this.hand.remove(cardIndice);
    }

    public void increaseLockCosts(){
        for(Card card : this.hand){
            if(card.getType() == 0){
                card.setGold(card.getGold()+2);
            }
        }
    }

    public void addHandCard(Card card){
        this.hand.add(new Card(card.getType()));
        this._handWidth += card.getWidth();
    }

    public void swapCards(int cardIndice1, int cardIndice2){

        Card card1 = this.hand.get(cardIndice1);
        this.hand.set(cardIndice1, this.hand.get(cardIndice2));
        this.hand.set(cardIndice2, card1);
    }

    public void reorderCardIndices(){
        int cardIndice = 1;
        for(Card card : this.storeCards){
            card.setIndex(cardIndice);
            cardIndice++;
        }
        for(Card card : this.hand){
            card.setDamage(card.getOriginalDamage()+this._originalStrength);
            card.setCooldown(card.getOrignalCooldown()-this._originalSpeed*card.getOrignalCooldown()/100);
            card.setIndex(cardIndice);
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
    public void takeDamage(int damage){
        if(this._armor > 0){
            this._armor = Math.max(0, this._armor - damage);
        }
        else{
            this._health -= damage;
        }
    }

    public void triggerCardCooldownEffects(Player enemyPlayer){
        for(int i = 0; i < this.hand.size(); i++){
            Card card = this.hand.get(i);

            card.triggerCooldownEffect(this, enemyPlayer, i);
        }
    }

    public String draw(boolean showStats){
        String text = "";
        text = text.concat("     |").concat(_name);
        text = text.concat(" #").concat(Integer.toString(_rank));
        text = text.concat(" +").concat(Integer.toString(_health));
        if(showStats) text = text.concat("[").concat(Integer.toString(_armor)).concat("]");
        text = text.concat("/").concat(Integer.toString(_maxHealth));
        if(showStats){
            text = text.concat(" $").concat(Integer.toString(_gold));
            text = text.concat(" »").concat(Integer.toString(_speed));
            text = text.concat(" §").concat(Integer.toString(_strength));
        }
        text = text.concat("|");
        return text;
    }


}
