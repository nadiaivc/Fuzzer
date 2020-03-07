public class CLoader {
    private native int IsDebug();
    public static void main(String[] args) {

    }
    static {
        System.loadLibrary("CLoader");
    }
    public static int checkDebug()
    {
        int Debug = new CLoader().IsDebug();
        return Debug;
    }
}
