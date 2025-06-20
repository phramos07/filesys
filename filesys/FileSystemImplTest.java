package filesys;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import exception.CaminhoJaExistenteException;
import exception.CaminhoNaoEncontradoException;
import exception.PermissaoException;

import java.util.Arrays;

public class FileSystemImplTest {

    private static final String ROOT_USER = "root";
    private static final String TEST_USER = "testuser";
    private static final String OTHER_USER = "otheruser";

    private IFileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = new FileSystemImpl();

        ((FileSystemImpl) fileSystem).addUser(TEST_USER);
        ((FileSystemImpl) fileSystem).addUser(OTHER_USER);
    }


    @Test
    public void testMkdirSuccess() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);

        try {
            fileSystem.mkdir("/testdir", ROOT_USER);
            fail("Deveria lançar CaminhoJaExistenteException");
        } catch (CaminhoJaExistenteException e) {
        }
    }

    @Test
    public void testMkdirNestedSuccess() throws Exception {
        fileSystem.mkdir("/parent", ROOT_USER);
        fileSystem.mkdir("/parent/child", ROOT_USER);

        try {
            fileSystem.mkdir("/parent/child", ROOT_USER);
            fail("Deveria lançar CaminhoJaExistenteException");
        } catch (CaminhoJaExistenteException e) {
        }
    }

    @Test(expected = CaminhoJaExistenteException.class)
    public void testMkdirAlreadyExists() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);
        fileSystem.mkdir("/testdir", ROOT_USER); // Deve lançar exceção
    }

    @Test(expected = PermissaoException.class)
    public void testMkdirNoPermission() throws Exception {
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.mkdir("/restricted/subdir", TEST_USER); // Deve lançar exceção
    }

    @Test(expected = PermissaoException.class)
    public void testMkdirParentNotFound() throws Exception {
        fileSystem.mkdir("/nonexistent/testdir", ROOT_USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMkdirNullPath() throws Exception {
        fileSystem.mkdir(null, ROOT_USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMkdirEmptyPath() throws Exception {
        fileSystem.mkdir("", ROOT_USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMkdirNullUser() throws Exception {
        fileSystem.mkdir("/testdir", null);
    }

    @Test(expected = PermissaoException.class)
    public void testMkdirNonexistentUser() throws Exception {
        fileSystem.mkdir("/testdir", "nonexistentuser");
    }


    @Test
    public void testChmodSuccess() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);

        fileSystem.chmod("/testdir", ROOT_USER, TEST_USER, "rwx");

        fileSystem.mkdir("/testdir/subdir", TEST_USER);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testChmodPathNotFound() throws Exception {
        fileSystem.chmod("/nonexistent", ROOT_USER, TEST_USER, "rwx");
    }

    @Test(expected = PermissaoException.class)
    public void testChmodNoPermission() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);

        fileSystem.chmod("/testdir", TEST_USER, OTHER_USER, "rwx");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChmodInvalidPermission() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);
        fileSystem.chmod("/testdir", ROOT_USER, TEST_USER, "invalid");
    }

    @Test
    public void testChmodAsOwner() throws Exception {
        fileSystem.mkdir("/userdir", ROOT_USER);

        fileSystem.chmod("/userdir", ROOT_USER, TEST_USER, "rwx");

        fileSystem.mkdir("/userdir/testdir", TEST_USER);

        fileSystem.chmod("/userdir/testdir", TEST_USER, OTHER_USER, "r--");

        try {
            fileSystem.mkdir("/userdir/testdir/subdir", OTHER_USER);
            fail("Deveria lançar PermissaoException");
        } catch (PermissaoException e) {

        }
    }


    @Test
    public void testRmFileSuccess() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);

        fileSystem.rm("/testfile", ROOT_USER, false);

        try {
            byte[] buffer = new byte[10];
            fileSystem.read("/testfile", ROOT_USER, buffer);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }
    }

    @Test
    public void testRmDirectorySuccess() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);

        fileSystem.rm("/testdir", ROOT_USER, false);

        try {
            fileSystem.ls("/testdir", ROOT_USER, false);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }
    }

    @Test
    public void testRmRecursiveSuccess() throws Exception {
        fileSystem.mkdir("/parent", ROOT_USER);
        fileSystem.mkdir("/parent/child", ROOT_USER);
        fileSystem.touch("/parent/file", ROOT_USER);

        fileSystem.rm("/parent", ROOT_USER, true);

        try {
            fileSystem.ls("/parent", ROOT_USER, false);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }
    }

    @Test(expected = PermissaoException.class)
    public void testRmNonEmptyDirectoryNonRecursive() throws Exception {
        fileSystem.mkdir("/parent", ROOT_USER);
        fileSystem.mkdir("/parent/child", ROOT_USER);

        fileSystem.rm("/parent", ROOT_USER, false);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testRmPathNotFound() throws Exception {
        fileSystem.rm("/nonexistent", ROOT_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testRmNoPermission() throws Exception {
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.rm("/restricted", TEST_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testRmRootDirectory() throws Exception {
        fileSystem.rm("/", ROOT_USER, true);
    }



    @Test
    public void testTouchSuccess() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);

        byte[] buffer = new byte[0];
        fileSystem.read("/testfile", ROOT_USER, buffer);
    }

    @Test
    public void testTouchInSubdirectory() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);

        fileSystem.touch("/testdir/testfile", ROOT_USER);

        byte[] buffer = new byte[0];
        fileSystem.read("/testdir/testfile", ROOT_USER, buffer);
    }

    @Test(expected = CaminhoJaExistenteException.class)
    public void testTouchFileAlreadyExists() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);
        fileSystem.touch("/testfile", ROOT_USER);
    }

    @Test(expected = CaminhoJaExistenteException.class)
    public void testTouchDirectoryExists() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);
        fileSystem.touch("/testdir", ROOT_USER);
    }

    @Test(expected = PermissaoException.class)
    public void testTouchNoPermission() throws Exception {
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.touch("/restricted/testfile", TEST_USER);
    }

    @Test(expected = PermissaoException.class)
    public void testTouchParentNotFound() throws Exception {
        fileSystem.touch("/nonexistent/testfile", ROOT_USER);
    }



    @Test
    public void testWriteSuccess() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);

        byte[] dataToWrite = "Hello, World!".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, dataToWrite);

        byte[] buffer = new byte[20];
        fileSystem.read("/testfile", ROOT_USER, buffer);

        byte[] expected = Arrays.copyOf(dataToWrite, dataToWrite.length);
        byte[] actual = Arrays.copyOf(buffer, dataToWrite.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testWriteAppend() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);

        byte[] initialData = "Hello, ".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, initialData);

        byte[] additionalData = "World!".getBytes();
        fileSystem.write("/testfile", ROOT_USER, true, additionalData);

        byte[] buffer = new byte[20];
        fileSystem.read("/testfile", ROOT_USER, buffer);

        byte[] expected = "Hello, World!".getBytes();
        byte[] actual = Arrays.copyOf(buffer, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testWriteOverwrite() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);

        byte[] initialData = "Initial content".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, initialData);

        byte[] newData = "New content".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, newData);

        byte[] buffer = new byte[20];
        fileSystem.read("/testfile", ROOT_USER, buffer);

        byte[] expected = newData;
        byte[] actual = Arrays.copyOf(buffer, newData.length);

        assertArrayEquals(expected, actual);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testWriteFileNotFound() throws Exception {
        byte[] data = "Test".getBytes();
        fileSystem.write("/nonexistent", ROOT_USER, false, data);
    }

    @Test(expected = PermissaoException.class)
    public void testWriteNoPermission() throws Exception {
        fileSystem.touch("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        byte[] data = "Test".getBytes();
        fileSystem.write("/restricted", TEST_USER, false, data);
    }



    @Test
    public void testReadSuccess() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);
        byte[] dataToWrite = "Test data".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, dataToWrite);

        byte[] buffer = new byte[20];
        fileSystem.read("/testfile", ROOT_USER, buffer);

        byte[] expected = dataToWrite;
        byte[] actual = Arrays.copyOf(buffer, dataToWrite.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testReadEmptyFile() throws Exception {
        fileSystem.touch("/emptyfile", ROOT_USER);

        byte[] buffer = new byte[10];
        fileSystem.read("/emptyfile", ROOT_USER, buffer);

        byte[] expected = new byte[0];
        byte[] actual = Arrays.copyOf(buffer, 0);

        assertArrayEquals(expected, actual);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testReadFileNotFound() throws Exception {
        byte[] buffer = new byte[10];
        fileSystem.read("/nonexistent", ROOT_USER, buffer);
    }

    @Test(expected = PermissaoException.class)
    public void testReadNoPermission() throws Exception {
        fileSystem.touch("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "-wx");

        byte[] buffer = new byte[10];
        fileSystem.read("/restricted", TEST_USER, buffer);
    }

    @Test
    public void testReadBufferSmallerThanContent() throws Exception {
        fileSystem.touch("/testfile", ROOT_USER);
        byte[] dataToWrite = "This is a long string that won't fit in a small buffer".getBytes();
        fileSystem.write("/testfile", ROOT_USER, false, dataToWrite);

        byte[] smallBuffer = new byte[10];
        fileSystem.read("/testfile", ROOT_USER, smallBuffer);

        byte[] expected = Arrays.copyOf(dataToWrite, 10);
        assertArrayEquals(expected, smallBuffer);
    }


    @Test
    public void testMvFileSuccess() throws Exception {
        fileSystem.touch("/sourcefile", ROOT_USER);
        byte[] data = "Test data".getBytes();
        fileSystem.write("/sourcefile", ROOT_USER, false, data);

        fileSystem.mv("/sourcefile", "/destfile", ROOT_USER);

        try {
            byte[] buffer = new byte[10];
            fileSystem.read("/sourcefile", ROOT_USER, buffer);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }

        byte[] buffer = new byte[20];
        fileSystem.read("/destfile", ROOT_USER, buffer);

        byte[] expected = data;
        byte[] actual = Arrays.copyOf(buffer, data.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testMvDirectorySuccess() throws Exception {
        fileSystem.mkdir("/sourcedir", ROOT_USER);
        fileSystem.touch("/sourcedir/file", ROOT_USER);

        fileSystem.mv("/sourcedir", "/destdir", ROOT_USER);

        try {
            fileSystem.ls("/sourcedir", ROOT_USER, false);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }

        byte[] buffer = new byte[0];
        fileSystem.read("/destdir/file", ROOT_USER, buffer);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testMvSourceNotFound() throws Exception {
        fileSystem.mv("/nonexistent", "/dest", ROOT_USER);
    }

    @Test(expected = PermissaoException.class)
    public void testMvNoPermissionSource() throws Exception {
        fileSystem.mkdir("/restricted", ROOT_USER);
        fileSystem.touch("/restricted/file", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.mv("/restricted/file", "/dest", TEST_USER);
    }

    @Test(expected = PermissaoException.class)
    public void testMvNoPermissionDest() throws Exception {
        fileSystem.mkdir("/source", ROOT_USER);
        fileSystem.touch("/source/file", ROOT_USER);
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/source", ROOT_USER, TEST_USER, "rwx");
        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.mv("/source/file", "/restricted/file", TEST_USER);
    }

    @Test(expected = PermissaoException.class)
    public void testMvRootDirectory() throws Exception {
        fileSystem.mv("/", "/newroot", ROOT_USER);
    }

    @Test
    public void testMvDestinationExists() throws Exception {
        fileSystem.touch("/source", ROOT_USER);
        byte[] sourceData = "Source data".getBytes();
        fileSystem.write("/source", ROOT_USER, false, sourceData);

        fileSystem.touch("/dest", ROOT_USER);
        byte[] destData = "Destination data".getBytes();
        fileSystem.write("/dest", ROOT_USER, false, destData);

        fileSystem.mv("/source", "/dest", ROOT_USER);

        try {
            byte[] buffer = new byte[20];
            fileSystem.read("/source", ROOT_USER, buffer);
            fail("Deveria lançar CaminhoNaoEncontradoException");
        } catch (CaminhoNaoEncontradoException e) {
        }

        byte[] buffer = new byte[20];
        fileSystem.read("/dest", ROOT_USER, buffer);

        byte[] expected = sourceData;
        byte[] actual = Arrays.copyOf(buffer, sourceData.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void testLsEmptyDirectory() throws Exception {
        fileSystem.mkdir("/emptydir", ROOT_USER);

        fileSystem.ls("/emptydir", ROOT_USER, false);
    }

    @Test
    public void testLsNonEmptyDirectory() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);
        fileSystem.touch("/testdir/file1", ROOT_USER);
        fileSystem.touch("/testdir/file2", ROOT_USER);
        fileSystem.mkdir("/testdir/subdir", ROOT_USER);

        fileSystem.ls("/testdir", ROOT_USER, false);
    }

    @Test
    public void testLsRecursive() throws Exception {
        fileSystem.mkdir("/testdir", ROOT_USER);
        fileSystem.touch("/testdir/file1", ROOT_USER);
        fileSystem.mkdir("/testdir/subdir", ROOT_USER);
        fileSystem.touch("/testdir/subdir/file2", ROOT_USER);

        fileSystem.ls("/testdir", ROOT_USER, true);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testLsDirectoryNotFound() throws Exception {
        fileSystem.ls("/nonexistent", ROOT_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testLsNoPermission() throws Exception {
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "-wx");

        fileSystem.ls("/restricted", TEST_USER, false);
    }



    @Test
    public void testCpFileSuccess() throws Exception {
        fileSystem.touch("/sourcefile", ROOT_USER);
        byte[] data = "Test data".getBytes();
        fileSystem.write("/sourcefile", ROOT_USER, false, data);

        fileSystem.cp("/sourcefile", "/destfile", ROOT_USER, false);

        byte[] sourceBuffer = new byte[20];
        fileSystem.read("/sourcefile", ROOT_USER, sourceBuffer);

        byte[] destBuffer = new byte[20];
        fileSystem.read("/destfile", ROOT_USER, destBuffer);

        assertArrayEquals(sourceBuffer, destBuffer);
    }

    @Test
    public void testCpDirectoryRecursive() throws Exception {
        fileSystem.mkdir("/sourcedir", ROOT_USER);
        fileSystem.touch("/sourcedir/file", ROOT_USER);
        byte[] data = "Test data".getBytes();
        fileSystem.write("/sourcedir/file", ROOT_USER, false, data);
        fileSystem.mkdir("/sourcedir/subdir", ROOT_USER);
        fileSystem.touch("/sourcedir/subdir/file2", ROOT_USER);

        fileSystem.cp("/sourcedir", "/destdir", ROOT_USER, true);

        byte[] buffer = new byte[20];
        fileSystem.read("/destdir/file", ROOT_USER, buffer);

        byte[] expected = data;
        byte[] actual = Arrays.copyOf(buffer, data.length);

        assertArrayEquals(expected, actual);

        byte[] buffer2 = new byte[0];
        fileSystem.read("/destdir/subdir/file2", ROOT_USER, buffer2);
    }

    @Test(expected = PermissaoException.class)
    public void testCpDirectoryNonRecursive() throws Exception {
        fileSystem.mkdir("/sourcedir", ROOT_USER);

        fileSystem.cp("/sourcedir", "/destdir", ROOT_USER, false);
    }

    @Test(expected = CaminhoNaoEncontradoException.class)
    public void testCpSourceNotFound() throws Exception {
        fileSystem.cp("/nonexistent", "/dest", ROOT_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testCpNoReadPermission() throws Exception {
        fileSystem.touch("/restricted", ROOT_USER);

        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "-wx");

        fileSystem.cp("/restricted", "/dest", TEST_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testCpNoWritePermission() throws Exception {
        fileSystem.touch("/source", ROOT_USER);
        fileSystem.mkdir("/restricted", ROOT_USER);

        fileSystem.chmod("/source", ROOT_USER, TEST_USER, "r-x");
        fileSystem.chmod("/restricted", ROOT_USER, TEST_USER, "r-x");

        fileSystem.cp("/source", "/restricted/dest", TEST_USER, false);
    }

    @Test(expected = PermissaoException.class)
    public void testCpDestinationExists() throws Exception {
        fileSystem.touch("/source", ROOT_USER);
        fileSystem.touch("/dest", ROOT_USER);

        fileSystem.cp("/source", "/dest", ROOT_USER, false);
    }



    @Test
    public void testComplexScenario() throws Exception {
        fileSystem.mkdir("/home", ROOT_USER);
        fileSystem.mkdir("/home/user1", ROOT_USER);
        fileSystem.mkdir("/home/user2", ROOT_USER);

        fileSystem.chmod("/home/user1", ROOT_USER, TEST_USER, "rwx");
        fileSystem.chmod("/home/user2", ROOT_USER, OTHER_USER, "rwx");

        fileSystem.touch("/home/user1/file1", TEST_USER);
        byte[] data1 = "User 1 data".getBytes();
        fileSystem.write("/home/user1/file1", TEST_USER, false, data1);

        fileSystem.touch("/home/user2/file2", OTHER_USER);
        byte[] data2 = "User 2 data".getBytes();
        fileSystem.write("/home/user2/file2", OTHER_USER, false, data2);

        try {
            byte[] buffer = new byte[20];
            fileSystem.read("/home/user2/file2", TEST_USER, buffer);
            fail("Deveria lançar PermissaoException");
        } catch (PermissaoException e) {
        }

        byte[] buffer1 = new byte[20];
        fileSystem.read("/home/user1/file1", ROOT_USER, buffer1);

        byte[] buffer2 = new byte[20];
        fileSystem.read("/home/user2/file2", ROOT_USER, buffer2);

        fileSystem.chmod("/home/user2/file2", OTHER_USER, TEST_USER, "r--");

        byte[] buffer3 = new byte[20];
        fileSystem.read("/home/user2/file2", TEST_USER, buffer3);

        try {
            fileSystem.write("/home/user2/file2", TEST_USER, false, "Attempt to modify".getBytes());
            fail("Deveria lançar PermissaoException");
        } catch (PermissaoException e) {
        }
    }
}