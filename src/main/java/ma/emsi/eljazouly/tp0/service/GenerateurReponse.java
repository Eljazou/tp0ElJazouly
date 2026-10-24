package ma.emsi.eljazouly.tp0.service;

import jakarta.enterprise.context.Dependent;

import java.io.Serializable;
import java.util.Locale;

/**
 * Classe de service pour les modificateurs de question.
 * Un modificateur de question prend en entrée une question et retourne
 * la question modifiée.
 */
@Dependent // Portée CDI pour rendre la classe injectable par CDI dans les autres classes.
// Portée Dependent : l'instance sera supprimée quand la classe qui l'a injectée sera supprimée.
public class GenerateurReponse implements Serializable { // Serializable car CDI peut mettre l'instance en mémoire secondaire.
    private boolean roleSystemeAffiche = false;

    /**
     * Génère une réponse à une question.
     * @param question La question à modifier.
     * @param roleSysteme Le rôle système à utiliser pour la modification de la question.
     * @return Le rôle système en majuscule au début, s'il n'est pas null (seulement pour la première réponse),
     * suivi d'un saut de ligne,
     * puis une petite fiche d'analyse de la question (nombre de mots, mot le plus long,
     * nombre de voyelles, palindrome ou non), adaptée au rôle choisi, le tout entouré de "||".
     */
    public String genererReponse(String question, String roleSysteme) {
        String resultat = "||";
        if (!this.roleSystemeAffiche && roleSysteme != null) {
            // Ajouter le rôle système en majuscule au début du résultat, suivi d'un saut de ligne.
            resultat += roleSysteme.toUpperCase(Locale.FRENCH) + "\n";
            this.roleSystemeAffiche = true;
        }

        // ===== Traitement personnel : analyseur de question =====

        // Cas de la question vide ou null : rien à analyser.
        if (question == null || question.isBlank()) {
            return resultat + "Ta question est vide, je n'ai rien à analyser.||";
        }

        // 1. Les mots : on coupe la question partout où il n'y a ni lettre ni chiffre
        //    (espaces, ponctuation, apostrophes...).
        String[] morceaux = question.trim().split("[^\\p{L}\\p{N}]+");
        int nbMots = 0;
        String motLePlusLong = "";
        for (String morceau : morceaux) {
            if (morceau.isEmpty()) {
                continue; // morceau vide si la question commence par une ponctuation
            }
            nbMots++;
            if (morceau.length() > motLePlusLong.length()) {
                motLePlusLong = morceau; // en cas d'égalité, on garde le premier
            }
        }
        // Cas d'une question faite seulement de ponctuation (par exemple "?!") : aucun mot.
        if (nbMots == 0) {
            return resultat + "Ta question ne contient aucun mot, je n'ai rien à analyser.||";
        }

        // 2. Les voyelles, accents compris (on compare en minuscules).
        String voyelles = "aeiouyàâäéèêëîïôöùûüÿæœ";
        String questionMinuscule = question.toLowerCase(Locale.FRENCH);
        int nbVoyelles = 0;
        for (char c : questionMinuscule.toCharArray()) {
            if (voyelles.indexOf(c) >= 0) {
                nbVoyelles++;
            }
        }

        // 3. Le palindrome : on garde seulement les lettres et les chiffres, en minuscules,
        //    puis on compare le texte avec son inverse.
        StringBuilder lettres = new StringBuilder();
        for (char c : questionMinuscule.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                lettres.append(c);
            }
        }
        String texte = lettres.toString();
        boolean palindrome = !texte.isEmpty() && texte.equals(lettres.reverse().toString());

        // 4. La phrase de base, commune aux 3 rôles.
        String fiche = "Ta question contient " + nbMots + (nbMots > 1 ? " mots" : " mot")
                + ", le plus long est \"" + motLePlusLong + "\". "
                + "Elle a " + nbVoyelles + (nbVoyelles > 1 ? " voyelles" : " voyelle")
                + (palindrome ? " et c'est un palindrome !" : " et ce n'est pas un palindrome.");

        // 5. Adaptation au rôle. On reconnaît le rôle avec un mot de son texte (voir Bb.getRolesSysteme()).
        String role = (roleSysteme == null) ? "" : roleSysteme.toLowerCase(Locale.ENGLISH);
        if (role.contains("interpreter")) {
            // Traducteur : on devine la langue en comptant quelques mots courants.
            String[] motsFrancais = {"le", "la", "les", "et", "est", "un", "une", "des", "de", "je", "tu", "il", "que", "pas", "bonjour"};
            String[] motsAnglais = {"the", "is", "and", "a", "an", "of", "to", "you", "are", "what", "it", "in", "hello"};
            int scoreFrancais = 0;
            int scoreAnglais = 0;
            for (String morceau : morceaux) {
                String mot = morceau.toLowerCase(Locale.FRENCH);
                for (String fr : motsFrancais) {
                    if (mot.equals(fr)) {
                        scoreFrancais++;
                    }
                }
                for (String en : motsAnglais) {
                    if (mot.equals(en)) {
                        scoreAnglais++;
                    }
                }
            }
            String langue;
            if (scoreFrancais > scoreAnglais) {
                langue = "Ton texte semble être en français.";
            } else if (scoreAnglais > scoreFrancais) {
                langue = "Ton texte semble être en anglais.";
            } else {
                langue = "Je n'arrive pas à savoir si ton texte est en français ou en anglais.";
            }
            fiche += "\n" + langue;
        } else if (role.contains("travel guide")) {
            // Guide touristique : mêmes statistiques, avec un petit mot de fin.
            fiche += "\nBonne visite !";
        }
        // Assistant (ou rôle écrit à la main) : la phrase de base suffit.

        resultat += fiche + "||";
        return resultat;
    }
}
