package concurrent;

import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReadTask.class);
    private final TabulatedFunction tabulatedFunction;

    public ReadTask(TabulatedFunction tabulatedFunction) {
        this.tabulatedFunction = tabulatedFunction;
    }

    @Override
    public void run() {
        logger.info("Поток {} начал чтение функции", Thread.currentThread().getName());
        for (int i = 0; i < tabulatedFunction.getCount(); i++) {
            synchronized (tabulatedFunction) {
                logger.debug("Прочитана точка: i = {}, x = {}, y = {}", i, tabulatedFunction.getX(i), tabulatedFunction.getY(i));
                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, tabulatedFunction.getX(i), tabulatedFunction.getY(i));
            }
        }
        logger.info("Поток {} завершил чтение функции", Thread.currentThread().getName());
    }
}
