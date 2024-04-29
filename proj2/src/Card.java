public class Card {

    final private int _value;


    Card(int value){
        this._value = value;

    }




    public String draw(int row, int width, int height){
        String text = "";

        if(row == 0){
            text = text.concat(" ");
            for(int i = 1; i < width - 1; i++){
                text = text.concat("_");
            }
            text = text.concat(" ");
        }
        else if(row == height - 1){
            text = text.concat("\\");
            for(int i= 1; i < width - 1; i++){
                text = text.concat("_");
            }
            text = text.concat("/");
        }
        else if(row == 1){
            String stringValue = Integer.toString(this._value);
            text = text.concat("/").concat(stringValue);
            for(int i = 1+stringValue.length(); i < width - 1;i++){
                text = text.concat(" ");
            }
            text = text.concat("\\");
        }
        else{
            text = text.concat("|");
            for(int i = 1; i < width - 1; i++){
                text = text.concat(" ");
            }
            text = text.concat("|");
        }


        return text;


    }
}
