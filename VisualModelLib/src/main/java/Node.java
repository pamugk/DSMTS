import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Класс, представляющий вершину гиперграфа с полюсами
public class Node {
    //Множество входных полюсов, имеющихся у вершины
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID полюса, а значение - сам полюс
    private HashMap<String, Pole> inputPoles;
    //Множество выходных полюсов, имеющихся у вершины
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID полюса, а значение - сам полюс
    private HashMap<String, Pole> outputPoles;
    //Гиперграф с полюсами, отражающий внутреннюю структуру вершины
    private PolarHypergraph innerStructure;

    private String id;

    public Node(String id) {
        inputPoles = new HashMap<>();
        outputPoles = new HashMap<>();
        this.id = id;
    }

    public List<Pole> getPoles(){
        return Stream.concat(inputPoles.values().stream(), outputPoles.values().stream()).collect(Collectors.toList());
    }

    public List<Pole> getInputPoles(){
        return new ArrayList<>(inputPoles.values());
    }
    public List<Pole> getOutputPoles(){
        return new ArrayList<>(inputPoles.values());
    }

    public PolarHypergraph getInnerStructure() {
        return innerStructure;
    }

    public void setInnerStructure(PolarHypergraph innerStructure) {
        this.innerStructure = innerStructure;
    }

    public String getId(){
        return id;
    }

    //Метод, возвращающий по переданному ID полюс вершины,
    // если в вершине имеется полюс с требуемым ID,
    //и null, если полюс с переданным ID в вершине отсутствует
    public Pole tryGetPole(String id) {
        Pole pole = inputPoles.getOrDefault(id, null);
        return pole == null ? outputPoles.getOrDefault(id, null) : pole;
    }

    //Метод для включения входного полюса в вершину
    //Возвращает true, есди добавление удалось (добавляемый полюс еще не является входным для вершины),
    //Иначе - false (добавляемый полюс уже является входным для вершины)
    public boolean tryAddInputPole(Pole pole) {
        return pole != null && pole.getParent().getId().equals(id) && inputPoles.putIfAbsent(pole.getId(), pole) == null;
    }

    //Метод для включения выходного полюса в вершину
    //Возвращает true, есди добавление удалось (добавляемый полюс еще не является выходным для вершины),
    //Иначе - false (добавляемый полюс уже является выходным для вершины)
    public boolean tryAddOutputPole(Pole pole) {
        return pole != null && pole.getParent().getId().equals(id) && inputPoles.putIfAbsent(pole.getId(), pole) == null;
    }

    //Метод для удаления полюса из вершины по переданному ID полюса
    //Возвращает true, если удаление удалось (полюс с переданным ID найден и удален)
    //Иначе возвращает false
    public boolean tryRemovePole(String id) {
        Object removedInputPole = inputPoles.remove(id);
        Object removedOutputPole = outputPoles.remove(id);
        return removedInputPole != null || removedOutputPole != null;
    }
}
