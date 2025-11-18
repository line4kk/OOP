package model.dto.requests;

import model.CompositeFunctionElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CompositeCreateRequest {
    private static final Logger logger = LoggerFactory.getLogger(CompositeCreateRequest.class);
    private List<Long> functionIdsInOrder;

    public CompositeCreateRequest() {
        logger.info("Создан CompositeCreateRequest");
        this.functionIdsInOrder = new ArrayList<>();

    }

    public CompositeCreateRequest(List<Long> functionIdsInOrder) {
        this.functionIdsInOrder = functionIdsInOrder;
        logger.info("Создан {}", toString());
    }

    public List<Long> getFunctionIdsInOrder() { return functionIdsInOrder; }
    public void setFunctionIdsInOrder(List<Long> functionIdsInOrder) { this.functionIdsInOrder = functionIdsInOrder; }

    public List<CompositeFunctionElement> toEntities(long compositeId) {
        logger.info("Преобразование CompositeCreateRequest → список CompositeFunctionElement для compositeId={}", compositeId);
        List<CompositeFunctionElement> elements = new ArrayList<>();
        for (int i = 0; i < this.getFunctionIdsInOrder().size(); i++) {
            Long functionId = this.getFunctionIdsInOrder().get(i);
            CompositeFunctionElement el = new CompositeFunctionElement(
                    compositeId,
                    i + 1,  // порядок начиная с 1
                    functionId
            );
            elements.add(el);
        }
        logger.info("Созданы {} элементов композиции", elements.size());
        return elements;
    }
    @Override
    public String toString() {
        return "CompositeCreateRequest{functionIdsInOrder=" + functionIdsInOrder + "}";
    }
}