package advanced_features.part5_generics.sortedniode;

public class MaxValLeafNode<T extends Comparable<T>> implements MaxValTreeNode<T> {

    private final T value;

    public MaxValLeafNode(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public MaxValTreeNode<T> getLeft() {
        return null;
    }

    @Override
    public MaxValTreeNode<T> getRight() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("[%s]", getValue());
    }
}
