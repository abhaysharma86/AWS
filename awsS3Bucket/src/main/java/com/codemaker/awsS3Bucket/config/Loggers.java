package com.codemaker.awsS3Bucket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Loggers {

    private static final Logger logs = LoggerFactory.getLogger(Loggers.class);

    public static void log(int id, String message) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        String logMessage = String.format("[%s.%s] %s", className, methodName, message);

        switch (id) {
            case 1 -> logs.info(logMessage);
            case 2 -> logs.warn(logMessage);
            case 3 -> logs.error(logMessage);
            default -> logs.debug(logMessage);
        }
    }
}

