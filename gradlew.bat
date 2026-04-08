@ECHO OFF
SETLOCAL

SET APP_DIR=%~dp0
SET CLASSPATH=%APP_DIR%gradle\wrapper\gradle-wrapper.jar

IF DEFINED JAVA_HOME (
  IF EXIST "%JAVA_HOME%\bin\java.exe" (
    SET JAVA_CMD=%JAVA_HOME%\bin\java.exe
  ) ELSE (
    SET JAVA_CMD=java.exe
  )
) ELSE (
  SET JAVA_CMD=java.exe
)

"%JAVA_CMD%" -Dorg.gradle.appname=gradlew -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
