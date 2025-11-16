CREATE TABLE operations_result_points(
    id SERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL,
    point1_id INT NOT NULL,
    point2_id INT,
    result_y DOUBLE PRECISION NOT NULL,

    FOREIGN KEY (point1_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (point2_id) REFERENCES function_points(id) ON DELETE CASCADE
);