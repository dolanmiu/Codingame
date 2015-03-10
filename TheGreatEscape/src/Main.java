import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int w = in.nextInt(); // width of the board
        int h = in.nextInt(); // height of the board
        int playerCount = in.nextInt(); // number of players (2 or 3)
        int myId = in.nextInt(); // id of my player (0 = 1st player, 1 = 2nd player, ...)

        Grid grid = new Grid(w, h);
        WallGrid wallGrid = new WallGrid(w, h);
        
        AStar aStar = new AStar(w, h);
        Destination destination = findDestination(myId, w, h);
        
        WallPlacer wallPlacer = new WallPlacer(wallGrid);
        
        Enemy[] enemies = new Enemy[playerCount - 1];
        
        int previousWallCount = 0;
        
        
        // game loop
        while (true) {
            int myX = 0;
            int myY = 0;
            int myWallsLeft = 0;
            int enemyIndex = 0;
            
            grid.resetAdjacencyList();
            for (int i = 0; i < playerCount; i++) {
                int x = in.nextInt(); // x-coordinate of the player
                int y = in.nextInt(); // y-coordinate of the player
                int wallsLeft = in.nextInt(); // number of walls available for the player
                
                if (i == myId) {
                    myX = x;
                    myY = y;
                    myWallsLeft = wallsLeft;
                } else {
                    if (enemies[enemyIndex] == null) {
                        enemies[enemyIndex] = new Enemy(i);
                        enemies[enemyIndex].destination = findDestination(i, w, h);
                    }
                    enemies[enemyIndex].setXY(x, y);
                    //enemies[enemyIndex].wallsLeft = wallsLeft;
                    enemyIndex++;
                }
            }
            int wallCount = in.nextInt(); // number of walls on the board
            for (int i = 0; i < wallCount; i++) {
                int wallX = in.nextInt(); // x-coordinate of the wall
                int wallY = in.nextInt(); // y-coordinate of the wall
                String wallOrientation = in.next(); // wall orientation ('H' or 'V')
                
                grid.addWall(wallX, wallY, wallOrientation);
                wallGrid.addWall(wallX, wallY, wallOrientation);
            }
            
            if (enemies.length > 1) {
                move(aStar, grid, myX, myY, destination);
            } else {
                if (atOrGreaterThanPlaceDistance(destination, 4, myX, myY)) {
                    System.err.println("at place distance");
                    if (myWallsLeft > 7) {
                        Tile nextTile = aStar.start(grid.getTile(enemies[0].getX(), enemies[0].getY()), enemies[0].destination, grid.getAdjList());
                        String orientation = getOrientationNormal(enemies[0].getX(), enemies[0].getY(), nextTile.x, nextTile.y);
                        Coordinate wallCoords = getWallPlacementBlock(enemies[0].getX(), enemies[0].getY(), nextTile.x, nextTile.y);
                        System.err.println("about to place");
                        WallData result = wallPlacer.placeWall(wallCoords.x, wallCoords.y, orientation);
                        if (result == null) {
                            move(aStar, grid, myX, myY, destination);
                        } else {
                            grid.addWall(result.x, result.y, result.orientation);
                            nextTile = aStar.start(grid.getTile(enemies[0].getX(), enemies[0].getY()), enemies[0].destination, grid.getAdjList());
                            if (nextTile != null) {
                                System.out.println(result.x + " " + result.y + " " + result.orientation);
                            } else {
                                move(aStar, grid, myX, myY, destination);
                            }
                        }
                    } else {
                        move(aStar, grid, myX, myY, destination);
                    }
                } else {
                    move(aStar, grid, myX, myY, destination);
                }
            }

            previousWallCount = wallCount;
        }
    }
    
    private static Destination findDestination(int id, int w, int h) {
        Destination destination = null;
        switch(id) {
            case 0:
                destination = new Destination(w - 1, "V");
                break;
            case 1:
                destination = new Destination(0, "V");
                break;
            case 2:
                destination = new Destination(h - 1, "H");
                break;
        }
        return destination;
    }
    
    private static boolean atOrGreaterThanPlaceDistance(Destination destination, int distance, int x, int y) {
        if (destination.orientation.equals("H")) {
            if (distance >= y) {
                System.err.println("reached distance y: " + y);
                return true;
            }
        } else {
            if (destination.tileIndex == 0) {
                if (distance >= x) {
                    System.err.println("reached distance x: " + x);
                    return true;
                }
            } else {
                if (distance <= x) {
                    System.err.println("reached distance x: " + x);
                    return true;
                }
            }
            

        }
        return false;
    }
    
    private static void move(AStar aStar, Grid grid, int myX, int myY, Destination destination) {
        Tile nextTile = aStar.start(grid.getTile(myX, myY), destination, grid.getAdjList());
        String currentDirection = getDirectionFromTile(grid.getTile(myX, myY), nextTile);
        System.out.println(currentDirection); // action: LEFT, RIGHT, UP, DOWN or "putX putY putOrientation" to place a wall
    }
    
    private static String getDirectionFromTile(Tile fromTile, Tile toTile) {
        System.err.println("Going to: " + toTile);
        if (toTile.x > fromTile.x) {
            return "RIGHT";
        } else if (toTile.x < fromTile.x) {
            return "LEFT";
        }
        
        if (toTile.y < fromTile.y) {
            return "UP";
        } else if (toTile.y > fromTile.y) {
            return "DOWN";
        }
        return "NOTHINGNESS";
    }
    
    private static String getOrientationNormal(int prevX, int prevY, int x, int y) {
        if (Math.abs(prevX - x) == 0) {
            return "H";
        } else {
            return "V";
        }
    }
    
    private static Coordinate getWallPlacementBlock(int prevX, int prevY, int x, int y) {
        if (Math.abs(prevX - x) == 0) {
            if (y > prevY) {
                return new Coordinate(x, y);
            } else {
                return new Coordinate(x, y + 1);
            }
        } else {
            if (x > prevX) {
                return new Coordinate(x, y);
            } else {
                return new Coordinate(x + 1, y);
            }
        }
    }

}

class WallData {
    public int x, y;
    public String orientation;
    
    public WallData(int x, int y, String orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}

class Coordinate {
    public int x, y;
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class WallPair {
    public boolean H;
    public boolean V;
}

class Enemy {
    private int x, y, prevX, prevY;
    public int wallsLeft;
    public int id;
    public Direction direction;
    public Destination destination;
    
    public Enemy(int id) {
        this.id = id;
    }
    
    public void setXY(int x, int y) {
        this.prevX = this.x;
        this.x = x;
        this.prevY = this.y;
        this.y = y;
        this.direction = this.findDirection(this.prevX, this.x, this.prevY, this.y);
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    private Direction findDirection(int prevX, int x, int prevY, int y) {
        if (Math.abs(prevX - x) == 0) { //must be vertical movement
            if (prevY < y) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else {
            if (prevX < x) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        }
    }
}

enum Direction {
    LEFT, RIGHT, UP, DOWN
}

class WallGrid {
    private WallPair[][] walls;
    
    public WallGrid(int w, int h) {
        this.walls = new WallPair[w][h];
        
        for (int i = 0; i < this.walls.length; i++) {
            for (int j = 0; j < this.walls[0].length; j++) {
                this.walls[i][j] = new WallPair();
            }
        }
    }
    
    public boolean addWall(int x, int y, String orientation) {
        boolean result = addUnitWall(x, y, orientation);
        if (result == false) {
            return false;
        }
        if (orientation.equals("H")) {
            result = addUnitWall(x + 1, y, orientation);
        } else {
            result = addUnitWall(x, y + 1, orientation);
        }
        
        if (result == false) {
            return false;
        }
        return true;
    }
    
    public boolean checkWall(int x, int y, String orientation) {
        boolean result = checkUnitWall(x, y, orientation);
        System.err.println("checking: " + x + " " + y);
        if (result == false) {
            return false;
        }
        if (orientation.equals("H")) {
            result = checkUnitWall(x + 1, y, orientation);
        } else {
            result = checkUnitWall(x, y + 1, orientation);
        }
        
        if (result == false) {
            return false;
        } else {
            return true;
        }

    }
    
    private boolean checkUnitWall(int x, int y, String orientation) {
        if (x > this.walls.length - 1 || y > this.walls[0].length - 1 || x < 0 || y < 0) {
            return false;
        }
        if (orientation.equals("H")) {
            if (this.walls[x][y].H == false) {
                return true;
            } else {
                return false;
            }
        } else {
            if (this.walls[x][y].V == false) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    public boolean addUnitWall(int x, int y, String orientation) {
        if (x > this.walls.length - 1 || y > this.walls[0].length - 1 || x < 0 || y < 0) {
            return false;
        }
        if (orientation.equals("H")) {
            if (this.walls[x][y].H == false) {
                this.walls[x][y].H = true;
            } else {
                return false;
            }
        } else {
            if (this.walls[x][y].V == false) {
                this.walls[x][y].V = true;
            } else {
                return false;
            }
        }
        return true;
    }
    
}

class WallPlacer {
    
    private WallGrid wallGrid;
    
    public WallPlacer(WallGrid wallGrid) {
        this.wallGrid = wallGrid;
    }
    
    private String getWallOrientation(Enemy enemy) {
        String orientation = null;
        if (enemy.id == 2) {
            orientation = "H";
        } else {
            orientation = "V";
        }
        return orientation;
    }
    
    public WallData placeWall(int x, int y, String orientation) {
        boolean result = this.wallGrid.checkWall(x, y, orientation);
        if (result) {
            this.wallGrid.addWall(x, y, orientation);
            return new WallData(x, y, orientation);
        } else {
            if (orientation.equals("H")) {
                if (this.wallGrid.checkWall(x + 1, y, orientation)) {
                    this.wallGrid.addWall(x + 1, y, orientation);
                    return new WallData(x + 1, y, orientation);
                } else if (this.wallGrid.checkWall(x - 1, y, orientation)) {
                    this.wallGrid.addWall(x - 1, y, orientation);
                    return new WallData(x - 1, y, orientation);
                }
            } else {
                System.err.println("Cheking verticals");
                if (this.wallGrid.checkWall(x, y + 1, orientation)) {
                    this.wallGrid.addWall(x, y + 1, orientation);
                    return new WallData(x, y + 1, orientation);
                } else if (this.wallGrid.checkWall(x, y - 1, orientation)) {
                    this.wallGrid.addWall(x, y - 1, orientation);
                    return new WallData(x, y - 1, orientation);
                }
            }
            System.err.println("unable to place wall!!!");
        }
        return null;
    }
}

class AStar {
    
    private int width;
    private int height;
    
    private Set closedSet;
    
    private List<Tile> openSet;
    private Map<Tile, Float> openSetDistanceMap;
    
    private float[][] gMatrix;
    
    private Map<Tile, Tile> pathAdjacencyList;
    
    public AStar(int width, int height) {
        this.width = width;
        this.height = height;
        this.closedSet = new HashSet();
        this.openSet = new ArrayList<Tile>();
        this.openSetDistanceMap = new HashMap<Tile, Float>();

        this.gMatrix = new float[width][height];
        this.pathAdjacencyList = new HashMap<Tile, Tile>();
    }
    
    public Tile start(Tile start, Destination destination, Map<Tile, List<Tile>> adjList) {
        this.resetAlgorithm();

		this.aStarAdjacent(start, destination, adjList);

		Tile currentTile = null;
		do {
			currentTile = getLowestTile();
			if (currentTile == null) { //blocked path
			    return null;
			}
			this.aStarAdjacent(currentTile, destination, adjList);
		} while (hasReached(currentTile, destination));
		System.err.println("Destination: " + currentTile);
		return this.extractPathFromAdjacencyList(this.pathAdjacencyList, currentTile).get(1);
    }
    
    private List<Tile> extractPathFromAdjacencyList(Map<Tile, Tile> pathAdjacencyList, Tile destinationTile) {
		if (!pathAdjacencyList.containsKey(destinationTile)) {
			return null;
		}
		
		Tile currentTile = destinationTile;
		List<Tile> path = new ArrayList<Tile>();
		path.add(0, currentTile);
		do {
			currentTile = pathAdjacencyList.get(currentTile);
			path.add(0, currentTile);
		} while (pathAdjacencyList.containsKey(currentTile));
		System.err.println(path.toString());
		return path;
	}
    
    private boolean hasReached(Tile currentTile, Destination destination) {
        if (destination.orientation.equals("H")) {
            return currentTile.y != destination.tileIndex;
        } else {
            return currentTile.x != destination.tileIndex;
        }
    }
    
    private void resetAlgorithm() {
        this.closedSet = new HashSet();
        this.openSet = new ArrayList<Tile>();
        this.openSetDistanceMap = new HashMap<Tile, Float>();
        this.gMatrix = new float[this.width][this.height];
        this.pathAdjacencyList = new HashMap<Tile, Tile>();
    }
    
    private Tile getLowestTile() {
        if (this.openSet.size() == 0) {
            //throw new RuntimeException("Open set is 0, there is no tiles to get lowest tile from");
            //means its blocked off path!
            return null;
        }
        Tile tile = null;
        float currentLowest = Integer.MAX_VALUE;
        for (int i = 0; i < this.openSet.size(); i++) {
            float tempLowest = this.openSetDistanceMap.get(this.openSet.get(i));
            if (tempLowest < currentLowest) {
                currentLowest = tempLowest;
                tile = this.openSet.get(i);
            }
        }
        this.openSet.remove(tile);
        this.openSetDistanceMap.remove(tile);
        return tile;
    }
    
    private void aStarAdjacent(Tile start, Destination destination, Map<Tile, List<Tile>> adjList) {
        if (start == null) {
            throw new RuntimeException("start node is null");
        }
        this.closedSet.add(start);
        List<Tile> adjTiles = adjList.get(start);
        this.doAStar(start, destination, adjTiles);

    }
    
    private void doAStar(Tile currentTile, Destination destination, List<Tile> adjTiles) {
        for (int i = 0; i < adjTiles.size(); i++) {
            this.unitAStar(currentTile, destination, adjTiles.get(i));
        }
    }
    
    private void unitAStar(Tile currentTile, Destination destination, Tile adjTile) {
        if (!this.isViableNode(adjTile)) {
			return;
		}
		if (this.isAbleToAddToOpenList(adjTile)) {
		    this.openSet.add(adjTile);
		    this.openSetDistanceMap.put(adjTile, 10f);
		}

		float gValueOriginal = this.gMatrix[currentTile.x][currentTile.y];
		float gValueDestination = this.gMatrix[adjTile.x][adjTile.y];
		float movementCost = 10;

		if (gValueOriginal + movementCost < gValueDestination || !this.pathAdjacencyList.containsKey(adjTile)) {
		    
			this.gMatrix[adjTile.x][adjTile.y] = gValueOriginal + movementCost;
			float fScore = getFScore(adjTile.x, adjTile.y, destination);
			this.openSetDistanceMap.put(adjTile, fScore);
			this.pathAdjacencyList.put(adjTile, currentTile);
			
		}
    }
    
    private float getFScore(int currentX, int currentY, Destination destination) {
		float gScore = this.gMatrix[currentX][currentY];
		float hScore;
		if (destination.orientation.equals("H")) {
		    hScore = Math.abs(destination.tileIndex - currentY);
		} else {
		    hScore = Math.abs(destination.tileIndex - currentX);
		}
		return gScore + hScore;
	}
    
    private boolean isAbleToAddToOpenList(Tile tile) {
		float gValueOriginal = this.gMatrix[tile.x][tile.y];
		if (gValueOriginal == 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isViableNode(Tile tile) {
		if (this.closedSet.contains(tile)) {
			return false;
		} else {
			return true;
		}
	}
    
}

class Destination {
    int tileIndex;
    String orientation;
    
    public Destination(int tileIndex, String orientation) {
        this.tileIndex = tileIndex;
        this.orientation = orientation;
    }
}

class Grid {
    private Map<Tile, List<Tile>> adjList;
    private Tile[][] tiles;
    
    public Grid(int width, int height) {
        this.tiles = new Tile[width][height];
        
        for (int i = 0; i < this.tiles.length; i++) {
            for (int j = 0; j < this.tiles[0].length; j++) {
                this.tiles[i][j] = new Tile(i, j);
            }
        }
        
        this.adjList = createAdjacentList(this.tiles);
    }
    
    public Map<Tile, List<Tile>> getAdjList() {
        return this.adjList;
    }
    
    public Tile getTile(int x, int y) {
        return this.tiles[x][y];
    }
    
    public boolean addWall(int x, int y, String orientation) {
        addUnitWall(x, y, orientation);
        if (orientation.equals("H")) {
            addUnitWall(x + 1, y, orientation);
        } else {
            addUnitWall(x, y + 1, orientation);
        }
        return true;
    }
    
    private boolean addUnitWall(int x, int y, String orientation) {
        if (x > this.tiles.length - 1 || y > this.tiles[0].length - 1) {
            return false;
        }
        
        Tile wallTile1 = this.tiles[x][y];
        Tile wallTile2 = null;
        if (orientation.equals("H")) {
            if (y >= 1) {
                wallTile2 = this.tiles[x][y - 1];
            }
        } else {
            if (x >= 1) {
                wallTile2 = this.tiles[x - 1][y];
            }
        }
        
        if (wallTile2 != null) {
            severTile(wallTile1, wallTile2);
            return true;
        } else {
            return false;
        }
    }
    
    private void severTile(Tile tile1, Tile tile2) {
        List<Tile> tiles = this.adjList.get(tile1);
        if (tiles.contains(tile2)) {
            tiles.remove(tile2);
        }
        
        tiles = this.adjList.get(tile2);
        if (tiles.contains(tile1)) {
            tiles.remove(tile1);
        }
    }
    
    private Map<Tile, List<Tile>> createAdjacentList(Tile[][] tiles) {
		Map<Tile, List<Tile>> adjacentList = new HashMap<Tile, List<Tile>>();
		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles[0].length; z++) {
				adjacentList.put(tiles[x][z], new ArrayList<Tile>());

				for (int i = x - 1; i <= x + 1; i++) {
					for (int j = z - 1; j <= z + 1; j++) {
						if (Math.abs(i - x) + Math.abs(j - z) > 1) {
							continue;
						}
						if (this.isTileInBounds(i, j, tiles)) {
							adjacentList.get(tiles[x][z]).add(tiles[i][j]);
						}
					}
				}
			}
		}

		return adjacentList;
	}
	
	public void resetAdjacencyList() {
	    this.adjList = createAdjacentList(this.tiles);
	}
    
    private boolean isTileInBounds(int x, int y, Tile[][] tiles) {
		if (x >= 0 && y >= 0 && x < tiles.length && y < tiles[0].length) {
			return true;
		} else {
			return false;
		}
	}
}

class Tile {
    public int x;
    public int y;
    
    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "Tile: " + x + ", " + y;
    }
}