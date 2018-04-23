import java.util.*;


//Just created this.. originally for efficient search. Not used in program..
public class BinarySearch {
    TreeMap<String, Integer> treeMap;
    Map<String,Integer> indexList;
    HashSet<String> urlHarvesterList;
    public BinarySearch(Map<String,Integer> indexList){
       treeMap = new TreeMap<>();

       this.indexList = new HashMap<>();
       this.indexList.putAll(indexList);

       loopOrganise(this.indexList);

       convertToHashMap();
    }

    private void loopOrganise(Map<String,Integer> listMap){
        Iterator it = listMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            treeMap.put((String) pair.getKey(), (Integer) pair.getValue());
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove();
        }

    }

    private void convertToHashMap(){
        this.indexList.clear();
        this.indexList.putAll(treeMap);

        for(Map.Entry<String,Integer> branch: indexList.entrySet()) {
            System.out.println(branch.getKey() + " = " + branch.getValue());


        }

    }

    private static void printAll(TreeMap<String, Integer> treeMap){
        for(Map.Entry<String,Integer> branch : treeMap.entrySet()){
            System.out.println(branch.getKey() + " = " + branch.getValue());
        }
        System.out.println();
    }
}
