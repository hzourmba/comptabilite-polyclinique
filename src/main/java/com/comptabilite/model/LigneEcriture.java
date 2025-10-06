package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lignes_ecriture")
public class LigneEcriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantDebit = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantCredit = BigDecimal.ZERO;

    @Column(length = 500)
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecritureComptable_id", nullable = false)
    private EcritureComptable ecritureComptable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    // Constructeurs
    public LigneEcriture() {}

    public LigneEcriture(BigDecimal montantDebit, BigDecimal montantCredit, String libelle, Compte compte) {
        this.montantDebit = montantDebit != null ? montantDebit : BigDecimal.ZERO;
        this.montantCredit = montantCredit != null ? montantCredit : BigDecimal.ZERO;
        this.libelle = libelle;
        this.compte = compte;
    }

    public static LigneEcriture creerDebit(BigDecimal montant, String libelle, Compte compte) {
        return new LigneEcriture(montant, BigDecimal.ZERO, libelle, compte);
    }

    public static LigneEcriture creerCredit(BigDecimal montant, String libelle, Compte compte) {
        return new LigneEcriture(BigDecimal.ZERO, montant, libelle, compte);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontantDebit() { return montantDebit; }
    public void setMontantDebit(BigDecimal montantDebit) {
        this.montantDebit = montantDebit != null ? montantDebit : BigDecimal.ZERO;
    }

    public BigDecimal getMontantCredit() { return montantCredit; }
    public void setMontantCredit(BigDecimal montantCredit) {
        this.montantCredit = montantCredit != null ? montantCredit : BigDecimal.ZERO;
    }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public EcritureComptable getEcritureComptable() { return ecritureComptable; }
    public void setEcritureComptable(EcritureComptable ecritureComptable) { this.ecritureComptable = ecritureComptable; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public BigDecimal getMontantNet() {
        return montantDebit.subtract(montantCredit);
    }

    public boolean isDebit() {
        return montantDebit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCredit() {
        return montantCredit.compareTo(BigDecimal.ZERO) > 0;
    }

    @PrePersist
    @PreUpdate
    private void validateAmounts() {
        if (montantDebit.compareTo(BigDecimal.ZERO) > 0 && montantCredit.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Une ligne ne peut avoir à la fois un montant débit et crédit");
        }
        if (montantDebit.compareTo(BigDecimal.ZERO) == 0 && montantCredit.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Une ligne doit avoir au moins un montant débit ou crédit");
        }
    }

    @Override
    public String toString() {
        return "LigneEcriture{" +
                "id=" + id +
                ", montantDebit=" + montantDebit +
                ", montantCredit=" + montantCredit +
                ", libelle='" + libelle + '\'' +
                ", compte=" + (compte != null ? compte.getNumeroCompte() : "null") +
                '}';
    }
}