import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

class Main {
    public static void main(String[] args) {
        referencesFind(siteMapFind("http://gmail.com/robots.txt"));
    }

    //Парсинг файла robots.txt (ссылка на него передается в параметре url)
    //Ссылка на файл sitemap.xml возвращается из метода
    public static String siteMapFind(String url) {
        HttpURLConnection connection = null;
        String answer = "", line;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                if (line.contains("Sitemap:")) {
                    answer = line.substring(8).trim();
                    break;
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Error siteMapFind: " + e.getMessage() + ", " + e.getCause());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return answer;
    }

    //Парсинг файла sitemap.xml (ссылка на него передается в параметре url)
    //Список всех ссылок из файла sitemap.xml возвращается в виде Set
    public static Set<String> referencesFind(String url) {
        Set<String> references = new HashSet<>();

        //Копируем sitemap из указанного адреса в интернете на жесткий диск
        HttpURLConnection connection = null;
        File xmlFile = new File("sitemap.xml");
        try {
            FileOutputStream output = new FileOutputStream(xmlFile);
            connection = (HttpURLConnection) new URL(url).openConnection();
            InputStreamReader input = new InputStreamReader(connection.getInputStream());

            int i = -1;
            while ((i = input.read()) != -1) {
                output.write(i);
            }
            input.close();
            output.close();
        } catch (Exception e) {
            System.out.println("Error referencesFind (copy): " + e.getMessage() + ", " + e.getCause());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        //Парсим скопированный sitemap.xml
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            NodeList nodeList = doc.getElementsByTagName("url");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    //System.out.println(element.getElementsByTagName("loc").item(0).getTextContent());
                    references.add(element.getElementsByTagName("loc").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error referencesFind (parse): " + e.getMessage() + ", " + e.getCause());
        }
        xmlFile.delete();
        return references;
    }
}
