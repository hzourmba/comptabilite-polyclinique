package com.comptabilite.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fournisseurs")
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codeFournisseur;

    @Column(name = "raisonSociale", nullable = true, length = 200)
    private String raisonSociale;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 20)
    private String siret;

    @Column(name = "numeroTVA", length = 30)
    private String numeroTVA;

    @Column(length = 200)
    private String adresse;

    @Column(name = "codePostal", length = 10)
    private String codePostal;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String pays = "France";

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(name = "personneContact", length = 100)
    private String personneContact;

    @Column(length = 50)
    private String numeroCompteFournisseur;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeFournisseur", nullable = false)
    private TypeFournisseur typeFournisseur = TypeFournisseur.ENTREPRISE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutFournisseur", nullable = false)
    private StatutFournisseur statutFournisseur = StatutFournisseur.ACTIF;

    @Column(name = "soldeFournisseur", precision = 15, scale = 2)
    private BigDecimal soldeFournisseur = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;


    public enum TypeFournisseur {
        PARTICULIER,
        ENTREPRISE,
        ASSOCIATION,
        ADMINISTRATION,
        FREELANCE
    }

    public enum StatutFournisseur {
        ACTIF,
        INACTIF,
        SUSPENDU
    }

    // Constructeurs
    public Fournisseur() {}

    public Fournisseur(String codeFournisseur, String nom, String email, Entreprise entreprise) {
        this.codeFournisseur = codeFournisseur;
        this.nom = nom;
        this.raisonSociale = nom;
        this.email = email;
        this.entreprise = entreprise;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeFournisseur() { return codeFournisseur; }
    public void setCodeFournisseur(String codeFournisseur) { this.codeFournisseur = codeFournisseur; }

    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }

    public String getNumeroTVA() { return numeroTVA; }
    public void setNumeroTVA(String numeroTVA) { this.numeroTVA = numeroTVA; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPersonneContact() { return personneContact; }
    public void setPersonneContact(String personneContact) { this.personneContact = personneContact; }

    public String getNumeroCompteFournisseur() { return numeroCompteFournisseur; }
    public void setNumeroCompteFournisseur(String numeroCompteFournisseur) { this.numeroCompteFournisseur = numeroCompteFournisseur; }

    public TypeFournisseur getTypeFournisseur() { return typeFournisseur; }
    public void setTypeFournisseur(TypeFournisseur typeFournisseur) { this.typeFournisseur = typeFournisseur; }

    public StatutFournisseur getStatutFournisseur() { return statutFournisseur; }
    public void setStatutFournisseur(StatutFournisseur statutFournisseur) { this.statutFournisseur = statutFournisseur; }

    public BigDecimal getSoldeFournisseur() { return soldeFournisseur; }
    public void setSoldeFournisseur(BigDecimal soldeFournisseur) { this.soldeFournisseur = soldeFournisseur; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }


    @PrePersist
    @PreUpdate
    private void beforeSave() {
        // S'assurer que raisonSociale a une valeur pour éviter l'erreur MySQL
        if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
            if (typeFournisseur == TypeFournisseur.PARTICULIER) {
                raisonSociale = getNomComplet();
            } else {
                raisonSociale = nom;
            }
        }
    }

    public String getNomComplet() {
        if (typeFournisseur == TypeFournisseur.PARTICULIER) {
            StringBuilder nomComplet = new StringBuilder();
            if (prenom != null && !prenom.trim().isEmpty()) {
                nomComplet.append(prenom).append(" ");
            }
            if (nom != null) {
                nomComplet.append(nom);
            }
            return nomComplet.toString().trim();
        } else {
            return nom != null ? nom : "";
        }
    }

    public String getAdresseComplete() {
        StringBuilder adresseComplete = new StringBuilder();
        if (adresse != null) adresseComplete.append(adresse);
        if (codePostal != null) adresseComplete.append(", ").append(codePostal);
        if (ville != null) adresseComplete.append(" ").append(ville);
        if (pays != null) adresseComplete.append(", ").append(pays);
        return adresseComplete.toString();
    }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "id=" + id +
                ", codeFournisseur='" + codeFournisseur + '\'' +
                ", nom='" + nom + '\'' +
                ", typeFournisseur=" + typeFournisseur +
                ", statutFournisseur=" + statutFournisseur +
                '}';
    }
}