import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Communication {

    public void write(PrintWriter writer, String text){
        writer.println(text);
    }

    public void flush(PrintWriter writer){
        writer.flush();
    }

    public String read(BufferedReader reader) throws IOException {
        String result = null;
        while(result == null){
            result = reader.readLine();
            System.out.println(result);
        }
        return result;
    }
}
