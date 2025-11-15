package functions;

import exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunction.class);
    private Node head = null;

    @Serial
    private final static long serialVersionUID = -3800647710626336537L;

    static class Node implements Serializable {
        @Serial
        private final static long serialVersionUID = -4823301658934728089L;
        public Node next, prev;
        public double x, y;
    }

    private void addNode(double x, double y){  // Добавить узел в конец списка
        if (head == null){
            head = new Node();
            head.next = head.prev = head;
            head.x = x;
            head.y = y;
            logger.debug("Создана первая нода: ({}, {})", x, y);
        }
        else {
            Node node = new Node();
            node.x = x;
            node.y = y;
            node.next = head;
            node.prev = head.prev;
            head.prev.next = node;
            head.prev = node;
            logger.debug("Добавлена нода: ({}, {})", x, y);
        }
        count++;
    }

    private Node getNode(int index) {  // Найти ноду по индексу
        if (index < 0 || index >= count) {
            logger.error("Попытка получить ноду по недопустимому индексу: {}", index);
            throw new IllegalArgumentException();
        }
        Node res = head;

        for (int i = 1; i <= index; i++)
            res = res.next;

        return res;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues){  // Задать функцию по спискам x, y
        if (xValues.length < 2) {
            logger.error("Попытка создать функцию с {} точками (требуется минимум 2)", xValues.length);
            throw new IllegalArgumentException("Размер таблицы меньше минимального");
        }

        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);

        for (int i = 0; i < xValues.length; i++){
            addNode(xValues[i], yValues[i]);
        }
        logger.info("Создан LinkedListTabulatedFunction с {} точками", count);
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count){  // Задать функцию с помощью дискретизации
        if (count < 2) {// Должно быть хотя-бы 2 точки, иначе - размер таблицы будет меньше минимального
            logger.error("Попытка создать  функцию с {} точками (требуется минимум 2)", count);
            throw new IllegalArgumentException("Количество точек меньше минимального");
        }
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
        logger.info("Создан LinkedListTabulatedFunction  с {} точками", count);
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
        Node node = getNode(index);
        logger.debug("Установка Y[{}] = {} (было {})", index, value, node.y);
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
            logger.error("x = {} меньше левой границы {}", x, head.x);
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
            logger.error("x = {} не в диапазоне [{}, {}]", x, floor.x, ceil.x);
            throw new InterpolationException("x не попадает в диапазон от " + floor.x + " до " + ceil.x);
        }

        double yFloor = floor.y;
        double xFloor = floor.x;
        double yCeil = ceil.y;
        double xCeil = ceil.x;

        logger.debug("Интерполяция для x = {} в интервале [{}, {}]", x, floor.x, ceil.x);
        return yFloor + (yCeil - yFloor) / (xCeil - xFloor) * (x - xFloor);
    }

    @Override
    protected double extrapolateLeft(double x) {
        logger.debug("Экстраполяция слева для x = {}", x);
        Node floor = getNode(0);
        Node ceil = getNode(1);
        return interpolate(x, floor.x, ceil.x, floor.y, ceil.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        logger.debug("Экстраполяция справа для x = {}", x);
        Node floor = getNode(count - 2);
        Node ceil = getNode(count - 1);
        return interpolate(x, floor.x, ceil.x, floor.y, ceil.y);
    }

    @Override
    public void insert(double x, double y) {
        logger.debug("Вставка точки ({}, {})", x, y);

        // Проверяем, существует ли уже узел с таким x
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.x - x) < 1e-12) {
                logger.debug("Точка с x = {} уже существует, обновление Y", x);
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
            logger.debug("Вставка в начало списка");
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
            logger.debug("Вставка в конец списка");
            addNode(x, y);
            return;
        }

        // Случай 3: Вставка в середину
        current = head;
        for (int i = 0; i < count; i++) {
            if (x > current.x && x < current.next.x) {
                logger.debug("Вставка в середину списка на позицию {}", i + 1);
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
        logger.debug("Удаление ноды с индексом {}", index);
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
        logger.debug("Нода удалена, новый размер: {}", count);
    }

    @Override
    public Iterator<Point> iterator() {  // Объект итератора
        Iterator<Point> iter = new Iterator<Point>() {
            Node node = head;
            int nodesProccesed = 0;

            @Override
            public boolean hasNext() {
                return nodesProccesed < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {  // Если следующего элемента нет, то выбрасываем исключение
                    logger.error("Попытка получить следующий элемент за пределами итератора");
                    throw new NoSuchElementException();
                }
                else {
                    Point point = new Point(node.x, node.y);
                    // Переходим на следующий элемент
                    node = node.next;
                    nodesProccesed++;
                    return point;
                }
            }
        };

        return iter;

    }
}
