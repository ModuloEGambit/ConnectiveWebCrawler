import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;

public class Connect {
    private Export export;
    private Map<String, Integer> wordCountList;
    private Map<String, String> urlHarvestedList;
    private TreeMap<String, Integer> titleWordList;
    private List<String> ignoreList;
    private int cap;


    public Connect(List<String> list, int cap) throws IOException {
        wordCountList = new TreeMap<>();
        urlHarvestedList = new HashMap<>();
        titleWordList = new TreeMap<>();
        ignoreList = new ArrayList<>();
        this.cap = cap;
        for (String url : list) {
            urlConnect(url);
        }


        Export export = new Export(urlHarvestedList, titleWordList, wordCountList);
    }


    // urlConnect connects to the url given and processes the data.
    public void urlConnect(String url) throws IOException {
        Document newNode;
        newNode = Jsoup.connect(url).get();
        harvestURLLinksFromIndexPage(newNode);
        repeatedHarvesterWordCount();
        frequencyInternalTitleWordCount();
    }

    // Looks into the harvestList of URLS and does a frequency word check, adding to the list
    private void repeatedHarvesterWordCount() throws IOException {
        Iterator it = urlHarvestedList.entrySet().iterator();
        int counter = 0, maxSize = urlHarvestedList.size(), counterCAP = 0;
        String url;
        Map.Entry pair;
        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            url = (String) pair.getKey();
            System.out.println("Analysing : " + counter++ + " of " + maxSize + " - " + url + " : " + pair.getValue() + " Size: " + wordCountList.size());
            try {
                Document doc = Jsoup.connect(url).get();

                // System.out.println(doc.text().toLowerCase()); Test measure: to check if doc.text() works
                frequencyWordCount(doc.text().toLowerCase());

            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (counterCAP++ > cap) {
                    break;
                }
            }

            //System.out.println("SIZE of wordCounter: " + wordCountList.size());

        }

    }

    // Does a separated count of the String set (text of webpage) and does a search for matching words.
    private void frequencyWordCount(String set) {
        String keyWord;
        //System.out.println("SPLIT LENGTH: " +  set.split(" ").length);
        for (String irregularWord : set.split(" ")) {
            //System.out.println("Counter of split: " + splitCounter++);
            if (searchWordInMap(irregularWord, wordCountList)) {
                keyWord = grabWordInMap(irregularWord, wordCountList);
                System.out.println("Adding word to Count: " + irregularWord);
                wordCountList.put(keyWord, wordCountList.get(keyWord) + 1);
                continue;
            } else {
                System.out.println("New Word: " + irregularWord);
                wordCountList.put(irregularWord, 1);
            }
        }
    }

    // Does a search in the TreeMap and adds to a list containing possibilities, then does a calculation to check for word accuracy
    // String Return uses a method to return the most accurate word from the accumulatedPossibleList
    private String grabWordInMap(String irregularWord, Map<String, Integer> map) {
        List<String> accumPossibleList = new ArrayList<>();
        Iterator it = map.entrySet().iterator();
        Map.Entry pair;

        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            if (irregularWord.contains((String) pair.getKey())) {
                accumPossibleList.add((String) pair.getKey());
                continue;
            }
        }
        return returnCorrectWord(irregularWord, accumPossibleList);

    }

    // Checks the accumulatedList and does a calculated check for accuracy in the possible list. : returns most accurate word.
    private String returnCorrectWord(String irregularWord, List<String> accumList) {
        final int BUFFER_ERROR = 1; // incase the word is zero count.
        Iterator itList = accumList.iterator();
        String possibleMatch, storedMatch = "";
        int count, lowestCount = irregularWord.length();
        while (itList.hasNext()) {
            possibleMatch = (String) itList.next();
            // Crucial IF statement .. uses BUFFER_ERROR to bypass nullpointer exception.. ListMatch word legnth check modulo against irregularWord lengths.. Lowest count to 0 is accurate.

            if ((count = ((possibleMatch.length() + BUFFER_ERROR) % (irregularWord.length() + BUFFER_ERROR))) < (lowestCount + BUFFER_ERROR)) {
                /* Eg..
                    pair2 % air // 5 % 3 = 2
                    pair2 % a // 5 % 1 = 3
                    pair2 % pair // 4 % 5 = 1
                    pair2 % ""
                 */
                //System.out.println("Inside IF - " + possibleMatch + " : " + count); TEST WORKS -- STORED POSSIBLE MATCH
                lowestCount = count - BUFFER_ERROR;
                storedMatch = possibleMatch;
                continue;

            }
        }
        //System.out.println("New Word: " + storedMatch); TEST WORKS -- STORED MATCH CHECK

        return storedMatch;


    }



    // Does a word search of exact word.. based on map
    private boolean searchWordInMap(String word, Map<String,Integer> map) {
            if ((map.containsKey(word))) {
                return true;
            }
        return false;
    }

    //Simpler method then frequencyWordCount; since it uses cleaner wording than the text of webpage.. eg. Mathematics compared to Mathematics123
    private void frequencyInternalTitleWordCount() {
        Iterator it = urlHarvestedList.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            for (String word : pair.getValue().toString().split(" ")) {
                if (titleWordList.containsKey(word)) {
                    titleWordList.put(word, titleWordList.get(word) + 1);

                } else {
                    titleWordList.put(word, 1);
                }

            }
        }
    }

    //Checks in a webpage and harvests all the URLs in the page, then reconstructs the base url. then attaches to the partial url
    // EG Original URL = https://www.wikipedia/wiki/Software_Development/
    // EG Partial URL = /wiki/Structured_Programming
    // Process -- reconstructs the original URL to https://www.wikipedia.com/
    // Process -- attaches the original URL with partial URL to create new link.
    // EG Whole URL = http://www.wikipedia/wiki/Structured_Programming
    // NOTE may contain errors. // currently reliable for wikipedia.
    private void harvestURLLinksFromIndexPage(Document doc) {
        //System.out.println("Harvesting Test");
        Elements wholeURLListRAW = doc.select("a[href]");
        String partialURL, wholeURL = "", origURL;
        String[] seperatedDepth;


        for (Element partial : wholeURLListRAW) {
            partialURL = partial.attr("href");
            System.out.println(partialURL);


            if(isContainsSymbolsForLinkTitle(partial.text())){
                ignoreList.add(partial.text());
                continue;
            }

            origURL = doc.location();
            System.out.println(origURL);
            seperatedDepth = origURL.split("/");
            origURL = constructOrigUrl(seperatedDepth);


            if (!(partialURL.startsWith("http"))) {
                if (isStartingWithBackslash(partialURL)) {
                    partialURL = partialURL.replaceFirst("/", "");
                }
                wholeURL = origURL + partialURL;
                System.out.println("Whole URL : " + wholeURL);
            }
            if (!(isDuplicateLink(wholeURL))) {
                System.out.println("Inserting: " + partial.text() + " size: " + urlHarvestedList.size());

                urlHarvestedList.put(wholeURL, partial.text());
            }

        }
        printIgnoreList();
        printAllHarvestedList();
    }

    //Separates the URL by depth of backslash '/' set to 3. Then constructs the original url used
    private String constructOrigUrl(String[] depthOfBackslash){
        StringBuilder builder = new StringBuilder();
        System.out.println("    Depth of URL -- " + depthOfBackslash.length);
        for(int depth = 0; depth < 3; depth++){
            System.out.println(depthOfBackslash[depth]);

            builder.append(depthOfBackslash[depth]);
            builder.append('/');
        }
        System.out.println("Constructed new URL : " + builder.toString());
        return builder.toString();
    }


    // Checks if starting with backslash..
    public boolean isStartingWithBackslash(String url){
        return url.startsWith("/");
    }

    // Checks if ending with backslash.
    public boolean isEndingWithBackslash(String url){
        return url.endsWith("/");
    }

    // Prints all the url harvestedList
    private void printAllHarvestedList() {
        Iterator it = urlHarvestedList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println("Key: " + pair.getKey() + " : " + pair.getValue());
        }
    }

    // Checks Duplicity of link in urlHarvestedList and returns true if duplicate.
    private boolean isDuplicateLink(String urlLink){
        Iterator it = urlHarvestedList.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (urlLink.equals(pair.getValue())) {
                System.out.println("Duplicate link : " + pair.getValue());
                return true;
            }
        }
        return false;
    }

    // Does a simple check for title articles.. presents cleaner urlHarvestedList with increased accessible links
    public boolean isContainsSymbolsForLinkTitle(String check) {
        return "^<>[]".contains(check) || (check.startsWith("[") && check.endsWith("]") || check.equals("edit"));
    }

    // Prints the list of ignored lists: currently only displays titles.
    public void printIgnoreList(){
        System.out.println("Printing Ignore List: ");

        for(int i = 0; i < ignoreList.size(); i++){
            System.out.println("Ignoring Link: " +ignoreList.get(i));
        }
    }

}
