package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compte")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, length = 20)
    private String numeroCompte;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false)
    private TypeCompte typeCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_compte", nullable = false)
    private ClasseCompte classeCompte;

    @Column(name = "solde_initial", precision = 15, scale = 2)
    private BigDecimal soldeInitial = BigDecimal.ZERO;

    @Column(name = "solde_debit", precision = 15, scale = 2)
    private BigDecimal soldeDebiteur = BigDecimal.ZERO;

    @Column(name = "solde_credit", precision = 15, scale = 2)
    private BigDecimal soldeCrediteur = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "accepte_sous_comptes", nullable = false)
    private Boolean accepteSousComptes = false;

    @Column(nullable = false)
    private Boolean lettrable = false;

    @Column(nullable = false)
    private Boolean auxiliaire = false;

    @Column(length = 500)
    private String description;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Compte compteParent;

    @OneToMany(mappedBy = "compteParent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Compte> sousComptes = new ArrayList<>();

    @OneToMany(mappedBy = "compte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneEcriture> lignesEcriture = new ArrayList<>();

    public enum TypeCompte {
        ACTIF,
        PASSIF,
        ACTIF_PASSIF,
        CHARGE,
        PRODUIT
    }

    public enum ClasseCompte {
        CLASSE_1("1", "Comptes de capitaux"),
        CLASSE_2("2", "Comptes d'immobilisations"),
        CLASSE_3("3", "Comptes de stocks et en-cours"),
        CLASSE_4("4", "Comptes de tiers"),
        CLASSE_5("5", "Comptes financiers"),
        CLASSE_6("6", "Comptes de charges"),
        CLASSE_7("7", "Comptes de produits"),
        CLASSE_8("8", "Comptes spéciaux");

        private final String numero;
        private final String libelle;

        ClasseCompte(String numero, String libelle) {
            this.numero = numero;
            this.libelle = libelle;
        }

        public String getNumero() { return numero; }
        public String getLibelle() { return libelle; }

        @Override
        public String toString() {
            return "Classe " + numero + " - " + libelle;
        }
    }

    // Constructeurs
    public Compte() {}

    public Compte(String numeroCompte, String libelle, TypeCompte typeCompte, ClasseCompte classeCompte, Entreprise entreprise) {
        this.numeroCompte = numeroCompte;
        this.libelle = libelle;
        this.typeCompte = typeCompte;
        this.classeCompte = classeCompte;
        this.entreprise = entreprise;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }

    // Alias pour compatibility
    public String getNumero() { return numeroCompte; }
    public void setNumero(String numero) { this.numeroCompte = numero; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public TypeCompte getTypeCompte() { return typeCompte; }
    public void setTypeCompte(TypeCompte typeCompte) { this.typeCompte = typeCompte; }

    public ClasseCompte getClasseCompte() { return classeCompte; }
    public void setClasseCompte(ClasseCompte classeCompte) { this.classeCompte = classeCompte; }

    public BigDecimal getSoldeInitial() { return soldeInitial; }
    public void setSoldeInitial(BigDecimal soldeInitial) { this.soldeInitial = soldeInitial; }

    public BigDecimal getSoldeDebiteur() { return soldeDebiteur; }
    public void setSoldeDebiteur(BigDecimal soldeDebiteur) { this.soldeDebiteur = soldeDebiteur; }

    public BigDecimal getSoldeCrediteur() { return soldeCrediteur; }
    public void setSoldeCrediteur(BigDecimal soldeCrediteur) { this.soldeCrediteur = soldeCrediteur; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public boolean isActif() { return Boolean.TRUE.equals(actif); }

    public Boolean getAccepteSousComptes() { return accepteSousComptes; }
    public void setAccepteSousComptes(Boolean accepteSousComptes) { this.accepteSousComptes = accepteSousComptes; }

    public boolean isAccepteSousComptes() { return Boolean.TRUE.equals(accepteSousComptes); }

    public Boolean getLettrable() { return lettrable; }
    public void setLettrable(Boolean lettrable) { this.lettrable = lettrable; }

    public boolean isLettrable() { return Boolean.TRUE.equals(lettrable); }

    public Boolean getAuxiliaire() { return auxiliaire; }
    public void setAuxiliaire(Boolean auxiliaire) { this.auxiliaire = auxiliaire; }

    public boolean isAuxiliaire() { return Boolean.TRUE.equals(auxiliaire); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public Compte getCompteParent() { return compteParent; }
    public void setCompteParent(Compte compteParent) { this.compteParent = compteParent; }

    public List<Compte> getSousComptes() { return sousComptes; }
    public void setSousComptes(List<Compte> sousComptes) { this.sousComptes = sousComptes; }

    public List<LigneEcriture> getLignesEcriture() { return lignesEcriture; }
    public void setLignesEcriture(List<LigneEcriture> lignesEcriture) { this.lignesEcriture = lignesEcriture; }

    public BigDecimal getSoldeNet() {
        return soldeDebiteur.subtract(soldeCrediteur);
    }

    public String getNumeroEtLibelle() {
        return numeroCompte + " - " + libelle;
    }

    public void crediter(BigDecimal montant) {
        this.soldeCrediteur = this.soldeCrediteur.add(montant);
    }

    public void debiter(BigDecimal montant) {
        this.soldeDebiteur = this.soldeDebiteur.add(montant);
    }

    /**
     * Calcule le solde débiteur consolidé incluant tous les sous-comptes
     */
    public BigDecimal getSoldeDebiteurConsolide() {
        // Si ce compte a des sous-comptes, utiliser uniquement la somme des enfants
        if (sousComptes != null && !sousComptes.isEmpty()) {
            BigDecimal soldeTotal = BigDecimal.ZERO;
            for (Compte sousCompte : sousComptes) {
                soldeTotal = soldeTotal.add(sousCompte.getSoldeDebiteurConsolide());
            }
            return soldeTotal;
        }

        // Sinon, utiliser le solde direct de ce compte
        return this.soldeDebiteur;
    }

    /**
     * Calcule le solde créditeur consolidé incluant tous les sous-comptes
     */
    public BigDecimal getSoldeCrediteurConsolide() {
        // Si ce compte a des sous-comptes, utiliser uniquement la somme des enfants
        if (sousComptes != null && !sousComptes.isEmpty()) {
            BigDecimal soldeTotal = BigDecimal.ZERO;
            for (Compte sousCompte : sousComptes) {
                soldeTotal = soldeTotal.add(sousCompte.getSoldeCrediteurConsolide());
            }
            return soldeTotal;
        }

        // Sinon, utiliser le solde direct de ce compte
        return this.soldeCrediteur;
    }

    /**
     * Calcule le solde net consolidé incluant tous les sous-comptes
     */
    public BigDecimal getSoldeNetConsolide() {
        return getSoldeDebiteurConsolide().subtract(getSoldeCrediteurConsolide());
    }

    /**
     * Met à jour les soldes du compte parent après modification d'un sous-compte
     */
    public void mettreAJourSoldesParent() {
        if (compteParent != null) {
            // Recalculer et sauvegarder les soldes consolidés du parent
            compteParent.calculerEtSauvegarderSoldesConsolides();
            // Propagation récursive vers le grand-parent
            compteParent.mettreAJourSoldesParent();
        }
    }

    /**
     * Calcule les soldes consolidés et les sauvegarde dans les champs du compte parent
     * UNIQUEMENT pour les comptes qui n'ont pas de soldes propres et sont de vrais regroupements
     */
    public void calculerEtSauvegarderSoldesConsolides() {
        // Ne pas utiliser cette méthode - elle cause des problèmes de double comptabilisation
        // Les soldes consolidés sont calculés dynamiquement par getSoldeDebiteurConsolide()
    }

    @Override
    public String toString() {
        return numeroCompte + " - " + libelle;
    }
}