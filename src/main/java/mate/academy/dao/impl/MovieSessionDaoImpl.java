package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalTime;
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

            return Optional.ofNullable(movieSessionQuery.getSingleResult());
        } catch (Exception e) {
            throw new DataProcessingException("Can't select MovieSession by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> movieSessionQuery = session.createQuery("from MovieSession s "
                    + "join fetch s.movie m "
                    + "join fetch s.cinemaHall h "
                    + "where m.id = :movieId "
                    + "and s.showTime between :startOfDay and :endOfDay", MovieSession.class);
            movieSessionQuery.setParameter("movieId", movieId);
            movieSessionQuery.setParameter("startOfDay", date.atStartOfDay());
            movieSessionQuery.setParameter("endOfDay", date.atTime(LocalTime.MAX));
            return movieSessionQuery.list();
        } catch (Exception e) {
            throw new DataProcessingException("Can't select Available Session by date: " + date, e);
        }
    }
}
