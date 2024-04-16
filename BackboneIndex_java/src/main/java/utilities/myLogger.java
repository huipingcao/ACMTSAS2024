package utilities;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;

public class myLogger {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static Formatter formatterLogger;
    private static FileHandler fh = null;
    private static final SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd~HH-mm-ss]");

    public static void configTheLogger(String className, ArrayList<String> parameters) {
        try {
            FileInputStream fis = new FileInputStream("../Data/logging.properties");
            LogManager.getLogManager().readConfiguration(fis);
            logger.setLevel(Level.INFO);
            String file_name = getFileName(className, parameters);
            fh = new FileHandler(file_name);
            formatterLogger = new MyLoggerFormatter();
            fh.setFormatter(formatterLogger);
            logger.addHandler(fh);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void configTheLogger(String className) {
        try {
            FileInputStream fis = new FileInputStream(ParsedOptions.propertyFile);
            LogManager.getLogManager().readConfiguration(fis);
            logger.setLevel(Level.INFO);
            String file_name = getFileName(className);
            fh = new FileHandler(file_name);
            formatterLogger = new MyLoggerFormatter();
            fh.setFormatter(formatterLogger);
            logger.addHandler(fh);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String configTheLogger(String className, long timestamp) {
        String file_name = "";
        try {
            FileInputStream fis = new FileInputStream(ParsedOptions.logProperties);
            LogManager.getLogManager().readConfiguration(fis);
            logger.setLevel(Level.INFO);
            file_name = getFileName(className, timestamp);
            fh = new FileHandler(file_name);
            formatterLogger = new MyLoggerFormatter();
            fh.setFormatter(formatterLogger);
            logger.addHandler(fh);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return file_name;
    }

    private static String getFileName(String className, ArrayList<String> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append(ParsedOptions.logFolder + "/" + className + "");
        sb.append("-");
        for (String p : parameters) {
            sb.append(p).append("-");
        }
        sb.append(format.format(Calendar.getInstance().getTime()) + ".log");
        return sb.toString();
    }

    private static String getFileName(String className) {
        StringBuffer sb = new StringBuffer();
        sb.append(ParsedOptions.logFolder + "/" + className + "");
        sb.append("-");
        sb.append(format.format(Calendar.getInstance().getTime()) + ".log");
        return sb.toString();
    }

    private static String getFileName(String className, long timestamp) {
        StringBuffer sb = new StringBuffer();
        sb.append(ParsedOptions.logFolder + "/" + className + "");
        sb.append("-");
        sb.append(format.format(new Date(timestamp)) + ".log");
        return sb.toString();
    }
}
