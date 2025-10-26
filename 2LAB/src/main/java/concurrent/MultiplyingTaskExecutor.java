package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;

import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {

        TabulatedFunction function = new LinkedListTabulatedFunction(
                new UnitFunction(), 1.0, 10000.0, 10000
        );

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.println("Главный поток был прерван");
            e.printStackTrace();
        }

        System.out.println(function);
    }
}
