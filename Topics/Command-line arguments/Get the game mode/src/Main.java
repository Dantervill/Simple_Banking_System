class Problem {
    public static void main(String[] args) {
        int index;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("mode")) {
                index = i;
                if (index % 2 == 0) {
                    System.out.println(args[++i]);
                    break;
                } else {
                    System.out.println("default");
                }
            }
        }
    }
}