package utils;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public final class StreamUtil {
    private StreamUtil() {
    }

    public static <T> BinaryOperator<T> toOnlyElement() {
        return toOnlyElementThrowing(IllegalArgumentException::new);
    }

    public static <T, E extends RuntimeException> BinaryOperator<T>
    toOnlyElementThrowing(Supplier<E> exception) {
        return (element, otherElement) -> {
            throw exception.get();
        };
    }
}
