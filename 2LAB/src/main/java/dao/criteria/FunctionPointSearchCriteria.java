package dao.criteria;

public class FunctionPointSearchCriteria {
    private Long functionId;           // обязательно, если null — вернуть все точки всех функций
    private Double xFrom;              // x_value >= ?
    private Double xTo;                // x_value <= ?
    private Double yFrom;              // y_value >= ?
    private Double yTo;                // y_value <= ?
    private FunctionPointSortField sortBy = FunctionPointSortField.X_VALUE;
    private SortDirection direction = SortDirection.ASC;

    // геттеры и сеттеры
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getxFrom() { return xFrom; }
    public void setxFrom(Double xFrom) { this.xFrom = xFrom; }
    public Double getxTo() { return xTo; }
    public void setxTo(Double xTo) { this.xTo = xTo; }
    public Double getyFrom() { return yFrom; }
    public void setyFrom(Double yFrom) { this.yFrom = yFrom; }
    public Double getyTo() { return yTo; }
    public void setyTo(Double yTo) { this.yTo = yTo; }
    public FunctionPointSortField getSortBy() { return sortBy; }
    public void setSortBy(FunctionPointSortField sortBy) { this.sortBy = sortBy; }
    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }

    @Override
    public String toString() {
        return "FunctionPointSearchCriteria{" +
                "functionId=" + functionId +
                ", xFrom=" + xFrom +
                ", xTo=" + xTo +
                ", yFrom=" + yFrom +
                ", yTo=" + yTo +
                ", sortBy=" + sortBy +
                ", direction=" + direction +
                '}';
    }
}