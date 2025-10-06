package com.comptabilite.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercices")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutExercice statut = StatutExercice.OUVERT;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @OneToMany(mappedBy = "exercice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EcritureComptable> ecritures = new ArrayList<>();

    public enum StatutExercice {
        OUVERT,
        CLOTURE,
        ARCHIVE
    }

    // Constructeurs
    public Exercice() {}

    public Exercice(String libelle, LocalDate dateDebut, LocalDate dateFin, Entreprise entreprise) {
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.entreprise = entreprise;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public StatutExercice getStatut() { return statut; }
    public void setStatut(StatutExercice statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public List<EcritureComptable> getEcritures() { return ecritures; }
    public void setEcritures(List<EcritureComptable> ecritures) { this.ecritures = ecritures; }

    public boolean isExerciceEnCours() {
        LocalDate today = LocalDate.now();
        return statut == StatutExercice.OUVERT &&
               !today.isBefore(dateDebut) &&
               !today.isAfter(dateFin);
    }

    public void cloturerExercice() {
        this.statut = StatutExercice.CLOTURE;
        this.dateCloture = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return libelle + " (" + dateDebut + " - " + dateFin + ")";
    }
}