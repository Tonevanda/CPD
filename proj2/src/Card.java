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
        NINE

    }

    final private Type _type;

    private String _owner;

    private String _art;

    Card(int type, String owner){

        this._owner = owner;
        this._type = Type.values()[type];
        switch(this._type){
            case ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->{
                this._art = "";
            }
        }
    }

    public String getOwner(){return this._owner;}


    public boolean isCreature(){
        switch(this._type){
            case ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->{
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
            for (int i = startingIndex; i < width - 1; i++) {
                if(row == height - 1) text = text.concat("_");
                else text = text.concat(" ");
            }
            text = text.concat("|");
        }

        return text;
    }
}
