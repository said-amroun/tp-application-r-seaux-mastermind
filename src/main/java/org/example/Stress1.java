public class Stress1 {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Stress1 <nombre_clients>");
            return;
        }

        int n = Integer.parseInt(args[0]);

        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                try {
                    IdleClient.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        System.out.println(n + " clients lancés.");
    }
}