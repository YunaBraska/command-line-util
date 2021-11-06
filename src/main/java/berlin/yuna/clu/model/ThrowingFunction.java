package berlin.yuna.clu.model;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {


    @Deprecated
    @Override
    default R apply(final T t) {
        try {
            return acceptThrows(t);
        } catch (final Throwable th) {
            throw new RuntimeException(th);
        }
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Throwable when the underlying apply throws
     */
    R acceptThrows(T t) throws Exception;
}