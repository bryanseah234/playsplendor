@echo off
setlocal
cd /d "%~dp0\..\.."

echo Three-terminal network validation ^(server + 2 players^)
echo.
echo Terminal 1 ^(server^):
echo   java -cp classes com.splendor.Main --server
echo.
echo Terminal 2 ^(player 1^):
echo   nc ^<server-ip^> ^<port^>
echo.
echo Terminal 3 ^(player 2^):
echo   nc ^<server-ip^> ^<port^>
echo.
echo Why this exists:
echo   Network validation is only representative when at least one server and two clients
echo   are connected simultaneously. Running only one terminal does not validate gameplay flow.
