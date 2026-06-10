@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN ("%~dp0\.mvn\wrapper\maven-wrapper.properties") DO @(
    IF "%%~A"=="wrapperUrl" SET "__MVNW_CMD__=%%~B"
    IF "%%~A"=="distributionUrl" SET "__MVNW_DISTURL__=%%~B"
)
@IF "%__MVNW_CMD__%"=="" SET __MVNW_CMD__=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
@IF "%__MVNW_DISTURL__%"=="" SET __MVNW_DISTURL__=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip

@SET WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@IF NOT EXIST %WRAPPER_JAR% (
    @REM Download wrapper jar
    powershell -Command "&{"^
		"$webclient = new-object System.Net.WebClient;"^
		"[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%__MVNW_CMD__', '%WRAPPER_JAR%')"^
		"}"
    @IF NOT EXIST %WRAPPER_JAR% (
        SET __MVNW_ERROR__=Failed to download wrapper jar from %__MVNW_CMD__
        GOTO ERROR
    )
)

@SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
@IF NOT EXIST %JAVA_EXE% (
    @REM Try to find java in PATH
    FOR /F "tokens=* USEBACKQ" %%F IN (`where java`) DO @SET JAVA_EXE="%%F"
)

%JAVA_EXE% ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0" ^
  %WRAPPER_LAUNCHER% %*
@IF ERRORLEVEL 1 GOTO ERROR
@ENDLOCAL & SET WRAPPER_ERRORLEVEL=%ERRORLEVEL%

@IF NOT "%WRAPPER_ERRORLEVEL%"=="0" (
    SET __MVNW_ERROR__=Maven exited with error code %WRAPPER_ERRORLEVEL%
    GOTO ERROR
)

GOTO END

:ERROR
@ECHO %__MVNW_ERROR__% 1>&2
@ENDLOCAL & SET WRAPPER_ERRORLEVEL=1

:END
@ENDLOCAL & SET WRAPPER_ERRORLEVEL=%WRAPPER_ERRORLEVEL%
@EXIT /B %WRAPPER_ERRORLEVEL%
