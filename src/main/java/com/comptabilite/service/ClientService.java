package com.comptabilite.service;

import com.comptabilite.dao.ClientDAO;
import com.comptabilite.model.Client;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final ClientDAO clientDAO;

    public ClientService() {
        this.clientDAO = new ClientDAO();
    }

    public List<Client> getClientsByEntreprise(Long entrepriseId) {
        return clientDAO.findByEntreprise(entrepriseId);
    }

    public Client creerClient(Client client) {
        try {
            // Générer le code client si nécessaire
            if (client.getCodeClient() == null || client.getCodeClient().isEmpty()) {
                String codeGenere = genererCodeClient();
                client.setCodeClient(codeGenere);
            }

            // Maintenant que la table a été corrigée, utiliser Hibernate standard
            Client savedClient = clientDAO.save(client);

            logger.info("Client créé avec succès: {}", savedClient.getId());
            return savedClient;

        } catch (Exception e) {
            logger.error("Erreur lors de la création du client", e);
            throw new RuntimeException("Erreur lors de la création du client: " + e.getMessage(), e);
        }
    }

    public Client modifierClient(Client client) {
        try {
            // Utiliser le DAO directement
            Client updatedClient = clientDAO.update(client);

            logger.info("Client modifié avec succès: {}", updatedClient.getCodeClient());
            return updatedClient;

        } catch (Exception e) {
            logger.error("Erreur lors de la modification du client", e);
            throw new RuntimeException("Erreur lors de la modification du client: " + e.getMessage(), e);
        }
    }

    public void supprimerClient(Client client) {
        try {
            // Vérifier qu'il n'y a pas de factures associées
            long nombreFactures = clientDAO.countFacturesByClient(client.getId());
            if (nombreFactures > 0) {
                throw new RuntimeException("Impossible de supprimer le client: " + nombreFactures + " facture(s) associée(s)");
            }

            clientDAO.delete(client);
            logger.info("Client supprimé: {}", client.getCodeClient());

        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du client", e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }

    public String genererCodeClient() {
        // Format: CLI-NNNN (ex: CLI-0001)
        long count = clientDAO.countClients();
        return String.format("CLI-%04d", count + 1);
    }

    public Client findByCodeClient(String codeClient) {
        return clientDAO.findByCodeClient(codeClient);
    }

    public List<Client> searchClients(String terme) {
        return clientDAO.searchByNomOrEmail(terme);
    }

    public List<Client> getClientsActifs(Long entrepriseId) {
        return clientDAO.findByStatut(Client.StatutClient.ACTIF, entrepriseId);
    }
}