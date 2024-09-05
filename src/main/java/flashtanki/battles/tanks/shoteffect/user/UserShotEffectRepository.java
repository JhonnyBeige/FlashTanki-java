package flashtanki.battles.tanks.shoteffect.user;

import flashtanki.logger.remote.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;
import flashtanki.battles.tanks.shoteffect.list.ShotEffectItem;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public class UserShotEffectRepository {
    private static UserShotEffectRepository instance;

    public static UserShotEffectRepository getInstance() {
        if (UserShotEffectRepository.instance == null) {
            UserShotEffectRepository.instance = new UserShotEffectRepository();
        }
        return UserShotEffectRepository.instance;
    }

    private UserShotEffectRepository() {
    }

    public void unmountAllShotEffectsByItem(Long userId, String item) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("UPDATE UserShotEffect US SET US.mounted = false " +
                    "WHERE US.userId = :userId and US.shotEffectId in (SELECT S.id FROM ShotEffectItem S WHERE S.itemId = :itemId)");
            query.setParameter("userId", userId);
            query.setParameter("itemId", item);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while unmounting all shot effects by item", e);
        }
    }

    public void mountShotEffect(Long userId, Long shotEffectId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("UPDATE UserShotEffect US SET US.mounted = true " +
                    "WHERE US.userId = :userId and US.shotEffectId = :shotEffectId");
            query.setParameter("userId", userId);
            query.setParameter("shotEffectId", shotEffectId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while mounting shot effect", e);
        }
    }

    public List<UserShotEffect> getUserShotEffectsForItem(Long userId, String item) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserShotEffect US WHERE US.userId = :userId " +
                    "and US.shotEffectId in (SELECT S.id FROM ShotEffectItem S WHERE S.itemId = :itemId)");
            query.setParameter("userId", userId);
            query.setParameter("itemId", item);
            List<UserShotEffect> userShotEffects = (List<UserShotEffect>) query.getResultList();
            tx.commit();
            return userShotEffects;
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user shot effects for item", e);
        }
    }

    public boolean userHaveShotEffect(Long userId, String clientId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("SELECT count(USE) FROM UserShotEffect USE " +
                    "JOIN ShotEffectItem SEI ON USE.shotEffectId = SEI.id " +
                    "WHERE USE.userId = :userId and SEI.clientId = :clientId");
            query.setParameter("clientId", clientId);
            query.setParameter("userId", userId);
            Long count = (Long) query.getSingleResult();
            tx.commit();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while checking if user have shot effect", e);
        }
    }

    public Optional<ShotEffectItem> getMountedUserShotEffect(long userId, String itemId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("SELECT SEI FROM UserShotEffect USE " +
                    "JOIN ShotEffectItem SEI ON USE.shotEffectId = SEI.id " +
                    "WHERE USE.userId = :userId and USE.mounted = true and SEI.itemId = :itemId");
            query.setParameter("userId", userId);
            query.setParameter("itemId", itemId);
            ShotEffectItem shotEffectItem = (ShotEffectItem) query.getSingleResult();
            tx.commit();
            return Optional.ofNullable(shotEffectItem);
        } catch (NoResultException e) {
            try {
                tx.rollback();
            }catch (Exception ex){

            }

            return Optional.empty();
        } catch (Exception e) {
            try {
                tx.rollback();
            }catch (Exception ex){
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting mounted user shot effect", e);
        }
    }

    public List<UserShotEffect> getMountedUserShotEffects(Long userId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserShotEffect US WHERE US.userId = :userId " +
                    "and US.mounted = true");
            query.setParameter("userId", userId);
            List<UserShotEffect> userShotEffects = (List<UserShotEffect>) query.getResultList();
            tx.commit();
            return userShotEffects;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception e1) {

            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting mounted user shot effect", e);
        }
    }

    public List<UserShotEffect> getAllUserShotEffects(Long userId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserShotEffect US WHERE US.userId = :userId");
            query.setParameter("userId", userId);
            List<UserShotEffect> userShotEffects = (List<UserShotEffect>) query.getResultList();
            tx.commit();
            return userShotEffects;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception e1) {

            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting all user shot effects", e);
        }
    }

    public void save(UserShotEffect userShotEffect) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(userShotEffect);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while saving user shot effect", e);
        }
    }
}
