# Assignment 6 - Visitor Pattern

**Student:** Zhassyn Zhalynuly  
**Group:** SE-2527

This Java 17 Maven project represents a small file system in memory. It does not read or change real files.

## Run

Install JDK 17 and Maven. Run these commands from the folder containing `pom.xml`.

```bash
mvn test
mvn -q compile exec:java
```

## Implemented operations

- `FileNode` stores a name and a non-negative size in bytes.
- `DirectoryNode` stores children and prevents duplicate names, multiple parents and cycles.
- `TotalSizeVisitor` adds the sizes of all files in the selected subtree.
- `SearchVisitor` finds files whose names contain a given string and whose sizes are at least a given minimum.
- `StatisticsVisitor` counts files and directories and finds the maximum tree depth.

Both concrete elements call `visitor.visit(this)` in `accept`. The visitors own traversal: their directory methods call `accept` on each child. The elements do not recursively visit children themselves.

Start each operation with `calculate(root)`, `search(root)` or `collect(root)`. These methods reset visitor state, so the same visitor can be used again. Search uses case-sensitive filename matching, `/` separators, root-relative paths and lexicographical sorting. A selected file root is returned by its own filename. Child and result lists are immutable snapshots.

Statistics include the selected root directory and use depth zero for the selected root. Empty directories also count and can determine the maximum depth.

`StatisticsVisitor` was added in a separate commit using the existing `Visitor` interface. `FileNode`, `DirectoryNode`, `Node`, `Element`, `Visitor`, and the existing `accept` methods were not changed.
