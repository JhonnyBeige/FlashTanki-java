package flashtanki.containers.list;

import java.util.List;
import java.util.Optional;

import flashtanki.logger.RemoteDatabaseLogger;
import org.hibernate.Session;

import flashtanki.services.hibernate.HibernateService;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class ContainerAssortmentRespository {
    private static ContainerAssortmentRespository instance;

    public static ContainerAssortmentRespository getInstance() {
        if (ContainerAssortmentRespository.instance == null) {
            ContainerAssortmentRespository.instance = new ContainerAssortmentRespository();
        }
        return ContainerAssortmentRespository.instance;
    }

    private ContainerAssortmentRespository() {
    }

    public List<ContainerItemInfo> findAll() {
        Session session =null;
        Transaction tx =null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();

            List<ContainerItemInfo> result = session.createQuery("FROM ContainerItemInfo ").list();
            tx.commit();
            return result;
        }catch (Exception e) {
            try {
                tx.rollback();
            }catch (Exception ex) {
            }
            RemoteDatabaseLogger.error(e);
        }
        return List.of();

    }


    public Optional<ContainerItemInfo> findByClientId(String clientId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM ContainerItemInfo CI WHERE CI.clientId = :clientId");
            query.setParameter("clientId", clientId);
            ContainerItemInfo skin = (ContainerItemInfo) query.getSingleResult();
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
}