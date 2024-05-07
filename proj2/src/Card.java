import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Card {





    enum Type{
        ZERO,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        ANTEATER

    }

    final private Type _type;

    private String _owner;

    private List<String> _art = new ArrayList<>();

    Card(int type, String owner){

        this._owner = owner;
        this._type = Type.values()[type];
        String ascii = "";
        switch(this._type){
            case ANTEATER -> {
                ascii = "               _,,......_\n" +
                        "            ,-'          `'--.\n" +
                        "         ,-'  _              '-.\n" +
                        "(`.    ,'   ,  `-.              `.\n" +
                        " \\ \\  -    / )    \\               \\\n" +
                        "  `\\`-^^^, )/      |     /         :\n" +
                        "    )^ ^ ^V/            /          '.\n" +
                        "    |      )            |           `.\n" +
                        "    9   9 /,--,\\    |._:`         .._`.\n" +
                        "    |    /   /  `.  \\    `.      (   `.`.\n" +
                        "    |   / \\  \\    \\  \\     `--\\   )    `.`.___\n" +
                        "   .;;./  '   )   '   )       ///'       `-\"'\n" +
                        "   `--'   7//\\    ///\\";
            }
        }
        fillArt(ascii);
    }

    public void fillArt(String ascii){
        if(!ascii.isEmpty())
            this._art = Arrays.asList(ascii.split("\n"));
    }

    public String getOwner(){return this._owner;}


    public boolean isCreature(){
        switch(this._type){
            case ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, ANTEATER ->{
                return true;
            }
        }
        return false;
    }




    public int getValue() {
        switch(this._type){
            case ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->{
                return this._type.ordinal();
            }
            case ANTEATER -> {return 8;}
        }
        return 0;
    }



    public String draw(int row, int width, int height){
        String value;
        if(isCreature()){
            value = Integer.toString(getValue());
        }
        else{
            value = " ";
        }
        String text = "";
        int startingIndex = 1;

        if(row == 0){
            text = text.concat(" ");
            for(int i = startingIndex; i < width - 1; i++){
                text = text.concat("_");
            }
            text = text.concat(" ");
        }
        else{
            text = text.concat("|");

            if(row == 1){
                text = text.concat(value);
                startingIndex += value.length();

            }
            else if(row-2 <this._art.size()){
                text = text.concat(this._art.get(row-2));
                startingIndex += this._art.get(row-2).length();
            }

            for (int i = startingIndex; i < width - 1; i++) {
                if(row == height - 1) text = text.concat("_");
                else text = text.concat(" ");
            }
            text = text.concat("|");
        }

        return text;
    }
}
