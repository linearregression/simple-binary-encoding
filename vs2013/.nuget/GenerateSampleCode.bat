@echo off

echo Generating C# classes for example-schema.xml...

java -Dsbe.output.dir=..\sample\output\ -Dsbe.target.language=CSHARP -jar ..\sbetool\SBETool.jar ..\sample\example-schema.xml

echo Code generated in \sample\output\

pause