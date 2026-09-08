@echo off
title Scaler Store Shutdown
echo ===============================================================================
echo                  STOPPING ALL SCALER STORE MICROSERVICES                       
echo ===============================================================================
powershell -NoProfile -Command "8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 3000 | ForEach-Object { $p = $_; Get-NetTCPConnection -LocalPort $p -ErrorAction SilentlyContinue | ForEach-Object { Write-Host ('Terminating process on port ' + $p + ' (PID: ' + $_.OwningProcess + ')...'); Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue } }"
echo All services stopped cleanly.
pause
