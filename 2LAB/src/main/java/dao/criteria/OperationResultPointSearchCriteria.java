package dao.criteria;

public class OperationResultPointSearchCriteria {
    private Long point1Id;           // точка 1
    private Long point2Id;           // точка 2
    private String operation;        // точное совпадение операции (+, -, *, / и т.д.)
    private Double resultYFrom;      // result_y >= ?
    private Double resultYTo;        // result_y <= ?
    private OperationResultPointSortField sortBy = OperationResultPointSortField.ID;
    private SortDirection direction = SortDirection.ASC;

    // геттеры и сеттеры
    public Long getPoint1Id() { return point1Id; }
    public void setPoint1Id(Long point1Id) { this.point1Id = point1Id; }
    public Long getPoint2Id() { return point2Id; }
    public void setPoint2Id(Long point2Id) { this.point2Id = point2Id; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Double getResultYFrom() { return resultYFrom; }
    public void setResultYFrom(Double resultYFrom) { this.resultYFrom = resultYFrom; }
    public Double getResultYTo() { return resultYTo; }
    public void setResultYTo(Double resultYTo) { this.resultYTo = resultYTo; }
    public OperationResultPointSortField getSortBy() { return sortBy; }
    public void setSortBy(OperationResultPointSortField sortBy) { this.sortBy = sortBy; }
    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }

    @Override
    public String toString() {
        return "OperationResultPointSearchCriteria{" +
                "point1Id=" + point1Id +
                ", point2Id=" + point2Id +
                ", operation='" + operation + '\'' +
                ", resultYFrom=" + resultYFrom +
                ", resultYTo=" + resultYTo +
                ", sortBy=" + sortBy +
                ", direction=" + direction +
                '}';
    }
}