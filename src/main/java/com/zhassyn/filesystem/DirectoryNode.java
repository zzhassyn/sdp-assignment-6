package com.zhassyn.filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DirectoryNode extends Node {
    private final List<Node> children = new ArrayList<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void addChild(Node child) {
        Objects.requireNonNull(child, "Child must not be null.");
        if (child.getParent() != null) {
            throw new IllegalArgumentException("A node can have only one parent.");
        }
        checkForCycle(child);
        checkDuplicateName(child.getName());
        child.attachTo(this);
        children.add(child);
    }

    private void checkForCycle(Node child) {
        for (DirectoryNode current = this; current != null; current = current.getParent()) {
            if (current == child) {
                throw new IllegalArgumentException("Adding this node would create a cycle.");
            }
        }
    }

    private void checkDuplicateName(String name) {
        for (Node child : children) {
            if (child.getName().equals(name)) {
                throw new IllegalArgumentException("Sibling names must be unique.");
            }
        }
    }

    public List<Node> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
