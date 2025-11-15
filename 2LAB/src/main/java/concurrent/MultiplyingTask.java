package concurrent;

import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplyingTask implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(MultiplyingTask.class);

    private final TabulatedFunction function;
    private volatile boolean completed = false;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        logger.info("Поток {} начал умножение функции", threadName);
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (function) {
                function.setY(i, function.getY(i) * 2);
            }
        }
        System.out.println("Поток " + Thread.currentThread().getName() + " закончил выполнение задачи");
        completed = true;
        logger.info("Поток {} завершил умножение функции", threadName);    }

    public boolean isCompleted() {
        return completed;
    }
}
