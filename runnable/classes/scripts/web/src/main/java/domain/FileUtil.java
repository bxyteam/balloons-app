package domain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileUtil {
  private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
  private static final ConcurrentHashMap<String, LockRef> fileLocks = new ConcurrentHashMap<>();
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  // Clase auxiliar para mantener el lock y su contador de uso
  private static class LockRef {
    final ReentrantLock lock = new ReentrantLock();
    final AtomicInteger usageCount = new AtomicInteger(0);
    volatile boolean marked = false; // para marcar como candidato a eliminación
  }

  /**
   * Adquiere un lock para el archivo especificado
   */
  private static LockRef acquireLock(String filePath) {
    while (true) {
      LockRef ref = fileLocks.computeIfAbsent(filePath, k -> new LockRef());

      // Incrementar contador atómicamente
      int count = ref.usageCount.incrementAndGet();

      // Si el objeto está marcado para eliminación, reintentar
      if (ref.marked) {
        ref.usageCount.decrementAndGet();
        continue;
      }

      ref.lock.lock();
      return ref;
    }
  }

  /**
   * Libera un lock para el archivo especificado
   */
  private static void releaseLock(String filePath, LockRef ref) {
    try {
      ref.lock.unlock();
    } finally {
      int remaining = ref.usageCount.decrementAndGet();
      if (remaining == 0) {
        ref.marked = true;
        // Usar remove con valor específico para evitar race conditions
        fileLocks.remove(filePath, ref);
      }
    }
  }

  /**
   * Ejecuta una operación de archivo con sincronización
   */
  private static <T> T withFileLock(String filePath, FileOperation<T> operation)
      throws IOException {
    LockRef lockRef = acquireLock(filePath);
    try {
      return operation.execute();
    } finally {
      releaseLock(filePath, lockRef);
    }
  }

  @FunctionalInterface
  private interface FileOperation<T> {
    T execute() throws IOException;
  }

  /**
   * Lee todas las líneas de un archivo
   */
  public static List<String> readFile(String filePath) throws IOException {
    return withFileLock(filePath, () -> Files.readAllLines(Paths.get(filePath), DEFAULT_CHARSET));
  }

  /**
   * Lee todas las líneas de un archivo sin lanzar excepciones
   */
  public static List<String> readFileSafe(String filePath) {
    try {
      return readFile(filePath);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Error reading file: " + filePath, e);
      return new ArrayList<>();
    }
  }

  /**
   * Lee un archivo completo como String con encoding específico
   */
  public static String readFile(String fileName, String encoding) throws IOException {
    Charset charset =
        encoding == null || encoding.isEmpty() ? DEFAULT_CHARSET : Charset.forName(encoding);

    return withFileLock(fileName, () -> {
      try (InputStream is = Files.newInputStream(Paths.get(fileName))) {
        return readFile(is, charset, true);
      }
    });
  }

  /**
   * Lee un archivo completo como String con encoding por defecto
   */
  public static String readFileAsString(String fileName) throws IOException {
    return readFile(fileName, null);
  }

  /**
   * Método privado para leer desde InputStream
   */
  private static String readFile(InputStream inputStream, Charset charset, boolean preserveNewlines)
      throws IOException {
    StringBuilder strBuilder = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      String line;
      boolean firstLine = true;
      while ((line = reader.readLine()) != null) {
        if (!firstLine && preserveNewlines) {
          strBuilder.append('\n');
        }
        strBuilder.append(line);
        firstLine = false;
      }
    }
    return strBuilder.toString();
  }

  /**
   * Escribe contenido a un archivo con encoding específico
   */
  public static void writeFile(String fileName, String encoding, String content)
      throws IOException {
    Charset charset =
        encoding == null || encoding.isEmpty() ? DEFAULT_CHARSET : Charset.forName(encoding);

    withFileLock(fileName, () -> {
      Files.createDirectories(Paths.get(fileName).getParent());

      try (OutputStreamWriter writer =
          new OutputStreamWriter(Files.newOutputStream(Paths.get(fileName)), charset)) {
        writer.write(content);
      }
      return null;
    });
  }

  /**
   * Escribe contenido a un archivo con encoding por defecto
   */
  public static void writeFile(String fileName, String content) throws IOException {
    writeFile(fileName, null, content);
  }


  public static void writeFileLines(String fileName, List<String> lines) throws IOException {
    String content = String.join(System.lineSeparator(), lines);
    writeFile(fileName, content);
  }

  /**
   * Añade contenido al final de un archivo
   */
  public static void appendToFile(String data, String fileName, String separator)
      throws IOException {
    withFileLock(fileName, () -> {
      java.nio.file.Path path = Paths.get(fileName);

      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }

      boolean fileExists = Files.exists(path);
      boolean isEmpty = fileExists && Files.size(path) == 0;

      try (BufferedWriter writer = Files.newBufferedWriter(path, DEFAULT_CHARSET,
          StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

        if (!isEmpty && fileExists && separator != null) {
          writer.write(separator);
        }
        writer.write(data.trim());
      }
      return null;
    });
  }

  /**
   * Añade contenido al final de un archivo con salto de línea como separador
   */
  public static void appendToFile(String data, String fileName) throws IOException {
    appendToFile(data, fileName, System.lineSeparator());
  }

  /**
   * Verifica si un archivo existe
   */
  public static boolean fileExists(String filePath) {
    return Files.exists(Paths.get(filePath));
  }

  /**
   * Obtiene el tamaño de un archivo
   */
  public static long getFileSize(String filePath) throws IOException {
    return Files.size(Paths.get(filePath));
  }

  /**
   * Elimina un archivo
   */
  public static boolean deleteFile(String filePath) throws IOException {
    return withFileLock(filePath, () -> Files.deleteIfExists(Paths.get(filePath)));
  }

  /**
   * Obtiene estadísticas de uso de locks (para debugging)
   */
  public static Map<String, Integer> getLockStats() {
    Map<String, Integer> stats = new HashMap<>();
    fileLocks.forEach((path, ref) -> stats.put(path, ref.usageCount.get()));
    return Collections.unmodifiableMap(stats);
  }
}
