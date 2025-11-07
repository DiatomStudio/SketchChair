@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set ANT_HOME=C:\ant\apache-ant-1.10.15
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
cd /d C:\Git\Diatom\SketchChair
ant clean build.standard
