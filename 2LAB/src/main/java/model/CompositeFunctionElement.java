package model;

public class CompositeFunctionElement {
    private long id;
    private long compositeId;
    private int functionOrder;
    private long functionId;

    public CompositeFunctionElement(long id, long compositeId, int functionOrder, long functionId) {
        this.id = id;
        this.compositeId = compositeId;
        this.functionOrder = functionOrder;
        this.functionId = functionId;
    }

    public CompositeFunctionElement(long compositeId, int functionOrder, long functionId) {
        this.compositeId = compositeId;
        this.functionOrder = functionOrder;
        this.functionId = functionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCompositeId() {
        return compositeId;
    }

    public void setCompositeId(long compositeId) {
        this.compositeId = compositeId;
    }

    public int getFunctionOrder() {
        return functionOrder;
    }

    public void setFunctionOrder(int functionOrder) {
        this.functionOrder = functionOrder;
    }

    public long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }
}
