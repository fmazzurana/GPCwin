@echo off
for /f "skip=4 delims=" %%A in ('reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Clients\StartMenuInternet" 2^>nul') do (
	echo %%~nA
)
