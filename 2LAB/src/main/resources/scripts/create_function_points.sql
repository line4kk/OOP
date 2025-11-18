CREATE TABLE function_points(
    id SERIAL PRIMARY KEY,
    function_id INT NOT NULL,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,

    UNIQUE(function_id, x_value),
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION delete_operation_result()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.y_value IS DISTINCT FROM NEW.y_value
    OR OLD.x_value IS DISTINCT FROM NEW.x_value
    THEN
        DELETE FROM operations_result_points
        WHERE point1_id = NEW.id OR point2_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_point_update
AFTER UPDATE ON function_points
FOR EACH ROW
EXECUTE FUNCTION delete_operation_result();