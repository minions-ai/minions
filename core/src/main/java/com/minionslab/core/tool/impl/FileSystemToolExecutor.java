package com.minionslab.core.tool.impl;

import com.minionslab.core.tool.ToolExecutor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A ToolExecutor implementation for file system operations.
 * Supports reading, writing, and listing files.
 */
@Component("fileSystemTool")
public class FileSystemToolExecutor implements ToolExecutor {

    private static final String OPERATION = "operation";
    private static final String FILE_PATH = "filePath";
    private static final String CONTENT = "content";
    private static final String START_LINE = "startLine";
    private static final String END_LINE = "endLine";

    @Override
    public Object execute(Map<String, Object> parameters) throws Exception {
        if (!parameters.containsKey(OPERATION)) {
            throw new IllegalArgumentException("Operation parameter is required");
        }

        String operation = parameters.get(OPERATION).toString().toLowerCase();
        String filePath = parameters.getOrDefault(FILE_PATH, "").toString();

        return switch (operation) {
            case "read" -> readFile(filePath, parameters);
            case "write" -> writeFile(filePath, parameters);
            case "list" -> listDirectory(filePath);
            case "exists" -> Files.exists(Paths.get(filePath));
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        };
    }

    private String readFile(String filePath, Map<String, Object> parameters) throws Exception {
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is required for read operation");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        // If line numbers are specified, read specific lines
        if (parameters.containsKey(START_LINE) && parameters.containsKey(END_LINE)) {
            int startLine = ((Number) parameters.get(START_LINE)).intValue();
            int endLine = ((Number) parameters.get(END_LINE)).intValue();
            
            var lines = Files.readAllLines(path);
            if (startLine < 1 || endLine > lines.size() || startLine > endLine) {
                throw new IllegalArgumentException("Invalid line range");
            }
            
            return String.join("\n", 
                lines.subList(startLine - 1, Math.min(endLine, lines.size())));
        }

        // Otherwise read entire file
        return Files.readString(path);
    }

    private String writeFile(String filePath, Map<String, Object> parameters) throws Exception {
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("File path is required for write operation");
        }
        if (!parameters.containsKey(CONTENT)) {
            throw new IllegalArgumentException("Content is required for write operation");
        }

        String content = parameters.get(CONTENT).toString();
        Path path = Paths.get(filePath);
        Files.writeString(path, content);
        return "File written successfully";
    }

    private String listDirectory(String directoryPath) throws Exception {
        Path dir = directoryPath.isEmpty() ? 
            Paths.get(".") : Paths.get(directoryPath);
            
        if (!Files.exists(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
        }

        var files = Files.list(dir)
                .map(Path::toString)
                .sorted()
                .toList();
        return String.join("\n", files);
    }
} 