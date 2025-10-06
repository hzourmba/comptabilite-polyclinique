package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lignes_facture")
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantite = BigDecimal.ONE;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantHT = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    // Constructeurs
    public LigneFacture() {}

    public LigneFacture(String designation, BigDecimal quantite, BigDecimal prixUnitaire) {
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantHT = quantite.multiply(prixUnitaire);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getQuantite() { return quantite; }
    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
        calculerMontantHT();
    }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        calculerMontantHT();
    }

    public BigDecimal getMontantHT() { return montantHT; }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT = montantHT; }

    public Facture getFacture() { return facture; }
    public void setFacture(Facture facture) { this.facture = facture; }

    private void calculerMontantHT() {
        this.montantHT = quantite.multiply(prixUnitaire);
    }

    @Override
    public String toString() {
        return "LigneFacture{" +
                "id=" + id +
                ", designation='" + designation + '\'' +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", montantHT=" + montantHT +
                '}';
    }
}