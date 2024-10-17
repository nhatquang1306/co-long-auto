public class Dest {
    public int methodId;
    public int mapX, mapY;
    public int x, y;
    public String dest;

    public Dest(int methodId) {
        this.methodId = methodId;
        if (methodId == 2) { // at ti vo dao
            this.mapX = 618;
            this.mapY = 503;
            this.x = 58;
            this.y = 132;
            this.dest = "tivo";
        } else if (methodId == 3) { // in hoang thach tran
            this.x = 34;
            this.y = 188;
            this.dest = "hoang thach";
        }
    }

    public Dest(int x, int y, String dest) {
        this.methodId = 0;
        this.mapX = -1;
        this.mapY = -1;
        this.x = x;
        this.y = y;
        this.dest = dest;
    }

    public Dest(int mapX, int mapY, int x, int y, String dest) {
        this.methodId = -1;
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = x;
        this.y = y;
        this.dest = dest;
    }
}
