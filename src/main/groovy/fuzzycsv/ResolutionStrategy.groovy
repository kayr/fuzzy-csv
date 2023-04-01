package fuzzycsv

enum ResolutionStrategy {
    DERIVED_FIRST, //same as final first
    SOURCE_FIRST,//same as left first
    LEFT_FIRST, RIGHT_FIRST, FINAL_FIRST
}

enum Mode {
    STRICT, RELAXED

    boolean isRelaxed() { this == RELAXED }

    boolean isStrict() { this == STRICT }
}