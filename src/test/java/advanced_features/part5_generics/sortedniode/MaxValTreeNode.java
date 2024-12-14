package advanced_features.part5_generics.sortedniode;

public interface MaxValTreeNode<T extends Comparable<T>> {

    T getValue();

    MaxValTreeNode<T> getLeft();

    MaxValTreeNode<T> getRight();
}

