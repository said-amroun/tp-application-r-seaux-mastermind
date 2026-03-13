import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class IdleClient {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 1234);

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // utilise ta classe Code
            Code randomCode = new Code(new Random());
            out.println(randomCode.toString());

            //reste connecté sans rien faire
            Thread.sleep(600000); // 10 minutes

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}