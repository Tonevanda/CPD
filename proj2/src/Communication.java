import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public abstract class Communication {



    public void write(PrintWriter writer, String text){
        writer.println(text);
    }

    public void flush(PrintWriter writer){
        writer.flush();
    }

    public String read(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
