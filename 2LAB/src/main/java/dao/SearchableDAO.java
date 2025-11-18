package dao;

import java.util.List;

public interface SearchableDAO<T, C> extends DAO<T> {
    List<T> search(C criteria);
}