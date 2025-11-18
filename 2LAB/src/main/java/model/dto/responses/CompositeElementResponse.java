package model.dto.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeElementResponse {
    private static final Logger logger = LoggerFactory.getLogger(CompositeElementResponse.class);

    private Long id;
    private int order;
    private Long functionId;

    public CompositeElementResponse() {}

    public CompositeElementResponse(Long id, int order, Long functionId) {
        this.id = id;
        this.order = order;
        this.functionId = functionId;
    }

    public static CompositeElementResponse from(model.CompositeFunctionElement el) {
        logger.info("Создан CompositeElementResponse из CompositeFunctionElement: id={}, order={}, functionId={}", el.getId(), el.getFunctionOrder(), el.getFunctionId());
        return new CompositeElementResponse(el.getId(), el.getFunctionOrder(), el.getFunctionId());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    @Override
    public String toString() {
        return "CompositeElementResponse{id=" + id + ", order=" + order + ", functionId=" + functionId + "}";
    }
}