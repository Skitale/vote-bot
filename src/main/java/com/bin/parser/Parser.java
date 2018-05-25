package com.bin.parser;

import java.io.*;
import java.util.*;

public class Parser {

    private List<String> excludeList = new ArrayList<>();
    private List<String> includeList = new ArrayList<>();
    private Map<String, String> messages = new HashMap<>();
    private Map<String, String> configurationValues = new HashMap<>();
    private String encoding;

    public Parser(String encoding) {
        this.encoding = encoding;
    }

    public void parseFiles(){
        try {
            Reader r = new InputStreamReader(new FileInputStream("res/excludeList.txt"), encoding);
            Scanner scanner = new Scanner(r).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = parseItemFile(scanner);
                excludeList.add(srt);
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Reader r = new InputStreamReader(new FileInputStream("res/includeList.txt"), encoding);
            Scanner scanner = new Scanner(r).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = parseItemFile(scanner);
                includeList.add(srt);
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Reader r = new InputStreamReader(new FileInputStream("res/messagesInChat.txt"), encoding);
            Scanner scanner = new Scanner(r).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = scanner.next();
                String[] array = srt.split("=");
                messages.put(array[0].replaceAll(" ", ""), array[1]);
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Reader r = new InputStreamReader(new FileInputStream("res/config.txt"), encoding);
            Scanner scanner = new Scanner(r).useDelimiter("(\r\n)");
            while (scanner.hasNext()){
                String srt = scanner.next();
                String[] array = srt.split("=");
                Integer i = array[1].indexOf(';');
                configurationValues.put(array[0].replaceAll(" ", ""), array[1].substring(0, i));
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public Map<String, String> getConfigurationValues() {
        return configurationValues;
    }
}
