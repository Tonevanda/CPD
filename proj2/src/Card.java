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
        AXE



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
                this._damage = 6;
                power = "Adjacent cards get +6 Damage";
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
                power = "8 Damage. Bought: +2» permanently";
            }
        }

        fillArt(ascii);
        fillDescription(power);
        this._originalCooldown = _cooldown;
        this._originalDamage = _damage;

    }

    public void resetStats(){
        this._cooldown = this._originalCooldown;
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

    public int getOriginalDamage(){return this._originalDamage;}


    public void setDamage(int damage){
        this._damage = damage;
        switch(this._type){
            case SWORD, ANTEATER -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage"));
            }
            case AXE -> {
                this._description.clear();
                fillDescription(Integer.toString(this._damage).concat(" Damage. Bought: +2» permanently"));
            }
        }
    }

    public void triggerCooldownEffect(Player friendlyPlayer, Player enemyPlayer){
        this._cooldown -= 1 + friendlyPlayer.getSpeed()*this._originalCooldown/100;
        if(this._cooldown <= 0) {
            switch (this._type) {
                case SWORD, ANTEATER, AXE -> {
                    enemyPlayer.takeDamage(this._damage);
                }
            }
            this._cooldown = this._originalCooldown;
        }
    }

    public void triggerOnMoveEffect(Card left, Card right){
        switch(this._type){
            case SHIELD -> {
                if(left != null && left.getOriginalDamage() != 0)left.setDamage(left.getDamage()+this._damage);
                if(right != null && right.getOriginalDamage() != 0)right.setDamage(right.getDamage()+this._damage);
            }
        }
    }

    public void triggerOnBuyEffect(Player friendlyPlayer){
        switch(this._type){
            case AXE -> {
                friendlyPlayer.setSpeed(friendlyPlayer.getSpeed()+2);
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
