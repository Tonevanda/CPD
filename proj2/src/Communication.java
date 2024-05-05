import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public abstract class Communication {

    protected List<PrintWriter> writers = new ArrayList<>();

    protected final String CLEAR_SCREEN = "\033[H\033[2J";

    protected final char NO_ENCODE = 'N';

    protected final char TIMER_ENCODE = 'T';

    protected final char ALIVE_ENCODE = 'A';









    public void write(PrintWriter writer, String text, char encoding){ writer.println(encoding + text);}

    public void write(PrintWriter writer, String text){
        write(writer, text, NO_ENCODE);
    }

    public void flush(PrintWriter writer){
        writer.flush();
    }



    public List<String> read(BufferedReader reader, PrintWriter writer) throws IOException {
        try {
            List<String> res = new ArrayList<>();
            String response = reader.readLine();
            if(response == null) throw new SocketException();
            if(response.isEmpty()){
                res.add(Character.toString(NO_ENCODE));
                res.add("");
            }
            else if(response.charAt(0) == TIMER_ENCODE){
                write(writer, "", ALIVE_ENCODE);
                return read(reader, writer);
            }
            else if(response.charAt(0) == ALIVE_ENCODE){
                return read(reader, writer);
            }
            else if(response.length() == 1){
                res.add(Character.toString(NO_ENCODE));
                res.add("");
            }
            else{
                res.add(Character.toString(response.charAt(0)));
                res.add(response.substring(1));
            }
            return res;
        }catch(SocketException e){
            throw e;
        }
    }

    public boolean isConnectionAlive(BufferedReader reader, boolean hasTimedOut) throws IOException {
        try {

            if (reader.ready() || hasTimedOut) {
                String response = reader.readLine();
                //System.out.println("Read message: ".concat(response));
                return true;
            }
            return false;
        }catch(SocketException e){
            throw e;
        }
    }



    public void broadcast(String text, char encoding){
        for(PrintWriter writer : writers){
            write(writer, text, encoding);
        }

        for(PrintWriter writer : writers){
            flush(writer);
        }
    }

    public void broadcast(String text){
        broadcast(text, NO_ENCODE);
    }
}
