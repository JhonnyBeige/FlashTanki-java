package flashtanki.users.garage.containers.users;

import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserContainerRepository {
    private static UserContainerRepository instance;

    public static UserContainerRepository getInstance() {
        if (UserContainerRepository.instance == null) {
            UserContainerRepository.instance = new UserContainerRepository();
        }
        return UserContainerRepository.instance;
    }

    private UserContainerRepository() {
    }


    public List<UserContainer> getUserContainers(Long userId) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserContainer UC WHERE UC.userId = :userId");
            query.setParameter("userId", userId);
            List<UserContainer> result = query.list();
            tx.commit();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user containers", e);
        }
    }

    public int countUserContainers(Long userId, Long containerId) {
        // Get count field if exists
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery(
                    "SELECT UC.count FROM UserContainer UC WHERE UC.userId = :userId and UC.containerId = :containerId");
            query.setParameter("userId", userId);
            query.setParameter("containerId", containerId);
            List<Integer> result = query.list();
            tx.commit();
            if (result.size() > 0) {
                return result.get(0);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user containers", e);
        }
    }

    public boolean existUserContainers(Long userId, Long containerId) {
        // Get count field if exists
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery(
                    "FROM UserContainer UC WHERE UC.userId = :userId and UC.containerId = :containerId");
            query.setParameter("userId", userId);
            query.setParameter("containerId", containerId);
            List<Integer> result = query.list();
            tx.commit();
            if (result.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user containers", e);
        }
    }

    public void save(UserContainer userContainer) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(userContainer);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while saving user container", e);
        }
    }

    public void decrementContainer(Long userId, Long containerId) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery(
                    "UPDATE UserContainer UC SET UC.count = UC.count - 1 WHERE UC.userId = :userId " +
                            "and UC.containerId = :containerId");
            query.setParameter("userId", userId);
            query.setParameter("containerId", containerId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while decrementing user container", e);
        }

    }

    public void addContainers(Long userId, Long containerId, int count) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery(
                    "UPDATE UserContainer UC SET UC.count = UC.count + :count WHERE UC.userId = :userId " +
                            "and UC.containerId = :containerId");
            query.setParameter("userId", userId);
            query.setParameter("containerId", containerId);
            query.setParameter("count", count);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while incrementing user container", e);
        }
    }
}
