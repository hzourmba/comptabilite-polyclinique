package com.comptabilite.service;

import com.comptabilite.dao.FournisseurDAO;
import com.comptabilite.model.Fournisseur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FournisseurService {

    private static final Logger logger = LoggerFactory.getLogger(FournisseurService.class);
    private final FournisseurDAO fournisseurDAO;

    public FournisseurService() {
        this.fournisseurDAO = new FournisseurDAO();
    }

    public List<Fournisseur> getFournisseursByEntreprise(Long entrepriseId) {
        return fournisseurDAO.findByEntreprise(entrepriseId);
    }

    public Fournisseur creerFournisseur(Fournisseur fournisseur) {
        try {
            // Générer le code fournisseur si nécessaire
            if (fournisseur.getCodeFournisseur() == null || fournisseur.getCodeFournisseur().isEmpty()) {
                String codeGenere = fournisseurDAO.generateNextCodeFournisseur();
                fournisseur.setCodeFournisseur(codeGenere);
            }

            // Vérifier l'unicité du code
            if (fournisseurDAO.existsByCodeFournisseur(fournisseur.getCodeFournisseur())) {
                throw new RuntimeException("Un fournisseur avec ce code existe déjà: " + fournisseur.getCodeFournisseur());
            }

            // Assurer la cohérence des données
            if (fournisseur.getNom() != null && fournisseur.getRaisonSociale() == null) {
                fournisseur.setRaisonSociale(fournisseur.getNom());
            }

            Fournisseur savedFournisseur = fournisseurDAO.save(fournisseur);
            logger.info("Fournisseur créé avec succès: {}", savedFournisseur.getId());
            return savedFournisseur;

        } catch (Exception e) {
            logger.error("Erreur lors de la création du fournisseur", e);
            throw new RuntimeException("Erreur lors de la création du fournisseur: " + e.getMessage(), e);
        }
    }

    public Fournisseur modifierFournisseur(Fournisseur fournisseur) {
        try {
            // Vérifier que le fournisseur existe
            java.util.Optional<Fournisseur> existantOpt = fournisseurDAO.findById(fournisseur.getId());
            if (existantOpt.isEmpty()) {
                throw new RuntimeException("Fournisseur introuvable avec l'ID: " + fournisseur.getId());
            }
            Fournisseur existant = existantOpt.get();

            // Vérifier l'unicité du code si modifié
            if (!existant.getCodeFournisseur().equals(fournisseur.getCodeFournisseur())) {
                if (fournisseurDAO.existsByCodeFournisseur(fournisseur.getCodeFournisseur())) {
                    throw new RuntimeException("Un fournisseur avec ce code existe déjà: " + fournisseur.getCodeFournisseur());
                }
            }

            // Assurer la cohérence des données
            if (fournisseur.getNom() != null && fournisseur.getRaisonSociale() == null) {
                fournisseur.setRaisonSociale(fournisseur.getNom());
            }

            Fournisseur updatedFournisseur = fournisseurDAO.update(fournisseur);
            logger.info("Fournisseur modifié avec succès: {}", updatedFournisseur.getCodeFournisseur());
            return updatedFournisseur;

        } catch (Exception e) {
            logger.error("Erreur lors de la modification du fournisseur", e);
            throw new RuntimeException("Erreur lors de la modification du fournisseur: " + e.getMessage(), e);
        }
    }

    public void supprimerFournisseur(Fournisseur fournisseur) {
        try {
            // Vérifier qu'il n'y a pas de factures associées
            long nombreFactures = fournisseurDAO.countFacturesByFournisseur(fournisseur.getId());
            if (nombreFactures > 0) {
                throw new RuntimeException("Impossible de supprimer le fournisseur: " + nombreFactures + " facture(s) associée(s)");
            }

            fournisseurDAO.delete(fournisseur);
            logger.info("Fournisseur supprimé: {}", fournisseur.getCodeFournisseur());

        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du fournisseur", e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }

    public Fournisseur findByCodeFournisseur(String codeFournisseur) {
        return fournisseurDAO.findByCodeFournisseur(codeFournisseur);
    }

    public List<Fournisseur> searchFournisseurs(String terme) {
        return fournisseurDAO.searchByNomOrEmail(terme);
    }

    public List<Fournisseur> getFournisseursActifs(Long entrepriseId) {
        return fournisseurDAO.findByStatut(Fournisseur.StatutFournisseur.ACTIF, entrepriseId);
    }

    public List<Fournisseur> getFournisseursByType(Fournisseur.TypeFournisseur type, Long entrepriseId) {
        return fournisseurDAO.findByType(type, entrepriseId);
    }

    public long getNombreFournisseurs() {
        return fournisseurDAO.countFournisseurs();
    }

    public List<Fournisseur> getFournisseursAvecSoldePositif(Long entrepriseId) {
        return fournisseurDAO.findFournisseursWithSoldePositif(entrepriseId);
    }

    public List<Fournisseur> getFournisseursAvecSoldeNegatif(Long entrepriseId) {
        return fournisseurDAO.findFournisseursWithSoldeNegatif(entrepriseId);
    }

    public boolean codeExiste(String code) {
        return fournisseurDAO.existsByCodeFournisseur(code);
    }
}