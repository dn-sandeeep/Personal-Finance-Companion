# Personal Finance Companion - Deep Clean Script
# This script forcefully terminates Gradle and deletes build artifacts to resolve R.jar locking issues.

Write-Host "Stopping Gradle Daemons..." -ForegroundColor Cyan
./gradlew --stop

Write-Host "Killing any lingering Java processes..." -ForegroundColor Cyan
Get-Process | Where-Object { $_.Name -eq "java" -or $_.Name -eq "kotlin" } | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Deleting build directories..." -ForegroundColor Yellow
if (Test-Path "app/build") { Remove-Item -Path "app/build" -Recurse -Force }
if (Test-Path "build") { Remove-Item -Path "build" -Recurse -Force }

Write-Host "Clearing intermediate R class jars..." -ForegroundColor Yellow
$rJarPath = "app/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar"
if (Test-Path $rJarPath) { Remove-Item -Path $rJarPath -Recurse -Force }

Write-Host "Running Gradle Clean..." -ForegroundColor Green
./gradlew clean

Write-Host "Clean Complete! Please Sync Project with Gradle Files in Android Studio." -ForegroundColor Green
