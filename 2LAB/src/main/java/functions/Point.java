package functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Point {
    private static final Logger logger = LoggerFactory.getLogger(Point.class);
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        logger.trace("Создана точка: ({}, {})", x, y);
    }
}
