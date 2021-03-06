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
    file = "/imqsvar/logs/services/imqs-classified-template-service.log"

    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "/imqs-classified-template-service.%d{yyyy-MM-dd}.log"
        maxHistory = 30
    }

    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %d{yyyy-MM-dd HH:mm:ss.SSS Z} [%thread] %X{MessageId}  %logger{100} - %msg%n"
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


logger("za.co.imqs", DEBUG)

logger("org.apache.commons", INFO)
logger("org.eclipse.jetty", INFO)
logger("org.eclipse.jetty.annotations", INFO)
logger("org.springframework", INFO)
logger("org.springframework.util", INFO)
logger("org.springframework.web", INFO)
logger("com.jolbox",INFO)
logger("org.activiti",INFO)
logger("org.springframework.jdbc.datasource", INFO)

logger("za.co.imqs.templates", DEBUG)

root(INFO, ["ASYNC", "CONSOLE"])

scan()
