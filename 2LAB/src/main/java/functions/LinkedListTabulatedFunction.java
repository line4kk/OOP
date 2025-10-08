package functions;

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
        Node res = head;

        for (int i = 1; i <= index; i++)
            res = res.next;

        return res;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues){  // Задать функцию по спискам x, y
        for (int i = 0; i < xValues.length; i++){
            addNode(xValues[i], yValues[i]);
        }
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count){  // Задать функцию с помощью дискретизации
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
        if (x < head.x) {  // Если все элементы массива больше x
            return 0;
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
        if (count == 1)
            return head.y;

        Node floor = getNode(floorIndex);
        Node ceil = getNode(floorIndex + 1);
        double yFloor = floor.y;
        double xFloor = floor.x;
        double yCeil = ceil.y;
        double xCeil = ceil.x;

        return yFloor + (yCeil - yFloor) / (xCeil - xFloor) * (x - xFloor);
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, 0);
    }

    @Override
    protected double extrapolateRight(double x) {
        return interpolate(x, count - 2);
    }

    @Override
    public void insert(double x, double y) {
        // Если список пустой, просто добавляем узел
        if (head == null) {
            addNode(x, y);
            return;
        }

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
}
