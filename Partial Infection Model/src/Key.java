public class Key {

    private Vertice x;
    private Vertice y;

    public Key(Vertice x, Vertice y) {
        this.x = x;
        this.y = y;
    }
    public Vertice getX()
    {
    	return x;
    }
    public Vertice getY()
    {
    	return y;
    }
    public void setXY(Vertice input, Vertice input2)
    {
    	x=input;
    	y=input2;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key key = (Key) o;
        return (x == key.getX() && y == key.getY())||(x==key.getY() && y ==key.getX());
    }

    @Override
    public int hashCode() {
        return x.getID().hashCode()+y.getID().hashCode();
    }

}