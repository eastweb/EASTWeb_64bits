@echo off

rem delete old output and project
@RD /S /Q "%~dp0..\Installer64x\Output"
pause

rem copy project to desktop
robocopy "%~dp0..\..\..\EASTWeb_64bits" "%temp%\EASTWeb_64bits" /s /mt[:18] /xf *.iss *.exe *.bat *.jar *.zip *.png
pause

rem copy project from desktop to installer folder (this prevents infinite loop)
rem then remove project form desktop
robocopy "%temp%\EASTWeb_64bits" "%~dp0..\EASTWeb_64bits" /mir /mt[:18]
@RD /S /Q "%temp%\EASTWeb_64bits"
del "%~dp0..\EASTWeb_64bits\projects\*.*"
@RD /S /Q "%~dp0..\EASTWeb_64bits\Installer"
@RD /S /Q "%~dp0..\EASTWeb_64bits\.git"
@RD /S /Q "%~dp0..\EASTWeb_64bits\.settings"
@RD /S /Q "%~dp0..\EASTWeb_64bits\.svn"
@RD /S /Q "%~dp0..\EASTWeb_64bits\bin"
@RD /S /Q "%~dp0..\EASTWeb_64bits\doc"
@RD /S /Q "%~dp0..\EASTWeb_64bits\Documentation"

::@RD /S /Q "%~dp0..\EASTWeb_64bits\lib"
@RD /S /Q "%~dp0..\EASTWeb_64bits\sources"
@RD /S /Q "%~dp0..\EASTWeb_64bits\src"
pause

rem compile installer 
"%~dp0..\InstallerCompiler\ISCC.exe" %~dp0EastWeb64x.iss
@RD /S /Q "%~dp0..\EASTWeb_64bits"
pause