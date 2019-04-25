import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hyperedge {
    //Множество полюсов, которые связывает гиперребро
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID полюса, а значение - сам полюс
    private HashMap<String, Pole> linkedPoles;
    //Гиперграф с полюсами, отражающий внутреннюю структуру гиперребра
    private PolarHypergraph innerStructure;
    //ID гиперребра
    private String id;

    public Hyperedge(String id)
    {
        linkedPoles = new HashMap<>();
        this.id = id;
    }

    //Метод, возвращающий список полюсов,связанных гиперребром
    public List<Pole> getLinkedPoles(){
        return new ArrayList<>(linkedPoles.values());
    }

    public PolarHypergraph getInnerStructure() {
        return innerStructure;
    }
    public void setInnerStructure(PolarHypergraph innerStructure) {
        this.innerStructure = innerStructure;
    }

    public String getId() {
        return id;
    }

    //Метод, возвращающий по переданному ID полюс гиперребра,
    // если в гиперребре имеется полюс с требуемым ID,
    //и null, если полюс с переданным ID в гиперребре отсутствует
    public Pole tryGetPole(String id){
        return linkedPoles.getOrDefault(id, null);
    }

    //Метод для включения полюса в гиперребро
    //Возвращает true, есди добавление удалось (добавляемый полюс еще не входит в гиперребро),
    //Иначе - false (добавляемый полюс уже входит в гиперребро)
    public boolean tryAddPole(Pole pole) {
        return pole != null && linkedPoles.putIfAbsent(pole.getId(), pole) == null;
    }

    //Метод для удаления полюса из гиперребра по переданному ID полюса
    //Возвращает true, если удаление удалось (полюс с переданным ID найден и удален)
    //Иначе возвращает false
    public boolean tryRemovePole(String id){
        return linkedPoles.remove(id) != null;
    }
}
