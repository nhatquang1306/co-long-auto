public class Dest {
    public boolean toTVD, toHTT;
    public int mapX, mapY;
    public int x, y;
    public String dest;

    public Dest(boolean toHTT) {
        this.toTVD = true;
        this.toHTT = toHTT;
        this.mapX = -1;
        this.mapY = -1;
        this.x = -1;
        this.y = -1;
        this.dest = "";
    }
    public Dest(int x, int y, String dest) {
        this.toTVD = false;
        this.mapX = -1;
        this.mapY = -1;
        this.x = x;
        this.y = y;
        this.dest = dest;
    }
    public Dest(int mapX, int mapY, int x, int y, String dest) {
        this.toTVD = false;
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = x;
        this.y = y;
        this.dest = dest;

    }

    @Override
    public String toString() {
        return "Dest{" +
                "toTVD=" + toTVD +
                ", toHTT=" + toHTT +
                ", mapX=" + mapX +
                ", mapY=" + mapY +
                ", x=" + x +
                ", y=" + y +
                ", dest='" + dest + '\'' +
                '}';
    }
}
