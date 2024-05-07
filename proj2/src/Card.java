import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Card {


    private int _width = 30;






    enum Type{
        SWORD,
        ANTEATER



    }


    final private Type _type;


    private List<String> _art = new ArrayList<>();

    private List<String> _description = new ArrayList<>();

    private int _gold = 5;

    private int _cooldown = 5;

    private int _index = 0;



    Card(int type){

        this._type = Type.values()[type];
        String ascii = "";
        String power = "";
        switch(this._type){
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
                power = "50 Damage";

            }
        }

        fillArt(ascii);
        fillDescription(power);

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
    public int getWidth(){return this._width;}

    public int getType(){return this._type.ordinal();}

    public int getGold(){return this._gold;}



    public void triggerEffect(Player friendlyPlayer, Player enemyPlayer, int time){
        if(time % this._cooldown == 0) {
            switch (this._type) {
                case SWORD -> {
                    enemyPlayer.takeDamage(3);
                }
                case ANTEATER -> {
                    enemyPlayer.takeDamage(50);
                }
            }
        }
    }





    public String draw(int row, int height){
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
                text = text.concat(gold).concat("$ ").concat(cooldown).concat("s (").concat(index).concat(")");
                startingIndex += gold.length()+cooldown.length()+index.length()+6;

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
                if(row == height - 1) text = text.concat("_");
                else text = text.concat(" ");
            }
            text = text.concat("|");
        }

        return text;
    }
}
