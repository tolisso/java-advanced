rm -rf .build 
mkdir .build
LIB=../../../../../../lib
javac --class-path "$LIB/hamcrest-core-1.3.jar:$LIB/junit-4.11.jar" --source-path ../../../../../ -d .build *.java

