package fr.ign.cogit.v2.utils.clustering;

public class PointND{
    
    private double[] coordinates;
    
    public PointND(int N){   
        if(N<1){
            throw new Error("Point dimension must be >1");
        }
        else{
            this.coordinates = new double[N];
        }
    }
    
    public PointND(double[] coordinates){       
        if(coordinates.length<1){
            throw new Error("Point dimension must be >1");
        }
        else{
            this.coordinates = coordinates;
        }
    }

    /**
     * Give first coordinate
     * @return
     */
    public double getX() {
        return this.coordinates[0];
    }

    /**
     * Give second coordinate
     * @return
     */
    public double getY() {
        if(Dimension()>1){
            return this.coordinates[1];
        }
        return Double.NaN;
    }

    /**
     * Give fird coordinate
     * @return
     */
    public double getZ() {
        if(Dimension()>2){
            return this.coordinates[2];
        }
        return Double.NaN;
    }

    public double[] get() {
        return this.coordinates;
    }


    public double get(int i) {
       if(this.Dimension() > i){
           return this.coordinates[i];
       }
       return Double.NaN;
    }

    public void set(double[] composantes) {
       this.coordinates = composantes;
    }

    public void set(int x, double composantes) {
       if(this.Dimension() > x){
           this.coordinates[x] = composantes;
       }
    }

    /**
     * Set first coordinate
     * @param X
     */
    public void setX(double X) {
        this.coordinates[0] = X;
    }

    /**
     * Set second coordinate
     * @param Y
     */
    public void setY(double Y) {
        if(this.Dimension() > 1){
            this.coordinates[1] = Y;
        }
    }

    /**
     * Set fird coordinate
     * @param Y
     */    public void setZ(double Z) {
        if(this.Dimension() > 2){
            this.coordinates[2] = Z;
        }
    }

    /**
     * Set first and second coordinates
     * @param X
     * @param Y
     */
    public void setXY(double X, double Y) {
        this.setX(X);
        this.setY(Y);
    }

    /**
     * Set first, second and fird coordinates
     * @param X
     * @param Y
     * @param Z
     */
    public void setXYZ(double X, double Y, double Z) {
        this.setX(X);
        this.setY(Y);
        this.setZ(Z);
    }

    public int Size() {
        return this.Dimension();
    }

    public int Dimension() {
        return this.coordinates.length;
    }

    public boolean Equal(PointND p, double Epsilon) {
      if(this.Dimension() != p.Dimension()){
          return false;
      }
      for(int i=0; i< this.Dimension(); i++){
          if(this.get(i) != p.get(i)){
              return false;
          }
      }
      return true;
    }
    
}
