package com.browxy.balloons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.config.Config;

public class ShellUtils {
  private static Logger logger = LoggerFactory.getLogger(ShellUtils.class);

  public static String executeScript(String script, boolean throwExceptionOnFailedResultCode,
      boolean readOutput) {
    return executeScript(script, throwExceptionOnFailedResultCode, readOutput, 60);
  }

  public static String executeScript(String script, boolean throwExceptionOnFailedResultCode,
      boolean readOutput, int timeoutInSeconds) {
    ProcessBuilder pb = new ProcessBuilder("bash", "-c", script);
    pb.redirectErrorStream(true);
    String result = null;
    Process process = null;
    String errorOutput = null;
    Integer processExitValue = null;
    try {
      process = pb.start();
      process.waitFor(timeoutInSeconds, TimeUnit.SECONDS);
      if (readOutput) {
        result = getStringFromStream(process.getInputStream());
      }
      processExitValue = process.exitValue();
      if (processExitValue != 0) {
        if (result == null) {
          result = getStringFromStream(process.getInputStream());
        }
        errorOutput = buildExecuteScriptOutput("execution of script returned a non 0 exit code",
            script, result, processExitValue);
        if (throwExceptionOnFailedResultCode) {
          throw new RuntimeException(errorOutput);
        } else {
          logger.error(errorOutput);
        }
      }
      return result;
    } catch (Exception exception) {
      throw new RuntimeException(
          buildExecuteScriptOutput("execution of script generated an exception", script, result,
              processExitValue),
          exception);
    } finally {
      try {
        if (process != null) {
          process.getErrorStream().close();
          process.getInputStream().close();
          process.getOutputStream().close();
          process.destroy();
        }
      } catch (Exception e1) {
        logger.error(buildExecuteScriptOutput("execution of script was unable to destroy process",
            script, result, processExitValue), e1);
      }
    }
  }

  private static String buildExecuteScriptOutput(String message, String script, String result,
      Integer exitCode) {
    StringBuilder output = new StringBuilder();
    output.append(message);
    output.append(" / script: " + script);
    output.append(" / exitCode: " + exitCode);
    if (result != null) {
      output.append(" / result: " + result);
    }
    return output.toString();
  }

  public static void executeScriptNoOutput(String script,
      boolean throwExceptionOnFailedResultCode) {
    executeScript(script, throwExceptionOnFailedResultCode, false);
  }

  public static String getStringFromStream(InputStream isr) throws IOException {
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(isr))) {
      StringBuffer buf = new StringBuffer();
      String line;
      while ((line = buffer.readLine()) != null) {
        buf.append(line + "\n");
      }
      return buf.toString();
    }
  }

  public static Process execute(String[] command, String[] envp, File dir) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));
    pb.directory(dir);
    pb.redirectErrorStream(true);
    Process pr = pb.start();
    return pr;
  }

  public static String generateFileTree(String path) {
    try {
      return ShellUtils.executeScript("cd " + path + " && find . | sort", true, true);
    } catch (Exception e1) {
      throw new RuntimeException(e1);
    }
  }

  public static void createExecutableJar(String sourcePath, String manifestFileName,
      String jarFileName) {
    try {
      executeScript(
          "cd " + sourcePath + " && jar cvfm " + jarFileName + " " + manifestFileName + " *", true,
          true);
    } catch (Exception e1) {
      FileManager.detectAndThrowNoSpaceLeftOnDeviceException(e1);
      throw (e1);
    }
  }

  // FIXME change implementation to get only the needed information without grep or awk
  // df /dev/loop27 --output=used
  public static Long getUsedSpaceInKilobytes(String deviceId) {
    return executeNumericResultDfCommand(deviceId, "used", "Used");
  }

  public static Long getFreeSpaceInKilobytes(String deviceId) {
    return executeNumericResultDfCommand(deviceId, "avail", "Available");
  }

  private static Long executeNumericResultDfCommand(String deviceId, String field,
      String headerName) {
    String result = null;
    try {
      result = executeDfCommand(deviceId, field, headerName);
      return Long.parseLong(result);
    } catch (Exception e1) {
      throw new RuntimeException("Invalid result df command result: deviceId: " + deviceId
          + " field: " + field + " output: " + result);
    }
  }

  private static String executeDfCommand(String deviceId, String field, String headerName) {
    String result = executeScript("df -k --output=" + field + " " + deviceId, true, true);
    String[] resultByLines = result.split("\n");
    if (resultByLines.length == 2 && headerName.equals(headerName.trim())) {
      return result.split("\n")[1].trim();
    } else {
      throw new RuntimeException("Invalid result df command result: deviceId: " + deviceId
          + " field: " + field + " output: " + result);
    }
  }

  public static String getLoopDevice(String path) {
    return executeDfCommand(path, "source", "Filesystem");
  }

  public static void umountLinuxFileSystemIfMounted(String path) {
    executeScriptNoOutput(
        "if mountpoint -q " + path + " ; then sudo umount -f -l -d " + path + " ; fi", true);
  }

  public static void umountLinuxFileSystem(String path) {
    executeScriptNoOutput("sudo umount -f -l -d " + path, true);
  }

  public static String getMountedPaths(String path) {
    return executeScript("mount | grep \"" + path + "\" || true", true, true);
  }

  public static void createLinuxFileSystem(String extFileName, int sizeInKilobytes) {
    String blockSize = Config.getInstance().getStringValue("shell.blockSize", "1k");
    executeScriptNoOutput("dd if=/dev/zero of=" + extFileName + " bs=" + blockSize + " count=" + sizeInKilobytes,
        true);
    executeScriptNoOutput("sudo mkfs.ext2 -m 0 -O ^resize_inode -i 10240 -F " + extFileName, true);
  }

  public static void mountLinuxFileSystem(String extFile, String path) {
    executeScriptNoOutput("mkdir -p " + path, true);
    executeScript("sudo mount -t ext2 -o acl -v " + extFile + " " + path, true, true);
  }

  public static void sharePathPermissions(String path, String group, String user) throws Exception {
    FileManager.changeOwnership(path, user, group, true, true);
    executeScriptNoOutput("setfacl -R -d -m group:" + group + ":rwx " + path, true);
  }

  public static void createRandomFile(String file, int sizeInKilobytes) {
    String blockSize = Config.getInstance().getStringValue("shell.blockSize", "1k");
    executeScript("dd if=/dev/urandom of=" + file + " bs=" + blockSize + " count=" + sizeInKilobytes, true, true);
  }

  public static String getGroupName(String user) {
    return executeScript("id -gn " + user, true, true);
  }

  public static int getOpenFilesCount() {
    try {
      return Integer
          .parseInt(executeScript("sudo lsof -w | wc -l", true, true).trim().replace("\n", ""));
    } catch (Exception e1) {
      throw new RuntimeException(e1);
    }
  }

  public static int getProcessCount() {
    try {
      return Integer
          .parseInt(executeScript("sudo ps aux | wc -l", true, true).trim().replace("\n", ""));
    } catch (Exception e1) {
      throw new RuntimeException(e1);
    }
  }

}
