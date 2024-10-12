public class Clan {
    private String location;
    private int[] info;
    private int[] queue;

    public Clan (String clan) {
        switch (clan) {
            case "LPM":
                location = "luc phien";
                info = new int[] {28, 30, 253, 258};
                queue = new int[] {20, 75, 318, 176};
                break;
            case "HKTĐ":
                location = "thanh dan";
                info = new int[] {15, 110, 456, 364};
                queue = new int[] {36, 111, 543, 207};
                break;
            case "HKLHO":
                location = "lien hoan";
                info = new int[] {17, 1053, 402, 391};
                queue = new int[] {38, 149, 651, 276};
                break;

            case "TYL":
                location = "thanh y";
                info = new int[] {13, 101, 476, 309};
                queue = new int[] {32, 63, 560, 215};
                break;
            case "QV": // 52 164
                location = "quyvuc";
                info = new int[] {52, 164, 502, 266};
                queue = new int[] {59, 68, 514, 194};
                break;
            case "TKLHO":
                location = "lien hoan";
                info = new int[] {6, 171, 402, 391};
                queue = new int[] {42, 158, 541, 202};
                break;

            case "PTV": // 26 156
                location = "phuc tho";
                info = new int[] {26, 156, 307,281};
                queue = new int[] {25, 93, 256, 206};
                break;
            case "DSĐM": // 19 140
                location = "duong mon";
                info = new int[] {19, 140, 330, 228};
                queue = new int[] {29, 57, 253, 210};
                break;
            case "DSTĐ":
                break;

            case "NCP":
                location = "ngan cau";
                info = new int[] {22, 113, 481, 283};
                queue = new int[] {51, 104, 577, 226};
                break;
            case "ĐTTĐ": // 41 61
                location = "thanh dan";
                info = new int[] {41, 61, 456, 364};
                queue = new int[] {41, 0, 382, 393};
                break;
            case "ĐTĐM":
                break;
        }
    }

    public String getLocation() {
        return location;
    }

    public int[] getInfo() {
        return info;
    }

    public int[] getQueue() {
        return queue;
    }
}
