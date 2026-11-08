package com.zhassyn.filesystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchVisitorTest {
    @Test
    void filenameAndMinimumSizeMustBothMatchIncludingTheThreshold() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("notes.txt", 100));
        root.addChild(new FileNode("small.txt", 99));
        root.addChild(new FileNode("large.csv", 500));
        root.addChild(new FileNode("draft.txt", 150));
        SearchVisitor visitor = new SearchVisitor(".txt", 100);

        assertEquals(List.of("draft.txt", "notes.txt"), visitor.search(root));
    }

    @Test
    void filenameMatchingIsCaseSensitive() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("notes.txt", 100));
        root.addChild(new FileNode("notes.TXT", 100));

        assertEquals(List.of("notes.TXT"),
                new SearchVisitor(".TXT", 0).search(root));
    }

    @Test
    void pathsAreSortedRelativeToTheRootDespiteInsertionOrder() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode zeta = new DirectoryNode("zeta");
        DirectoryNode alpha = new DirectoryNode("alpha");
        DirectoryNode drafts = new DirectoryNode("drafts");
        root.addChild(zeta);
        root.addChild(new FileNode("middle.txt", 10));
        root.addChild(alpha);
        zeta.addChild(new FileNode("last.txt", 20));
        alpha.addChild(new FileNode("notes.txt", 30));
        alpha.addChild(drafts);
        drafts.addChild(new FileNode("first.txt", 40));

        assertEquals(List.of("alpha/drafts/first.txt", "alpha/notes.txt",
                        "middle.txt", "zeta/last.txt"),
                new SearchVisitor(".txt", 0).search(root));
    }

    @Test
    void aChosenSubtreeExcludesItsAncestorsAndOtherBranches() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        root.addChild(new FileNode("outside.txt", 500));
        root.addChild(documents);
        documents.addChild(new FileNode("notes.txt", 100));
        documents.addChild(drafts);
        drafts.addChild(new FileNode("outline.txt", 200));

        assertEquals(List.of("drafts/outline.txt", "notes.txt"),
                new SearchVisitor(".txt", 0).search(documents));
    }

    @Test
    void anEmptyCriterionMatchesEveryFilenameIncludingZeroByteFiles() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("image.png", 50));
        root.addChild(new FileNode("empty.txt", 0));
        root.addChild(new DirectoryNode("directory.txt"));

        assertEquals(List.of("empty.txt", "image.png"),
                new SearchVisitor("", 0).search(root));
        assertEquals(List.of("image.png"),
                new SearchVisitor("", 1).search(root));
    }

    @Test
    void emptyDirectoriesAndNoMatchesReturnEmptyResults() {
        DirectoryNode empty = new DirectoryNode("empty");
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("notes.txt", 100));

        assertTrue(new SearchVisitor(".txt", 0).search(empty).isEmpty());
        assertTrue(new SearchVisitor(".csv", 0).search(root).isEmpty());
        assertTrue(new SearchVisitor(".txt", 101).search(root).isEmpty());
    }

    @Test
    void repeatedSearchesResetStateAndPreserveEarlierSnapshots() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addChild(new FileNode("first.txt", 10));
        SearchVisitor visitor = new SearchVisitor(".txt", 0);
        List<String> firstResult = visitor.search(root);

        assertEquals(List.of("first.txt"), visitor.search(root));

        root.addChild(new FileNode("second.txt", 20));

        assertEquals(List.of("first.txt", "second.txt"), visitor.search(root));
        assertTrue(visitor.search(new DirectoryNode("empty")).isEmpty());
        assertEquals(List.of("other.txt"),
                visitor.search(new FileNode("other.txt", 30)));
        assertEquals(List.of("first.txt"), firstResult);
    }

    @Test
    void aFileRootUsesItsOwnNameAndSearchResultsAreImmutable() {
        FileNode file = new FileNode("notes.txt", 100);
        List<String> result = new SearchVisitor(".txt", 100).search(file);

        assertEquals(List.of("notes.txt"), result);
        assertThrows(UnsupportedOperationException.class,
                () -> result.add("extra.txt"));
    }
}
