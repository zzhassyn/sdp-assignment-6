package com.zhassyn.filesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SearchVisitor implements Visitor {
    private final String namePart;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private Element root;
    private String currentPath;

    public SearchVisitor(String namePart, long minimumSize) {
        this.namePart = Objects.requireNonNull(namePart, "Name criterion must not be null.");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size must not be negative.");
        }
        this.minimumSize = minimumSize;
    }

    public List<String> search(Element root) {
        this.root = Objects.requireNonNull(root, "Root must not be null.");
        matches.clear();
        currentPath = "";
        root.accept(this);
        Collections.sort(matches);
        return List.copyOf(matches);
    }

    @Override
    public void visit(FileNode file) {
        if (file.getName().contains(namePart) && file.getSize() >= minimumSize) {
            matches.add(joinPath(currentPath, file.getName()));
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        String previousPath = currentPath;
        if (directory != root) {
            currentPath = joinPath(currentPath, directory.getName());
        }
        for (Node child : directory.getChildren()) {
            child.accept(this);
        }
        currentPath = previousPath;
    }

    private String joinPath(String parentPath, String name) {
        return parentPath.isEmpty() ? name : parentPath + "/" + name;
    }
}
