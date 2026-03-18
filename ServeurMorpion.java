import java.net.*;
import java.util.*;

public class ServeurMorpion {
    private static final int TAILLE_TAMPON = 1024;
    private DatagramSocket socket;
    private InetAddress client1Addr;
    private int client1Port;
    private InetAddress client2Addr;
    private int client2Port;
    private String sessionId;
    private Plateau plateau;
    private boolean tourClient1;

    public static void main(String[] args) {
        int port = 9876;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new ServeurMorpion().demarrer(port);
    }

    private void demarrer(int port) {
        try {
            initialiserServeur(port);
            attendreDeuxJoueurs();
            jouerPartie();
            socket.close();
            System.out.println("Serveur arrêté.");
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    private void initialiserServeur(int port) throws Exception {
        socket = new DatagramSocket(port);
        sessionId = UUID.randomUUID().toString();
        plateau = new Plateau();
        tourClient1 = true;
        System.out.println("Serveur démarré sur le port " + port);
    }

    private void attendreDeuxJoueurs() throws Exception {
        recevoirHello(1);
        recevoirHello(2);
    }

    private void recevoirHello(int numero) throws Exception {
        byte[] tampon = new byte[TAILLE_TAMPON];
        DatagramPacket paquet = new DatagramPacket(tampon, tampon.length);
        socket.receive(paquet);
        String contenu = new String(paquet.getData(), 0, paquet.getLength());
        if (!contenu.startsWith("HELLO")) {
            recevoirHello(numero);
            return;
        }
        notifierClient(paquet, numero);
    }

    private void notifierClient(DatagramPacket paquet, int numero) throws Exception {
        InetAddress addr = paquet.getAddress();
        int port = paquet.getPort();
        String symbole = (numero == 1) ? "X" : "O";
        if (numero == 1) {
            client1Addr = addr;
            client1Port = port;
        } else {
            client2Addr = addr;
            client2Port = port;
        }
        String reponse = "WELCOME|" + sessionId + "|" + symbole;
        socket.send(new DatagramPacket(reponse.getBytes(), reponse.length(), addr, port));
        System.out.println("Joueur " + numero + " connecté : " + addr + ":" + port);
    }

    private void jouerPartie() throws Exception {
        while (true) {
            MessageCoup mc = recevoirCoup();
            if (!appliquerSiValide(mc)) {
                envoyerMessage("ERREUR|MOUVEMENT_INVALIDE");
                continue;
            }
            String etat = calculerEtatJeu(mc.symbole);
            envoyerMessage(etat);
            if (etat.startsWith("FIN")) {
                break;
            }
            tourClient1 = !tourClient1;
        }
    }

    private MessageCoup recevoirCoup() throws Exception {
        byte[] tampon = new byte[TAILLE_TAMPON];
        DatagramPacket paquet = new DatagramPacket(tampon, tampon.length);
        socket.receive(paquet);
        String[] parts = new String(paquet.getData(), 0, paquet.getLength()).split("\\|");
        int ligne = Integer.parseInt(parts[2]);
        int colonne = Integer.parseInt(parts[3]);
        String symbole = (ticketClient1(paquet) ? "X" : "O");
        return new MessageCoup(ligne, colonne, symbole);
    }

    private boolean ticketClient1(DatagramPacket p) {
        return p.getAddress().equals(client1Addr) && p.getPort() == client1Port;
    }

    private boolean appliquerSiValide(MessageCoup mc) {
        boolean bonTour = (tourClient1 && mc.symbole.equals("X")) || (!tourClient1 && mc.symbole.equals("O"));
        if (!bonTour) {
            return false;
        }
        return plateau.appliquerCoup(mc.ligne, mc.colonne,
            mc.symbole.equals("X") ? Plateau.Cellule.CROIX : Plateau.Cellule.ROND);
    }

    private String calculerEtatJeu(String sym) {
        Plateau.Cellule gagnant = plateau.verifierVictoir();
        if (gagnant != Plateau.Cellule.VIDE) {
            return "FIN|GAGNE_" + sym;
        }
        if (plateau.estPlein()) {
            return "FIN|MATCH_NUL";
        }
        return "MISE_A_JOUR|" + plateau.serialiser() + "|EN_COURS";
    }

    private void envoyerMessage(String texte) throws Exception {
        byte[] data = texte.getBytes();
        DatagramPacket p1 = new DatagramPacket(data, data.length, client1Addr, client1Port);
        DatagramPacket p2 = new DatagramPacket(data, data.length, client2Addr, client2Port);
        socket.send(p1);
        socket.send(p2);
    }

    private static class MessageCoup {
        int ligne;
        int colonne;
        String symbole;
        MessageCoup(int l, int c, String s) {
            ligne = l;
            colonne = c;
            symbole = s;
        }
    }
}