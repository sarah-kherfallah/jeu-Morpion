public class Plateau {
    public enum Cellule { VIDE, CROIX, ROND }
    private Cellule[][] grille = new Cellule[3][3];

    public Plateau() {
        reinitialiser();
    }

    public void reinitialiser() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grille[i][j] = Cellule.VIDE;
            }
        }
    }

    public boolean appliquerCoup(int ligne, int colonne, Cellule symbole) {
        if (ligne < 0 || ligne > 2 || colonne < 0 || colonne > 2) {
            return false;
        }
        if (grille[ligne][colonne] != Cellule.VIDE) {
            return false;
        }
        grille[ligne][colonne] = symbole;
        return true;
    }

    public Cellule verifierVictoir() {
        Cellule gagnant = verifierLignesColonnes();
        if (gagnant != Cellule.VIDE) {
            return gagnant;
        }
        return verifierDiagonales();
    }

    private Cellule verifierLignesColonnes() {
        for (int i = 0; i < 3; i++) {
            Cellule result = verifierTrois(grille[i][0], grille[i][1], grille[i][2]);
            if (result != Cellule.VIDE) {
                return result;
            }
            result = verifierTrois(grille[0][i], grille[1][i], grille[2][i]);
            if (result != Cellule.VIDE) {
                return result;
            }
        }
        return Cellule.VIDE;
    }

    private Cellule verifierDiagonales() {
        Cellule result = verifierTrois(grille[0][0], grille[1][1], grille[2][2]);
        if (result != Cellule.VIDE) {
            return result;
        }
        return verifierTrois(grille[0][2], grille[1][1], grille[2][0]);
    }

    private Cellule verifierTrois(Cellule a, Cellule b, Cellule c) {
        if (a == b && b == c && a != Cellule.VIDE) {
            return a;
        }
        return Cellule.VIDE;
    }

    public boolean estPlein() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[i][j] == Cellule.VIDE) {
                    return false;
                }
            }
        }
        return true;
    }

    public String serialiser() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[i][j] == Cellule.CROIX) {
                    sb.append('X');
                } else if (grille[i][j] == Cellule.ROND) {
                    sb.append('O');
                } else {
                    sb.append('_');
                }
            }
        }
        return sb.toString();
    }

    public static Plateau deserialiser(String donnees) {
        Plateau plateau = new Plateau();
        for (int k = 0; k < 9 && k < donnees.length(); k++) {
            char c = donnees.charAt(k);
            if (c == 'X') {
                plateau.grille[k/3][k%3] = Cellule.CROIX;
            } else if (c == 'O') {
                plateau.grille[k/3][k%3] = Cellule.ROND;
            } else {
                plateau.grille[k/3][k%3] = Cellule.VIDE;
            }
        }
        return plateau;
    }

    @Override
    public String toString() {
        return formaterGrille();
    }

    private String formaterGrille() {
        StringBuilder sb = new StringBuilder();
        // entête colonnes
        sb.append("    0   1   2\n");
        for (int i = 0; i < 3; i++) {
            // ligne sépare
            if (i > 0) {
                sb.append("   ---+---+---\n");
            }
            // numéro de ligne et contenu
            sb.append(i).append("  ");
            for (int j = 0; j < 3; j++) {
                char symbole;
                if (grille[i][j] == Cellule.CROIX) {
                    symbole = 'X';
                } else if (grille[i][j] == Cellule.ROND) {
                    symbole = 'O';
                } else {
                    symbole = ' ';
                }
                sb.append(" ").append(symbole).append(" ");
                if (j < 2) {
                    sb.append("|");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}