package com.zhassyn.filesystem;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementTest {
    @Test
    void eachElementCallsItsMatchingVisitMethod() {
        Element file = new FileNode("notes.txt", 32);
        Element directory = new DirectoryNode("documents");
        RecordingVisitor visitor = new RecordingVisitor();

        file.accept(visitor);
        directory.accept(visitor);

        assertSame(file, visitor.file);
        assertSame(directory, visitor.directory);
        assertEquals(1, visitor.fileVisits);
        assertEquals(1, visitor.directoryVisits);
    }

    @Test
    void negativeFileSizesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileNode("invalid.txt", -1));
    }

    @Test
    void invalidNamesAreRejectedForBothElementTypes() {
        List<String> invalidNames = Arrays.asList(
                null, "", "   ", ".", "..", "docs/file.txt", "docs\\file.txt");

        for (String name : invalidNames) {
            assertThrows(IllegalArgumentException.class,
                    () -> new FileNode(name, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> new DirectoryNode(name));
        }
    }

    @Test
    void nodesStoreTheirNamesSizesAndParentReferences() {
        DirectoryNode root = new DirectoryNode("workspace");
        FileNode file = new FileNode("empty.txt", 0);
        DirectoryNode documents = new DirectoryNode("documents");

        root.addChild(file);
        root.addChild(documents);

        assertEquals("workspace", root.getName());
        assertEquals("empty.txt", file.getName());
        assertEquals(0L, file.getSize());
        assertEquals(List.of(file, documents), root.getChildren());
        assertSame(root, file.getParent());
        assertSame(root, documents.getParent());
        assertNull(root.getParent());
    }

    @Test
    void childrenAreReturnedAsAnImmutableSnapshot() {
        DirectoryNode root = new DirectoryNode("workspace");
        FileNode first = new FileNode("first.txt", 10);
        root.addChild(first);
        List<Node> savedChildren = root.getChildren();

        assertThrows(UnsupportedOperationException.class,
                () -> savedChildren.add(new FileNode("outside.txt", 20)));

        FileNode second = new FileNode("second.txt", 30);
        root.addChild(second);

        assertEquals(List.of(first), savedChildren);
        assertEquals(List.of(first, second), root.getChildren());
    }

    @Test
    void duplicateSiblingNamesAreRejectedAcrossElementTypes() {
        DirectoryNode root = new DirectoryNode("workspace");
        FileNode original = new FileNode("shared", 10);
        DirectoryNode duplicate = new DirectoryNode("shared");
        root.addChild(original);

        assertThrows(IllegalArgumentException.class,
                () -> root.addChild(duplicate));

        assertEquals(List.of(original), root.getChildren());
        assertNull(duplicate.getParent());
    }

    @Test
    void equalNamesAreAllowedInDifferentDirectories() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode firstNotes = new FileNode("notes.txt", 10);
        FileNode secondNotes = new FileNode("notes.txt", 20);

        root.addChild(first);
        first.addChild(second);
        first.addChild(firstNotes);
        second.addChild(secondNotes);

        assertSame(first, firstNotes.getParent());
        assertSame(second, secondNotes.getParent());
        assertSame(first, second.getParent());
        assertEquals(List.of(second, firstNotes), first.getChildren());
    }

    @Test
    void nodesCannotHaveMultipleParentsOrBeAddedTwice() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("notes.txt", 10);
        DirectoryNode directory = new DirectoryNode("documents");
        first.addChild(file);
        first.addChild(directory);

        assertThrows(IllegalArgumentException.class, () -> second.addChild(file));
        assertThrows(IllegalArgumentException.class, () -> second.addChild(directory));
        assertThrows(IllegalArgumentException.class, () -> first.addChild(file));

        assertSame(first, file.getParent());
        assertSame(first, directory.getParent());
        assertEquals(List.of(file, directory), first.getChildren());
        assertTrue(second.getChildren().isEmpty());
    }

    @Test
    void aDirectoryCannotContainItself() {
        DirectoryNode root = new DirectoryNode("workspace");

        assertThrows(IllegalArgumentException.class, () -> root.addChild(root));

        assertNull(root.getParent());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void aDirectoryCannotContainItsAncestor() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        root.addChild(documents);
        documents.addChild(drafts);

        assertThrows(IllegalArgumentException.class, () -> drafts.addChild(root));

        assertNull(root.getParent());
        assertSame(root, documents.getParent());
        assertSame(documents, drafts.getParent());
        assertEquals(List.of(documents), root.getChildren());
        assertEquals(List.of(drafts), documents.getChildren());
        assertTrue(drafts.getChildren().isEmpty());
    }

    @Test
    void nullChildrenAreRejectedWithoutChangingTheDirectory() {
        DirectoryNode root = new DirectoryNode("workspace");

        assertThrows(NullPointerException.class, () -> root.addChild(null));

        assertTrue(root.getChildren().isEmpty());
    }

    private static class RecordingVisitor implements Visitor {
        private FileNode file;
        private DirectoryNode directory;
        private int fileVisits;
        private int directoryVisits;

        @Override
        public void visit(FileNode file) {
            this.file = file;
            fileVisits++;
        }

        @Override
        public void visit(DirectoryNode directory) {
            this.directory = directory;
            directoryVisits++;
        }
    }
}
