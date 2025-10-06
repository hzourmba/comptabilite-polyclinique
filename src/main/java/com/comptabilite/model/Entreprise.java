package com.comptabilite.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entreprises")
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String raisonSociale;

    @Column(unique = true, length = 20)
    private String siret;

    @Column(length = 20)
    private String siren;

    @Column(length = 30)
    private String numeroTVA;

    @Column(length = 200)
    private String adresse;

    @Column(length = 10)
    private String codePostal;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String pays = "France";

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(length = 100)
    private String siteWeb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormeJuridique formeJuridique;

    @Column(nullable = false)
    private Double capitalSocial = 0.0;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Exercice> exercices = new ArrayList<>();

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Compte> comptes = new ArrayList<>();

    public enum FormeJuridique {
        SARL,
        SAS,
        SA,
        EURL,
        SNC,
        EIRL,
        MICRO_ENTREPRISE,
        ASSOCIATION,
        AUTRE
    }

    // Constructeurs
    public Entreprise() {}

    public Entreprise(String raisonSociale, String siret, FormeJuridique formeJuridique) {
        this.raisonSociale = raisonSociale;
        this.siret = siret;
        this.formeJuridique = formeJuridique;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }

    public String getSiren() { return siren; }
    public void setSiren(String siren) { this.siren = siren; }

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

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public FormeJuridique getFormeJuridique() { return formeJuridique; }
    public void setFormeJuridique(FormeJuridique formeJuridique) { this.formeJuridique = formeJuridique; }

    public Double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(Double capitalSocial) { this.capitalSocial = capitalSocial; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<Utilisateur> getUtilisateurs() { return utilisateurs; }
    public void setUtilisateurs(List<Utilisateur> utilisateurs) { this.utilisateurs = utilisateurs; }

    public List<Exercice> getExercices() { return exercices; }
    public void setExercices(List<Exercice> exercices) { this.exercices = exercices; }

    public List<Compte> getComptes() { return comptes; }
    public void setComptes(List<Compte> comptes) { this.comptes = comptes; }

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
        return "Entreprise{" +
                "id=" + id +
                ", raisonSociale='" + raisonSociale + '\'' +
                ", siret='" + siret + '\'' +
                ", formeJuridique=" + formeJuridique +
                '}';
    }
}