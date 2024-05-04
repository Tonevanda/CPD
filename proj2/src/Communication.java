import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class Communication {

    protected List<PrintWriter> writers = new ArrayList<>();

    protected final String CLEAR_SCREEN = "\033[H\033[2J";






    public void write(PrintWriter writer, String text){
        writer.println(text);
    }


    public void write(PrintWriter writer, String text, String encoding){
        writer.println(encoding.concat(text));
    }

    public void flush(PrintWriter writer){
        writer.flush();
    }



    public String read(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
    public String readEncoded(BufferedReader reader) throws IOException {
        return reader.readLine().substring(1);
    }




    public void broadcast(String text){
        for(PrintWriter writer : writers){
            write(writer, text);
        }

        for(PrintWriter writer : writers){
            flush(writer);
        }
    }
    public void broadcast(String text, String encoding){
        for(PrintWriter writer : writers){
            write(writer, text, encoding);
        }

        for(PrintWriter writer : writers){
            flush(writer);
        }
    }
}
