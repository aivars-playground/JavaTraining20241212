package advanced_features.part5_generics.basictree;

public class InnerNode<T> implements TreeNode<T> {

    private final TreeNode<T> left, right;

    public InnerNode(TreeNode<T> left, TreeNode<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public TreeNode<T> getLeft() {
        return left;
    }

    @Override
    public TreeNode<T> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", getLeft(), getRight());
    }
}
