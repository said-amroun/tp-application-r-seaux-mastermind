import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serveur {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java Serveur port");
            return;
        }
        //
        int port = Integer.parseInt(args[0]);

        ExecutorService pool = Executors.newWorkStealingPool();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur WorkStealingPool sur le port " + port);

        while (true) {

            Socket socket = serverSocket.accept();
            System.out.println("Client connecté");

            pool.execute(new ClientHandler(socket));
        }
    }
}