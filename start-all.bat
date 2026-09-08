@echo off
title Scaler Store Microservices Launcher
echo ===============================================================================
echo                     SCALER STORE - MICROSERVICES PLATFORM                      
echo ===============================================================================
cd /d "%~dp0"

echo [1/9] Starting Netflix Eureka Service Registry (Port 8761)...
start "Eureka-Registry:8761" /min cmd /c "java -jar service-registry\target\service-registry-1.0.0-SNAPSHOT.jar"
timeout /t 6 /nobreak >nul

echo [2/9] Starting Spring Cloud API Gateway (Port 8080)...
start "API-Gateway:8080" /min cmd /c "java -jar api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar"
timeout /t 4 /nobreak >nul

echo [3/9] Starting User Management Service (Port 8081)...
start "User-Service:8081" /min cmd /c "java -jar user-service\target\user-service-1.0.0-SNAPSHOT.jar"

echo [4/9] Starting Product Catalog Service (Port 8082)...
start "Product-Service:8082" /min cmd /c "java -jar product-service\target\product-service-1.0.0-SNAPSHOT.jar"

echo [5/9] Starting Cart Service (Port 8083)...
start "Cart-Service:8083" /min cmd /c "java -jar cart-service\target\cart-service-1.0.0-SNAPSHOT.jar"

echo [6/9] Starting Order Management Service (Port 8084)...
start "Order-Service:8084" /min cmd /c "java -jar order-service\target\order-service-1.0.0-SNAPSHOT.jar"

echo [7/9] Starting Payment Service (Port 8085)...
start "Payment-Service:8085" /min cmd /c "java -jar payment-service\target\payment-service-1.0.0-SNAPSHOT.jar"

echo [8/9] Starting Notification Service (Port 8086)...
start "Notification-Service:8086" /min cmd /c "java -jar notification-service\target\notification-service-1.0.0-SNAPSHOT.jar"
timeout /t 5 /nobreak >nul

echo [9/9] Starting Web Frontend (Port 3000)...
start "Frontend:3000" /min cmd /c "python -m http.server 3000 --directory frontend"
timeout /t 2 /nobreak >nul

echo Opening browser at http://localhost:3000 ...
start http://localhost:3000

echo ===============================================================================
echo All microservices launched successfully!
echo Service Registry : http://localhost:8761
echo API Gateway      : http://localhost:8080
echo Web Frontend     : http://localhost:3000
echo ===============================================================================
pause
