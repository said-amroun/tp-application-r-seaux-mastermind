

import java.io.*;
import java.net.*;
import java.util.Random;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            Random random = new Random();
            Code secret = new Code(random);



            while (true) {

                String guessStr = in.readLine();

                if (guessStr == null) {
                    System.out.println("Client déconnecté");
                    break;
                }

                guessStr = guessStr.trim();
                System.out.println("Reçu : " + guessStr);

                if (guessStr.length() != 4) {
                    out.println("ERREUR_LONGUEUR");
                    continue;
                }

                if (!guessStr.matches("[BGORWY]{4}")) {
                    out.println("ERREUR_CARACTERE");
                    continue;
                }

                Code guess = new Code(guessStr);

                long start = System.nanoTime();

                int corrPos = secret.numberOfColorsWithCorrectPosition(guess);
                int incorrPos = secret.numberOfColorsWithIncorrectPosition(guess);

                long end = System.nanoTime();
                long duration = end - start;

                System.out.println("Temps réponse (ns) = " + duration);
                // ecrire dans un fichier csv
                FileWriter fw = new FileWriter("mesures.csv", true);
                fw.write(duration + "\n");
                fw.close();

                out.println(corrPos + " " + incorrPos);

                if (corrPos == 4) {
                    System.out.println("Combinaison trouvée !");
                    break;
                }
            }

            socket.close();
            System.out.println("Connexion fermée");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}