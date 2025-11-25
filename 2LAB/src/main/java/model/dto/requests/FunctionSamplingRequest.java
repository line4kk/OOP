package model.dto.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionSamplingRequest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionSamplingRequest.class);

    private long functionId;
    private long analyticalFunctionId;
    private double xFrom;
    private double xTo;
    private int count;

    public FunctionSamplingRequest() {
        logger.info("Создан FunctionSamplingRequest");
    }

    public FunctionSamplingRequest(long functionId, long analyticalFunctionId, double xFrom, double xTo, int count) {
        this.functionId = functionId;
        this.analyticalFunctionId = analyticalFunctionId;
        this.xFrom = xFrom;
        this.xTo = xTo;
        this.count = count;
        logger.info("Создан {}", toString());
    }

    public long getFunctionId() {
        return functionId;
    }
    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }

    public Long getAnalyticalFunctionId() { return analyticalFunctionId; }
    public void setAnalyticalFunctionId(Long analyticalFunctionId) { this.analyticalFunctionId = analyticalFunctionId; }

    public Double getXFrom() { return xFrom; }
    public void setXFrom(Double xFrom) { this.xFrom = xFrom; }

    public Double getXTo() { return xTo; }
    public void setXTo(Double xTo) { this.xTo = xTo; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    @Override
    public String toString() {
        return "FunctionSamplingRequest{" +
                "functionId=" + functionId +
                ", analyticalFunctionId=" + analyticalFunctionId +
                ", xFrom=" + xFrom +
                ", xTo=" + xTo +
                ", count=" + count +
                '}';
    }
}
