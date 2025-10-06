package com.comptabilite.view;

import com.comptabilite.dao.CompteDAO;
import com.comptabilite.dao.ExerciceDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.model.Exercice;
import com.comptabilite.service.ReportService;
import com.comptabilite.service.ReportService.*;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.CurrencyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class RapportsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RapportsController.class);

    @FXML private TabPane tabPaneRapports;

    @FXML private ComboBox<Exercice> cbExerciceBalance;
    @FXML private TableView<LigneBalance> tableBalance;
    @FXML private TableColumn<LigneBalance, String> colNumeroCompte;
    @FXML private TableColumn<LigneBalance, String> colNomCompte;
    @FXML private TableColumn<LigneBalance, BigDecimal> colTotalDebit;
    @FXML private TableColumn<LigneBalance, BigDecimal> colTotalCredit;
    @FXML private TableColumn<LigneBalance, BigDecimal> colSoldeDebiteur;
    @FXML private TableColumn<LigneBalance, BigDecimal> colSoldeCrediteur;

    @FXML private ComboBox<Exercice> cbExerciceGrandLivre;
    @FXML private ComboBox<Compte> cbCompteGrandLivre;
    @FXML private DatePicker dpDateDebutGL;
    @FXML private DatePicker dpDateFinGL;
    @FXML private TableView<LigneGrandLivre> tableGrandLivre;
    @FXML private TableColumn<LigneGrandLivre, LocalDate> colDateGL;
    @FXML private TableColumn<LigneGrandLivre, String> colLibelleGL;
    @FXML private TableColumn<LigneGrandLivre, String> colJournalGL;
    @FXML private TableColumn<LigneGrandLivre, BigDecimal> colDebitGL;
    @FXML private TableColumn<LigneGrandLivre, BigDecimal> colCreditGL;
    @FXML private TableColumn<LigneGrandLivre, BigDecimal> colSoldeCumuleGL;

    @FXML private ComboBox<Exercice> cbExerciceBilan;
    @FXML private Label lblActifImmobilise;
    @FXML private Label lblActifCirculant;
    @FXML private Label lblCreances;
    @FXML private Label lblTresorerie;
    @FXML private Label lblTotalActif;
    @FXML private Label lblCapitauxPropres;
    @FXML private Label lblDettesFinancieres;
    @FXML private Label lblDettesExploitation;
    @FXML private Label lblTotalPassif;

    @FXML private ComboBox<Exercice> cbExerciceResultat;
    @FXML private Label lblChiffresAffaires;
    @FXML private Label lblChargesExploitation;
    @FXML private Label lblResultatExploitation;
    @FXML private Label lblChargesFinancieres;
    @FXML private Label lblProduitsFinanciers;
    @FXML private Label lblResultatFinancier;
    @FXML private Label lblResultatNet;

    private ReportService reportService;
    private ExerciceDAO exerciceDAO;
    private CompteDAO compteDAO;
    private AuthenticationService authService;
    private CurrencyService currencyService;

    private ObservableList<LigneBalance> balanceData;
    private ObservableList<LigneGrandLivre> grandLivreData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportService = new ReportService();
        exerciceDAO = new ExerciceDAO();
        compteDAO = new CompteDAO();
        authService = AuthenticationService.getInstance();
        currencyService = CurrencyService.getInstance();

        balanceData = FXCollections.observableArrayList();
        grandLivreData = FXCollections.observableArrayList();

        initializeComponents();
        loadData();
    }

    private void initializeComponents() {
        initializeBalanceTable();
        initializeGrandLivreTable();
        setupEventHandlers();
    }

    private void initializeBalanceTable() {
        colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        colNomCompte.setCellValueFactory(new PropertyValueFactory<>("nomCompte"));
        colTotalDebit.setCellValueFactory(new PropertyValueFactory<>("totalDebit"));
        colTotalCredit.setCellValueFactory(new PropertyValueFactory<>("totalCredit"));
        colSoldeDebiteur.setCellValueFactory(new PropertyValueFactory<>("soldeDebiteur"));
        colSoldeCrediteur.setCellValueFactory(new PropertyValueFactory<>("soldeCrediteur"));

        colTotalDebit.setCellFactory(tc -> new TableCell<LigneBalance, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colTotalCredit.setCellFactory(tc -> new TableCell<LigneBalance, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colSoldeDebiteur.setCellFactory(tc -> new TableCell<LigneBalance, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colSoldeCrediteur.setCellFactory(tc -> new TableCell<LigneBalance, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        tableBalance.setItems(balanceData);
    }

    private void initializeGrandLivreTable() {
        colDateGL.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLibelleGL.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colJournalGL.setCellValueFactory(new PropertyValueFactory<>("numeroJournal"));
        colDebitGL.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCreditGL.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colSoldeCumuleGL.setCellValueFactory(new PropertyValueFactory<>("soldeCumule"));

        colDebitGL.setCellFactory(tc -> new TableCell<LigneGrandLivre, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colCreditGL.setCellFactory(tc -> new TableCell<LigneGrandLivre, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        colSoldeCumuleGL.setCellFactory(tc -> new TableCell<LigneGrandLivre, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    setStyle("-fx-alignment: CENTER-RIGHT;" +
                            (item.compareTo(BigDecimal.ZERO) < 0 ? "-fx-text-fill: red;" : ""));
                }
            }
        });

        tableGrandLivre.setItems(grandLivreData);
    }

    private void setupEventHandlers() {
        cbExerciceBalance.setOnAction(e -> genererBalance());
        cbExerciceGrandLivre.setOnAction(e -> loadComptesForGrandLivre());
        cbCompteGrandLivre.setOnAction(e -> genererGrandLivre());
        dpDateDebutGL.setOnAction(e -> genererGrandLivre());
        dpDateFinGL.setOnAction(e -> genererGrandLivre());
        cbExerciceBilan.setOnAction(e -> genererBilan());
        cbExerciceResultat.setOnAction(e -> genererCompteResultat());
    }

    private void loadData() {
        try {
               // Charger seulement les exercices de l'entreprise de l'utilisateur connecté
            Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
            List<Exercice> exercices = exerciceDAO.findByEntreprise(entrepriseId);
 
            cbExerciceBalance.setItems(FXCollections.observableArrayList(exercices));
            cbExerciceGrandLivre.setItems(FXCollections.observableArrayList(exercices));
            cbExerciceBilan.setItems(FXCollections.observableArrayList(exercices));
            cbExerciceResultat.setItems(FXCollections.observableArrayList(exercices));

            if (!exercices.isEmpty()) {
                Exercice exerciceCourant = exercices.get(0);
                cbExerciceBalance.setValue(exerciceCourant);
                cbExerciceGrandLivre.setValue(exerciceCourant);
                cbExerciceBilan.setValue(exerciceCourant);
                cbExerciceResultat.setValue(exerciceCourant);

                // Charger les comptes pour le Grand Livre
                loadComptesForGrandLivre();
            }

        } catch (Exception e) {
            logger.error("Erreur lors du chargement des données", e);
            showError("Erreur", "Impossible de charger les exercices: " + e.getMessage());
        }
    }

    private void loadComptesForGrandLivre() {
        Exercice exercice = cbExerciceGrandLivre.getValue();
        logger.info("Chargement des comptes pour Grand Livre. Exercice: {}", exercice);

        if (exercice != null) {
            try {
                Long entrepriseId = exercice.getEntreprise().getId();
                logger.info("ID Entreprise: {}", entrepriseId);

                List<Compte> comptes = compteDAO.findByEntreprise(entrepriseId);
                logger.info("Nombre de comptes trouvés: {}", comptes.size());

                if (!comptes.isEmpty()) {
                    logger.info("Premier compte: {}", comptes.get(0));
                }

                cbCompteGrandLivre.setItems(FXCollections.observableArrayList(comptes));

                dpDateDebutGL.setValue(exercice.getDateDebut());
                dpDateFinGL.setValue(exercice.getDateFin());

            } catch (Exception e) {
                logger.error("Erreur lors du chargement des comptes", e);
                showError("Erreur", "Impossible de charger les comptes: " + e.getMessage());
            }
        } else {
            logger.warn("Aucun exercice sélectionné pour le chargement des comptes");
        }
    }

    @FXML
    private void genererBalance() {
        Exercice exercice = cbExerciceBalance.getValue();
        if (exercice == null) {
            showWarning("Attention", "Veuillez sélectionner un exercice");
            return;
        }

        try {
            List<LigneBalance> balance = reportService.getBalance(exercice.getId());
            balanceData.clear();
            balanceData.addAll(balance);

        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la balance", e);
            showError("Erreur", "Impossible de générer la balance: " + e.getMessage());
        }
    }

    @FXML
    private void genererGrandLivre() {
        Compte compte = cbCompteGrandLivre.getValue();
        LocalDate dateDebut = dpDateDebutGL.getValue();
        LocalDate dateFin = dpDateFinGL.getValue();

        if (compte == null) {
            showWarning("Attention", "Veuillez sélectionner un compte");
            return;
        }
        if (dateDebut == null || dateFin == null) {
            showWarning("Attention", "Veuillez sélectionner une plage de dates");
            return;
        }

        try {
            logger.info("Génération Grand Livre pour compte ID: {}, du {} au {}",
                compte.getId(), dateDebut, dateFin);

            List<LigneGrandLivre> grandLivre = reportService.getGrandLivre(
                compte.getId(), dateDebut, dateFin);

            logger.info("Nombre de lignes trouvées: {}", grandLivre.size());

            grandLivreData.clear();
            grandLivreData.addAll(grandLivre);

            if (grandLivre.isEmpty()) {
                showWarning("Information", "Aucune écriture trouvée pour ce compte sur cette période");
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du grand livre", e);
            showError("Erreur", "Impossible de générer le grand livre: " + e.getMessage() +
                "\nCause: " + (e.getCause() != null ? e.getCause().getMessage() : "Inconnue"));
        }
    }

    @FXML
    private void genererBilan() {
        Exercice exercice = cbExerciceBilan.getValue();
        if (exercice == null) {
            showWarning("Attention", "Veuillez sélectionner un exercice");
            return;
        }

        try {
            BilanData bilan = reportService.getBilan(exercice.getId());

            lblActifImmobilise.setText(formatCurrency(bilan.getActifImmobilise()));
            lblActifCirculant.setText(formatCurrency(bilan.getActifCirculant()));
            lblCreances.setText(formatCurrency(bilan.getCreances()));
            lblTresorerie.setText(formatCurrency(bilan.getTresorerie()));
            lblTotalActif.setText(formatCurrency(bilan.getTotalActif()));

            lblCapitauxPropres.setText(formatCurrency(bilan.getCapitauxPropres()));
            lblDettesFinancieres.setText(formatCurrency(bilan.getDettesFinancieres()));
            lblDettesExploitation.setText(formatCurrency(bilan.getDettesExploitation()));
            lblTotalPassif.setText(formatCurrency(bilan.getTotalPassif()));

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du bilan", e);
            showError("Erreur", "Impossible de générer le bilan: " + e.getMessage());
        }
    }

    @FXML
    private void genererCompteResultat() {
        Exercice exercice = cbExerciceResultat.getValue();
        if (exercice == null) {
            showWarning("Attention", "Veuillez sélectionner un exercice");
            return;
        }

        try {
            CompteResultatData resultat = reportService.getCompteResultat(exercice.getId());

            lblChiffresAffaires.setText(formatCurrency(resultat.getChiffresAffaires()));
            lblChargesExploitation.setText(formatCurrency(resultat.getChargesExploitation()));
            lblResultatExploitation.setText(formatCurrency(resultat.getResultatExploitation()));
            lblChargesFinancieres.setText(formatCurrency(resultat.getChargesFinancieres()));
            lblProduitsFinanciers.setText(formatCurrency(resultat.getProduitsFinanciers()));
            lblResultatFinancier.setText(formatCurrency(resultat.getResultatFinancier()));
            lblResultatNet.setText(formatCurrency(resultat.getResultatNet()));

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du compte de résultat", e);
            showError("Erreur", "Impossible de générer le compte de résultat: " + e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return currencyService.formatAmount(BigDecimal.ZERO);
        return currencyService.formatAmount(amount);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}