package fuzzycsv

enum ResolutionStrategy {
    DERIVED_FIRST, //same as final first, todo delete
    SOURCE_FIRST,//same as left first, todo delete
    LEFT_FIRST, RIGHT_FIRST, FINAL_FIRST
}

enum Mode {
    STRICT, RELAXED

    boolean isRelaxed() { this == RELAXED }

    boolean isStrict() { this == STRICT }
}