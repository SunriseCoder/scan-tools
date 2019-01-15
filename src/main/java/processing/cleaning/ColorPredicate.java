package processing.cleaning;

@FunctionalInterface
public interface ColorPredicate {
    boolean test(int actualRGB);
}
