//Класс полюса гиперграфа с полюсами
public class Pole {
    //Вершина-родитель полюса (содержащая данный полюс)
    private Node parent;
    //ID полюса
    private String id;

    public Pole(Node parent, String id)
    {
        this.parent = parent;
        this.id = id;
    }

    public Node getParent(){
        return parent;
    }
    public String getId(){
        return id;
    }
}
