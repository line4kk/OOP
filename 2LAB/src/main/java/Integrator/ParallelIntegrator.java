package Integrator;

import functions.TabulatedFunction;
import java.util.concurrent.*;

public class ParallelIntegrator implements Integrator {
    private final int numThreads;

    public ParallelIntegrator(int numThreads) {
        this.numThreads = numThreads;
    }

    // Функция интегрирования
    @Override
    public double integrate(TabulatedFunction function) {
        if (function.getCount() < 2) return 0.0;

        // Создание пула потоков и массива в котором будет результат
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<Double>[] futures = new Future[numThreads];

        int segments = function.getCount() - 1;
        int segmentsPerThread = segments / numThreads;

        // Вычисление результата по потокам
        try {
            for (int i = 0; i < numThreads; i++) {
                int startSegment = i * segmentsPerThread;
                int endSegment = (i == numThreads - 1) ? segments : (i + 1) * segmentsPerThread;
                futures[i] = executor.submit(new IntegrationTask(function, startSegment, endSegment));
            }

            double total = 0.0;
            for (Future<Double> future : futures) {
                total += future.get();
            }
            return total;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка в интеграле", e);
        } finally {
            executor.shutdown();
        }
    }

    // Задача для одного потока
    private static class IntegrationTask implements Callable<Double> {
        private final TabulatedFunction function;
        private final int startSegment;
        private final int endSegment;

        public IntegrationTask(TabulatedFunction function, int startSegment, int endSegment) {
            this.function = function;
            this.startSegment = startSegment;
            this.endSegment = endSegment;
        }

        @Override
        // Метод трапеций
        public Double call() {
            double sum = 0.0;
            for (int i = startSegment; i < endSegment; i++) {
                double width = function.getX(i + 1) - function.getX(i);
                double y1 = function.getY(i);
                double y2 = function.getY(i + 1);
                sum += width * (y1 + y2) / 2.0;
            }
            return sum;
        }
    }
}
