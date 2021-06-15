LIB=../../../../../../lib

./build.sh
java -cp "$LIB/hamcrest-core-1.3.jar:$LIB/junit-4.11.jar:.build" \
       info.kgeorgiy.ja.malko.i18n.MainAnalyzer "$@"

rm -rf .build
