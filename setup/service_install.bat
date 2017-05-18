@echo off
set MYDIR=C:\Apps\MyPC
cd %MYDIR%
rem  --Jvm=auto
mypc.exe //IS//mypc --Install="%MYDIR%\mypc.exe" --JavaHome="C:\Program Files\Java\jre1.8.0_121\bin" --Startup=auto --StartMode=jvm --Classpath="%MYDIR%\mypc.jar" --StartClass=mypc.MyPCMain --Description="My Parental Control Service" --LogPath="%MYDIR%\log"
pause
