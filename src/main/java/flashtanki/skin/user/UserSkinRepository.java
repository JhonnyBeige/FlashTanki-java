package flashtanki.skin.user;

import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;
import flashtanki.skin.list.SkinItem;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public class UserSkinRepository {
    private static UserSkinRepository instance;

    public static UserSkinRepository getInstance() {
        if (UserSkinRepository.instance == null) {
            UserSkinRepository.instance = new UserSkinRepository();
        }
        return UserSkinRepository.instance;
    }

    private UserSkinRepository() {
    }

    public void unmountAllSkinsByItem(Long userId, String item) {
        Transaction tx = null;
        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("UPDATE UserSkin US SET US.mounted = false " +
                    "WHERE US.userId = :userId and US.skinId in (SELECT S.id FROM SkinItem S WHERE S.itemId = :itemId)");
            query.setParameter("userId", userId);
            query.setParameter("itemId", item);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while unmounting all skins by item", e);
        }
    }

    public void mountSkin(Long userId, Long skinId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("UPDATE UserSkin US SET US.mounted = true " +
                    "WHERE US.userId = :userId and US.skinId = :skinId");
            query.setParameter("userId", userId);
            query.setParameter("skinId", skinId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while mounting skin", e);
        }
    }

    public List<UserSkin> getUserSkinsForItem(Long userId, String item) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserSkin US WHERE US.userId = :userId " +
                    "and US.skinId in (SELECT S.id FROM SkinItem S WHERE S.itemId = :itemId)");
            query.setParameter("userId", userId);
            query.setParameter("itemId", item);
            List<UserSkin> userSkins = (List<UserSkin>) query.getResultList();
            tx.commit();
            return userSkins;
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user skins for item", e);
        }
    }

    public boolean userHaveSkin(Long userId, String clientId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("SELECT count(US) FROM UserSkin US " +
                    "JOIN SkinItem SI ON US.skinId = SI.id " +
                    "WHERE SI.clientId = :clientId and US.userId = :userId");
            query.setParameter("clientId", clientId);
            query.setParameter("userId", userId);
            Long count = (Long) query.getSingleResult();
            tx.commit();
            return count > 0;
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while checking skin by client id", e);
        }
    }

    public Optional<SkinItem> getMountedUserSkin(Long userId, String item) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("SELECT SI FROM UserSkin US " +
                    "JOIN SkinItem SI ON US.skinId = SI.id " +
                    "WHERE US.userId = :userId and US.mounted = true and SI.itemId = :itemId");
            query.setParameter("userId", userId);
            query.setParameter("itemId", item);
            SkinItem skinItem = (SkinItem) query.getSingleResult();
            tx.commit();
            return Optional.ofNullable(skinItem);
        } catch (NoResultException e) {
            tx.rollback();
            return Optional.empty();
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting mounted user skin", e);
        }
    }

    public List<UserSkin> getMountedUserSkins(Long userId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserSkin US WHERE US.userId = :userId " +
                    "and US.mounted = true");
            query.setParameter("userId", userId);
            List<UserSkin> userSkins = (List<UserSkin>) query.getResultList();
            tx.commit();
            return userSkins;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {

            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting mounted user skins", e);
        }
    }

    public List<UserSkin> getAllUserSkins(Long userId) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM UserSkin US WHERE US.userId = :userId");
            query.setParameter("userId", userId);
            List<UserSkin> userSkins = (List<UserSkin>) query.getResultList();
            tx.commit();
            return userSkins;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {

            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting mounted user skins", e);
        }
    }

    public void save(UserSkin skin) {
        Transaction tx = null;

        try {
            Session session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(skin);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while saving user skin " + skin, e);
        }
    }
}
