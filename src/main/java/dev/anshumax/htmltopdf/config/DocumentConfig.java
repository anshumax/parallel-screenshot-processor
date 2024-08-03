package dev.anshumax.htmltopdf.config;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;


@CommonsLog
@EnableAsync
@Configuration
public class DocumentConfig {

    @Value("${CHROME_DRIVER_LOC}")
    String chromeDriverLoc;

    @Bean
    String chromeDriverLoc(){
        return chromeDriverLoc;
    }


    @Bean
    Path sgUtilsTemporaryDirectoryPath() throws IOException {
        String temporaryFilesDirectoryPrefix = "sg-utils";
        log.info("Clearing old temporary files with prefix " + temporaryFilesDirectoryPrefix);
        File[] oldTemporaryDirs = FileUtils.getTempDirectory().listFiles((dir, name) -> name.startsWith(temporaryFilesDirectoryPrefix));

        int count = Objects.requireNonNull(oldTemporaryDirs).length;
        for (File oldTemporaryDir : oldTemporaryDirs) {
            log.info("Deleting " + oldTemporaryDir.getAbsolutePath());
            FileUtils.deleteDirectory(oldTemporaryDir);
            log.info("Deleted");
        }

        log.info("Deleted " + count + " old temporary directories with prefix " + temporaryFilesDirectoryPrefix);
        Path sgUtilsTemporaryDirectoryPath = Files.createTempDirectory(temporaryFilesDirectoryPrefix);
        log.info("Temporary Files Directory for this instance is " + sgUtilsTemporaryDirectoryPath.toRealPath());
        return sgUtilsTemporaryDirectoryPath;
    }

    @Bean(name = "pdfGenerator")
    public Executor pdfGenerator() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("pdfgen-");
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(12);
        executor.setPrestartAllCoreThreads(true);
        executor.initialize();
        return executor;
    }
}
