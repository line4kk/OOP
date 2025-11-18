package dao.searchcriteria;

public class FunctionSearchCriteria {
    private Long userId;           // необязательно
    private String nameContains;   // LIKE %...%
    private String type;           // точное совпадение
    private SortField sortBy = SortField.ID;
    private SortDirection direction = SortDirection.ASC;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNameContains() { return nameContains; }
    public void setNameContains(String nameContains) { this.nameContains = nameContains; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public SortField getSortBy() { return sortBy; }
    public void setSortBy(SortField sortBy) { this.sortBy = sortBy; }
    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }

    @Override
    public String toString() {
        return "FunctionSearchCriteria{" +
                "userId=" + userId +
                ", nameContains='" + nameContains + '\'' +
                ", type='" + type + '\'' +
                ", sortBy=" + sortBy +
                ", direction=" + direction +
                '}';
    }
}
