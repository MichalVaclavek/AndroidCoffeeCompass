package cz.fungisoft.coffeecompass2;

import androidx.core.util.Consumer;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Adapter for Kotlin Functional interface to Java's Consumer interface?
 * based on stackoverflow.com advice
 */
public class FunctionalUtils {
    public static Function1<? super Integer, Unit> fromConsumer(Consumer<? super Integer> callable) {
        return t -> {
                callable.accept(t);
            return Unit.INSTANCE;
        };
    }
}
