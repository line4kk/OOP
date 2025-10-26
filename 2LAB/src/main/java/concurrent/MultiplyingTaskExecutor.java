package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;

import java.util.*;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {

        TabulatedFunction function = new LinkedListTabulatedFunction(
                new UnitFunction(), 1.0, 10000.0, 10000
        );

        List<Thread> threads = new ArrayList<>();
        Set<MultiplyingTask> tasks = Collections.synchronizedSet(new LinkedHashSet<>());

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            tasks.add(task);
            Thread thread = new Thread(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        while (!tasks.isEmpty()) {
            MultiplyingTask[] tasksArray;
            synchronized (tasks) {
                tasksArray = tasks.toArray(new MultiplyingTask[0]);

                for (MultiplyingTask task : tasksArray) {
                    if (task.isCompleted()) {
                        synchronized (tasks) {
                            tasks.remove(task);
                        }
                    }
                }
            }
        }
        System.out.println(function);
    }
}
