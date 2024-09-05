package flashtanki.users.friends;

import flashtanki.logger.remote.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;

public class FriendsRepository {
    private static FriendsRepository instance;

    public static FriendsRepository getInstance() {
        if (instance == null) {
            instance = new FriendsRepository();
        }
        return instance;
    }

    private FriendsRepository() {
    }
    public void save(Friends friends) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(friends);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while saving friends", e);
        }
    }

    public Optional<Friends> getFriendsByUser(long userId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Friends where userId = :userId");
            query.setParameter("userId", userId);
            Friends result = (Friends) query.uniqueResult();
            tx.commit();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting skins", e);
        }
    }
}
