package model.dto.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FunctionsResponse {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsResponse.class);
    private List<FunctionResponse> functions;

    public List<FunctionResponse> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionResponse> functions) {
        this.functions = functions;
    }

    public FunctionsResponse() {
        logger.info("Создан пустой FunctionsResponse");
    }

    public FunctionsResponse(List<FunctionResponse> functions) {
        this.functions = functions;
    }

    public static FunctionsResponse from(List<model.Function> functions) {
        if (functions.isEmpty()) {
            return new FunctionsResponse();
        }
        logger.info("Создан FunctionsResponse для пользователя {}", functions.getFirst().getUserId());
        List<FunctionResponse> result = new ArrayList<>();
        for (model.Function function : functions) {
            result.add(new FunctionResponse(
                    function.getId(),
                    function.getName(),
                    function.getType(),
                    function.getSource()
                )
            );
        }
        return new FunctionsResponse(result);
    }

    @Override
    public String toString() {
        return "FunctionsResponse{" +
                "functions=" + functions +
                '}';
    }
}

