@echo off
title 影院票务管理系统
chcp 65001 >nul

cd /d "%~dp0"

echo ========================================
echo         影院票务管理系统
echo ========================================
echo.

echo [信息] 正在启动程序...
echo.

jre\bin\java.exe -jar CinemaTicketSystem.jar

echo.
echo ========================================
echo 程序已退出，请查看上方错误信息
echo ========================================
pause