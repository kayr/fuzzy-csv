

clean:
	./gradlew clean

test:
	make clean test-g3
	make clean test-g4

test-g4:
	./gradlew test -Pvariant=4

test-g3:
	./gradlew test -Pvariant=3

build:
	./gradlew build
