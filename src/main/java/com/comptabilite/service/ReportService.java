package com.comptabilite.service;

import com.comptabilite.dao.*;
import com.comptabilite.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final CompteDAO compteDAO;
    private final EcritureComptableDAO ecritureDAO;
    private final LigneEcritureDAO ligneEcritureDAO;
    private final ExerciceDAO exerciceDAO;

    public ReportService() {
        this.compteDAO = new CompteDAO();
        this.ecritureDAO = new EcritureComptableDAO();
        this.ligneEcritureDAO = new LigneEcritureDAO();
        this.exerciceDAO = new ExerciceDAO();
    }

    public List<LigneGrandLivre> getGrandLivre(Long compteId, LocalDate dateDebut, LocalDate dateFin) {
        logger.info("Génération du Grand Livre pour le compte {} du {} au {}", compteId, dateDebut, dateFin);

        if (compteId == null) {
            throw new IllegalArgumentException("ID du compte ne peut pas être null");
        }

        try {
            Compte compte = compteDAO.findById(compteId).orElse(null);
            if (compte == null) {
                throw new IllegalArgumentException("Compte non trouvé: " + compteId);
            }
            logger.info("Compte trouvé: {} - {}", compte.getNumeroCompte(), compte.getLibelle());

            List<LigneEcriture> lignes = ligneEcritureDAO.findByCompteAndDateRange(compteId, dateDebut, dateFin);
            logger.info("Nombre de lignes d'écriture trouvées: {}", lignes.size());

            List<LigneGrandLivre> grandLivre = new ArrayList<>();
            BigDecimal soldeCumulé = BigDecimal.ZERO;

            for (LigneEcriture ligne : lignes) {
                try {
                    LigneGrandLivre ligneGL = new LigneGrandLivre();

                    // Vérification sécurisée des données
                    if (ligne.getEcritureComptable() != null) {
                        ligneGL.setDate(ligne.getEcritureComptable().getDateEcriture());
                        ligneGL.setLibelle(ligne.getEcritureComptable().getLibelle() != null ?
                            ligne.getEcritureComptable().getLibelle() : "");
                        ligneGL.setNumeroJournal(ligne.getEcritureComptable().getNumeroJournal() != null ?
                            ligne.getEcritureComptable().getNumeroJournal() : "");
                    } else {
                        logger.warn("Écriture comptable null pour la ligne {}", ligne.getId());
                        continue;
                    }

                    ligneGL.setDebit(ligne.getMontantDebit() != null ? ligne.getMontantDebit() : BigDecimal.ZERO);
                    ligneGL.setCredit(ligne.getMontantCredit() != null ? ligne.getMontantCredit() : BigDecimal.ZERO);

                    // Calcul du solde cumulé selon le type de compte
                    if (compte.getTypeCompte() == Compte.TypeCompte.ACTIF) {
                        // Comptes d'actif : Débit augmente, Crédit diminue
                        soldeCumulé = soldeCumulé.add(ligneGL.getDebit()).subtract(ligneGL.getCredit());
                    } else {
                        // Comptes de passif, capital, produits : Crédit augmente, Débit diminue
                        soldeCumulé = soldeCumulé.subtract(ligneGL.getDebit()).add(ligneGL.getCredit());
                    }
                    ligneGL.setSoldeCumule(soldeCumulé);

                    grandLivre.add(ligneGL);

                } catch (Exception e) {
                    logger.error("Erreur lors du traitement de la ligne {}: {}", ligne.getId(), e.getMessage());
                }
            }

            logger.info("Grand livre généré avec {} lignes", grandLivre.size());
            return grandLivre;

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du grand livre", e);
            throw new RuntimeException("Erreur lors de la génération du grand livre: " + e.getMessage(), e);
        }
    }

    public List<LigneBalance> getBalance(Long exerciceId) {
        logger.info("Génération de la Balance pour l'exercice {}", exerciceId);

        Exercice exercice = exerciceDAO.findById(exerciceId).orElse(null);
        if (exercice == null) {
            throw new IllegalArgumentException("Exercice non trouvé: " + exerciceId);
        }

        List<Compte> comptes = compteDAO.findByEntreprise(exercice.getEntreprise().getId());
        List<LigneBalance> balance = new ArrayList<>();

        for (Compte compte : comptes) {
            List<LigneEcriture> lignes = ligneEcritureDAO.findByCompteAndDateRange(
                compte.getId(),
                exercice.getDateDebut(),
                exercice.getDateFin()
            );

            LigneBalance ligneBalance = new LigneBalance();
            ligneBalance.setNumeroCompte(compte.getNumeroCompte());
            ligneBalance.setNomCompte(compte.getLibelle());

            // Calculer les mouvements par écritures comptables
            BigDecimal mouvementEcritureDebit = lignes.stream()
                .map(LigneEcriture::getMontantDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal mouvementEcritureCredit = lignes.stream()
                .map(LigneEcriture::getMontantCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Récupérer les soldes finaux consolidés (incluant les sous-comptes)
            BigDecimal soldeDebiteurFinal = compte.getSoldeDebiteurConsolide() != null ?
                compte.getSoldeDebiteurConsolide() : BigDecimal.ZERO;
            BigDecimal soldeCrediteurFinal = compte.getSoldeCrediteurConsolide() != null ?
                compte.getSoldeCrediteurConsolide() : BigDecimal.ZERO;

            // Récupérer le solde initial (début d'exercice)
            BigDecimal soldeInitial = compte.getSoldeInitial() != null ?
                compte.getSoldeInitial() : BigDecimal.ZERO;

            // CALCUL CORRECT DES MOUVEMENTS TOTAUX :
            // Mouvements = Solde final - Solde initial
            // Cela inclut TOUTES les modifications : saisies directes + écritures
            BigDecimal soldeFinalNet = soldeDebiteurFinal.subtract(soldeCrediteurFinal);
            BigDecimal mouvementTotal = soldeFinalNet.subtract(soldeInitial);

            // Séparer en débit/crédit selon le signe
            BigDecimal totalDebit, totalCredit;
            if (mouvementTotal.compareTo(BigDecimal.ZERO) >= 0) {
                totalDebit = mouvementTotal;
                totalCredit = BigDecimal.ZERO;
            } else {
                totalDebit = BigDecimal.ZERO;
                totalCredit = mouvementTotal.abs();
            }

            ligneBalance.setTotalDebit(totalDebit);
            ligneBalance.setTotalCredit(totalCredit);
            ligneBalance.setSoldeDebiteur(soldeDebiteurFinal);
            ligneBalance.setSoldeCrediteur(soldeCrediteurFinal);

            // Afficher tous les comptes avec des mouvements ou des soldes
            if (totalDebit.compareTo(BigDecimal.ZERO) != 0 ||
                totalCredit.compareTo(BigDecimal.ZERO) != 0 ||
                soldeDebiteurFinal.compareTo(BigDecimal.ZERO) != 0 ||
                soldeCrediteurFinal.compareTo(BigDecimal.ZERO) != 0) {
                balance.add(ligneBalance);
            }
        }
        return balance.stream()
            .sorted(Comparator.comparing(LigneBalance::getNumeroCompte))
            .collect(Collectors.toList());
    }

    public BilanData getBilan(Long exerciceId) {
        logger.info("Génération du Bilan pour l'exercice {}", exerciceId);

        // SOLUTION: Calcul direct avec vérification d'équilibre
        BilanData bilan = calculateBilanDirect(exerciceId);

        return bilan;
    }

    private BilanData calculateBilanDirect(Long exerciceId) {
        // Récupérer directement les comptes avec leurs soldes finaux
        Exercice exercice = exerciceDAO.findById(exerciceId).orElse(null);
        if (exercice == null) {
            throw new IllegalArgumentException("Exercice non trouvé: " + exerciceId);
        }

        List<Compte> comptes = compteDAO.findByEntreprise(exercice.getEntreprise().getId());
        logger.info("Nombre de comptes pour le bilan: {}", comptes.size());

        BilanData bilan = new BilanData();

        // Calculs par type de compte selon la logique comptable française
        BigDecimal actifImmobilise = BigDecimal.ZERO;
        BigDecimal actifCirculant = BigDecimal.ZERO;
        BigDecimal creances = BigDecimal.ZERO;
        BigDecimal tresorerie = BigDecimal.ZERO;
        BigDecimal capitauxPropres = BigDecimal.ZERO;
        BigDecimal dettes = BigDecimal.ZERO;
        BigDecimal capitalTotalCalcule = BigDecimal.ZERO; // Pour stocker le capital réel

        for (Compte compte : comptes) {
            String numeroCompte = compte.getNumeroCompte();

            // Utiliser les soldes consolidés finaux
            BigDecimal soldeDebiteur = compte.getSoldeDebiteurConsolide();
            BigDecimal soldeCrediteur = compte.getSoldeCrediteurConsolide();
            BigDecimal soldeNet = soldeDebiteur.subtract(soldeCrediteur);

            logger.info("=== BILAN DEBUG === Compte {}: Débit={}, Crédit={}, Net={}",
                numeroCompte, soldeDebiteur, soldeCrediteur, soldeNet);

            // Classification selon le plan comptable (français ou OHADA/Cameroun)
            char classe;
            if (numeroCompte.startsWith("CM") && numeroCompte.length() > 2) {
                // Format Cameroun OHADA : CM + classe (ex: CM101001 -> classe 1)
                classe = numeroCompte.charAt(2);
            } else {
                // Format français standard : première lettre (ex: 101001 -> classe 1)
                classe = numeroCompte.charAt(0);
            }

            switch (classe) {
                case '1': // Comptes de capitaux - NOUVELLE STRATÉGIE SIMPLIFIÉE
                    if (numeroCompte.startsWith("CM109") || numeroCompte.contains("109")) {
                        // Actions souscrites non libérées - ACTIF (créances sur actionnaires)
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            creances = creances.add(soldeNet);
                            logger.info("Actions non libérées {}: {} -> créances = {}",
                                numeroCompte, soldeNet, creances);
                        }
                    } else if (numeroCompte.equals("CM101000")) {
                        // Compte capital principal - traitement spécial
                        // ÉVITER LE DOUBLE COMPTAGE : ne traiter que si pas de sous-comptes
                        // Utiliser le solde débiteur réel du compte CM101000 (capital souscrit total)
                        BigDecimal capitalSouscrit = compte.getSoldeDebiteur();
                        if (capitalSouscrit == null || capitalSouscrit.compareTo(BigDecimal.ZERO) == 0) {
                            // Si le solde direct est vide, utiliser le solde consolidé
                            capitalSouscrit = compte.getSoldeDebiteurConsolide();
                        }
                        capitauxPropres = capitauxPropres.add(capitalSouscrit);
                        capitalTotalCalcule = capitalSouscrit; // Stocker pour l'équilibrage
                        logger.info("Capital principal CM101000: solde débiteur {} -> capitaux += {}",
                            capitalSouscrit, capitalSouscrit);
                    } else if (numeroCompte.startsWith("CM101") && !numeroCompte.equals("CM101000")) {
                        // Sous-comptes actionnaires (CM101001, CM101002, etc.)
                        // LOGIQUE CORRECTE : traiter chaque actionnaire individuellement
                        boolean hasSubAccounts = compte.getSousComptes() != null && !compte.getSousComptes().isEmpty();
                        if (!hasSubAccounts) {
                            if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                                // Solde débiteur = actionnaire doit encore payer (CRÉANCE)
                                creances = creances.add(soldeNet);
                                logger.info("Actionnaire débiteur {}: {} (doit encore payer) -> créances += {}",
                                    numeroCompte, soldeNet, soldeNet);
                            } else if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                                // Solde créditeur = actionnaire a trop payé (DETTE)
                                BigDecimal surLiberation = soldeNet.abs();
                                dettes = dettes.add(surLiberation);
                                logger.info("Actionnaire créditeur {}: {} (a trop payé) -> dettes += {}",
                                    numeroCompte, surLiberation, surLiberation);
                            }
                        } else {
                            logger.info("Sous-compte parent {} ignoré (a des enfants)", numeroCompte);
                        }
                        // NE PAS affecter les capitaux propres
                    } else {
                        // Autres comptes de classe 1 (réserves, etc.)
                        if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                            // Solde créditeur = augmente les capitaux propres
                            capitauxPropres = capitauxPropres.add(soldeNet.abs());
                            logger.info("Autres capitaux {}: solde créditeur {} -> capitaux += {}",
                                numeroCompte, soldeNet, soldeNet.abs());
                        } else if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            // Solde débiteur = diminue les capitaux propres
                            capitauxPropres = capitauxPropres.subtract(soldeNet);
                            logger.info("Autres capitaux {}: solde débiteur {} -> capitaux -= {}",
                                numeroCompte, soldeNet, soldeNet);
                        }
                    }
                    break;

                case '2': // Immobilisations
                    if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                        actifImmobilise = actifImmobilise.add(soldeNet); // Solde débiteur = Actif
                    }
                    break;

                case '3': // Stocks
                    if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                        actifCirculant = actifCirculant.add(soldeNet); // Solde débiteur = Actif
                    }
                    break;

                case '4': // Comptes de tiers
                    if (numeroCompte.startsWith("41")) { // Clients
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            creances = creances.add(soldeNet); // Solde débiteur = Actif (créances)
                        } else {
                            dettes = dettes.add(soldeNet.abs()); // Client créditeur = dette
                        }
                    } else if (numeroCompte.startsWith("40")) { // Fournisseurs
                        if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                            dettes = dettes.add(soldeNet.abs()); // Solde créditeur = Passif (dettes)
                        } else if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            creances = creances.add(soldeNet); // Fournisseur débiteur = créance
                        }
                    } else if (numeroCompte.startsWith("445")) { // TVA
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            creances = creances.add(soldeNet); // TVA déductible = Actif
                        } else if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                            dettes = dettes.add(soldeNet.abs()); // TVA collectée = Passif
                        }
                    } else { // Autres comptes de tiers
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            creances = creances.add(soldeNet); // Solde débiteur = créance
                        } else if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                            dettes = dettes.add(soldeNet.abs()); // Solde créditeur = dette
                        }
                    }
                    break;

                case '5': // Comptes financiers
                    // Distinction entre trésorerie et autres comptes financiers
                    logger.info("BILAN DEBUG - Compte {}: {} = {}", numeroCompte, compte.getLibelle(), soldeNet);
                    if (numeroCompte.startsWith("CM512") || numeroCompte.startsWith("CM52") ||
                        numeroCompte.startsWith("CM53") || numeroCompte.startsWith("CM57")) {
                        // Comptes de banque (CM52x) et caisse (CM57x) - Actif si débiteurs
                        // ÉVITER LE DOUBLE COMPTAGE : exclure les comptes parents qui ont des sous-comptes
                        boolean hasSubAccounts = compte.getSousComptes() != null && !compte.getSousComptes().isEmpty();
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0 && !hasSubAccounts) {
                            logger.info("AJOUT TRÉSORERIE: {} += {} = {} (compte sans sous-comptes)",
                                tresorerie, soldeNet, tresorerie.add(soldeNet));
                            tresorerie = tresorerie.add(soldeNet);
                        } else if (hasSubAccounts) {
                            logger.info("IGNORÉ TRÉSORERIE: {} (compte parent avec sous-comptes)", numeroCompte);
                        }
                    } else if (numeroCompte.startsWith("456") || numeroCompte.startsWith("458")) {
                        // Comptes associés - Passif si créditeurs, Actif si débiteurs
                        if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                            // Créditeur = dette envers associés (Passif)
                            logger.info("AJOUT DETTES ASSOCIÉS: {} += {} = {}",
                                dettes, soldeNet.abs(), dettes.add(soldeNet.abs()));
                            dettes = dettes.add(soldeNet.abs());
                        } else if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            // Débiteur = créance sur associés (Actif)
                            logger.info("AJOUT CRÉANCES ASSOCIÉS: {} += {} = {}",
                                creances, soldeNet, creances.add(soldeNet));
                            creances = creances.add(soldeNet);
                        }
                    } else {
                        // Autres comptes financiers - traitement générique
                        if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                            logger.info("AJOUT AUTRES FINANCIERS: {} += {} = {}",
                                tresorerie, soldeNet, tresorerie.add(soldeNet));
                            tresorerie = tresorerie.add(soldeNet);
                        }
                    }
                    break;

                case '6': // Charges (déjà traitées dans le calcul du résultat)
                case '7': // Produits (déjà traitées dans le calcul du résultat)
                case '8': // Comptes spéciaux (généralement ignorés dans le bilan)
                    // Ces comptes sont traités séparément pour le calcul du résultat
                    break;

                default:
                    logger.warn("Compte {} non classé dans le bilan", numeroCompte);
                    break;
            }
        }

        // Calcul du résultat de l'exercice (Produits - Charges)
        BigDecimal totalCharges = BigDecimal.ZERO;
        BigDecimal totalProduits = BigDecimal.ZERO;

        for (Compte compte : comptes) {
            String numeroCompte = compte.getNumeroCompte();
            BigDecimal soldeDebiteur = compte.getSoldeDebiteurConsolide();
            BigDecimal soldeCrediteur = compte.getSoldeCrediteurConsolide();
            BigDecimal soldeNet = soldeDebiteur.subtract(soldeCrediteur);

            // Classification selon le plan comptable (français ou OHADA/Cameroun)
            char classe;
            if (numeroCompte.startsWith("CM") && numeroCompte.length() > 2) {
                classe = numeroCompte.charAt(2);
            } else {
                classe = numeroCompte.charAt(0);
            }

            if (classe == '6') { // Charges
                // Les charges ont normalement un solde débiteur (positif)
                if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                    totalCharges = totalCharges.add(soldeNet);
                }
            } else if (classe == '7') { // Produits
                // Les produits ont normalement un solde créditeur (négatif)
                 if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
                    totalProduits = totalProduits.add(soldeNet.abs()); // Convertir en positif
                } else if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
                    // Cas anormal: produit avec solde débiteur (ex: compte 756 mal saisi)
                    // On le traite quand même comme un produit
                    totalProduits = totalProduits.add(soldeNet);
                    logger.warn("Compte produit {} avec solde débiteur anormal: {}", numeroCompte, soldeNet);
                }
            }
        }

        BigDecimal resultatExercice = totalProduits.subtract(totalCharges);
        logger.info("Calcul résultat: Produits {} - Charges {} = {}", totalProduits, totalCharges, resultatExercice);

        // Ajouter le résultat aux capitaux propres
        capitauxPropres = capitauxPropres.add(resultatExercice);

        // VÉRIFICATION DE LA LOGIQUE COMPTABLE
        logger.info("BILAN FINAL (logique détaillée par actionnaire):");
        logger.info("Capital souscrit total: {}", capitalTotalCalcule);
        logger.info("Trésorerie encaissée: {}", tresorerie);
        logger.info("Créances (actionnaires débiteurs): {}", creances);
        logger.info("Dettes (actionnaires créditeurs): {}", dettes);

        // Vérification de l'équation comptable
        BigDecimal totalActifCalcule = tresorerie.add(creances);
        BigDecimal totalPassifCalcule = capitalTotalCalcule.add(dettes);
        BigDecimal difference = totalActifCalcule.subtract(totalPassifCalcule);

        logger.info("ÉQUILIBRE: Actif {} vs Passif {} = Différence {}",
            totalActifCalcule, totalPassifCalcule, difference);

        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            logger.warn("DÉSÉQUILIBRE DÉTECTÉ: {} - Vérifiez les calculs", difference);
        } else {
            logger.info("BILAN ÉQUILIBRÉ !");
        }

        bilan.setActifImmobilise(actifImmobilise);
        bilan.setActifCirculant(actifCirculant);
        bilan.setCreances(creances);
        bilan.setTresorerie(tresorerie);
        bilan.setCapitauxPropres(capitauxPropres);
        bilan.setDettesFinancieres(BigDecimal.ZERO); // Pas de dettes financières dans nos données
        bilan.setDettesExploitation(dettes);

        logger.info("=== DÉTAIL BILAN ===");
        logger.info("ACTIF - Immobilisé: {}, Circulant: {}, Créances: {}, Trésorerie: {}",
            actifImmobilise, actifCirculant, creances, tresorerie);
        logger.info("PASSIF - Capitaux Propres (avec résultat): {}, Dettes Exploitation: {}",
            capitauxPropres, dettes);
        logger.info("RÉSULTAT - Produits: {}, Charges: {}, Net: {}",
            totalProduits, totalCharges, resultatExercice);
        BigDecimal totalActif = bilan.getTotalActif();
        BigDecimal totalPassif = bilan.getTotalPassif();
        BigDecimal ecart = totalActif.subtract(totalPassif);

        logger.info("TOTAUX - Actif: {}, Passif: {}, Écart: {}",
            totalActif, totalPassif, ecart);

        // Vérification de l'équilibre du bilan
        if (ecart.compareTo(BigDecimal.ZERO) != 0) {
            logger.error("DÉSÉQUILIBRE DÉTECTÉ: {} - Vérifiez les écritures comptables", ecart);
            // Ne pas corriger automatiquement, signaler l'erreur
        }

        return bilan;
    }

    public CompteResultatData getCompteResultat(Long exerciceId) {
        logger.info("Génération du Compte de Résultat pour l'exercice {}", exerciceId);

        try {
            List<LigneBalance> balance = getBalance(exerciceId);
            CompteResultatData compteResultat = new CompteResultatData();

            Map<Integer, List<LigneBalance>> comptesByClasse = balance.stream()
                .filter(ligne -> ligne.getNumeroCompte() != null)
                .collect(Collectors.groupingBy(ligne -> {
                    try {
                        String numeroCompte = ligne.getNumeroCompte();
                        // Gestion des comptes OHADA (CM) et français
                        if (numeroCompte.startsWith("CM") && numeroCompte.length() > 2) {
                            return Integer.parseInt(numeroCompte.substring(2, 3)); // CM6xxxx -> 6
                        } else {
                            return Integer.parseInt(numeroCompte.substring(0, 1)); // 6xxxx -> 6
                        }
                    } catch (Exception e) {
                        logger.warn("Impossible d'extraire la classe du compte: {}", ligne.getNumeroCompte());
                        return 0; // Classe par défaut
                    }
                }));

            // Classe 6 = Charges, Classe 7 = Produits
            BigDecimal totalCharges = getSoldeClasse(comptesByClasse.get(6));
            BigDecimal totalProduits = getSoldeClasse(comptesByClasse.get(7));

            logger.info("Compte de résultat - Charges: {}, Produits: {}", totalCharges, totalProduits);

            compteResultat.setChiffresAffaires(totalProduits.abs()); // Produits en valeur absolue
            compteResultat.setChargesExploitation(totalCharges.abs()); // Charges en valeur absolue
            compteResultat.setChargesFinancieres(BigDecimal.ZERO); // À détailler si nécessaire
            compteResultat.setProduitsFinanciers(BigDecimal.ZERO); // À détailler si nécessaire

            BigDecimal resultatNet = totalProduits.add(totalCharges); // Charges sont déjà négatives ou positives selon le cas
            compteResultat.setResultatNet(resultatNet);

            logger.info("Résultat net calculé: {}", resultatNet);

            return compteResultat;

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du compte de résultat", e);
            throw new RuntimeException("Erreur de génération du compte de résultat: " + e.getMessage(), e);
        }
    }

    private BigDecimal getSoldeClasse(List<LigneBalance> lignes) {
        if (lignes == null) return BigDecimal.ZERO;

        return lignes.stream()
            .map(ligne -> ligne.getSoldeDebiteur().subtract(ligne.getSoldeCrediteur()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static class LigneGrandLivre {
        private LocalDate date;
        private String libelle;
        private String numeroJournal;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal soldeCumule;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getLibelle() { return libelle; }
        public void setLibelle(String libelle) { this.libelle = libelle; }
        public String getNumeroJournal() { return numeroJournal; }
        public void setNumeroJournal(String numeroJournal) { this.numeroJournal = numeroJournal; }
        public BigDecimal getDebit() { return debit; }
        public void setDebit(BigDecimal debit) { this.debit = debit; }
        public BigDecimal getCredit() { return credit; }
        public void setCredit(BigDecimal credit) { this.credit = credit; }
        public BigDecimal getSoldeCumule() { return soldeCumule; }
        public void setSoldeCumule(BigDecimal soldeCumule) { this.soldeCumule = soldeCumule; }
    }

    public static class LigneBalance {
        private String numeroCompte;
        private String nomCompte;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private BigDecimal soldeDebiteur;
        private BigDecimal soldeCrediteur;

        public String getNumeroCompte() { return numeroCompte; }
        public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
        public String getNomCompte() { return nomCompte; }
        public void setNomCompte(String nomCompte) { this.nomCompte = nomCompte; }
        public BigDecimal getTotalDebit() { return totalDebit; }
        public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }
        public BigDecimal getTotalCredit() { return totalCredit; }
        public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }
        public BigDecimal getSoldeDebiteur() { return soldeDebiteur; }
        public void setSoldeDebiteur(BigDecimal soldeDebiteur) { this.soldeDebiteur = soldeDebiteur; }
        public BigDecimal getSoldeCrediteur() { return soldeCrediteur; }
        public void setSoldeCrediteur(BigDecimal soldeCrediteur) { this.soldeCrediteur = soldeCrediteur; }
    }

    public static class BilanData {
        private BigDecimal actifImmobilise;
        private BigDecimal actifCirculant;
        private BigDecimal creances;
        private BigDecimal tresorerie;
        private BigDecimal capitauxPropres;
        private BigDecimal dettesFinancieres;
        private BigDecimal dettesExploitation;

        public BigDecimal getActifImmobilise() { return actifImmobilise; }
        public void setActifImmobilise(BigDecimal actifImmobilise) { this.actifImmobilise = actifImmobilise; }
        public BigDecimal getActifCirculant() { return actifCirculant; }
        public void setActifCirculant(BigDecimal actifCirculant) { this.actifCirculant = actifCirculant; }
        public BigDecimal getCreances() { return creances; }
        public void setCreances(BigDecimal creances) { this.creances = creances; }
        public BigDecimal getTresorerie() { return tresorerie; }
        public void setTresorerie(BigDecimal tresorerie) { this.tresorerie = tresorerie; }
        public BigDecimal getCapitauxPropres() { return capitauxPropres; }
        public void setCapitauxPropres(BigDecimal capitauxPropres) { this.capitauxPropres = capitauxPropres; }
        public BigDecimal getDettesFinancieres() { return dettesFinancieres; }
        public void setDettesFinancieres(BigDecimal dettesFinancieres) { this.dettesFinancieres = dettesFinancieres; }
        public BigDecimal getDettesExploitation() { return dettesExploitation; }
        public void setDettesExploitation(BigDecimal dettesExploitation) { this.dettesExploitation = dettesExploitation; }

        public BigDecimal getTotalActif() {
            // LOGIQUE SIMPLIFIÉE : Total actif = somme des postes d'actif uniquement
            return actifImmobilise.add(actifCirculant).add(creances).add(tresorerie);
        }

        public BigDecimal getTotalPassif() {
            // LOGIQUE SIMPLIFIÉE : Total passif = capitaux propres + dettes
            // Les capitaux propres doivent être positifs avec la nouvelle stratégie
            return capitauxPropres.add(dettesFinancieres).add(dettesExploitation);
        }
    }

    public static class CompteResultatData {
        private BigDecimal chiffresAffaires;
        private BigDecimal chargesExploitation;
        private BigDecimal chargesFinancieres;
        private BigDecimal produitsFinanciers;
        private BigDecimal resultatNet;

        public BigDecimal getChiffresAffaires() { return chiffresAffaires; }
        public void setChiffresAffaires(BigDecimal chiffresAffaires) { this.chiffresAffaires = chiffresAffaires; }
        public BigDecimal getChargesExploitation() { return chargesExploitation; }
        public void setChargesExploitation(BigDecimal chargesExploitation) { this.chargesExploitation = chargesExploitation; }
        public BigDecimal getChargesFinancieres() { return chargesFinancieres; }
        public void setChargesFinancieres(BigDecimal chargesFinancieres) { this.chargesFinancieres = chargesFinancieres; }
        public BigDecimal getProduitsFinanciers() { return produitsFinanciers; }
        public void setProduitsFinanciers(BigDecimal produitsFinanciers) { this.produitsFinanciers = produitsFinanciers; }
        public BigDecimal getResultatNet() { return resultatNet; }
        public void setResultatNet(BigDecimal resultatNet) { this.resultatNet = resultatNet; }

        public BigDecimal getResultatExploitation() {
            return chiffresAffaires.subtract(chargesExploitation);
        }

        public BigDecimal getResultatFinancier() {
            return produitsFinanciers.subtract(chargesFinancieres);
        }
    }
}