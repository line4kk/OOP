package model.dto.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationRequest {
    private static final Logger logger = LoggerFactory.getLogger(OperationRequest.class);
    private long function1Id;
    private Long function2Id;
    private String operation;

    public OperationRequest() {
        logger.info("Создан OperationRequest");
    }

    public OperationRequest(long function1Id, Long function2Id, String operation) {
            this.function1Id = function1Id;
            this.function2Id = function2Id;
            this.operation = operation;
            logger.info("Создан {}", toString());
        }

        public long getFunction1Id() { return function1Id; }
        public void setFunction1Id(long function1Id) { this.function1Id = function1Id; }
        public Long getFunction2Id() { return function2Id; }
        public void setFunction2Id(Long function2Id) { this.function2Id = function2Id; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }



        @Override
        public String toString() {
            return "OperationRequest{function1Id=" + function1Id + ", function2Id=" + function2Id + ", operation='" + operation + "'}";
        }
    }