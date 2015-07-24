package fuzzycsv;

enum ResolutionStrategy {
    DERIVED_FIRST, SOURCE_FIRST
}

enum Mode {
    STRICT, RELAXED

    boolean isRelaxed() { this == RELAXED }

    boolean isStrict() { this == STRICT }
}