CREATE TABLE function_points(
    function_id INT NOT NULL,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (function_id, x_value),
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE
);