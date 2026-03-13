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

        Map<SocketChannel, ClientState> clients = new HashMap<>();
        Map<String, Integer> scores = new HashMap<>();

        Random random = new Random();

        for (;;) {

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

                    clients.put(client, new ClientState(new Code(random)));

                    System.out.println("Client connecté | Total = " + clients.size());
                }

                else if (key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();
                    ClientState state = clients.get(client);

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int read = client.read(buffer);

                    if (read == -1) {
                        clients.remove(client);
                        client.close();
                        System.out.println("Client déconnecté | Total = " + clients.size());
                        continue;
                    }

                    String msg = new String(buffer.array()).trim();

                    System.out.println("Reçu : " + msg);

                    if (state.attendreNom) {

                        String nom = msg;

                        scores.put(nom, state.tentatives);

                        client.write(ByteBuffer.wrap(
                                "Score enregistré !\n".getBytes()));

                        state.secret = new Code(random);
                        state.tentatives = 0;
                        state.attendreNom = false;

                        continue;
                    }

                    if (msg.equals("/SCORE")) {

                        StringBuilder sb = new StringBuilder();

                        if (scores.isEmpty()) {
                            sb.append("Aucun score pour le moment\n");
                        } else {
                            for (String name : scores.keySet()) {
                                sb.append(name)
                                        .append(" : ")
                                        .append(scores.get(name))
                                        .append("\n");
                            }
                        }

                        client.write(ByteBuffer.wrap(sb.toString().getBytes()));
                        continue;
                    }

                    if (msg.length() != 4) {
                        client.write(ByteBuffer.wrap("ERREUR_LONGUEUR\n".getBytes()));
                        continue;
                    }

                    if (!msg.matches("[BGORWY]{4}")) {
                        client.write(ByteBuffer.wrap("ERREUR_CARACTERE\n".getBytes()));
                        continue;
                    }

                    Code guess = new Code(msg);

                    state.tentatives++;

                    Code secret = state.secret;

                    int corr = secret.numberOfColorsWithCorrectPosition(guess);
                    int incorr = secret.numberOfColorsWithIncorrectPosition(guess);

                    String rep = corr + " " + incorr + " " + state.tentatives + "\n";

                    client.write(ByteBuffer.wrap(rep.getBytes()));

                    if (corr == 4) {

                        client.write(ByteBuffer.wrap(
                                "GAGNE ! Entrez votre nom :\n".getBytes()));

                        state.attendreNom = true;

                        System.out.println("Combinaison trouvée !");
                    }
                }
            }
        }
    }
}