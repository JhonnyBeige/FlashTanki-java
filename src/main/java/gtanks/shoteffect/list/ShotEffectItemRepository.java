package gtanks.shoteffect.list;

import gtanks.logger.RemoteDatabaseLogger;
import gtanks.services.hibernate.HibernateService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShotEffectItemRepository {
    private static ShotEffectItemRepository instance;
    public static ShotEffectItemRepository getInstance() {
        if (ShotEffectItemRepository.instance == null) {
            ShotEffectItemRepository.instance = new ShotEffectItemRepository();
        }
        return ShotEffectItemRepository.instance;
    }
    private ShotEffectItemRepository() {
    }

    public Optional<ShotEffectItem> findShotEffectByClientId(String shotEffectClientId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM ShotEffectItem SI WHERE SI.clientId = :clientId");
            query.setParameter("clientId", shotEffectClientId);
            ShotEffectItem shotEffect = (ShotEffectItem) query.getSingleResult();
            tx.commit();
            return Optional.ofNullable(shotEffect);
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
                RemoteDatabaseLogger.error(ex);
            }
            return Optional.empty();
        }
    }

    public List<ShotEffectItem> findAll(){
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM ShotEffectItem");
            List<ShotEffectItem> shotEffectItems = (List<ShotEffectItem>) query.getResultList();
            tx.commit();
            return shotEffectItems;
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting shot effects", e);
        }
    }
    public List<ShotEffectItem> getShotEffectItems(String itemId) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM ShotEffectItem SI WHERE SI.itemId = :itemId");
            query.setParameter("itemId", itemId);
            List<ShotEffectItem> resultList =(List<ShotEffectItem>) query.getResultList();
            tx.commit();
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting shot effects", e);
        }
    }
}
