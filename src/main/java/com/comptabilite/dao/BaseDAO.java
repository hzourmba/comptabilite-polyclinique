package com.comptabilite.dao;

import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAO<T, ID extends Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    protected final Class<T> entityClass;

    public BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            logger.debug("Entité {} sauvegardée", entityClass.getSimpleName());
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la sauvegarde de {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde", e);
        }
    }

    public T update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            transaction.commit();
            logger.debug("Entité {} mise à jour", entityClass.getSimpleName());
            return mergedEntity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la mise à jour de {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la mise à jour", e);
        }
    }

    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par ID pour {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la recherche", e);
        }
    }

    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de toutes les entités {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }

    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.debug("Entité {} supprimée", entityClass.getSimpleName());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la suppression de {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    public void deleteById(ID id) {
        Optional<T> entity = findById(id);
        entity.ifPresent(this::delete);
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName(), Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des entités {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors du comptage", e);
        }
    }

    public boolean exists(ID id) {
        return findById(id).isPresent();
    }

    protected List<T> executeQuery(String hql, Object... parameters) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution de la requête HQL", e);
            throw new RuntimeException("Erreur lors de l'exécution de la requête", e);
        }
    }

    protected Optional<T> executeSingleResultQuery(String hql, Object... parameters) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution de la requête HQL", e);
            throw new RuntimeException("Erreur lors de l'exécution de la requête", e);
        }
    }
}