LIB=../../../../../../lib

./build.sh

java -cp "$LIB/hamcrest-core-1.3.jar:$LIB/junit-4.11.jar:.build" org.junit.runner.JUnitCore \
       info.kgeorgiy.ja.malko.i18n.Tester

rm -rf .build
