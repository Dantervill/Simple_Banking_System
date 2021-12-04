class Problem {
    public static void main(String[] args) {
        System.out.println(args[0] + "=" + args[1]);
        for (int i = 2; i < args.length; i++) {
            if (i % 2 == 0) {
                System.out.print(args[i] + "=");
            } else {
                System.out.print(args[i]);
            }
        }
    }
}