public class Card {

    final private int _value;


    Card(int value){
        this._value = value;

    }

    public int getValue() { return _value; }




    public String draw(int row, int width, int height){
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
                String stringValue = Integer.toString(this._value);
                text = text.concat(stringValue);
                startingIndex += stringValue.length();

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
