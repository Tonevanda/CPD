import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Card {


    private int _width = 0;






    enum Type{
        LOCK,
        SWORD,
        ANTEATER,
        SHIELD,
        AXE,
        WEIGHTS,
        CASTLE,
        COIN,
        BANDAID,
        BOOTS,
        SHOVEL,
        FENCE,
        TEDDY,
        SCROLL,
        CAT



    }


    final private Type _type;


    private List<String> _art = new ArrayList<>();

    private List<String> _description = new ArrayList<>();

    private int _gold = 0;

    private int _originalCooldown = -1;

    private int _cooldown = -1;

    private int _index = 0;

    private int _originalDamage = 0;

    private int _damage = 0;


    private int _originalArmor = 0;
    private int _armor = 0;


    private boolean _isInstant = false;



    Card(int type){

        this._type = Type.values()[type];
        String ascii = "";
        String power = "";
        switch(this._type){
            case LOCK -> {
                ascii = """
                            .-""-.
                           / .--. \\
                          / /    \\ \\
                          | |    | |
                          | |.-""-.|
                         ///`.::::.`\\
                        ||| ::/  \\:: ;
                        ||; ::\\__/:: ;
                         \\\\\\ '::::' /
                          `=':-..-'`""";
                this._width = 15;
                this._gold = 6;
            }
            case SWORD -> {
                ascii = """
                            ()
                            )(
                         o======o
                            ||
                            ||
                            ||
                            ||
                            ||
                            ||
                            ||
                            ||
                            ||
                            \\/\
                        """;
                this._width = 11;
                this._gold = 1;
                this._cooldown = 7;
                this._damage = 3;
                power = "3 Damage";
            }
            case ANTEATER -> {
                ascii = """
                                       _,,......_
                                    ,-'          `'--.
                                 ,-'  _              '-.
                        (`.    ,'   ,  `-.              `.
                         \\ \\  -    / )    \\               \\
                          `\\`-^^^, )/      |     /         :
                            )^ ^ ^V/            /          '.
                            |      )            |           `.
                            9   9 /,--,\\    |._:`         .._`.
                            |    /   /  `.  \\    `.      (   `.`.
                            |   / \\  \\    \\  \\     `--\\   )    `.`.___
                           .;;./  '   )   '   )       ///'       `-"'
                           `--'   7//\\    ///\\""";

                this._width = 47;
                this._gold = 1;
                this._cooldown = 24;
                this._damage = 50;
                power = "50 Damage";

            }
            case SHIELD -> {
                ascii = """
                           |\\                     /)
                         /\\_\\\\__               (_//
                        |   `>\\-`     _._       //`)
                         \\ /` \\\\  _.-`:::`-._  //
                          `    \\|`    :::    `|/
                                |     :::     |
                                |.....:::.....|
                                |:::::::::::::|
                                |     :::     |
                                 \\    :::    /
                                  `-. ::: .-'
                                   //`:::`\\\\
                                  //   '   \\\\
                                 |/         \\\\""";
                this._width = 29;
                this._gold = 1;
                power = "Adjacent: +6 Damage";
            }
            case AXE -> {
                ascii = """
                          ,  /\\  . \s
                         //`-||-'\\\\\s
                        (| -=||=- |)
                         \\\\,-||-.//\s
                          `  ||  ' \s
                             ||    \s
                             ||    \s
                             ||    \s
                             ||    \s
                             ||    \s
                             ()""";
                this._width = 13;
                this._gold = 1;
                this._cooldown = 12;
                this._damage = 8;
                power = "8 Damage. Bought: 2 Speed";
            }
            case WEIGHTS -> {
                ascii = """
                            _                    _
                          _| |                  | |_
                         | | |__________________| | |
                        [| | |------------------| | |]
                         |_| |                  | |_|
                           |_|                  |_|
                            _                    _
                          _| |                  | |_
                         | | |__________________| | |
                        [| | |------------------| | |]
                         |_| |                  | |_|
                           |_|                  |_|""";
                this._width = 31;
                this._gold = 3;
                this._cooldown = 9;
                power = "3 Strength";
            }
            case CASTLE -> {
                ascii = """
                             |>>>                        |>>>
                         _  _|_  _                   _  _|_  _
                        | |_| |_| |                 | |_| |_| |
                        \\  .      /                 \\ .    .  /
                         \\    ,  /                   \\    .  /
                          | .   |_   _   _   _   _   _| ,   |
                          |    .| |_| |_| |_| |_| |_| |  .  |
                          | ,   | .    .     .      . |    .|
                          | .   |.   ,  _______   .   |   , |
                          |     |  .   /+++++++\\    . | .   |
                          |.    | .    |+++++++| .    |   . |
                          |   . |   ,  |+++++++|.  . _|__   |
                          '--~~__ .    |++++ __|----~    ~`--  \s""";
                this._width = 41;
                this._gold = 4;
                this._cooldown = 9;
                power = "2 Strength, heal 15. Bought: 1 Strength";
            }
            case COIN -> {
                ascii = """
                               _.-'~~`~~'-._
                             .'`  B   E   R  `'.
                            / I               T \\
                          /`       .-'~"-.       `\\
                         ; L      / `-    \\      Y ;
                        ;        />  `.  -.|        ;
                        |       /_     '-.__)       |
                        |        |-  _.' \\ |        |
                        ;        `~~;     \\\\        ;
                         ;          /      \\\\)     ;
                          \\        '.___.-'`"     /
                           `\\                   /`
                             '._   1 9 9 7   _.'
                                `'-..,,,..-'`""";

                this._width = 30;
                this._gold = 2;
                this._isInstant = true;
                power = "Bought: +3$";
            }
            case BANDAID -> {
                ascii = """ 
                         \n \n \n \n
                          /==========================\\
                         / : : : : : |::::| : : : : : \\
                        { : : : : : :|::::|: : : : : : }
                         \\ : : : : : |::::| : : : : : /
                          \\==========================/
                         \n""";

                this._width = 33;
                this._gold = 1;
                this._isInstant = true;
                power = "Bought: Heal 100";
            }
            case BOOTS -> {
                ascii = """
                                      _    _
                                     (_\\__/(,_
                                     | \\ `_////-._
                         _    _      L_/__ "=> __/`\\
                        (_\\__/(,_    |=====;__/___./
                        | \\ `_////-._'-'-'-""\"""\"`
                        J_/___"=> __/`\\
                        |=====;__/___./
                        '-'-'-""\"""\""`
                         \n \n""";
                this._width = 30;
                this._gold = 1;
                power = "20 Speed";
            }
            case SHOVEL -> {
                ascii = """
                                      ____\s
                                     / -- )
                                    (____/
                                    / /
                                   / /
                                  / /
                                 / /
                                / /
                           ____/ /___
                          / .------  )
                         / /        /
                        / /        /""";
                this._width = 20;
                this._gold = 2;
                this._cooldown = 4;
                this._damage = 1;
                power = "1 Damage";
            }
            case FENCE -> {
                ascii = """
                                   ____              \s
                                __/ \\--\\           \s
                                U |_|__|             \s
                                     ||              \s
                                     ||              \s
                         ,   ,   ,   ,|  ,   ,   ,   ,
                        ||  ||  ||  ||| ||  ||  ||  ||
                        ||__||__||__|||_||__||__||__||
                        ..--..--..--..--..--..--..--..
                        ||__||__||__|||_||__||__||__||
                        ..--..--..--..--..--..--..--..
                        ||  ||  ||  ||| ||  ||  ||  ||
                        //\\\\//\\||/|\\/\\||\\\\|//\\|\\|//\\|/""";
                this._width = 31;
                this._gold = 1;
                this._cooldown = 10;
                this._armor = 10;
                power = "10 Armor. Bought: 10 Armor";
            }
            case TEDDY -> {
                ascii = """
                              ,-._____,-.
                             (_c       c_)
                              /  e-o-e  \\
                             (  (._|_,)  )
                              >._`---'_,<
                            ,'/  `---'  \\`.
                          ,' /           \\ `.
                         (  (             )  )
                          `-'\\           /`-'
                             |`-._____.-'|
                             |     Y     |
                             /     |     \\
                            (______|______)""";
                this._width = 22;
                this._gold = 4;
                this._cooldown = 9;
                this._armor = 1;
                power = "1 Armor. +1 Armor per ability triggered";
            }
            case SCROLL -> {
                ascii = """
                             _______________
                        ()==(              (@==()
                             '______________'|
                               |             |
                               |             |
                               |             |
                               |             |
                               |             |
                               |             |
                               |             |
                             __)_____________|
                        ()==(               (@==()
                             '--------------'""";
                this._width = 27;
                this._isInstant = true;
                power = "Bought: -42 Max Health and +3 Strength";
            }
            case CAT -> {
                ascii = """
                                   ___
                                  (___)
                           ____
                         _\\___ \\  |\\_/|
                        \\     \\ \\/ , , \\ ___
                         \\__   \\ \\ ="= //|||\\
                          |===  \\/____)_)||||
                          \\______|    | |||||
                              _/_|  | | =====
                             (_/  \\_)_)    
                          _____________,___
                         (    '  '        _)
                          (________________)""";
                this._width = 22;
                this._gold = 1;
                this._isInstant = true;
                power = "Bought: +10 MaxHealth and Heal 40";
            }
        }

        fillArt(ascii);
        fillDescription(power);
        this._originalCooldown = _cooldown;
        this._originalDamage = _damage;
        this._originalArmor = _armor;

    }

    public void resetStats(){
        setCooldown(this._originalCooldown);
        setDamage(this._originalDamage);

        setArmor(this._originalArmor);
    }

    public void fillDescription(String power){
        if(!power.isEmpty()){
            while(true){
                if(power.length() <= this._width-1){
                    this._description.add(power);
                    break;
                }
                String line = power.substring(0, this._width-1);


                power = power.substring(this._width-1);

                while(!line.endsWith(" ") && !power.startsWith(" ")){
                    power = Character.toString(line.charAt(line.length()-1)).concat(power);
                    line = line.substring(0, line.length()-1);
                }
                this._description.add(line);
            }
        }

    }

    public void fillArt(String ascii){
        if(!ascii.isEmpty())
            this._art = Arrays.asList(ascii.split("\n"));
    }

    public void setIndex(int index){this._index = index;}

    public void setGold(int gold){this._gold = gold;}


    public int getWidth(){return this._width;}

    public int getType(){return this._type.ordinal();}

    public int getGold(){return this._gold;}

    public int getDamage(){return this._damage;}

    public int getOrignalCooldown(){return this._originalCooldown;}

    public int getCooldown(){return this._cooldown;}

    public int getOriginalDamage(){return this._originalDamage;}

    public boolean isInstant(){return this._isInstant;}

    public void setCooldown(int cooldown){
        this._cooldown = cooldown;
        if(this._originalCooldown > 0) this._cooldown = Math.max(this._cooldown, 1);
    }


    public void setDamage(int damage){
        this._damage = damage;
        switch(this._type){
            case SWORD, ANTEATER, SHOVEL -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage"));
            }
            case AXE -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage. Bought: 2 Speed"));
            }
        }
    }

    public void setArmor(int armor){
        this._armor = armor;
        switch(this._type){
            case TEDDY -> {
                this._description.clear();
                fillDescription(Integer.toString(this._armor).concat(" Armor. +1 Armor per ability triggered"));
            }
        }
    }

    public void triggerCooldownEffect(Player friendlyPlayer, Player enemyPlayer){
        this._cooldown--;
        if(this._cooldown <= 0) {
            switch (this._type) {
                case SWORD, ANTEATER, AXE, SHOVEL -> {
                    enemyPlayer.takeDamage(this._damage);
                }
                case WEIGHTS -> {
                    friendlyPlayer.setStrength(friendlyPlayer.getStrength()+3);
                }
                case CASTLE -> {
                    friendlyPlayer.setStrength(friendlyPlayer.getStrength()+2);
                    friendlyPlayer.setHealth(friendlyPlayer.getHealth()+15);
                }
                case FENCE, TEDDY -> {
                    friendlyPlayer.setArmor(friendlyPlayer.getArmor()+this._armor);
                }
            }
            if(this._originalCooldown > 0) {
                for (int i = 0; i < friendlyPlayer.getHandCardsCount(); i++) {
                    Card card = friendlyPlayer.getHandCard(i);
                    card.triggerAfterItemTriggersEffect();
                }
            }
            this._cooldown = this._originalCooldown-friendlyPlayer.getSpeed()*this._originalCooldown/100;
        }
    }

    public void triggerOnMoveAdjacentEffect(Card left, Card right){
        switch(this._type){
            case SHIELD -> {
                if(left != null && left.getOriginalDamage() != 0)left.setDamage(left.getDamage()+6);
                if(right != null && right.getOriginalDamage() != 0)right.setDamage(right.getDamage()+6);
            }

        }
    }


    public void triggerOnBuyEffect(Player friendlyPlayer){
        switch(this._type){
            case AXE -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()+2);
            }
            case CASTLE -> {
                friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+1);
            }
            case COIN -> {
                friendlyPlayer.setGold(friendlyPlayer.getGold()+3);
            }
            case BANDAID -> {
                friendlyPlayer.setHealth(friendlyPlayer.getHealth()+100);
            }
            case BOOTS -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()+20);
            }
            case FENCE -> {
                friendlyPlayer.setOriginalArmor(friendlyPlayer.getArmor()+this._armor);
            }
            case SCROLL -> {
                friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth()-42);
                friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+3);
            }
            case CAT -> {
                friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth() + 10);
                friendlyPlayer.setHealth(friendlyPlayer.getHealth()+40);
            }
        }
    }

    public void triggerOnSellEffect(Player friendlyPlayer){
        switch(this._type){
            case BOOTS -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()-20);
            }
        }
    }

    public void triggerAfterItemTriggersEffect(){
        switch(this._type){
            case TEDDY -> {
                setArmor(this._armor+1);
            }
        }
    }








    public String draw(int row, int height, boolean hideIndex, boolean hideGold){
        String cooldown = Integer.toString(this._cooldown);
        String gold = Integer.toString(this._gold);
        String index = Integer.toString(this._index);
        String text = "";
        int startingIndex = 1;

        if(row == 0){
            for(int i = startingIndex; i < this._width; i++){
                text = text.concat("_");
            }
            text = text.concat(" ");
        }
        else{

            if(row == 1){
                if(!hideGold || this._gold == 0){
                    text = text.concat(gold).concat("$ ");
                    startingIndex += gold.length()+2;
                }
                if(this._originalCooldown >= 0) {
                    text = text.concat(cooldown).concat("s");
                    startingIndex += cooldown.length()+1;
                }


            }
            else if(row-2 <this._art.size()){
                text = text.concat(this._art.get(row-2));
                startingIndex += this._art.get(row-2).length();
            }
            else if(row-2-this._art.size() < this._description.size()){
                text = text.concat(this._description.get(row-2-this._art.size()));
                startingIndex += this._description.get(row-2-this._art.size()).length();
            }

            for (int i = startingIndex; i < this._width; i++) {
                if(row == 1 && i == this._width-2-index.length() && !hideIndex){
                    text = text.concat("(").concat(index).concat(")");
                    break;
                }
                if(row == height - 1) text = text.concat("_");
                else text = text.concat(" ");
            }
            text = text.concat("|");
        }

        return text;
    }
}
