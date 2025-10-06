package com.comptabilite.service;

import com.comptabilite.model.Entreprise;
import com.comptabilite.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyServiceTest {

    private CurrencyService currencyService;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        currencyService = CurrencyService.getInstance();
        authService = AuthenticationService.getInstance();
    }

    @Test
    void testFrenchCurrency() {
        // Simuler une entreprise française
        Entreprise entrepriseFrance = new Entreprise();
        entrepriseFrance.setPays("France");

        Utilisateur userFrance = new Utilisateur();
        userFrance.setEntreprise(entrepriseFrance);

        authService.setUtilisateurConnecte(userFrance);

        // Tester formatage français (Euro)
        assertFalse(currencyService.isOHADAEntreprise());
        assertEquals("€", currencyService.getCurrencySymbol());
        assertEquals("EUR", currencyService.getCurrencyCode());
        assertEquals("1234,56 €", currencyService.formatAmount(new BigDecimal("1234.56")));
        assertEquals(20.0, currencyService.getDefaultVATRate());
    }

    @Test
    void testCameroonCurrency() {
        // Simuler une entreprise camerounaise
        Entreprise entrepriseCameroun = new Entreprise();
        entrepriseCameroun.setPays("Cameroun");

        Utilisateur userCameroun = new Utilisateur();
        userCameroun.setEntreprise(entrepriseCameroun);

        authService.setUtilisateurConnecte(userCameroun);

        // Tester formatage camerounais (FCFA)
        assertTrue(currencyService.isOHADAEntreprise());
        assertEquals("FCFA", currencyService.getCurrencySymbol());
        assertEquals("XAF", currencyService.getCurrencyCode());
        assertEquals("1 235 FCFA", currencyService.formatAmount(new BigDecimal("1234.56")));
        assertEquals(19.25, currencyService.getDefaultVATRate());
    }

    @Test
    void testAmountParsing() {
        // Test parsing français
        Entreprise entrepriseFrance = new Entreprise();
        entrepriseFrance.setPays("France");

        Utilisateur userFrance = new Utilisateur();
        userFrance.setEntreprise(entrepriseFrance);

        authService.setUtilisateurConnecte(userFrance);

        assertEquals(new BigDecimal("1234.56"), currencyService.parseAmount("1234,56"));
        assertEquals(new BigDecimal("1000"), currencyService.parseAmount("1000 €"));

        // Test parsing camerounais
        Entreprise entrepriseCameroun = new Entreprise();
        entrepriseCameroun.setPays("Cameroun");

        Utilisateur userCameroun = new Utilisateur();
        userCameroun.setEntreprise(entrepriseCameroun);

        authService.setUtilisateurConnecte(userCameroun);

        assertEquals(new BigDecimal("1234"), currencyService.parseAmount("1234"));
        assertEquals(new BigDecimal("1000"), currencyService.parseAmount("1000 FCFA"));
    }
}