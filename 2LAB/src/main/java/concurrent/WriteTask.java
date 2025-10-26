package concurrent;

import functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction tabulatedFunction;
    private final double value;  // Значение, которое будет записываться в функцию для каждого x

    public WriteTask(TabulatedFunction tabulatedFunction, double value) {
        this.tabulatedFunction = tabulatedFunction;
        this.value = value;
    }

    @Override
    public void run() {
        for (int i = 0; i < tabulatedFunction.getCount() - 1; i++) {
            tabulatedFunction.setY(i, value);
            System.out.printf("Writing for index %d complete%n", i);
        }
    }
}
