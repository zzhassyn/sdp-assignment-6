# Assignment 6 - Visitor Pattern

**Student:** Zhassyn Zhalynuly  
**Group:** SE-2527  
**Course:** Software Design Patterns

This Java 17 Maven project represents a small file system in memory. It does not read or change real files. All source code and explanations are in English. The Java files contain no comments.

## Submission files

- [PDF report](docs/Assignment_6_Report_Zhassyn_Zhalynuly_SE-2527.pdf): three report pages and one separate UML appendix page.
- [UML diagram](docs/visitor-uml.png).
- [Recorded test results](docs/test-results.txt).
- [Recorded application output](docs/sample-output.txt).

## Run

Install JDK 17 and Maven. Run these commands from the folder containing `pom.xml`.

```bash
mvn test
mvn -q compile exec:java
```

The first command runs every test. The second command runs the demonstration. No IDE, manual input, database or external service is required. Maven downloads dependencies on the first run.

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

## Example data and output

`Main.createSampleTree()` creates a directory named `study`. These paths are relative to that root.

| Path | Type | Size in bytes |
| --- | --- | ---: |
| `readme.txt` | File | 120 |
| `docs` | Directory | - |
| `docs/plan.txt` | File | 200 |
| `docs/notes.txt` | File | 450 |
| `docs/archive` | Directory | - |
| `docs/archive/old-notes.txt` | File | 80 |
| `src` | Directory | - |
| `src/Main.java` | File | 900 |
| `src/Visitor.java` | File | 300 |
| `empty` | Empty directory | - |

The file sizes are example metadata, not the sizes of the repository's source files. The total is `120 + 200 + 450 + 80 + 900 + 300 = 2050` bytes.

```text
Assignment 6 - Visitor Pattern
Student: Zhassyn Zhalynuly
Group: SE-2527

Total size: 2050 bytes

Search: name contains ".txt", size >= 200 bytes
docs/notes.txt
docs/plan.txt

Statistics:
Files: 6
Directories: 5
Maximum depth: 3
```

## Understand the design

| Class or interface | Responsibility |
| --- | --- |
| `Element` | Defines `getName()` and `accept(Visitor)`. |
| `Node` | Shares the immutable name and parent reference. |
| `FileNode` | Stores a non-negative size in bytes. |
| `DirectoryNode` | Stores children and validates additions. |
| `Visitor` | Declares `visit(FileNode)` and `visit(DirectoryNode)`. |
| `TotalSizeVisitor` | Adds file sizes. |
| `SearchVisitor` | Combines filename and size conditions. |
| `StatisticsVisitor` | Counts nodes and finds maximum depth. |
| `Main` | Builds the sample tree and prints the results. |

### Double dispatch in two steps

1. An operation calls `root.accept(this)`. Java uses the actual element object to run either `FileNode.accept` or `DirectoryNode.accept`.
2. That method calls `visitor.visit(this)`. Inside `FileNode`, `this` has type `FileNode`, so Java selects the file overload. Inside `DirectoryNode`, Java selects the directory overload. The actual visitor object supplies the operation's implementation.

For example, `TotalSizeVisitor.visit(FileNode)` adds a size, while its directory overload visits children. No `instanceof`, reflection, type-selection casts or `getClass()` branches are used.

### Traversal, paths and depth

The visitors own traversal. Their directory methods call `accept` once per child, while element `accept` methods only dispatch. This is easy to follow but repeats the short child loop in three classes. A separate walker could centralize traversal but would need to pass path and depth context to visitors.

Search tests the filename, not a directory name or full path. A filename must contain `namePart` and its size must be at least `minimumSize`. An empty string matches every filename. A negative minimum size or null criterion is rejected. Each directory visit saves the previous path, adds its name if it is not the selected root, then restores the previous path after its children. Sorting happens after traversal.

Depth is the number of parent-child edges from the selected root. The root is at depth 0. Statistics increase the current depth before visiting children and decrease it afterward. Both files and directories update maximum depth. Searching or collecting statistics on an attached subtree starts again from that selected root.

### Integrity and reuse

`DirectoryNode.addChild` validates the entire addition before changing the tree. Following parent references detects self-cycles and ancestor-cycles without checking node types. Duplicate names are rejected among siblings, but the same name is allowed in different directories. A child already owned by a parent cannot be added again.

Names cannot be null, blank, `.` or `..`, or contain `/` or `\`. Invalid names, negative sizes, duplicate names, ownership violations and cycles cause `IllegalArgumentException`. A null child causes `NullPointerException`. Failed additions leave the tree unchanged. Children are returned as an immutable copy, and there is no public parent setter.

Use `calculate(root)`, `search(root)` or `collect(root)` to begin each operation. They reset all accumulated state. `accept` and `visit` are dispatch callbacks, not entry points for a fresh operation. Statistics getters describe the latest collection. Earlier search result lists are unchanged by later searches.

Visitors are intended for sequential use, with no tree changes during traversal. Extremely deep trees are limited by the Java call stack. Total size uses `long`; overflow causes `ArithmeticException` rather than a wrong negative total.

## Automated verification

Verified on Eclipse Temurin **17.0.21**, Maven **3.9.16**, JUnit **5.11.4** and Surefire **3.5.2**.

```text
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test class | Tests | Main coverage |
| --- | ---: | --- |
| `ElementTest` | 11 | Dispatch, valid data, immutable children, duplicate names, ownership and cycles. |
| `TotalSizeVisitorTest` | 5 | Nested totals, empty roots, large files, reuse and overflow recovery. |
| `SearchVisitorTest` | 8 | Combined filters, exact threshold, sorted relative paths, no matches and reuse. |
| `StatisticsVisitorTest` | 7 | Counts, depth, empty directories, subtree roots, reuse and operation order. |

These cover T1 (dispatch), T2 (nested totals), T3 (both filters), T4 (paths and sorting), T5 (statistics), T6 (empty/no-match cases), T7 (state reset), and T8 (invalid mutations). Tests use their own fixtures and do not depend on `Main` or test execution order.

## Five Clean Code practices

| Practice | Code reference | Reason |
| --- | --- | --- |
| Meaningful naming | `minimumSize`, `getMaxDepth()`, `checkForCycle()` | Names explain the data and actions. |
| Single Responsibility | `DirectoryNode` and the three visitors | Structure rules and independent operations live in separate classes. |
| Small methods | `accept()`, `joinPath()`, `checkDuplicateName()` | Each method performs one readable step. |
| Encapsulation | Private fields and `DirectoryNode.getChildren()` | Callers cannot bypass public tree rules. |
| No type-checking chains | Both concrete `accept()` methods | Overloaded visitor methods select behavior without conditional type checks. |

## Extension boundary and commit history

1. [Initial implementation](https://github.com/zzhassyn/sdp-assignment-6/commit/98e03f89258c2c626cfc87c848a716dc23fa8764): file tree, total size, search and 24 tests.
2. [Statistics extension](https://github.com/zzhassyn/sdp-assignment-6/commit/6fbbaafd7e6abb023572676f5ae7496c476534a3): one new visitor, seven tests and its use in `Main`.

The second step did not change the element classes, interfaces or existing `accept` methods. The initial contract already supported both element types.

Adding a new operation means implementing the existing `Visitor` interface. Adding a **new element type** is more expensive: add a `visit` overload to `Visitor`, implement it in every existing visitor, and give the new element its own `accept`. Visitor works best when element types are stable and operations change more often.

## Prepare for the defense

1. Open `FileNode.accept`, `DirectoryNode.accept` and `Visitor` to explain the two dispatch steps.
2. Run `mvn test`. Explain an assertion such as accepting a file exactly at the minimum-size threshold.
3. In `Main`, change `namePart` from `".txt"` to `".java"` and `minimumSize` from `200` to `300`. Run again. Matches become `src/Main.java` and `src/Visitor.java`.
4. Change data in `createSampleTree()` and run again to demonstrate different input.
5. Explain why the root has depth 0, why the root directory is counted, and how visitor state resets.
6. Explain why adding a visitor leaves element classes unchanged, while adding an element type affects existing visitors.

## References

- Course handout: *Assignment 06 - Visitor Pattern: Advanced File System*, supplied with the task.
- *Lecture 9*, especially pages 10-11, 18 and 31, supplied with the task.
- [JUnit 5.11.4 User Guide](https://docs.junit.org/5.11.4/user-guide/).
- [Maven Compiler Plugin 3.13.0: setting the Java release](https://maven.apache.org/plugins-archives/maven-compiler-plugin-3.13.0/examples/set-compiler-release.html).
