package com.zhassyn.filesystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TotalSizeVisitorTest {
    @Test
    void allNestedFileSizesAreAddedExactlyOnce() {
        DirectoryNode root = createTree();

        assertEquals(1180L, new TotalSizeVisitor().calculate(root));
    }

    @Test
    void anEmptyDirectoryHasZeroSize() {
        DirectoryNode root = new DirectoryNode("empty");

        assertEquals(0L, new TotalSizeVisitor().calculate(root));
    }

    @Test
    void aFileCanBeTheRootAndItsSizeCanExceedIntegerRange() {
        FileNode file = new FileNode("backup.zip", 4_000_000_000L);

        assertEquals(4_000_000_000L, new TotalSizeVisitor().calculate(file));
    }

    @Test
    void repeatedCalculationsResetThePreviousTotal() {
        DirectoryNode root = createTree();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        assertEquals(1180L, visitor.calculate(root));
        assertEquals(1180L, visitor.calculate(root));

        root.addChild(new FileNode("new.txt", 20));

        assertEquals(1200L, visitor.calculate(root));
        assertEquals(0L, visitor.calculate(new DirectoryNode("empty")));
        assertEquals(7L, visitor.calculate(new FileNode("small.txt", 7)));
    }

    @Test
    void overflowIsRejectedAndTheNextCalculationStartsFresh() {
        DirectoryNode root = new DirectoryNode("too-large");
        root.addChild(new FileNode("large.bin", Long.MAX_VALUE));
        root.addChild(new FileNode("extra.bin", 1));
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        assertThrows(ArithmeticException.class, () -> visitor.calculate(root));
        assertEquals(9L, visitor.calculate(new FileNode("small.txt", 9)));
    }

    private DirectoryNode createTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        DirectoryNode images = new DirectoryNode("images");

        root.addChild(new FileNode("todo.txt", 120));
        root.addChild(images);
        root.addChild(documents);
        root.addChild(new DirectoryNode("empty"));
        images.addChild(new FileNode("logo.png", 480));
        images.addChild(new FileNode("preview.txt", 0));
        documents.addChild(new FileNode("notes.txt", 200));
        documents.addChild(drafts);
        drafts.addChild(new FileNode("outline.txt", 80));
        drafts.addChild(new FileNode("data.csv", 300));

        return root;
    }
}
