import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class ServeurSelect {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java ServeurSelect port");
            return;
        }

        int port = Integer.parseInt(args[0]);

        Selector selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(false);

        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Serveur Select sur le port " + port);

        Map<SocketChannel, Code> secrets = new HashMap<>();
        Random random = new Random();

        for (;;) {   // boucle infinie

            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();

            for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {

                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {

                    ServerSocketChannel srv = (ServerSocketChannel) key.channel();
                    SocketChannel client = srv.accept();

                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                    secrets.put(client, new Code(random));

                    System.out.println("Client connecté");
                }

                else if (key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int read = client.read(buffer);

                    if (read == -1) {
                        secrets.remove(client);
                        client.close();
                        continue;
                    }

                    String msg = new String(buffer.array()).trim();

                    System.out.println("Reçu : " + msg);

                    if (msg.length() != 4) {
                        client.write(ByteBuffer.wrap("ERREUR_LONGUEUR\n".getBytes()));
                        continue;
                    }

                    if (!msg.matches("[BGORWY]{4}")) {
                        client.write(ByteBuffer.wrap("ERREUR_CARACTERE\n".getBytes()));
                        continue;
                    }

                    Code guess = new Code(msg);
                    Code secret = secrets.get(client);

                    int corr = secret.numberOfColorsWithCorrectPosition(guess);
                    int incorr = secret.numberOfColorsWithIncorrectPosition(guess);

                    String rep = corr + " " + incorr + "\n";

                    client.write(ByteBuffer.wrap(rep.getBytes()));

                    if (corr == 4) {
                        System.out.println("Combinaison trouvée !");
                        secrets.put(client, new Code(random));
                    }
                }
            }
        }
    }
}