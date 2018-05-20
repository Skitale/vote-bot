package com.bin.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Parser {

    private List<String> excludeList = new ArrayList<>();
    private List<String> includeList = new ArrayList<>();
    private Map<String, String> messages = new HashMap<>();

    public void parseFiles(){
        try {
            Scanner scanner = new Scanner(new FileReader("res/excludeList.txt")).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = parseItemFile(scanner);
                excludeList.add(srt);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner = new Scanner(new FileReader("res/includeList.txt")).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = parseItemFile(scanner);
                includeList.add(srt);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner = new Scanner(new FileReader("res/messagesInChat.txt")).useDelimiter("(\r\n)");

            while (scanner.hasNext()){
                String srt = scanner.next();
                String[] array = srt.split("=");
                messages.put(array[0].replaceAll(" ", ""), array[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private String parseItemFile(Scanner scanner){
        String srt = scanner.next();
        Integer i = srt.indexOf(';');
        srt = srt.substring(0, i);
        return srt;
    }

    public List<String> getExcludeList() {
        return excludeList;
    }

    public List<String> getIncludeList() {
        return includeList;
    }

    public Map<String, String> getMessages() {
        return messages;
    }
}
