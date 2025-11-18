package dao.criteria;

public class CompositeFunctionElementSearchCriteria {
    private Long compositeId;           // обязательный фильтр — по какой композиции ищем
    private Integer orderFrom;          // function_order >= ?
    private Integer orderTo;            // function_order <= ?
    private Long functionId;            // конкретная функция в композиции
    private CompositeElementSortField sortBy = CompositeElementSortField.FUNCTION_ORDER;
    private SortDirection direction = SortDirection.ASC;

    // геттеры/сеттеры
    public Long getCompositeId() { return compositeId; }
    public void setCompositeId(Long compositeId) { this.compositeId = compositeId; }
    public Integer getOrderFrom() { return orderFrom; }
    public void setOrderFrom(Integer orderFrom) { this.orderFrom = orderFrom; }
    public Integer getOrderTo() { return orderTo; }
    public void setOrderTo(Integer orderTo) { this.orderTo = orderTo; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public CompositeElementSortField getSortBy() { return sortBy; }
    public void setSortBy(CompositeElementSortField sortBy) { this.sortBy = sortBy; }
    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }

    @Override
    public String toString() {
        return "CompositeFunctionElementSearchCriteria{" +
                "compositeId=" + compositeId +
                ", orderFrom=" + orderFrom +
                ", orderTo=" + orderTo +
                ", functionId=" + functionId +
                ", sortBy=" + sortBy +
                ", direction=" + direction +
                '}';
    }
}