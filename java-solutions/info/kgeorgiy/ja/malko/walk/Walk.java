package info.kgeorgiy.ja.malko.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {

    private final int depth;
    public Walk(final int depth) {
        this.depth = depth;
    }

    public void walk(final String inputPathStr, final String outputPathStr) {
        try (final BufferedReader input = Files.newBufferedReader(getPath(inputPathStr))) {
            try (final BufferedWriter output = Files.newBufferedWriter(makeOutputPath(outputPathStr))) {
                walk(input, output);
            } catch (final IOException e) {
                throw new WalkerException("Can't read output file.", e);
            } catch (final SecurityException e) {
                throw new WalkerException("Can't read output file because of security problems.", e);
            }
        } catch (final IOException e) {
            throw new WalkerException("Can't read input file.", e);
        } catch (final SecurityException e) {
            throw new WalkerException("Can't read input file because of security problems.", e);
        }
    }

    private static Path makeOutputPath(final String pathStr) {
        final Path path = getPath(pathStr);
        try {
            final Path parentPath = path.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
        } catch (final IOException | SecurityException ignored) {
        }
        return path;
    }

    private void walk(final BufferedReader input, final BufferedWriter output) {
        final FileHandler handler = new FileHandler(output);
        try {
            String pathStr;
            while ((pathStr = input.readLine()) != null) {
                try {
                    handler.walkFileTree(Paths.get(pathStr), depth);
                } catch (final InvalidPathException e) {
                    handler.writeErrorResult(pathStr);
                }
            }
        } catch (final IOException e) {
            throw new WalkerException("Exception while reading input file", e);
        }
    }

    private static Path getPath(final String s) {
        try {
            return Paths.get(s);
        } catch (final InvalidPathException e) {
            throw new WalkerException("Can't convert string '" + s + "' to path.", e);
        }
    }

    public static void main(final String[] args) {
        run(args, 0);
    }

    protected static void run(final String[] args, final int depth) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new WalkerException("2 arguments required.");
            }
            new Walk(depth).walk(args[0], args[1]);
        } catch (final WalkerException e) {
            System.err.println(e.getMessage());
        }
    }
}
