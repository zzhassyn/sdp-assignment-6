package com.zhassyn.filesystem;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        DirectoryNode root = createSampleTree();
        String namePart = ".txt";
        long minimumSize = 200;
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        SearchVisitor searchVisitor = new SearchVisitor(namePart, minimumSize);
        StatisticsVisitor statisticsVisitor = new StatisticsVisitor();

        System.out.println("Assignment 6 - Visitor Pattern");
        System.out.println("Student: Zhassyn Zhalynuly");
        System.out.println("Group: SE-2527");
        System.out.println();
        System.out.println("Total size: " + sizeVisitor.calculate(root) + " bytes");
        System.out.println();
        System.out.println("Search: name contains \"" + namePart
                + "\", size >= " + minimumSize + " bytes");
        List<String> matches = searchVisitor.search(root);
        for (String path : matches) {
            System.out.println(path);
        }

        statisticsVisitor.collect(root);
        System.out.println();
        System.out.println("Statistics:");
        System.out.println("Files: " + statisticsVisitor.getFileCount());
        System.out.println("Directories: " + statisticsVisitor.getDirectoryCount());
        System.out.println("Maximum depth: " + statisticsVisitor.getMaxDepth());
    }

    private static DirectoryNode createSampleTree() {
        DirectoryNode root = new DirectoryNode("study");
        DirectoryNode docs = new DirectoryNode("docs");
        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode source = new DirectoryNode("src");

        root.addChild(new FileNode("readme.txt", 120));
        root.addChild(docs);
        root.addChild(source);
        root.addChild(new DirectoryNode("empty"));
        docs.addChild(new FileNode("plan.txt", 200));
        docs.addChild(new FileNode("notes.txt", 450));
        docs.addChild(archive);
        archive.addChild(new FileNode("old-notes.txt", 80));
        source.addChild(new FileNode("Main.java", 900));
        source.addChild(new FileNode("Visitor.java", 300));
        return root;
    }
}
