package dao.criteria;

public class UserSearchCriteria {
    private String usernameContains;
    private String role;
    private String factoryType;
    private UserSortField sortBy = UserSortField.ID;
    private SortDirection direction = SortDirection.ASC;

    public String getUsernameContains() { return usernameContains; }
    public void setUsernameContains(String usernameContains) { this.usernameContains = usernameContains; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFactoryType() { return factoryType; }
    public void setFactoryType(String factoryType) { this.factoryType = factoryType; }

    public UserSortField getSortBy() { return sortBy; }
    public void setSortBy(UserSortField sortBy) { this.sortBy = sortBy; }

    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }

    @Override
    public String toString() {
        return "UserSearchCriteria{" +
                "usernameContains='" + usernameContains + '\'' +
                ", role='" + role + '\'' +
                ", factoryType='" + factoryType + '\'' +
                ", sortBy=" + sortBy +
                ", direction=" + direction +
                '}';
    }
}