package gtanks.skin.list;

import gtanks.logger.RemoteDatabaseLogger;
import gtanks.services.hibernate.HibernateService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SkinItemRepository {
    private static SkinItemRepository instance;

    public static SkinItemRepository getInstance() {
        if (SkinItemRepository.instance == null) {
            SkinItemRepository.instance = new SkinItemRepository();
        }
        return SkinItemRepository.instance;
    }

    private SkinItemRepository() {
    }

    public List<SkinItem> findAll() {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM SkinItem");
            List<SkinItem> skins = (List<SkinItem>) query.getResultList();
            tx.commit();
            return skins;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            }catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting skins", e);
        }
    }

    public Optional<SkinItem> findSkinByClientId(String clientId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM SkinItem SI WHERE SI.clientId = :clientId");
            query.setParameter("clientId", clientId);
            SkinItem skin = (SkinItem) query.getSingleResult();
            tx.commit();
            return Optional.ofNullable(skin);
        } catch (Exception e) {
            try {
                tx.rollback();
            }catch (Exception ex) {
            }
            return Optional.empty();
        }
    }

    public List<SkinItem> getItemSkins(String itemId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM SkinItem SI WHERE SI.itemId = :itemId");
            query.setParameter("itemId", itemId);
            List<SkinItem> skins = query.getResultList();
            tx.commit();
            return skins;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            }catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting skins", e);
        }
    }
}
