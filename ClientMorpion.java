import java.net.*;
import java.util.*;

public class ClientMorpion {
    private static final int TAILLE_TAMPON = 1024;
    private DatagramSocket socket;
    private InetAddress serveurAddr;
    private int serveurPort;
    private String sessionId;
    private String monSymbole;
    private Plateau plateau;
    private boolean monTour;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage : java ClientMorpion <adresse_serveur> <port>");
            return;
        }
        new ClientMorpion().demarrer(args[0], Integer.parseInt(args[1]));
    }

    private void demarrer(String host, int port) {
        try {
            serveurAddr = InetAddress.getByName(host);
            serveurPort = port;
            socket = new DatagramSocket();
            plateau = new Plateau();
            envoyerMessage("HELLO|Player");
            recevoirWelcome();
            jouer();
        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    private void recevoirWelcome() throws Exception {
        String data = recevoirMessage();
        String[] parts = data.split("\\|");
        sessionId = parts[1];
        monSymbole = parts[2];
        monTour = monSymbole.equals("X");
        System.out.println("Connecté. Vous jouez : " + monSymbole);
    }

    private void jouer() throws Exception {
        while (true) {
            System.out.println(plateau);
            if (monTour) {
                System.out.print("Votre coup (ligne colonne) : ");
                Scanner sc = new Scanner(System.in);
                int l = sc.nextInt();
                int c = sc.nextInt();
                envoyerMessage("COUP|" + sessionId + "|" + l + "|" + c);
            }
            String retour = recevoirMessage();
            String[] parts = retour.split("\\|");
            if (parts[0].equals("ERREUR")) {
                System.out.println("Coup invalide.");
            } else if (parts[0].equals("MISE_A_JOUR")) {
                plateau = Plateau.deserialiser(parts[1]);
                monTour = !monTour;
            } else if (parts[0].equals("FIN")) {
                System.out.println("Fin : " + parts[1]);
                break;
            }
        }
    }

    private void envoyerMessage(String texte) throws Exception {
        byte[] buf = texte.getBytes();
        DatagramPacket p = new DatagramPacket(buf, buf.length, serveurAddr, serveurPort);
        socket.send(p);
    }

    private String recevoirMessage() throws Exception {
        byte[] buf = new byte[TAILLE_TAMPON];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        socket.receive(p);
        return new String(p.getData(), 0, p.getLength());
    }
}