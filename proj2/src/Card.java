import java.util.*;

public class Card {

    private int _width = 0;
    private int MAX_WIDTH = 156;

    public enum Type{
        LOCK,
        COIN,
        TREASURE,
        BANDAID,
        SCROLL,
        CAT,
        BED,
        CHOCOLATE,
        SHOPPING_CART,
        BOOK,
        SWORD,
        ANTEATER,
        SHIELD,
        AXE,
        WEIGHTS,
        CASTLE,
        SHOES,
        SHOVEL,
        FENCE,
        TEDDY,

        DRUMS,
        BOW,
        BRUSH,
        KNIFE,

        SYRINGE,
        BOOTS,
        TURN_TABLE,
        SHIP,
        CITY,
        HOUSE,
        MERCHANT,
        MOSQUITO
    }

    public enum BookType{
        CONDITIONING,
        REGULAR_CUSTOMER,
        DRAIN
    }



    final static int BOOK_COUNT = BookType.values().length;

    final static int ENCOUNTER_COUNT = 2;

    final static int ITEMS_COUNT = 20;

    final static int TOKENS_COUNT = Type.values().length - ENCOUNTER_COUNT - ITEMS_COUNT;

    final private Type _type;

    private List<String> _art = new ArrayList<>();

    private final List<String> _description = new ArrayList<>();

    private int _gold = 0;

    private int _originalCooldown = -1;

    private int _cooldown = -1;

    private int _index = 0;

    private int _originalDamage = 0;

    private int _damage = -1;

    private int _originalArmor = -1;
    private int _armor = -1;

    private int _speed = 0;

    private int _rand = -1;

    private int _health = -1;

    private boolean _isInstant = false;


    Card(int type, int rand){
        this(type);
        randomize(rand);
    }
    //Constructs the Card given its type
    Card(int type){
        //29
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
                          `=':-..-'`
                         \n""";
                this._width = 15;
                this._gold = 6;
                power = "Pay this card's cost to get rid of it";
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
                           |_|                  |_|
                         \n""";
                this._width = 31;
                this._gold = 3;
                this._cooldown = 9;
                power = "+3 Strength";
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
                power = "+2 Strength, heal 15. Bought: +1 Strength";
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
                this._isInstant = true;
                power = "Bought: +1$ and +30% Health";
            }
            case BANDAID -> {
                ascii = """ 
                         \n \n \n \n
                          /==========================\\
                         / : : : : : |::::| : : : : : \\
                        { : : : : : :|::::|: : : : : : }
                         \\ : : : : : |::::| : : : : : /
                          \\==========================/
                         \n \n""";

                this._width = 33;
                this._gold = 1;
                this._isInstant = true;
                randomize(getRandomNumber());

            }
            case SHOES -> {
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
                power = "+20 Speed";
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
                        / /        /
                         \n""";
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
                power = "+10 Armor. Bought: +10 Armor";
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
                power = "+1 Armor. +1 Armor per ability triggered";
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
            case DRUMS -> {
                ascii = """
                            __________        ,.     ,.
                          ,','.___.,',`.      `.\\   /,'       ___________
                         / /.____./ / \\ \\       \\\\ //      ,''..-------..``.
                        : :      : :   : :       \\//       :`-..._____...-':
                        [ |.____.| ]   | ]       //\\       |`-..._____...-'|
                        | |      | |   | |      // \\\\      |   |       |   |
                        [ |.____.| ]   | ]     //   \\\\     |]  |       |  [|
                        | |      | |   | |    //     \\\\    |   []     []   |
                        : :.____.: :   ; ;   //       \\\\   |   |  ___  |   |
                         \\ \\.____.\\ \\ / /   //        \\\\   :`-.:._]|[_.:.-';
                          `.`.____.`.`,'   `'          `'   `-...|   |...-'
                         \n \n""";

                this._width = 53;
                this._gold = 12;
                this._cooldown = 10;
                power = "Gain Speed equal to your Strength";
            }
            case BOW -> {
                ascii = """
                            /`.
                           /   :.
                          /     \\\\
                         /       ::
                        /        ||
                        )>>>--,-'-)>
                        \\        ||
                         \\       ;;
                          \\     //
                           \\   ;'
                            \\,'
                            \s""";

                this._width = 13;
                this._gold = 4;
                this._cooldown = 9;
                this._damage = 0;
                power = "0 Damage.+3 for each small card.";

            }
            case BRUSH -> {

                ascii = """
                         .----,
                        /--._(
                        |____|
                        [____] .=======.
                          YY   q.     .p
                          ||   | `---' |
                          ||   |       |
                          ||   |       |
                          ||   |       |
                          []   |_______|
                         \n
                         """;

                this._width = 17;
                this._gold = 4;
                this._cooldown = 10;
                this._damage = 3;
                power = "3 Damage. +3 Damage whenever you gain Armor";
            }
            case KNIFE -> {
                ascii = """
                                    \n \n\s
                                    /\\
                        /vvvvvvvvvvvv \\--------------------,
                        `^^^^^^^^^^^^ /==================="
                                    \\/
                         \n\s""";

                this._width = 37;
                this._gold = 10;
                this._cooldown = 10;
                this._damage = 1;
                power = "1 Damage. Double this card's damage";
            }
            case BED -> {
                ascii = """
                          ()___\s
                        ()//__/)_________________()
                        ||(___)//#/_/#/_/#/_/#()/||
                        ||----|#| |#|_|#|_|#|_|| ||
                        ||____|_|#|_|#|_|#|_|#||/||
                        ||    |#|_|#|_|#|_|#|_||
                         \n \n""";

                this._width = 28;
                this._isInstant = true;
                randomize(getRandomNumber());


            }
            case CHOCOLATE -> {
                ascii = """
                        \n \n \n _________,-.___
                        |__      { {]_]
                        |__`---.__\\ \\_]
                        |  `---.___} }]_]_]
                        |_________/ {_]_]_]
                        \s
                         \n \n \n""";
                this._width = 20;
                this._gold = 1;
                power = "Sell: +10 MaxHealth";
            }
            case SHOPPING_CART -> {
                ascii = """
                         _        ,
                        (_\\______/________
                           \\-|-|/|-|-|-|-|/
                            \\==/-|-|-|-|-/
                             \\/|-|-|-|,-'
                              \\--|-'''
                               \\_j________
                               (_)     (_)
                         \n""";
                this._width = 20;
                this._gold = 5;
                this._isInstant = true;
                power = "Bought: Fill your hand with Chocolate Bars";
            }
            case SYRINGE -> {
                ascii = """

                        \s
                        \s
                        .     |____________________
                        |-----|- - -|''''|''''|'##\\|__
                        |- -  |  cc 3    2    1 ### __]==--------------
                        |-----|_________________##/|
                        '     |""\"""\"""\"""\"""\"""\"""`
                        \s
                        \s
                        """;

                this._width = 48;
                this._gold = 6;
                this._cooldown = 11;
                power = "Heal 12. Advance 2s per ability triggered";
            }
            case BOOTS -> {
                ascii = "       ________\n"+
                        "    __(_____  <|\n" +
                        "   (____ / <| <|\n" +
                        "   (___ /  <| L`-------.\n" +
                        "   (__ /   L`--------.  \\\n" +
                        "   /  `.    ^^^^^ |   \\  |\n" +
                        "  |     \\---------'    |/\n" +
                        "  |______|____________/]\n" +
                        "  [_____|`-.__________]\n \n";

                this._width = 27;
                this._gold = 5;
                power = "Bought: +20 Armor. Give armor gaining abilities +8 Armor";
            }
            case TURN_TABLE -> {
                ascii = """
                                     ____           __
                                     \\===\\=========|__|=[]
                          <========^==='======>    _||_
                         _/_|_|_|_|_|_|_|_|_|_\\_^__||||_
                        |                              |
                        |                              |
                        +---/||\\----------------/||\\---+
                           ^^^^^^              ^^^^^^
                        \s
                        \s
                        """;

                this._width = 35;
                this._gold = 5;
                this._cooldown = 10;
                power = "Advance the right card 4s";
            }
            case SHIP -> {
                ascii = """
                                    ____
                                     ---|
                         \\/            /|     \\/
                                      / |\\
                                     /  | \\        \\/
                                    /   || \\
                                   /    | | \\
                                  /     | |  \\
                                 /      | |   \\
                                /       ||     \\
                               /________/       \\
                               ________/_________--/
                        ~~~    \\__________________/""";

                this._width = 30;
                this._gold = 10;
                this._armor = 3;
                power = "+3 Armor per ability triggered";
            }
            case CITY -> {
                ascii = """

                        \s
                                           ..======..
                                           ||::: : |
                                      .~.===: : : :|   ..===.
                        .=======.~,   |"|: :|::::::|   ||:::|
                        |: :: ::|"|l_l|"|:: |:;;:::|___!| ::|
                        ::: :: :|"||_||"| : |: :: :|: : |:: |
                        |:===F=:|"!/|\\!"|::F|:====:|::_:|: :|
                        ![_][I_]!//_:_\\\\![]I![_][_]!_[_]![]_!
                        \s
                        """;
                this._width = 38;
                this._gold = 6;
                this._cooldown = 8;
                this._armor = 8;
                power = "+8 Armor and give armor gaining abilities +1 Armor";
            }
            case HOUSE -> {
                ascii = """
                                                    (_)
                                            ________[_]________
                                   /\\      /\\        ______    \\
                                  /  \\    //_\\       \\    /\\    \\
                           /\\    / /\\/\\  //___\\       \\__/  \\    \\
                          /  \\  /\\/    \\//_____\\       \\ |[]|     \\
                         /\\/\\/\\/       //_______\\       \\|__|      \\
                        /      \\      /XXXXXXXXXX\\                  \\
                                \\    /_I_II  I__I_\\__________________\\
                                       I_I|  I__I_____[]_|_[]_____I
                                       I_II  I__I_____[]_|_[]_____I
                                       I II__I  I     XXXXXXX     I
                        \s
                        """;

                this._width = 47;
                this._gold = 12;
                this._cooldown = 7;
                power = "Heal 4. Whenever you heal, gain +1 Strength this fight";
            }
            case BOOK -> {
                ascii = "      ,   ,   \n" +
                        "     /////|   \n" +
                        "    ///// |   \n" +
                        "   |===|  |   \n" +
                        "   |j  |  |   \n" +
                        "   | g |  |   \n" +
                        "   |  s| /   \n" +
                        "   |===|/   \n" +
                        "   '---'   ";

                this._width = 15;
                randomize(getRandomNumber());

            }
            case MERCHANT -> {
                ascii = """
                                     _____
                                    /     \\
                                  /- (*) |*)\\
                                  |/\\.  _>/\\|
                                      \\__/    |\\
                                     _| |_   \\-/
                                    /|\\__|\\  //
                                   |/|   |\\\\//
                                   ||| __|
                                   /_\\| ||
                                   \\_/| ||
                                     || ||
                                     /\\ \\ \\
                                    ^^^^ ^^^\
                        """;
                this._width = 35;
                this._isInstant = true;
                power = "Sells Items";
            }
            case MOSQUITO -> {
                ascii = """
                           \n \n                ,-.
                               `._        /  |        ,
                                  `--._  ,   '    _,-'
                           _       __  `.|  / ,--'
                            `-._,-'  `-. \\ : /
                                 ,--.-.-`'.'.-.,_-
                               _ `--'-'-;.'.'-'`--
                           _,-' `-.__,-' / : \\
                                      _,'|  \\ `--._
                                 _,--'   '   .     `-.
                               ,'         \\  |        `
                         \n
                        """;

                this._width = 35;
                this._isInstant = true;
                this._health = 25;
                this._speed = 23;
                power = "Reward: 2$";
            }
            case TREASURE -> {
                ascii = """

                        \s
                        \s
                           __________
                          /\\____;;___\\
                         | /         /
                         `. ())oo() .
                          |\\(%()*^^()^\\
                         %| |-%-------|
                        % \\ | %  ))   |
                        %  \\|%________|
                        \s
                        \s
                        """;
                this._width = 16;
                this._isInstant = true;
            }
        }

        fillArt(ascii);
        fillDescription(power);
        this._originalCooldown = _cooldown;
        this._originalDamage = _damage;
        this._originalArmor = _armor;

    }

    //resets the cards stats after a fight has ended
    public void resetStats(){
        setCooldown(this._originalCooldown);
        setDamage(this._originalDamage);

        setArmor(this._originalArmor);
    }

    //fills the description part of the card
    public void fillDescription(String power){
        if(!power.isEmpty()){
            while(true){
                if(power.length() <= this._width-1){
                    if(power.startsWith(" "))power = power.substring(1);
                    this._description.add(power);
                    break;
                }
                String line = power.substring(0, this._width-1);


                power = power.substring(this._width-1);

                while(!line.endsWith(" ") && !power.startsWith(" ")){
                    power = Character.toString(line.charAt(line.length()-1)).concat(power);
                    line = line.substring(0, line.length()-1);
                }
                if(line.startsWith(" "))line = line.substring(1);
                this._description.add(line);
            }
        }

    }

    //fills the art part of the card
    public void fillArt(String ascii){
        if(!ascii.isEmpty())
            this._art = Arrays.asList(ascii.split("\n"));
    }

    //generates a random number
    public int getRandomNumber(){
        Random random = new Random();
        return Math.abs(random.nextInt());
    }

    //generates different card attributes given a random number
    public void randomize(int random){
        switch(this._type){
            case BANDAID -> {
                this._rand = random % 3;
                this._description.clear();
                switch(this._rand){
                    case 0 ->{
                        fillDescription("Bought: Heal 100");
                    }
                    case 1 -> {
                        fillDescription("Bought: Heal 126");
                    }
                    case 2 -> {
                        fillDescription("Bought: Heal 30% of Health");
                    }
                }
            }
            case BED -> {
                this._rand = random % 5;
                this._description.clear();
                switch(this._rand){
                    case 0 -> {
                        this._gold = 1;
                        fillDescription("Bought: +20 maxHealth");
                    }
                    case 1 ->{
                        this._gold = 5;
                        fillDescription("Bought: +50 maxHealth");
                    }
                    case 2 ->{
                        this._gold = 1;
                        fillDescription("Bought: +2 Strength");
                    }
                    case 3 ->{
                        this._gold = 5;
                        fillDescription("Bought: +4 Strength");
                    }
                    case 4 -> {
                        this._gold = 5;
                        fillDescription("Bought: +6 Speed");
                    }
                }
            }
            case BOOK -> {
                this._rand = random % BookType.values().length;
                this._description.clear();
                System.out.println(this._rand);
                switch(BookType.values()[this._rand]){
                    case CONDITIONING -> {
                        fillDescription("Your Strength also increases the Armor you gain in a fight. Bought: +3 Strength");
                    }
                    case REGULAR_CUSTOMER -> {
                        fillDescription("When you sell a card, Heal 25");
                    }
                    case DRAIN -> {
                        fillDescription("+1 Health whenever you deal damage");
                    }
                }
            }
        }
    }

    public void setIndex(int index){this._index = index;}

    public void setGold(int gold){this._gold = gold;}

    public int getWidth(){return this._width;}

    public Type getType(){return this._type;}

    public int getGold(){return this._gold;}

    public int getRand(){return this._rand;}

    public int getDamage(){return this._damage;}

    public int getSpeed(){return this._speed;}

    public int getArmor(){return this._armor;}

    public int getHealth(){return this._health;}

    public int getCooldown(){return this._cooldown;}

    public int getOrignalCooldown(){return this._originalCooldown;}

    public int getOrignalArmor(){return this._originalArmor;}

    public int getOriginalDamage(){return this._originalDamage;}

    public boolean isInstant(){return this._isInstant;}

    public void setCooldown(int cooldown){
        this._cooldown = cooldown;
        if(this._originalCooldown > 0) this._cooldown = Math.max(this._cooldown, 1);
    }

    //sets the card damage and some specific cards need to change it's description to match the new damage
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
            case BOW -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage.+3 for each small card."));
            }
            case BRUSH -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage. +3 Damage whenever you gain Armor"));
            }
            case KNIFE -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage. Double this card's damage"));
            }
        }
    }

    //sets the card armor and some specific cards need to change it's description to match the new armor
    public void setArmor(int armor){
        this._armor = armor;
        switch(this._type){
            case TEDDY -> {
                this._description.clear();
                fillDescription("+".concat(Integer.toString(this._armor)).concat(" Armor. +1 Armor per ability triggered"));
            }
            case FENCE -> {
                this._description.clear();
                fillDescription("+".concat(Integer.toString(this._armor)).concat(" Armor. Bought: +10 Armor"));
            }
            case SHIP -> {
                this._description.clear();
                fillDescription("+".concat(Integer.toString(this._armor)).concat(" Armor per ability triggered"));
            }
            case CITY -> {
                this._description.clear();
                fillDescription("+".concat(Integer.toString(this._armor)).concat(" Armor and give armor gaining abilities +1 Armor"));
            }
        }
    }

    //advances the cooldown of a card and triggers it if it reaches below 0
    public void advance(Player friendlyPlayer, Player enemyPlayer, int cardIndex, int cooldownAdvance){
        if(this._originalCooldown > 0) {
            this._cooldown = this._cooldown - cooldownAdvance;
            if (this._cooldown <= 0) {
                int nextCooldownAdvance = -this._cooldown;
                triggerCooldownEffect(friendlyPlayer, enemyPlayer, cardIndex);
                if (nextCooldownAdvance > 0) {
                    advance(friendlyPlayer, enemyPlayer, cardIndex, nextCooldownAdvance);
                }
            }
        }


    }

    //triggers the cooldown power of a card if it reaches 0 or lower
    public void triggerCooldownEffect(Player friendlyPlayer, Player enemyPlayer, int cardIndex){
        this._cooldown--;
        if(this._cooldown <= 0) {
            switch (this._type) {
                case SWORD, ANTEATER, AXE, SHOVEL, BOW, BRUSH -> {
                    enemyPlayer.takeDamage(this._damage, friendlyPlayer);
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
                case DRUMS -> {
                    friendlyPlayer.setSpeed(friendlyPlayer.getSpeed() + friendlyPlayer.getStrength());
                }
                case KNIFE -> {
                    enemyPlayer.takeDamage(this._damage, enemyPlayer);
                    setDamage(this._damage*2);
                }
                case SYRINGE -> {
                    friendlyPlayer.setHealth(friendlyPlayer.getHealth()+12);
                }
                case TURN_TABLE -> {
                    Card rightCard = friendlyPlayer.getHandCard(cardIndex+1);
                    if(rightCard != null){
                        rightCard.advance(friendlyPlayer, enemyPlayer, cardIndex+1, 4);
                    }
                }
                case CITY -> {
                    friendlyPlayer.setArmor(friendlyPlayer.getArmor()+this._armor);
                    friendlyPlayer.setArmorBuffing(friendlyPlayer.getArmorBuffing()+1);
                }
                case HOUSE -> {
                    friendlyPlayer.setHealth(friendlyPlayer.getHealth()+4);
                }
            }
            if(this._originalCooldown > 0) {
                for (int i = 0; i < friendlyPlayer.getHandCardsCount(); i++) {
                    if(i != cardIndex) {
                        Card card = friendlyPlayer.getHandCard(i);
                        card.triggerAfterItemTriggersEffect(friendlyPlayer, enemyPlayer, i);
                    }
                }
            }
            this._cooldown = this._originalCooldown-(friendlyPlayer.getSpeed()+this._speed)*this._originalCooldown/100;

        }
    }

    //triggers the effect of a card whenever it is moved on the hand
    public void triggerOnMove(Player friendlyPlayer, Card left, Card right, int cardIndex){
        switch(this._type){
            case SHIELD -> {
                if(left != null && left.getOriginalDamage() != 0)left.setDamage(left.getDamage()+6);
                if(right != null && right.getOriginalDamage() != 0)right.setDamage(right.getDamage()+6);
            }
            case BOW -> {
                int count = 0;
                for(int i = 0; i < friendlyPlayer.getHandCardsCount(); i++){
                    if(i != cardIndex){
                        Card card = friendlyPlayer.getHandCard(i);
                        if(card.getWidth() <= 20 && card.getType() != Type.LOCK) count++;
                    }
                }
                setDamage(this._damage+3*count);
            }

        }
    }

    //triggers an effect of the card whenever it is bought.
    public void triggerOnBuyEffect(Player friendlyPlayer){
        friendlyPlayer.setEncounter(null);
        switch(this._type){
            case AXE -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()+2);
            }
            case CASTLE -> {
                friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+1);
            }
            case COIN -> {
                friendlyPlayer.setGold(friendlyPlayer.getGold()+1);
                friendlyPlayer.setHealth(friendlyPlayer.getHealth()+30*(friendlyPlayer.getMaxHealth()-friendlyPlayer.getHealth())/100);
            }
            case BANDAID -> {
                switch(this._rand){
                    case 1 ->{
                        friendlyPlayer.setHealth(friendlyPlayer.getHealth()+100);
                    }
                    case 2 ->{
                        friendlyPlayer.setHealth(friendlyPlayer.getHealth()+126);
                    }
                    case 3 ->{
                        friendlyPlayer.setHealth(friendlyPlayer.getHealth()+30*(friendlyPlayer.getMaxHealth()-friendlyPlayer.getHealth())/100);
                    }
                }

            }
            case SHOES -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()+20);
            }
            case FENCE -> {
                friendlyPlayer.setOriginalArmor(friendlyPlayer.getOriginalArmor()+this._armor);
            }
            case SCROLL -> {
                friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth()-42);
                friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+3);
            }
            case CAT -> {
                friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth() + 10);
                friendlyPlayer.setHealth(friendlyPlayer.getHealth()+40);
            }
            case BED -> {
                switch(this._rand){
                    case 0 -> {
                        friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth()+20);
                    }
                    case 1 ->{
                        friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth()+50);
                    }
                    case 2 ->{
                        friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+2);
                    }
                    case 3 ->{
                        friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+4);
                    }
                    case 4 -> {
                        friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()+6);
                    }
                }
            }
            case SHOPPING_CART -> {
                while(friendlyPlayer.getHandWidth()+this._width <= MAX_WIDTH){
                    friendlyPlayer.addHandCard(new Card(20));
                }
            }
            case BOOTS -> {
                friendlyPlayer.setOriginalArmor(friendlyPlayer.getOriginalArmor()+20);
                friendlyPlayer.setOriginalArmorBuffing(friendlyPlayer.getOriginalArmorBuffing()+8);

            }
            case TREASURE -> {
                friendlyPlayer.setGold(friendlyPlayer.getGold()+this._armor);
            }
            case BOOK -> {
                friendlyPlayer.activateSkill(this._rand);
                switch(BookType.values()[this._rand]){
                    case CONDITIONING -> {
                        friendlyPlayer.setOriginalStrength(friendlyPlayer.getOriginalStrength()+3);
                    }
                }
            }
            case MERCHANT, MOSQUITO -> {
                friendlyPlayer.setEncounter(this._type);
            }

        }
    }

    //triggers an effect of the card whenever it is sold
    public void triggerOnSellEffect(Player friendlyPlayer){
        switch(this._type){
            case SHOES -> {
                friendlyPlayer.setOriginalSpeed(friendlyPlayer.getOriginalSpeed()-20);
            }
            case CHOCOLATE -> {
                friendlyPlayer.setMaxHealth(friendlyPlayer.getMaxHealth()+10);
            }
            case BOOTS -> {
                friendlyPlayer.setOriginalArmorBuffing(friendlyPlayer.getOriginalArmorBuffing()-8);
            }
            case BOOK -> {
                friendlyPlayer.deactivateSkill(this._rand);
                switch(BookType.values()[this._rand]){
                    case CONDITIONING -> {
                        for(Card card : friendlyPlayer.getHandCards()){
                            if(card.getOrignalArmor() >= 0){
                                card.setArmor(card.getArmor()-friendlyPlayer.getStrength());
                            }
                        }
                    }
                }
            }
        }
    }

    //triggers an effect caused whenever another card triggers it's ability during a fight
    public void triggerAfterItemTriggersEffect(Player friendlyPlayer, Player enemyPlayer, int cardIndex){
        switch(this._type){
            case TEDDY -> {
                setArmor(this._armor+1);
            }
            case SYRINGE -> {
                advance(friendlyPlayer, enemyPlayer, cardIndex, 2);
            }
            case SHIP -> {
                friendlyPlayer.setArmor(friendlyPlayer.getArmor()+this._armor);
            }
        }
    }

    //after a card is bought from the store it triggers an effect
    public void triggerAfterBuyingEffect(Card boughtCard){
        switch(this._type){

        }
    }

    //triggers affect whenever you gain armor
    public void triggerOnGainingArmorEffect(){
        switch(this._type){
            case BRUSH -> {
                setDamage(this._damage + 3);
            }
        }
    }

    //triggers an effect whenever you gain health
    public void triggerOnGainingHealthEffect(Player friendlyPlayer){
        switch(this._type){
            case HOUSE -> {
                friendlyPlayer.setStrength(friendlyPlayer.getStrength()+1);
            }
        }
    }

    //draws the borders, the art, the stats, the description and cooldown animation of the card
    public String draw(int row, int height, int cooldownLinesCount, boolean hideIndex, boolean hideGold){
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
                if(cooldownLinesCount > 0){
                    text = text.concat(drawCooldownLines());
                    startingIndex = this._width;
                }
                else {
                    if (!hideGold && this._gold > 0) {
                        text = text.concat(gold).concat("$ ");
                        startingIndex += gold.length() + 2;
                    }
                    if(this._health > 0){
                        text = text.concat("Health:").concat(Integer.toString(this._health));
                        startingIndex += Integer.toString(this._health).length() + 7;
                        if(this._speed > 0){
                            text = text.concat(" Speed: ").concat(Integer.toString(this._speed));
                            startingIndex += Integer.toString(this._speed).length() + 8;
                        }
                    }
                    if (this._originalCooldown >= 0) {
                        text = text.concat(cooldown).concat("s");
                        startingIndex += cooldown.length() + 1;
                    }
                }


            }
            else if(row-2 <this._art.size()){
                if(cooldownLinesCount > 0){
                    text = text.concat(drawCooldownLines());
                    startingIndex = this._width;
                }
                else {
                    text = text.concat(this._art.get(row - 2));
                    startingIndex += this._art.get(row - 2).length();
                }
            }
            else if(row-2-this._art.size() < this._description.size()){
                if(cooldownLinesCount > 0){
                    text = text.concat(drawCooldownLines());
                    startingIndex = this._width;
                }
                else{
                    text = text.concat(this._description.get(row-2-this._art.size()));
                    startingIndex += this._description.get(row-2-this._art.size()).length();
                }

            }


            if(cooldownLinesCount > 0 && row-2-this._art.size()-this._description.size() >= 0){
                text = text.concat(drawCooldownLines());
            }
            else {
                for (int i = startingIndex; i < this._width; i++) {
                    if (row == 1 && i == this._width - 2 - index.length() && !hideIndex) {
                        text = text.concat("(").concat(index).concat(")");
                        break;
                    }
                    if (row == height - 1) text = text.concat("_");
                    else text = text.concat(" ");
                }
            }
            text = text.concat("|");
        }

        return text;
    }

    //draws the cooldown animation of the card
    public String drawCooldownLines(){
        String text = "";
        for(int i = 1; i < this._width; i++){
            text = text.concat("-");
        }
        return text;
    }
}
