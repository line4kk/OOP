CREATE TABLE composite_function_elements(
    id SERIAL PRIMARY KEY,
    composite_id INT NOT NULL,
    function_order INT NOT NULL,
    function_id INT NOT NULL,

    FOREIGN KEY (composite_id) REFERENCES functions(id) ON DELETE CASCADE,
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE RESTRICT
);