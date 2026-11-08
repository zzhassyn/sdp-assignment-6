package com.zhassyn.filesystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsVisitorTest {
    @Test
    void nestedFilesAndDirectoriesHaveCorrectCountsAndDepth() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(createTree());

        assertEquals(4, visitor.getFileCount());
        assertEquals(5, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaxDepth());
    }

    @Test
    void deepEmptyDirectoriesAlsoDetermineMaximumDepth() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        DirectoryNode third = new DirectoryNode("third");
        DirectoryNode fourth = new DirectoryNode("fourth");
        root.addChild(new FileNode("shallow.txt", 10));
        root.addChild(first);
        first.addChild(second);
        second.addChild(third);
        third.addChild(fourth);
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(root);

        assertEquals(1, visitor.getFileCount());
        assertEquals(5, visitor.getDirectoryCount());
        assertEquals(4, visitor.getMaxDepth());
    }

    @Test
    void anEmptyDirectoryCountsAsOneDirectoryAtDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(new DirectoryNode("empty"));

        assertEquals(0, visitor.getFileCount());
        assertEquals(1, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaxDepth());
    }

    @Test
    void aFileRootCountsAsOneFileAtDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(new FileNode("notes.txt", 50));

        assertEquals(1, visitor.getFileCount());
        assertEquals(0, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaxDepth());
    }

    @Test
    void anAttachedSubtreeStartsAtDepthZeroAndExcludesOutsideNodes() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        root.addChild(new FileNode("outside.txt", 300));
        root.addChild(documents);
        documents.addChild(new FileNode("notes.txt", 40));
        documents.addChild(drafts);
        drafts.addChild(new FileNode("outline.txt", 20));
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(documents);

        assertEquals(2, visitor.getFileCount());
        assertEquals(2, visitor.getDirectoryCount());
        assertEquals(2, visitor.getMaxDepth());
    }

    @Test
    void repeatedCollectionsResetCountsAndDepthAfterChanges() {
        DirectoryNode root = createTree();
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.collect(root);
        visitor.collect(root);

        assertEquals(4, visitor.getFileCount());
        assertEquals(5, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaxDepth());

        DirectoryNode added = new DirectoryNode("added");
        root.addChild(added);
        added.addChild(new FileNode("new.txt", 15));
        visitor.collect(root);

        assertEquals(5, visitor.getFileCount());
        assertEquals(6, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaxDepth());

        visitor.collect(new FileNode("only.txt", 5));

        assertEquals(1, visitor.getFileCount());
        assertEquals(0, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaxDepth());

        visitor.collect(new DirectoryNode("empty"));

        assertEquals(0, visitor.getFileCount());
        assertEquals(1, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaxDepth());
    }

    @Test
    void visitorResultsDoNotDependOnOperationOrder() {
        DirectoryNode root = createTree();
        StatisticsVisitor statistics = new StatisticsVisitor();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(".txt", 40);

        statistics.collect(root);
        assertEquals(320L, size.calculate(root));
        assertEquals(List.of("documents/lesson.txt", "todo.txt"), search.search(root));

        assertEquals(4, statistics.getFileCount());
        assertEquals(5, statistics.getDirectoryCount());
        assertEquals(3, statistics.getMaxDepth());

        assertEquals(List.of("documents/lesson.txt", "todo.txt"), search.search(root));
        assertEquals(320L, size.calculate(root));
        statistics.collect(root);

        assertEquals(4, statistics.getFileCount());
        assertEquals(5, statistics.getDirectoryCount());
        assertEquals(3, statistics.getMaxDepth());
    }

    private DirectoryNode createTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        DirectoryNode images = new DirectoryNode("images");

        root.addChild(new FileNode("todo.txt", 40));
        root.addChild(documents);
        root.addChild(images);
        root.addChild(new DirectoryNode("empty"));
        documents.addChild(new FileNode("lesson.txt", 60));
        documents.addChild(drafts);
        drafts.addChild(new FileNode("outline.txt", 20));
        images.addChild(new FileNode("logo.png", 200));

        return root;
    }
}
