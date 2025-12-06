package dao;

public interface DAO<T> {
    T insert(T entity);
    void deleteById(long id);
}
