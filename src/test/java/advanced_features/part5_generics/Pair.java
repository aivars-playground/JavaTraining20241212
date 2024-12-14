package advanced_features.part5_generics;

import java.util.function.BiFunction;

public record Pair<T,U>(T left, U right) {

    public static <V, W> Pair<V, W> of(V first, W second) {
        return new Pair<>(first, second);
    }

    public <U1> Pair<T, U1> withOtherRight(U1 newRight) {
        return new Pair<>(this.left, newRight);
    }

    public Pair<U,T> reverse() {
        return new Pair<>(right, left);
    }

    public <V,W> Pair<V,W> map(BiFunction<T,U, Pair<V,W>> fun) {
        return fun.apply(left, right);
    }
}
