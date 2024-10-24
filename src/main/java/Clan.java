public class Clan {
    private String location;
    private int[] info;

    public Clan (String clan) {
        switch (clan) {
            case "LPM":
                location = "luc phien";
                info = new int[] {270, 278, 20, 75, 318, 176};
                break;
            case "TYL":
                location = "thanh y";
                info = new int[] {445, 323, 32, 63, 560, 215};
                break;
            case "QV":
                location = "quyvuc";
                info = new int[] {488, 283, 59, 68, 514, 194};
                break;

            case "PTV": // 26 156
                location = "phuc tho";
                info = new int[] {336, 297, 25, 93, 256, 206};
                break;
            case "NCP":
                location = "ngan cau";
                info = new int[] {459, 293, 51, 104, 577, 226};
                break;

            case "TĐ":
                location = "thanh dan";
                info = new int[] {421, 380, 36, 111, 543, 207};
                break;
            case "LHO":
                location = "lien hoan";
                info = new int[] {383, 403, 42, 158, 541, 202};
                break;
            case "ĐM": // 19 140
                location = "duong mon";
                info = new int[] {358, 243, 29, 57, 253, 210};
                break;
        }
    }

    public String getLocation() {
        return location;
    }

    public int[] getInfo() {
        return info;
    }
}
