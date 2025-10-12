package functions;

import exceptions.InterpolationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable{

    private Node head = null;

    static class Node {
        public Node next, prev;
        public double x, y;
    }

    private void addNode(double x, double y){  // Добавить узел в конец списка
        if (head == null){
            head = new Node();
            head.next = head.prev = head;
            head.x = x;
            head.y = y;
        }
        else {
            Node node = new Node();
            node.x = x;
            node.y = y;
            node.next = head;
            node.prev = head.prev;
            head.prev.next = node;
            head.prev = node;
        }
        count++;
    }

    private Node getNode(int index){  // Найти ноду по индексу
        if (index < 0 || index >= count)
            throw new IllegalArgumentException();
        Node res = head;

        for (int i = 1; i <= index; i++)
            res = res.next;

        return res;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues){  // Задать функцию по спискам x, y
        if (xValues.length < 2)
            throw new IllegalArgumentException("Размер таблицы меньше минимального");

        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);

        for (int i = 0; i < xValues.length; i++){
            addNode(xValues[i], yValues[i]);
        }
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count){  // Задать функцию с помощью дискретизации
        if (count < 2)  // Должно быть хотя-бы 2 точки, иначе - размер таблицы будет меньше минимального
            throw new IllegalArgumentException("Количество точек меньше минимального");
        if (xFrom > xTo) {
            double xTemp = xFrom;
            xFrom = xTo;
            xTo = xTemp;
        }

        double segmentLength = xTo - xFrom;  // Длина всего отрезка
        double deltaX = segmentLength / (count - 1);  // Длина каждого маленького отрезка

        double xCurrent = xFrom;

        if (deltaX == 0){
            for (int i = 0; i < count; i++)
                addNode(xTo, source.apply(xTo));
        }
        else {
            for (int i = 0; i < count; i++){
                if (i == count - 1)
                    xCurrent = xTo;
                addNode(xCurrent, source.apply(xCurrent));
                xCurrent += deltaX;
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        int res = -1;
        Node node = head;

        for (int i = 0; i < count && res == -1; i++) {
            if (node.x == x)
                res = i;
            node = node.next;
        }
        return res;
    }

    @Override
    public int indexOfY(double y) {
        int res = -1;
        Node node = head;

        for (int i = 0; i < count && res == -1; i++) {
            if (node.y == y)
                res = i;
            node = node.next;
        }
        return res;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < head.x) {  // Если x меньше левой границы
            throw new IllegalArgumentException();
        }
        if (x > head.prev.x) {  // Если все элементы массива меньше x
            return count;
        }

        Node node = head;
        int result = 0;
        for (int i = 0; i < count; i++){
            if (node.x < x)
                result = i;
            node = node.next;
        }

        return result;
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node floor = getNode(floorIndex);
        Node ceil = getNode(floorIndex + 1);

        if (x < floor.x || x > ceil.x) {
            throw new InterpolationException("x не попадает в диапазон от " + floor.x + " до " + ceil.x);
        }

        double yFloor = floor.y;
        double xFloor = floor.x;
        double yCeil = ceil.y;
        double xCeil = ceil.x;

        return yFloor + (yCeil - yFloor) / (xCeil - xFloor) * (x - xFloor);
    }

    @Override
    protected double extrapolateLeft(double x) {
        Node floor = getNode(0);
        Node ceil = getNode(1);
        return interpolate(x, floor.x, ceil.x, floor.y, ceil.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        Node floor = getNode(count - 2);
        Node ceil = getNode(count - 1);
        return interpolate(x, floor.x, ceil.x, floor.y, ceil.y);
    }

    @Override
    public void insert(double x, double y) {

        // Проверяем, существует ли уже узел с таким x
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.x - x) < 1e-12) {
                current.y = y;
                return;
            }
            current = current.next;
        }

        // Ищем место для вставки
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        // Случай 1: Вставка в начало
        if (x < head.x) {
            newNode.next = head;
            newNode.prev = head.prev;
            head.prev.next = newNode;
            head.prev = newNode;
            head = newNode;
            count++;
            return;
        }

        // Случай 2: Вставка в конец
        if (x > head.prev.x) {
            addNode(x, y);
            return;
        }

        // Случай 3: Вставка в середину
        current = head;
        for (int i = 0; i < count; i++) {
            if (x > current.x && x < current.next.x) {
                newNode.next = current.next;
                newNode.prev = current;
                current.next.prev = newNode;
                current.next = newNode;
                count++;
                return;
            }
            current = current.next;
        }
    }

    @Override
    public void remove(int index) {
        if (count == 1) {  // Если в списке только один узел
            head = null;
            count = 0;
            return;
        }

        Node nodeToRemove = getNode(index);

        if (index == 0) {  // Если удаляем головной узел
            head = head.next;
        }

        nodeToRemove.prev.next = nodeToRemove.next;
        nodeToRemove.next.prev = nodeToRemove.prev;

        count--;
    }

    @Override
    public Iterator<Point> iterator() {  // Объект итератора
        Iterator<Point> iter = new Iterator<Point>() {
            Node node = head;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public Point next() {
                if (!hasNext())  // Если следующего элемента нет, то выбрасываем исключение
                    throw new NoSuchElementException();
                else {
                    Point point = new Point(node.x, node.y);
                    // Переходим на следующий элемент или на null
                    if (node.next == head)
                        node = null;
                    else
                        node = node.next;
                    return point;
                }
            }
        };

        return iter;

    }
}
