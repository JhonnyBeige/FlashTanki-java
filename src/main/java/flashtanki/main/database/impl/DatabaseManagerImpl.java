/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.database.impl;

import flashtanki.users.garage.containers.ContainerSystem;
import flashtanki.logger.remote.types.LogType;
import flashtanki.logger.remote.LogObject;
import flashtanki.logger.LoggerService;
import flashtanki.logger.remote.RemoteDatabaseLogger;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.netty.blackip.BlackIP;
import flashtanki.services.hibernate.HibernateService;
import flashtanki.users.TypeUser;
import flashtanki.users.User;
import flashtanki.users.friends.Friends;
import flashtanki.users.garage.Garage;
import flashtanki.users.karma.Karma;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatabaseManagerImpl extends Thread implements DatabaseManager {
    private static final DatabaseManagerImpl instance = new DatabaseManagerImpl();
    private final Map<String, User> cache = new TreeMap<String, User>(String.CASE_INSENSITIVE_ORDER);
    private final static LoggerService loggerService = LoggerService.getInstance();
    private final ContainerSystem containerSystem = ContainerSystem.getInstance();

    @Override
    public void register(User user) {
        this.configurateNewAccount(user);
        Garage garage = new Garage();
        garage.parseJSONData();
        Karma emptyKarma = new Karma();
        emptyKarma.setUserId(user.getNickname());
        System.out.println(user.getNickname());
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            //save new User and get generated id
            long userId = (long) session.save(user);
            garage.setUserId(userId);
            session.save(garage);
            System.out.println(emptyKarma.toString());
            session.save(emptyKarma);
            tx.commit();
            garage.giveItem("hunter_m0", 1, () -> garage.mountItem("hunter_m0"), () -> {
            });
            garage.giveItem("smoky_m0", 1, () -> garage.mountItem("smoky_m0"), () -> {
            });
            garage.giveItem("holiday_m0", 1, () -> garage.mountItem("holiday_m0"), () -> {
            });
            garage.giveItem("green_m0", 1, () -> garage.mountItem("green_m0"), () -> {
            });
            garage.giveItem("standard_m0", 1, () -> garage.mountItem("standard_m0"), () -> {
            });
            containerSystem.giveContainer(user.getId(), "container_0", 0);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void update(Karma karma) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        user = this.cache.get(karma.getUserId());
        if (user != null) {
            user.setKarma(karma);
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(karma);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void update(Garage garage) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(garage);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void update(User user) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public User getUserByNickName(String nickname) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        user = this.getUserByIdFromCache(nickname);
        if (user != null) {
            return user;
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM User U WHERE U.nickname = :nickname");
            query.setString("nickname", nickname);
            user = (User) query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return user;
    }

    @Override
    public String getNicknameByEmail(String email) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        String nickname = null;

        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM User U WHERE U.email = :email");
            query.setString("email", email);
            user = (User) query.uniqueResult();

            if (user != null) {
                nickname = user.getNickname();
            }

            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                RemoteDatabaseLogger.error(ex);
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }

        return nickname; // Return the retrieved nickname
    }

    @Override
    public User getUserById(Long userId) {
        Session session = null;
        Transaction tx = null;
        User user = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            user = session.get(User.class, userId);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return user;
    }

    public static DatabaseManager instance() {
        return instance;
    }

    @Override
    public Friends getFriendByUser(final User user) {
        Session session = null;
        Transaction tx = null;
        Friends friends = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            if (!session.getTransaction().isActive() || session.getTransaction() == null) {
                tx = session.beginTransaction();
            }
            final Query e = session.createQuery("FROM Friends F WHERE F.userId = :userId");
            e.setLong("userId", user.getId());
            friends = (Friends) e.uniqueResult();
            tx.commit();
        } catch (Exception var6) {
            var6.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(var6);
        }
        return friends;
    }

    @Override
    public void update(final Friends friends) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.update(friends);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void cache(User user) {
        if (user == null) {
            loggerService.log(LogType.ERROR, "DatabaseManagerImpl::cache user is null!");
            return;
        }
        this.cache.put(user.getNickname(), user);
    }

    @Override
    public void uncache(String id) {
        this.cache.remove(id);
    }

    @Override
    public User getUserByIdFromCache(String nickname) {
        return this.cache.get(nickname);
    }

    @Override
    public boolean contains(String nickname) {
        return this.getUserByNickName(nickname) != null;
    }

    public void configurateNewAccount(User user) {
        user.setCrystall(500);
        user.setNextScore(100);
        user.setType(TypeUser.DEFAULT);
        user.setEmail(null);
    }

    @Override
    public int getCacheSize() {
        return this.cache.size();
    }

    @Override
    public Garage getGarageByUser(User user) {
        Session session = null;
        Transaction tx = null;
        Garage garage = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Garage G WHERE G.id = :uid");
            query.setLong("uid", user.getId());
            garage = (Garage) query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
        return garage;
    }

    @Override
    public Karma getKarmaByUser(User user) {
        Session session = null;
        Transaction tx = null;
        Karma karma = null;
        if (user.getKarma() != null) {
            return user.getKarma();
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Karma K WHERE K.userId = :nickname");
            query.setString("nickname", user.getNickname());
            karma = (Karma) query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
        }
        return karma;
    }

    @Override
    public Karma getKarmaByNickname(String user) {
        Session session = null;
        Transaction tx = null;
        Karma karma = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM Karma K WHERE K.userId = :nickname");
            query.setString("nickname", user);
            karma = (Karma) query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
        }
        return karma;
    }

    @Override
    public BlackIP getBlackIPbyAddress(String address) {
        Session session = null;
        Transaction tx = null;
        BlackIP ip = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("FROM BlackIP B WHERE B.ip = :ip");
            query.setString("ip", address);
            ip = (BlackIP) query.uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
        }
        return ip;
    }

    @Override
    public void register(BlackIP blackIP) {
        Session session = null;
        Transaction tx = null;
        if (this.getBlackIPbyAddress(blackIP.getIp()) != null) {
            return;
        }
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(blackIP);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void unregister(BlackIP blackIP) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            SQLQuery query = session.createSQLQuery("delete from flashtanki.black_ips where ip = :ip");
            query.setString("ip", blackIP.getIp());
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
        }
    }

    @Override
    public void register(LogObject log) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            session.save(log);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        }
    }

    @Override
    public List<LogObject> collectLogs() {
        Session session = null;
        Transaction tx = null;
        List logs = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            logs = session.createCriteria(LogObject.class).list();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            return null;
        }
        return logs;
    }


    @Override
    public List<Garage> collectGarages() {
        Session session = null;
        List garages = null;
        Transaction tx = null;
        try {
            session = HibernateService.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            garages = session.createCriteria(Garage.class).list();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            RemoteDatabaseLogger.error(e);
            return null;
        }
        return garages;
    }
}

