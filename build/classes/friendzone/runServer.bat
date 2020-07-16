@ECHO OFF

set DIR=%~dp0
javac Debug.java
javac UserAcc.java
javac Coordination.java
javac ServerConnection.java
javac ServerFileStoring.java
javac ServerMain.java
java ServerMain

exit