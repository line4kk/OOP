package model.dto.requests;

import model.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionCreateRequest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionCreateRequest.class);
    private String name;
    private String type;
    private String source;

    public FunctionCreateRequest() {
        logger.info("Создан FunctionCreateRequest");
    }

    public FunctionCreateRequest(String name, String type, String source) {
        this.name = name;
        this.type = type;
        this.source = source;
        logger.info("Создан {}", toString());
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Function toEntity(long userId) {
        logger.info("Преобразование FunctionCreateRequest → Function для userId={}", userId);
        Function function = new Function(
                userId,
                this.getName(),
                this.getType(),
                this.getSource()
        );
        return function;
    }

    @Override
    public String toString() {
        return "FunctionCreateRequest{name='" + name + "', type='" + type + "'}";
    }
}