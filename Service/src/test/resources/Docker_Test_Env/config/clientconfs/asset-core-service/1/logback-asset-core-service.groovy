import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.Appender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.status.OnConsoleStatusListener

import static ch.qos.logback.classic.Level.*

statusListener(OnConsoleStatusListener)

appender("FILE", RollingFileAppender) {
    file = "/imqsvar/logs/asset-core-service.log"

    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "asset-core-service.%d{yyyy-MM-dd}.log"
        maxHistory = 30
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %d{HH:mm:ss.SSS} [%thread] %X{MessageId}  %logger{100} - %msg%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %d{HH:mm:ss.SSS} [%thread]  %logger{100} - %msg%n"
    }
}

//
// The bit of acrobatics below is required because of a bug in the implementation - so something like
//
//      appender("async", AsyncAppender) {
//          appender-ref =
//      }
//
// does not work
//
Appender fileAppender = appenderList.find { it -> it.name == "FILE" };
Appender consoleAppender = appenderList.find { it -> it.name == "CONSOLE" };

AsyncAppender asyncAppender = new AsyncAppender();
asyncAppender.queueSize = 10000
asyncAppender.name = "ASYNC"
asyncAppender.context = context
asyncAppender.addAppender(fileAppender)
asyncAppender.addAppender(consoleAppender)
appenderList.add(asyncAppender)
asyncAppender.start()



logger("org.apache.commons", INFO)
logger("org.eclipse.jetty", ERROR)
logger("org.eclipse.jetty.annotations", ERROR)
logger("org.springframework", ERROR)
logger("org.springframework.util", ERROR)
logger("org.springframework.web", ERROR)
logger("org.springframework.jdbc.datasource", ERROR)
logger("ch.qos.logback", ERROR)

logger("liquibase", OFF)
logger("com.zaxxer.hikari", ERROR)

root(DEBUG, ["ASYNC", "CONSOLE"])

scan()
