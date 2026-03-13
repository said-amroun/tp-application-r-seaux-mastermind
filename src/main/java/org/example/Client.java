import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", 1234);

        BufferedReader clavier = new BufferedReader(
                new InputStreamReader(System.in));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

        while (true) {

            System.out.print("Entrez une combinaison : ");
            String message = clavier.readLine().trim();

            out.println(message);

            String reponse = in.readLine();

            if (reponse == null) {
                System.out.println("Serveur déconnecté.");
                break;
            }

            if (message.equals("/SCORE")) {
                System.out.println("Scores :");
                System.out.println(reponse);

                socket.setSoTimeout(200);

                try {
                    while ((reponse = in.readLine()) != null) {
                        System.out.println(reponse);
                    }
                } catch (Exception e) {
                    // fin des scores
                }

                socket.setSoTimeout(0);
                continue;
            }

            if (reponse.equals("ERREUR_LONGUEUR")) {
                System.out.println("Erreur : la combinaison doit contenir 4 lettres.");
                continue;
            }

            if (reponse.equals("ERREUR_CARACTERE")) {
                System.out.println("Erreur : lettres autorisées = B G O R W Y.");
                continue;
            }

            String[] decoupe = reponse.split(" ");
            int corr = Integer.parseInt(decoupe[0]);
            int incorr = Integer.parseInt(decoupe[1]);

            System.out.println("Correct : " + corr + " | Incorrect : " + incorr);

            if (corr == 4) {

                System.out.println("Combinaison trouvée !");

                String demandeNom = in.readLine();
                System.out.println(demandeNom);

                String nom = clavier.readLine();
                out.println(nom);

                String confirmation = in.readLine();
                System.out.println(confirmation);
            }
        }
    }
}