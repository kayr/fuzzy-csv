./gradlew clean build publish -Pvariant=4 --no-daemon
./gradlew closeAndReleaseRepository

./gradlew clean build publish -Pvariant=3 --no-daemon
./gradlew closeAndReleaseRepository
