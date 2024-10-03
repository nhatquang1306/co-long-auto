import java.util.Queue;

public class SwitchStatements {
    public static boolean parseDestination(String destination, Queue<Dest> queue) {
        boolean res = false;
        int index = destination.indexOf("do") + 3;
        switch (destination.charAt(index)) {
            case 'C':
                char c3 = destination.charAt(index + 1);
                if (c3 == 'u') { // cung to to
                    res = true;
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(3));
                    queue.offer(new Dest(57, 48, "hoang thach"));
                } else { // chuong chan seu
                    queue.offer(new Dest(37, 145, "long mon"));
                }
                break;
            case 'L': // ly than dong
                res = true;
                queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                queue.offer(new Dest(2));
                queue.offer(new Dest(3));
                queue.offer(new Dest(623, 264, 10, 307, "luc thuy"));
                queue.offer(new Dest(30, 199, "ngan cau"));
                break;
            case 'T':
                char c2 = destination.charAt(index + 3);
                if (c2 == 'm') { // tram lang
                    res = true;
                    queue.offer(new Dest(472, 227, 173, 164, "kinh thanh"));
                    queue.offer(new Dest(2));
                    queue.offer(new Dest(74, 86, "vo danh"));
                } else if (c2 == 't') { // tiet dai han
                    res = true;
                    queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                    queue.offer(new Dest(51, 161, "hao han"));
                } else if (c2 == 'n') { // trinh trung
                    res = true;
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(38, 79, "dien vo"));
                } else { // thiet dien phan quan
                    res = true;
                    queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                    queue.offer(new Dest(32, 57, "tang kiem"));
                }
                break;
            case 'M': // ma khong quan
                char c = destination.charAt(index + 3);
                if (c == 'K') {
                    res = true;
                    queue.offer(new Dest(102, 497, 161, 49, "dieu phong"));
                    queue.offer(new Dest(18, 60, "quan dong"));
                } else { // ma quan lao thai ba
                    queue.offer(new Dest(22, 110, "ky dao"));
                }
                break;
            case 'Đ': // duong thu thanh duong mon
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(14, 71, "thoi luyen"));
                break;
            case 'N': // ngoc linh lung quy vuc
                res = true;
                queue.offer(new Dest(688, 199, 18, 254, "kinh thanh dong"));
                queue.offer(new Dest(29, 70, "quy"));
                break;
            case 'S': // so luu huong
                queue.offer(new Dest(26, 57, "luu huong"));
                break;
            case 'H': // han thuan + hac sinh y
                char c4 = destination.charAt(index + 2);
                if (c4 == 'n') {
                    queue.offer(new Dest(29, 84, "binh khi"));
                } else {
                    queue.offer(new Dest(10, 73, "thai binh"));
                }
                break;
            case 'K': // kim phung hoang
                queue.offer(new Dest(20, 6, "kim ly"));
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
            case "tivodo.":
                return new int[] {166, 194};
            case "thanh y lau- cuu k":
                return new int[] {432, 274};
            case "thuc trung.":
                if (x == 14) {
                    return new int[] {174, 179};
                } else if (x == 29) {
                    return new int[] {618, 156};
                } else {
                    return new int[] {642, 346};
                }
            case "nguyet nha loan":
                return new int[] {288, 426};
            case "thanh dan.":
                return new int[] {578, 365};
            case "phuc tho vien.":
                return new int[] {249, 444};
            case "phu van linh.":
                return new int[] {541, 471};
            case "van ma nguyen.":
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
            case "kinh thanh.":
                if (x == 29) {
                    return new int[] {105, 342};
                } else {
                    return new int[] {702, 495};
                }
            case "luc phien mon.":
                return new int[] {207, 349};
            case "kinh thanh dong g":
                if (x == 32) {
                    return new int[] {511, 198};
                } else {
                    return new int[] {370, 166};
                }
            case "duong mon.":
                return new int[] {188, 267};
            case "kim sa loan.":
                return new int[] {546, 464};
            case "lien hoan o.":
                return new int[] {434, 243};
            case "kinh thanh nam gi":
                if (x == 26) {
                    return new int[] {620, 473};
                } else {
                    return new int[] {471, 479};
                }
            case "bach hoa coc.":
                if (x == 22) {
                    return new int[] {187, 180};
                } else if (x == 20) {
                    return new int[] {351, 180};
                } else {
                    return new int[] {636, 397};
                }
            case "giang nam.":
                return new int[] {163, 498};
            case "long mon.":
                return new int[] {244, 404};
            case "lang son son loc.":
                return new int[] {199, 180};
            case "lang sdn tieu kinh":
                return new int[] {626, 318};
            case "dieu phong son.":
                return new int[] {389, 469};
            case "luc thuy ho.":
                return new int[] {632, 181};
            case "bac hanh dao.":
                return new int[] {577, 176};
        }
        return new int[2];
    }

}
