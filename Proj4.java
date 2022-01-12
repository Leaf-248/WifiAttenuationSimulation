import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** Lavinia Kong.
    lkong12
    10/29/2021
    This project models and simulate the signal strength of a wifi router
    in a rectangular grid based on the distance of a cell from the router,
    the attenuation, or weakening of the signal strength due to walls
    made of common building materials
    
 */
public class Proj4 {

   public static void main(String[] args) throws IOException {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
   
      final double EPS = .0001;
      final String PROMPT1 = "Enter name of grid data file: ";
      final String ERROR = "ERROR: invalid input file";
   
      Scanner kb = new Scanner(System.in);
      System.out.print(PROMPT1);
      String name = kb.nextLine();
      
      FileInputStream infile = new FileInputStream(name);
      Scanner scanFile = new Scanner(infile);
      double size = scanFile.nextDouble();
      int rows = scanFile.nextInt();
      int cols = scanFile.nextInt();
      scanFile.nextLine(); // skip past end of line character 
      
      FileOutputStream outstream = new FileOutputStream("signals.txt");
      PrintWriter outfile = new PrintWriter(outstream);  
      
      Cell[][] grid = new Cell[rows][cols];
      Cell[][] old = new Cell[rows][cols];
      initialize(grid);
      initialize(old); 
               
      int routerRow;
      int routerCol;
      final double ROUTER = 23;
      
      
      read(grid, scanFile);
      if (! isValid(grid)) {
         System.out.println(ERROR);
      } else { 
         // keep processing
         System.out.print("Enter router row and column: ");
         routerRow = kb.nextInt();
         routerCol = kb.nextInt();
         grid[routerRow][routerCol].setSignal(ROUTER);
         
         setAllDirections(grid, routerRow, routerCol);
         setAllDistances(grid, routerRow, routerCol, size);
         
         while (! equivalent(grid, old, EPS)) {
            copy(grid, old);     
            iterate(grid, old, routerRow, routerCol);  
            printAll(grid, outfile);   
            outfile.println();  // blank link separator
         }
      }
      
      double minSignal = findMinSignal(grid);
      System.out.println("minimum signal strength: " + minSignal + 
                     " occurs in these cells: ");
      printMinCellCoordinates(grid, minSignal);
      
      outstream.flush();
      outfile.close();
   }
  
  
   
   /** Set the direction from the router position to every other cell
    *  in the grid. (Do not change the direction of the router cell.)
    *  @param grid the grid of cells to manage
    *  @param routerRow the row position of the router cell in the grid
    *  @param routerCol the column position of the router cell in the grid
    */
   public static void setAllDirections(Cell[][] grid, int routerRow, 
                                       int routerCol) {
   // done
      String dir = "";
   //loop through 2d array
      for (int row = 0; row < grid.length; row++) {
         for (int col = 0; col < grid[row].length; col++) {
            if (!(routerRow == row && routerCol == col)) {
            
               dir = direction(routerRow, routerCol, row, col);
               grid[row][col].setDirection(dir);
            
            } 
         }
      }
   
   }
   
   /** Set the distance from the router position to every other cell
    *  in the grid. (Do not change the distance of the router cell.)
    *  @param grid the grid of cells to manage
    *  @param routerRow the row position of the router cell in the grid
    *  @param routerCol the column position of the router cell in the grid
    *  @param size the size of each cell
    */
   public static void setAllDistances(Cell[][] grid, int routerRow, 
                                       int routerCol, double size) {
      double distance = 0; 
      for (int row = 0; row < grid.length; row++) {
         for (int col = 0; col < grid[row].length; col++) {
            distance = size * Math.sqrt(Math.pow((routerRow - row), 2) + 
                       Math.pow((routerCol - col), 2));
            grid[row][col].setDistance(distance);
            
         }
      }
   
   }

   
   /** Iterate over the grid, updating the signal strength and
    *  attenuation rate of each cell based on the old values of
    *  the relevant neighbor cells.
    *  @param current the updated values of each cell
    *  @param previous the old values of each cell
    *  @param routerRow the row position of the router's cell
    *  @param routerCol the column position of the router's cell
    */         
   public static void iterate(Cell[][] current, Cell[][] previous,  
                              int routerRow, int routerCol) {
   // maybe good for now 
   /* loop through each element, if the cell is not occupied by the router
      (router is not a real word to me at this point
      it looks fake no matter how you spell it. anyway.)
      iterate the atten and then the signal strength and update previous
      to the current values    
   */
      double newSigStrength = 0.0;
      for (int row = 0; row < current.length; row++) {
         for (int col = 0; col < current[row].length; col++) {
            if (!(row == routerRow && routerCol == col)) {
               //calc new attenrate and set it
               previous[row][col] = current[row][col].makeCopy();
               current[row][col].setRate(attenRate(previous, row, col));
               //System.out.println(current[row][col].getRate());
               //calc new signal strength and set it
               //System.out.println(current[row][col].getSignal());
               newSigStrength = 23 - (fspl(current[row][col].getDistance(), 5) 
                                + current[row][col].getRate());
               current[row][col].setSignal(newSigStrength);
                
            }
         }
      }
   }
   
   /** Calculate the signal transmission free space path loss (FSPL).
    *  @param distance the distance from the source to the receiver
    *  @param frequency the frequency of the transmission
    *  @return the fspl ratio
    */
   public static double fspl(double distance, double frequency) {
      //System.out.println(distance);
      return 20 * Math.log10(distance) + 20 * Math.log10(frequency) + 92.45;
      
   }
   
   /** Calculate the attenuation rate of a cell based on the
    *  attenuation of its relevant neighbor(s).
    *  @param prev the grid of cells from prior iteration
    *  @param row the row of the current cell
    *  @param col the column of the current cell
    *  @return the new attenuation rate of that cell
    */
   public static int attenRate(Cell[][] prev, int row, int col) {
   // halp  
      int atten = 0;
      
      //get direction of the cell to the router
      //attenuation(char wall) returns int
      String dir = prev[row][col].getDirection();
      
      if ("N".equals(dir)) { // if the curr cell is north of router
         //atten of south neighbor and south wall  
         atten = prev[row + 1][col].getRate() 
                 + attenuation(prev[row][col].getSouth());
                 
      } else if ("S".equals(dir)) { // if it's south
         //atten of north neighbor and north wall
         atten = prev[row - 1][col].getRate() 
                 + attenuation(prev[row][col].getNorth());
                 
      } else if ("E".equals(dir)) { // if its east
         //atten of west neighbor and west wall
         atten = prev[row][col - 1].getRate() 
                 + attenuation(prev[row][col].getWest());
                 
      } else if ("W".equals(dir)) { // if its west
         //atten of east neighbor and west wall
         atten = prev[row][col + 1].getRate() 
                 + attenuation(prev[row][col].getEast());
                       
      } else if ("NE".equals(dir)) { // if its north east
         // you might be wondering: what is going on inside her head?
         // get atten of both west and south neighbor and their atten wall stre
         //ngth and see which direction is worse
         if (prev[row][col - 1].getRate() + 
             attenuation(prev[row][col].getWest())
             > prev[row + 1][col].getRate() 
                 + attenuation(prev[row][col].getSouth())) {
            atten = prev[row][col - 1].getRate() 
                 + attenuation(prev[row][col].getWest());
         } else {
            atten = prev[row + 1][col].getRate() 
                 + attenuation(prev[row][col].getSouth());
         }
      
      } else if ("SE".equals(dir)) { // if its South east 
         // get atten of both west and north neighbor and their atten wall stre
         //ngth and see which direction is worse
         if (prev[row][col - 1].getRate() 
                 + attenuation(prev[row][col].getWest()) > 
                 prev[row - 1][col].getRate() 
                 + attenuation(prev[row][col].getNorth())) {
            atten = prev[row][col - 1].getRate() 
                 + attenuation(prev[row][col].getWest());
         } else {
            atten = prev[row - 1][col].getRate() 
                 + attenuation(prev[row][col].getNorth());
         }
         
      } else if ("NW".equals(dir)) { // if it is north west 
         // get atten of both east and south neighbor and their atten wall stre
         //ngth and see which direction is worse
         if (prev[row][col + 1].getRate() 
                 + attenuation(prev[row][col].getEast()) > 
                 prev[row + 1][col].getRate() 
                 + attenuation(prev[row][col].getSouth())) {
            atten = prev[row][col + 1].getRate() 
                 + attenuation(prev[row][col].getEast());
         } else {
            atten = prev[row + 1][col].getRate() 
                 + attenuation(prev[row][col].getSouth());
         }
         
      } else { // if it is south west 
         // get atten of both east and north neighbor and their atten wall stre
         //ngth and see which direction is worse
         if (prev[row][col + 1].getRate() 
                 + attenuation(prev[row][col].getEast()) > 
                 prev[row - 1][col].getRate() 
                 + attenuation(prev[row][col].getNorth())) {
            atten = prev[row][col + 1].getRate() 
                 + attenuation(prev[row][col].getEast());
         } else {
            atten = prev[row - 1][col].getRate() 
                 + attenuation(prev[row][col].getNorth());
         }
      }
      
      return atten;  
      
   }
    
   
   /** Find the direction between the router cell and the current cell.
    *  For example, if (r0,c0) is (2,3) and (r1,c1) is (0,3), this
    *  method returns the string "N" to denote that the current cell
    *  is north of the router cell.
    *  @param r0 the router row
    *  @param c0 the router column
    *  @param r1 the current cell row
    *  @param c1 the current cell column
    *  @return a string direction heading (N, E, S, W, NE, SE, SW, NW)
    */
   public static String direction(int r0, int c0, int r1, int c1) {
   // maybe good
      int deltaRow = Math.abs(r1 - r0);
      int deltaCol = Math.abs(c1 - c0);      
      
      if (deltaRow == deltaCol) {
         // on one of the diagonals   
         if (r1 > r0 && c1 > c0) {
            //southeast
            return "SE";
         } else if (r1 > r0 && c1 < c0) {
            // Southwest
            return "SW";
         } else if (r1 < r0 && c1 > c0) {
            // southwest
            return "NE";
         } else {
            //south east
            return "NW";
         }    
      } else if (deltaRow > deltaCol) {
         // North
         if (r1 < r0) {
            return "N";
         }
         //south
         else if (r1 > r0) {
            return "S";
         }
      } else {
      // east and west
         if (c1 > c0) {
            return "E";
         } else if (c1 < c0) {
            return "W";
         }
      
      }
      return "";
   } 


   /** Determine if the corresponding cells in the two grids of the same size
    *  have the same signal value, to a specified precision.
    *  @param grid1 the first grid
    *  @param grid2 the second grid
    *  @param epsilon the difference cutoff that makes two values "equivalent"
    *  @return true if the grids are the same sizes, and the signal values 
    *   are all within (<=) epsilon of each other; false otherwise
    */
   public static boolean equivalent(Cell[][] grid1, Cell[][] grid2, 
                                    double epsilon) {
      
      for (int row = 0; row < grid1.length; row++) {
         for (int col = 0; col < grid1[row].length; col++) {
            if (Math.abs(grid1[row][col].getSignal() - 
                grid2[row][col].getSignal()) >= epsilon) {
               return false;
            }
         }
      }
      return true;
   }


   /**
    * Read a grid from a plain text file using a Scanner that has
    * already advanced past the first line. This method assumes the 
    * specified file exists. Each subsequent line provides the wall
    * information for the cells in a single row, using a 4-character 
    * string in NESW (north-east-south-west) order for each cell. 
    * @param grid is the grid whose Cells must be updated with the input data
    * @param scnr is the Scanner to use to read the rest of the file
    * @throws IOException if file can not be read
    */
   public static void read(Cell[][] grid, Scanner scnr) throws IOException {
     
      while (scnr.hasNext()) {
         for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
               grid[row][col].setWalls(scnr.next());
            }
         }
      }
   
   }


   /**
    * Validate the cells of a maze as being consistent with respect
    * to neighboring internal walls. For example, suppose some cell
    * C has an east wall with material 'b' for brick. Then for the 
    * maze to be valid, the cell to C's east must have a west wall
    * that is also 'b' for brick. (This method does not need to check
    * external walls.) 
    * @param grid the grid to check
    * @return true if valid (consistent), false otherwise
    */
   public static boolean isValid(Cell[][] grid) {
      
      for (int row = 0; row < grid.length; row++) {
         for (int col = 0; col < grid[row].length; col++) {
            //check top
            if ((row >= 1) && 
               (grid[row][col].getNorth() != grid[row - 1][col].getSouth())) {
               return false;
            }
            //check bottom
            if ((row + 1 < grid.length - 1) && 
               (grid[row][col].getSouth() != grid[row + 1][col].getNorth())) {
               return false;
            }
            // check left
            if ((col >= 1) && 
               (grid[row][col].getWest() != grid[row][col - 1].getEast())) {
               return false;
            }
            if ((col + 1 < grid[row].length - 1) && 
               (grid[row][col].getEast() != grid[row][col + 1].getWest())) {
               return false;
            }
         }
      }
      return true;
   }

   /** Find the minimum cell signal strength.
    *  @param grid the grid of cells to search
    *  @return the minimum signal value
    */
   public static double findMinSignal(Cell[][] grid) {

      double signalStrength = 1.1;
      for (int row = 0; row < grid.length; row++) {
         for (int col = 0; col < grid[row].length; col++) {
            if (grid[row][col].getSignal() < signalStrength) {
               signalStrength = grid[row][col].getSignal();
            }
         }
      }
      return signalStrength;
   }
   
   /** Print the coordinates of cells with <= the minimum signal strength,
    *  one per line in (i, j) format, in row-column order.
    *  @param grid the collection of cells
    *  @param minSignal the minimum signal strength
    */
   public static void printMinCellCoordinates(Cell[][] grid, double minSignal) {
   // should be done
   // well the real question is if it is right
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[i].length; j++) {
            if (grid[i][j].getSignal() == minSignal) {
               System.out.println("(" + i + ", " + j + ")");
               
            }
         }
      } 
   
   }

   /** Get the attenuation rate of a wall material.
    *  @param wall the material type 
    *  @return the attenuation rating
    */
   public static int attenuation(char wall) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      switch (wall) {
         case 'b': 
            return 22;
         case 'c': 
            return 6;
         case 'd': 
            return 4;
         case 'g': 
            return 20;
         case 'w': 
            return 6;
         case 'n': 
            return 0;
         default:
            System.out.println("ERROR: invalid wall type");
      }
      return -1;
   }


   /** Create a copy of a grid by copying the contents of each
    *  Cell in an original grid to a copy grid. Note that we use the 
    *  makeCopy method in the Cell class for this to work correctly.
    *  @param from the original grid
    *  @param to the copy grid
    */
   public static void copy(Cell[][] from, Cell[][] to) { 
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT 
      for (int i = 0; i < from.length; i++) {
         for (int j = 0; j < from[0].length; j++) {
            to[i][j] = from[i][j].makeCopy();
         }
      }
   }
   
   /** Initialize a grid to contain a bunch of new Cell objects.
    *  @param grid the array to initialize
    */
   public static void initialize(Cell[][] grid) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[0].length; j++) {
            grid[i][j] = new Cell();
         }
      }
   }
   
   /** Display the computed values of a grid (signal strenth, direction,
    *  attenuation rate, and distance to the provided output destination,
    *  using the format provided by the toString method in the Cell class.
    *  @param grid the signal grid to display
    *  @param pout the output location
    */
   public static void printAll(Cell[][] grid, PrintWriter pout) {
   // THIS METHOD IS COMPLETE - DO NOT CHANGE IT
      for (int i = 0; i < grid.length; i++) {
         for (int j = 0; j < grid[0].length; j++) {
            pout.print(grid[i][j].toString() + " ");
         }
         pout.println();
      }
   }
}