package com.comptabilite.service;

import com.comptabilite.model.Utilisateur;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Service de formatage de la monnaie selon l'entreprise (France = Euro, Cameroun = FCFA)
 */
public class CurrencyService {

    private static CurrencyService instance;
    private final AuthenticationService authService;

    private CurrencyService() {
        this.authService = AuthenticationService.getInstance();
    }

    public static CurrencyService getInstance() {
        if (instance == null) {
            instance = new CurrencyService();
        }
        return instance;
    }

    /**
     * Détermine si l'utilisateur connecté est dans une entreprise camerounaise (OHADA)
     */
    public boolean isOHADAEntreprise() {
        if (!authService.isUserLoggedIn()) {
            return false;
        }

        Utilisateur utilisateur = authService.getUtilisateurConnecte();
        return utilisateur != null &&
               utilisateur.getEntreprise() != null &&
               "Cameroun".equals(utilisateur.getEntreprise().getPays());
    }

    /**
     * Retourne le symbole de la monnaie selon l'entreprise
     */
    public String getCurrencySymbol() {
        return isOHADAEntreprise() ? "FCFA" : "€";
    }

    /**
     * Retourne le code de la monnaie selon l'entreprise
     */
    public String getCurrencyCode() {
        return isOHADAEntreprise() ? "XAF" : "EUR";
    }

    /**
     * Formate un montant avec la monnaie appropriée
     */
    public String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return isOHADAEntreprise() ? "0 FCFA" : "0,00 €";
        }

        if (isOHADAEntreprise()) {
            // FCFA : pas de décimales, espace avant le symbole
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
            symbols.setGroupingSeparator(' ');
            DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
            return formatter.format(amount.doubleValue()) + " FCFA";
        } else {
            // Euro : 2 décimales, virgule comme séparateur décimal
            return String.format("%.2f €", amount.doubleValue());
        }
    }

    /**
     * Formate un montant double avec la monnaie appropriée
     */
    public String formatAmount(double amount) {
        return formatAmount(BigDecimal.valueOf(amount));
    }

    /**
     * Formate un montant avec le format d'affichage dans les tableaux
     */
    public String formatAmountForTable(BigDecimal amount) {
        if (amount == null) {
            return isOHADAEntreprise() ? "0 FCFA" : "0,00 €";
        }

        if (isOHADAEntreprise()) {
            // Pour les tables, format plus compact pour FCFA
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
            symbols.setGroupingSeparator(' ');
            DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
            return formatter.format(amount.doubleValue()) + " FCFA";
        } else {
            return String.format("%.2f €", amount.doubleValue());
        }
    }

    /**
     * Retourne le taux de TVA selon le pays
     */
    public double getDefaultVATRate() {
        return isOHADAEntreprise() ? 19.25 : 20.0; // 19.25% au Cameroun, 20% en France
    }

    /**
     * Retourne le format de saisie pour les montants selon le pays
     */
    public String getAmountInputFormat() {
        return isOHADAEntreprise() ?
            "Format: 1234567 (sans décimales)" :
            "Format: 1234,56 (avec décimales)";
    }

    /**
     * Parse un montant saisi selon le format du pays
     */
    public BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String cleanAmount = amountStr.trim()
            .replace(" FCFA", "")
            .replace("€", "")
            .replace(" ", "");

        try {
            if (isOHADAEntreprise()) {
                // FCFA : pas de décimales attendues
                return new BigDecimal(cleanAmount.replace(",", ""));
            } else {
                // Euro : remplacer virgule par point pour le parsing
                return new BigDecimal(cleanAmount.replace(",", "."));
            }
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}