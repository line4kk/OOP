package concurrent;

import functions.TabulatedFunction;

public class ReadTask implements Runnable {
    private final TabulatedFunction tabulatedFunction;

    public ReadTask(TabulatedFunction tabulatedFunction) {
        this.tabulatedFunction = tabulatedFunction;
    }

    @Override
    public void run() {
        for (int i = 0; i < tabulatedFunction.getCount() - 1; i++) {
            System.out.printf("After read: i = %d, x = %f, y = %f", i, tabulatedFunction.getX(i), tabulatedFunction.getY(i));
        }
    }
}
