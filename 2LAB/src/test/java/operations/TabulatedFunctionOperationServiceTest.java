package operations;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionOperationServiceTest {
    @Test
    // Проверка с array
    public void testAsPointsWithArrayTabulatedFunction() {
        double[] xValues = {-4.0, -3.0, -2.0, -1.0};
        double[] yValues = {11.0, 22.0, 33.0, 44.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(xValues, yValues);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(4, points.length);

        // Проверяем точки
        for (int i = 0; i < points.length; i++) {
            assertEquals(xValues[i], points[i].x, 1e-10);
            assertEquals(yValues[i], points[i].y, 1e-10);
        }
    }
    @Test
    // Проверка с List
    public void testAsPointsWithListTabulatedFunction() {
        double[] xValues = {-4.0, -3.0, -2.0, -1.0, 0.0};
        double[] yValues = {11.0, 22.0, 33.0, 44.0, 55.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(xValues, yValues);

        Point[] points = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(5, points.length);

        // Проверяем точки
        for (int i = 0; i < points.length; i++) {
            assertEquals(xValues[i], points[i].x, 1e-10);
            assertEquals(yValues[i], points[i].y, 1e-10);
        }
    }
    @Test
    void testAsPointsWithNullFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            TabulatedFunctionOperationService.asPoints(null);
        });
    }
}