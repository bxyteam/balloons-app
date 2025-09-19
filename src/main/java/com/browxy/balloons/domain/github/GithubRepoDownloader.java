package com.browxy.balloons.domain.github;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.Application;

public class GithubRepoDownloader {
  private static Logger logger = LoggerFactory.getLogger(GithubRepoDownloader.class);
  
  public void download() throws IOException, InterruptedException  {
    String downloadGithubUrl = Application.getInstance().getBalloonGithubConfig().getDownloadUrl();
    if (downloadGithubUrl == null || downloadGithubUrl.isEmpty()) {
        return;
    }

    String script = String.format(
        "set -e\n" +
        "TMP_DIR=\"/var/balloon/data/tmp\"\n" +
        "TARGET_DIR=\"/var/balloon/data\"\n" +
        "WEB_DIR=\"/var/balloon/data/web\"\n" +
        "GITHUB_DIR=\"/var/balloon/data/github\"\n" +
        "ZIP_FILE=\"$TMP_DIR/main.zip\"\n" +
        "mkdir -p \"$TMP_DIR\"\n" +
        "chmod -R ugo+rwx ${TMP_DIR}\n" +
        "wget -O \"$ZIP_FILE\" \"%s\"\n" +
        "unzip -o \"$ZIP_FILE\" -d \"$TMP_DIR\"\n" +
        "UNZIPPED_DIR=$(find \"$TMP_DIR\" -maxdepth 1 -type d -name \"*-main\")\n" +
        "if [ -d \"$UNZIPPED_DIR\" ]; then\n" +
        "    mv \"$UNZIPPED_DIR\" \"$TMP_DIR/github\"\n" +
        "    mv \"$TMP_DIR/github\" \"$TARGET_DIR/\"\n" +
        "    chmod -R ugo+rwx ${GITHUB_DIR}\n" +
        "else\n" +
        "    echo \"Error: Unzipped directory not found.\" >&2\n" +
        "    exit 1\n" +
        "fi\n" +
        "rm -f \"$ZIP_FILE\"\n" +
        "echo \"Done: Folder 'github' is now in $TARGET_DIR\"\n" +
        "\n" +
        "# Copy templates if source directory exists and has files\n" +
        "if [ -d \"${GITHUB_DIR}/frontend/templates\" ] && [ \"$(ls -A ${GITHUB_DIR}/frontend/templates)\" ]; then\n" +
        "    cp ${GITHUB_DIR}/frontend/templates/* ${WEB_DIR}/templates\n" +
        "else\n" +
        "    echo \"Templates directory is missing or empty: ${GITHUB_DIR}/frontend/templates\"\n" +
        "fi\n" +
        "\n" +
        "# Copy satellite assets if source directory exists and has files\n" +
        "if [ -d \"${GITHUB_DIR}/frontend/builder\" ] && [ \"$(ls -A ${GITHUB_DIR}/frontend/builder)\" ]; then\n" +
        "    cp ${GITHUB_DIR}/frontend/builder/. ${WEB_DIR}\n" +
        "else\n" +
        "    echo \"Sats directory is missing or empty: ${GITHUB_DIR}/frontend/builder\"\n" +
        "fi\n" +
        "\n" +
        "# Copy html assets if source directory exists and has files\n" +
        "if [ -d \"${GITHUB_DIR}/balloons_processor\" ] && [ \"$(ls -A ${GITHUB_DIR}/balloons_processor)\" ]; then\n" +
        "    cp ${GITHUB_DIR}/balloons_processor/. ${WEB_DIR}\n" +
        "else\n" +
        "    echo \"HTML directory is missing or empty: ${GITHUB_DIR}/balloons_processor\"\n" +
        "fi\n",
        downloadGithubUrl
    );

    ProcessBuilder builder = new ProcessBuilder("bash", "-s");
    builder.redirectErrorStream(true); 
    
    // Start the process
    Process process = builder.start();

    // Write the script to the process's stdin
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
        writer.write(script);
    }

    // Read and print script output
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            logger.info(line);
        }
    }

    // Wait for completion
    int exitCode = process.waitFor();
    if (exitCode != 0) {
        logger.error("Script failed with exit code: " + exitCode);
    } else {
        logger.info("GitHub repo downloaded and moved successfully.");
    }
  }
}
