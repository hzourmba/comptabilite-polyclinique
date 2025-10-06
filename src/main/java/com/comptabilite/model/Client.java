package com.comptabilite.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codeClient;

    @Column(name = "raisonSociale", nullable = false, length = 200)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "typeClient", nullable = false)
    private TypeClient typeClient = TypeClient.ENTREPRISE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutClient", nullable = false)
    private StatutClient statutClient = StatutClient.ACTIF;

    @Column(name = "soldeClient", precision = 15, scale = 2)
    private java.math.BigDecimal soldeClient = java.math.BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Facture> factures = new ArrayList<>();

    public enum TypeClient {
        PARTICULIER,
        ENTREPRISE,
        ASSOCIATION,
        ADMINISTRATION
    }

    public enum StatutClient {
        ACTIF,
        INACTIF,
        SUSPENDU
    }

    // Constructeurs
    public Client() {}

    public Client(String codeClient, String nom, String email, Entreprise entreprise) {
        this.codeClient = codeClient;
        this.nom = nom;
        this.email = email;
        this.entreprise = entreprise;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeClient() { return codeClient; }
    public void setCodeClient(String codeClient) { this.codeClient = codeClient; }

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

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }

    public StatutClient getStatutClient() { return statutClient; }
    public void setStatutClient(StatutClient statutClient) { this.statutClient = statutClient; }

    public java.math.BigDecimal getSoldeClient() { return soldeClient; }
    public void setSoldeClient(java.math.BigDecimal soldeClient) { this.soldeClient = soldeClient; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public List<Facture> getFactures() { return factures; }
    public void setFactures(List<Facture> factures) { this.factures = factures; }

    public String getNomComplet() {
        if (typeClient == TypeClient.PARTICULIER) {
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
        return "Client{" +
                "id=" + id +
                ", codeClient='" + codeClient + '\'' +
                ", nom='" + nom + '\'' +
                ", typeClient=" + typeClient +
                ", statutClient=" + statutClient +
                '}';
    }
}