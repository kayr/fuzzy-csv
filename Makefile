

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

publish-groovy3:
	./gradlew build publish -Pvariant=3 --no-daemon

publish-groovy4:
	./gradlew build publish -Pvariant=4 --no-daemon

close-release:
	./gradlew closeAndReleaseRepository