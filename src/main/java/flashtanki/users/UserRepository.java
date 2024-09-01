package flashtanki.users;

import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.services.hibernate.HibernateService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepository {
    private static UserRepository instance;

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private UserRepository() {
    }

    public Map<Long, Integer> getUsersRanks(List<Long> usersIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            transaction = session.beginTransaction();
            List<Object[]> result = session.createNativeQuery("SELECT user_id, rank FROM users WHERE user_id IN (:usersIds)")
                    .setParameterList("usersIds", usersIds)
                    .getResultList();
            transaction.commit();
            return result.stream()
                    .collect(Collectors.toMap(
                            obj -> ((BigInteger) obj[0]).longValue(),
                            obj -> ((BigInteger) obj[1]).intValue()
                    ));
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting users ranks", e);
        }
    }

    public Map<Long, UserRank> getUsersNicknamesAndRanks(List<Long> usersIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            transaction = session.beginTransaction();
            List<Object[]> result = session.createNativeQuery("SELECT uid, nickname, rank FROM users WHERE uid IN (:usersIds)")
                    .setParameterList("usersIds", usersIds)
                    .getResultList();
            transaction.commit();
            return result.stream()
                    .collect(Collectors.toMap(
                            obj -> ((BigInteger) obj[0]).longValue(),
                            obj -> UserRank.builder()
                                    .nickname((String) obj[1])
                                    .rank(((BigInteger) obj[2]).intValue())
                                    .build()
                    ));
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting users ranks", e);
        }
    }

    public User findUserByNickname(String nickname) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            transaction = session.beginTransaction();
            User result = session.createQuery("FROM User WHERE nickname = :nickname", User.class)
                    .setParameter("nickname", nickname)
                    .uniqueResult();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException("Error while getting user by nickname", e);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRank {
        private String nickname;
        private int rank;
    }
}
