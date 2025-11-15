package concurrent;

import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WriteTask.class);
    private final TabulatedFunction tabulatedFunction;
    private final double value;  // Значение, которое будет записываться в функцию для каждого x

    public WriteTask(TabulatedFunction tabulatedFunction, double value) {
        this.tabulatedFunction = tabulatedFunction;
        this.value = value;
    }

    @Override
    public void run() {
        logger.info("Поток {} начал запись значения {}", Thread.currentThread().getName(), value);
        for (int i = 0; i < tabulatedFunction.getCount(); i++) {
            synchronized (tabulatedFunction) {
                tabulatedFunction.setY(i, value);
                logger.debug("Записано значение {} в точку {}", value, i);
                System.out.printf("Writing for index %d complete%n", i);
            }
        }
        logger.info("Поток {} завершил запись значения {}", Thread.currentThread().getName(), value);
    }
}
