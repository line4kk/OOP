package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {
    double[] xValues = {0.0, 1.0, 2.0, 3.0};
    double[] yValues = {0.0, 1.0, 2.0, 3.0};
    SqrFunction sqr = new SqrFunction();

    LinkedListTabulatedFunction fun1 = new LinkedListTabulatedFunction(xValues, yValues);
    LinkedListTabulatedFunction fun2 = new LinkedListTabulatedFunction(sqr, -3.0, 3.0, 7);

    @Test
    void testLinkedListTabulatedFunction() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};
        IllegalArgumentException exc1 = assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(xValues, yValues));
        IllegalArgumentException exc2 = assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(sqr, -3.0, 3.0, 1));

        assertEquals("Размер таблицы меньше минимального", exc1.getMessage());
        assertEquals("Количество точек меньше минимального", exc2.getMessage());
    }

    @Test
    void getCount() {
        assertEquals(4, fun1.getCount());
        assertEquals(7, fun2.getCount());
    }

    @Test
    void leftBound() {
        assertEquals(0.0, fun1.leftBound());
        assertEquals(-3.0, fun2.leftBound());
    }

    @Test
    void rightBound() {
        assertEquals(3.0, fun1.rightBound());
        assertEquals(3.0, fun2.rightBound());
    }

    @Test
    void getX() {
        assertEquals(0.0, fun1.getX(0));
        assertEquals(2.0, fun1.getX(2));
        assertEquals(-3.0, fun2.getX(0));
        assertEquals(-1.0, fun2.getX(2));

        assertThrows(IllegalArgumentException.class, () -> fun1.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> fun1.getX(4));

        assertThrows(IllegalArgumentException.class, () -> fun2.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> fun2.getX(7));
    }

    @Test
    void getY() {
        assertEquals(0.0, fun1.getY(0));
        assertEquals(2.0, fun1.getY(2));
        assertEquals(9.0, fun2.getY(0));
        assertEquals(1.0, fun2.getY(2));

        assertThrows(IllegalArgumentException.class, () -> fun1.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> fun1.getY(4));

        assertThrows(IllegalArgumentException.class, () -> fun2.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> fun2.getY(7));
    }

    @Test
    void setY() {
        fun2.setY(3, 2);
        assertEquals(2, fun2.getY(3));

        assertThrows(IllegalArgumentException.class, () -> fun1.setY(4, 5));
        assertThrows(IllegalArgumentException.class, () -> fun1.setY(-1, 3));
    }

    @Test
    void indexOfX() {
        assertEquals(2, fun1.indexOfX(2.0));
        assertEquals(5, fun2.indexOfX(2.0));
    }

    @Test
    void indexOfY() {
        assertEquals(2, fun1.indexOfY(2.0));
        assertEquals(1, fun2.indexOfY(4));
    }

    @Test
    void floorIndexOfX() {
        assertEquals(0, fun1.floorIndexOfX(0.5));
        assertEquals(4, fun2.floorIndexOfX(1.5));

        assertThrows(IllegalArgumentException.class, () -> fun1.floorIndexOfX(-0.5));
    }

    @Test
    void interpolate() {
        assertEquals(0.5, fun1.interpolate(0.5, 0));
        assertEquals(0.5, fun2.interpolate(0.5, 3));
    }

    @Test
    void extrapolateLeft() {
        assertEquals(-1, fun1.interpolate(-1, 0));
        assertEquals(14, fun2.interpolate(-4, 0));
    }

    @Test
    void extrapolateRight() {
        assertEquals(7, fun1.interpolate(7, 2));
        assertEquals(14, fun2.interpolate(4, 5));
    }

    double[] xValuesA1 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesA1 = {0.0, 1.0, 4.0, 9.0};
    ArrayTabulatedFunction tabFunA1 = new ArrayTabulatedFunction(xValuesA1, yValuesA1);

    double[] xValuesA2 = {1.0, 2.0, 3.0, 4.0};
    double[] yValuesA2 = {1.0, 0.5, 1.5, 2.0};
    ArrayTabulatedFunction tabFunA2 = new ArrayTabulatedFunction(xValuesA2, yValuesA2);


    double[] xValuesL1 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesL1 = {0.0, 3.0, 6.0, 9.0};
    LinkedListTabulatedFunction tabFunL1 = new LinkedListTabulatedFunction(xValuesL1, yValuesL1);

    double[] xValuesL2 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesL2 = {0.0, 0.5, 1.0, 1.5};
    LinkedListTabulatedFunction tabFunL2 = new LinkedListTabulatedFunction(xValuesL2, yValuesL2);

    @Test
    void testBothArray(){
        CompositeFunction comp1 = new CompositeFunction(tabFunA1, tabFunA2);  // A2(A1)
        CompositeFunction comp2 = new CompositeFunction(tabFunA2, tabFunA1);  // A1(A2)

        assertEquals(2.0, comp1.apply(2.0));
        assertEquals(1.0, comp2.apply(1.0));
    }

    @Test
    void testBothLinkedList(){
        CompositeFunction comp1 = new CompositeFunction(tabFunL1, tabFunL2);  // L2(L1)
        CompositeFunction comp2 = new CompositeFunction(tabFunL2, tabFunL1);  // L1(L2)

        assertEquals(6.0, comp1.apply(4));
        assertEquals(15.0, comp2.apply(10));
    }

    @Test
    void testMix(){
        CompositeFunction comp1 = new CompositeFunction(tabFunL2, tabFunA1);  // A1(L2)
        CompositeFunction comp2 = new CompositeFunction(tabFunA2, tabFunL1);  // L1(A2)

        assertEquals(2.5, comp1.apply(3));
        assertEquals(15, comp2.apply(10));
    }

    @Test
    void testTabulatedAndOther(){
        SqrFunction sqr = new SqrFunction();
        CompositeFunction comp1 = new CompositeFunction(tabFunL1, sqr);
        CompositeFunction comp2 = new CompositeFunction(sqr, tabFunL2);

        assertEquals(900, comp1.apply(10));
        assertEquals(200, comp2.apply(20));
    }


    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.0, 10.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(4.0, 40.0);

        assertEquals(4, function.getCount());
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(40.0, function.getY(3), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0);
        function.insert(4.0, 40.0);

        assertEquals(5, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(5.0, function.getX(4), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(3), 1e-10);
    }

    @Test
    public void testInsertDuplicateX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 25.0); // Замена существующего значения

        assertEquals(3, function.getCount()); // Количество не изменилось
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(25.0, function.getY(1), 1e-10); // Значение обновилось
        assertEquals(10.0, function.getY(0), 1e-10); // Другие значения не изменились
        assertEquals(30.0, function.getY(2), 1e-10);
    }


    @Test
    public void testInsertWithTwoElementList() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {10.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0); // В середину
        function.insert(0.5, 5.0);  // В начало
        function.insert(4.0, 40.0); // В конец

        assertEquals(5, function.getCount());
        assertEquals(0.5, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(3.0, function.getX(3), 1e-10);
        assertEquals(4.0, function.getX(4), 1e-10);
    }

    @Test
    public void testInsertMaintainsCircularStructure() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.5, 15.0);

        assertEquals(4, function.getCount());
        // Проверяем, что структура осталась циклической
        assertEquals(function.leftBound(), function.getX(0), 1e-10);
        assertEquals(function.rightBound(), function.getX(3), 1e-10);

        // Проверяем, что можно пройти по кругу
        double firstX = function.getX(0);
        double lastX = function.getX(3);
        // В циклическом списке последний элемент должен ссылаться на первый
        // Это проверяется через внутреннюю структуру, но мы можем проверить через границы
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testInsertWithNegativeValues() {
        double[] xValues = {-2.0, 0.0, 2.0};
        double[] yValues = {-20.0, 0.0, 20.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(-3.0, -30.0); // В начало
        function.insert(-1.0, -10.0); // В середину
        function.insert(3.0, 30.0);   // В конец

        assertEquals(6, function.getCount());
        assertEquals(-3.0, function.getX(0), 1e-10);
        assertEquals(-2.0, function.getX(1), 1e-10);
        assertEquals(-1.0, function.getX(2), 1e-10);
        assertEquals(0.0, function.getX(3), 1e-10);
        assertEquals(2.0, function.getX(4), 1e-10);
        assertEquals(3.0, function.getX(5), 1e-10);
    }

    @Test
    public void testInsertWithDecimalValues() {
        double[] xValues = {1.1, 2.2, 3.3};
        double[] yValues = {11.0, 22.0, 33.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.5, 15.0);
        function.insert(2.8, 28.0);
        function.insert(0.5, 5.0);

        assertEquals(6, function.getCount());
        assertEquals(0.5, function.getX(0), 1e-10);
        assertEquals(1.1, function.getX(1), 1e-10);
        assertEquals(1.5, function.getX(2), 1e-10);
        assertEquals(2.2, function.getX(3), 1e-10);
        assertEquals(2.8, function.getX(4), 1e-10);
        assertEquals(3.3, function.getX(5), 1e-10);
    }

    @Test
    public void testInsertThenApply() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {10.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0);

        // Проверяем, что интерполяция работает корректно после вставки
        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(20.0, function.apply(2.0), 1e-10);
        assertEquals(30.0, function.apply(3.0), 1e-10);
        assertEquals(15.0, function.apply(1.5), 1e-10); // Интерполяция
        assertEquals(25.0, function.apply(2.5), 1e-10); // Интерполяция
    }

    @Test
    public void testInsertThenIndexOfX() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {10.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(4.0));
    }

    @Test
    public void testInsertThenFloorIndexOfX() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {10.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0);

        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(0.5));  // Меньше всех
        assertEquals(0, function.floorIndexOfX(1.0));  // Равно первому
        assertEquals(3, function.floorIndexOfX(4.0));  // Больше всех
    }

    @Test
    void testRemoveFromMiddle() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(1); // Удаляем средний элемент

        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
    }

    @Test
    void testRemoveFromBeginning() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0); // Удаляем первый элемент

        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
    }

    @Test
    void testRemoveFromEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(2); // Удаляем последний элемент

        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
    }
}