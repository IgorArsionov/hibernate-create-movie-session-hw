package mate.academy.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {

    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert MovieSession: " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return movieSession;
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> movieSessionQuery = session.createQuery("from MovieSession s "
                    + "join fetch s.movie "
                    + "join fetch s.cinemaHall "
                    + "where s.id = :id", MovieSession.class);
            movieSessionQuery.setParameter("id", id);

            return movieSessionQuery.uniqueResultOptional();
        } catch (Exception e) {
            throw new DataProcessingException("Can't select MovieSession by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(
            Long movieId, LocalDateTime start, LocalDateTime end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> movieSessionQuery = session.createQuery("from MovieSession s "
                    + "join fetch s.movie m "
                    + "join fetch s.cinemaHall h "
                    + "where m.id = :movieId "
                    + "and s.showTime >= :startOfDay "
                    + "and s.showTime < :endOfDay", MovieSession.class);
            movieSessionQuery.setParameter("movieId", movieId);
            movieSessionQuery.setParameter("startOfDay", start);
            movieSessionQuery.setParameter("endOfDay", end);
            return movieSessionQuery.list();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get available movie sessions for movieId=" + movieId + ", between " + start + " and " + end, e);
        }
    }
}
