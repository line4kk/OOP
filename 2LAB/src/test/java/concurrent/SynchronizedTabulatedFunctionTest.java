package concurrent;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizedTabulatedFunctionTest {

    LinkedListTabulatedFunction linkedListFun = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{-2.0, -4.0, -6.0});
    SynchronizedTabulatedFunction syncLinkedListFun = new SynchronizedTabulatedFunction(linkedListFun);


    @Test
    void getCountSyncLinkedListFun() {
        assertEquals(3, syncLinkedListFun.getCount());

    }

    @Test
    void getXSyncLinkedListFun() {
        assertEquals(2.0, syncLinkedListFun.getX(1), 1e-10);

    }

    @Test
    void getYSyncLinkedListFun() {
        assertEquals(-6.0, syncLinkedListFun.getY(2), 1e-10);

    }

    @Test
    void setYSyncLinkedListFun() {
        syncLinkedListFun.setY(2, 6.0);
        assertEquals(6.0, syncLinkedListFun.getY(2), 1e-10);
    }

    @Test
    void indexOfXSyncLinkedListFun() {
        assertEquals(2, syncLinkedListFun.indexOfX(3.0));
    }

    @Test
    void indexOfYSyncLinkedListFun() {
        assertEquals(1, syncLinkedListFun.indexOfY(-4.0));
    }

    @Test
    void leftBoundSyncLinkedListFun() {
        assertEquals(1.0, syncLinkedListFun.leftBound(), 1e-10);
    }

    @Test
    void rightBoundSyncLinkedListFun() {
        assertEquals(3.0, syncLinkedListFun.rightBound(), 1e-10);

    }

    @Test
    void applySyncLinkedListFun() {
        assertEquals(0.0, syncLinkedListFun.apply(0.0), 1e-10);

    }

    @Test
    void iteratorSyncLinkedListFun() {
        Iterator<Point> iterator = syncLinkedListFun.iterator();
        assertEquals(-2.0, iterator.next().y, 1e-10);
        assertEquals(-4.0, iterator.next().y, 1e-10);
        assertEquals(-6.0, iterator.next().y, 1e-10);
    }

    ArrayTabulatedFunction arrayFun = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{-2.0, -4.0, -6.0});
    SynchronizedTabulatedFunction syncArrayFun = new SynchronizedTabulatedFunction(arrayFun);

    @Test
    void getCountSyncArrayFun() {
        assertEquals(3, syncArrayFun.getCount());

    }

    @Test
    void getXSyncArrayFun() {
        assertEquals(2.0, syncArrayFun.getX(1), 1e-10);

    }

    @Test
    void getYSyncArrayFun() {
        assertEquals(-6.0, syncArrayFun.getY(2), 1e-10);

    }

    @Test
    void setYSyncArrayFun() {
        syncArrayFun.setY(2, 6.0);
        assertEquals(6.0, syncArrayFun.getY(2), 1e-10);
    }

    @Test
    void indexOfXSyncArrayFun() {
        assertEquals(2, syncArrayFun.indexOfX(3.0));
    }

    @Test
    void indexOfYSyncArrayFun() {
        assertEquals(1, syncArrayFun.indexOfY(-4.0));
    }

    @Test
    void leftBoundSyncArrayFun() {
        assertEquals(1.0, syncArrayFun.leftBound(), 1e-10);
    }

    @Test
    void rightBoundSyncArrayFun() {
        assertEquals(3.0, syncArrayFun.rightBound(), 1e-10);

    }

    @Test
    void applySyncArrayFun() {
        assertEquals(0.0, syncArrayFun.apply(0.0), 1e-10);

    }

    @Test
    void iteratorSyncArrayFun() {
        Iterator<Point> iterator = syncArrayFun.iterator();
        assertEquals(-2.0, iterator.next().y, 1e-10);
        assertEquals(-4.0, iterator.next().y, 1e-10);
        assertEquals(-6.0, iterator.next().y, 1e-10);
    }
}