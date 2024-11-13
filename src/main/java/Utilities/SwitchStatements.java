package Utilities;

import Objects.Dest;

import java.util.Queue;

public class SwitchStatements {
    public static boolean parseDestination(String NPC, Queue<Dest> queue) {
        boolean res = false;
        switch (NPC) {
            case "ctt":
                res = true;
                queue.offer(new Dest(491, 227, 173, 164, "kt"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(57, 48, "htt"));
                break;
            case "ccs":
                queue.offer(new Dest(37, 145, "lm"));
                break;
            case "ltd":
                res = true;
                queue.offer(new Dest(491, 227, 173, 164, "kt"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, 10, 307, "lth"));
                queue.offer(new Dest(30, 199, "ncp"));
                break;
            case "tl":
                res = true;
                queue.offer(new Dest(491, 227, 173, 164, "kt"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(74, 86, "vdd"));
                break;
            case "tdh":
                res = true;
                queue.offer(new Dest(101, 496, 161, 49, "dps"));
                queue.offer(new Dest(51, 161, "hht"));
                break;
            case "tt":
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "ktdg"));
                queue.offer(new Dest(38, 79, "dvd"));
                break;
            case "tdpq":
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "ktdg"));
                queue.offer(new Dest(32, 57, "tkt"));
                break;
            case "mkq":
                res = true;
                queue.offer(new Dest(101, 496, 161, 49, "dps"));
                queue.offer(new Dest(18, 60, "qdvmd"));
                break;
            case "mqltb":
                queue.offer(new Dest(22, 110, "kdn"));
                break;
            case "dtt":
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "ktdg"));
                queue.offer(new Dest(14, 71, "tld"));
                break;
            case "nll":
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "ktdg"));
                queue.offer(new Dest(29, 70, "qv"));
                break;
            case "slh":
                queue.offer(new Dest(26, 57, "lhc"));
                break;
            case "ht":
                queue.offer(new Dest(29, 84, "bkd"));
                break;
            case "hsy":
                queue.offer(new Dest(10, 73, "tbks"));
                break;
            case "kph":
                queue.offer(new Dest(20, 65, "klh"));
                break;
        }
        return res;
    }

    public static int[] fixFinishQuest(int x, int y) {
        switch (x) {
            case 10: // hac sinh y 10 73
                return new int[] {399, 212};
            case 14: // duong thu thanh 14 71
                return new int[] {505, 182};
            case 18: // ma khong quan 18 60
                return new int[] {286, 198};
            case 20: // kim phung hoang 20 65
                return new int[] {239, 230};
            case 22: // ma quan lao thai ba 22 110
                return new int[] {537, 401};
            case 26: // so luu huong 26 57
                return new int[] {145, 276};
            case 29: // ngoc linh lung 29 70, han thuan 29 84
                if (y == 70) {
                    return new int[] {400, 131};
                } else {
                    return new int[] {286, 146};
                }
            case 30: // ly than dong 30 199
                return new int[] {683, 308};
            case 32: // thiet dien phan qua 32 57
                return new int[] {539, 177};
            case 37: // chuong chan seu 37 145
                return new int[] {549, 228};
            case 38: // trinh trung 38 79
                return new int[] {399, 125};
            case 51: // tiet dai han 51 161
                return new int[] {128, 284};
            case 57: // cung to to 57 48
                return new int[] {682, 260};
            case 74: // tram lang 74 86
                return new int[] {250, 201};
        }
        return new int[2];
    }

    public static int[] handleIdling(String location, int x) {
        if (location.charAt(0) <= 'm') {
            return handleIdlingSmall(location, x);
        } else {
            return handleIdlingLarge(location, x);
        }
    }

    private static int[] handleIdlingLarge(String location, int x) {
        switch (location) {
            case "tvd":
                return new int[] {166, 194};
            case "tyl":
                return new int[] {432, 274};
            case "tt":
                if (x == 14) {
                    return new int[] {174, 179};
                } else if (x == 29) {
                    return new int[] {618, 156};
                } else {
                    return new int[] {642, 346};
                }
            case "nnl":
                return new int[] {288, 426};
            case "td":
                return new int[] {578, 365};
            case "ptv":
                return new int[] {249, 444};
            case "pvl":
                return new int[] {541, 471};
            case "vmn":
                if (x == 18) {
                    return new int[] {385, 212};
                } else {
                    return new int[] {149, 216};
                }
        }
        return new int[2];
    }

    private static int[] handleIdlingSmall(String location, int x) {
        switch (location) {
            case "kt":
                if (x == 29) {
                    return new int[] {105, 342};
                } else {
                    return new int[] {702, 495};
                }
            case "lpm":
                return new int[] {207, 349};
            case "ktdg":
                if (x == 32) {
                    return new int[] {511, 198};
                } else {
                    return new int[] {370, 166};
                }
            case "dm":
                return new int[] {188, 267};
            case "ksl":
                return new int[] {546, 464};
            case "lho":
                return new int[] {434, 243};
            case "ktng":
                if (x == 26) {
                    return new int[] {620, 473};
                } else {
                    return new int[] {471, 479};
                }
            case "bhc":
                if (x == 22) {
                    return new int[] {187, 180};
                } else if (x == 20) {
                    return new int[] {351, 180};
                } else {
                    return new int[] {636, 397};
                }
            case "gn":
                return new int[] {163, 498};
            case "lm":
                return new int[] {244, 404};
            case "lssl":
                return new int[] {199, 180};
            case "lstk":
                return new int[] {626, 318};
            case "dps":
                return new int[] {389, 469};
            case "lth":
                return new int[] {632, 181};
            case "bhd":
                return new int[] {577, 176};
        }
        return new int[2];
    }

}
