import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public abstract class Communication {

    protected List<PrintWriter> writers = new ArrayList<>();

    protected final String CLEAR_SCREEN = "\033[H\033[2J";

    protected final char NO_ENCODE = 'N';



    //writes to a writer given an encoding
    public void write(PrintWriter writer, String text, char encoding){ writer.println(encoding + text);}

    //writes to a writer and uses the default encoding
    public void write(PrintWriter writer, String text){
        write(writer, text, NO_ENCODE);
    }

    //flushes the writer
    public void flush(PrintWriter writer){
        writer.flush();
    }

    //reads in a blocking fashion from the reader and returns a list where the first element is the encoding char and the second the content
    public List<String> read(BufferedReader reader)throws IOException{
        try {
            List<String> res = new ArrayList<>();
            String response = reader.readLine();
            if(response == null) throw new SocketException();
            if(response.isEmpty()){
                res.add(Character.toString(NO_ENCODE));
                res.add("");
            }
            else if(response.length() == 1){
                res.add(Character.toString(response.charAt(0)));
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



    //reads in a nonblocking fashion where the first element of the returning list is the encoding char and the second element the actual content of the message.
    //if the socket is disconnected it will throw a SocketException and it will gracefully catch it returning null
    public List<String> readNonBlocking(BufferedReader reader) throws IOException {
        try {
            String response = reader.readLine();

            if(response == null) throw new SocketException();
            List<String> res = new ArrayList<>();
            if(response.isEmpty()){
                res.add(Character.toString(NO_ENCODE));
                res.add("");
            }
            else if(response.length() == 1){
                res.add(Character.toString(response.charAt(0)));
                res.add("");
            }
            else{
                res.add(Character.toString(response.charAt(0)));
                res.add(response.substring(1));
            }
            return res;
        } catch(SocketTimeoutException e){
            return null;
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
