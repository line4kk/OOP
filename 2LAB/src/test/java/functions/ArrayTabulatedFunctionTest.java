package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionTest {
    // Тесты конструкторов
    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(5, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(5.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testConstructorWithMathFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 0.0, 4.0, 5);

        assertEquals(5, func.getCount());
        assertEquals(0.0, func.getX(0), 1e-10);
        assertEquals(4.0, func.getX(4), 1e-10);
        assertEquals(0.0, func.getY(0), 1e-10);
        assertEquals(16.0, func.getY(4), 1e-10);
    }

    @Test
    public void testConstructorWithSwappedBounds() {
        MathFunction source = x -> x + 1;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 5.0, 1.0, 5);

        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(5.0, func.getX(4), 1e-10);
    }

    @Test
    public void testConstructorWithEqualBounds() {
        MathFunction source = x -> x * 2;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 3.0, 3.0, 4);

        assertEquals(3.0, func.getX(0), 1e-10);
        assertEquals(3.0, func.getX(3), 1e-10);
        assertEquals(6.0, func.getY(0), 1e-10);
        assertEquals(6.0, func.getY(3), 1e-10);
    }

    // Тесты методов доступа
    @Test
    public void testGetCount() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3, function.getCount());
    }

    @Test
    public void testGetX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
    }

    @Test
    public void testGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.getY(0), 1e-10);
        assertEquals(5.0, function.getY(1), 1e-10);
        assertEquals(6.0, function.getY(2), 1e-10);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 15.0);
        assertEquals(15.0, function.getY(1), 1e-10);
    }

    // Тесты поиска
    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(1.5));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(2.0));
        assertEquals(2, function.indexOfY(6.0));
        assertEquals(-1, function.indexOfY(5.0));
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(0.5));   // Меньше всех
        assertEquals(0, function.floorIndexOfX(1.0));   // Равно первому
        assertEquals(0, function.floorIndexOfX(1.5));   // Между 1 и 2
        assertEquals(1, function.floorIndexOfX(2.5));   // Между 2 и 3
        assertEquals(3, function.floorIndexOfX(4.5));   // Между 4 и 5
        assertEquals(4, function.floorIndexOfX(5.0));   // Равно последнему
        assertEquals(5, function.floorIndexOfX(6.0));   // Больше всех
    }

    // Тесты границ
    @Test
    public void testLeftBound() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.leftBound(), 1e-10);
    }

    @Test
    public void testRightBound() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    // Тесты интерполяции и экстраполяции
    @Test
    public void testExtrapolateLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.extrapolateLeft(0.0);
        double expected = 0.0; // Экстраполяция слева: y = 2x
        assertEquals(expected, result, 1e-10);
    }

    @Test
    public void testExtrapolateRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.extrapolateRight(4.0);
        double expected = 8.0; // Экстраполяция справа: y = 2x
        assertEquals(expected, result, 1e-10);
    }

    @Test
    public void testInterpolateWithIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.interpolate(1.5, 0); // Между 1.0 и 2.0
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testApplyWithExactX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(8.0, function.apply(4.0), 1e-10);
    }

    @Test
    public void testApplyWithInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3.0, function.apply(1.5), 1e-10); // Между 1 и 2
        assertEquals(5.0, function.apply(2.5), 1e-10); // Между 2 и 3
        assertEquals(7.0, function.apply(3.5), 1e-10); // Между 3 и 4
    }

    @Test
    public void testApplyWithExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-10);  // Слева
        assertEquals(8.0, function.apply(4.0), 1e-10); // Справа
    }

    @Test
    public void testInterpolateMethod() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.interpolate(1.5, 1.0, 2.0, 2.0, 4.0);
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testLinearFunction() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 2.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(1.5, function.apply(1.5), 1e-10);
        assertEquals(-0.5, function.apply(-0.5), 1e-10);
        assertEquals(2.5, function.apply(2.5), 1e-10);
    }

    @Test
    public void testSquareFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 0.0, 2.0, 3);

        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(1.0, function.getY(1), 1e-10);
        assertEquals(4.0, function.getY(2), 1e-10);
    }
    // ---------------------------------
    // ---------------------------------
    // Новые тесты со сложными функциями
    // ---------------------------------
    // ---------------------------------
    @Test
    public void testArrayTabulatedWithCompositeFunctionBasic() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 2.0, 3.0, 4.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction f = x -> x + 1;
        MathFunction g = x -> x * 2;
        CompositeFunction compositeFG = new CompositeFunction(f, g);

        // Композиция: compositeFG(tabulated(x)) = g(f(tabulated(x)))
        MathFunction result = tabulated.andThen(compositeFG);

        assertEquals(6.0, result.apply(1.0), 1e-10);  // tabulated(1)=2, f(2)=3, g(3)=6
        assertEquals(8.0, result.apply(2.0), 1e-10); // tabulated(2)=3, f(3)=4, g(4)=8
    }

    @Test
    public void testCompositeFunctionWithArrayTabulated() {
        MathFunction f = x -> x * 3;
        MathFunction g = x -> x - 1;
        CompositeFunction composite = new CompositeFunction(f, g);

        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {5.0, 10.0, 15.0, 20.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        // Композиция: tabulated(composite(x)) = tabulated(g(f(x)))
        MathFunction result = composite.andThen(tabulated);

        // composite(1)=g(f(1))=g(3)=2, tabulated(2)=10
        assertEquals(10.0, result.apply(1.0), 1e-10);
        // composite(2)=g(f(2))=g(6)=5, tabulated(5)=25 (экстраполяция)
        assertEquals(25.0, result.apply(2.0), 1e-10);
    }

    @Test
    public void testNestedCompositeFunctionsWithArrayTabulated() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {1.0, 3.0, 5.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction f = x -> x + 1;
        MathFunction g = x -> x * 2;
        MathFunction h = x -> x - 1;

        // Создаем вложенные композитные функции
        CompositeFunction composite1 = new CompositeFunction(f, g);  // g(f(x)) = 2*(x+1)
        CompositeFunction composite2 = new CompositeFunction(composite1, h); // h(g(f(x))) = 2*(x+1) - 1

        // Композиция: composite2(tabulated(x)) = h(g(f(tabulated(x))))
        MathFunction result = tabulated.andThen(composite2);

        // tabulated(0)=1, f(1)=2, g(2)=4, h(4)=3
        assertEquals(3.0, result.apply(0.0), 1e-10);
        // tabulated(1)=3, f(3)=4, g(4)=8, h(8)=7
        assertEquals(7.0, result.apply(1.0), 1e-10);
    }

    @Test
    public void testArrayTabulatedBetweenCompositeFunctions() {
        MathFunction f = x -> x * 2;
        MathFunction g = x -> x + 3;
        CompositeFunction firstComposite = new CompositeFunction(f, g);  // g(f(x)) = 2x + 3

        double[] xValues = {1.0, 3.0, 5.0, 7.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction h = x -> x / 2;
        MathFunction i = x -> x - 1;
        CompositeFunction secondComposite = new CompositeFunction(h, i);  // i(h(x)) = x/2 - 1

        // Цепочка: firstComposite -> tabulated -> secondComposite
        MathFunction chain = firstComposite.andThen(tabulated).andThen(secondComposite);

        // firstComposite(1)=5, tabulated(5)=6, secondComposite(6)=2
        assertEquals(2.0, chain.apply(1.0), 1e-10);
        // firstComposite(2)=7, tabulated(7)=8, secondComposite(8)=3
        assertEquals(3.0, chain.apply(2.0), 1e-10);
    }

    @Test
    public void testMultipleCompositeFunctionsChain() {
        double[] xValues = {0.0, 2.0, 4.0};
        double[] yValues = {1.0, 5.0, 9.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        // Первая композитная функция
        MathFunction a = x -> x + 1;
        MathFunction b = x -> x * 3;
        CompositeFunction comp1 = new CompositeFunction(a, b);  // b(a(x)) = 3*(x+1)

        // Вторая композитная функция
        MathFunction c = x -> x - 2;
        MathFunction d = x -> x / 2;
        CompositeFunction comp2 = new CompositeFunction(c, d);  // d(c(x)) = (x-2)/2

        // Третья композитная функция
        MathFunction e = x -> x * x;
        MathFunction f = x -> x + 10;
        CompositeFunction comp3 = new CompositeFunction(e, f);  // f(e(x)) = x² + 10

        // Сложная цепочка: comp1 -> tabulated -> comp2 -> comp3
        MathFunction complexChain = comp1.andThen(tabulated).andThen(comp2).andThen(comp3);

        // comp1(1)=6, tabulated(6)=13 (экстраполяция), comp2(13)=5.5, comp3(5.5)=40.25
        assertEquals(40.25, complexChain.apply(1.0), 1e-10);
    }

    @Test
    public void testCompositeFunctionWithArrayTabulatedInterpolation() {
        MathFunction f = x -> x * 2;
        MathFunction g = x -> x - 1;
        CompositeFunction composite = new CompositeFunction(f, g);  // g(f(x)) = 2x - 1

        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {2.0, 6.0, 10.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        // Композиция: tabulated(composite(x))
        MathFunction result = composite.andThen(tabulated);

        // composite(1.5)=2, tabulated(2)=4 (интерполяция между 1 и 3)
        assertEquals(4.0, result.apply(1.5), 1e-10);
        // composite(2.5)=4, tabulated(4)=8 (интерполяция между 3 и 5)
        assertEquals(8.0, result.apply(2.5), 1e-10);
    }

    @Test
    public void testCompositeFunctionWithArrayTabulatedExtrapolation() {
        MathFunction f = x -> x + 5;
        MathFunction g = x -> x * 2;
        CompositeFunction composite = new CompositeFunction(f, g);  // g(f(x)) = 2*(x+5)

        double[] xValues = {10.0, 20.0, 30.0};
        double[] yValues = {1.0, 2.0, 3.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        // Композиция: tabulated(composite(x))
        MathFunction result = composite.andThen(tabulated);

        // composite(0)=10, tabulated(10)=1
        assertEquals(1.0, result.apply(0.0), 1e-10);
        // composite(20)=50, tabulated(50)=5 (экстраполяция справа)
        assertEquals(5.0, result.apply(20.0), 1e-10);
    }

    @Test
    public void testArrayTabulatedAsInnerFunctionInComposite() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 8.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction f = x -> x + 1;

        // Создаем композитную функцию, где tabulated - внутренняя функция
        CompositeFunction composite = new CompositeFunction(tabulated, f);  // f(tabulated(x))

        assertEquals(3.0, composite.apply(1.0), 1e-10);  // tabulated(1)=2, f(2)=3
        assertEquals(5.0, composite.apply(2.0), 1e-10);  // tabulated(2)=4, f(4)=5
        assertEquals(9.0, composite.apply(3.0), 1e-10);  // tabulated(3)=8, f(8)=9
    }

    @Test
    public void testArrayTabulatedAsOuterFunctionInComposite() {
        MathFunction f = x -> x * 2;
        double[] xValues = {2.0, 4.0, 6.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        // Создаем композитную функцию, где tabulated - внешняя функция
        CompositeFunction composite = new CompositeFunction(f, tabulated);  // tabulated(f(x))

        assertEquals(10.0, composite.apply(1.0), 1e-10);  // f(1)=2, tabulated(2)=10
        assertEquals(20.0, composite.apply(2.0), 1e-10);  // f(2)=4, tabulated(4)=20
        assertEquals(25.0, composite.apply(2.5), 1e-10);  // f(2.5)=5, tabulated(5)=25 (интерполяция)
    }

    @Test
    public void testComplexNestedCompositesWithMultipleArrayTabulated() {
        // Первая табличная функция
        double[] xValues1 = {0.0, 3.0, 6.0};
        double[] yValues1 = {1.0, 3.0, 5.0};
        ArrayTabulatedFunction tabulated1 = new ArrayTabulatedFunction(xValues1, yValues1);

        // Вторая табличная функция
        double[] xValues2 = {1.0, 4.0, 7.0};
        double[] yValues2 = {2.0, 8.0, 14.0};
        ArrayTabulatedFunction tabulated2 = new ArrayTabulatedFunction(xValues2, yValues2);

        // Композитные функции
        MathFunction f = x -> x + 2;
        MathFunction g = x -> x * 3;
        CompositeFunction comp1 = new CompositeFunction(f, g);  // g(f(x)) = 3*(x+2)

        MathFunction h = x -> x - 1;
        MathFunction i = x -> x / 2;
        CompositeFunction comp2 = new CompositeFunction(h, i);  // i(h(x)) = (x-1)/2

        // Сложная цепочка: comp1 -> tabulated1 -> comp2 -> tabulated2
        MathFunction complexChain = comp1.andThen(tabulated1).andThen(comp2).andThen(tabulated2);

        // comp1(0)=6, tabulated1(6)=9 (экстраполяция), comp2(9)=4, tabulated2(4)=8
        assertEquals(4.0, complexChain.apply(0.0), 1e-10);
    }

    @Test
    public void testCompositeFunctionWithArrayTabulatedAndConstant() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {5.0, 10.0, 15.0};
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(xValues, yValues);

        ConstantFunction constant = new ConstantFunction(7.0);
        MathFunction f = x -> x * 2;

        CompositeFunction composite = new CompositeFunction(constant, f);  // f(constant(x)) = 14 (всегда)

        // Композиция: composite(tabulated(x)) = f(constant(tabulated(x))) = 14 (всегда)
        MathFunction result = tabulated.andThen(composite);

        assertEquals(14.0, result.apply(1.0), 1e-10);
        assertEquals(14.0, result.apply(2.0), 1e-10);
        assertEquals(14.0, result.apply(1.5), 1e-10);
    }

    @Test
    public void testArrayTabulatedInBothPartsOfComposite() {
        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction tabulated1 = new ArrayTabulatedFunction(xValues1, yValues1);

        double[] xValues2 = {2.0, 4.0, 6.0};
        double[] yValues2 = {1.0, 3.0, 5.0};
        ArrayTabulatedFunction tabulated2 = new ArrayTabulatedFunction(xValues2, yValues2);

        // Композитная функция из двух табличных функций
        CompositeFunction composite = new CompositeFunction(tabulated1, tabulated2);  // tabulated2(tabulated1(x))

        assertEquals(1.0, composite.apply(1.0), 1e-10);  // tabulated1(1)=2, tabulated2(2)=1
        assertEquals(3.0, composite.apply(2.0), 1e-10);  // tabulated1(2)=4, tabulated2(4)=3
        assertEquals(4.0, composite.apply(2.5), 1e-10);  // tabulated1(2.5)=5, tabulated2(5)=4 (интерполяция)
    }

    @Test
    public void testComplexMixedCompositions() {
        // Табличная функция из дискретизации
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction tabulated = new ArrayTabulatedFunction(source, 0.0, 12.0, 13);

        // Композитные функции
        MathFunction f = x -> x + 2;
        MathFunction g = x -> x * 3;
        CompositeFunction comp1 = new CompositeFunction(f, g);  // g(f(x)) = 3*(x+2)

        SqrFunction sqr = new SqrFunction();
        ConstantFunction constant = new ConstantFunction(5.0);
        CompositeFunction comp2 = new CompositeFunction(sqr, constant);  // constant(sqr(x)) = 5 (всегда)

        // Смешанная цепочка: comp1 -> tabulated -> comp2
        MathFunction chain = comp1.andThen(tabulated).andThen(comp2);

        // Всегда возвращает 5, так как comp2 всегда возвращает 5
        assertEquals(5.0, chain.apply(0.0), 1e-10);
        assertEquals(5.0, chain.apply(1.0), 1e-10);
        assertEquals(5.0, chain.apply(2.0), 1e-10);


    }

    @Test
    void testInsertNewPointInMiddle() {
        double[] xValues = {1.0, 2.0, 4.0};
        double[] yValues = {10.0, 20.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(3.0, 30.0);

        assertEquals(4, function.getCount());
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
    }

    @Test
    void testInsertExistingPoint() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 999.0);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(999.0, function.getY(1), 1e-10);
    }

    @Test
    void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0};
        double[] yValues = {20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(1.0, 10.0);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
    }
    //---------------------
    //---------------------
    // Тесты для remove
    //---------------------
    //---------------------
    @Test
    public void testRemoveFirstElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(20.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);
        assertEquals(2.0, function.leftBound(), 1e-10);
        assertEquals(4.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testRemoveLastElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(3);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
        assertEquals(30.0, function.getY(2), 1e-10);
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testRemoveMiddleElement() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(1);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(4.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(40.0, function.getY(2), 1e-10);
    }

    @Test
    public void testRemoveMultipleElements() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(1); // Удаляем элемент 2.0
        function.remove(2); // Теперь удаляем элемент 4.0 (индекс изменился)

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(3.0, function.getX(1), 1e-10);
        assertEquals(5.0, function.getX(2), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(30.0, function.getY(1), 1e-10);
        assertEquals(50.0, function.getY(2), 1e-10);
    }
    @Test
        // Проверка исключения когда длины массивов разные (выполнится)
    void testCheckLengthIsTheSameArray_ThrowException() {
        double[] x = {2.0, 3.0, 4.0};
        double[] y = {1.0, 1.5, 2.0, 10.0};
        String message = "Has Error";
        DifferentLengthOfArraysException exception1 = new DifferentLengthOfArraysException();
        DifferentLengthOfArraysException exception2 = new DifferentLengthOfArraysException(message);

        assertThrows(exception1.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertThrows(exception2.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertEquals(message, exception2.getMessage());
    }

    @Test
        // Проверка исключения когда длины массивов разные (не выполнится)
    void testCheckLengthIsTheSameArray_NotThrowException() {
        double[] x = {2.0, 3.0, 4.0};
        double[] y = {1.0, 1.5, 2.0};
        assertDoesNotThrow(() -> new ArrayTabulatedFunction(x, y));
    }

    @Test
        // Проверка исключения когда массив не отсортирован (выполнится)
    void testCheckSortedArray1_NotSorted() {
        double[] x = {10.0, 3.0, 4.0};
        double[] y = {1.0, 1.5, 2.0};
        String message = "Has Error";
        ArrayIsNotSortedException exception1 = new ArrayIsNotSortedException();
        ArrayIsNotSortedException exception2 = new ArrayIsNotSortedException(message);

        assertThrows(exception1.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertThrows(exception2.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertEquals(message, exception2.getMessage());
    }

    @Test
        // Проверка исключения когда массив не отсортирован (не выполнится)
    void testCheckSortedArray2_ActuallySorted() {
        double[] x = {1.0, 3.0, 9.0};
        double[] y = {1.0, 1.5, 2.0};
        assertDoesNotThrow(() -> new ArrayTabulatedFunction(x, y));
    }
    @Test
        // Проверка исключения когда массив не отсортирован (выполнится, есть повторения)
    void testCheckSortedArray3_SortedWithRepeat() {
        double[] x = {10.0, 10.0, 20.0};
        double[] y = {1.0, 1.5, 2.0};
        String message = "Has Error";
        ArrayIsNotSortedException exception1 = new ArrayIsNotSortedException();
        ArrayIsNotSortedException exception2 = new ArrayIsNotSortedException(message);

        assertThrows(exception1.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertThrows(exception2.getClass(), () -> new ArrayTabulatedFunction(x, y));
        assertEquals(message, exception2.getMessage());
    }

    @Test
    // Проверка исключения при интерполяции
    void testInterpolationOutOfArray() {
        double[] x = {0.0, 4.0, 8.0, 9.0};
        double[] y = {1.0, 2.0, 3.0, 4.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        String message = "Has Error";
        InterpolationException exception1 = new InterpolationException();
        InterpolationException exception2 = new InterpolationException(message);

        assertThrows(exception1.getClass(), () -> func.interpolate(0.5, 1));
        assertThrows(exception1.getClass(), () -> func.interpolate(10.0, 2));

        assertThrows(exception2.getClass(), () -> func.interpolate(0.5, 1));
        assertThrows(exception2.getClass(), () -> func.interpolate(10.0, 2));
        assertEquals(message, exception2.getMessage());
    }

    @Test
            // Тест итератора в массивах с помощью цикла while
    public void testArrayTabulatedFunctionIterator1() {
        double[] xValues = {0.0, 3.0, 6.0, 9.0, 12.0};
        double[] yValues = {0.0, -1.0, -2.0, -3.0, -4.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = f.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);
    }

    @Test
    // Тест итератора в массивах с помощью цикла for-each
    public void testArrayTabulatedFunctionIterator2() {
        double[] xValues = {0.0, 10.0, 20.0, 30.0, 40.0};
        double[] yValues = {0.0, -2.0, -4.0, -6.0, -8.0};
        ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(xValues, yValues);

        int index = 0;
        for (Point point : tabulatedFunction) {
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);
    }

}
