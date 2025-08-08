package com.browxy.balloons.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import com.browxy.balloons.exceptions.InformationEnabledException;
import com.browxy.balloons.exceptions.NoSpaceLeftOnDeviceException;


public class FileManager {

    public enum TimeMeasurement {
        SECOND, MINUTE, HOUR, DAY
    }

    public static String readResource(String fileName, String encoding, Boolean preserveCR) throws IOException {
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        try (InputStream fstream = FileManager.class.getClassLoader().getResourceAsStream(fileName); BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(fstream), encoding));) {
            StringBuffer fileContent = new StringBuffer();
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                fileContent.append(strLine);
                if (preserveCR) {
                    fileContent.append("\n");
                }
            }
            return fileContent.toString();
        } catch (Exception e) {
            InformationEnabledException iee = new InformationEnabledException(e);
            iee.addInformation("fileName", fileName);
            throw iee;
        }
    }

    public static String readFile(String completeFilePath) throws FileNotFoundException {
        return readFile(completeFilePath, "UTF-8");
    }

    public static String readFile(String fileName, String encoding) throws FileNotFoundException {
        return readFile(new FileInputStream(fileName), encoding);
    }

    public static String readFile(InputStream inputStream, String encoding) {
        return readFile(inputStream, encoding, true);
    }

    public static String readFile(InputStream inputStream, String encoding, boolean preserveCR) {
        StringBuilder strBuilder = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding))) {
            strBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line + (preserveCR ? '\n' : ""));
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        return strBuilder.toString();
    }

    private static String getRemoveFilesCommand(String path, boolean recursive, boolean force, boolean useSudo) {
        String flags = "";
        if (recursive) {
            flags += "r";
        }
        if (force) {
            flags += "f";
        }
        String command = "";
        if (useSudo) {
            command = "sudo ";
        }
        command += "rm";
        if (flags.length() > 0) {
            command += " -" + flags;
        }
        return command + " " + path;
    }

    public static void deleteFiles(String path, boolean recursive, boolean force, boolean useSudo) {
        ShellUtils.executeScript(getRemoveFilesCommand(path, recursive, force, useSudo), true, true);
    }

    public static void deleteDirectoryRecursive(File path) {
        try {
            if (path.exists()) {
                ShellUtils.executeScriptNoOutput("rm -rf " + path.getAbsolutePath().replace(" ", "\\ "), true);
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    public static void createDirectory(String path) {
        try {
            ShellUtils.executeScriptNoOutput("mkdir -p " + path.replace(" ", "\\ "), true);
        } catch (Exception e1) {
            FileManager.detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void createParentPath(String file) {
        createDirectory(new File(file).getParent());
    }

    public static void createEmptyFile(String path) {
        try {
            ShellUtils.executeScriptNoOutput("touch " + path.replace(" ", "\\ "), true);
        } catch (Exception e1) {
            FileManager.detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void tar(String path, String targetFileName) {
        try {
          ShellUtils.executeScriptNoOutput("cd " + path + " && tar --format gnu --xz --sort=name --mtime=\"2020-08-01 00:00Z\" --owner=0 --group=0 --numeric-owner -cf " + targetFileName + " -C " + path + " .", true);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void unTar(String file, String path) {
        try {
            ShellUtils.executeScriptNoOutput("tar xvf " + file + " -C " + path, true);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            if (e1.getMessage() != null && e1.getMessage().indexOf("Wrote only") > -1) {
                throw new NoSpaceLeftOnDeviceException(e1);
            }
            throw new RuntimeException(e1);
        }
    }

    public static void zip(String targetPath, boolean recursePaths, String zipFileName) {
        try {
            String zipCommand = "zip";
            if (recursePaths) {
                zipCommand += " -r";
            }
            String command = "cd " + targetPath + " && " + zipCommand + " " + zipFileName + " -UN=UTF8 .";
            ShellUtils.executeScript(command, true, false, 180);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void unzip(String zipFile, String path) {
        unzip(zipFile,null,path);
    }

    public static void unzip(String zipFile, String flags, String path) {
        try {
            ShellUtils.executeScript("unzip " + (flags != null ? flags + " " : "") + zipFile + " -d " + path, true, true);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void copy(String from, String to, boolean force, boolean recursive) {
        String flags = "";
        if (recursive) {
            flags += "r";
        }
        if (force) {
            flags += "f";
        }
        String command = "cp" + (flags != null ? " -" + flags : "") + " " + from + " " + to;
        try {
            ShellUtils.executeScriptNoOutput(command, true);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void saveStreamToFile(InputStream stream, String completeFilePath) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(completeFilePath)); BufferedInputStream in = new BufferedInputStream(stream);) {
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void saveStringToFile(String fileName, String source, String encoding) {
        try (OutputStreamWriter bw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8")) {
            if (encoding == null) {
                encoding = "UTF-16";
            }
            bw.write(source);
        } catch (IOException e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    public static void saveUTF8File(String fileName, String source) {
        saveStringToFile(fileName, source, "UTF-8");
    }

    public static void removeDirectoryContents(String directory) {
        try {
            ShellUtils.executeScriptNoOutput("rm -rf " + directory + "/*", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String computePath(String basePath, String subPath) {
        if (subPath != null && subPath.length() > 0) {
            return basePath + "/" + subPath;
        } else {
            return basePath;
        }
    }

    public static String computeFilePath(String path, String fileName) {
        if (path == null) {
            return "/" + fileName;
        } else {
            return path + "/" + fileName;
        }
    }

    public static boolean isSubDirectory(File base, File child) throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }

    public static void move(String source, String target) {
        try {
            ShellUtils.executeScriptNoOutput("mv " + source + " " + target, true);
        } catch (Exception e1) {
            detectAndThrowNoSpaceLeftOnDeviceException(e1);
            throw new RuntimeException(e1);
        }
    }

    public static void changePermissions(String path, String permissions, boolean recursive) {
        String flags = "";
        if (recursive) {
            flags += "R";
        }
        String command = "sudo chmod";
        if (flags.length() > 0) {
            command += " -" + flags;
        }
        command += " " + permissions + " " + path;
        ShellUtils.executeScriptNoOutput(command, true);
    }

    public static String getChangeOwnershipCommand(String path, String user, String group, boolean recursive , boolean useSudo) {
        String flags = "";
        String command = "";
        if (recursive) {
            flags += "R";
        }
        if (useSudo) {
            command = "sudo chown";
        } else {
          command = "chown";
        }
        if (flags.length() > 0) {
            command += " -" + flags;
        }
        command += " " + user + ":" + group + " " + path;
        return command;
    }

    public static void changeOwnership(String path, String user, String group, boolean recursive , boolean useSudo) {
        ShellUtils.executeScriptNoOutput(getChangeOwnershipCommand(path,user,group,recursive,useSudo), true);
    }

    public static void detectAndThrowNoSpaceLeftOnDeviceException(Exception e1) {
        if (e1.getMessage() != null && e1.getMessage().indexOf("No space left on device") > -1) {
            throw new NoSpaceLeftOnDeviceException(e1);
        }
    }

    public static String createTimeBasedFileName() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.HOUR_OF_DAY) + "-" + today.get(Calendar.MINUTE) + "-" + today.get(Calendar.SECOND) + "-" + today.get(Calendar.MILLISECOND);
    }

    private static String getFindTimeExpression(TimeMeasurement timeMeasurement, int value) {
        switch (timeMeasurement) {
        case SECOND:
            return "-mmin +" + value / 60;
        case MINUTE:
            return "-mmin +" + value;
        case HOUR:
            return "-mmin +" + value * 60;
        case DAY:
            return "-mtime +" + value;
        default:
            throw new RuntimeException(timeMeasurement.toString() + " does not have a valid time conversion");
        }
    }

    public static void removeExpiredFiles(String basePathToDelete, TimeMeasurement timeMeasurement, int value) {
        if (new File(basePathToDelete).exists()) {
            String removeFilesCommand = getRemoveFilesCommand("{}", true, true, false);
            ShellUtils.executeScript("cd " + basePathToDelete + " && find . " + getFindTimeExpression(timeMeasurement, value) + " -exec " + removeFilesCommand + " \\;", true, true);
        }
    }

    public static void writeFile(InputStream inputStream, String fileName) throws IOException {
        File targetFile = new File(fileName);
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outStream);
    }


    public static void setPermissions(String path) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Path folderPath = Paths.get(path);
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                // perms.add(PosixFilePermission.OTHERS_EXECUTE);
                Files.setPosixFilePermissions(folderPath, perms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
