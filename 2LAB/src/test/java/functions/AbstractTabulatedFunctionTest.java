package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractTabulatedFunctionTest {

    @Test
    // Проверка исключения когда длины массивов разные (выполнится)
    void testCheckLengthIsTheSame_ThrowException() {
        double[] x = {2.0, 3.0, 4.0};
        double[] y = {1.0, 1.5, 2.0, 10.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
        // Проверка исключения когда длины массивов разные (не выполнится)
    void testCheckLengthIsTheSame_NotThrowException() {
        double[] x = {2.0, 3.0, 4.0};
        double[] y = {1.0, 1.5, 2.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
        // Проверка исключения когда массив не отсортирован (выполнится)
    void testCheckSortedArray_NotSorted() {
        double[] x = {10.0, 3.0, 4.0};
        assertThrows(ArrayIsNotSortedException.class, () -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
        // Проверка исключения когда массив не отсортирован (не выполнится)
    void testCheckSortedArray_ActuallySorted() {
        double[] x = {1.0, 3.0, 9.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(x));
    }
    @Test
        // Проверка исключения когда массив не отсортирован (выполнится, есть повторения)
    void testCheckSortedArray_SortedWithRepeat() {
        double[] x = {10.0, 10.0, 20.0};
        assertThrows(ArrayIsNotSortedException.class, () -> AbstractTabulatedFunction.checkSorted(x));
    }
}