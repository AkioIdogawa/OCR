package org.ocr;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class ElasticsearchClientConfig {
    private RestClient restClient;
    private static ElasticsearchTransport elasticsearchTransport;
    private ElasticsearchClient elasticsearchClient;
    private JacksonJsonpMapper jsonMapper;
    public ElasticsearchClientConfig(){
        init();
    }
    /**
     * Inicialização do cliente
     */
    public void init(){
        jsonMapper = new JacksonJsonpMapper();
        restClient = RestClient.builder(
                new HttpHost("localhost",9200)).build();
        elasticsearchTransport =
                new RestClientTransport(restClient, jsonMapper);
        elasticsearchClient = new ElasticsearchClient(elasticsearchTransport);
    }
    /**
     * Encerra o transporte
     * @throws IOException
     */
    public void close() throws IOException {
        System.out.println("Encerrando o cliente..");
        elasticsearchTransport.close();
    }
    /**
     * Pega o cliente
     * @return
     */
    public ElasticsearchClient getElasticsearchClient(){
        return this.elasticsearchClient;
    }
}