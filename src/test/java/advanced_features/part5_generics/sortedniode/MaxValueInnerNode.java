package advanced_features.part5_generics.sortedniode;

public class MaxValueInnerNode<T extends Comparable<T>> implements MaxValTreeNode<T> {

    private final MaxValTreeNode<T> left, right;

    public MaxValueInnerNode(MaxValTreeNode<T> left, MaxValTreeNode<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public T getValue() {

        T leftValue = left.getValue();
        T rightValue = right.getValue();

        var res = leftValue.compareTo(rightValue) > 0 ? leftValue : rightValue;
        return res;
    }

    @Override
    public MaxValTreeNode<T> getLeft() {
        return left;
    }

    @Override
    public MaxValTreeNode<T> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", getLeft(), getRight());
    }
}
