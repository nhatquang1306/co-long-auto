public class Dest {
    public int methodId;
    public int mapX, mapY;
    public int[][] coords;
    public String dest;

    public Dest(int methodId) {
        this.methodId = methodId;
        if (methodId == 2) { // at ti vo dao
            this.mapX = 618;
            this.mapY = 503;
            this.coords = new int[][] {{58, 132}};
            this.dest = "tivo";
        } else if (methodId == 3) { // in hoang thach tran
            this.coords = new int[][] {{34, 188}};
            this.dest = "hoang thach";
        }
    }
    public Dest(int[][] coords, String dest) {
        this.methodId = 0;
        this.mapX = -1;
        this.mapY = -1;
        this.coords = coords;
        this.dest = dest;
    }
    public Dest(int mapX, int mapY, int[][] coords, String dest) {
        this.methodId = -1;
        this.mapX = mapX;
        this.mapY = mapY;
        this.coords = coords;
        this.dest = dest;

    }
}
