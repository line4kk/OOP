CREATE TABLE operations(
    id SERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL,
    function1_id INT NOT NULL,
    function2_id INT,
    result_function_id INT NOT NULL,

    FOREIGN KEY (function1_id) REFERENCES functions(id),
    FOREIGN KEY (function2_id) REFERENCES functions(id),
    FOREIGN KEY (result_function_id) REFERENCES functions(id)
);