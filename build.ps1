$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$binDir = Join-Path $scriptDir "bin"
$srcDir = Join-Path $scriptDir "src\main\java"
$resDir = Join-Path $scriptDir "src\main\resources"
$minecraftModsDir = "d:\PrismLauncher\instances\1.21.1(1)\minecraft\mods"
$jarPath = Join-Path $minecraftModsDir "satisfactory_wheel-1.0.0.jar"

Write-Host "Compiling Satisfactory Wheel mod..." -ForegroundColor Cyan

if (Test-Path $binDir) { Remove-Item -Recurse -Force $binDir }
New-Item -ItemType Directory -Force -Path $binDir | Out-Null

$cp = @(
    "E:\12\libraries\net\minecraft\client\1.21.1-20240808.144430\client-1.21.1-20240808.144430-srg.jar",
    "E:\12\libraries\net\neoforged\neoforge\21.1.256\neoforge-21.1.256-universal.jar",
    "E:\12\libraries\net\neoforged\fancymodloader\loader\4.0.45\loader-4.0.45.jar",
    "E:\12\libraries\net\neoforged\bus\8.0.5\bus-8.0.5.jar",
    "E:\12\libraries\net\neoforged\mergetool\2.0.3\mergetool-2.0.3-api.jar",
    "E:\12\libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar",
    "E:\12\libraries\com\google\guava\guava\32.1.2-jre\guava-32.1.2-jre.jar",
    "E:\12\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar",
    "E:\12\libraries\io\netty\netty-buffer\4.1.77.Final\netty-buffer-4.1.77.Final.jar",
    "E:\12\libraries\io\netty\netty-common\4.1.77.Final\netty-common-4.1.77.Final.jar",
    "E:\12\libraries\org\lwjgl\lwjgl-glfw\3.2.2\lwjgl-glfw-3.2.2.jar",
    "E:\12\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar",
    "E:\12\libraries\com\mojang\brigadier\1.0.17\brigadier-1.0.17.jar"
) -join ";"

$srcFiles = (Get-ChildItem -Path $srcDir -Recurse -Filter "*.java").FullName
javac -encoding UTF-8 -d $binDir -cp $cp $srcFiles

Copy-Item -Path "$resDir\*" -Destination $binDir -Recurse -Force

Push-Location $binDir
jar cf $jarPath *
Pop-Location

Write-Host "Successfully built and deployed to: $jarPath" -ForegroundColor Green

