package concurrent;

import functions.ConstantFunction;
import functions.LinkedListTabulatedFunction;

public class ReadWriteTaskExecutor {
    static void main(String[] args) {

        // Тождественная функция, равная отрицательному числу -1
        ConstantFunction constantFunction = new ConstantFunction(-1);

        // Передаем ее в LinkedListTabulatedFunction на интервале от 1 до 1000, с кол-вом точек 1000
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(constantFunction, 1, 1000, 1000);

        Thread threadReadTask = new Thread(new ReadTask(function));  // Поток исполнения ReadTask
        Thread threadWriteTask = new Thread(new WriteTask(function, 3));  // Поток исполнения WriteTask. Передается произвольное положительно число - 3

        threadReadTask.start();
        threadWriteTask.start();

    }
}
