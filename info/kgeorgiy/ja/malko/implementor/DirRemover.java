package info.kgeorgiy.ja.malko.implementor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class for recursively removing dirs
 */
public class DirRemover extends SimpleFileVisitor<Path> {

    /**
     * Removes visiting file.
     *
     * @param file  visiting file
     * @param attrs file attributes
     * @return code to continue visiting
     * @throws IOException when deletion of file throws it
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.deleteIfExists(file);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Removes visiting directory.
     *
     * @param dir visiting directory
     * @param exc exception thrown while {@code dir} was walked.
     * @return code to continue visiting
     * @throws IOException if occurred
     *                     <ul>
     *                         <li>while deleting {@code dir}</li>
     *                         <li>in walk recursion</li>
     *                     </ul>
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
            throw exc;
        }
        Files.deleteIfExists(dir);
        return FileVisitResult.CONTINUE;
    }
}
