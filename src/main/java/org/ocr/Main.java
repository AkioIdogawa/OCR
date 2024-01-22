package org.ocr;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.exception.TikaException;
//import org.apache.tika.parser.ocr.TesseractOCRConfig;
//import org.apache.tika.parser.pdf.PDFParserConfig;
import org.xml.sax.SAXException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.ocr.Indexador.bulkIndex;
//--------------------------------------------------------------------------------------
public class Main {
    public static void anexaArquivosAoIndice(String startDir, String index) throws IOException {
        File dir = new File(startDir);
        File[] files = dir.listFiles();
        ArrayList<Documento> documentos = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) { // Checa se file é um diretório
                    anexaArquivosAoIndice(file.getAbsolutePath(), index); // Começa novo loop
                } else {
                    try {
                        String caminho = file.getAbsolutePath();
                        String[] extensoes = {"pdf"};
                        for(String extensao : extensoes){
                            if (caminho.endsWith(extensao)){
                                String conteudo = extraiConteudo(file);
                                System.out.print(caminho);
                                Documento documento = new Documento(conteudo, caminho);
                                documentos.add(documento);
                            }
                        }
                    } catch (TikaException | IOException | SAXException | TesseractException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        bulkIndex(documentos, index);
    } //--------------------------------------------------------------------------------------
    public static String extraiConteudo(File stream)
            throws IOException, TikaException, SAXException, TesseractException {
        String text = null;
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Java\\Maven Projects\\OCR\\Tess4J\\tessdata");
        tesseract.setLanguage("por");
        try {
            text = tesseract.doOCR(stream);
        }catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return text;
    }
    //--------------------------------------------------------------------------------------
    public static ArrayList<String> lerPastasNoArquivo(File arquivos){
        BufferedReader reader;
        ArrayList<String> pastas = new ArrayList<>();
        try {
            reader = new BufferedReader( new InputStreamReader(new FileInputStream(arquivos), StandardCharsets.ISO_8859_1));
            String line = reader.readLine();
            System.out.println("Pastas a serem indexadas:");
            while (line != null) {
                System.out.println(line);
                pastas.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pastas;
    } //--------------------------------------------------------------------------------------
    public static ArrayList<File> lerArquivosDaPasta(File folder) {
        ArrayList<File> arquivos = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (!file.isDirectory()) {
                System.out.println(file.getName());
                arquivos.add(file);
            } else {
                lerArquivosDaPasta(file);
            }
        }
        return arquivos;
    } //--------------------------------------------------------------------------------------
    public static void indexaPastaIndexes() throws IOException {
        File folder = new File("c:/Ocr"); //Contém pastas com arquivos de index
        ArrayList<File> arquivos = lerArquivosDaPasta(folder);
        for (File arquivo: arquivos){
            //File caminho = new File(arquivo.getParent()); //Pega caminho sem nome do arquivo
            File nomeSemCaminho = new File(arquivo.getName());
            String index = nomeSemCaminho.toString().substring(0, nomeSemCaminho.toString().lastIndexOf('.'));
            //apagaIndex(index); //Apaga indexes para reindexar
            ArrayList<String> pastas = lerPastasNoArquivo(arquivo);
            for (String pasta : pastas) {
                try {
                    anexaArquivosAoIndice(pasta, index);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    } //--------------------------------------------------------------------------------------
    public static void apagaIndex(String index) throws IOException {
        try {
            URL url = new URL("http://localhost:9200/" + index);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpCon.setRequestMethod("DELETE");
            httpCon.connect();
            httpCon.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } //--------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException{
        long inicio = System.nanoTime();
        indexaPastaIndexes();
        long tempoCorrido = (System.nanoTime() - inicio) / 1_000_000_000;
        System.out.println("Tempo total para indexar: "
                + tempoCorrido / 60 + " minuto(s).");
    }
}