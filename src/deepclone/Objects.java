package deepclone;

public class Objects {
    public static <T> T deepClone(T original) throws IllegalAccessException {
        Cloner cloner = new Cloner();
        return cloner.deepClone(original);
    }
}
