package com.comptabilite;

import com.comptabilite.util.HibernateUtil;
import com.comptabilite.service.InitializationService;
import com.comptabilite.view.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComptabiliteApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ComptabiliteApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Démarrage de l'application de comptabilité");

            // Initialiser l'application (créer utilisateur admin, etc.)
            InitializationService initService = new InitializationService();
            initService.initializeApplication();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);

            LoginController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.setTitle("Comptabilité Entreprise - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            logger.info("Interface de connexion affichée");

        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de l'application", e);
            throw new RuntimeException("Impossible de démarrer l'application", e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Arrêt de l'application");
        HibernateUtil.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        logger.info("Lancement de l'application de comptabilité d'entreprise");
        launch(args);
    }
}