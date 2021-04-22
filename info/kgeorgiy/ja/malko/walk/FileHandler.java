package info.kgeorgiy.ja.malko.walk;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class FileHandler extends SimpleFileVisitor<Path> {
    private final BufferedWriter resultWriter;
    private final byte[] buffer = new byte[1024 * 4];

    public FileHandler(final BufferedWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    @Override
    public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) {
        try (final InputStream fileInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            long hash = 0;
            final long upperLongByte = 0xFF00_0000_0000_0000L;

            int c;
            while ((c = fileInputStream.read(buffer)) >= 0) {
                for (int i = 0; i < c; i++) {
                    final byte b = buffer[i];
                    hash = (hash << 8) + (b & 0xff);
                    final long high = hash & upperLongByte;
                    if (high != 0) {
                        hash = hash ^ (high >> 48);
                        hash = hash & ~high;
                    }
                }
            }
            // :NOTE: Двойная запись
            writeResult(hash, path);
        } catch (final IOException e) {
            writeErrorResult(path);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path path, final IOException exc) {
        writeErrorResult(path);
        return FileVisitResult.CONTINUE;
    }

    private void writeResult(final long hash, final String pathStr) {
        try {
            resultWriter.write(String.format("%016x %s%n", hash, pathStr));
        } catch (final IOException e) {
            throw new WalkerException("Unable to write into output file.", e);
        }
    }

    private void writeResult(final long hash, final Path path) {
        writeResult(hash, path.toString());
    }

    private void writeErrorResult(final Path path) {
        writeResult(0, path);
    }

    public void writeErrorResult(final String pathStr) {
        writeResult(0, pathStr);
    }

    public void walkFileTree(final Path path, final int depth) {
        try {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), depth, this);
        } catch (final IOException e) {
            // Never entering here.
            throw new AssertionError(e);
        } catch (final SecurityException e) {
            throw new WalkerException("Not enough permissions for walking file tree of '" + path.toString() + "'", e);
        }
    }
}
