package fuzzycsv

enum ResolutionStrategy {
    DERIVED_FIRST, SOURCE_FIRST,LEFT_FIRST,RIGHT_FIRST,FINAL_FIRST
}

enum Mode {
    STRICT, RELAXED

    boolean isRelaxed() { this == RELAXED }

    boolean isStrict() { this == STRICT }
}