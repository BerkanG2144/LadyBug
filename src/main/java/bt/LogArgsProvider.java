package bt;

/**
 * Implementieren von Behaviors, die Argumente fürs Log formatiert bereitstellen möchten.
 * @author ujnaa
 * */
public interface LogArgsProvider {
    /**
     * Retunr von ka was.
     * @return z. B. "10,2" oder null/leer, wenn es keine Argumente gibt.
     */
    String logArgs();
}
