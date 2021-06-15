TESTS_PATH="../../../../../../../java-advanced-2021"
LIB_PATH="$TESTS_PATH"/lib:"$TESTS_PATH"/artifacts:.
SRC_PATH="$PWD"
MODULE_PATH="../../../../../"
BUILD_DIR="$SRC_PATH"/build
JAR_PATH="$SRC_PATH"/Implementor.jar

rm -f "$JAR_PATH"
rm -rf "$BUILD_DIR"

javac --module-path "$LIB_PATH" \
    -d "$BUILD_DIR" \
    "$MODULE_PATH"/module-info.java *.java \
    || exit 1
    
cd "$BUILD_DIR" || exit 1
    
jar -c \
    --manifest="$SRC_PATH"/MANIFEST.MF \
    --file="$JAR_PATH" \
    --module-path="$LIB_PATH" \
    module-info.class ./info/kgeorgiy/ja/malko/implementor/*

rm -rf "$BUILD_DIR"
