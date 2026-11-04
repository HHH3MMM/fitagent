@echo off
rem Fitness Agent - dev runner. Requires JDK 21 and JAVA_HOME set.
if not defined JAVA_HOME (
  echo JAVA_HOME is not set. Point it to a JDK 21 installation first.
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
if "%MAVEN_OPTS%"=="" set "MAVEN_OPTS=-Xmx512m -XX:MaxMetaspaceSize=256m"
call mvnw.cmd %*
