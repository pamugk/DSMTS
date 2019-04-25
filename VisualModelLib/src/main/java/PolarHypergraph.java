import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Класс, представляющий гиперграф с полюсами
public class PolarHypergraph {
    //Множество вершин, входящих в гиперграф
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID вершины, а значение - сама вершина
    private HashMap<String, Node> nodes;
    //Множество внешних полюсов гиперграфа
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID полюса, а значение - сам полюс
    private HashMap<String, Pole> poles;
    //Множество гиперребер, входящих в гиперграф
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID гиперребра, а значение - само гиперребро
    private HashMap<String, Hyperedge> hyperedges;

    public PolarHypergraph()
    {
        nodes = new HashMap<>();
        poles = new HashMap<>();
        hyperedges = new HashMap<>();
    }

    public List<Node> getNodes() { return new ArrayList<>(nodes.values()); }

    public List<Pole> getPoles() {
        return new ArrayList<>(poles.values());
    }

    public List<Hyperedge> getHyperedges() {
        return new ArrayList<>(hyperedges.values());
    }

    //Метод, возвращающий по переданному ID вершину гиперграфа,
    // если в гиперграфе имеется вершина с требуемым ID,
    //и null, если вершина с переданным ID в гиперграфе отсутствует
    public Node tryGetNode(String id){
        return nodes.getOrDefault(id, null);
    }

    //Метод для включения полюса в гиперребро
    //Возвращает true, есди добавление удалось (добавляемый полюса еще не входит в гиперребро),
    //Иначе - false (добавляемый полюса уже входит в гиперребро),
    public boolean tryAddNode(Node node) {
        return nodes.putIfAbsent(node.getId(), node) == null;
    }

    //Метод для удаления вершины из гиперграфа по переданному ID вершины
    //Возвращает true, если удаление удалось (вершина с переданным ID найдена и удалена)
    //Иначе возвращает false
    public boolean tryRemoveNode(String id) {
        return nodes.remove(id) != null;
    }

    //Метод, возвращающий по переданному ID полюс гиперграфа,
    // если в гиперграфе имеется полюс с требуемым ID,
    //и null, если полюс с переданным ID в гиперграфе отсутствует
    public Pole tryGetPole(String id){
        return poles.getOrDefault(id, null);
    }

    //Метод для включения внешнего полюса в гиперграф
    //Возвращает true, есди добавление удалось (добавляемый полюс еще не входит в гиперграф),
    //Иначе - false (добавляемый полюс уже входит в гиперграф),
    public boolean tryAddPole(Pole pole){
        return poles.putIfAbsent(pole.getId(), pole) == null;
    }

    //Метод для удаления полюса из гиперграфа по переданному ID полюса
    //Возвращает true, если удаление удалось (полюс с переданным ID найден и удален)
    //Иначе возвращает false
    public boolean tryRemovePole(String id) {
        return poles.remove(id) != null;
    }

    //Метод, возвращающий по переданному ID гиперребро гиперграфа,
    // если в гиперграфе имеется гиперребро с требуемым ID,
    //и null, если гиперребро с переданным ID в гиперграфе отсутствует
    public Hyperedge tryGetHyperedge(String id){
        return hyperedges.getOrDefault(id, null);
    }

    //Метод для включения гиперребро в гиперграф
    //Возвращает true, есди добавление удалось (добавляемое гиперребро еще не входит в гиперграф),
    //Иначе - false (добавляемое гиперребро уже входит в гиперграф),
    public boolean tryAddHyperedge(Hyperedge hyperedge){
        return hyperedges.putIfAbsent(hyperedge.getId(), hyperedge) == null;
    }

    //Метод для удаления гиперребра из гиперграфа по переданному ID гиперребра
    //Возвращает true, если удаление удалось (гиперребро с переданным ID найдено и удалено)
    //Иначе возвращает false
    public boolean tryRemoveHyperedge(String id) {
        return hyperedges.remove(id) != null;
    }
}
