package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ecritures_comptables")
public class EcritureComptable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroEcriture;

    @Column(nullable = false)
    private LocalDate dateEcriture;

    @Column(length = 500)
    private String libelle;

    @Column(length = 50)
    private String numeroJournal;

    @Column(length = 50)
    private String referencePiece;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEcriture statut = StatutEcriture.BROUILLON;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @OneToMany(mappedBy = "ecritureComptable", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<LigneEcriture> lignes = new ArrayList<>();

    public enum StatutEcriture {
        BROUILLON,
        VALIDEE,
        CLOTUREE
    }

    // Constructeurs
    public EcritureComptable() {}

    public EcritureComptable(String numeroEcriture, LocalDate dateEcriture, String libelle, Exercice exercice, Utilisateur utilisateur) {
        this.numeroEcriture = numeroEcriture;
        this.dateEcriture = dateEcriture;
        this.libelle = libelle;
        this.exercice = exercice;
        this.utilisateur = utilisateur;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroEcriture() { return numeroEcriture; }
    public void setNumeroEcriture(String numeroEcriture) { this.numeroEcriture = numeroEcriture; }

    public LocalDate getDateEcriture() { return dateEcriture; }
    public void setDateEcriture(LocalDate dateEcriture) { this.dateEcriture = dateEcriture; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getNumeroJournal() { return numeroJournal; }
    public void setNumeroJournal(String numeroJournal) { this.numeroJournal = numeroJournal; }

    public String getReferencePiece() { return referencePiece; }
    public void setReferencePiece(String referencePiece) { this.referencePiece = referencePiece; }

    public StatutEcriture getStatut() { return statut; }
    public void setStatut(StatutEcriture statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public List<LigneEcriture> getLignes() { return lignes; }
    public void setLignes(List<LigneEcriture> lignes) { this.lignes = lignes; }

    // Alias pour compatibilité
    public List<LigneEcriture> getLignesEcriture() { return lignes; }
    public void setLignesEcriture(List<LigneEcriture> lignes) { this.lignes = lignes; }

    // Méthode pour la référence (alias pour referencePiece)
    public String getReference() { return referencePiece; }
    public void setReference(String reference) { this.referencePiece = reference; }

    public void ajouterLigne(LigneEcriture ligne) {
        lignes.add(ligne);
        ligne.setEcritureComptable(this);
    }

    public void supprimerLigne(LigneEcriture ligne) {
        lignes.remove(ligne);
        ligne.setEcritureComptable(null);
    }

    public BigDecimal getTotalDebit() {
        return lignes.stream()
                .map(LigneEcriture::getMontantDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCredit() {
        return lignes.stream()
                .map(LigneEcriture::getMontantCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEquilibree() {
        return getTotalDebit().compareTo(getTotalCredit()) == 0;
    }

    public BigDecimal getMontantTotal() {
        return getTotalDebit();
    }

    public void valider() {
        if (!isEquilibree()) {
            throw new IllegalStateException("L'écriture n'est pas équilibrée");
        }
        this.statut = StatutEcriture.VALIDEE;
        this.dateValidation = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "EcritureComptable{" +
                "id=" + id +
                ", numeroEcriture='" + numeroEcriture + '\'' +
                ", dateEcriture=" + dateEcriture +
                ", libelle='" + libelle + '\'' +
                ", statut=" + statut +
                '}';
    }
}