package operations;

import exceptions.InconsistentFunctionsException;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
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

    @Test
    public void testAddArrayFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {2.0, 4.0, 6.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.add(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(3.0, result.getY(0), 1e-10); // 2.0 + 1.0
        assertEquals(6.0, result.getY(1), 1e-10); // 4.0 + 2.0
        assertEquals(9.0, result.getY(2), 1e-10); // 6.0 + 3.0
    }

    @Test
    public void testSubtractLinkedListFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {2.0, 3.0, 4.0};

        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction result = service.subtract(func1, func2);

        assertEquals(3, result.getCount());
        assertEquals(3.0, result.getY(0), 1e-10); // 5.0 - 2.0
        assertEquals(7.0, result.getY(1), 1e-10); // 10.0 - 3.0
        assertEquals(11.0, result.getY(2), 1e-10); // 15.0 - 4.0
    }

    @Test
    public void testDifferentCountThrowsException() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.0};
        double[] yValues2 = {10.0, 20.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> service.add(func1, func2));
        assertThrows(InconsistentFunctionsException.class, () -> service.subtract(func1, func2));
    }

    @Test
    public void testDifferentXValuesThrowsException() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        double[] xValues2 = {1.0, 2.5, 3.0}; // разные x
        double[] yValues2 = {10.0, 20.0, 30.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> service.add(func1, func2));
    }

    @Test
    public void testDefaultConstructorUsesArrayFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testSetterChangesFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);

        service.setFactory(new LinkedListTabulatedFunctionFactory());
        assertTrue(service.getFactory() instanceof LinkedListTabulatedFunctionFactory);
    }

    @Test
    public void testFactorySetterWithSubtraction() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        // Проверяем вычитание с Array фабрикой
        service.setFactory(new ArrayTabulatedFunctionFactory());
        TabulatedFunction arrayResult = service.subtract(func1, func2);
        assertTrue(arrayResult instanceof ArrayTabulatedFunction);

        // Меняем на LinkedList фабрику и проверяем вычитание
        service.setFactory(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction listResult = service.subtract(func1, func2);
        assertTrue(listResult instanceof LinkedListTabulatedFunction);


    }

    @Test
    void testMultiplicationWithArrayTabulatedFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction multiplicationOfFunctions = service.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        multiplicationOfFunctions = anotherService.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

    }

    @Test
    void testMultiplicationWithLinkedListTabulatedFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction multiplicationOfFunctions = service.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        multiplicationOfFunctions = anotherService.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

    }

    @Test
    void testMultiplicationWithDifferentTabulatedFunctions1() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction multiplicationOfFunctions = service.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        multiplicationOfFunctions = anotherService.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

    }

    @Test
    void testMultiplicationWithDifferentTabulatedFunctions2() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 15.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction multiplicationOfFunctions = service.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        multiplicationOfFunctions = anotherService.multiplication(func1, func2);

        assertEquals(3, multiplicationOfFunctions.getCount());
        assertEquals(5.0, multiplicationOfFunctions.getY(0), 1e-10); // 5.0 * 1.0
        assertEquals(20.0, multiplicationOfFunctions.getY(1), 1e-10); // 10.0 * 2.0
        assertEquals(45.0, multiplicationOfFunctions.getY(2), 1e-10); // 15.0 * 3.0

    }

    @Test
    void testDevisionWithArrayTabulatedFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 18.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction devisionOfFunctions = service.devision(func1, func2);  // func1 / func2
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        devisionOfFunctions = anotherService.devision(func1, func2);

        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

    }

    @Test
    void testDevisionWithLinkedListTabulatedFunctions() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 18.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction devisionOfFunctions = service.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        devisionOfFunctions = anotherService.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

    }

    @Test
    void testDivisionWithDifferentTabulatedFunctions1() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 18.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        LinkedListTabulatedFunction func1 = new LinkedListTabulatedFunction(xValues1, yValues1);
        ArrayTabulatedFunction func2 = new ArrayTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction devisionOfFunctions = service.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        devisionOfFunctions = anotherService.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

    }

    @Test
    void testDivisionWithDifferentTabulatedFunctions2() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {5.0, 10.0, 18.0};
        double[] xValues2 = {1.0, 2.0, 3.0};
        double[] yValues2 = {1.0, 2.0, 3.0};

        ArrayTabulatedFunction func1 = new ArrayTabulatedFunction(xValues1, yValues1);
        LinkedListTabulatedFunction func2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction devisionOfFunctions = service.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

        TabulatedFunctionOperationService anotherService = new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        devisionOfFunctions = anotherService.devision(func1, func2);
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(5.0, devisionOfFunctions.getY(0), 1e-10); // 5.0 / 1.0
        assertEquals(5.0, devisionOfFunctions.getY(1), 1e-10); // 10.0 / 2.0
        assertEquals(6.0, devisionOfFunctions.getY(2), 1e-10); // 18.0 / 3.0

        devisionOfFunctions = service.devision(func2, func1);  // func2 / func1
        assertEquals(3, devisionOfFunctions.getCount());
        assertEquals(0.2, devisionOfFunctions.getY(0), 1e-4); // 1.0 / 5.0
        assertEquals(0.2, devisionOfFunctions.getY(1), 1e-4); // 2.0 / 10.0
        assertEquals(0.166666666, devisionOfFunctions.getY(2), 1e-4); // 3.0 / 18.0

    }
}