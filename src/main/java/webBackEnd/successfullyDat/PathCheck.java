package webBackEnd.successfullyDat;

import org.springframework.stereotype.Component;

@Component
public class PathCheck {

    private final boolean windows;
    private final String baseDir;

    public PathCheck() {
        String os = System.getProperty("os.name").toLowerCase();
        this.windows = os.contains("win");


        this.baseDir = windows
                ? "D:/"
                : "/home/urantune/Documents/";
    }

    public String getBaseDir() {
        return baseDir;
    }
}

