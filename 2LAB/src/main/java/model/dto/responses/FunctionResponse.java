package model.dto.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionResponse {
    private static final Logger logger = LoggerFactory.getLogger(FunctionResponse.class);
    private Long id;
    private String name;
    private String type;
    private String source;

    public FunctionResponse() {}

    public FunctionResponse(Long id, String name, String type, String source) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public static FunctionResponse from(model.Function function) {
        logger.info("Создан FunctionResponse из Function: id={}, name={}", function.getId(), function.getName());
        return new FunctionResponse(
                function.getId(),
                function.getName(),
                function.getType(),
                function.getSource()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @Override
    public String toString() {
        return "FunctionResponse{id=" + id + ", name='" + name + "'}";
    }
}