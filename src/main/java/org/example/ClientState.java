public class ClientState {

    Code secret;
    int tentatives;
    boolean attendreNom;

    public ClientState(Code secret) {
        this.secret = secret;
        this.tentatives = 0;
        this.attendreNom = false;
    }
}