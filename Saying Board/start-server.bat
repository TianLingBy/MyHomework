@echo off
chcp 65001 >nul
title 今天吃什么留言板 - 服务器启动

echo ========================================
echo   今天吃什么留言板 - 局域网服务器
echo ========================================
echo.

:: 获取本机局域网 IP
echo [信息] 正在获取本机 IP 地址...
echo.

:: 显示所有局域网 IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set "ip=%%a"
    setlocal enabledelayedexpansion
    set "ip=!ip: =!"
    echo   局域网地址: http://!ip!:5173
    endlocal
)

echo.
echo ========================================
echo   请确保 MySQL 已启动，数据库已初始化
echo ========================================
echo.

:: 启动后端
echo [1/2] 正在启动后端 (Spring Boot)...
start "后端 - Spring Boot" cmd /k "chcp 65001 >nul && cd /d %~dp0backend && apache-maven-3.9.6\bin\mvn.cmd spring-boot:run"

:: 等待后端启动
echo [等待] 等待后端启动 (约 15 秒)...
timeout /t 15 /nobreak >nul

:: 启动前端
echo [2/2] 正在启动前端 (Vite)...
start "前端 - Vite" cmd /k "chcp 65001 >nul && cd /d %~dp0frontend && npm run dev"

echo.
echo ========================================
echo   服务器启动完成！
echo.
echo   本机访问: http://localhost:5173
echo.
echo   局域网用户请访问上面的局域网地址
echo.
echo   关闭此窗口不会停止服务器
echo   要停止服务器请关闭 "后端" 和 "前端" 窗口
echo ========================================
echo.
pause
