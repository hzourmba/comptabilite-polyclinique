package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numeroFacture;

    @Column(nullable = false)
    private LocalDate dateFacture;

    @Column
    private LocalDate dateEcheance;

    @Column(length = 500)
    private String objet;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantHT = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantTVA = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montantTTC = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxTVA = new BigDecimal("20.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeFacture typeFacture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.BROUILLON;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(columnDefinition = "TEXT")
    private String commentaires;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    public enum TypeFacture {
        VENTE,
        ACHAT,
        AVOIR_VENTE,
        AVOIR_ACHAT
    }

    public enum StatutFacture {
        BROUILLON,
        ENVOYEE,
        PAYEE,
        ANNULEE,
        EN_RETARD
    }

    // Constructeurs
    public Facture() {}

    public Facture(String numeroFacture, LocalDate dateFacture, TypeFacture typeFacture, Client client, Entreprise entreprise) {
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.typeFacture = typeFacture;
        this.client = client;
        this.entreprise = entreprise;
    }

    public Facture(String numeroFacture, LocalDate dateFacture, TypeFacture typeFacture, Fournisseur fournisseur, Entreprise entreprise) {
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.typeFacture = typeFacture;
        this.fournisseur = fournisseur;
        this.entreprise = entreprise;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFacture() { return numeroFacture; }
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }

    public LocalDate getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDate dateFacture) { this.dateFacture = dateFacture; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }

    public BigDecimal getMontantHT() { return montantHT; }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT = montantHT; }

    public BigDecimal getMontantTVA() { return montantTVA; }
    public void setMontantTVA(BigDecimal montantTVA) { this.montantTVA = montantTVA; }

    public BigDecimal getMontantTTC() { return montantTTC; }
    public void setMontantTTC(BigDecimal montantTTC) { this.montantTTC = montantTTC; }

    public BigDecimal getTauxTVA() { return tauxTVA; }
    public void setTauxTVA(BigDecimal tauxTVA) { this.tauxTVA = tauxTVA; }

    public TypeFacture getTypeFacture() { return typeFacture; }
    public void setTypeFacture(TypeFacture typeFacture) { this.typeFacture = typeFacture; }

    public StatutFacture getStatut() { return statut; }
    public void setStatut(StatutFacture statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public List<LigneFacture> getLignes() { return lignes; }
    public void setLignes(List<LigneFacture> lignes) { this.lignes = lignes; }

    public void ajouterLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
        calculerMontants();
    }

    public void supprimerLigne(LigneFacture ligne) {
        lignes.remove(ligne);
        ligne.setFacture(null);
        calculerMontants();
    }

    public void calculerMontants() {
        montantHT = lignes.stream()
                .map(LigneFacture::getMontantHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        montantTVA = montantHT.multiply(tauxTVA.divide(new BigDecimal("100")));
        montantTTC = montantHT.add(montantTVA);
    }

    public void envoyer() {
        this.statut = StatutFacture.ENVOYEE;
        this.dateEnvoi = LocalDateTime.now();
    }

    public void marquerPayee() {
        this.statut = StatutFacture.PAYEE;
        this.datePaiement = LocalDate.now();
    }

    public boolean isEnRetard() {
        return dateEcheance != null &&
               LocalDate.now().isAfter(dateEcheance) &&
               statut != StatutFacture.PAYEE &&
               statut != StatutFacture.ANNULEE;
    }

    public String getNomPartenaire() {
        if (typeFacture == TypeFacture.VENTE || typeFacture == TypeFacture.AVOIR_VENTE) {
            return client != null ? client.getRaisonSociale() : "Client non défini";
        } else {
            return fournisseur != null ? fournisseur.getRaisonSociale() : "Fournisseur non défini";
        }
    }

    public boolean isFactureAchat() {
        return typeFacture == TypeFacture.ACHAT || typeFacture == TypeFacture.AVOIR_ACHAT;
    }

    public boolean isFactureVente() {
        return typeFacture == TypeFacture.VENTE || typeFacture == TypeFacture.AVOIR_VENTE;
    }

    @Override
    public String toString() {
        return "Facture{" +
                "id=" + id +
                ", numeroFacture='" + numeroFacture + '\'' +
                ", dateFacture=" + dateFacture +
                ", montantTTC=" + montantTTC +
                ", statut=" + statut +
                '}';
    }
}