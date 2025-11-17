package dao;

public interface DAO<T> {
    void insert(T entity);
    void deleteById(long id);
}
